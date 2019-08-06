package org.chen.cloudatlas.crow.remote;

public interface Client extends Channel{

	void connect() throws RemoteException;
	
	void reconnect() throws RemoteException;
	
	void send(Request request) throws RemoteException;
	
	Response sendWithResult(Request request) throws RemoteException;
}
