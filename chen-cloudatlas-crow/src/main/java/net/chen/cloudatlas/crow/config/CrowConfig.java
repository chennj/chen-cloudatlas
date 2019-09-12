package net.chen.cloudatlas.crow.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.chen.cloudatlas.crow.common.KeyUtil;
import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.common.exception.ConfigInvalidException;
import net.chen.cloudatlas.crow.common.exception.MethodNotImplException;

/**
 * 
 * @author chenn
 *
 */
@XmlRootElement(name = "casual")
@XmlAccessorType(XmlAccessType.FIELD)
public class CrowConfig implements Configuarable, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@XmlElement(name="application")
	private ApplicationConfig applicationConfig;
	
	/**
	 * 集群配置
	 */
	@XmlElement(name="registry")
	private RegistryConfig registryConfig;
	
	@XmlElement(name="monitor")
	private MonitorConfig monitorConfig;
	
	@XmlElement(name="protocol")
	private List<ProtocolConfig> protocolConfigList = new ArrayList<ProtocolConfig>();
	
	@XmlElement(name="service")
	private List<ServiceConfig> serviceConfigList = new ArrayList<ServiceConfig>();
	
	@XmlElement(name="reference")
	private List<ReferenceConfig> referenceConfigList = new ArrayList<ReferenceConfig>();
	
	
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

	public List<ProtocolConfig> getProtocolConfigList() {
		return protocolConfigList;
	}

	public void setProtocolConfigList(List<ProtocolConfig> protocolConfigList) {
		this.protocolConfigList = protocolConfigList;
	}

	public List<ServiceConfig> getServiceConfigList() {
		return serviceConfigList;
	}

	public void setServiceConfigList(List<ServiceConfig> serviceConfigList) {
		this.serviceConfigList = serviceConfigList;
	}

	public List<ReferenceConfig> getReferenceConfigList() {
		return referenceConfigList;
	}

	public void setReferenceConfigList(List<ReferenceConfig> referenceConfigList) {
		this.referenceConfigList = referenceConfigList;
	}

	public List<URL> getAllReferenceUrls(){
		
		List<URL> result = new ArrayList<URL>();
		List<ReferenceConfig> list = this.getReferenceConfigList();
		
		for (ReferenceConfig one : list){
			
			result.addAll(one.getURLs());
		}
		
		return result;
	}
	
	/**
	 * 仅仅获取非RPC的reference列表
	 * @return
	 */
	public List<URL> getBinaryReferenceUrls(){
		throw new MethodNotImplException();
	}
	
	@Override
	public void check() throws ConfigInvalidException {
		
		if (null != applicationConfig){
			applicationConfig.check();
		}
		
		if (null != monitorConfig){
			monitorConfig.check();
		}
		
		if (null != registryConfig){
			registryConfig.check();
		}
		
		/**
		 * 检查protocol是否有id重复
		 */
		Set<String> idSet = new HashSet<String>();
		
		for (ProtocolConfig<?> protocol : this.protocolConfigList){
			protocol.check();
			idSet.add(protocol.getId());
		}
		
		if (idSet.size() != this.protocolConfigList.size()){
			throw new ConfigInvalidException("multiple <protocol> exist with same id, please make sure id is unique.");
		}
		
		/**
		 * 检查service是否有serviceId重复
		 */
		Set<String> serviceIdSet = new HashSet<>();
		for (ServiceConfig<?> service : this.serviceConfigList){
			service.check();
			serviceIdSet.add(KeyUtil.getServiceKey(service.getServiceId(), service.getServiceVersion()));
		}
		if (serviceIdSet.size() != this.serviceConfigList.size()){
			throw new ConfigInvalidException("multiple <service> exist with same id, please make sure id is unique.");
		}
		
		/**
		 * 检查reference是否有serviceId重复
		 */
		Set<String> refIdSet = new HashSet<>();
		for (ReferenceConfig<?> ref : this.referenceConfigList){
			ref.check();
			refIdSet.add(KeyUtil.getServiceKey(ref.getServiceId(), ref.getServiceVersion()));
		}
		if (refIdSet.size() != this.referenceConfigList.size()){
			throw new ConfigInvalidException("multiple <reference> exist with same id, please make sure id is unique.");
		}
	}

	@Override
	public void setDefaultValue() {
		
		if (null == this.applicationConfig){
			this.applicationConfig = new ApplicationConfig();
		}
		this.applicationConfig.setDefaultValue();
		
		if (null != this.monitorConfig){
			this.monitorConfig.setDefaultValue();
			this.monitorConfig.setApplicationConfig(applicationConfig);
		}
		
		if (null != this.registryConfig){
			this.registryConfig.setDefaultValue();
			this.registryConfig.setApplicationConfig(applicationConfig);
		}
		
		for (ProtocolConfig<?> protocol : this.protocolConfigList){
			
			if (null != this.applicationConfig){
				protocol.setApplicationConfig(applicationConfig);
			}
			if (null != this.monitorConfig){
				protocol.setMonitorConfig(monitorConfig);
			}
			if (null != this.registryConfig){
				protocol.setRegistryConfig(registryConfig);
			}
			protocol.setDefaultValue();
		}
		
		for (ServiceConfig<?> service : this.serviceConfigList){
			
			if (null != this.applicationConfig){
				service.setApplicationConfig(applicationConfig);
			}
			if (null != this.monitorConfig){
				service.setMonitorConfig(monitorConfig);
			}
			if (null != this.registryConfig){
				service.setRegistryConfig(registryConfig);
			}
			service.setDefaultValue();
		}
		
		for (ReferenceConfig<?> reference : this.referenceConfigList){
			
			if (null != this.applicationConfig){
				reference.setApplicationConfig(applicationConfig);
			}
			if (null != this.monitorConfig){
				reference.setMonitorConfig(monitorConfig);
			}
			if (null != this.registryConfig){
				reference.setRegistryConfig(registryConfig);
			}
			reference.setDefaultValue();
		}
	}

}
