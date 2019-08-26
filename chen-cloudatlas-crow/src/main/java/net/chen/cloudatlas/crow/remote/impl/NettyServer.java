package net.chen.cloudatlas.crow.remote.impl;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.tinylog.Logger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.Timer;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.common.thread.SingletonTimer;
import net.chen.cloudatlas.crow.config.CrowServerContext;
import net.chen.cloudatlas.crow.remote.ChannelListener;

public class NettyServer extends AbstractServer{

	private final Timer timer;
	
	private final Lock lock = new ReentrantLock();
	
	private static Map<String, NettyServer> serverMap = new ConcurrentHashMap<>();
	
	private ServerBootstrap bootstrap;
	
	private EventExecutorGroup eventExecutorGroup;
	private ChannelGroup channelGroup;
	
	private static EventLoopGroup bossGroup;
	
	private static EventLoopGroup workerGroup;
	
	private boolean isStarted;
	
	private boolean isBound;
	
	static{
		if (CrowServerContext.getConfig() != null){
			int bossCount = CrowServerContext.getConfig().getApplicationConfig().getNettyBossCount();
			int workerCount = CrowServerContext.getConfig().getApplicationConfig().getNettyWorkerCount();
			bossGroup = new NioEventLoopGroup(bossCount, new DefaultThreadFactory("crow-client-boss", true));
			workerGroup = new NioEventLoopGroup(workerCount, new DefaultThreadFactory("crow-client-worker"));
		} else {
			bossGroup = new NioEventLoopGroup(1,new DefaultThreadFactory("crow-client-boss",true));
			workerGroup = new NioEventLoopGroup(0,new DefaultThreadFactory("crow-client-worker"));
		}
	}
	
	public NettyServer(URL url, ChannelListener listener){
		super(url, listener);
		this.timer = SingletonTimer.getTimer();
		serverMap.put(url.getHostAndPort(), this);
		
		// get maxThreads from url
		int maxThreads;
		String maxThreadsStr = getUrl().getParameter(Constants.CROW_NETTY_EXECUTOR_SIZE_KEY);
		if (null == maxThreadsStr){
			// 有些testcase直接构建了url
			maxThreads = Constants.DEFAULT_CROW_NETTY_EXECUTOR_SIZE;
		} else {
			maxThreads = Integer.parseInt(maxThreadsStr);
		}
		
		Logger.info("netty server event executor max size:" + maxThreads + " for " + getUrl());
		
		String protocolId = getUrl().getParameter(Constants.PROTOCOL_ID);
		String executorThreadName = "crow-server-execurot-" + protocolId;
		
		//MemoryAwareThreadPoolExecutor netty 4 已经抛弃
		//eventExecutorGroup = new DefaultEventExecutor(
		//		new MemoryAwareThreadPoolExecutor(maxThreads, 1048567, 1048567, 30, TimeUnit.SECONDS, new DefaultThreadFactory(executorThreadName)));
		eventExecutorGroup = new DefaultEventExecutorGroup(maxThreads,new DefaultThreadFactory(executorThreadName));
	}

	@Override
	public void bind() {
		
		if (!isStarted){
			Logger.warn("NettyServer is already started, 'bind' method should not be called twice");
			return ;
		}
		
		if (null == listener){
			throw new IllegalArgumentException("ChannelListener must not be null!");
		}
		
		SocketAddress localAddress = getUrl().getSocketAddress();

		try {
			bootstrap = new ServerBootstrap();
					
			bootstrap
			.group(bossGroup,workerGroup)
			.channel(NioServerSocketChannel.class)
			.option(ChannelOption.SO_REUSEADDR, true)
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Constants.DEFAULT_SOCKET_TIMEOUT)
			.childOption(ChannelOption.TCP_NODELAY, true)
			.childOption(ChannelOption.SO_REUSEADDR, true)
			.childOption(ChannelOption.SO_KEEPALIVE, true)
			.childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, Constants.DEFAULT_SOCKET_TIMEOUT);
			bootstrap.childHandler(new ServerChannelInitializer(getUrl(), listener, channelGroup, timer, eventExecutorGroup));
			
			// Start the server.
			ChannelFuture f = bootstrap.bind(localAddress).sync();
			
			// add serverChannel into channelGroup
			channelGroup.add(f.channel());
			
			// flag server status
			isStarted = true;
			isBound = true;
			
			// log
			Logger.info("Bind at " + localAddress);
			
			// Wait until the server socket is closed.
			f.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
			// Shut down all event loops to terminate all threads.
	        bossGroup.shutdownGracefully();
	        workerGroup.shutdownGracefully();
	        
	        // Wait until all threads are terminated.
	        try {
				bossGroup.terminationFuture().sync();
		        workerGroup.terminationFuture().sync();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public void shutDown() {
		
		lock.lock();
		try {
			if (isStarted){
				
				channelGroup.close().awaitUninterruptibly();
				serverMap.remove(getUrl().getHostAndPort());
				
				isStarted = false;
				isBound = false;
			} else {
				Logger.warn("NettyServer is already shutdown! Don't call shutDown twice!");
			}
		} finally {
			lock.lock();
		}
	}

	@Override
	public boolean isBound() {
		
		return isBound;
	}

	@Override
	public void setChannelListener(ChannelListener listener) {
		
		this.listener = listener;
	}
	
	public static void  shutDownAll(){
		
		for (String key : serverMap.keySet()){
			
			if (null != serverMap.get(key)){
				serverMap.get(key).shutDown();
			}
		}
		
		serverMap.clear();
	}
}
