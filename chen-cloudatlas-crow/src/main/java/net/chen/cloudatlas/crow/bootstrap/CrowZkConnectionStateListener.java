package net.chen.cloudatlas.crow.bootstrap;

import java.util.Arrays;
import java.util.List;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.client.ServiceRegistry;
import net.chen.cloudatlas.crow.client.impl.ServiceControllerImpl;
import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.DcType;
import net.chen.cloudatlas.crow.common.KeyUtil;
import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.common.utils.NameableServiceLoader;
import net.chen.cloudatlas.crow.common.utils.UrlUtil;
import net.chen.cloudatlas.crow.config.ConfigUtil;
import net.chen.cloudatlas.crow.config.CrowClientContext;
import net.chen.cloudatlas.crow.config.CrowConfig;
import net.chen.cloudatlas.crow.config.MonitorConfig;
import net.chen.cloudatlas.crow.config.ReferenceConfig;
import net.chen.cloudatlas.crow.config.RegistryConfig;
import net.chen.cloudatlas.crow.config.ServiceConfig;
import net.chen.cloudatlas.crow.manager.api.RegistryClient;
import net.chen.cloudatlas.crow.manager.api.RegistryConnectionState;
import net.chen.cloudatlas.crow.manager.api.RegistryConnectionStateListener;
import net.chen.cloudatlas.crow.manager.api.RegistryData;
import net.chen.cloudatlas.crow.manager.api.RegistryEvent;
import net.chen.cloudatlas.crow.manager.api.RegistryEventType;
import net.chen.cloudatlas.crow.manager.api.RegistryEventWatcher;
import net.chen.cloudatlas.crow.manager.api.RegistryFixDaemonService;
import net.chen.cloudatlas.crow.manager.api.RegistryLocalStore;
import net.chen.cloudatlas.crow.manager.api.RegistryManager;
import net.chen.cloudatlas.crow.manager.api.support.ServiceConsumer;
import net.chen.cloudatlas.crow.manager.api.support.ServiceProvider;
import net.chen.cloudatlas.crow.monitor.api.MonitorService;
import net.chen.cloudatlas.crow.remote.ChannelRegistry;
import net.chen.cloudatlas.crow.rpc.protocol.ReferenceGet;

/**
 * 
 * @author chenn
 *
 */
public class CrowZkConnectionStateListener implements RegistryConnectionStateListener{

	private final RegistryConfig registryConfig;
	
	private final List<ServiceConfig> serviceConfigs;
	
	private final List<ReferenceConfig> referenceConfigs;
	
	private final MonitorConfig monitorConfig;
	
	private final boolean connectOnNodeCreated;
	
	private final RegistryClient registryClient;
	
	private final RegistryLocalStore localStore;
	
	private final String applicationName;
	
	private final ClientSideBooter clientSideBooter;
	
	private final Bootstrap bootStrap;
	
	private Object lock = new Object();
	
	public CrowZkConnectionStateListener(
			CrowConfig config,
			RegistryClient registryClient,
			ClientSideBooter clientSideBooter,
			Bootstrap bootStrap,
			boolean connectOnNodeCreated){
		
		this (
				config.getRegistryConfig(),
				config.getServiceConfigList(),
				config.getReferenceConfigList(),
				config.getMonitorConfig(),
				bootStrap,
				registryClient,
				clientSideBooter,
				connectOnNodeCreated);
	}
	
	public CrowZkConnectionStateListener(
			RegistryConfig registryConfig, 
			List<ServiceConfig> serviceConfigList,
			List<ReferenceConfig> referenceConfigList, 
			MonitorConfig monitorConfig, 
			Bootstrap bootStrap,
			RegistryClient registryClient, 
			ClientSideBooter clientSideBooter, 
			boolean connectOnNodeCreated) {
		
		this.registryConfig = registryConfig;
		this.serviceConfigs = serviceConfigList;
		this.referenceConfigs = referenceConfigList;
		this.monitorConfig = monitorConfig;
		this.connectOnNodeCreated = connectOnNodeCreated;
		this.registryClient = registryClient;
		
		RegistryManager registryManager = NameableServiceLoader.getService(RegistryManager.class, registryConfig.getType());
		
		this.localStore = registryManager.getLocalStore();
		this.applicationName = CrowClientContext.getApplicationName();
		this.clientSideBooter = clientSideBooter;
		this.bootStrap = bootStrap;
	}
	
	@Override
	public void stateChanged(RegistryConnectionState newState) {
		
		if (RegistryConnectionState.CONNECTED.equals(newState)){
			connected();
		} else if (RegistryConnectionState.RECONNECTED.equals(newState)){
			reConnected();
		} else if (RegistryConnectionState.NO_CONNECTION_YET.equals(newState)){
			noConnection();
		} else if (RegistryConnectionState.LOST.equals(newState)){
			reConnected();
		}
	}
	
	@Override
	public void unregister() {
		
		RegistryFixDaemonService.stop();
		
		for (final ServiceConfig c : this.serviceConfigs){
			
			if (c.isValidServiceName()){
				try {
					String key = KeyUtil.getServiceKey(c.getRealServiceId(), c.getServiceVersion());
					registryClient.unregisterProvider(key, new DcType[]{c.getApplicationConfig().getDc()},
							new ServiceProvider(){

								@Override
								public ServiceConfig<?> getConfig() {
									return c;
								}
						
					});
				} catch (Exception e){
					Logger.error("unregister exception:",e);
				}
			}
		}
	}

	
	private void connected(){
		
		Logger.info("*****registry connected:******");
		
		register(registryClient, serviceConfigs);
		subscribe(registryClient, referenceConfigs, monitorConfig, registryConfig, connectOnNodeCreated);
		
		RegistryFixDaemonService.start(new Runnable(){

			@Override
			public void run() {
				
				checkAndRegister(registryClient, serviceConfigs);
			}
			
		});
	}
	
	private void reConnected(){
		
		Logger.info("*****registry connected:******");
		
		// 由于provider信息在zk中是ephemeral的，
		// 一旦重连，则node就会消失，provider需要重新注册一遍，
		// consumer需要重新watch一遍
		register(registryClient, serviceConfigs);
		subscribe(registryClient, referenceConfigs, monitorConfig, registryConfig, connectOnNodeCreated);
		
	}
	
	private void noConnection(){
		
		Logger.info("*****can not connect to registry, loading local persistent data******");
		
		// 如果连接不上zk，则加载本地存储的数据
		try {
			RegistryData localData = localStore.load(applicationName);
			if (null != localData){
				// 存储到context中
				CrowConfig old = CrowClientContext.getConfig();
				old.setReferenceConfigList(localData.getProviderConfig());
				old.setMonitorConfig(localData.getMonitorConfig());
				// 开始建立binary的链接
				bootStrap.startAsClient(old);
			}
		} catch (Exception e){
			Logger.error("error loading local persistent data!",e);
		}
		
	}
	
	private void checkAndRegister(RegistryClient registryClient, List<ServiceConfig> sConfigs){
		
		for (final ServiceConfig c : sConfigs){
			
			if (c.isValidServiceName()){
				try {
					String key = KeyUtil.getServiceKey(c.getRealServiceId(), c.getServiceVersion());
					boolean existProvider = registryClient.checkServiceProvider(key, c.getApplicationConfig().getDc(),
							new ServiceProvider(){

								@Override
								public ServiceConfig<?> getConfig() {
									return c;
								}
						
					});
					if (!existProvider){
						Logger.info("provider node " + c.getInterface() + " is missing, start fixing");
						singleRegister(registryClient, c);
						Logger.info("provider node " + c.getInterface() + " fixed.");
					}
				} catch (Exception e){
					Logger.error("check or fix provider node error");
				}
			}
		}
	}
	
	private void register(RegistryClient registryClient, List<ServiceConfig> sConfigs){
		
		for (final ServiceConfig c : sConfigs){
			
			if (c.isValidServiceName()){
				singleRegister(registryClient,c);
			}
		}
	}

	private void singleRegister(RegistryClient registryClient, final ServiceConfig sConfig){
		
		try {
			String key = KeyUtil.getServiceKey(sConfig.getRealServiceId(), sConfig.getServiceVersion());
			registryClient.registerProvider(key, new DcType[]{sConfig.getApplicationConfig().getDc()},
					new ServiceProvider(){

						@Override
						public ServiceConfig<?> getConfig() {
							return sConfig;
						}
				
			});
			registryClient.watchService(key, sConfig.getApplicationConfig().getDc(),
					new ServiceProvider(){

						@Override
						public ServiceConfig<?> getConfig() {
							return sConfig;
						}
				
			});
			registryClient.watchHostPort(key, sConfig.getApplicationConfig().getDc(), 
					new ServiceProvider(){

						@Override
						public ServiceConfig<?> getConfig() {
							return sConfig;
						}
				
			});
		} catch (Exception e){
			Logger.error("register " + sConfig.getRealServiceId() + " error!",e);
		}
	}
	
	/**
	 * 订阅
	 * @param registryClient
	 * @param rConfigs
	 * @param mConfig
	 * @param rConfig
	 * @param connectOnNodeCreated
	 */
	private void subscribe(
			RegistryClient registryClient, 
			final List<ReferenceConfig> referConfigs,
			final MonitorConfig mConfig,
			final RegistryConfig rgConfig,
			final boolean connectOnNodeCreated){
		
		if (null!=mConfig){
			subscribeMonitor(registryClient, mConfig, rgConfig, connectOnNodeCreated);
		}
		
		for (final ReferenceConfig config : referConfigs){
			
			if (!config.isValidServiceName()){
				continue;
			}
			
			final String serviceId = config.getRealServiceId();
			final String serviceVersion = config.getServiceVersion();
			final String serviceKey = KeyUtil.getServiceKey(serviceId, serviceVersion);
			final DcType[] dcs = config.getRealDc();
			
			for (final DcType dc : dcs){
				
				Logger.info("begin to subscribe {}@{}",serviceKey,dc);
				
				try {
					registryClient.watchProvider(serviceKey, dc, 
							new RegistryEventWatcher<ServiceConfig>(){

								@Override
								public void onEvent(RegistryEvent<ServiceConfig> event) {
									
									Logger.debug("({}) event: {}",serviceKey,event);
									
									RegistryEventType type = event.getType();
									
									if (	type.equals(RegistryEventType.NODE_CREATED)
											|| type.equals(RegistryEventType.NODE_DATA_UPDATE)){
										// 当zk重启时，NODE_DATA_UPDATA被触发，这时取最新的provider
										// 信息再做一遍。
										onEventNodeCreated(event, serviceId, serviceVersion, dc);
									} else if (type.equals(RegistryEventType.NODE_REMOVED)){
										onEventNodeRemoved(event, serviceId, serviceVersion, dc);
									}
									
								}

								@Override
								public Class<ServiceConfig> payloadClass() {
									return ServiceConfig.class;
								}
						
					});
					
					registryClient.watchService(serviceId, dc, 
							new ServiceConsumer(){

								@Override
								public ReferenceConfig<?> getConfig() {
									return config;
								}
						
					});
					
					Logger.info("begin to register consumer {}@{}",serviceKey,dc);
					
					// 注册consumer，crow console需要用到
					registryClient.registerConsumer(serviceId, new DcType[]{dc}, 
							new ServiceConsumer(){

								@Override
								public ReferenceConfig<?> getConfig() {
									return config;
								}
						
					});
					
				} catch (Exception e){
					Logger.error("subscribe [" + serviceKey + "]@[" + dc + "] error!",e);
				}
				
			}
		}	//---end for (final DcType dc : dcs)
	}
	
	private void subscribeMonitor(
			RegistryClient registryClient, 
			final MonitorConfig mConfig,
			final RegistryConfig registryConfig,
			final boolean connectOnNodeCreated){
		
		final ReferenceConfig rConfig = new ReferenceConfig();
		rConfig.setApplicationConfig(mConfig.getApplicationConfig());
		rConfig.setServiceId("MonitorService");
		rConfig.setInterfaceClass(MonitorService.class.getName());
		rConfig.setServiceVersion(Constants.DEFAULT_SERVICE_VERSION);
		
		final String serviceId = rConfig.getRealServiceId();		
		final String serviceKey = KeyUtil.getServiceKey(serviceId, Constants.DEFAULT_SERVICE_VERSION);
		// 考虑到灾备monitor需要两个中心都发
		final DcType[] dcs = new DcType[]{DcType.SHANGHAI, DcType.BEIJING};
		for (final DcType dc : dcs){
			
			Logger.info("begin to subscribe monitor {}@{}",serviceKey,dc);
			
			try {
				rConfig.setDc(dc);
				registryClient.watchProvider(serviceId, dc, 
						new RegistryEventWatcher<ServiceConfig>(){

							@Override
							public void onEvent(RegistryEvent<ServiceConfig> event) {
								
								Logger.debug("({}) event: ",serviceKey,event);
								
								RegistryEventType type = event.getType();
								
								if (	type.equals(RegistryEventType.NODE_CREATED)
										|| type.equals(RegistryEventType.NODE_DATA_UPDATE)){
									// 当zk重启时，NODE_DATA_UPDATA被触发，这时取最新的provider
									// 信息再做一遍。
									onEventNodeCreated(event, serviceId, Constants.DEFAULT_SERVICE_VERSION, dc);
								} else if (type.equals(RegistryEventType.NODE_REMOVED)){
									onEventNodeRemoved(event, serviceId, Constants.DEFAULT_SERVICE_VERSION, dc);
								}
							}

							@Override
							public Class<ServiceConfig> payloadClass() {
								return ServiceConfig.class;
							}
					
				});
				
				Logger.info("begin to register monitor consumer {}@{}",serviceKey,dc);
				
				// 注册consumer，crow console需要用到
				registryClient.registerConsumer(serviceId, new DcType[]{dc}, 
						new ServiceConsumer(){

							@Override
							public ReferenceConfig<?> getConfig() {
								return rConfig;
							}
					
				});
			} catch (Exception e){
				Logger.error("subscribe monitor [" + serviceKey + "]@[" + dc + "] error!",e);
			}
		}
	}
	
	private void onEventNodeRemoved(RegistryEvent<ServiceConfig> event, String serviceId, String serviceVersion, DcType dc){
		
		Logger.debug("provider [{}] is removed!",event.getPath());
		
		ServiceConfig removedProvider = event.getNodeData();
		String key = KeyUtil.getServiceKey(removedProvider.getRealServiceId(), removedProvider.getServiceVersion());
		ReferenceConfig rConfig;
		
		synchronized(lock){
			
			String ipAndPort = UrlUtil.getUrl(removedProvider.getProtocol().getIp(), removedProvider.getProtocol().getPort());
			if (ChannelRegistry.isChannelAvailable(ipAndPort)){
				Logger.info("the channel [{}] is alive and in availableChannels, will skip onEventNodeRemoved.",ipAndPort);
				return;
			}
			
			/**
			 * 当一台provider下线后,zk中的ephemeral node会等到session timeout后才会触发consumer端的watcher
			 * 而在此之前，该provider已被放入unavailableChannels中，这里需要把该
			 * unavailableChannels移除，不用retry了。因为provider既然下线，就没必要再试了。
			 * 如果后面provider有重启了，也会当作新的provider处理，没毛病。
			 */
			if (MonitorService.class.getName().equals(serviceId)){
				
				MonitorConfig.setModified(true);
				CrowClientContext.getConfig().getMonitorConfig().removeUrl(
						removedProvider.getApplicationConfig().getDc(), 
						removedProvider.getProtocol().getIp() + ":" + removedProvider.getProtocol().getPort());
				return;
			}
			
			rConfig = CrowClientContext.getReferenceConfig(serviceId, serviceVersion);
			
			Logger.debug("remove {} from unavailableChannels to stop retry.",ipAndPort);
			
			ChannelRegistry.stopRetryChannel(ipAndPort,false);
			
			// 通知invoker
			if (removedProvider.isRpc()){
				ReferenceGet referenceGet = ServiceRegistry.getReferenceGetMap().get(key);
				URL url = rConfig.buildURLFromService(removedProvider);
				referenceGet.deleteInvoker(url);
			} else {
				ServiceControllerImpl controller = (ServiceControllerImpl)ServiceRegistry.getControllerMap().get(key);
				if (null!=controller){
					URL url = rConfig.buildURLFromService(removedProvider);
					controller.deleteInvoker(url);
				}
			}
			
			removeProviderFromLocal(
					CrowClientContext.getReferenceConfig(serviceId, serviceVersion),
					dc,
					removedProvider);
			
			Logger.info("saving as local persistent data");
			
			try {
				localStore.save(new RegistryData(applicationName, CrowClientContext.getReferenceConfigList()));
			} catch (Exception e){
				Logger.error("error occurs while saving local persistent data!",e);
			}
		}
	}
	
	private void onEventNodeCreated(
			RegistryEvent<ServiceConfig> event,
			String serviceId,
			String serviceVersion,
			DcType dc){
		
		if (null == event.getPath()){
			Logger.warn("node path is null,skip!");
			return;
		}
		
		Logger.info("({}) found new provider [{}]",serviceId,event.getPath());
		
		ServiceConfig addedProvider = event.getNodeData();
		String key = KeyUtil.getServiceKey(addedProvider.getRealServiceId(),addedProvider.getServiceVersion());
		ReferenceConfig rConfig;
		
		synchronized(lock){
			// 如果时monitor，去更新原有的monitor url，尽量减少原有
			// 的monitor操作的修改
			if (MonitorService.class.getName().equals(serviceId)){
				
				MonitorConfig.setModified(true);
				CrowClientContext.getConfig().getMonitorConfig().addUrl(
						addedProvider.getApplicationConfig().getDc(),
						addedProvider.getProtocol().getIp() + ":" + addedProvider.getProtocol().getPort());
				
				Logger.info("saving info into local persistent store");
				
				try {
					localStore.save(new RegistryData(
							applicationName,
							CrowClientContext.getReferenceConfigList(),
							CrowClientContext.getConfig().getMonitorConfig()));
				} catch (Exception e){
					Logger.error("error occurs while saving local persistent data!",e);
				}
				
			} else {
				rConfig = CrowClientContext.getReferenceConfig(serviceId, serviceVersion);
				boolean dcStrategy = checkDcStrategy(rConfig, dc, key);
				rConfig.setDcStrategy(dc,dcStrategy);
				boolean isAdded = addNewProvidersToLocal(rConfig, dc, Arrays.asList(new ServiceConfig[]{addedProvider}));
				
				Logger.info("saving info into local persistent data!");
				
				try {
					localStore.save(new RegistryData(
							applicationName,
							CrowClientContext.getReferenceConfigList(),
							CrowClientContext.getConfig().getMonitorConfig()));
				} catch (Exception e){
					Logger.error("error occurs while saving local persistent data!",e);
				}
				
				// 通知invoker
				if (addedProvider.isRpc()){
					
					ReferenceGet referenceGet = ServiceRegistry.getReferenceGetMap().get(key);
					if (null == referenceGet){
						// 没有立即调用
						if (isAdded){
							ServiceRegistry.getReferenceGetMap().putIfAbsent(key, new ReferenceGet(rConfig));
							referenceGet = ServiceRegistry.getReferenceGetMap().get(key);
							referenceGet.get();
						} else {
							return ; // 初始化拉下来是status为0的服务
						}
					} else if (null != referenceGet.get()){
						URL url = rConfig.buildURLFromService(addedProvider);
						if (isAdded){
							referenceGet.insertInvoker(url);
						} else {
							// 立即调用，且初始化，
							// 并且拉下来的是一台status为0的服务，
							// 忽略此次 onEventNodeCreated
							referenceGet.deleteInvoker(url);
							return;
						}
					} else {
						return;
					}
				} else {
					
					if (connectOnNodeCreated && null!=clientSideBooter){
						clientSideBooter.connect(ConfigUtil.getBinaryReferenceUrls(this.referenceConfigs));
					}
					
					ServiceControllerImpl controller = (ServiceControllerImpl)ServiceRegistry.getControllerMap().get(key);
					URL url = rConfig.buildURLFromService(addedProvider);
					if (isAdded){
						if (null == controller){
							ServiceRegistry.getControllerMap().putIfAbsent(key, new ServiceControllerImpl(serviceId,serviceVersion));
							controller = (ServiceControllerImpl)ServiceRegistry.getControllerMap().get(key);
						}
						controller.insertInvoker(url);
					} else {
						if (null != controller){
							controller.deleteInvoker(url);
						}
						return;
					}
				}	// --end if (addedProvider.isRpc())
				
				// 此处需要先check一下策略，如果拉下来的服务在dc策略中是不可用的，
				// 则不允许countDown，让应用等到策略可用的服务
				if (dcStrategy){
					ServiceRegistry.countDown(key);
				}
			}	// --end if (MonitorService.class.getName().equals(serviceId))
		}
	}
	
	private synchronized boolean addNewProvidersToLocal(ReferenceConfig rConfig, DcType dc, List<ServiceConfig> newProviders){
		
		if (null == rConfig){
			Logger.debug("rConfig is null, ignore it.");
			return false;
		}
		
		if (newProviders.size() == 0){
			Logger.warn("size of newProviders from registry is 0, skip...");
			return false;
		}
		
		Logger.debug("start merging config info ..");
		
		// 如果providers相互配置还一样的话，那么应该报错！
		// providers之间除了ip和port应该不一样之外，其他的都应该一样
		// 取第一个provider的共用信息
		ServiceConfig sConfig = newProviders.get(0);
		
		rConfig.getApplicationConfig().setHeartbeatInterval(sConfig.getProtocol().getApplicationConfig().getHeartbeatInterval());
		rConfig.getProtocol().setCodec(sConfig.getProtocol().getCodec());
		rConfig.getProtocol().setVersion(sConfig.getProtocol().getVersion());
		rConfig.getProtocol().setSerializationType(sConfig.getProtocol().getSerializationType());
		rConfig.getProtocol().setCompressAlgorithm(sConfig.getProtocol().getCompressAlgorithm());
		
		if (sConfig.getStatus() == 0){
			removeProviderFromLocal(rConfig, dc, newProviders.get(0));
			return false;
		}
		
		if (sConfig.getProtocol().getHeartbeatInterval() == 0){
			rConfig.getProtocol().setHeartbeatInterval(rConfig.getApplicationConfig().getHeartbeatInterval());
		} else {
			rConfig.getProtocol().setHeartbeatInterval(sConfig.getProtocol().getHeartbeatInterval());
		}
		
		rConfig.getProtocol().setMaxMsgSize(sConfig.getProtocol().getMaxMsgSize());
		
		// consumer端如果想使用provider的timeout值，则配置timeout为-1即可
		if (rConfig.isServiceTimeout()){
			rConfig.setTimeout(sConfig.getTimeout());
		}
		
		rConfig.setOneway(sConfig.isOneway());
		
		if (!CrowClientContext
				.isPasswordInitByRef(KeyUtil.getServiceKey(rConfig.getServiceId(), rConfig.getServiceVersion()))
				&& sConfig.getPassword() != null){
			rConfig.setPassword(sConfig.getPassword());
			CrowClientContext.setPasswordForRef(
					KeyUtil.getServiceKey(rConfig.getServiceId(), rConfig.getServiceVersion()),
					sConfig.getPassword());
		}
		
		// 更新 urls 和 weights,先假定只有sh和bj俩个组
		for (ServiceConfig sc : newProviders){
			
			String url = UrlUtil.getUrl(sc.getProtocol().getIp(),sc.getProtocol().getPort());
			List urls = (List)rConfig.getUrlGroupsMap().get(dc);
			
			if (!urls.contains(url)){
				urls.add(url);
				((List)rConfig.getWeightGroupsMap().get(dc)).add(sc.getWeight());
			} else {
				// 如果已包含该url，则说明是node update，用新的覆盖本地
				// 先把url与weight remove掉，然后再add进来，就像全新的一样
				int index = urls.indexOf(url);
				urls.remove(index);
				List weights = (List)rConfig.getWeightGroupsMap().get(dc);
				weights.remove(index);
				urls.add(url);
				weights.add(sc.getWeight());
			}
		}
		
		rConfig.setUrlsModified(true);
		
		return true;
	}
	
	/**
	 * 从本地移除指定provider的信息
	 * @param rConfig
	 * @param dc
	 * @param removedProvider
	 */
	private void removeProviderFromLocal(ReferenceConfig rConfig, DcType dc, ServiceConfig removedProvider){
		
		String url = UrlUtil.getUrl(removedProvider.getProtocol().getIp(),removedProvider.getProtocol().getPort());
		List list = (List)rConfig.getUrlGroupsMap().get(dc);
		int index = list.indexOf(url);
		if (-1==index){
			return;
		}
		list.remove(index);
		((List)rConfig.getWeightGroupsMap().get(dc)).remove(index);
		rConfig.setUrlsModified(true);
	}
	
	public Boolean checkDcStrategy(ReferenceConfig rConfig, DcType dc, String serviceKey){
		
		// 先拉取服务端的配置，如果为0，则check本地的dcStrategy
		int strategy = this.registryClient.checkDcStrategy(dc, serviceKey);
		if (strategy == 0){
			return (Boolean)rConfig.getDcStrategy().get(dc);
		} else {
			return strategy == 1;
		}
	}
	
}
