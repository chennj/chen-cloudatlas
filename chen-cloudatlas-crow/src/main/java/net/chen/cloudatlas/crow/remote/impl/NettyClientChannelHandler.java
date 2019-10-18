package net.chen.cloudatlas.crow.remote.impl;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.tinylog.Logger;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.common.utils.UrlUtil;
import net.chen.cloudatlas.crow.config.CrowClientContext;
import net.chen.cloudatlas.crow.remote.Channel;
import net.chen.cloudatlas.crow.remote.ChannelListener;
import net.chen.cloudatlas.crow.remote.ChannelRegistry;
import net.chen.cloudatlas.crow.remote.Message;
import net.chen.cloudatlas.crow.remote.Response;

/**
 * 业务逻辑处理器<br>
 * @author chenn
 *
 */
@ChannelHandler.Sharable
public class NettyClientChannelHandler extends ChannelDuplexHandler{

	/**
	 * 自定义的Channel
	 */
	private final Map<String, Channel> channels = new ConcurrentHashMap<>();
	
	private final URL url;
	
	private String ipAndPort;
	
	private ChannelListener listener;
	
	public NettyClientChannelHandler(URL url, ChannelListener listener){
		
		if (null == url){
			throw new IllegalArgumentException("url is null");
		}
		
		this.url = url;
		this.ipAndPort = UrlUtil.getAddressKey((InetSocketAddress)this.url.getSocketAddress());
		this.listener = listener;
	}
	
	@Override
	public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress,
                        SocketAddress localAddress, ChannelPromise promise) throws Exception{
		// cache channel
		NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url, listener);
		try {
			if (null != channel){
				channels.put(toAddressString((InetSocketAddress)ctx.channel().remoteAddress()), channel);
			}
			if (null != listener){
				listener.connected(channel);
			}
		} finally{
			NettyChannel.removeChannelIfDisconnected(ctx.channel());
		}
		
		Logger.info(ctx.channel().remoteAddress() + " connected");
	}

	@Override
	public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
		
		NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url, listener);
		try {
			channels.remove(toAddressString((InetSocketAddress) ctx.channel().remoteAddress()));
			if (null != listener){
				listener.disconnected(channel);
			}
		} finally {
			NettyChannel.removeChannelIfDisconnected(ctx.channel());
			// add to retry list for channel fixing
			ChannelRegistry.invalidateChannel(ctx.channel());
			NettyClient.handleErrorChannelReqToken(channel);
		}
		
		Logger.info(ctx.channel().remoteAddress() + " disconnected");
		Logger.info("client {} has lose connection from remote server {}",ctx.channel().localAddress(), ctx.channel().remoteAddress());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		 	 	
		// 服务端也可以发送心跳给客户端，这样收到的报文，可以为请求，也可能为应答
		Message message = (Message)msg;
		if (message.isHeartbeat()){
			//心跳报文直接跳过
			return;
		}
		
		// 在crow decoder后，我们可以获取到Crow Response Object instance
		Response response = (Response)msg;
		
		NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url, listener);
		try {
			if (null != listener){
				listener.received(channel, msg);
			}
		} finally{
			NettyChannel.removeChannelIfDisconnected(ctx.channel());
		}
		
		// BINARY方式并且Oneway=false才会将request放入REQ_TOKEN_MAP中，RPC和acall不会
		if (response.isBinary()){
			
			RequestToken token = NettyClient.REQ_TOKEN_MAP.get(response.getTokenKey()+"");
			if (null != token){
				
				Logger.debug("Response for token key {} matched.", response.getTokenKey());
				token.handle(response);
			} else {
				
				Logger.warn("Corresponding RequestToken NOT FOUND! response is dropped:{}", response);
			}
		}
		
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		
		// 捕获异常后，应立即invalidate这条链路，让其他人不要再网这条链路发了
		InetSocketAddress remoteAddress = (InetSocketAddress) this.url.getSocketAddress();
		
		// 发现Channel异常，且是本地配置引用远端的Channel（自己是客户端），则放入重连列表中，
		// 供ChannelFixer来重连
		if (CrowClientContext.isRemoteEnd(ipAndPort)){
			ChannelRegistry.invalidateChannel(remoteAddress);
		}
		
		io.netty.channel.Channel netty_channel = ctx.channel();
		if (null != netty_channel){
			
			netty_channel.close();
			NettyChannel channel = NettyChannel.getOrAddChannel(netty_channel, url, listener);
			Logger.debug("exceptionCaught: {}", cause);
			try {
				if (null != listener){
					listener.caught(channel, cause);
				}
			} finally {
				NettyChannel.removeChannelIfDisconnected(ctx.channel());
				NettyClient.handleErrorChannelReqToken(channel);
			}
		}
	}
	
	public static String toAddressString(InetSocketAddress address){
		
		return address.getAddress().getHostAddress() + ":" + address.getPort();
	}
}
