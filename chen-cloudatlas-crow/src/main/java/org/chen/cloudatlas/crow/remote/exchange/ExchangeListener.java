package org.chen.cloudatlas.crow.remote.exchange;

import org.chen.cloudatlas.crow.remote.ChannelListener;
import org.chen.cloudatlas.crow.remote.RemoteException;

public interface ExchangeListener extends ChannelListener{
	
	Object reply(ExchangeChannel context, Object request) throws RemoteException;
}
