package net.chen.cloudatlas.crow.bootstrap;

import io.netty.util.internal.logging.InternalLoggerFactory;
import net.chen.cloudatlas.crow.remote.log.CrowLoggerFactory;
import net.chen.cloudatlas.crow.server.AbstractServerPayloadListener;

public class CrowBootstrap {

	static {
		InternalLoggerFactory.setDefaultFactory(new CrowLoggerFactory());
	}
	
	private CrowBootstrap(){
		
	}
	
	private static Bootable bootstrap;
	
	public static synchronized Bootable getBootstrap(){
		
		if (null == bootstrap){
			bootstrap = new Bootstrap();
		}
		return bootstrap;
	}
	
	public static synchronized Bootable getBootstrap(AbstractServerPayloadListener serverListener){
		
		if (null == bootstrap){
			bootstrap = new Bootstrap(serverListener);
		}
		return bootstrap;
	}
}
