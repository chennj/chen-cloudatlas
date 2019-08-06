package org.chen.cloudatlas.crow.common;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.chen.cloudatlas.crow.common.utils.CollectionUtil;
import org.springframework.util.StringUtils;

/**
 * 存放用户配置信息
 * <p><font color=red>
 * 有待完成
 * </font></p>
 * @author chenn
 *
 */
public class URL implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String protocol;
	private String host;
	private int port;
	private String path;
	private Map<String, String> parameters;
	private transient volatile Map<String, Number> numbers;
	
	public URL(){}
	
	public URL(String host, int port){
		
		this(Constants.DEFAULT_PROTOCOL, host, port);
	}

	public URL(String protocol, String host, int port) {
		
		this(protocol, host, port, null, (Map<String, String>)null);
	}

	public URL(String protocol, String host, int port, String path, Map<String, String> originParams) {
		
		this.protocol = protocol;
		this.host = host;
		this.port = port;
		this.path = path;
		
		if (originParams == null){
			originParams = new HashMap<>();
		} else {
			originParams = new HashMap<>(originParams);
		}
		
		this.parameters = Collections.unmodifiableMap(originParams);
	}
	
	public URL(String protocol, String host, int port, String path, String... pairs) {
		this(protocol, host, port, path, CollectionUtil.toStringMap(pairs));
	}

	public URL(String protocol, String host, int port, Map<String, String> parameters) {
		this(protocol, host, port, null, parameters);
	}

	public String getHostAndPort(){
		return host + Constants.IP_PORT_SEPERATOR + port;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
	
	public String getParameter(String key){
		return parameters.get(key);
	}
	
	public String getParameter(String key, String defaultValue){
		
		String value = getParameter(key);
		if (StringUtils.isEmpty(value)){
			return defaultValue;
		}
		return value;
	}

	public InetSocketAddress getSocketAddress() {
		return new InetSocketAddress(host, port);
	}

	public String getProtocol() {
		return this.protocol;
	}
}
