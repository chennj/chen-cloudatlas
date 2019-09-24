package net.chen.cloudatlas.crow.filter.support;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.annotation.Activate;
import net.chen.cloudatlas.crow.filter.BinaryFilter;
import net.chen.cloudatlas.crow.filter.BinaryFilterChain;
import net.chen.cloudatlas.crow.remote.RemoteException;
import net.chen.cloudatlas.crow.remote.Request;
import net.chen.cloudatlas.crow.remote.Response;
import net.chen.cloudatlas.crow.rpc.SubInvoker;

/**
 * 默认filter,在filter chain末尾，一定被调用
 * @author chenn
 *
 */
@Activate(side={Constants.CONSUMER, Constants.PROVIDER},order=999)
public class BinaryDefaultFilter implements BinaryFilter{

	@Override
	public String getName() {
		return "defaultFilter";
	}

	@Override
	public Response doFilter(SubInvoker subInvoker, Request request, BinaryFilterChain chain) throws RemoteException {
		Logger.trace(this.getClass().getSimpleName()+"#doFilter called!");
		return subInvoker.call(request);
	}

}
