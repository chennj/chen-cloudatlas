package org.chen.cloudatlas.crow.remote.exchange;

import org.chen.cloudatlas.crow.remote.Channel;
import org.chen.cloudatlas.crow.remote.RemoteException;

public interface ExchangeChannel extends Channel{

	ResponseFuture request(Object request) throws RemoteException;
	
	ResponseFuture request(Object request, int timeout) throws RemoteException;
	
	ExchangeListener getExchangeListener();
}
