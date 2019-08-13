package org.chen.cloudatlas.crow.rpc.protocol;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import org.chen.cloudatlas.crow.common.URL;
import org.chen.cloudatlas.crow.rpc.Invocation;
import org.chen.cloudatlas.crow.rpc.Invoker;
import org.chen.cloudatlas.crow.rpc.Result;
import org.chen.cloudatlas.crow.rpc.RpcException;
import org.tinylog.Logger;

public abstract class AbstractProxyProtocol extends AbstractProtocol{

	private final List<Class<?>> rpcExceptions = new CopyOnWriteArrayList<Class<?>>();
	
	private ProxyFactory proxyFactory;
	
	public AbstractProxyProtocol(){
		
	}
	
	public AbstractProxyProtocol(Class<?>...exceptions){
		
		for (Class<?> exception : exceptions){
			addRpcException(exception);
		}
	}
	
	public void addRpcException(Class<?> exception){
		this.rpcExceptions.add(exception);
	}

	public ProxyFactory getProxyFactory() {
		return proxyFactory;
	}

	public void setProxyFactory(ProxyFactory proxyFactory) {
		this.proxyFactory = proxyFactory;
	}

	@Override
	public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
		
		final String uri = serviceKey(invoker.getUrl());
		
		Exporter<T> exporter = (Exporter<T>)exporterMap.get(uri);
		if (null != exporter){
			return exporter;
		}
		
		final Destroyable runnable = doExport(proxyFactory.getProxy(invoker), invoker.getInterface(), invoker.getUrl());
		
		exporter = new AbstractExporter<T>(invoker){

			@Override
			public void unexport() {
				
				super.unexport();
				exporterMap.remove(uri);
				if (null != runnable){
					
					try {
						runnable.destroy();
					} catch (Exception ec){
						Logger.error("exception caught while finishing doExport",ec);
					}
				}
			}
			
		};
		
		exporterMap.put(uri, exporter);
		return exporter;
	}
	
	@Override
	public <T> Invoker<T> refer(Class<T> type, URL url, CountDownLatch latch) throws RpcException {
		
		final Invoker<T> target = proxyFactory.getInvoker(doRefer(type,url), type, url);
		
		Invoker<T> invoker = new AbstractInvoker<T>(type,url){

			@Override
			protected Result doInvoke(Invocation invocation) throws Exception {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
	}

	protected abstract <T> Destroyable doExport(T impl, Class<T> type, URL url) throws RpcException;
	
	protected abstract <T> T doRefer(Class<T> type, URL url) throws RpcException;
	
}
