package org.chen.cloudatlas.crow.bootstrap;

import java.util.List;

import org.chen.cloudatlas.crow.common.utils.NameableServiceLoader;
import org.chen.cloudatlas.crow.config.CrowClientContext;
import org.chen.cloudatlas.crow.config.CrowConfig;
import org.chen.cloudatlas.crow.config.MonitorConfig;
import org.chen.cloudatlas.crow.config.ReferenceConfig;
import org.chen.cloudatlas.crow.config.RegistryConfig;
import org.chen.cloudatlas.crow.config.ServiceConfig;
import org.chen.cloudatlas.crow.manager.api.RegistryClient;
import org.chen.cloudatlas.crow.manager.api.RegistryConnectionState;
import org.chen.cloudatlas.crow.manager.api.RegistryConnectionStateListener;
import org.chen.cloudatlas.crow.manager.api.RegistryLocalStore;
import org.chen.cloudatlas.crow.manager.api.RegistryManager;

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
	
	private final CrowBootstrap bootStrap;
	
	private Object lock = new Object();
	
	public CrowZkConnectionStateListener(
			CrowConfig config,
			RegistryClient registryClient,
			ClientSideBooter clientSideBooter,
			CrowBootstrap bootStrap,
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
			CrowBootstrap bootStrap,
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
