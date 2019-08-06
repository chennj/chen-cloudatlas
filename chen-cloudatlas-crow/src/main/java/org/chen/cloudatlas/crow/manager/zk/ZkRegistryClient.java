package org.chen.cloudatlas.crow.manager.zk;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.chen.cloudatlas.crow.common.DcType;
import org.chen.cloudatlas.crow.common.URL;
import org.chen.cloudatlas.crow.config.ServiceConfig;
import org.chen.cloudatlas.crow.config.utils.ServiceConfigQueue;
import org.chen.cloudatlas.crow.manager.api.RegistryClient;
import org.chen.cloudatlas.crow.manager.api.RegistryConnectionState;
import org.chen.cloudatlas.crow.manager.api.RegistryConnectionStateListener;
import org.chen.cloudatlas.crow.manager.api.RegistryEventWatcher;
import org.chen.cloudatlas.crow.manager.api.support.CommandType;
import org.chen.cloudatlas.crow.manager.api.support.RegistryCommandExecutor;
import org.chen.cloudatlas.crow.manager.api.support.ServiceConsumer;
import org.chen.cloudatlas.crow.manager.api.support.ServiceProvider;

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
	public void start(RegistryConnectionStateListener listener) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdown() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public RegistryConnectionState getState() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void registerProvider(String serviceId, DcType[] dcs, ServiceProvider provider) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unregisterProvider(String serviceId, DcType[] dcs, ServiceProvider provider) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<DcType, List<ServiceProvider>> fetchProviders(String serviceId, DcType[] dcs) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void watchProvider(String serviceId, DcType dc, RegistryEventWatcher<T> watcher) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registerConsumer(String serviceId, DcType[] dcs, ServiceConsumer consumer) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<DcType, List<ServiceConsumer>> fetchConsumers(String serviceId, DcType[] dcs) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void watchConsumer(String serviceId, DcType dc, RegistryEventWatcher<T> watcher) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> void watchService(String serviceId, DcType dc, ServiceProvider provider) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> void watchService(String serviceId, DcType dc, ServiceConsumer consumer) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> void watchHostPort(String serviceId, DcType dc, ServiceProvider provider) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean checkServiceProvider(String serviceId, DcType dc, ServiceProvider provider) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T> void watchCommand(String serviceId, DcType[] dcs, CommandType commandType,
			RegistryCommandExecutor<T> executor) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int checkDcStrategy(DcType dc, String serviceKey) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

}
