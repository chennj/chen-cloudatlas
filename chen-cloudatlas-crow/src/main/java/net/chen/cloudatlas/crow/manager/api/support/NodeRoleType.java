package net.chen.cloudatlas.crow.manager.api.support;

/**
 * Registry 节点角色<br>
 * @author chenn
 *
 */
public enum NodeRoleType {
	
	/**
	 * 服务提供者
	 */
	PROVIDER,
	
	/**
	 * 服务消费者
	 */
	CONSUMER,
	
	/**
	 * 配置管理指令
	 */
	COMMAND;
}
