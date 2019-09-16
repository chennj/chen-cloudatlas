package net.chen.cloudatlas.crow.config.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.util.StringUtils;

import net.chen.cloudatlas.crow.config.ReferenceConfig;

/**
 * @author chenn
 *
 */
public class ConfigUtil {

	public static final String FILE_FOLDER = "appCfg";

	public static Set<URL> getAllFilesFromClasspath(String[] fileNames){
		
		Set<URL> result = new HashSet<>();
		for (String s : fileNames){
			result.addAll(getAllFilesFromClasspath(s));
		}
		
		return result;
	}
	
	/**
	 * 从$CLASSPATH/appCfg与$CLASSPATH/中找file
	 * @param fileName
	 * @return
	 */
	public static Set<URL> getAllFilesFromClasspath(String fileName) {
		
		Set<URL> result = new HashSet<>();
		// 1st search from $CLASSPATH/appCfg/
		Set<URL> set1 = getFilesFromClasspath(FILE_FOLDER+File.separator+fileName);
		// 2st search from $CLASSPATH/
		Set<URL> set2 = getFilesFromClasspath(fileName);
		
		result.addAll(set1);
		result.addAll(set2);
		
		return result;
	}
	
	public static Set<URL> getFilesFromClasspath(String fileName){
		
		Enumeration<URL> urls = null;
		try {
			urls = Thread.currentThread().getContextClassLoader().getResources(fileName);
		} catch (IOException e){
			throw new RuntimeException(e);
		}
		
		Set<URL> result = new HashSet<>();
		
		while(urls.hasMoreElements()){
			URL url = urls.nextElement();
			if (null != url){
				result.add(url);
			}
		}
		
		return result;
	}

	/**
	 * 仅仅获取非rpc的reference列表
	 * @param referenceConfigs
	 * @return
	 */
	public static List<net.chen.cloudatlas.crow.common.URL> getBinaryReferenceUrls(
			List<ReferenceConfig> referenceConfigs) {
		
		List<net.chen.cloudatlas.crow.common.URL> result = new ArrayList<>();
		for (ReferenceConfig one : referenceConfigs){
			if (StringUtils.isEmpty(one.getInterfaceClass())){
				result.addAll(one.getURLs());
			}
		}
		return result;
	}
	
	
}
