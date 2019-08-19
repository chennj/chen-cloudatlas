package org.chen.cloudatlas.crow.rpc.crow;

import java.util.Map;

import org.chen.cloudatlas.crow.rpc.Invoker;
import org.chen.cloudatlas.crow.rpc.protocol.AbstractExporter;
import org.chen.cloudatlas.crow.rpc.protocol.Exporter;

public class CrowExporter<T> extends AbstractExporter<T>{

	private final String key;
	
	private final Map<String, Exporter<?>> exporterMap;
	
	public CrowExporter(Invoker<T> invoker, String key, Map<String, Exporter<?>> exporterMap) {
		super(invoker);
		this.key = key;
		this.exporterMap = exporterMap;
	}
	
	@Override
	public void unexport(){
		super.unexport();
		exporterMap.remove(key);
	}

}
