package net.chen.cloudatlas.crow.remote;

import net.chen.cloudatlas.crow.config.ServiceConfig;

/**
 * @author chenn
 *
 */
public class IPFilter {

	public static boolean isRejected(ServiceConfig<?> sc, String address){
		
		if (null == sc){
			return false;
		}
		
		//在config中进行校验，不可能出现既有白名单又有黑名单的状况
		String ipBlackList = sc.getIpBlackList();
		String ipWhiteList = sc.getIpWhiteList();
		
		if (null != ipWhiteList && ipWhiteList.length() > 0){
			return !(sc.getIpWhiteSet().contains(address));
		} else if (null != ipBlackList && ipBlackList.length() > 0){
			return sc.getIpBlackSet().contains(address);
		} else {
			return false;
		}
		
	}
}
