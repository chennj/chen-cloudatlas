package net.chen.cloudatlas.crow.rpc;

/**
 * 为了让框架支持用户自定义ProxyFactory,以便用户在server端可以在invoke之前，做一些自定义的filter
 * 
 * @author chenn
 *
 */
public interface SubProxyFactory extends ProxyFactory{

	void beforeServerInvoke(Invocation invocation);
}
