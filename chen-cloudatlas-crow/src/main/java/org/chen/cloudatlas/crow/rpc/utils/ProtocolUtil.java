package org.chen.cloudatlas.crow.rpc.utils;

import org.chen.cloudatlas.crow.common.Constants;
import org.chen.cloudatlas.crow.common.URL;
import org.springframework.util.StringUtils;

/**
 * 
 * @author chenn
 *
 */
public class ProtocolUtil {
	
	private ProtocolUtil(){}
	
	public static String serviceKeyOld(URL url){
		
		return serviceKey(url.getPort(), url.getPath(), null, null);
	}

	public static String serviceKey(int port, String serviceName, String dc, String serviceVersion) {
		
		StringBuffer sb = new StringBuffer();
		
		if (dc != null && dc.trim().length() > 0){
			sb.append(dc);
			sb.append("/");
		}
		
		sb
		.append(serviceName)
		.append(":")
		.append(port);
		
		if (!StringUtils.isEmpty(serviceVersion)){
			sb.append(":");
			sb.append(serviceVersion);
		}
		
		return sb.toString();
	}
	
	public static String invokeKey(URL url){
		
		return invokeKey(url.getHost(), url.getPort(), url.getPath(), url.getParameter(Constants.SERVICE_VERSION));
	}

	public static String invokeKey(String ip, int port, String serviceName, String serviceVersion) {
		
		StringBuilder sb = new StringBuilder();
		
		sb
		.append(ip)
		.append(":")
		.append(port)
		.append("\\");
		
		if (!StringUtils.isEmpty(serviceName)){
			sb.append(serviceName);
		}
		
		if (!StringUtils.isEmpty(serviceVersion)){
			sb.append(":");
			sb.append(serviceVersion);
		}

		return sb.toString();
	}

}
