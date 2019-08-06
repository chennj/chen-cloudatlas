package org.chen.cloudatlas.crow.remote.impl;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.chen.cloudatlas.crow.common.Constants;
import org.chen.cloudatlas.crow.common.URL;
import org.chen.cloudatlas.crow.common.utils.NameableServiceLoader;
import org.chen.cloudatlas.crow.remote.ChannelListener;
import org.chen.cloudatlas.crow.remote.MessageWrapper;
import org.chen.cloudatlas.crow.remote.Request;
import org.chen.cloudatlas.crow.remote.codec.AbstractEncoder;
import org.chen.cloudatlas.crow.remote.codec.CodecFactory;

import io.netty.channel.ChannelHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.Timer;

@ChannelHandler.Sharable
public class ClientChannelInitializer extends ChannelInitializer<SocketChannel>{

	private static final LoggingHandler LOGGING_HANDLER = new LoggingHandler();
	
	private static final boolean hexDump = Boolean.parseBoolean(System.getProperty("crow.hexDump", "true"));
	
	private static final Map<String, MessageWrapper> heartbeatMap = 
			NameableServiceLoader.getLoader(MessageWrapper.class).getServices();
	
	private URL url;
	
	private ChannelListener listener;
	
	private Timer timer;
	
	private final long heartbeatInterval;
	
	private final long readerIdleTime;
	
	/**
	 * netty 管道初始化器
	 * @param url
	 * @param listener
	 * @param timer
	 */
	public ClientChannelInitializer(URL url, ChannelListener listener, final Timer timer){
		
		this.url = url;
		this.listener = listener;
		this.heartbeatInterval = Long.parseLong(url.getParameter(Constants.HEARTBEAT_INTERVAL));
		this.readerIdleTime = heartbeatInterval * 3;
		this.timer = timer;
	}
	
	public ClientChannelInitializer(URL url, ChannelListener listener){
		this(url,listener,null);
	}
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		
		ChannelPipeline pipeline = ch.pipeline();
		
		pipeline.addLast("LOGGING_HANDLER",LOGGING_HANDLER);
		
		String maxMsgSize = url.getParameter(Constants.MAX_MSG_SIZE);
		
		int maxFrameLength;
		
		if (
				null == maxMsgSize ||
				"".equals(maxMsgSize.trim()) ||
				(Integer.parseInt(maxMsgSize) <= 0)){
			maxFrameLength = Constants.DEFAULT_MAX_MSG_SIZE;
		} else {
			maxFrameLength = Integer.parseInt(maxMsgSize);
		}
		
		LengthFieldBasedFrameDecoder lenDecoder = 
				((AbstractEncoder)CodecFactory.getEncoder(url)).getLengthFieldBasedFrameDecoder(maxFrameLength);
		
		if (null != lenDecoder){
			pipeline.addLast("LengthFieldBasedFrameDecoder", lenDecoder);
		}
		
		pipeline.addLast("CrowEncoder", CodecFactory.getEncoder(url));
		pipeline.addLast("CrowDecoder", CodecFactory.getDecoder(url));
		
		MessageWrapper heartbeat = heartbeatMap.get(url.getProtocol());
		if (null != heartbeat && heartbeatInterval > 0){
			Request heartbeatMessage = heartbeat.wrapHearbeat(Constants.DEFAULT_PROTOCOL_VERSION);
			if (null != heartbeatMessage){
				// netty3
				// idle handle 会发送心跳包（heartbeatMessage），需要经过codec
				/*
				pipeline.addLast(
						"idleStateHandler", 
						new IdleStateHandler(
								timer, 
								readerIdleTime, 
								heartbeatInterval, 
								0, 
								TimeUnit.MILLISECONDS));
				*/
				pipeline.addLast(
						"idleStateHandler", 
						new IdleStateHandler(
								readerIdleTime, 
								heartbeatInterval, 
								0, 
								TimeUnit.MILLISECONDS));
				pipeline.addLast("idleHandler",new NettyIdleChannelHandler(readerIdleTime,heartbeatInterval,heartbeatMessage));
			}
		}
		
		// business logic
		pipeline.addLast("NettyIdleChannelHandler", new NettyClientChannelHandler(url,listener));
	}

}
