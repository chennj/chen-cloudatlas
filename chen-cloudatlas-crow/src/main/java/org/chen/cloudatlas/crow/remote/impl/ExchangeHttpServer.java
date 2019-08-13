package org.chen.cloudatlas.crow.remote.impl;

import java.lang.reflect.Method;
import java.util.List;

import org.chen.cloudatlas.crow.common.ApiRouting;
import org.chen.cloudatlas.crow.common.SerializationType;
import org.chen.cloudatlas.crow.config.CrowServerContext;
import org.chen.cloudatlas.crow.config.ProtocolConfig;
import org.chen.cloudatlas.crow.config.ServiceConfig;
import org.chen.cloudatlas.crow.remote.HttpServer;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

public class ExchangeHttpServer {

	private HttpServer httpServer;
	
	private List<ServiceConfig> serviceConfig;
	
	public ExchangeHttpServer(HttpServer httpServer, ProtocolConfig protocolConfig){
		
		this.httpServer = httpServer;
		export(protocolConfig);
		this.httpServer.start();
	}

	/**
	 * 缓存路径：方法对到apirouting<br>
	 * path e.g. www.xx.xxx/abc/methodName?p1=xx&p2=yy<br>
	 * api->invoker:serviceConfig.getImpl(),method:methodName,params:{p1,p2}<br>
	 * @param protocolConfig
	 */
	private void export(ProtocolConfig protocolConfig) {
		
		List<ServiceConfig> serviceList = CrowServerContext.getServiceListByProtocol(protocolConfig);
		
		for (ServiceConfig serviceConfig : serviceList){
			
			String serviceId = serviceConfig.getServiceId();
			String serviceVersion = serviceConfig.getServiceVersion();
			
			Method[] methods;
			if (protocolConfig.getSerializationType().equals(SerializationType.BINARY)){
				methods = protocolConfig.getListenerImpl().getClass().getMethods();
			} else {
				methods = serviceConfig.getImpl().getClass().getMethods();
			}
			
			for (Method method : methods){
				String path = ApiRouting.Path.build(serviceId, serviceVersion, method.getName());
				ApiRouting.cacheApiRoute(path, ApiRouting.Api.build(serviceConfig.getImpl(), method));
				ApiRouting.cacheParametersRoute(path, analyzeParameters(method));
			}
		}
	}

	private String[] analyzeParameters(Method method) {
		ParameterNameDiscoverer parameterNameDiscoverer = 
				new LocalVariableTableParameterNameDiscoverer();
		String[] parameters = parameterNameDiscoverer.getParameterNames(method);
		return parameters;
	}
}
