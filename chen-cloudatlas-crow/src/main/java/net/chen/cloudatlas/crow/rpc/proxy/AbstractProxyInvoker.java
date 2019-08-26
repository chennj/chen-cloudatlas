package net.chen.cloudatlas.crow.rpc.proxy;

import java.lang.reflect.InvocationTargetException;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.common.DcType;
import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.rpc.Invocation;
import net.chen.cloudatlas.crow.rpc.Invoker;
import net.chen.cloudatlas.crow.rpc.Result;
import net.chen.cloudatlas.crow.rpc.RpcException;
import net.chen.cloudatlas.crow.rpc.impl.RpcResult;

public abstract class AbstractProxyInvoker<T> implements Invoker<T> {

	private final T proxy;
	
	private final Class<T> interfaceClass;
	
	private final URL url;
	
	public AbstractProxyInvoker(T proxy, Class<T> interfaceClass, URL url){
		
		if (null == proxy){
			throw new IllegalArgumentException("proxy is null");
		}
		if (null == interfaceClass){
			throw new IllegalArgumentException("interfaceClass is null");
		}
		if (null == url){
			throw new IllegalArgumentException("url is null");
		}
		
		this.proxy = proxy;
		this.interfaceClass = interfaceClass;
		this.url = url;
	}

	@Override
	public Result invoke(Invocation invocation) throws RpcException {
		
		try {
			return new RpcResult(doInvoke(proxy, invocation.getMethodName(), invocation.getParameterTypes(),invocation.getArguments()));
		} catch (InvocationTargetException te){
			Logger.error("InvocationTargetException occurs while doInvoke {}",te);
			return new RpcResult(te.getTargetException());
		} catch (Exception e){
			throw new RpcException("failed to invoke remote proxy method " + invocation.getMethodName() + " to " + getUrl());
		}
	}

	@Override
	public Class<T> getInterface() {
		return interfaceClass;
	}

	@Override
	public URL getUrl() {
		return url;
	}

	@Override
	public void insertInvoker(Invoker<?> invoker) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteInvoker(Invoker<?> invoker) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public void setInterface(Class<T> interfaceClass) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDc(DcType dc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getInvokeKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		return getInterface() + " -> " + getUrl() == null ? " " : getUrl().toString();
	}
	
	protected abstract Object doInvoke(T proxy, String methodName, Class<?>[] parameterTypes, Object[] arguments) throws Exception;
}
