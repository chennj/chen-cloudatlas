package net.chen.cloudatlas.crow.rpc;

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
