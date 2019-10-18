package net.chen.cloudatlas.crow.rpc.crow;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.util.StringUtils;
import org.tinylog.Logger;

import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.common.utils.UrlUtil;
import net.chen.cloudatlas.crow.remote.RemoteException;
import net.chen.cloudatlas.crow.remote.exchange.ExchangeClient;
import net.chen.cloudatlas.crow.remote.exchange.ResponseFuture;
import net.chen.cloudatlas.crow.rpc.Invocation;
import net.chen.cloudatlas.crow.rpc.InvocationFilterContext;
import net.chen.cloudatlas.crow.rpc.Invoker;
import net.chen.cloudatlas.crow.rpc.Result;
import net.chen.cloudatlas.crow.rpc.RpcException;
import net.chen.cloudatlas.crow.rpc.impl.RpcInvocation;
import net.chen.cloudatlas.crow.rpc.impl.RpcResult;
import net.chen.cloudatlas.crow.rpc.protocol.AbstractInvoker;
import net.chen.cloudatlas.crow.rpc.utils.RpcUtil;

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
				ResponseFuture f = currentClient.request(inv,timeout);
				return new RpcResult();
			} else {
				ResponseFuture f = currentClient.request(inv, timeout);
				return resultTypeWrapper((Result)f.get(), invocation);
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
	
	/**
	 * 将用户在InvocationFilterContext线程变量中的attachments赋值到invocation中
	 * @param invocation
	 * @return
	 */
	private RpcInvocation doFilter(RpcInvocation invocation){
		
		String cstVersion = "version";
		String cstProtocolVersion = "protocolVersion";
		
		Map<String, String> attachments = InvocationFilterContext.getContext().getSubAttachments();
		if (null == attachments || attachments.size() == 0){
			return invocation;
		}
		
		try {
			for (Map.Entry<String, String> entry : attachments.entrySet()){
				
				if (	entry.getKey() != null &&
						StringUtils.isEmpty(invocation.getAttachment(entry.getKey())) && 
						!entry.getKey().equals(cstProtocolVersion) && 
						!entry.getKey().equals(cstVersion)){
					invocation.getAttachments().put(entry.getKey(), entry.getValue());
				} else {
					Logger.error("error while InvocationFilter.getSubAttachments attachments key cannot be {}",entry.getKey());
				}
			}
		} catch (Exception e){
			Logger.error("error while InvocationFilter put attachments,msg:{}",e.getMessage());
		}
		
		Logger.debug("after invocationfilter, invocation:{}",invocation.toString());
		
		return invocation;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return super.equals(obj);
	}
	
	
}
