package org.chen.cloudatlas.crow.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * 
 * @author chenn
 *
 */
public class SpringContextUtil {

	/**
	 * spring应用上下文环境
	 */
	private static ApplicationContext applicationContext;
	
	public static void SetApplicationContext(ApplicationContext applicationContext){
		SpringContextUtil.applicationContext = applicationContext;
	}
	
	public static ApplicationContext getApplicationContext(){
		return applicationContext;
	}
	
	public static Object getBean(String name) throws BeansException{
		return applicationContext.getBean(name);
	}
	
	public static <T> T getBean(Class<T> clazz) throws BeansException{
		return applicationContext.getBean(clazz);
	}
}
