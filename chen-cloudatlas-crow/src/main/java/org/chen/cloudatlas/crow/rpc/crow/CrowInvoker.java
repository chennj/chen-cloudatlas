package org.chen.cloudatlas.crow.rpc.crow;

import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.chen.cloudatlas.crow.common.Constants;
import org.chen.cloudatlas.crow.common.URL;
import org.chen.cloudatlas.crow.common.utils.UrlUtil;
import org.chen.cloudatlas.crow.remote.RemoteException;
import org.chen.cloudatlas.crow.remote.exchange.ExchangeClient;
import org.chen.cloudatlas.crow.rpc.Invocation;
import org.chen.cloudatlas.crow.rpc.Invoker;
import org.chen.cloudatlas.crow.rpc.Result;
import org.chen.cloudatlas.crow.rpc.RpcException;
import org.chen.cloudatlas.crow.rpc.impl.RpcInvocation;
import org.chen.cloudatlas.crow.rpc.impl.RpcResult;
import org.chen.cloudatlas.crow.rpc.protocol.AbstractInvoker;

public class CrowInvoker<T> extends AbstractInvoker<T> {

	private final ExchangeClient[] clients;
	
	private int index;
	
	private final String version;
	
	private final ReentrantLock destroyLock = new ReentrantLock();
	
	private final Set<Invoker<?>> invokers;
	
	public CrowInvoker(Class<T> serviceType, URL url, ExchangeClient[] clients){
		this(serviceType, url, clients, null);
	}

	public CrowInvoker(Class<T> serviceType, URL url, ExchangeClient[] clients, Set<Invoker<?>> invokers) {
		super(serviceType, url, new String[]{});
		this.clients = clients;
		this.version = url.getParameter("0.0.0");
		this.invokers = invokers;
	}

	@Override
	protected Result doInvoke(Invocation invocation) throws Exception {
		
		final String methodName = RpcUtil.getMethodName(invocation);
		
		RpcInvocation inv = (RpcInvocation)invocation;
		inv.setAttachment("path", getUrl().getPath());
		inv.setAttachment(Constants.DC, getUrl().getParameter(Constants.DC));
		inv.setAttachment(Constants.PROTOCOL_VERSION, getUrl().getParameter(Constants.PROTOCOL_VERSION));
		inv.setAttachment(Constants.SERVICE_VERSION, getUrl().getParameter(Constants.SERVICE_VERSION));
		inv.setAttachment(Constants.SERVICE_ID, getUrl().getParameter(Constants.SERVICE_ID));
		
		inv = doFilter(inv);
		
		ExchangeClient currentClient;
		if (clients.length == 1){
			currentClient = clients[0];
		} else {
			currentClient = clients[++index % clients.length];
		}
		
		try {
			boolean isAsync = false;
			
			boolean isOneway = Boolean.parseBoolean(UrlUtil.getParameter(getUrl(), Constants.ONE_WAY, String.valueOf(Constants.DEFAULT_ONEWAY)));
			
			int timeout = UrlUtil.getParameter(getUrl(), Constants.TIMEOUT, Constants.DEFAULT_NO_RESPONSE_TIMEOUT);
			
			if (isOneway){
				
				boolean isSent = false;
				currentClient.send(inv,isSent);
				return new RpcResult();
			} else if (isAsync){
				
			}
		} catch (RemoteException e){
			throw new RpcException(
					RpcException.NETWORK_EXCEPTION,
					"failed to invoke remote method: " + invocation.getMethodName() + "\nprovider: " + getUrl(), e);
		}
	}
	
	public Result resultTypeWrapper(Result result, Invocation invocation){
		
		if (invocation.getReturnType() != null){
			((RpcResult)result).setAttachment(Constants.RETURN_TYPE, invocation.getReturnType().getName());
		}
		return result;
	}
}
