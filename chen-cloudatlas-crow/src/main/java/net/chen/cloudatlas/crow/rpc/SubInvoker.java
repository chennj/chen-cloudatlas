package net.chen.cloudatlas.crow.rpc;

import net.chen.cloudatlas.crow.remote.RemoteException;
import net.chen.cloudatlas.crow.remote.Request;
import net.chen.cloudatlas.crow.remote.Response;

/**
 * 让框架同时支持byte[]的方式,选择继承{@link net.chen.cloudatlas.crow.rpc.Invoker}
 * @author chenn
 *
 */
@SuppressWarnings("rawtypes")
public interface SubInvoker extends Invoker{

	Response call(Request request)  throws RemoteException;
	
	void acall(Request request) throws RemoteException;
}
