package net.chen.cloudatlas.crow.rpc.protocol;

import net.chen.cloudatlas.crow.config.ReferenceConfig;
import net.chen.cloudatlas.crow.rpc.Invoker;
import net.chen.cloudatlas.crow.rpc.Protocol;
import net.chen.cloudatlas.crow.rpc.ProxyFactory;

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
