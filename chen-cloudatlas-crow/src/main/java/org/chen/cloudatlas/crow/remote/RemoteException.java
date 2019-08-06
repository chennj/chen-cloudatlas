package org.chen.cloudatlas.crow.remote;

import java.net.InetSocketAddress;

public class RemoteException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private InetSocketAddress localAddress;
	private InetSocketAddress remoteAddress;
	
	public RemoteException(String msg){
		super(msg);
	}
	
	public RemoteException(Throwable cause){
		super(cause);
	}
	
	public RemoteException(String msg, Throwable cause){
		super(msg,cause);
	}
	
	public RemoteException(Channel channel, String msg){
		this(channel==null ? null : channel.getLocalAddress(), channel==null ? null : channel.getRemoteAddress(),msg);
	}
	
	public RemoteException(InetSocketAddress localAddress, InetSocketAddress remoteAddress, String msg){
		super(msg+">>>"+localAddress.getHostName()+","+remoteAddress.getHostName());
	}

	public RemoteException(Channel channel, String msg, Throwable cause) {
		
		this(channel == null ? null :channel.getLocalAddress(), channel == null ? null : channel.getRemoteAddress(),msg,cause);
	}

	public RemoteException(InetSocketAddress localAddress, InetSocketAddress remoteAddress, String msg, Throwable cause) {
		super(msg,cause);
		this.localAddress = localAddress;
		this.remoteAddress = remoteAddress;
	}

	public InetSocketAddress getLocalAddress() {
		return localAddress;
	}

	public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}
	
	
}
