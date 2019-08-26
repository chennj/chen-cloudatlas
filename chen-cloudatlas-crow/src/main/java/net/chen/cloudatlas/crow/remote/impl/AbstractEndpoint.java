package net.chen.cloudatlas.crow.remote.impl;

import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.remote.ChannelListener;
import net.chen.cloudatlas.crow.remote.Codec;
import net.chen.cloudatlas.crow.remote.Endpoint;

public abstract class AbstractEndpoint implements Endpoint{

	private Codec codec;
	
	protected ChannelListener listener;
	
	private URL url;
	
	protected volatile boolean shutdown;
	
	public AbstractEndpoint(URL url, ChannelListener listener){
		
		if (null == url){
			throw new IllegalArgumentException("url is null");
		}
		
		this.url = url;
		this.listener = listener;
	}
	
	@Override
	public URL getUrl(){
		return url;
	}
	
	protected void setUrl(URL url){
		
		if (null == url){
			throw new IllegalArgumentException("url is null");
		}
		
		this.url = url;
	}
	
	protected Codec getCodec(){
		return codec;
	}
	
	@Override
	public ChannelListener getChannelListener(){
		return listener;
	}
	
	@Override
	public void shutDown(int timeout){
		shutDown();
	}
	
	@Override 
	public void shutDown(){
		shutdown = true;
	}
	
	@Override
	public boolean isShutDown(){
		return shutdown;
	}
}
