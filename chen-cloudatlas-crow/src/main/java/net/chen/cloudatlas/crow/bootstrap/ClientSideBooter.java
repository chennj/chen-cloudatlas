package net.chen.cloudatlas.crow.bootstrap;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.config.CrowConfig;
import net.chen.cloudatlas.crow.remote.ChannelListener;
import net.chen.cloudatlas.crow.remote.RemoteException;
import net.chen.cloudatlas.crow.remote.impl.NettyClient;
import net.chen.cloudatlas.crow.remote.thread.ChannelFixedDaemon;
import net.chen.cloudatlas.crow.remote.thread.ScheduleService;
import net.chen.cloudatlas.crow.rpc.utils.ProtocolUtil;

/**
 * 
 * @author chenn
 *
 */
public class ClientSideBooter implements Bootable{

	private ChannelListener clientListener;
	
	private CrowConfig config;
	
	private static ConcurrentMap<String, AtomicBoolean> initializedMap = 
			new ConcurrentHashMap<String, AtomicBoolean>();
	
	private ScheduleService<ChannelFixedDaemon> scheduleFixedService;
	
	public ClientSideBooter(CrowConfig config, ChannelListener clientListener){
		
		this.config = config;
		this.clientListener = clientListener;
	}
	
	public void init(){
		
		if (
				config.getReferenceConfigList().size() > 0 ||
				config.getMonitorConfig() != null){
			
			scheduleFixedService = new ScheduleService<ChannelFixedDaemon>();
			scheduleFixedService.schedule(new ChannelFixedDaemon(Constants.DEFAULT_FIX_INTERVAL));
		}
	}
	
	public void start() {
		
		init();
		//为每个reference中的所有url建立连接
		connect(config.getBinaryReferenceUrls());
	}
	
	public void connect(List<URL> urls){
		
		//线程完成计数
		CountDownLatch latch = new CountDownLatch(urls.size());
		
		for (URL u : urls){
			connect(u, latch);
		}
		
		if (Boolean.valueOf(
				System.getProperty(
						Constants.WAIT_ALL_URLS_CONNECTED,
						Constants.DEFAULT_WAIT_ALL_URLS_CONNECTED))){
			
			try {
				Logger.debug("最大重试连接等待时间是：{}ms",Constants.DEFAULT_MAX_WAIT_RETRY_CONNECT_INTERVAL);
				boolean waitForLatch = latch.await(Constants.DEFAULT_MAX_WAIT_RETRY_CONNECT_INTERVAL, TimeUnit.MILLISECONDS);
				if (!waitForLatch){
					Logger.warn("Time on connection to {} hash elapsed, {}ms, current thread will no longer wait",
							urls,Constants.DEFAULT_MAX_WAIT_RETRY_CONNECT_INTERVAL);
				}
			} catch (InterruptedException e){
				
				throw new RuntimeException("当前等待连接的线程被中断："+urls);
			}
		}
	}

	private boolean isTryConnected(URL url){
		
		String invokeKey = ProtocolUtil.invokeKey(url);
		initializedMap.putIfAbsent(invokeKey, new AtomicBoolean(false));
		AtomicBoolean hasTryConnected = initializedMap.get(invokeKey);
		
		if (hasTryConnected.get()){
			return true;
		} else {
			return !hasTryConnected.compareAndSet(false, true);
		}
	}
	
	private void removeFromInitializedMap(URL url){
		
		String invokeKey = ProtocolUtil.invokeKey(url);
		initializedMap.remove(invokeKey);
	}
	
	public void connect(final URL u, final CountDownLatch latch) {
		
		Logger.debug("connect to " + u);
		
		String threadName = "crow-retry-connect-" + u.getHostAndPort();
		
		/*
		 * 已经开始尝试连接的计数减一，并返回
		 * 还没开始连接的，标记为一开始尝试连接，然后用独立线程去做
		 */
		if (isTryConnected(u)){
			
			Logger.debug("another thread is trying to connect to " + u.getHostAndPort() + " current thread will return");
			latch.countDown();
			return;
		}
		
		//用独立线程去做connect的工作
		Thread t = new Thread(new Runnable(){
			@Override
			public void run(){
				
				NettyClient client = NettyClient.getOrAddClient(u, clientListener);
				
				for(;;){
					
					try {
						if (client != null && !client.isConnected()){
							client.connect();
							Logger.info("Successfully connected to " + u.getHostAndPort());
						}
					} catch (RemoteException e){
						//连接失败，睡会儿再试
						Logger.error("failed connect to " + u.getHostAndPort());
						Logger.warn("will retry to connect to " + u.getHostAndPort() + " after " + Constants.DEFAULT_RETRY_CONNECT_INTERVAL / 1000 + " seconds", e);
						
						try {
							Thread.sleep(Constants.DEFAULT_RETRY_CONNECT_INTERVAL);
						} catch (InterruptedException ie){
							Logger.warn("sleep is interrupted.",ie);
						}
						
						continue;
					}
					
					removeFromInitializedMap(u);
					latch.countDown();
					break;
				}
			}
		}, threadName);
		
		t.setDaemon(true);
		t.start();
	}

	public void shutDown() {
		
		if (null != scheduleFixedService){
			scheduleFixedService.stop();
		}
		NettyClient.shutDownAll();
	}

}
