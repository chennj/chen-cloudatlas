package org.chen.cloudatlas.crow.rpc.protocol;

import org.chen.cloudatlas.crow.common.URL;
import org.chen.cloudatlas.crow.rpc.Invoker;
import org.chen.cloudatlas.crow.rpc.RpcException;

/**
 * 
 * @author chenn
 *
 */
public interface ProxyFactory {

	<T> T getProxy(Invoker<T> invoker) throws RpcException;
	
	<T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url) throws RpcException;
}
