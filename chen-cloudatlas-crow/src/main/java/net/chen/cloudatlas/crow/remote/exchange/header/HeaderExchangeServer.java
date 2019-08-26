package net.chen.cloudatlas.crow.remote.exchange.header;

import java.net.InetSocketAddress;

import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.remote.ChannelListener;
import net.chen.cloudatlas.crow.remote.RemoteException;
import net.chen.cloudatlas.crow.remote.Server;
import net.chen.cloudatlas.crow.remote.exchange.ExchangeChannel;
import net.chen.cloudatlas.crow.remote.exchange.ExchangeServer;

public class HeaderExchangeServer implements ExchangeServer{

	private final Server server;
	
	private volatile boolean shutdown = false;
	
	public HeaderExchangeServer(Server server){
		
		if (null == server){
			throw new IllegalArgumentException("server is null");
		}
		this.server = server;
	}
	
	public Server getServer(){
		return server;
	}
	
	@Override
	public void bind() {
		server.bind();
	}

	@Override
	public boolean isBound() {
		return server.isBound();
	}

	@Override
	public void setChannelListener(ChannelListener listener) {
		server.setChannelListener(listener);
	}

	@Override
	public URL getUrl() {
		return server.getUrl();
	}

	@Override
	public ChannelListener getChannelListener() {
		return server.getChannelListener();
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		return server.getLocalAddress();
	}

	@Override
	public void send(Object message) throws RemoteException {
		if (shutdown){
			throw new RemoteException(
					this.getLocalAddress(),
					null,
					"failed to send message " + message + ",cause: the server " + getLocalAddress() + " is closed!");
		}
		server.send(message);
	}

	@Override
	public void send(Object message, boolean send) throws RemoteException {
		if (shutdown){
			throw new RemoteException(
					this.getLocalAddress(),
					null,
					"failed to send message " + message + ",cause: the server " + getLocalAddress() + " is closed!");
		}
		server.send(message,send);
	}

	@Override
	public void shutDown() {
		if (shutdown){
			return;
		}
		shutdown = true;
		server.shutDown();
	}

	@Override
	public void shutDown(int timeout) {
		if (shutdown){
			return;
		}
		shutdown = true;
		server.shutDown(timeout);
	}

	@Override
	public boolean isShutDown() {
		return server.isShutDown();
	}

	@Override
	public ExchangeChannel getExchangeChannel(InetSocketAddress remoteAddress) {
		// TODO Auto-generated method stub
		return null;
	}

}
