package net.chen.cloudatlas.crow.remote.impl;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.tinylog.Logger;

import io.netty.channel.ChannelFuture;
import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.remote.ChannelListener;
import net.chen.cloudatlas.crow.remote.RemoteException;

public class NettyChannel extends AbstractEndpoint implements net.chen.cloudatlas.crow.remote.Channel{

	private static final ConcurrentMap<io.netty.channel.Channel, NettyChannel> channelMap = 
			new ConcurrentHashMap<>();
	
	private final io.netty.channel.Channel netty_channel;
	
	//private final Map<String, Object> attributes = new ConcurrentHashMap<String, Object>();
	
	private NettyChannel(io.netty.channel.Channel netty_channel, URL url, ChannelListener listener){
		
		super(url, listener);
		
		if (null == netty_channel){
			throw new IllegalArgumentException("netty channel is null");
		}
		
		this.netty_channel = netty_channel;
	}
	
	public static NettyChannel getOrAddChannel(io.netty.channel.Channel netty_channel, URL url, ChannelListener listener){
		
		if (null == netty_channel){
			return null;
		}
		
		NettyChannel ret = channelMap.get(netty_channel);
		if (null == ret){
			
			NettyChannel nc = new NettyChannel(netty_channel, url, listener);
			if (netty_channel.isActive()){
				
				ret = channelMap.putIfAbsent(netty_channel, nc);
			}
			if (null == ret){
				
				ret = nc;
			}
		}
		
		return ret;
	}

	static NettyChannel get(io.netty.channel.Channel netty_channel){
		return channelMap.get(netty_channel);
	}
	
	public static void removeChannelIfDisconnected(io.netty.channel.Channel netty_channel){
		if (netty_channel != null && !netty_channel.isActive()){
			channelMap.remove(netty_channel);
		}
	}
	
	@Override
	public InetSocketAddress getLocalAddress() {
		
		return (InetSocketAddress) netty_channel.localAddress();
	}

	@Override
	public void send(Object message) throws RemoteException {
		
		if (isShutDown()){
			throw new RemoteException(this, "Failed to send message"
					+ (message == null ? "" : message.getClass().getName()) + ":" + message
					+ ", cause: Channel closed. channel: " + getLocalAddress() + " ->" + getRemoteAddress());
		}
		send(message, false);
	}

	@Override
	public void send(Object message, boolean sent) throws RemoteException {
		
		boolean success = true;
		int timeout = 0;
		try {
			ChannelFuture future = netty_channel.write(message);
			if (sent){
				timeout = 0;
				success = future.await(timeout);
			}
			Throwable cause = future.cause();
			if (null != cause){
				throw new RemoteException(cause);
			}
		} catch (Exception e){
			throw new RemoteException(this, "failed to send message " + message + 
					" to " + this.getRemoteAddress() +
					", cause:" + e.getMessage(),
					e);
		}
		
		if (!success){
			throw new RemoteException(this, "failed to send message " + message + 
					" to " + this.getRemoteAddress() +
					" in timeout(" + timeout + "ms) limit");
		}
		
	}

	@Override
	public void shutDown() {
		
		try {
			super.shutDown();
		} catch (Exception e){
			Logger.error("Exception while shutting down ", e);
		}
		
		try {
			removeChannelIfDisconnected(netty_channel);
		} catch (Exception e){
			Logger.error("Exception while checking channel ",e);
		}
		
		try {
			netty_channel.close();
		} catch (Exception e){
			Logger.error("exception while closing channel ",e);
		}
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		
		return (InetSocketAddress) netty_channel.remoteAddress();
	}

	@Override
	public boolean isConnected() {
		
		return netty_channel.isActive();
	}
	
	@Override
	public int hashCode(){
		
		final int prime = 31;
		int result = 1;
		result = prime * result + (netty_channel == null ? 0 : netty_channel.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj){
		
		if (obj == this){
			return true;
		}
		
		if (obj == null){
			return false;
		}
		
		if (getClass() != obj.getClass()){
			return false;
		}
		
		NettyChannel other = (NettyChannel) obj;
		if (null == netty_channel){
			if (other.netty_channel != null){
				return false;
			}
		} else if (!netty_channel.equals(other.netty_channel)){
			return false;
		}
		return true;
	}
	
	@Override
	public String toString(){
		
		return "NettyChannel [netty_channel=" + netty_channel + "]";
	}
}
