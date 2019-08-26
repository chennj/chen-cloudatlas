package net.chen.cloudatlas.crow.rpc;

import java.util.concurrent.CountDownLatch;

import net.chen.cloudatlas.crow.common.NameableService;
import net.chen.cloudatlas.crow.common.URL;

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
