package net.chen.cloudatlas.crow.bootstrap;

import java.util.List;

import net.chen.cloudatlas.crow.common.utils.NameableServiceLoader;
import net.chen.cloudatlas.crow.config.CrowClientContext;
import net.chen.cloudatlas.crow.config.CrowConfig;
import net.chen.cloudatlas.crow.config.MonitorConfig;
import net.chen.cloudatlas.crow.config.ReferenceConfig;
import net.chen.cloudatlas.crow.config.RegistryConfig;
import net.chen.cloudatlas.crow.config.ServiceConfig;
import net.chen.cloudatlas.crow.manager.api.RegistryClient;
import net.chen.cloudatlas.crow.manager.api.RegistryConnectionState;
import net.chen.cloudatlas.crow.manager.api.RegistryConnectionStateListener;
import net.chen.cloudatlas.crow.manager.api.RegistryLocalStore;
import net.chen.cloudatlas.crow.manager.api.RegistryManager;

/**
 * 未完成
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unregister() {
		// TODO Auto-generated method stub
		
	}

}
