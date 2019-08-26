package net.chen.cloudatlas.crow.remote;

import java.net.InetSocketAddress;

public interface Channel extends Endpoint{

	InetSocketAddress getRemoteAddress();
	
	boolean isConnected();
}
