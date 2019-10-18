package net.chen.cloudatlas.crow.rpc.protocol;

import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.tinylog.Logger;

import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.config.ServiceConfig;
import net.chen.cloudatlas.crow.rpc.Exporter;
import net.chen.cloudatlas.crow.rpc.Invoker;
import net.chen.cloudatlas.crow.rpc.Protocol;
import net.chen.cloudatlas.crow.rpc.ProxyFactory;
import net.chen.cloudatlas.crow.rpc.proxy.JdkProxyFactory;


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
			} else {
				Class<?> cls = Class.forName("org.chen.cloudatlas.crow.rpc.crow.CrowProtocol");
				Method method = cls.getMethod("getCrowProtocol", new Class[0]);
				Object obj = method.invoke(cls, new Object[0]);
				expProtocol = new ProtocolFilterWrapper((Protocol)obj);
			}
		} catch (Exception e){
			Logger.error("Exception while exporting {}", e);
		}
		
		Invoker<T> invoker = proxyFactory.getInvoker(config.getImpl(), config.getInterface(), url);
		exporter = expProtocol.export(invoker);
	}
	
	public void unexport(){
		exporter.unexport();
	}
}
