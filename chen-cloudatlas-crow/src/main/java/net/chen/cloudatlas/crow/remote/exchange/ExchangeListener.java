package net.chen.cloudatlas.crow.remote.exchange;

import net.chen.cloudatlas.crow.remote.ChannelListener;
import net.chen.cloudatlas.crow.remote.RemoteException;

public interface ExchangeListener extends ChannelListener{
	
	Object reply(ExchangeChannel context, Object request) throws RemoteException;
}
