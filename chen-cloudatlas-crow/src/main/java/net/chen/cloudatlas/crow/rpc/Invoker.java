package net.chen.cloudatlas.crow.rpc;

import net.chen.cloudatlas.crow.common.DcType;
import net.chen.cloudatlas.crow.common.URL;

/**
 * 调度器接口
 * @author chenn
 *
 */
public interface Invoker<T> {

	boolean isAvailable();
	
	Class<T> getInterface();
	
	Result invoke(Invocation invocation) throws RpcException;
	
	URL getUrl();
	
	void insertInvoker(Invoker<?> invoker);
	
	void deleteInvoker(Invoker<?> invoker);
	
	void setInterface(Class<T> interfaceClass);
	
	void destroy();
	
	void setDc(DcType dc);
	
	String getInvokeKey();
}
