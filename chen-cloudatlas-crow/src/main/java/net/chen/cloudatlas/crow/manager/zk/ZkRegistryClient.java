package net.chen.cloudatlas.crow.manager.zk;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.transaction.CuratorTransaction;
import org.apache.curator.framework.api.transaction.CuratorTransactionFinal;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.tinylog.Logger;

import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.DcType;
import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.config.CrowServerContext;
import net.chen.cloudatlas.crow.config.ReferenceConfig;
import net.chen.cloudatlas.crow.config.ServiceBaseConfig;
import net.chen.cloudatlas.crow.config.ServiceConfig;
import net.chen.cloudatlas.crow.config.utils.ServiceConfigQueue;
import net.chen.cloudatlas.crow.manager.api.RegistryClient;
import net.chen.cloudatlas.crow.manager.api.RegistryConnectionState;
import net.chen.cloudatlas.crow.manager.api.RegistryConnectionStateListener;
import net.chen.cloudatlas.crow.manager.api.RegistryEvent;
import net.chen.cloudatlas.crow.manager.api.RegistryEventType;
import net.chen.cloudatlas.crow.manager.api.RegistryEventWatcher;
import net.chen.cloudatlas.crow.manager.api.support.CommandContext;
import net.chen.cloudatlas.crow.manager.api.support.CommandType;
import net.chen.cloudatlas.crow.manager.api.support.RegistryCommandExecutor;
import net.chen.cloudatlas.crow.manager.api.support.ServiceConsumer;
import net.chen.cloudatlas.crow.manager.api.support.ServiceProvider;

/**
 * RegistryClient的ZooKeeper实现<br>
 * <font color=red>未完成</font>
 * @author chenn
 *
 */
public class ZkRegistryClient implements RegistryClient{

	private CuratorFramework client;
	
	private final URL url;
	
	private final Queue<ServiceConfig> crowConfigQueue = ServiceConfigQueue.getInstance();
	
	private volatile RegistryConnectionState connState = RegistryConnectionState.LOST;
	
	private ConcurrentMap<String, PathChildrenCache> providerCacheMap = new ConcurrentHashMap<>();
	
	private ConcurrentMap<String, PathChildrenCache> consumerCacheMap = new ConcurrentHashMap<>();
	
	private ConcurrentMap<String, NodeCache> commandCacheMap = new ConcurrentHashMap<>();
	
	private ConcurrentMap<String, NodeCache> serviceCacheMap = new ConcurrentHashMap<>();
	
	private ConcurrentMap<String, NodeCache> hostportCacheMap = new ConcurrentHashMap<>();
	
	private RegistryConnectionStateListener listener;
	
	public ZkRegistryClient(final URL url){
		super();
		this.url = url;
	}
	
	@Override
	public void start(RegistryConnectionStateListener listener){
		this.listener = listener;
		init(listener);
		Logger.info("zk client started");
	}

	@Override
	public void shutdown(){
		if (null != client){
			listener.unregister();
			client.close();
		}
		Logger.info("zk client has been shutted down ..");
	}

	@Override
	public RegistryConnectionState getState(){
		return connState;
	}

	@Override
	public void registerProvider(String serviceId, DcType[] dcs, ServiceProvider provider) throws Exception {
		
		checkStarted();
		
		for (DcType dc : dcs){
			CuratorTransaction transaction = client.inTransaction();
			ServiceConfig serviceConfig = provider.getConfig();
			ServiceConfig persistentServiceConfig = provider.getConfig();
			ServiceConfig backupServiceConfig = provider.getConfig();
			
			Logger.debug("registing service provider {} on dc: {}",serviceId,dc.getText());
			
			String providerNodeKey = ZkRegistryUtil.getProviderNodeKey(serviceConfig.getProtocol());
			
			final String path = ZkRegistryUtil.getProviderPath(dc, serviceId, providerNodeKey);
			final String servicePath = ZkRegistryUtil.getServicePath(dc, serviceId);
			
			String persistentProviderPath = ZkRegistryUtil.getPersistentProviderPath(dc, serviceId, providerNodeKey);
			String backupProviderPath = ZkRegistryUtil.getBackupProviderPath(dc, serviceId, providerNodeKey);
			
			/**
			 * step 1. 如果存在持久节点，则按照zk上的数据覆盖本地的配置
			 */
			if (	client.checkExists().forPath(persistentProviderPath) != null &&
					!serviceConfig.isLocal()){
				persistentServiceConfig = ZkRegistryUtil.deserializeNodeData(
						client.getData().forPath(persistentProviderPath),
						ServiceConfig.class);
				
				serviceConfig.setStatus(persistentServiceConfig.getStatus());
				serviceConfig.setWeight(persistentServiceConfig.getWeight());
				serviceConfig.setThrottleType(persistentServiceConfig.getThrottleType());
				serviceConfig.setThrottleValue(persistentServiceConfig.getThrottleValue());
			} else if (client.checkExists().forPath(persistentProviderPath) == null){
				try {
					transaction = client.inTransaction();
					String persistentProviderParentPath = ZkRegistryUtil.getPersistentProviderParentPath(dc,serviceId);
					transaction = createParentPathTransactions(persistentProviderParentPath,transaction,client)
							.create().withMode(CreateMode.PERSISTENT)
							.forPath(persistentProviderPath,ZkRegistryUtil.serializeNodeData(serviceConfig))
							.and();
					if (transaction instanceof CuratorTransactionFinal){
						((CuratorTransactionFinal)transaction).commit();
					}
				} catch (Exception ex){
					Logger.error(ex);
				}
			}
			// 先将可能超时的节点删除
			Logger.debug("deleting expire provicer zk node {}", path);
			if (client.checkExists().forPath(path) != null){
				transaction = (CuratorTransaction)transaction.delete().forPath(path);
				if (transaction instanceof CuratorTransactionFinal){
					((CuratorTransactionFinal)transaction).commit();
				}
			}
			
			/**
			 * step 2. 再建立新的节点
			 */
			try {
				transaction = client.inTransaction();
				Logger.debug("creating new provider zk node {}",path);
				// 节点不存在，先生成父路径的create事务，再生成子节点create事务
				final String parentPath = ZkRegistryUtil.getProviderParentPath(dc, serviceId);
				transaction = createParentPathTransactions(parentPath,transaction,client)
						.create().withMode(CreateMode.EPHEMERAL)
						.forPath(path,ZkRegistryUtil.serializeNodeData(serviceConfig))
						.and();
				if (transaction instanceof CuratorTransactionFinal){
					((CuratorTransactionFinal)transaction).commit();
				}
			} catch(Exception ex){
				Logger.error(ex);
			}
			
			/**
			 * step 3. 写入appName到serviceId的节点（理论上，该service节点一定存在
			 */
			if (client.checkExists().forPath(servicePath) != null){
				byte[] data = client.getData().forPath(servicePath);
				ServiceBaseConfig sc;
				if (null == data || data.length == 0){
					sc = null;
				} else {
					try {
						sc = ZkRegistryUtil.deserializeNodeData(data, ServiceBaseConfig.class);
					} catch (Exception e){
						sc = null;
						Logger.warn("exception occurs wile deserializeNodeData from service node,will rewrite the node data");
					}
				}
				if (null != sc){
					provider.getConfig().setIpWhiteList(sc.getIpWhiteList());
					provider.getConfig().setIpBlackList(sc.getIpBlackList());
					provider.getConfig().setDcStrategy(sc.getDcOn());
					if (	!sc.getAppName().equals(serviceConfig.getApplicationConfig().getName()) ||
							!sc.getContact().equals(serviceConfig.getApplicationConfig().getConteact())){
						sc.setAppName(serviceConfig.getApplicationConfig().getName());
						sc.setContact(serviceConfig.getApplicationConfig().getConteact());
						client.setData().forPath(servicePath, ZkRegistryUtil.serializeNodeData(sc));
					}
				} else {
					sc = new ServiceBaseConfig();
					sc.setAppName(serviceConfig.getApplicationConfig().getName());
					sc.setContact(serviceConfig.getApplicationConfig().getConteact());
					sc.setDcOn(serviceConfig.getDcStrategy());
					sc.setIpWhiteList(serviceConfig.getIpWhiteList());
					sc.setIpBlackList(serviceConfig.getIpBlackList());
					client.setData().forPath(servicePath, ZkRegistryUtil.serializeNodeData(sc));
				}
			}
			
			/**
			 * step 4. 最后写入backup节点
			 */
			// 先将可能超时的节点删除
			transaction = client.inTransaction();
			Logger.debug("deleting expire provicer zk node {}", path);
			if (client.checkExists().forPath(backupProviderPath) != null){
				transaction = (CuratorTransaction)transaction.delete().forPath(backupProviderPath);
				if (transaction instanceof CuratorTransactionFinal){
					((CuratorTransactionFinal)transaction).commit();
				}
			}
			// 再建立新的节点
			try {
				transaction = client.inTransaction();
				Logger.debug("creating new backup zk node {}",backupProviderPath);
				// 节点不存在，先生成父路径的create事务，再生成子节点create事务
				final String backParentPath = ZkRegistryUtil.getProviderParentPath(dc, serviceId);
				transaction = createParentPathTransactions(backParentPath,transaction,client)
						.create().withMode(CreateMode.EPHEMERAL)
						.forPath(path,ZkRegistryUtil.serializeNodeData(backupServiceConfig))
						.and();
				if (transaction instanceof CuratorTransactionFinal){
					((CuratorTransactionFinal)transaction).commit();
				}
			} catch (Exception ex){
				Logger.error(ex);
			}
		}
	}

	@Override
	public void unregisterProvider(String serviceId, DcType[] dcs, ServiceProvider provider) throws Exception {
		
		checkStarted();
		
		for (DcType dc : dcs){
			
			CuratorTransaction transaction = client.inTransaction();
			ServiceConfig serviceConfig = provider.getConfig();
			
			Logger.debug("unregistry service provider " + serviceId + " on dc: " + dc.getText());
			
			String providerNodeKey = ZkRegistryUtil.getProviderNodeKey(serviceConfig.getProtocol());
			final String path = ZkRegistryUtil.getProviderPath(dc, serviceId, providerNodeKey);
			
			if (client.checkExists().forPath(path) != null){
				transaction = (CuratorTransaction)transaction.delete().forPath(path);
				if (transaction instanceof CuratorTransactionFinal){
					((CuratorTransactionFinal) transaction).commit();
				}
			}
		}
	}

	@Override
	public Map<DcType, List<ServiceProvider>> fetchProviders(String serviceId, DcType[] dcs) throws Exception {
		
		checkStarted();
		
		Map<DcType, List<ServiceProvider>> result = new EnumMap<>(DcType.class);
		
		for (DcType dc : dcs){
			
			List<ServiceProvider> eachList = new ArrayList<ServiceProvider>();
			Logger.debug("fetch service provider " + serviceId + " on dc:" + dc.getText());
			final String path = ZkRegistryUtil.getProviderParentPath(dc, serviceId);
			if (client.checkExists().forPath(path) == null){
				Logger.warn("provider zk node " + path + " not found");
				continue;
			} else {
				for (String eachPath : client.getChildren().forPath(path)){
					final String eachFullPath = path + "/" +eachPath;
					Logger.debug("found provider zk node " + eachPath + ", fullPath=" + eachFullPath);
					final ServiceConfig<?> config = ZkRegistryUtil.deserializeNodeData(
							client.getData().forPath(eachFullPath),
							ServiceConfig.class);
					eachList.add(
							new ServiceProvider(){

								@Override
								public ServiceConfig<?> getConfig() {
									return config;
								}
								
							});
				}
			}
			result.put(dc, eachList);
		}
		
		return result;
	}

	@Override
	public <T> void watchProvider(String serviceId, DcType dc, RegistryEventWatcher<T> watcher) throws Exception {
		
		checkStarted();
		
		Logger.debug("set EventWatcher on service provider "+serviceId+" on dc:"+dc.getText());
		
		final String path = ZkRegistryUtil.getProviderParentPath(dc, serviceId);
		if (null == client.checkExists().forPath(path)){
			// 父节点不存在，创建一个空的父节点
			client.create()
				.creatingParentsIfNeeded()
				.forPath(path,ZkConst.ZK_EMPTY_NODE_DATA);
			Logger.debug("provider parent zk node "+path+" not found, creating ..");
		} else {
			// 节点存在
			Logger.debug("provider parent zk node "+path+"found.");
		}
		
		PathChildrenCache cache = providerCacheMap.get(path);
		if (null == cache){
			
			cache = new PathChildrenCache(client, path, true);
			// 初始化后，会收到事件PathChildrenEvent.Type#INITIALIZED
			cache.start(StartMode.POST_INITIALIZED_EVENT);
			cache.getListenable().addListener(new PathChildrenCacheListener(){

				@Override
				public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
					
					try {
						Logger.debug("provider child event type:"+event.getType());
						ChildData nodeData = event.getData();
						if (null != nodeData){
							Logger.debug("provider child event child path:"+nodeData.getPath());
						}
						List<ChildData> initData = event.getInitialData();
						if (null != initData){
							
							for (ChildData d : initData){
								Logger.debug("provider child event child init path:"+d.getPath());
							}
							
						}
						
						RegistryEvent<T> evt = new RegistryEvent<T>(ZkRegistryUtil.eventTypeConvert(event.getType()));		
						if (null != nodeData){
							evt.setPath(nodeData.getPath());
							evt.setNodeData(ZkRegistryUtil.deserializeNodeData(nodeData.getData(), watcher.payloadClass()));
						}
						evt.setDc(dc);
						
						if (PathChildrenCacheEvent.Type.INITIALIZED == event.getType() && null != initData){
							List<T> initChildren = new ArrayList<>();
							for (ChildData one : initData){
								initChildren.add(
										ZkRegistryUtil.deserializeNodeData(one.getData(), watcher.payloadClass()));
							}
							evt.setInitData(initChildren);
						}
						watcher.onEvent(evt);
					} catch (Exception e){
						Logger.error("provider childEvent error ",e);
						throw e;
					}
				}// end of PathChildrenCacheListener
				
			});
			providerCacheMap.put(path, cache);
			// end of addListener
		} else {
			Logger.warn("already watched on "+path+",ignoring it ..");
		}
	}

	@Override
	public void registerConsumer(String serviceId, DcType[] dcs, ServiceConsumer consumer) throws Exception {
		
		checkStarted();
		
		CuratorTransaction transaction = client.inTransaction();
		
		for (DcType dc : dcs){
			
			Logger.debug("registing service consumer "+serviceId+" on dc:"+dc.getText());
			
			final String path = ZkRegistryUtil.getConsumerPath(
					dc, 
					serviceId, 
					ZkRegistryUtil.getConsumerNodeKey(consumer.getConfig().getProtocol()));
			
			Logger.debug("deleting expire consumer zk node "+path);
			
			if (client.checkExists().forPath(path) != null){
				transaction = (CuratorTransaction)transaction.delete().forPath(path);
			}
			
			if (transaction instanceof CuratorTransactionFinal){
				((CuratorTransactionFinal)transaction).commit();
			}
			
			Logger.debug("creating new consumer zk node "+path);
			
			transaction = client.inTransaction();
			// 节点不存在，先生成父路径的create事务，再生成子节点create事务
			final String parentPath = ZkRegistryUtil.getConsumerParentPath(dc, serviceId);
			transaction = createParentPathTransactions(parentPath,transaction,client)
					.create().withMode(CreateMode.EPHEMERAL)
					.forPath(path, ZkRegistryUtil.serializeNodeData(consumer.getConfig()))
					.and();
			
		}
		
		// 提交事务
		if (transaction instanceof CuratorTransactionFinal){
			((CuratorTransactionFinal)transaction).commit();
		}
	}

	@Override
	public Map<DcType, List<ServiceConsumer>> fetchConsumers(String serviceId, DcType[] dcs) throws Exception {
		
		checkStarted();
		
		Map<DcType, List<ServiceConsumer>> result = new EnumMap<DcType,List<ServiceConsumer>>(DcType.class);
		for (DcType dc : dcs){
			List<ServiceConsumer> eachList = new ArrayList<>();
			Logger.debug("fetch service consumer "+serviceId+" on dc:"+dc.getText());
			final String path = ZkRegistryUtil.getConsumerParentPath(dc, serviceId);
			if (client.checkExists().forPath(path) == null){
				//节点存在
				Logger.warn("consumer zk node "+path+" not found");
				continue;
			} else {
				//节点不存在
				for (String eachPath : client.getChildren().forPath(path)){
					final String eachFullPath = path + "/" + eachPath;
					Logger.debug("found provider zk node " + eachPath + ",full path="+eachFullPath);
					final ReferenceConfig<?> config = ZkRegistryUtil.deserializeNodeData(
							client.getData().forPath(eachFullPath), 
							ReferenceConfig.class);
					eachList.add(new ServiceConsumer(){

						@Override
						public ReferenceConfig<?> getConfig() {
							return config;
						}
						
					});
				}
			}
			result.put(dc, eachList);
		}
		return result;
	}

	@Override
	public <T> void watchConsumer(String serviceId, DcType dc, RegistryEventWatcher<T> watcher) throws Exception {
		
		checkStarted();
		
		Logger.debug("set EventWatcher on service consumer "+serviceId+" on dc:"+dc.getText());
		
		final String path = ZkRegistryUtil.getConsumerParentPath(dc, serviceId);
		if (client.checkExists().forPath(path) == null){
			//父节点不存在，创建一个空的父节点
			client.create().creatingParentsIfNeeded().forPath(path,ZkConst.ZK_EMPTY_NODE_DATA);
			Logger.debug("consumer parent zk node "+path+" not found, creating ..");
		} else {
			//节点存在
			Logger.debug("consumer parent zk node "+path+" found");
		}
		
		PathChildrenCache cache = consumerCacheMap.get(path);
		if (null == cache){
			
			cache = new PathChildrenCache(client, path, true);
			// 初始化后，会收到事件PathChildrenCacheEvent.Type#INITIALIZED
			cache.start(StartMode.POST_INITIALIZED_EVENT);
			
			cache.getListenable().addListener(new PathChildrenCacheListener(){

				@Override
				public void childEvent(CuratorFramework client, PathChildrenCacheEvent cacheEvent) throws Exception {
					try {
						Logger.debug("consumer child event type: " + cacheEvent.getType());
						
						ChildData nodeData = cacheEvent.getData();
						if (null != nodeData){
							Logger.debug("concumer child event child path: "+nodeData.getPath());
						}
						
						List<ChildData> initData = cacheEvent.getInitialData();
						if (null != initData){
							for (ChildData one : initData){
								Logger.debug("consumer child event child init path:"+one.getPath());
							}
						}
						
						RegistryEvent<T> event = new RegistryEvent<T>(ZkRegistryUtil.eventTypeConvert(cacheEvent.getType()));
						
						if (null != nodeData){
							event.setPath(nodeData.getPath());
							event.setNodeData(ZkRegistryUtil.deserializeNodeData(nodeData.getData(), watcher.payloadClass()));
						}
						
						event.setDc(dc);
						
						if (null != initData && PathChildrenCacheEvent.Type.INITIALIZED == cacheEvent.getType()){
							List<T> initChildren = new ArrayList<>();
							for (ChildData one : initData){
								initChildren.add(
										ZkRegistryUtil.deserializeNodeData(one.getData(), watcher.payloadClass()));
							}
							event.setInitData(initChildren);
						}
						
						watcher.onEvent(event);
					} catch (Exception e){
						Logger.error("consumer childEvent error ",e);
						throw e;
					}
				}
				
			});
			
			consumerCacheMap.put(path, cache);
		} else {
			Logger.warn("already watched on "+path+",ignoring it ..");
		}
	}

	@Override
	public <T> void watchService(String serviceId, final DcType dc, final ServiceProvider provider) throws Exception {
		
		checkStarted();
		
		Logger.debug("set EventWatcher on service "+serviceId+" on dc:"+dc.getText());
		
		final String path = ZkRegistryUtil.getServicePath(dc, serviceId);
		
		if (client.checkExists().forPath(path) == null){
			//父节点不存在，创建一个空的父节点
			client.create().creatingParentsIfNeeded().forPath(path, ZkConst.ZK_EMPTY_NODE_DATA);
			Logger.debug("service zk node "+path+" not found, creating ..");
		} else {
			//节点存在
			Logger.debug("service zk node "+path+" found");
		}
		
		NodeCache cache = serviceCacheMap.get(path);
		if (null == cache){
			
			final NodeCache newCache = new NodeCache(client, path);
			newCache.start();
			newCache.getListenable().addListener(new NodeCacheListener(){

				@Override
				public void nodeChanged() throws Exception {
					try {
						ChildData nodeData = newCache.getCurrentData();
						
						Logger.debug("service node changed: "+nodeData);
						
						if (null != nodeData){
							Logger.debug("service node path:"+nodeData.getPath());
							if (null != nodeData && null != nodeData.getData()){
								byte[] dataBytes = nodeData.getData();
								ServiceBaseConfig config = ZkRegistryUtil
										.deserializeNodeData(dataBytes, ServiceBaseConfig.class);
								if (null == config){
									return ;
								}
								provider.getConfig().setIpBlackList(config.getIpBlackList());
								provider.getConfig().setIpWhiteList(config.getIpWhiteList());
								provider.getConfig().setDcStrategy(config.getDcOn());
								
								Logger.debug("service node data:"+new String(dataBytes));
							}
						}
					} catch (Exception e){
						Logger.error("service nodeChanged error ",e);
						throw e;
					}
				}
				
			});
			
			serviceCacheMap.put(path, newCache);
			cache = newCache;
			
			Logger.debug("EventWatcher on service "+serviceId+" on dc:"+dc.getText()+" has been set");
		}
	}

	@Override
	public <T> void watchService(String serviceId, final DcType dc, final ServiceConsumer consumer) throws Exception {
		
		checkStarted();
		
		Logger.debug("set EventWatcher on service "+serviceId+" on dc:"+dc.getText());
		
		final String path = ZkRegistryUtil.getServicePath(dc, serviceId);
		
		if (client.checkExists().forPath(path) == null){
			//父节点不存在，创建一个空的父节点
			client.create().creatingParentsIfNeeded().forPath(path, ZkConst.ZK_EMPTY_NODE_DATA);
			Logger.debug("service zk node "+path+" not found, creating ..");
		} else {
			//节点存在
			Logger.debug("service zk node "+path+" found");
		}
		
		NodeCache cache = serviceCacheMap.get(path);
		if (null == cache){
			
			final NodeCache newCache = new NodeCache(client, path);
			newCache.start();
			newCache.getListenable().addListener(new NodeCacheListener(){

				@Override
				public void nodeChanged() throws Exception {
					try {
						ChildData nodeData = newCache.getCurrentData();
						
						Logger.debug("service node changed: "+nodeData);
						
						if (null != nodeData){
							Logger.debug("service node path:"+nodeData.getPath());
							if (null != nodeData && null != nodeData.getData()){
								byte[] dataBytes = nodeData.getData();
								ServiceBaseConfig config = null;
								try {
									config = ZkRegistryUtil
											.deserializeNodeData(dataBytes, ServiceBaseConfig.class);
								} catch (Exception ec){
									Logger.error(ec);
									config = new ServiceBaseConfig();
								}
								if (null == config){
									config = new ServiceBaseConfig();
								}
								
								consumer.getConfig().setDcStrategy(dc, config.getDcOn());
								
								Logger.debug("service node data:"+new String(dataBytes));
							}
						}
					} catch (Exception e){
						Logger.error("service nodeChanged error ",e);
						throw e;
					}
				}
				
			});
			
			serviceCacheMap.put(path, newCache);
			cache = newCache;
			
			Logger.debug("EventWatcher on service "+serviceId+" on dc:"+dc.getText()+" has been set");
			
		}
	}

	@Override
	public <T> void watchHostPort(String serviceId, DcType dc, final ServiceProvider provider) throws Exception {
		
		checkStarted();
		
		final ServiceConfig<?> sConfig = provider.getConfig();
		String providerNodeKey = ZkRegistryUtil.getProviderNodeKey(sConfig.getProtocol());
		final String path = ZkRegistryUtil.getProviderPath(dc, serviceId, providerNodeKey);
		
		Logger.debug("set EventWatcher on host-port "+providerNodeKey+" on dc:"+dc.getText());
		
		if (client.checkExists().forPath(path) == null){
			//host-port节点不存在，尝试创建新节点
			client.create().withMode(CreateMode.EPHEMERAL).forPath(
					path,
					ZkRegistryUtil.serializeNodeData(sConfig));
			Logger.debug("host-port node "+path+" not found, creating ..");
		} else {
			// 节点存在
			Logger.debug("host-port node "+path+" found.");
		}
		
		NodeCache cache = hostportCacheMap.get(path);
		if (null == cache){
			
			final NodeCache newCache = new NodeCache(client, path);
			newCache.start();
			newCache.getListenable().addListener(new NodeCacheListener(){

				@Override
				public void nodeChanged() throws Exception {
					try {
						ChildData nodeData = newCache.getCurrentData();
						
						Logger.debug("host-port node changed: "+nodeData);
						
						if (null != nodeData){
							Logger.debug("host-port node path:"+nodeData.getPath());
							byte[] dataBytes = nodeData.getData();
							ServiceConfig<?> tmpConfig = ZkRegistryUtil.deserializeNodeData(dataBytes, ServiceConfig.class);
							sConfig.setStatus(tmpConfig.getStatus());
							sConfig.setWeight(tmpConfig.getWeight());
							sConfig.setThrottleType(tmpConfig.getThrottleType());
							sConfig.setThrottleValue(tmpConfig.getThrottleValue());
							
							if (tmpConfig.getThrottleValue() > 0){
								CrowServerContext.setThrottleOpen(true);
							}
							
							crowConfigQueue.remove(sConfig);
							crowConfigQueue.add(sConfig);
							
							Logger.debug("host-port node data:"+new String(dataBytes,"UTF-8"));
						}
					} catch (Exception e){
						Logger.error("host-port nodeChanged error ",e);
						throw e;
					}
				}
				
			});
			
			hostportCacheMap.put(path, newCache);
			cache = newCache;
			
			Logger.debug("EventWatcher on service "+providerNodeKey+" on dc:"+dc.getText()+" has been set");
			
		}		
		
	}

	@Override
	public boolean checkServiceProvider(String serviceId, DcType dc, ServiceProvider provider) throws Exception {
		
		ServiceConfig serviceConfig = provider.getConfig();
		String providerNodeKey = ZkRegistryUtil.getProviderNodeKey(serviceConfig.getProtocol());
		final String path = ZkRegistryUtil.getProviderPath(dc, serviceId, providerNodeKey);
		return (client.checkExists().forPath(path) != null);
	}

	@Override
	public <T> void watchCommand(String serviceId, DcType[] dcs, CommandType commandType,
			RegistryCommandExecutor<T> executor) throws Exception {
		
		try {
			checkStarted();
			
			for (DcType dc :dcs){
				
				Logger.debug("set EventWatcher on service command "+commandType.name()+" of service "
						+ serviceId + " on dc:" + dc.getText());
				
				final String path = ZkRegistryUtil.getCommandPath(dc, serviceId, commandType);
				
				if (client.checkExists().forPath(path) == null){
					//父节点不存在，尝试创建空的父节点
					client.create().creatingParentsIfNeeded().forPath(path,ZkConst.ZK_EMPTY_NODE_DATA);
					Logger.debug("command parent zk node "+path+" not found, creating ..");
				} else {
					//父节点存在
					Logger.debug("command parent zk node "+path+" found");
				}
				
				NodeCache cache = commandCacheMap.get(path);
				if (null == cache){
					final NodeCache newCache = new NodeCache(client,path);
					newCache.start();
					newCache.getListenable().addListener(new NodeCacheListener(){

						@Override
						public void nodeChanged() throws Exception {
							try {
								ChildData nodeData = newCache.getCurrentData();
								
								Logger.debug("command node changed: "+nodeData);
								
								if (null != nodeData){
									Logger.debug("command node path:"+nodeData.getPath());
									byte[] dataBytes = nodeData.getData();
									Logger.debug("host-port node data:"+new String(dataBytes,"UTF-8"));
								}
								
								CommandContext<T> context = new CommandContext<T>(RegistryEventType.NODE_DATA_UPDATE);
								
								if (null != nodeData){
									context.setPath(nodeData.getPath());
									context.setData(
											ZkRegistryUtil.deserializeNodeData(nodeData.getData(),executor.contextDataClass()));
								}
								
								executor.execute(context);
							} catch (Exception e){
								Logger.error("command nodeChanged error ",e);
								throw e;
							}
						}
						
					});
					
					commandCacheMap.put(path, newCache);
					cache = newCache;
					
					Logger.debug("EventWatcher on service command "+commandType.name()+" of service "
							+ serviceId + " on dc:"+dc.getText()+" has been set");
				} else {
					Logger.warn("already watched on "+path+",ignoring it ..");
				}
				
			}
		} catch (Exception e){
			Logger.error("watch command error ",e);
			throw e;
		}
		
	}

	@Override
	public int checkDcStrategy(DcType dc, String serviceKey) {
		
		try {
			final String servicePath = ZkRegistryUtil.getServicePath(dc, serviceKey);
			byte[] data = client.getData().forPath(servicePath);
			ServiceBaseConfig sc = ZkRegistryUtil.deserializeNodeData(data, ServiceBaseConfig.class);
			return sc.getDcOn();
		} catch (Exception e){
			Logger.error("error on ServiceBaseConfig found for "+serviceKey);
			return 0;
		}
	}
	
	private void checkStarted() throws Exception {
		if (null == client || !(CuratorFrameworkState.STARTED.equals(client.getState()))){
			Logger.error("zk client has not been started yet!");
			throw new Exception("zk client has not been started yet!");
		}
	}

	private CuratorTransaction createParentPathTransactions(String parentPath,
			CuratorTransaction orgTrans, CuratorFramework client) throws Exception {
		
		String[] parentNodes = ZkRegistryUtil.nodePathArray(parentPath);
		CuratorTransaction result = orgTrans;
		for (int i=0; i<parentNodes.length; i++){
			if (null == client.checkExists().forPath(parentNodes[i])){
				result = result.create().forPath(parentNodes[i], null).and();
			}
		}
		
		return result;
	}
	
	/**
	 * 初始化ZK客户端
	 * @param lisenter
	 */
	private void init(final RegistryConnectionStateListener lisenter){
		
		client = CuratorFrameworkFactory.newClient(
				url.getParameter(Constants.ADDRESS),
				url.getPositiveParameter(Constants.SESSION_TIMEOUT_MS, Constants.DEFAULT_REGISTRY_SESSION_TIMEOUT_MS),
				url.getPositiveParameter(Constants.SESSION_TIMEOUT_MS, Constants.DEFAULT_REGISTRY_CONNECTION_TIMEOUT_MS),
				new RetryNTimes(Integer.MAX_VALUE,Constants.DEFAULT_REGISTRY_SLEEP_MS_BETWEEN_RETRIES));
		
		client.getConnectionStateListenable().addListener(new ConnectionStateListener(){

			@Override
			public void stateChanged(CuratorFramework client, ConnectionState newState) {
				
				if (ConnectionState.LOST == newState){
					Logger.warn("lost connection to zk registry: {}",
							client.getZookeeperClient().getCurrentConnectionString());
				}
				connState = ZkRegistryUtil.stateConvert(newState);
				listener.stateChanged(connState);
			}
			
		});
		
		client.start();
		
		int blockMs = url.getPositiveParameter(
				Constants.REGISTRY_CONNECTION_BLOCK_TIMEOUT,
				Constants.REGISTRY_CONNECTION_BLOCK);
		
		boolean connected = false;
		try {
			Logger.debug("start blocking " + blockMs + "ms for connection to "
					+ client.getZookeeperClient().getCurrentConnectionString());
			connected = client.blockUntilConnected(blockMs, TimeUnit.MILLISECONDS);
			Logger.debug("end blocking for connection to " + client.getZookeeperClient().getCurrentConnectionString());
		} catch (InterruptedException e){
			Logger.error("connection blocking is interrupted!",e);
		}
		
		if (!connected){
			Logger.warn("connection not established in " + blockMs + "ms, trigger ConnectionStateListener");
			this.connState = RegistryConnectionState.NO_CONNECTION_YET;
			listener.stateChanged(RegistryConnectionState.NO_CONNECTION_YET);
		}
	}
}
