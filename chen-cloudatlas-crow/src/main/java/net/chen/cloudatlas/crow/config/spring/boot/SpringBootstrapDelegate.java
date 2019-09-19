package net.chen.cloudatlas.crow.config.spring.boot;

import net.chen.cloudatlas.crow.config.CrowConfig;
import net.chen.cloudatlas.crow.server.AbstractServerPayloadListener;

public class SpringBootstrapDelegate {

	private static SpringBootstrap bootstrap;
	
	private static CrowConfig crowConfig;
	
	private SpringBootstrapDelegate(){}
	
	/**
	 * 仅仅用于客户端角色
	 * @return
	 */
	public static synchronized SpringBootstrap getSpringBootstrap(){
		if (bootstrap == null){
			bootstrap = new SpringBootstrap();
		}
		return bootstrap;
	}
	
	public static synchronized SpringBootstrap getSpringBootstrap(AbstractServerPayloadListener serverListener){
		
		if (bootstrap == null){
			bootstrap = new SpringBootstrap(serverListener);
		}
		return bootstrap;
	}

	public static CrowConfig getCrowConfig() {
		return crowConfig;
	}

	public static void setCrowConfig(CrowConfig crowConfig) {
		SpringBootstrapDelegate.crowConfig = crowConfig;
	}
	
	
}
