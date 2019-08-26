package net.chen.cloudatlas.crow.manager.api;

import java.io.Serializable;
import java.util.List;

import net.chen.cloudatlas.crow.config.MonitorConfig;
import net.chen.cloudatlas.crow.config.ReferenceConfig;

public class RegistryData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2027381904217538814L;

	private String applicationName;
	
	private List<ReferenceConfig> providerConfig;
	
	private MonitorConfig monitorConfig;
	
	public RegistryData(){}
	
	public RegistryData(String applicationName, List<ReferenceConfig> providerConfig){
		super();
		this.applicationName = applicationName;
		this.providerConfig = providerConfig;
	}
	
	public RegistryData(String applicationName, List<ReferenceConfig> providerConfig,MonitorConfig monitorConfig){
		super();
		this.applicationName = applicationName;
		this.providerConfig = providerConfig;
		this.monitorConfig = monitorConfig;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public List<ReferenceConfig> getProviderConfig() {
		return providerConfig;
	}

	public void setProviderConfig(List<ReferenceConfig> providerConfig) {
		this.providerConfig = providerConfig;
	}

	public MonitorConfig getMonitorConfig() {
		return monitorConfig;
	}

	public void setMonitorConfig(MonitorConfig monitorConfig) {
		this.monitorConfig = monitorConfig;
	}

	@Override
	public String toString(){
		return "RegistryData [applicationName=" + applicationName + ", providerConfig=" + providerConfig + "]";
	}
}
