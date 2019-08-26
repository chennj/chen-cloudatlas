package net.chen.cloudatlas.crow.rpc;

import java.net.InetSocketAddress;

import net.chen.cloudatlas.crow.common.utils.UrlUtil;

public class Context {

	private static final ThreadLocal<Context> LOCAL = new ThreadLocal<Context>(){

		@Override
		protected Context initialValue() {
			return new Context();
		}
		
	};
	
	public static Context getContext(){
		return LOCAL.get();
	}
	
	public static void removeContext(){
		LOCAL.remove();
	}
	
	private InetSocketAddress localAddress;
	
	private InetSocketAddress remoteAddress;

	public InetSocketAddress getLocalAddress() {
		return localAddress;
	}

	public void setLocalAddress(InetSocketAddress localAddress) {
		this.localAddress = localAddress;
	}

	public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(InetSocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}
	
	public String getRemoteHost(){
		return UrlUtil.getAddressKey(remoteAddress);
	}
}
