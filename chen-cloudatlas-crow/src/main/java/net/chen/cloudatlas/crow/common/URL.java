package net.chen.cloudatlas.crow.common;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.StringUtils;

import net.chen.cloudatlas.crow.common.utils.CollectionUtil;

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
	
	public int getParameter(String key, int defaultValue){
		
		Number n = getNumbers().get(key);
		if (null != n){
			return n.intValue();
		}
		
		String value = getParameter(key);
		if (StringUtils.isEmpty(value)){
			return defaultValue;
		}
		
		int i = Integer.parseInt(value);
		getNumbers().put(key,i);
		return i;
	}

	public InetSocketAddress getSocketAddress() {
		return new InetSocketAddress(host, port);
	}

	public String getProtocol() {
		return this.protocol;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public int getPositiveParameter(String key, int defaultValue) {

		if (defaultValue <= 0){
			throw new IllegalArgumentException("defaultValue <= 0");
		}
		
		int value = getParameter(key, defaultValue);
		if (value <=0){
			return defaultValue;
		}
		return value;
	}
	
	private Map<String, Number> getNumbers(){
		
		if (null == numbers){
			numbers = new ConcurrentHashMap<String, Number>();
		}
		return numbers;
	}

	public String toIdentityString() {
		return buildString(true);
	}

	private String buildString(boolean appendParamter, String... parameters) {
		
		StringBuilder sb = new StringBuilder();
		
		if (!StringUtils.isEmpty(protocol)){
			sb.append(protocol).append("://");
		}
		
		if (!StringUtils.isEmpty(host)){
			sb.append(host);
			if (port>0){
				sb.append(":").append(port);
			}
		}
		
		if (!StringUtils.isEmpty(path)){
			sb.append("/").append(path);
		}
		
		if (appendParamter){
			buildParameter(sb,true,parameters);
		}
		
		return sb.toString();
	}

	private void buildParameter(StringBuilder sb, boolean concat, String[] parameters) {
		
		if (null != getParameters() && getParameters().size() > 0){
			
			List<String> includes = parameters == null || parameters.length == 0 ? null : Arrays.asList(parameters);
			boolean first = true;
			
			for (Map.Entry<String, String> entry : new TreeMap<>(getParameters()).entrySet()){
				
				if (!StringUtils.isEmpty(entry.getKey()) 
						&& (null == includes || includes.contains(entry.getKey()))){
					if (first){
						if (concat){
							sb.append("?");
						}
						first = false;
					} else {
						sb.append("&");
					}
				}
				
				sb.append(entry.getKey());
				sb.append("=");
				sb.append(entry.getValue() == null ? "" : entry.getValue().trim());
			}
		}
	}
}
