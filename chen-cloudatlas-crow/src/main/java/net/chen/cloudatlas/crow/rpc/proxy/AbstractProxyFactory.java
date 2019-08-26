package net.chen.cloudatlas.crow.rpc.proxy;

import net.chen.cloudatlas.crow.rpc.Invoker;
import net.chen.cloudatlas.crow.rpc.ProxyFactory;
import net.chen.cloudatlas.crow.rpc.RpcException;

public abstract class AbstractProxyFactory implements ProxyFactory{

	public <T> T getProxy(Invoker<T> invoker) throws RpcException{
		// 在dubbo源码中，除了参数invoker的interface，还有url中的interface值及
		// EchoService，一起组成了Class[],这里简化了
		return getProxy(invoker, new Class<?>[]{invoker.getInterface()});
	}

	public abstract <T> T getProxy(Invoker<T> invoker, Class<?>[] types);
}
