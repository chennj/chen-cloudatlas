package net.chen.cloudatlas.crow.client.impl;

import java.util.Map;

import net.chen.cloudatlas.crow.client.ServiceController;
import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.DcType;
import net.chen.cloudatlas.crow.config.CrowClientContext;
import net.chen.cloudatlas.crow.config.ReferenceConfig;
import net.chen.cloudatlas.crow.remote.MessageWrapper;
import net.chen.cloudatlas.crow.remote.RemoteException;
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
	
	@Override
	public byte[] call(byte[] requestBytes) throws RemoteException {
		return call(requestBytes,null);
	}

	@Override
	public byte[] call(byte[] requestBytes, Map<String, Object> attachments) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void acall(byte[] requestBytes) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] acall(byte[] requestBytes, Map<String, Object> attachments) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ReferenceConfig getReferenceConfig() {
		// TODO Auto-generated method stub
		return null;
	}

}
