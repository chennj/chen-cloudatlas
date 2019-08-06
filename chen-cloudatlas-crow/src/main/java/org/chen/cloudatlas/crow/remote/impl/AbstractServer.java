package org.chen.cloudatlas.crow.remote.impl;

import java.net.InetSocketAddress;

import org.chen.cloudatlas.crow.common.URL;
import org.chen.cloudatlas.crow.remote.ChannelListener;
import org.chen.cloudatlas.crow.remote.RemoteException;
import org.chen.cloudatlas.crow.remote.Server;

public abstract class AbstractServer extends AbstractEndpoint implements Server{

	public AbstractServer(URL url, ChannelListener listener){
		super(url,listener);
	}
	
	@Override
	public InetSocketAddress getLocalAddress(){
		return null;
	}
	
	@Override
	public void send(Object message) throws RemoteException{
		
	}
	
	@Override
	public void send(Object message, boolean sent) throws RemoteException{
		
	}
}
