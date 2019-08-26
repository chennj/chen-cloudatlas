package net.chen.cloudatlas.crow.manager.api;

/**
 * 连接状态变化监听器
 * @author chenn
 *
 */
public interface RegistryConnectionStateListener {

	/**
	 * 链接状态变化
	 * @param newState
	 */
	void stateChanged(RegistryConnectionState newState);
	
	/**
	 * 注销,<b><font color=red>放在这有待商榷</font></b><br>
	 */
	void unregister();
}
