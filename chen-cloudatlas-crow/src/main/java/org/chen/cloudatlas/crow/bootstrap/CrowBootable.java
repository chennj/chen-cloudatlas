package org.chen.cloudatlas.crow.bootstrap;

/**
 * 
 * @author chenn
 *
 */
public interface CrowBootable {
	
	/**
	 * 服务端或客户端启动
	 */
	void start();
	
	/**
	 * 服务端或客户端关闭
	 */
	void shutDown();
}
