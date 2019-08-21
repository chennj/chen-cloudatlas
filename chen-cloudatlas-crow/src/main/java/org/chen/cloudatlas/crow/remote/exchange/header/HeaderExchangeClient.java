package org.chen.cloudatlas.crow.remote.exchange.header;

import java.net.InetSocketAddress;

import org.chen.cloudatlas.crow.common.URL;
import org.chen.cloudatlas.crow.remote.ChannelListener;
import org.chen.cloudatlas.crow.remote.Client;
import org.chen.cloudatlas.crow.remote.RemoteException;
import org.chen.cloudatlas.crow.remote.Request;
import org.chen.cloudatlas.crow.remote.Response;
import org.chen.cloudatlas.crow.remote.exchange.ExchangeChannel;
import org.chen.cloudatlas.crow.remote.exchange.ExchangeClient;
import org.chen.cloudatlas.crow.remote.exchange.ExchangeListener;
import org.chen.cloudatlas.crow.remote.exchange.ResponseFuture;

public class HeaderExchangeClient implements ExchangeClient{

	private final Client client;
	
	private final ExchangeChannel channel;
	
	public HeaderExchangeClient(Client client){
		
		if (null == client){
			throw new IllegalArgumentException("client is null");
		}
		this.client = client;
		this.channel = new HeaderExchangeChannel(client);
	}
	
	
	@Override
	public void connect() throws RemoteException {
		if (null != client && !this.client.isConnected()){
			this.client.connect();
		}
	}

	@Override
	public void reconnect() throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send(Request request) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Response sendWithResult(Request request) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return channel.getRemoteAddress();
	}

	@Override
	public boolean isConnected() {
		return channel.isConnected();
	}

	@Override
	public URL getUrl() {
		return channel.getUrl();
	}

	@Override
	public ChannelListener getChannelListener() {
		return channel.getChannelListener();
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		return channel.getLocalAddress();
	}

	@Override
	public void send(Object message) throws RemoteException {
		channel.send(message);
	}

	@Override
	public void send(Object message, boolean send) throws RemoteException {
		channel.send(message, send);
	}

	@Override
	public void shutDown() {
		channel.shutDown();
	}

	@Override
	public void shutDown(int timeout) {
		channel.shutDown(timeout);
	}

	@Override
	public boolean isShutDown() {
		return channel.isShutDown();
	}

	@Override
	public ResponseFuture request(Object request) throws RemoteException {
		return channel.request(request);
	}

	@Override
	public ResponseFuture request(Object request, int timeout) throws RemoteException {
		return channel.request(request, timeout);
	}

	@Override
	public ExchangeListener getExchangeListener() {
		return (ExchangeListener)channel.getExchangeListener();
	}

}
