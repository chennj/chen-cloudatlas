package org.chen.cloudatlas.crow.rpc.protocol;

import org.chen.cloudatlas.crow.rpc.Invoker;

public abstract class AbstractExporter<T> implements Exporter<T> {

	private final Invoker<T> invoker;
	
	private volatile boolean unexported = false;
	
	public AbstractExporter(Invoker<T> invoker){
		
		if (null == invoker){
			throw new IllegalStateException("service invoker is null");
		}
		
		if (invoker.getInterface() == null){
			throw new IllegalStateException("service type is null");
		}
		
		if (invoker.getUrl() == null){
			throw new IllegalStateException("service url is null");
		}
		
		this.invoker = invoker;
	}
	
	public Invoker<T> getInvoker(){
		return invoker;
	}
	
	public void unexport(){
		
		if (unexported){
			return;
		}
		unexported = true;
		getInvoker().destroy();
	}
	
	public String toString(){
		return getInvoker().toString();
	}
}
