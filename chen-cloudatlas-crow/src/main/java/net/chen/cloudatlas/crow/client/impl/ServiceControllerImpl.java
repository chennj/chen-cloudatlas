package net.chen.cloudatlas.crow.client.impl;

import java.util.HashMap;
import java.util.Map;

import net.chen.cloudatlas.crow.client.ServiceController;
import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.DcType;
import net.chen.cloudatlas.crow.config.CrowClientContext;
import net.chen.cloudatlas.crow.config.ReferenceConfig;
import net.chen.cloudatlas.crow.remote.MessageWrapper;
import net.chen.cloudatlas.crow.remote.RemoteException;
import net.chen.cloudatlas.crow.remote.Request;
import net.chen.cloudatlas.crow.remote.Response;
import net.chen.cloudatlas.crow.rpc.SubInvoker;

public class ServiceControllerImpl extends AbstractServiceControllerImpl implements ServiceController{

	private ReferenceConfig config;
	
	private MessageWrapper wrapper;
	
	private SubInvoker clusterInvoker;
	
	public ServiceControllerImpl(String serviceId, String serviceVersion) {
		this(CrowClientContext.getReferenceConfig(serviceId, serviceVersion));
	}
	
	public ServiceControllerImpl(String serviceId){
		this(serviceId, Constants.DEFAULT_SERVICE_VERSION);
	}

	public ServiceControllerImpl(ReferenceConfig config){
		this.config = config;
		wrapper = MessageWrapper.get(config.getProtocol().getCodec());
		clusterInvoker = getClusterInvoker();
	}
	
	public void setDc(DcType dc){
		clusterInvoker.setDc(dc);
	}
	
	/**
	 * 检查调用的reference 配置的oneway是否和调用方式一致
	 * @param isOneway call:isOneway=false, acall:isOneway=true;
	 */
	private void checkOnewayConfig(boolean isOneway){
		
		if (isOneway != config.isOneway()){
			throw new RuntimeException("service " + this.getReferenceConfig().getServiceId()
					+ " oneway config does not match the way it's called."
					+ " correct usage: oneWay=true, use acall(), otherwise , use call() instead.");
		}
	}
	
	public Response call(Request request) throws RemoteException{
		
		checkOnewayConfig(false);
		doWrap(request);
		checkBroadcast(clusterInvoker);
		return clusterInvoker.call(request);
	}
	
	public void acall(Request request) throws RemoteException{
		
		checkOnewayConfig(true);
		doWrap(request);
		clusterInvoker.acall(request);
	}
	
	private void doWrap(Request request) throws RemoteException{
		
		Map<String, Object> attachments = new HashMap<>();
		attachments.put(Constants.PROTOCOL_VERSION, config.getProtocol().getVersion());
		attachments.put(Constants.SERVICE_ID,config.getProtocol().getVersion());
		attachments.put(Constants.SERVICE_VERSION,config.getServiceVersion());
		attachments.put(Constants.CALLER_ID,CrowClientContext.getApplicationName());
		attachments.put(Constants.SERIALIZATION_TYPE,config.getProtocol().getSerializationType());
		attachments.put(Constants.COMPRESS_ALGORITHM,config.getProtocol().getCompressAlgorithm());
		attachments.put(Constants.DC,config.getDc().getText());
		
		request = wrapper.wrapRequest(request, attachments);
	}
	
	private void checkBroadcast(SubInvoker clusterInvoker) throws RemoteException{
		
	}
	
	@Override
	public byte[] call(byte[] requestBytes) throws RemoteException {
		return call(requestBytes,null);
	}

	@Override
	public byte[] call(byte[] requestBytes, Map<String, Object> attachments) throws RemoteException {
		
		checkOnewayConfig(false);
		
		Request request = wrapper.wrapRequest(requestBytes, attachments);
		Response response = call(request);
		
		return wrapper.decomposeResponse(response);
	}

	@Override
	public void acall(byte[] requestBytes) throws RemoteException {
		acall(requestBytes,null);
	}

	@Override
	public void acall(byte[] requestBytes, Map<String, Object> attachments) throws RemoteException {
		acall(wrapper.wrapRequest(requestBytes, attachments));
	}

	@Override
	protected ReferenceConfig getReferenceConfig() {
		// 每次都从context中拿新的
		return CrowClientContext.getReferenceConfig(config.getRealServiceId(), config.getServiceVersion());
	}

}
