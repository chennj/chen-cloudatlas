package net.chen.cloudatlas.crow.remote.exchange;

import net.chen.cloudatlas.crow.remote.Channel;
import net.chen.cloudatlas.crow.remote.RemoteException;

public abstract class ExchangeListenerAdapter implements ExchangeListener{

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
	public Object reply(ExchangeChannel context, Object request) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
