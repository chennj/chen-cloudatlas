package org.chen.cloudatlas.crow.manager.api;

import org.chen.cloudatlas.crow.common.NameableService;
import org.chen.cloudatlas.crow.common.URL;

/**
 * 
 * @author chenn
 *
 */
public interface RegistryManager extends NameableService{

	/**
	 * 获取注册中心实例
	 * @param url
	 * @return
	 */
	RegistryClient getRegistry(URL url);
	
	/**
	 * 获取注册中心数据本地保存对象
	 * @return
	 */
	RegistryLocalStore getLocalStore();
}
