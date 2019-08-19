package org.chen.cloudatlas.crow.server;

import java.util.List;

import org.chen.cloudatlas.crow.common.Constants;
import org.chen.cloudatlas.crow.common.DcType;
import org.chen.cloudatlas.crow.common.URL;
import org.chen.cloudatlas.crow.common.exception.MethodNotImplException;
import org.chen.cloudatlas.crow.common.utils.NameableServiceLoader;
import org.chen.cloudatlas.crow.config.CrowServerContext;
import org.chen.cloudatlas.crow.config.ServiceConfig;
import org.chen.cloudatlas.crow.filter.BinaryFilter;
import org.chen.cloudatlas.crow.filter.BinaryFilterChain;
import org.chen.cloudatlas.crow.remote.AbstractCrowControlListener;
import org.chen.cloudatlas.crow.remote.Channel;
import org.chen.cloudatlas.crow.remote.RemoteException;
import org.chen.cloudatlas.crow.remote.Request;
import org.chen.cloudatlas.crow.remote.Response;
import org.chen.cloudatlas.crow.remote.support.bthead.BtheadMessage;
import org.chen.cloudatlas.crow.remote.support.crow.CrowRequest;
import org.chen.cloudatlas.crow.remote.support.crow.CrowResponse;
import org.chen.cloudatlas.crow.remote.support.crow.CrowStatus;
import org.chen.cloudatlas.crow.rpc.Invocation;
import org.chen.cloudatlas.crow.rpc.Invoker;
import org.chen.cloudatlas.crow.rpc.Result;
import org.chen.cloudatlas.crow.rpc.RpcException;
import org.chen.cloudatlas.crow.rpc.SubInvoker;
import org.tinylog.Logger;

/**
 * <b><font color=red>
 * 有待完成
 * </font></b>
 * @author chenn
 *
 */
public abstract class AbstractServerPayloadListener extends AbstractCrowControlListener implements PayloadListener{

	@Override
	public void connected(Channel context) throws RemoteException {
		Logger.trace("connected: " + context.getRemoteAddress());
	}

	@Override
	public void disconnected(Channel context) throws RemoteException {
		Logger.trace("disconnected: " + context.getRemoteAddress());
	}

	@Override
	public void sent(Channel context, Object message) throws RemoteException {
		Logger.trace("sent: " + context.getRemoteAddress() + ", message: " + message);
	}

	@Override
	public void received(Channel context, Object message) throws RemoteException {

		if (isRejected(context, message, CrowResponse.class)){
			//被黑名单拦截
			return;
		}
		
		if (isThrottled(context, message, CrowResponse.class)){
			//被流量控制拦截
			return;
		}
		
		Request request = (Request) message;
		
		internalHandle(request, context);
	}

	@Override
	public void caught(Channel context, Throwable exception) throws RemoteException {
		Logger.trace("exception caught: " + exception);
	}

	@Override
	public abstract byte[] handle(String serviceId, String serviceVersion, byte[] requestBytes, DcType sourceDc);

	private void internalHandle(Request request, Channel channel) throws RemoteException {
		
		//如果是heartbeat,则直接返回应答报文，与应用无关
		if (request.isHeartbeat()){
			Logger.debug("Heartbeat received from " + channel.getRemoteAddress());
			return;
		} else {
			SubInvoker subInvoker = new InnerInvokerWrapper(new InnerInvoker(channel.getUrl()));
			
			Response response = subInvoker.call(request);
			
			if (response != null){
				channel.send(response);
			}
		}
	}

	public class InnerInvoker implements SubInvoker{

		private URL url;
		
		private String ipAndPort;
		
		public InnerInvoker(URL url){
			this.url = url;
			this.ipAndPort = url.getHostAndPort();
		}
		
		@Override
		public boolean isAvailable() {
			return false;
		}

		@Override
		public Class getInterface() {
			return null;
		}

		@Override
		public Result invoke(Invocation invocation) throws RpcException {
			return null;
		}

		@Override
		public URL getUrl() {
			return url;
		}

		@Override
		public void insertInvoker(Invoker invoker) {
			
		}

		@Override
		public void deleteInvoker(Invoker invoker) {
			
		}

		@Override
		public void setInterface(Class interfaceClass) {
			
		}

		@Override
		public void destroy() {
			
		}

		@Override
		public void setDc(DcType dc) {
		}

		@Override
		public String getInvokeKey() {
			return null;
		}

		@Override
		public Response call(Request request) throws RemoteException {
			
			Response response = null;
			if (request instanceof CrowRequest){
				response = doWithCrowProtocol(request);
			} else if (request instanceof BtheadMessage){
				response = doWithBtheadProtocol(request);
			} else {
				response = doWithExtendedProtocol(request);
			}
			return response;
		}

		@Override
		public void acall(Request request) throws RemoteException {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public class InnerInvokerWrapper implements SubInvoker{

		private List<BinaryFilter> providerFilterMap 
				= NameableServiceLoader.getLoader(BinaryFilter.class).getActiveServices(Constants.PROVIDER);
		
		private InnerInvoker invoker;
		
		public InnerInvokerWrapper(InnerInvoker invoker){
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
		public void setDc(DcType dc) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String getInvokeKey() {
			return invoker.getInvokeKey();
		}

		@Override
		public Response call(Request request) throws RemoteException {
			BinaryFilterChain chain = new BinaryFilterChain().addLast(providerFilterMap);
			Response result = chain.doFilter(invoker, request, chain);
			return result;
		}

		@Override
		public void acall(Request request) throws RemoteException {
			invoker.acall(request);
		}
		
	}

	private Response doWithCrowProtocol(Request request) throws RemoteException{
		
		CrowResponse resp;
		CrowRequest req = (CrowRequest)request;
		
		/**
		 * step1. 先校验service是否存在
		 */
		String serviceId = req.getServiceId();
		String serviceVersion = req.getServiceVersion();
		
		ServiceConfig serviceConfig = CrowServerContext.getServiceConfig(serviceId, serviceVersion);
		
		if (null == serviceConfig){
			Logger.error(Constants.ERR_801,serviceId,serviceVersion);
			resp = new CrowResponse(req);
			resp.setStatus(CrowStatus.SERVICE_NOT_FOUND);
			return resp;
		}
		
		/**
		 * step2. 处理
		 */
		if (!req.isOneWay()){
			// 如果是同步，先处理，后write
			resp = handle(req);
			resp.setRequestId(req.getRequestId());
			resp.setSourceDc((byte)CrowServerContext.getConfig().getApplicationConfig().getDc().toInt());
			return resp;
		} else {
			// 如果是异步，先应答回去，然后再handle
			// resp = new CrowResponse(req);
			// channel.write(resp);
			
			// 这里丢给应用处理，处理完暂时不做处理了
			handle(req);
			return null;
		}
	}

	public CrowResponse handle(CrowRequest req) {

		CrowResponse result = new CrowResponse(req);
		result.setResponseBytes(
				handle(
						req.getServiceId(),
						req.getServiceVersion(),
						req.getRequestBytes(),
						DcType.fromInt((int)req.getSourceDc()))
				);
		return result;
	}

	public Response doWithExtendedProtocol(Request request) throws RemoteException {
		throw new MethodNotImplException();
	}

	public Response doWithBtheadProtocol(Request request) throws RemoteException {
		throw new MethodNotImplException();
	}
}
