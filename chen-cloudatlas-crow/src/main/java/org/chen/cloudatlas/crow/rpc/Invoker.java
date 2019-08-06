package org.chen.cloudatlas.crow.rpc;

import org.chen.cloudatlas.crow.common.DcType;
import org.chen.cloudatlas.crow.common.URL;

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
	
	void destory();
	
	void setDc(DcType dc);
	
	String getInvokerKey();
}
