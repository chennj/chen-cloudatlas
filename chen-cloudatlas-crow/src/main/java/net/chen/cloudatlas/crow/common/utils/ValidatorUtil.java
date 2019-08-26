package net.chen.cloudatlas.crow.common.utils;

import net.chen.cloudatlas.crow.common.Constants;

public class ValidatorUtil {

	private ValidatorUtil(){}
	
	public static boolean validateIpAndPort(String ipAndPort){
		return Constants.PATTERN_IP_AND_PORT.matcher(ipAndPort).matches();
	}

	public static boolean validateEmail(String string) {
		return Constants.PATTERN_EMAIL.matcher(string).matches();
	}

	public static boolean validatePhone(String string) {
		return Constants.PATTERN_PHONE.matcher(string).matches();
	}

	public static boolean validateIp(String ip) {
		return Constants.PATTERN_IP.matcher(ip).matches();
	}
}
