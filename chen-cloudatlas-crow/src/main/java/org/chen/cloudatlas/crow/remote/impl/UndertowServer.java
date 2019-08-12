package org.chen.cloudatlas.crow.remote.impl;

import org.chen.cloudatlas.crow.remote.HttpServer;

import io.undertow.Undertow;

public class UndertowServer implements HttpServer{

	private String ip;
	private int port;
	private Undertow server;
	
	public UndertowServer(String ip, int httpPort) {
		this.ip = ip;
		this.port = httpPort;
	}

	@Override
	public void start() {
		
		Undertow _server = Undertow.builder()
				.addHttpListener(port, ip)
				.setHandler(new DefaultHttpHandler())
				.build();
		_server.start();
		this.server = _server;
	}

	@Override
	public void shutDown() {
		
		this.server.stop();
	}

}
