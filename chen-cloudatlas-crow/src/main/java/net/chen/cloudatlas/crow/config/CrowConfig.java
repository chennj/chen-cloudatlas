package net.chen.cloudatlas.crow.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
	
	public void check() throws ConfigInvalidException {
		// TODO Auto-generated method stub
		
	}

	public void setDefaultValue() {
		// TODO Auto-generated method stub
		
	}

}
