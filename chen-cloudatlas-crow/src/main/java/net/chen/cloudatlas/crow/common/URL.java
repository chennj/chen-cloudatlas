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

	public static URL valueOf(String originUrl){
		
		if (StringUtils.isEmpty(originUrl)){
			throw new IllegalArgumentException("url is null");
		}
		
		originUrl = originUrl.trim();
		String protocol = null;
		String host = null;
		int port = 0;
		String path = null;
		Map<String, String> parameters = null;
		int i = originUrl.indexOf("?");
		if (i>=0){
			String[] parts = originUrl.substring(i+1).split("\\&");
			parameters = new HashMap<>();
			for(String part : parts){
				part = part.trim();
				if (part.length()>0){
					int j = part.indexOf("=");
					if (j>=0){
						parameters.put(part.substring(0,j), part.substring(j+1));
					} else {
						parameters.put(part, part);
					}
				}
			}
			originUrl = originUrl.substring(0,i);
		}
		i = originUrl.indexOf("://");
		if (i>=0){
			if (i==0){
				throw new IllegalStateException("url missing protocol:\""+originUrl+"\"");
			}
			protocol = originUrl.substring(0,i);
			originUrl = originUrl.substring(i+3);
		} else {
			// case: file://path/to/file.txt
			i = originUrl.indexOf(":/");
			if (i>=0){
				if (i==0){
					throw new IllegalStateException("url missing protocol:\"" + originUrl + "\"");
				}
				protocol = originUrl.substring(0,i);
				originUrl = originUrl.substring(i+1);
			}
		}
		
		i = originUrl.indexOf("/");
		if (i>=0){
			path = originUrl.substring(i+1);
			originUrl = originUrl.substring(0,i);
		}
		
		i = originUrl.indexOf(":");
		if (i>=0 && i<originUrl.lastIndexOf(-1)){
			port = Integer.parseInt(originUrl.substring(i+1));
			originUrl = originUrl.substring(0,i);
		}
		if (originUrl.length()>0){
			host = originUrl;
		}
		return new URL(protocol, host, port, path, parameters);
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
	
	public void setProtocol(String protocol){
		this.protocol = protocol;
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
	
	public URL addParameters(String... pairs){
		
		if (null == pairs || pairs.length == 0){
			return this;
		}
		
		if (pairs.length % 2 != 0){
			throw new IllegalArgumentException("map pairs can not be odd number.");
		}
		
		Map<String, String> map = new HashMap<String, String>();
		int len = pairs.length / 2;
		for (int i=0; i<len; i++){
			map.put(pairs[2*i], pairs[2*i+1]);
		}
		return addParameters(map);
	}
	
	public URL addParameters(Map<String, String> parameters){
		
		if (null == parameters || parameters.size() == 0){
			return this;
		}
		
		boolean hasAndEqual = true;
		for (Map.Entry<String, String> entry : parameters.entrySet()){
			
			String value = getParameters().get(entry.getKey());
			if (null==value || entry.getValue()==null ||!value.equals(entry.getValue())){
				hasAndEqual = false;
				break;
			}
		}
		
		if (hasAndEqual){
			// 如果没有修改，直接返回
			return this;
		}
		
		Map<String, String> map = new HashMap<>();
		map.putAll(parameters);
		return new URL(protocol, host, port, path, map);
	}
	
	public double getParameter(String key, double defaultValue){
		
		Number n = getNumbers().get(key);
		if (null != n){
			return n.doubleValue();
		}
		
		String value = getParameter(key);
		if (StringUtils.isEmpty(value)){
			return defaultValue;
		}
		
		double d = Double.parseDouble(value);
		getNumbers().put(key, d);
		return d;
	}
	
	public float getParameter(String key, float defaultValue){
		
		Number n = getNumbers().get(key);
		if (null != n){
			return n.floatValue();
		}
		
		String value = getParameter(key);
		if (StringUtils.isEmpty(value)){
			return defaultValue;
		}
		
		float f = Float.parseFloat(value);
		getNumbers().put(key, f);
		return f;
	}
	
	public long getParameter(String key, long defaultValue){
		
		Number n = this.getNumbers().get(key);
		if (null!=n){
			return n.longValue();
		}
		
		String value = this.getParameter(key);
		if (StringUtils.isEmpty(value)){
			return defaultValue;
		}
		
		long l = Long.parseLong(value);
		this.getNumbers().put(key, l);
		return l;
	}
	
	public short getParameter(String key, short defaultValue){
		
		Number n = this.getNumbers().get(key);
		if (null!=n){
			return n.shortValue();
		}
		
		String value = this.getParameter(key);
		if (StringUtils.isEmpty(value)){
			return defaultValue;
		}
		
		short s = Short.parseShort(value);
		this.getNumbers().put(key, s);
		return s;
	}
	
	public byte getParameter(String key, byte defaultValue){
		
		Number n = this.getNumbers().get(key);
		if (null!=n){
			return n.byteValue();
		}
		
		String value = this.getParameter(key);
		if (StringUtils.isEmpty(value)){
			return defaultValue;
		}
		
		byte b = Byte.parseByte(value);
		this.getNumbers().put(key, b);
		return b;
	}
	
	public float getPositiveParamter(String key, float defaultValue){
		
		if (0>=defaultValue){
			throw new IllegalArgumentException("defaultValue <= 0");
		}
		
		float value = getParameter(key,defaultValue);
		if (0>=value){
			return defaultValue;
		}
		return value;
	}
	
	public double getPositiveParamter(String key, double defaultValue){
		
		if (0>=defaultValue){
			throw new IllegalArgumentException("defaultValue <= 0");
		}
		
		double value = getParameter(key,defaultValue);
		if (0>=value){
			return defaultValue;
		}
		return value;
	}
	
	public long getPositiveParamter(String key, long defaultValue){
		
		if (0>=defaultValue){
			throw new IllegalArgumentException("defaultValue <= 0");
		}
		
		long value = getParameter(key,defaultValue);
		if (0>=value){
			return defaultValue;
		}
		return value;
	}
	
	public int getPositiveParamter(String key, int defaultValue){
		
		if (0>=defaultValue){
			throw new IllegalArgumentException("defaultValue <= 0");
		}
		
		int value = getParameter(key,defaultValue);
		if (0>=value){
			return defaultValue;
		}
		return value;
	}
	
	public short getPositiveParamter(String key, short defaultValue){
		
		if (0>=defaultValue){
			throw new IllegalArgumentException("defaultValue <= 0");
		}
		
		short value = getParameter(key,defaultValue);
		if (0>=value){
			return defaultValue;
		}
		return value;
	}
	
	public byte getPositiveParamter(String key, byte defaultValue){
		
		if (0>=defaultValue){
			throw new IllegalArgumentException("defaultValue <= 0");
		}
		
		byte value = getParameter(key,defaultValue);
		if (0>=value){
			return defaultValue;
		}
		return value;
	}
	
	public char getParameter(String key, char defaultValue){
		
		String value = getParameter(key);
		if (StringUtils.isEmpty(value)){
			return defaultValue;
		}
		return value.charAt(0);
	}
	
	public boolean getParameter(String key, boolean defaultValue){
		
		String value = getParameter(key);
		if (StringUtils.isEmpty(value)){
			return defaultValue;
		}
		return Boolean.parseBoolean(value);
	}
}
