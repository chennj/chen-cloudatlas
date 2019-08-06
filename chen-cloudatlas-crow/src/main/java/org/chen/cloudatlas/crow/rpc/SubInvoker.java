package org.chen.cloudatlas.crow.rpc;

import org.chen.cloudatlas.crow.remote.RemoteException;
import org.chen.cloudatlas.crow.remote.Request;
import org.chen.cloudatlas.crow.remote.Response;

@SuppressWarnings("rawtypes")
public interface SubInvoker extends Invoker{

	Response call(Request request)  throws RemoteException;
	
	void acall(Request request) throws RemoteException;
}
