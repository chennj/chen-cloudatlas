package org.chen.cloudatlas.crow.remote.exchange.header;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.chen.cloudatlas.crow.common.Constants;
import org.chen.cloudatlas.crow.common.DcType;
import org.chen.cloudatlas.crow.common.URL;
import org.chen.cloudatlas.crow.common.utils.UrlUtil;
import org.chen.cloudatlas.crow.config.CrowClientContext;
import org.chen.cloudatlas.crow.remote.Channel;
import org.chen.cloudatlas.crow.remote.ChannelListener;
import org.chen.cloudatlas.crow.remote.RemoteException;
import org.chen.cloudatlas.crow.remote.codec.crow.CrowCodecVersion;
import org.chen.cloudatlas.crow.remote.exchange.DefaultFuture;
import org.chen.cloudatlas.crow.remote.exchange.ExchangeChannel;
import org.chen.cloudatlas.crow.remote.exchange.ExchangeListener;
import org.chen.cloudatlas.crow.remote.exchange.ExchangeRequest;
import org.chen.cloudatlas.crow.remote.exchange.ExchangeResponse;
import org.chen.cloudatlas.crow.remote.exchange.ResponseFuture;
import org.chen.cloudatlas.crow.rpc.impl.RpcInvocation;
import org.springframework.util.StringUtils;
import org.tinylog.Logger;

public class HeaderExchangeChannel implements ExchangeChannel{

	private static Map<String, Channel> channels = new ConcurrentHashMap<>();
	
	private final Channel channel;
	
	private volatile boolean shutdown = false;
	
	public HeaderExchangeChannel(Channel channel){
		
		if (null == channel){
			throw new IllegalArgumentException("channel is null");
		}
		this.channel = channel;
	}
	

	public static ExchangeChannel getOrAddChannel(Channel channel) {
		
		if (null == channel){
			return null;
		}
		
		String key = UrlUtil.getAddressKey(channel.getUrl());
		HeaderExchangeChannel ret = (HeaderExchangeChannel)channels.get(key);
		if (null == ret){
			ret = new HeaderExchangeChannel(channel);
			if (channel.isConnected()){
				channels.put(key, ret);
			}
		}
		
		return ret;
	}
	
	public static void removeChannelIfDisconnected(Channel channel){
		
		if (null == channel){
			throw new IllegalArgumentException("channel is null");
		}
		String key = UrlUtil.getAddressKey(channel.getUrl());
		if (StringUtils.isEmpty(key) && !channel.isConnected()){
			channels.remove(key);
		}
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
		send(message, false);
	}

	@Override
	public void send(Object message, boolean send) throws RemoteException {
		
		if (shutdown){
			throw new RemoteException(
					this.getLocalAddress(), 
					null,
					"failed to send message " + message + ",cause: the channel " + this + " is closed!");
		}
		
		if (
				message instanceof ExchangeRequest || 
				message instanceof ExchangeResponse || 
				message instanceof String){
			channel.send(message, send);
		} else {
			// create request
			ExchangeRequest req = new ExchangeRequest();
			req.setOneWay(true);
			req.setRequest(true);
			req.setData(message);
			
			if (message instanceof RpcInvocation){
				String protocolVersion = ((RpcInvocation)message).getAttachment(Constants.PROTOCOL_VERSION);
				CrowCodecVersion version = CrowCodecVersion.getCodecVersion(protocolVersion);
				req.setMajorVersion(version.getMajorByte());
				req.setMinorVersion(version.getMinorByte());
				String serviceId = ((RpcInvocation)message).getAttachment(Constants.SERVICE_ID);
				String serviceVersion = ((RpcInvocation)message).getAttachment(Constants.SERVICE_VERSION);
				req.setServiceVersion(serviceVersion);
				req.setServiceId(serviceId);
			}
			
			channel.send(req);
		}
		
	}

	@Override
	public void shutDown() {
		try {
			channel.shutDown();
		} catch (Exception e){
			Logger.error("exception while shutting down {}", e);
		}
	}

	@Override
	public void shutDown(int timeout) {
		shutDown();
	}

	@Override
	public boolean isShutDown() {
		return channel.isShutDown();
	}

	@Override
	public ResponseFuture request(Object request) throws RemoteException {
		return request(request, 1000);
	}

	@Override
	public ResponseFuture request(Object request, int timeout) throws RemoteException {
		
		if (shutdown){
			throw new RemoteException(
					this.getLocalAddress(), 
					null, 
					"failed to send request " + request + ", cause: the channel " + this + " is closed!");
		}
		
		// create request
		ExchangeRequest req = new ExchangeRequest();
		req.setOneWay(true);
		req.setRequest(true);
		req.setData(request);
		
		if (request instanceof RpcInvocation){
			String protocolVersion = ((RpcInvocation)request).getAttachment(Constants.PROTOCOL_VERSION);
			CrowCodecVersion version = CrowCodecVersion.getCodecVersion(protocolVersion);
			req.setMajorVersion(version.getMajorByte());
			req.setMinorVersion(version.getMinorByte());
			String serviceId = ((RpcInvocation)request).getAttachment(Constants.SERVICE_ID);
			serviceId = (serviceId == null ? "" : serviceId);
			String serviceVersion = ((RpcInvocation)request).getAttachment(Constants.SERVICE_VERSION);
			req.setServiceVersion(serviceVersion);
			req.setServiceId(serviceId);
			if (CrowClientContext.getConfig() != null){
				req.setSourceDc((byte)CrowClientContext.getConfig().getApplicationConfig().getDc().toInt());
			} else {
				req.setSourceDc((byte)DcType.SHANGHAI.toInt());
			}
		}
		
		DefaultFuture future = new DefaultFuture(channel, req, timeout);
		try {
			channel.send(req);
		} catch (RemoteException e){
			Logger.error("error while sending request {}",e);
			future.cancel();
			throw e;
		}
		
		return future;
	}

	@Override
	public ExchangeListener getExchangeListener() {
		return (ExchangeListener) channel.getChannelListener();
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = prime + (channel==null ? 0 : channel.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		
		if (this == obj){
			return true;
		}
		
		if (null == obj){
			return false;
		}
		
		if (getClass() != obj.getClass()){
			return false;
		}
		
		HeaderExchangeChannel other = (HeaderExchangeChannel)obj;
		if (null == channel){
			if (other.channel != null){
				return false;
			}
		} else if (!channel.equals(other.channel)){
			return false;
		}
		
		return true;
	}


	@Override
	public String toString() {
		return channel.toString();
	}
	
}
