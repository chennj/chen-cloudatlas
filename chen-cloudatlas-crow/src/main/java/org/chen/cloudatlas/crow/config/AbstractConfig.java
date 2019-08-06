package org.chen.cloudatlas.crow.config;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlTransient;

public abstract class AbstractConfig implements Configuarable, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@XmlTransient
	private ApplicationConfig applicationConfig;
	
	@XmlTransient
	private RegistryConfig registryConfig;
	
	@XmlTransient
	private MonitorConfig monitorConfig;

	public ApplicationConfig getApplicationConfig() {
		return applicationConfig;
	}

	public void setApplicationConfig(ApplicationConfig applicationConfig) {
		this.applicationConfig = applicationConfig;
	}

	public RegistryConfig getRegistryConfig() {
		return registryConfig;
	}

	public void setRegistryConfig(RegistryConfig registryConfig) {
		this.registryConfig = registryConfig;
	}

	public MonitorConfig getMonitorConfig() {
		return monitorConfig;
	}

	public void setMonitorConfig(MonitorConfig monitorConfig) {
		this.monitorConfig = monitorConfig;
	}
	
	
}
