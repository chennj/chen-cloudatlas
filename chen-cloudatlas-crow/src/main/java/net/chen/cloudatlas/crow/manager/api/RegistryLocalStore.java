package net.chen.cloudatlas.crow.manager.api;

/**
 * Registry数据本地保存接口
 * @author chenn
 *
 */
public interface RegistryLocalStore {

	/**
	 * 保存
	 * @param data
	 * @throws Exception
	 */
	void save(RegistryData data) throws Exception;
	
	/**
	 * 读取
	 * @param applicationName
	 * @return
	 * @throws Exception
	 */
	RegistryData load(final String applicationName) throws Exception;
}
