package org.chen.cloudatlas.crow.remote.impl;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.chen.cloudatlas.crow.common.URL;
import org.chen.cloudatlas.crow.common.utils.UrlUtil;
import org.chen.cloudatlas.crow.config.CrowClientContext;
import org.chen.cloudatlas.crow.remote.Channel;
import org.chen.cloudatlas.crow.remote.ChannelListener;
import org.chen.cloudatlas.crow.remote.ChannelRegistry;
import org.chen.cloudatlas.crow.remote.Message;
import org.tinylog.Logger;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;

public class NettyServerChannelHandler extends ChannelDuplexHandler {

	private final Map<String, Channel> channels = new ConcurrentHashMap<>();
	
	private final URL url;
	
	private ChannelListener listener;
	
	private ChannelGroup channelGroup;
	
	public NettyServerChannelHandler(URL url, ChannelListener listener, ChannelGroup channelGroup){
		
		if (null == url){
			throw new IllegalArgumentException("url is null");
		}
		
		if (null == listener){
			throw new IllegalArgumentException("listener is null");
		}
		
		this.url = url;
		this.listener = listener;
		this.channelGroup = channelGroup;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {

		channelGroup.add(ctx.channel());
		
		NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url, listener);
		try {
			if (null != channel){
				channels.put(toAddressString((InetSocketAddress)ctx.channel().remoteAddress()), channel);
				listener.connected(channel);
			}
		} finally{
			NettyChannel.removeChannelIfDisconnected(ctx.channel());
			ChannelRegistry.registerChannel(ctx.channel());
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		
		Message message = (Message)msg;
		if (message.isHeartbeat()){
			return;
		}
		
		NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url, listener);
		try {
			listener.received(channel, message);
		} finally {
			NettyChannel.removeChannelIfDisconnected(ctx.channel());
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		
		NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url, listener);
		try {
			listener.caught(channel, cause);
		} finally {
			NettyChannel.removeChannelIfDisconnected(ctx.channel());
		}
		
		io.netty.channel.Channel netty_channel = ctx.channel();
		SocketAddress address = netty_channel.remoteAddress();
		String ipAndPort = UrlUtil.getAddressKey((InetSocketAddress)address);
		
		// 发现Channel异常，且是本地配置引用远端的Channel（自己是客户端），
		// 则放入重连列表中，供ChannelFixer重连
		if (CrowClientContext.isRemoteEnd(ipAndPort)){
			ChannelRegistry.invalidateChannel(netty_channel);
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		
		NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url, listener);
		try {
			channels.remove(toAddressString((InetSocketAddress)ctx.channel().remoteAddress()));
			listener.disconnected(channel);
		} finally {
			NettyChannel.removeChannelIfDisconnected(ctx.channel());
			ChannelRegistry.unregisterChannel(ctx.channel());
		}
		
		Logger.info("server {} has lose connection from remote client {}",
				ctx.channel().localAddress().toString(),
				ctx.channel().remoteAddress().toString());
	}

	private static String toAddressString(InetSocketAddress address) {
		
		return address.getAddress().getHostAddress() + ":" + address.getPort();
	}
	
}
