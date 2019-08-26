package net.chen.cloudatlas.crow.common;

/**
 * 
 * @author chenn
 *
 */
public class KeyUtil {

	public static String getServiceKey(String serviceId, String serviceVersion){
		return serviceId + ":" + serviceVersion;
	}
}
