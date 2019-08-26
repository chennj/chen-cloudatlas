package net.chen.cloudatlas.crow.manager.api;

/**
 * 配置变化时事件处理<br>
 * @author chenn
 *
 * @param <T>
 */
public interface RegistryEventWatcher<T> {

	void onEvent(RegistryEvent<T> event);
	
	Class<T> payloadClass();
}
