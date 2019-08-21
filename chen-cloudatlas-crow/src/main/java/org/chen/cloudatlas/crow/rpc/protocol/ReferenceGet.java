package org.chen.cloudatlas.crow.rpc.protocol;

import org.chen.cloudatlas.crow.config.ReferenceConfig;
import org.chen.cloudatlas.crow.rpc.Invoker;
import org.chen.cloudatlas.crow.rpc.Protocol;
import org.chen.cloudatlas.crow.rpc.ProxyFactory;

/**
 * <b><font color=red>
 * 未完成
 * </font></b>
 * @author chenn
 *
 */
public class ReferenceGet<T> {

	ReferenceConfig<T> config;
	
	private T ref;
	private Invoker<T> finalInvoker;
	private Protocol refProtocol;
	private ProxyFactory proxyFactory;
	
	public ReferenceGet(ReferenceConfig<T> config){
		this.config = config;
	}
	
	public ReferenceConfig<T> getReferenceConfig(){
		return this.config;
	}
}
