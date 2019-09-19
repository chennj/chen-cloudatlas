package net.chen.cloudatlas.crow.config.spring.boot;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.bootstrap.Bootstrap;
import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.exception.ConfigInvalidException;
import net.chen.cloudatlas.crow.config.CrowConfig;
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

	@Override
	protected void parseConfigFile() {
		
		// 需要判断classpath中是否有crow.xml的配置文件，如果没有的话，直接以spring读取的配置为准；
		// 如有需要合并
		try {
			if (this.isMultiFiles){
				List<CrowConfig> toMergeConfig = new ArrayList<>();
				toMergeConfig.add(CrowConfigParser.parse());
				toMergeConfig.add(SpringBootstrapDelegate.getCrowConfig());
				config = CrowConfigParser.merge("spring && crow*.xml", toMergeConfig);
			} else {
				config = SpringBootstrapDelegate.getCrowConfig();
			}
			config.setDefaultValue();
			config.check();
		} catch (ConfigInvalidException e){
			Logger.error(e);
			throw new RuntimeException(System.getProperty(Constants.CROW_CONFIG_FILE_KEY,Constants.DEFAULT_CROW_CONFIG_FILE_NAME),e);
		}
	}
	
	
	
}
