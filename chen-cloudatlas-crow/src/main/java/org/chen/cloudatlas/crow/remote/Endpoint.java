package org.chen.cloudatlas.crow.remote;

import java.net.InetSocketAddress;

import org.chen.cloudatlas.crow.common.URL;

public interface Endpoint {

	URL getUrl();
	
	ChannelListener getChannelListener();
	
	InetSocketAddress getLocalAddress();
	
	void  send(Object message) throws RemoteException;
	
	void send(Object message, boolean send) throws RemoteException;
	
	void shutDown();
	
	void shutDown(int timeout);
	
	boolean isShutDown();
}
