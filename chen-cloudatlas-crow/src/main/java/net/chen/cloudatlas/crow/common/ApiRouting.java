package net.chen.cloudatlas.crow.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ApiRouting {

	private static final Map<String, Api> routingCache = new HashMap<>();
	
	private static final Map<String, String[]> parametersCache = new HashMap<>();
	
	public static void cacheApiRoute(String path, Api api){
		routingCache.put(path, api);
	}
	
	public static void cacheParametersRoute(String path, String[] parameters){
		parametersCache.put(path, parameters);
	}
	
	public static String[] getParameters(String path){
		return parametersCache.get(path);
	}
	
	public static Api getApi(String path){
		return routingCache.get(path);
	}
	
	public static class Path{
		
		public static String build(String serviceId, String serviceVersion, String apiName){
			String requestPath = "/" + serviceId + "/" + serviceVersion + "/" + apiName;
			return requestPath;
		}
	}
	
	public static class Api{
		private Object invoker;
		private Method method;
		
		private Api(Object invoker, Method method){
			this.invoker = invoker;
			this.method = method;
		}
		
		public static Api build(Object invoker, Method method){
			return new Api(invoker, method);
		}
		
		public Object invoke(Object[] arguments) throws InvocationTargetException, IllegalAccessException{
			return method.invoke(invoker, arguments);
		}
	}
}
