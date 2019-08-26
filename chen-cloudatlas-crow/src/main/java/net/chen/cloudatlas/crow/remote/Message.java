package net.chen.cloudatlas.crow.remote;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author chenn
 *
 */
public interface Message {

	public static final AtomicInteger SEED = new AtomicInteger(0);
	
	String getProtocol();
	
	String getTokenKey();
	
	byte[] getPayload();
	
	void setPayload(byte[] payload);
	
	boolean isHeartbeat();
	
	String getServiceId();
	
	String getServiceVersion();
}
