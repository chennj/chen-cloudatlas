package net.chen.cloudatlas.crow.rpc.utils;

import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.rpc.Invocation;
import net.chen.cloudatlas.crow.rpc.Invoker;

public class RpcUtil {

	public static String getMethodName(Invocation invocation){
		
		if (	"$invoke".equals(invocation.getMethodName()) &&
				invocation.getArguments() != null &&
				invocation.getArguments().length > 0 &&
				invocation.getArguments()[0] instanceof String){
			return (String)invocation.getArguments()[0];
		}
		
		return invocation.getMethodName();
	}
	
	public static boolean isConsumer(Invoker<?> invoker){
		return isConsumer(invoker.getUrl());
	}
	
	public static boolean isConsumer(URL url){
		return Constants.CONSUMER.equals(url.getParameter(Constants.SIDE));
	}
}
