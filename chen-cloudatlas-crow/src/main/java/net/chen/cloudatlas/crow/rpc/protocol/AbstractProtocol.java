package net.chen.cloudatlas.crow.rpc.protocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.rpc.Exporter;
import net.chen.cloudatlas.crow.rpc.Invoker;
import net.chen.cloudatlas.crow.rpc.Protocol;
import net.chen.cloudatlas.crow.rpc.utils.ProtocolUtil;

public abstract class AbstractProtocol implements Protocol{

	protected final Map<String, Exporter<?>> exporterMap = new HashMap<>();
	
	protected final Set<Invoker<?>> invokers = new CopyOnWriteArraySet<>();
	
	protected static String serviceKey(URL url){
		return ProtocolUtil.serviceKey(url);
	}
	
	protected static String serviceKeyOld(URL url){
		return ProtocolUtil.serviceKeyOld(url);
	}
	
	protected static String serviceKey(int port, String serviceName, String dc, String serviceVersion){
		return ProtocolUtil.serviceKey(port, serviceName, dc, serviceVersion);
	}

	@Override
	public void destroy() {
		
		for (Invoker<?> invoker : invokers){
			
			if (null != invoker){
				invokers.remove(invoker);
				try {
					if (Logger.isInfoEnabled()){
						Logger.info("Destory reference: " + invoker.getUrl());
					}
					invoker.destroy();
				} catch (Exception e){
					Logger.warn("destroy exception ", e);
				}
			}
		}
		
		for (String key : new ArrayList<String>(exporterMap.keySet())){
			
			Exporter<?> exporter = exporterMap.remove(key);
			if (null != exporter){
				try {
					if (Logger.isInfoEnabled()){
						Logger.info("Unexport service: " + exporter.getInvoker().getUrl());
					}
					exporter.unexport();
				} catch (Exception e){
					Logger.warn("unexport exception ", e);
				}
			}
		}
	}
}
