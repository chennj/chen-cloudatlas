package org.chen.cloudatlas.crow.client;

import org.chen.cloudatlas.crow.common.DcType;
import org.chen.cloudatlas.crow.remote.Channel;
import org.chen.cloudatlas.crow.remote.RemoteException;
import org.chen.cloudatlas.crow.server.PayloadListener;

public class NoopChannelListener implements PayloadListener{

	@Override
	public void connected(Channel context) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disconnected(Channel context) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sent(Channel context, Object message) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void received(Channel context, Object message) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void caught(Channel context, Throwable exception) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] handle(String serviceId, String serviceVersion, byte[] requestBytes, DcType sourceDc) {
		// TODO Auto-generated method stub
		return new byte[0];
	}

}
