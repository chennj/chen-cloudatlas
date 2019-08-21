package org.chen.cloudatlas.crow.rpc;

import org.chen.cloudatlas.crow.common.URL;

/**
 * 
 * @author chenn
 *
 */
public interface ProxyFactory {

	<T> T getProxy(Invoker<T> invoker) throws RpcException;
	
	<T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url) throws RpcException;
}
