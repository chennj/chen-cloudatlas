package org.chen.cloudatlas.crow.manager.api.support;

/**
 * Command 执行器接口
 * @author chenn
 *
 */
public interface RegistryCommandExecutor<T> {

	void execute(CommandContext<T> context);
	
	Class<T> contextDataClass();
}
