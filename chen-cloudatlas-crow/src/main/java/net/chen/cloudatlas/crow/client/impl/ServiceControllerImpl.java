package net.chen.cloudatlas.crow.client.impl;

import java.util.Map;

import net.chen.cloudatlas.crow.client.ServiceController;
import net.chen.cloudatlas.crow.config.ReferenceConfig;
import net.chen.cloudatlas.crow.remote.RemoteException;

public class ServiceControllerImpl extends AbstractServiceControllerImpl implements ServiceController{

	public ServiceControllerImpl(String serviceId, String serviceVersion) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public byte[] call(byte[] requestBytes) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
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
