package org.chen.cloudatlas.crow.manager.api.support;

import org.chen.cloudatlas.crow.manager.api.model.command.AccessData;
import org.chen.cloudatlas.crow.manager.api.model.command.LimitData;
import org.chen.cloudatlas.crow.manager.api.model.command.TokenData;
import org.chen.cloudatlas.crow.manager.api.model.command.WeightData;

/**
 * Registry指令类型<br>
 * @author chenn
 *
 */
public enum CommandType {

	/**
	 * Consumer 监听：Provider 的权重，权重设置为0则为服务禁用
	 */
	WEIGHT(NodeRoleType.PROVIDER, NodeRoleType.CONSUMER, WeightData.class),
	
	/**
	 * Provider 监听：某台Provider节点限流
	 */
	LIMIT(NodeRoleType.PROVIDER, NodeRoleType.PROVIDER, LimitData.class),
	
	/**
	 * Provider 监听：黑白名单控制，非黑即白
	 */
	ACCESS(NodeRoleType.PROVIDER, NodeRoleType.PROVIDER, AccessData.class),

	/**
	 * Provider 监听：更新调用凭据
	 */
	TOKEN(NodeRoleType.PROVIDER, NodeRoleType.PROVIDER, TokenData.class);
	
	private final NodeRoleType issuer;
	private final NodeRoleType executor;
	private final Class<?> dataClass;
	
	private CommandType(final NodeRoleType issuer, final NodeRoleType executor, final Class<?> dataClass){
		this.issuer = issuer;
		this.executor = executor;
		this.dataClass = dataClass;
	}

	public NodeRoleType getIssuer() {
		return issuer;
	}

	public NodeRoleType getExecutor() {
		return executor;
	}

	public Class<?> getDataClass() {
		return dataClass;
	}
	
}
