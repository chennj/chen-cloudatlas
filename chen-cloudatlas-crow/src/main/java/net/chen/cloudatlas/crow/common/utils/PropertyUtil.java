package net.chen.cloudatlas.crow.common.utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.StringUtils;
import org.tinylog.Logger;

/**
 * 获取配置文件工具类
 * @author chenn
 *
 */
public class PropertyUtil {

	private static PropertyUtil instance = null;
	
	private static final String FILE_FOLDER = "appCfg";
	
	private static Map<String, Properties> configPropsMap = new ConcurrentHashMap<>();
	
	private PropertyUtil(){}
	
	public synchronized static PropertyUtil getInstance(){
		if (null == instance){
			instance = new PropertyUtil();
		}
		return instance;
	}
	
	/**
	 * 根据指定目录下的文件名称获取属性文件的内容
	 * @param dir 相对路径
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public Properties getPropertiesByFileName(String dir, String fileName) throws IOException{
		
		//根据文件名构造URL对象
		String tmpfilename;
		
		if (StringUtils.isEmpty(dir)){
			tmpfilename = dir + File.separator + fileName;
		} else {
			tmpfilename = fileName;
		}
		
		Properties props = configPropsMap.get(tmpfilename);
		if (null != props){
			return props;
		} else {
			URL url = getConfigResourceUrl(tmpfilename);
			if (null == url){
				throw new IOException("can not found property file: " + fileName);
			}
			Properties ps = new Properties();
			try {
				Logger.info(tmpfilename + " properties file found and used. URL=" + url.toExternalForm());
				ps.load(url.openStream());
			} catch (IOException e){
				throw new IOException("can not load property file:"+url.toExternalForm(),e);
			}
			configPropsMap.put(tmpfilename, ps);
			return ps;
		}
	}
	
	public Properties getPropertiesByFileName(String fileName) throws IOException{
		return getPropertiesByFileName(null, fileName);
	}
	
	/**
	 * 根据指定目录下的文件名称获取属性文件的内容中对应key的具体 value值
	 * @param dir
	 * @param fileName
	 * @param key
	 * @return
	 * @throws IOException
	 */
	public String getValueByFileNameKey(String dir, String fileName, String key) throws IOException{
		return (String)getPropertiesByFileName(dir, fileName).get(key);
	}
	
	/**
	 * 
	 * @param dir appCfg目录
	 * @param fileName
	 * @param key
	 * @return
	 * @throws IOException
	 */
	public String getValueByDefaultFileKey(String dir, String fileName, String key) throws IOException{
		return (String)getPropertiesByFileName(null,fileName).get(key);
	}
	
	public URL getConfigResourceUrl(String resource){
		
		URL url = null;
		/**
		 * 相对路径查找当前classloader的资源，
		 * 绝对路径查找绝对路径的资源。
		 */
		try {
			File configFile = new File(resource);
			url = configFile.toURI().toURL();
			url.openStream();
		} catch (MalformedURLException e){
			url = null;
		} catch (IOException e){
			url = null;
		}
		
		// 先查找appCfg下的资源
		if (null == url){
			url = Thread.currentThread().getContextClassLoader().getResource(FILE_FOLDER+File.separatorChar+resource);
		}
		
		// 再查找classpath下的资源
		if (null == url){
			url = Thread.currentThread().getContextClassLoader().getResource(resource);
		}
		
		return url;
	}
}
