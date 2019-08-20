package org.chen.cloudatlas.crow.remote.exchange;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.chen.cloudatlas.crow.common.Constants;
import org.chen.cloudatlas.crow.remote.Channel;
import org.chen.cloudatlas.crow.remote.RemoteException;
import org.chen.cloudatlas.crow.remote.TimeoutException;
import org.chen.cloudatlas.crow.remote.support.crow.CrowStatus;
import org.springframework.util.StringUtils;
import org.tinylog.Logger;

public class DefaultFuture implements ResponseFuture{

	private static final Map<Long, Channel> CHANNELS = new ConcurrentHashMap<>();
	
	private static final Map<Long, DefaultFuture> FUTURES = new ConcurrentHashMap<>();
	
	// invoke id
	private final long requestId;
	
	private final Channel channel;
	
	private final ExchangeRequest request;
	
	private final int timeout;
	
	private final Lock lock = new ReentrantLock();
	
	private final Condition done = lock.newCondition();
	
	private final long start = System.currentTimeMillis();
	
	private volatile long sent;
	
	private volatile ExchangeResponse response;
	
	private volatile ResponseCallback callback;
	
	private boolean isChannelError = false;
	
	public DefaultFuture(Channel channel, ExchangeRequest request, int timeout){
		
		this.channel = channel;
		this.request = request;
		this.requestId = request.getRequestId();
		this.timeout = timeout;
		
		FUTURES.put(requestId, this);
		CHANNELS.put(requestId, channel);
	}
	
	@Override
	public Object get() throws RemoteException{
		return get(timeout);
	}
	
	@Override
	public Object get(int timeout) throws RemoteException{
		
		int finalTimeout = timeout <= 0 ? Constants.DEFAULT_NO_RESPONSE_TIMEOUT : timeout;
		sent = System.currentTimeMillis();
		if (!isDone()){
			
			long startTime = System.currentTimeMillis();
			lock.lock();
			try {
				while(!isDone()){
					done.await(finalTimeout, TimeUnit.MICROSECONDS);
					if (isDone() || System.currentTimeMillis() - startTime > finalTimeout){
						break;
					}
				}
			} catch (InterruptedException e){
				throw new RuntimeException(e);
			} finally{
				lock.unlock();
			}
			
			if (isChannelError){
				throw new RemoteException("channel is closed, request failed");
			}
			
			if (!isDone()){
				throw new TimeoutException(sent > 0, channel, getTimeoutMessage(false));
			}
		}
		
		return returnFromResponse();
	}
	
	public void cancel(){
		
		ExchangeResponse errorResult = new ExchangeResponse(requestId);
		errorResult.setErrorMsg("request future has been canceled.");
		response = errorResult;
		FUTURES.remove(requestId);
		CHANNELS.remove(requestId);
	}
	
	@Override
	public boolean isDone(){
		return response != null;
	}
	
	@Override
	public void setCallback(ResponseCallback callback) {
		
		if (isDone()){
			invokeCallback(callback);
		} else {
			boolean isdone = false;
			lock.lock();
			try {
				if (!isDone()){
					this.callback = callback;
				} else {
					isdone = true;
				}
			} finally{
				lock.unlock();
			}
			
			if (isdone){
				invokeCallback(callback);
			}
		}
	}

	private void invokeCallback(ResponseCallback c) {
		
		ResponseCallback callbackCopy = c;
		if (null == callbackCopy){
			throw new NullPointerException("callback cannot be null.");
		}
		
		c = null;
		ExchangeResponse res = response;
		if (null == res){
			throw new IllegalStateException("response cannot be null. url: " + channel.getUrl());
		}
		
		if (res.getStatus() == CrowStatus.OK){
			
			try {
				callbackCopy.done(res.getData());
			} catch (Exception e){
				Logger.error("callback invoke error. result: " + res.getData() + ", url: " + channel.getUrl() + e);
			}
		} else if (
				res.getStatus() == CrowStatus.CLIENT_TIMEOUT || 
				res.getStatus() == CrowStatus.SERVER_TIMEOUT){
			try {
				TimeoutException te = new TimeoutException(res.getStatus() == CrowStatus.SERVER_TIMEOUT, channel, res.getErrorMsg());
				callbackCopy.caught(te);
			} catch (Exception e){
				Logger.error("callback invoke error, url:{} {}", channel.getUrl(), e);
			}
		} else if (res.getStatus() == CrowStatus.SERVICE_EXCEEDTHROTTLE){
			
			try {
				if (StringUtils.isEmpty(res.getErrorMsg())){
					res.setErrorMsg("the service exceeds the max throttle value");
				}
				RemoteException re = 
						new RemoteException(channel, res.getErrorMsg());
				callbackCopy.caught(re);
			} catch (Exception e){
				Logger.error("callback invoke error, url: {} {}",channel.getUrl(),e);
			}
		} else {
			
			try {
				RuntimeException re = new RuntimeException(res.getErrorMsg());
				callbackCopy.caught(re);
			} catch (Exception e){
				Logger.error("callback invoke error, url: {} {}",channel.getUrl(), e);
			}
		}
	}
	
	private Object returnFromResponse() throws RemoteException{
		
		ExchangeResponse res = response;
		if (null == res){
			throw  new IllegalStateException("response cannot be null");
		}
		
		if (res.getStatus() == CrowStatus.OK){
			return res.getData();
		}
		
		if (res.getStatus() == CrowStatus.CLIENT_TIMEOUT || res.getStatus() == CrowStatus.SERVER_TIMEOUT){
			throw new TimeoutException(res.getStatus() == CrowStatus.SERVER_TIMEOUT, channel, res.getErrorMsg());
		}
		
		if (res.getStatus() == CrowStatus.SERVICE_EXCEEDTHROTTLE){
			res.setErrorMsg("the service exceeds the max throttle value");
		}
		
		if (res.getStatus() == CrowStatus.SERVICE_REJECTED){
			res.setErrorMsg("the request was rejected by the service. add your ip to the service whitelist");
		}
		
		throw new RemoteException(channel,res.getErrorMsg());
	}
	
	private long getRequestId(){
		return requestId;
	}
	
	private Channel getChannel(){
		return channel;
	}
	
	private boolean isSent(){
		return sent > 0;
	}
	
	public ExchangeRequest getRequest(){
		return request;
	}
	
	private int getTimeout(){
		return timeout;
	}
	
	private long getStartTimestamp(){
		return start;
	}
	
	public static DefaultFuture getFuture(long id){
		return FUTURES.get(id);
	}
	
	public static boolean hasFuture(Channel channel){
		return CHANNELS.containsValue(channel);
	}
	
	public static void sent(Channel channel, ExchangeRequest request){
		DefaultFuture future = FUTURES.get(request.getRequestId());
		if (null != future){
			future.doSent();
		}
	}
	
	private void doSent(){
		sent = System.currentTimeMillis();
	}
	
	public static void received(Channel channel, ExchangeResponse response){
		
		if (null == response){
			
			for (Map.Entry<Long, DefaultFuture> entry : FUTURES.entrySet()){
				if (((DefaultFuture)entry.getValue()).getChannel().getUrl() == channel.getUrl()){
					((DefaultFuture)entry.getValue()).doError();
					FUTURES.remove(entry.getKey());
					CHANNELS.remove(entry.getKey());
				}
			}
		} else {
			
			try {
				DefaultFuture future = FUTURES.remove(response.getRequestId());
				if (null != future){
					future.doReceived(response);
				} else {
					Logger.warn("the timeout response finally returned at {}, "
							+ "but crow will ignore this response message {}, response: {}",
							(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date())),
							(channel == null ? "" : ",channel:"+channel.getLocalAddress()+" -> "+channel.getUrl().getHostAndPort()),
							response);
				}
			} finally {
				CHANNELS.remove(response.getRequestId());
			}
		}
	}
	
	private void doReceived(ExchangeResponse res){
		
		lock.lock();
		try {
			response = res;
			if (null != done){
				done.signal();
			}
		} finally {
			lock.unlock();
		}
		
		if (null != callback){
			invokeCallback(callback);
		}
	}
	
	private void doError(){
		
		isChannelError = true;
		lock.lock();
		try {
			response = new ExchangeResponse();
			if (null != done){
				done.signal();
			}
		} finally {
			lock.unlock();
		}
		
		if (null != callback){
			invokeCallback(callback);
		}
	}

	private String getTimeoutMessage(boolean scan){
		
		long nowTimestamp = System.currentTimeMillis();
		return (sent > 0 ? "waiting server-side response timeout" : "sending request timeout in client-side") +
				(scan ? " by scan timer" : "") + ". start time: "
				+ (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(start))) + ", end time: "
				+ (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date())) + ","
				+ (sent > 0 ? 
						" client elapsed: " + (sent - start) + " ms, server elapsed: " + (nowTimestamp - start)
						: 
						" elapsed: " + (nowTimestamp - start)) 
				+ " ms, timeout: " + timeout 
				+ " ms, channle: " + channel.getLocalAddress() + " -> " + channel.getUrl().getHostAndPort() + ", request: " + request;
	}
	
	private static class RemoteInvocationTimoutScan implements Runnable{

		@Override
		public void run() {
			
			while(true){
				try {
					for (DefaultFuture future : FUTURES.values()){
						
						if (null == future || future.isDone()){
							continue;
						}
						
						if (System.currentTimeMillis() - future.getStartTimestamp() > future.getTimeout()){
							// create exception response;
							ExchangeResponse timeoutResponse = new ExchangeResponse(future.getRequestId());
							// set timeout status
							timeoutResponse.setStatus(
									future.isSent() ? CrowStatus.SERVER_TIMEOUT : CrowStatus.CLIENT_TIMEOUT);
							timeoutResponse.setErrorMsg(future.getTimeoutMessage(true));
							// handle response
							DefaultFuture.received(future.getChannel(), timeoutResponse);
						}
					}
					Thread.sleep(30);
				} catch (Exception e){
					Logger.error("exception when scan the timeout invocation of remote. {}",e);
				}
				
			}
		}
		
	}
	
	static {
		Thread th = new Thread(new RemoteInvocationTimoutScan(), "CrowResponseTimeoutScanTimer");
		th.setDaemon(true);
		th.start();
	}
}
