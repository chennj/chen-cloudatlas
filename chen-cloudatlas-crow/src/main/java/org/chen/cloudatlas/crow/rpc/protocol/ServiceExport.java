package org.chen.cloudatlas.crow.rpc.protocol;

import org.apache.commons.lang.StringUtils;
import org.chen.cloudatlas.crow.common.URL;
import org.chen.cloudatlas.crow.config.ServiceConfig;
import org.tinylog.Logger;


public class ServiceExport<T> {

	private ServiceConfig<T> config;
	
	private Protocol expProtocol;
	private ProxyFactory proxyFactory;
	private Exporter<?> exporter;
	
	public ServiceExport(ServiceConfig<T> config){
		this.config = config;
	}
	
	public void doExport(URL url){
		
		if (!StringUtils.isEmpty(config.getProxyFactory())){
			
			try {
				proxyFactory = (ProxyFactory)Class.forName(config.getProxyFactory()).newInstance();
				
				Logger.info("ProxyFactory is: {}. [serviceId: {}, interfaceClass: {}, serviceVersion: {}]",
						config.getProxyFactory(),
						config.getServiceId(),
						config.getInterfaceClass(),
						config.getServiceVersion());
			} catch (Exception e){
				throw new RuntimeException("Error create proxy of ProxyFactory for service: " + config.getServiceId(), e);
			}
		} else {
			proxyFactory = new JdkProxyFactory();
		}
		
		try {
			if ("rmi".equals(config.getProtocol().getCodec())){
				expProtocol = (Protocol)Class.forName("org.chen.cloudatlas.crow.rpc.rmi.RmiProtocol").newInstance();
				((AbstractProxyProtocol)expProtocol).setProxyFactory(proxyFactory);
			}
		}
	}
}
