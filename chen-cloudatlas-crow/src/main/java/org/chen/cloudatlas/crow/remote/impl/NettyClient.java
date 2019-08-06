package org.chen.cloudatlas.crow.remote.impl;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.chen.cloudatlas.crow.common.Constants;
import org.chen.cloudatlas.crow.common.URL;
import org.chen.cloudatlas.crow.common.thread.SingletonTimer;
import org.chen.cloudatlas.crow.common.utils.UrlUtil;
import org.chen.cloudatlas.crow.config.CrowClientContext;
import org.chen.cloudatlas.crow.remote.ChannelListener;
import org.chen.cloudatlas.crow.remote.ChannelRegistry;
import org.chen.cloudatlas.crow.remote.MessageWrapper;
import org.chen.cloudatlas.crow.remote.RemoteException;
import org.chen.cloudatlas.crow.remote.Request;
import org.chen.cloudatlas.crow.remote.Response;
import org.tinylog.Logger;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.Timer;
import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * 
 * @author chenn
 *
 */
public class NettyClient extends AbstractClient{

	private Bootstrap nettyClientBootstrap;
	
	private final Lock lock = new ReentrantLock();
	
	private final Timer timer;
	
	//netty3
	/*
	private static Executor bossExecutor = Executors.newCachedThreadPool(new NamedThreadFactory("crow-client-boss"));
	private static Executor workerExecutor = Executors.newCachedThreadPool(new NamedThreadFactory("crow-client-worker"));
	private static ChannelFactory channelFactory;
	*/
	
	private static EventLoopGroup worker;
	
	private int timeout = UrlUtil.getParameter(this.getUrl(), Constants.TIMEOUT, Constants.DEFAULT_NO_RESPONSE_TIMEOUT);
	private String codec = UrlUtil.getParameter(this.getUrl(), Constants.PROTOCOL, Constants.DEFAULT_PROTOCOL);
	
	//netty3
	/*
	static {
		if (CrowClientContext.getConfig() != null){
			int bossCount = CrowClientContext.getConfig().getApplicationConfig().getNettyBossCount();
			int workerCount = CrowClientContext.getConfig().getApplicationConfig().getNettyWorkerCount();			
			workerCount = (workerCount == 0 ? Constants.DEFAULT_IO_THREADS : workerCount);
			channelFactory = new NioClientSocketChannelFactory(bossExecutor,workerExecutor,bossCount,workCount);
		} else {
			channelFactory = new NioClientSocketChannelFactory(bossExecutor,workerExecutor);
		}
	}
	*/
	// 用 DefaultThreadFactory 取代 NamedThreadFactory
	static {
		if (CrowClientContext.getConfig() != null){
			int workerCount = CrowClientContext.getConfig().getApplicationConfig().getNettyWorkerCount();
			workerCount = (workerCount == 0 ? Constants.DEFAULT_IO_THREADS : workerCount);
			worker = new NioEventLoopGroup(workerCount, new DefaultThreadFactory("crow-client-worker"));
		} else {
			worker = new NioEventLoopGroup(0, new DefaultThreadFactory("crow-client-worker"));		
		}
	}
	
	/**
	 * key: host:port, value: NettyClient
	 */
	private static final ConcurrentMap<String, NettyClient> clientMap = new ConcurrentHashMap<>();
	
	/**
	 * key: requestId, value: RequestToken
	 */
	public static final ConcurrentMap<String, RequestToken> REQ_TOKEN_MAP = new ConcurrentHashMap<>();
	
	public NettyClient(URL url, ChannelListener listener) {
		super(url, listener);
		this.timer = SingletonTimer.getTimer();
	}

	public static NettyClient getOrAddClient(URL url, ChannelListener listener){
		
		if (null == url){
			return null;
		}
		
		InetSocketAddress address = (InetSocketAddress) url.getSocketAddress();
		
		NettyClient nc = null;
		
		try {
			nc = getClient(address);
		} catch (RemoteException e){
			Logger.trace("NettyClient for " + address + " dose not exist, will create one");
		}
		
		if (null == nc){
			
			NettyClient client = new NettyClient(url, listener);
			nc = clientMap.putIfAbsent(UrlUtil.getAddressKey(address), client);
			
			if (null == nc){
				nc = client;
			}
		} else {
			nc.setUrl(url);
		}
		
		return nc;
	}
	
	public static NettyClient getClient(URL url) throws RemoteException{
		return getClient((InetSocketAddress) url.getSocketAddress());
	}
	
	public static NettyClient getClient(String addressKey) throws RemoteException{
		
		NettyClient client = clientMap.get(addressKey);
		
		if (null == client){
			throw new RemoteException("client connect to " + addressKey + " has not been established");
		}
		
		return client;
	}
	
	public static NettyClient getClient(InetSocketAddress address) throws RemoteException{
		
		if (null == address){
			throw new IllegalArgumentException("url address is null");
		}
		
		return getClient(UrlUtil.getAddressKey(address));
	}
	
	public static void removeClient(URL url){
		
		if (null != url){
			
			InetSocketAddress address = (InetSocketAddress) url.getSocketAddress();
			if (null == address){
				throw new IllegalArgumentException("url address is null");
			}
			
			clientMap.remove(UrlUtil.getAddressKey(address));
		}
	}
	
	@Override
	public void connect() throws RemoteException {
		
		if (this.isShutDown()){
			throw new RemoteException(this, "client has been shutdown. url:" + getUrl());
		}
		
		// add connection await timeout
		int timeout = Constants.DEFAULT_CONNECTION_AWAIT_TIMEOUT;
		ChannelFuture future;
		
		lock.lock();
		// netty3
		/*
		try {
			if (!this.isConnected()){
				nettyClientBootstrap = new ClientBootstrap(channelFactory);
				
				nettyClientBootstrap.setOption("keepAlive", true);
				nettyClientBootstrap.setOption("tcpNoDelay", true);
				nettyClientBootstrap.setOption("reuseAddress", true);
				nettyClientBootstrap.setOption("connectTimeoutMillis", true);
				
				// NettyClientPiplineFactory implements ChannelPipelineFactory
				nettyClientBootstrap.setPipelineFactory(new NettyClientPiplineFactory(this.getUrl(), listener, timer));
			}
		} catch(Exception e){}
		*/
		
		try {
			// check connection to avoid duplicated & concurrent connnect() calls in HeaderExchangeClient
			if (!this.isConnected()){
				
				nettyClientBootstrap = new Bootstrap();
				
				nettyClientBootstrap.group(worker)
				.channel(NioSocketChannel.class)
				.option(ChannelOption.SO_KEEPALIVE, true)
				.option(ChannelOption.TCP_NODELAY, true)
				.option(ChannelOption.SO_REUSEADDR, true)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Constants.DEFAULT_SOCKET_TIMEOUT);	
				
				nettyClientBootstrap.handler(new ClientChannelInitializer(this.getUrl(), listener, timer));
				
				final CountDownLatch channelLatch = new CountDownLatch(1);
				
				String dest = this.getUrl().getSocketAddress().getAddress().getHostName();
				int destPort = this.getUrl().getPort();
				Logger.info("connecting to " + dest);
				future = nettyClientBootstrap.connect(dest, destPort);
				future.addListener(new ChannelFutureListener(){

					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						
						channelLatch.countDown();
					}
					
				});
				
				boolean finished;
				
				try {
					finished = channelLatch.await(timeout, TimeUnit.MILLISECONDS);
				} catch (InterruptedException ep){
					throw new RemoteException(this, "interrupted while waiting for connection response", ep);
				}
				
				Throwable cause = future.cause();
				if (null != cause){
					throw new RemoteException(cause);
				}
				
				if (!finished){
					throw new RemoteException(this, "Connection await timeout, failed to connect to" + 
							this.getUrl().getSocketAddress() + 
							" in " + timeout + "(ms)");
				}
				
				if (future.isSuccess()){
					ChannelRegistry.registerChannel(future.channel());
				} else {
					this.shutDown();
					throw new RemoteException(this, "failed to connect to " + 
							this.getUrl().getSocketAddress() +
							" in timeout(" + timeout + "ms) limit");
				}
			}
		} catch (Exception e){
			throw new RemoteException(this, "failed to connect to " + this.getUrl().getSocketAddress(), e);
		} finally{
			lock.unlock();
		}
	}

	@Override
	public void reconnect() throws RemoteException {
		
		if (this.isShutDown()){
			throw new RemoteException(this, "client has been shutdown, url:" + getUrl());
		}
		
		// add connection await timeout
		int timeout = Constants.DEFAULT_CONNECTION_AWAIT_TIMEOUT;
		
		Channel oldChannel = ChannelRegistry.getChannel(this.getUrl().getSocketAddress());
		ChannelFuture future;
		
		lock.lock();
		try {
			final CountDownLatch channelLatch = new CountDownLatch(1);
			future = nettyClientBootstrap.connect(
					this.getUrl().getSocketAddress().getAddress(), 
					this.getUrl().getPort());
			future.addListener(new ChannelFutureListener(){

				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					
					channelLatch.countDown();
				}
				
			});
			
			boolean finished;
			
			try {
				finished = channelLatch.await(timeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException ep){
				throw new RemoteException(this, "interrupted while waiting for connection response", ep);
			}
			
			Throwable cause = future.cause();
			if (null != cause){
				throw new RemoteException(cause);
			}
			
			if (!finished){
				throw new RemoteException(this, "Connection await timeout, failed to connect to" + 
						this.getUrl().getSocketAddress() + 
						" in " + timeout + "(ms)");
			}
			
			if (future.isSuccess()){
				ChannelRegistry.registerChannel(future.channel(), true);
				// close old connection asynchronously
				if (null != oldChannel && oldChannel.isActive()){
					oldChannel.close();
				}
			} else {
				throw new RemoteException(this, "failed to connect to " + 
						this.getUrl().getSocketAddress() +
						" in timeout(" + timeout + "ms) limit");
			}
		} catch (Exception e){
			throw new RemoteException(this, "failed to connect to " + this.getUrl().getSocketAddress(), e);
		} finally{
			lock.unlock();
		}
	}

	@Override
	public void send(Request request) throws RemoteException {		
		// 单项发送，无需反馈
		sendMsg(request, true);
	}

	@Override
	public Response sendWithResult(Request request) throws RemoteException {
		// 发送，需反馈
		return sendMsg(request, false);
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return getChannel() == null ? null : (InetSocketAddress)getChannel().remoteAddress();
	}

	@Override
	public boolean isConnected() {
		
		Channel channel = getChannel();
		if (null != channel){
			return channel.isActive();
		} else {
			return false;
		}
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		return getChannel() == null ? null : (InetSocketAddress)getChannel().localAddress();
	}

	@Override
	public void send(Object message) throws RemoteException {
		
		send(message, false);
	}

	@Override
	public void send(Object message, boolean sent) throws RemoteException {
		
		if (!isConnected()){
			connect();
		}
		
		Channel channel = getChannel();
		if (null == channel || !channel.isActive()){
			throw new RemoteException(this, "message can not send, because channel is close. url:"+getUrl());
		}
		
		boolean success = true;
		
		int timeout = 0;
		
		try {
			ChannelFuture future = channel.write(message);
			if (sent){
				// 不等待，立即返回
				timeout = 0;
				success = future.await(timeout);
			}
			Throwable cause = future.cause();
			if (null != cause){
				throw new RemoteException(cause);
			}
		} catch (Exception e){
			Logger.error(e);
			throw new RemoteException(this, "failed to send message " + message +
					" to " + this.getRemoteAddress() + 
					", cause:" + e.getMessage(), e);
		}
		
		if (!success){
			throw new RemoteException(this, "failed to send message " + message +
					" to " + this.getRemoteAddress() + 
					" in timeout(" + timeout + "ms) limit");
		}
	}

	@Override
	public void shutDown() {
		
		lock.lock();
		try {
			
			this.shutdown = true;
			
			removeClient(this.getUrl());
			
			Channel channel = this.getChannel();
			if (null != channel){
				channel.close();
			}
			
			ChannelRegistry.unregisterChannel(this.getUrl());
		} finally{
			lock.unlock();
		}
	}

	private Channel getChannel() {
		
		return ChannelRegistry.getChannel(getUrl().getSocketAddress());
	}

	/**
	 * 只有在 binary 的情况下存在REQ_TOKEN_MAP
	 * @param channel
	 */
	public static void handleErrorChannelReqToken(NettyChannel channel) {
		
		for (Map.Entry<String, RequestToken> entry : REQ_TOKEN_MAP.entrySet()){
			
			if (((RequestToken)entry.getValue()).getChannel() == channel){
				entry.getValue().handleError();
			}
		}
	}
	
	/**
	 * 关闭所有客户端
	 */
	public static void shutDownAll(){
		for (NettyClient client : clientMap.values()){
			client.shutDown();
		}
		
		worker.shutdownGracefully();
		
		clientMap.clear();
		REQ_TOKEN_MAP.clear();
	}

	private Response sendMsg(Request request, boolean oneWay) throws RemoteException{
		
		if (this.isShutDown()){
			throw new RemoteException(this, "client has been shudown. url:"+getUrl());
		}
		
		Channel channel = getChannel();
		if (null == channel || !channel.isActive()){
			ChannelRegistry.invalidateChannel(channel);
			throw new RemoteException(this, "message can not send, because channel is closed. url:"+getUrl());
		}
		
		// 准备附加到request的附件
		Map<String, Object> attachments = new HashMap<>();
		attachments.put(Constants.IP_AND_PORT, UrlUtil.getAddressKey((InetSocketAddress)this.getUrl().getSocketAddress()));
		attachments.put(Constants.ONE_WAY, oneWay);
		
		RequestToken token = null;
		MessageWrapper wrapper = MessageWrapper.get(codec);
		// 不同的协议对应不同的wrap需求
		request = wrapper.wrapRequest(request, attachments);
		
		try {
			if (oneWay){
				
				channel.write(request);
			} else {
				
				try {
					lock.lock();
					token = new RequestToken(request.getTokenKey(), timeout, NettyChannel.get(channel), request);
					REQ_TOKEN_MAP.put(request.getTokenKey(), token);
					channel.write(request);
				} finally{
					lock.unlock();
				}
			}
		} catch (Exception e){
			if (REQ_TOKEN_MAP.containsKey(request.getTokenKey())){
				REQ_TOKEN_MAP.remove(request.getTokenKey(),token);
			}
			throw new RemoteException(this, "failed to send message " + request + " to " + getUrl().getSocketAddress());
		}
		
		Response response;
		if (oneWay){
			
			response = wrapper.wrapResponse(request, attachments);
		} else {
			
			try {
				response = token.get();
			} catch (RemoteException e){
				// 可能是timeout,也可能是链路异常，直接报错
				throw e;
			}
			if (null != response){
				wrapper.wrapResponse(request, attachments);
			}
		}
		
		if (null == response){
			throw new RemoteException(this, "failed get response for " + request + " from" + getUrl().getSocketAddress() + 
					" in timeout(" + timeout + "ms) limit");
		}
		
		return response;
		
	}
}
