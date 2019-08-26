package net.chen.cloudatlas.crow.remote.impl;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.Timer;
import io.netty.util.concurrent.EventExecutorGroup;
import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.common.utils.NameableServiceLoader;
import net.chen.cloudatlas.crow.remote.ChannelListener;
import net.chen.cloudatlas.crow.remote.MessageWrapper;
import net.chen.cloudatlas.crow.remote.Request;
import net.chen.cloudatlas.crow.remote.codec.AbstractDecoder;
import net.chen.cloudatlas.crow.remote.codec.CodecFactory;

public class ServerChannelInitializer extends ChannelInitializer<SocketChannel>{

	private static final LoggingHandler LOGGING_HANDLER = new LoggingHandler();
	
	private static final boolean hexDump = Boolean.parseBoolean(System.getProperty("crow.hexDump", "true"));

	private static final Map<String, MessageWrapper> heartbeatMap = 
			NameableServiceLoader.getLoader(MessageWrapper.class).getServices();
	
	private URL url;
	
	private ChannelListener listener;
	
	private ChannelGroup channelGroup;
	
	private Timer timer;
	
	private final long heartbeatInterval;
	
	private final long readerIdleTime;
	
	private EventExecutorGroup  executionHandler;
	
	public ServerChannelInitializer(
			URL url, ChannelListener listener, 
			ChannelGroup channelGroup, 
			final Timer timer,
			final EventExecutorGroup executionHandler){
		
		this.url = url;
		this.listener = listener;
		this.channelGroup = channelGroup;
		this.heartbeatInterval = Long.parseLong(url.getParameter(Constants.HEARTBEAT_INTERVAL));
		this.readerIdleTime = heartbeatInterval * 3;
		this.timer = timer;
		this.executionHandler = executionHandler;
	}
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		
		ChannelPipeline pipeline = ch.pipeline();
		
		pipeline.addLast("LOGGING_HANDLER",LOGGING_HANDLER);
		
		String maxMsgSize = url.getParameter(Constants.MAX_MSG_SIZE);
		int maxFrameLength = maxMsgSize == null ? Constants.DEFAULT_MAX_MSG_SIZE : Integer.parseInt(maxMsgSize);
		LengthFieldBasedFrameDecoder lenDecoder = 
				((AbstractDecoder)CodecFactory.getDecoder(url)).getLengthFieldBasedFrameDecoder(maxFrameLength);
		if (null != lenDecoder){
			pipeline.addLast("LengthFieldBasedFrameDecoder", lenDecoder);
		}
		
		pipeline.addLast("CrowEncoder", CodecFactory.getEncoder(url));
		pipeline.addLast("CrowDecoder", CodecFactory.getDecoder(url));
		
		MessageWrapper heartbeat = heartbeatMap.get(url.getProtocol());
		if (null != heartbeat && heartbeatInterval > 0){
			Request heartbeatMessage = heartbeat.wrapHearbeat(Constants.DEFAULT_PROTOCOL_VERSION);
			if (null != heartbeatMessage){
				// idle handler 会发送heartbeatMessage,需要经过codec
				pipeline.addLast(
						"idleStateHandler", 
						new IdleStateHandler(
								readerIdleTime,
								heartbeatInterval,
								0,
								TimeUnit.MILLISECONDS));
				pipeline.addLast("idleHandler", new NettyIdleChannelHandler(readerIdleTime, heartbeatMessage));
			}
		}
		
		// bussiness logic
		pipeline.addLast(executionHandler, "nettyServerChannelHandler", new NettyServerChannelHandler(url, listener, channelGroup));
	}

}
