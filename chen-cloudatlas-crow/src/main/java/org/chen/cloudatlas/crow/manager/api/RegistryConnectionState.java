package org.chen.cloudatlas.crow.manager.api;

/**
 * Registry连接状态<br>
 * @author chenn
 *
 */
public enum RegistryConnectionState {

	NO_CONNECTION_YET,	//启动时，在一定时间内未能连接到Registry，会变为此状态，后续应重试
	CONNECTED,
	SUSPENDED,
	RECONNECTED,
	LOST,
	READ_ONLY;
}
