package org.chen.cloudatlas.crow.manager.api;

import java.util.List;
import java.util.Map;

import org.chen.cloudatlas.crow.common.DcType;
import org.chen.cloudatlas.crow.manager.api.support.CommandType;
import org.chen.cloudatlas.crow.manager.api.support.RegistryCommandExecutor;
import org.chen.cloudatlas.crow.manager.api.support.ServiceConsumer;
import org.chen.cloudatlas.crow.manager.api.support.ServiceProvider;

/**
 * 配置管理器<br>
 * @author chenn
 *
 */
public interface RegistryClient {

	/**
	 * 启用<br>
	 * @param listener
	 */
	void start(RegistryConnectionStateListener listener);
	
	/**
	 * 停用<br>
	 */
	void shutdown();
	
	/**
	 * 获取连接状态<br>
	 * @see org.chen.cloudatlas.crow.manager.api.RegistryConnectionState
	 * @return
	 */
	RegistryConnectionState getState();
	
	/**
	 * 注册提供者(provider)<br>
	 * @param serviceId
	 * @param dcs 注册站点
	 * @param provider
	 * @throws Exception
	 */
	void registerProvider(final String serviceId, final DcType[] dcs, ServiceProvider provider) throws Exception;
	
	/**
	 * 移除提供者<br>
	 * @param serviceId
	 * @param dcs
	 * @param provider
	 * @throws Exception
	 */
	void unregisterProvider(final String serviceId, final DcType[] dcs, ServiceProvider provider) throws Exception;
	
	/**
	 * 获取服务的所有提供者（provider）<br>
	 * @param serviceId
	 * @param dcs
	 * @return
	 * @throws Exception
	 */
	Map<DcType, List<ServiceProvider>> fetchProviders(String serviceId, final DcType[] dcs) throws Exception;
	
	/**
	 * 监听服务提供者（provider）的配置变化事件<br>
	 * @param serviceId
	 * @param dc
	 * @param watcher
	 * @throws Exception
	 */
	<T> void watchProvider(String serviceId, final DcType dc, RegistryEventWatcher<T> watcher) throws Exception;
	
	/**
	 * 注册服务消费者（consumer）<br>
	 * @param serviceId
	 * @param dcs
	 * @param consumer
	 * @throws Exception
	 */
	void  registerConsumer(String serviceId, final DcType[] dcs, ServiceConsumer consumer) throws Exception;
	
	/**
	 * 获取服务的所有 Consumer
	 * @param serviceId
	 * @param dcs
	 * @return
	 * @throws Exception
	 */
	Map<DcType, List<ServiceConsumer>> fetchConsumers(String serviceId, final DcType[] dcs) throws Exception;
	
	/**
	 * 监听 Consumer 的配置变化事件<br>
	 * @param serviceId
	 * @param dc
	 * @param watcher
	 * @throws Exception
	 */
	<T> void  watchConsumer(String serviceId, final DcType dc, RegistryEventWatcher<T> watcher) throws Exception;
	
	/**
	 * 监听 service 节点配置变化事件<br>
	 * <b>对服务端</b>
	 * @param serviceId
	 * @param dc
	 * @param provider
	 * @throws Exception
	 */
	<T> void watchService(String serviceId, final DcType dc, final ServiceProvider provider) throws Exception;
	
	/**
	 * 监听 service 节点配置变化事件<br>
	 * <b>对客户端</b>
	 * @param serviceId
	 * @param dc
	 * @param consumer
	 * @throws Exception
	 */
	<T> void watchService(String serviceId, final DcType dc, final ServiceConsumer consumer) throws Exception;
	
	/**
	 * 监听 host-port 节点配置变化事件<br>
	 * @param serviceId
	 * @param dc
	 * @param provider
	 * @throws Exception
	 */
	<T> void watchHostPort(String serviceId, final DcType dc, final ServiceProvider provider) throws Exception;
	
	/**
	 * 检查节点服务状态<br>
	 * @param serviceId
	 * @param dc
	 * @param provider
	 * @return
	 * @throws Exception
	 */
	boolean checkServiceProvider(String serviceId, final DcType dc, final ServiceProvider provider) throws Exception;
	
	/**
	 * 监听服务上的命令<br>
	 * @param serviceId
	 * @param dcs
	 * @param commandType
	 * @param executor
	 * @throws Exception
	 */
	<T> void watchCommand(String serviceId, final DcType[] dcs, final CommandType commandType, RegistryCommandExecutor<T> executor) throws Exception;
	
	/**
	 * 检查dc上某个服务的策略<br>
	 * @param dc
	 * @param serviceKey
	 * @return
	 * @throws Exception
	 */
	int checkDcStrategy(DcType dc, String serviceKey) throws Exception;
	
}
