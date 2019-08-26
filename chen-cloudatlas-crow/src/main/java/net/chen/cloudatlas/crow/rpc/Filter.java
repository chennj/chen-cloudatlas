package net.chen.cloudatlas.crow.rpc;

import net.chen.cloudatlas.crow.common.NameableService;

public interface Filter extends NameableService{

	Result doFilter(Invoker<?> invoker, Invocation invocation)throws RpcException;
}
