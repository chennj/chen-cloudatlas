package net.chen.cloudatlas.crow.remote.exchange;

import net.chen.cloudatlas.crow.remote.RemoteException;

public interface ResponseFuture {

	Object get() throws RemoteException;
	
	Object get(int timeout) throws RemoteException;
	
	void setCallback(ResponseCallback callback);
	
	boolean isDone();
}
