package net.chen.cloudatlas.crow.config.spring.boot;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.Set;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.bootstrap.Bootstrap;
import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.config.utils.ConfigUtil;
import net.chen.cloudatlas.crow.config.utils.CrowConfigParser;
import net.chen.cloudatlas.crow.server.AbstractServerPayloadListener;

public class SpringBootstrap extends Bootstrap{

	private volatile boolean isMultiFiles = false;
	
	public SpringBootstrap(){}
	
	public SpringBootstrap(AbstractServerPayloadListener serverListener){
		super(serverListener);
	}

	/**
	 * crow 启动前，先加载crow.properties。为了与spring集成解析出来的crowConfig对象合并
	 */
	@Override
	public void initSystemProperties() {
		
		Set<URL> propertyUrls = ConfigUtil.getAllFilesFromClasspath(System.getProperty(
				Constants.CROW_PROPERTIES_FILE_KEY,
				Constants.DEFAULT_CROW_PROPERTIES_FILE_KEY));
		
		Properties properties = System.getProperties();
		for (URL url : propertyUrls){
			
			Properties props = new Properties();
			try {
				props.load(url.openStream());
				properties.putAll(props);
			} catch (FileNotFoundException e){
				Logger.error("FileNotFoundException",e);
			} catch (IOException e){
				Logger.error("IOException",e);
			}
		}
		
		Set<URL> configUrls = ConfigUtil.getAllFilesFromClasspath(System.getProperty(
				Constants.CROW_CONFIG_FILE_KEY,
				Constants.DEFAULT_CROW_CONFIG_FILE_NAME));
		
		for (URL url :  configUrls){
			
			if (CrowConfigParser.getSpringConfigStore().contains(url)){
				configUrls.remove(url);
			}
		}
		
		if (configUrls.size() > 0){
			this.isMultiFiles = true;//代表存在crow.xml格式的配置文件
		}
	}
	
	
}
