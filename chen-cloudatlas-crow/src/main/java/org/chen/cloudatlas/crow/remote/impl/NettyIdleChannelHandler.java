package org.chen.cloudatlas.crow.remote.impl;

import java.net.InetSocketAddress;

import org.chen.cloudatlas.crow.common.utils.UrlUtil;
import org.chen.cloudatlas.crow.config.CrowClientContext;
import org.chen.cloudatlas.crow.remote.ChannelRegistry;
import org.chen.cloudatlas.crow.remote.Request;
import org.tinylog.Logger;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 处理Channel空闲<br>
 * 1.READ空闲：关闭Channel<br>
 * 2.WRITE空闲：发送心跳包<br>
 * @author chenn
 *
 */
public class NettyIdleChannelHandler extends IdleStateHandler{

	private long readerIdleTime;
	
	private Request heartbeatMessage;
	
	public NettyIdleChannelHandler(long readerIdleTime, Request heartbeatMessage){
		super((int)readerIdleTime, 0, 0);
		this.readerIdleTime = readerIdleTime;
		this.heartbeatMessage = heartbeatMessage;
	}
	
	public NettyIdleChannelHandler(long readerIdleTime, long writeIdleTime, Request heartbeatMessage){
		super((int)readerIdleTime, (int)writeIdleTime, 0);
		this.readerIdleTime = readerIdleTime;
		this.heartbeatMessage = heartbeatMessage;
	}
	
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		
		if (evt instanceof IdleStateEvent){
			
			IdleStateEvent idleevt = (IdleStateEvent) evt;
			Logger.trace("channelIdle evt.state()=" + idleevt.state());
			
			Channel nettyChannel = ctx.channel();
			String ipAndPort = UrlUtil.getAddressKey((InetSocketAddress)nettyChannel.remoteAddress());
			
			if (idleevt.state() == IdleState.READER_IDLE){
				
				if (CrowClientContext.isRemoteEnd(ipAndPort)){
					ChannelRegistry.invalidateChannel(nettyChannel);
				}
				
				Logger.warn("CAN NOT receive heartbeat from {} after {} ms timeout, close this channel now.", ipAndPort,readerIdleTime);
				
				nettyChannel.close();
			} else if (idleevt.state() == IdleState.WRITER_IDLE){
				
				nettyChannel.write(heartbeatMessage);
				Logger.debug("Send heartbeat to " + nettyChannel);
			}
		}
	}
}
