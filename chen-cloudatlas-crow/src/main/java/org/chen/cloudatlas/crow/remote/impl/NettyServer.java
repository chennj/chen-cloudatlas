package org.chen.cloudatlas.crow.remote.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.chen.cloudatlas.crow.common.URL;
import org.chen.cloudatlas.crow.common.thread.SingletonTimer;
import org.chen.cloudatlas.crow.remote.ChannelListener;

import io.netty.util.Timer;

public class NettyServer extends AbstractServer{

	private final Timer timer;
	
	private final Lock lock = new ReentrantLock();
	
	private static Map<String, NettyServer> serverMap = new ConcurrentHashMap<>();
	
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
