package org.chen.cloudatlas.crow.common.utils;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.springframework.util.StringUtils;
import org.tinylog.Logger;

public class DuplicatChecker {

	private DuplicatChecker(){
		
	}
	
	public static boolean check(Class<?> clz){
		return check(clz.getName().replace('.', '/') + ".class");
	}

	public static boolean check(String fileName) {
		
		boolean result = true;
		
		try {
			Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(fileName);
			
			Set<String> files = new HashSet<>();
			
			while(urls.hasMoreElements()){
				URL url = urls.nextElement();
				if (null != url){
					Logger.debug("url:{}",url);
					String file = url.getFile();
					if (!StringUtils.isEmpty(file)){
						files.add(file);
					}
				}
			}
			
			if (files.size() > 1){
				Logger.error("{} duplicate {} found!",files.size(),fileName);
				result = false;
			}
		} catch (IOException e){
			Logger.trace("IO error",e);
		}
		
		return result;
	}
}
