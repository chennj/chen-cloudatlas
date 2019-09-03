package net.chen.cloudatlas.crow.client.impl;

import java.util.List;

import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.DcType;
import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.common.utils.NameableServiceLoader;
import net.chen.cloudatlas.crow.filter.BinaryFilter;
import net.chen.cloudatlas.crow.filter.BinaryFilterChain;
import net.chen.cloudatlas.crow.remote.RemoteException;
import net.chen.cloudatlas.crow.remote.Request;
import net.chen.cloudatlas.crow.remote.Response;
import net.chen.cloudatlas.crow.rpc.Invocation;
import net.chen.cloudatlas.crow.rpc.Invoker;
import net.chen.cloudatlas.crow.rpc.Result;
import net.chen.cloudatlas.crow.rpc.RpcException;
import net.chen.cloudatlas.crow.rpc.impl.OneToOneInvoker;

public class OneToOneInvokerWrapper extends OneToOneInvoker{

	private List<BinaryFilter> consumerFilterMap = 
			NameableServiceLoader.getLoader(BinaryFilter.class).getActiveServices(Constants.CONSUMER);
	
	private OneToOneInvoker invoker;
	
	public OneToOneInvokerWrapper(OneToOneInvoker invoker){
		this.invoker = invoker;
	}

	@Override
	public boolean isAvailable() {
		return invoker.isAvailable();
	}

	@Override
	public Class getInterface() {
		return invoker.getInterface();
	}

	@Override
	public Result invoke(Invocation invocation) throws RpcException {
		return invoker.invoke(invocation);
	}

	@Override
	public URL getUrl() {
		return invoker.getUrl();
	}

	@Override
	public void insertInvoker(Invoker invoker) {
		invoker.insertInvoker(invoker);
	}

	@Override
	public void deleteInvoker(Invoker invoker) {
		invoker.deleteInvoker(invoker);
	}

	@Override
	public void setInterface(Class interfaceClass) {
		invoker.setInterface(interfaceClass);
	}

	@Override
	public void destroy() {
		invoker.destroy();
	}

	@Override
	public Response call(Request request) throws RemoteException {
		BinaryFilterChain chain = new BinaryFilterChain().addLast(consumerFilterMap);
		Response result = chain.doFilter(invoker, request, chain);
		return result;
	}

	@Override
	public String toString() {
		return invoker.toString();
	}

	@Override
	public void setDc(DcType dc) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getInvokeKey() {
		// TODO Auto-generated method stub
		return invoker.getInvokeKey();
	}

	@Override
	public void acall(Request request) throws RemoteException {
		// TODO Auto-generated method stub
		invoker.acall(request);
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return invoker.equals(obj);
	}
	
	
}
