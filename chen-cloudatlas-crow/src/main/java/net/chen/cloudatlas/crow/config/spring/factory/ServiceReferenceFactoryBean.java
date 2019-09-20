package net.chen.cloudatlas.crow.config.spring.factory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import net.chen.cloudatlas.crow.client.ServiceController;
import net.chen.cloudatlas.crow.client.ServiceRegistry;
import net.chen.cloudatlas.crow.common.DcType;

/**
 * 创建了一个factory bean,用来从配置的reference选项产生出service的对象代理，
 * 供客户端调用
 * @author chenn
 *
 */
public class ServiceReferenceFactoryBean implements FactoryBean<Object>,InitializingBean{

	private String serviceId;
	
	private String interfaceName;
	
	private String serviceVersion;
	
	private Class<?> clazz;
	
	private String refDc;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		
		if (!StringUtils.hasText(serviceId) && !StringUtils.hasText(interfaceName)){
			throw new IllegalArgumentException("property 'serviceId' or 'interfaceName' must not be null both!");
		}
		
		if (StringUtils.hasText(serviceId) && StringUtils.hasText(interfaceName)){
			throw new IllegalArgumentException("property 'serviceId' or 'interfaceName' must not be not null both!");
		}
		
		if (StringUtils.hasText(serviceId)){
			clazz = ServiceController.class;
		} else {
			try {
				clazz = Class.forName(interfaceName);
			} catch (ClassNotFoundException e){
				throw new IllegalArgumentException(String.format("Class[%s] not found. check property[interfaceName]", this.interfaceName));
			}
		}
	}

	@Override
	public Object getObject() throws Exception {
		
		if (StringUtils.hasText(serviceId)){
			return ServiceRegistry.getService(
					serviceId,
					StringUtils.hasText(serviceVersion)?serviceVersion.trim():"1.0",
					StringUtils.hasText(refDc)?DcType.fromString(refDc):null);
		} else {
			return ServiceRegistry.getService(
					Class.forName(interfaceName),
					StringUtils.hasText(serviceVersion)?serviceVersion.trim():"1.0",
					StringUtils.hasText(refDc)?DcType.fromString(refDc):null);			
		}
	}

	@Override
	public Class<?> getObjectType() {
		return clazz;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public void setServiceVersion(String serviceVersion) {
		this.serviceVersion = serviceVersion;
	}

	public void setRefDc(String refDc) {
		this.refDc = refDc;
	}

	
}
