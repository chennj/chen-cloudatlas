package org.chen.cloudatlas.crow.common.utils;

import java.net.InetSocketAddress;
import java.net.URL;

import org.chen.cloudatlas.crow.common.Constants;
import org.springframework.util.StringUtils;

public class UrlUtil {

	private UrlUtil(){}
	
	public static String getAddressKey(InetSocketAddress address){
		
		return address.getAddress().getHostAddress() +
				Constants.IP_PORT_SEPERATOR +
				address.getPort();
	}
	
	public static String getAddressKey(URL url){
		
		return url.getHost() +
				Constants.IP_PORT_SEPERATOR +
				url.getPort();
	}

	public static int getParameter(org.chen.cloudatlas.crow.common.URL url, String key,
			int defaultValue) {
		
		if (null == url){
			return defaultValue;
		}
		
		String value = url.getParameter(key);
		
		if (StringUtils.isEmpty(value)){
			return defaultValue;
		} else {
			return Integer.valueOf(value);
		}
	}
	
	public static String getParameter(org.chen.cloudatlas.crow.common.URL url, String key,
			String defaultValue) {
		
		if (null == url){
			return defaultValue;
		}
		
		String value = url.getParameter(key);
		
		if (StringUtils.isEmpty(value)){
			return defaultValue;
		} else {
			return value;
		}
	}
}
