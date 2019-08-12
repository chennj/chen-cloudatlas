package org.chen.cloudatlas.crow.remote.impl;

import java.util.List;

import org.chen.cloudatlas.crow.config.CrowServerContext;
import org.chen.cloudatlas.crow.config.ProtocolConfig;
import org.chen.cloudatlas.crow.config.ServiceConfig;
import org.chen.cloudatlas.crow.remote.HttpServer;

public class ExchangeHttpServer {

	private HttpServer httpServer;
	
	private List<ServiceConfig> serviceConfig;
	
	public ExchangeHttpServer(HttpServer httpServer, ProtocolConfig protocolConfig){
		
		this.httpServer = httpServer;
		export(protocolConfig);
		this.httpServer.start();
	}

	private void export(ProtocolConfig protocolConfig) {
		
		List<ServiceConfig> serviceList = CrowServerContext.getServiceListByProtocol(protocolConfig);
		
		for (ServiceConfig serviceConfig : serviceList){
			
			String serviceId = serviceConfig.getServiceId();
		}
	}
}
