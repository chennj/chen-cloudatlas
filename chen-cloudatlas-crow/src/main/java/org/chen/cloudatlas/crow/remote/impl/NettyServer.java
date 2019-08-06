package org.chen.cloudatlas.crow.remote.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.chen.cloudatlas.crow.common.URL;
import org.chen.cloudatlas.crow.common.thread.SingletonTimer;
import org.chen.cloudatlas.crow.config.CrowServerContext;
import org.chen.cloudatlas.crow.remote.ChannelListener;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.Timer;
import io.netty.util.concurrent.DefaultThreadFactory;

public class NettyServer extends AbstractServer{

	private final Timer timer;
	
	private final Lock lock = new ReentrantLock();
	
	private static Map<String, NettyServer> serverMap = new ConcurrentHashMap<>();
	
	private ServerBootstrap bootstrap;
	
	private static EventLoopGroup parentGroup;
	
	private static EventLoopGroup childGroup;
	
	private boolean isStarted;
	
	private boolean isBound;
	
	static{
		if (CrowServerContext.getConfig() != null){
			int bossCount = CrowServerContext.getConfig().getApplicationConfig().getNettyBossCount();
			int workerCount = CrowServerContext.getConfig().getApplicationConfig().getNettyWorkerCount();
			parentGroup = new NioEventLoopGroup(bossCount, new DefaultThreadFactory("crow-client-boss"));
			childGroup = new NioEventLoopGroup(workerCount, new DefaultThreadFactory("crow-client-worker"));
		} else {
			parentGroup = new NioEventLoopGroup(0,new DefaultThreadFactory("crow-client-boss"));
			childGroup = new NioEventLoopGroup(0,new DefaultThreadFactory("crow-client-worker"));
		}
	}
	public NettyServer(URL url, ChannelListener listener){
		super(url, listener);
		this.timer = SingletonTimer.getTimer();
		serverMap.put(url.getHostAndPort(), this);
	}

	@Override
	public void bind() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isBound() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setChannelListener(ChannelListener listener) {
		// TODO Auto-generated method stub
		
	}
}
