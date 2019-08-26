package net.chen.cloudatlas.crow.remote;

/**
 * 
 * @author chenn
 *
 */
public interface ChannelListener {

	void connected(Channel context) throws RemoteException;
	
	void disconnected(Channel context) throws RemoteException;
	
	void sent(Channel context, Object message) throws RemoteException;
	
	void received(Channel context, Object message) throws RemoteException;
	
	void caught(Channel context, Throwable exception) throws RemoteException;
}
