package org.chen.cloudatlas.crow.rpc.protocol;

import org.chen.cloudatlas.crow.rpc.Invoker;

/**
 * 
 * @author chenn
 *
 * @param <T>
 */
public interface Exporter<T> {

	Invoker<T> getInvoker();
	
	void unexport();
}
