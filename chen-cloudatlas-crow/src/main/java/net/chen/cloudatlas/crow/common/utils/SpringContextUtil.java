package net.chen.cloudatlas.crow.common.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

public class SpringContextUtil {

	//Spring应用上下文
	private static ApplicationContext applicationContext;
	
	public static void setApplicationContext(ApplicationContext applicationContext){
		SpringContextUtil.applicationContext = applicationContext;
	}
	
	public static ApplicationContext getApplicationContext(){
		return applicationContext;
	}
	
	/**
	 * 获取对象
	 * @param name
	 * @return
	 * @throws BeansException
	 */
	public static Object getBean(String name)throws BeansException{
		return applicationContext.getBean(name);
	}
	
	public static <T> T getBean(Class<T> clazz)throws BeansException{
		return applicationContext.getBean(clazz);
	}
}
