package org.chen.cloudatlas.crow.rpc.protocol;

import java.util.concurrent.CountDownLatch;

import org.chen.cloudatlas.crow.common.NameableService;
import org.chen.cloudatlas.crow.common.URL;
import org.chen.cloudatlas.crow.rpc.Invoker;
import org.chen.cloudatlas.crow.rpc.RpcException;

/**
 * 
 * @author chenn
 *
 */
public interface Protocol extends NameableService{

	int getDefaultPort();
	
	<T> Exporter<T> export(Invoker<T> invoker) throws RpcException;
	
	<T> Invoker<T> refer(Class<T> type, URL url, CountDownLatch latch) throws RpcException;
}
