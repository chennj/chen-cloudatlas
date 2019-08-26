package net.chen.cloudatlas.crow.remote.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.remote.Channel;
import net.chen.cloudatlas.crow.remote.RemoteException;
import net.chen.cloudatlas.crow.remote.Request;
import net.chen.cloudatlas.crow.remote.Response;
import net.chen.cloudatlas.crow.remote.TimeoutException;

/**
 * token to match the right request while response arrives
 * 
 * @author chenn
 *
 */
public class RequestToken {

	private final CountDownLatch latch = new CountDownLatch(1);
	
	private Response response;
	
	private boolean isRequestError = false;
	
	private String key;
	
	private long timeout; //millisecond
	
	private long start;
	
	private Channel channel;
	
	private Request request;
	
	public RequestToken(String key, long timeout, Channel channel, Request request){
		
		this.key = key;
		this.timeout = timeout;
		this.channel = channel;
		this.request = request;
	}
	
	public Response get() throws RemoteException{
		
		start = System.currentTimeMillis();
		boolean ok;
		
		try {
			/**
			 * until response message has arrived or time out
			 */
			ok = latch.await(this.timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e){
			throw new RuntimeException(e);
		} finally{
			// remove RequestToken in reqTokenMap when time is out or resposne arrives
			Logger.debug("remove (" + key + ") from reqTokenMap");
			if (NettyClient.REQ_TOKEN_MAP.containsKey(key)){
				NettyClient.REQ_TOKEN_MAP.remove(key);
			}
		}
		
		// skip timeout and throw exception if net working go wrong when invoke
		if (isRequestError){
			throw new RemoteException("channel has been closed, Request failed");
		}
		
		// throw TimeoutException if time out
		if (!ok){
			throw new TimeoutException(true, channel, getTimeoutMessage());
		}
		
		// response could be null since latch.await() can be returned in timeout limit
		return response;
	}
	
	public Channel getChannel(){
		return channel;
	}
	
	public void handle(Response response){
		this.response = response;
		latch.countDown();
	}
	
	public void handleError(){
		isRequestError = true;
		latch.countDown();
	}
	
	private String getTimeoutMessage(){
		
		long nowTimestamp = System.currentTimeMillis();
		
		return ("Waiting server-side response timeout. start time:"
				+ (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(start))) + ", end time:"
				+ (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date())) + ","
				+ " elapsed: " + (nowTimestamp - start) + " ms, timeout: "
				+ timeout + " ms, channel: " + channel.getLocalAddress()
				+ " -> " + channel.getUrl().getHostAndPort() + ", request: " + request);
	}
}
