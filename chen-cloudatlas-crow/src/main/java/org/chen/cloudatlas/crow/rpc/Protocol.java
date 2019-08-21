package org.chen.cloudatlas.crow.rpc;

import java.util.concurrent.CountDownLatch;

import org.chen.cloudatlas.crow.common.NameableService;
import org.chen.cloudatlas.crow.common.URL;

/**
 * 
 * @author chenn
 *
 */
public interface Protocol extends NameableService{

	int getDefaultPort();
	
	<T> Exporter<T> export(Invoker<T> invoker) throws RpcException;
	
	<T> Invoker<T> refer(Class<T> type, URL url, CountDownLatch latch) throws RpcException;
	
	void destroy();
}
