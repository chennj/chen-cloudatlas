package net.chen.cloudatlas.crow.rpc.filter;

import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.annotation.Activate;
import net.chen.cloudatlas.crow.rpc.Context;
import net.chen.cloudatlas.crow.rpc.Filter;
import net.chen.cloudatlas.crow.rpc.Invocation;
import net.chen.cloudatlas.crow.rpc.Invoker;
import net.chen.cloudatlas.crow.rpc.Result;
import net.chen.cloudatlas.crow.rpc.RpcException;

/**
 * 该filter在rpc调用filter chain的开头。
 * filter chain全部结束后去除context
 * @author chenn
 *
 */
@Activate(side={Constants.CONSUMER, Constants.PROVIDER},order=-1000)
public class ProviderContextFilter implements Filter{

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public Result doFilter(Invoker<?> invoker, Invocation invocation) throws RpcException {
		//在context中需要为后面的filter们放些什么？
		try {
			return invoker.invoke(invocation);
		} finally{
			//filter chain结束，去除context
			Context.removeContext();
		}
	}

}
