package net.chen.cloudatlas.crow.rpc.proxy;

import java.lang.reflect.Proxy;

import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.rpc.Invoker;
import net.chen.cloudatlas.crow.rpc.RpcException;
import net.chen.cloudatlas.crow.rpc.SubProxyFactory;

public abstract class AbstractJdkProxyFactory implements SubProxyFactory{

	@Override
	public <T> T getProxy(Invoker<T> invoker) throws RpcException {
		
		return getProxy(invoker, new Class<?>[] { invoker.getInterface()});
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getProxy(Invoker<T> invoker, Class<?>[] interfaces){
		return (T) Proxy.newProxyInstance(
				Thread.currentThread().getContextClassLoader(), 
				interfaces, 
				new InvokerInvocationHandler(invoker));
	}
	
	@Override
	public <T> Invoker<T> getInvoker(final T proxy, Class<T> interfaceClass, URL url) throws RpcException{
		
		return new AbstractProxyInvoker<T>(proxy, interfaceClass, url){

			@Override
			protected Object doInvoke(T proxy, String methodName, Class<?>[] parameterTypes, Object[] arguments)
					throws Exception {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
	}
}
