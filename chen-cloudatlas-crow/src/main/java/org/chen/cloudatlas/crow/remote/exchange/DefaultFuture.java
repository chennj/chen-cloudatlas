package org.chen.cloudatlas.crow.remote.exchange;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.chen.cloudatlas.crow.common.Constants;
import org.chen.cloudatlas.crow.remote.Channel;
import org.chen.cloudatlas.crow.remote.RemoteException;

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
	
	private boolean isChannelError = false;
	
	public DefaultFuture(Channel channel, ExchangeRequest request, int timeout){
		
		this.channel = channel;
		this.request = request;
		this.requestId = request.getRequestId();
		this.timeout = timeout;
		
		FUTURES.put(requestId, this);
		CHANNELS.put(requestId, channel);
	}
	
	public Object get() throws RemoteException{
		return get(timeout);
	}
	
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
	
	@Override
	public Object get() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object get(int timeout) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCallback(ResponseCallback callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isDone() {
		// TODO Auto-generated method stub
		return false;
	}

}
