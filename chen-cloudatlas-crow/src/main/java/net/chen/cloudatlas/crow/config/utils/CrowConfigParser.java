package net.chen.cloudatlas.crow.config.utils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.tinylog.Logger;
import org.xml.sax.SAXException;

import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.exception.ConfigInvalidException;
import net.chen.cloudatlas.crow.common.utils.PropertyUtil;
import net.chen.cloudatlas.crow.config.ApplicationConfig;
import net.chen.cloudatlas.crow.config.CrowConfig;
import net.chen.cloudatlas.crow.config.MonitorConfig;
import net.chen.cloudatlas.crow.config.ProtocolConfig;
import net.chen.cloudatlas.crow.config.ReferenceConfig;
import net.chen.cloudatlas.crow.config.RegistryConfig;
import net.chen.cloudatlas.crow.config.ServiceConfig;

/**
 * xml parser using jaxb
 * @author chenn
 *
 */
public class CrowConfigParser {

	private static List<URL> springConfigStore = new LinkedList<URL>();
	
	private CrowConfigParser(){
		
	}

	/**
	 * 解析 
	 * @return
	 * @throws ConfigInvalidException
	 */
	public static CrowConfig parse() throws ConfigInvalidException{
		
		// system property 优先
		String names = System.getProperty(Constants.CROW_CONFIG_FILE_KEY, Constants.DEFAULT_CROW_CONFIG_FILE_NAME);
		
		// 支持逗号分隔的多个文件名
		String[] fileNames = names.split(Constants.COMMA_SEPARATOR);
		
		// 搜索classpath中（包括appCfg目录）所有crow的配置文件并合并
		Set<URL> urls = ConfigUtil.getAllFilesFromClasspath(fileNames);
		String namesStr = new String();
		for (URL url : urls){
			namesStr = namesStr + url.getPath() + " ";
		}
		
		if (urls.size() == 0){
			throw new RuntimeException("no crow config file found in classpath!");
		}
		
		Logger.info("total [{}] config file found:[{}]",urls.size(),namesStr);
		
		return parseFiles(namesStr,urls);
	}
	
	/**
	 * 解析多个配置文件
	 * @param namesStr
	 * @param urls
	 * @return
	 * @throws ConfigInvalidException
	 */
	public static CrowConfig parseFiles(String namesStr, Collection<URL> urls) throws ConfigInvalidException{
		
		List<CrowConfig> list = new ArrayList<>();
		
		for (URL url : urls){
			CrowConfig config = parse(url);
			list.add(config);
		}
		
		return merge(namesStr, list);
	}
	
	/**
	 * 解析文件
	 * @param fileName
	 * @return
	 * @throws ConfigInvalidException
	 */
	public static CrowConfig parse(String fileName) throws ConfigInvalidException{
		return parse(PropertyUtil.getInstance().getConfigResourceUrl(fileName));
	}
	
	public static CrowConfig parse(URL url) throws ConfigInvalidException{
		
		// 先到$CLASSPATH/appCfg/目录下找，再到$CLASSPATH/
		Logger.info("parsing config file [{}]",url.toString());
		
		validate(url);
		
		CrowConfig result = null;
		try {
			JAXBContext context = JAXBContext.newInstance(CrowConfig.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			result = (CrowConfig)unmarshaller.unmarshal(url);
		} catch (JAXBException e){
			throw new IllegalStateException("unmarshal error.",e);
		}
		
		result.setDefaultValue();
		result.check();
		
		return result;
	}
	
	/**
	 * xsd校验
	 * @param url
	 */
	private static void validate(URL url){
		
		try {
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
			String xsdFile = System.getProperty(Constants.CROW_XSD_FILE_KEY, Constants.DEFAULT_SCHEMA_NAME);
			Logger.trace("xsdFile:"+xsdFile);
			URL fullPathXsdFile = Thread.currentThread().getContextClassLoader().getResource(xsdFile);
			Logger.trace("using xml schemal {}",fullPathXsdFile);
			Schema schema = sf.newSchema(fullPathXsdFile);
			Validator validator = schema.newValidator();
			validator.validate(new StreamSource(url.openStream()));
			Logger.info("[validate ok] config file {}",url.toString());
		} catch (SAXException e){
			throw new IllegalStateException("[validate error] config file ["+url.toString()+"]",e);
		} catch (IOException e){
			throw new RuntimeException("validate file error",e);
		}
	}
	
	public static CrowConfig merge(String namesStr, List<CrowConfig> configs) throws ConfigInvalidException{
		
		if (1 == configs.size()){
			return configs.get(0);
		}
		
		CrowConfig result = new CrowConfig();
		
		List<ApplicationConfig> appList = new ArrayList<>();
		List<ProtocolConfig> protocolConfigList = new ArrayList<>();
		List<ServiceConfig> serviceConfigList = new ArrayList<>();
		List<ReferenceConfig> referenceConfigList = new ArrayList<>();
		List<RegistryConfig> registryConfigList = new ArrayList<>();
		List<MonitorConfig> monitorConfigList = new ArrayList<>();
		
		for (CrowConfig one : configs){
			// 忽略name为unknown的application，因为unknown是<application>没有设置时的默认值
			if (one.getApplicationConfig() != null && !Constants.UNKNOWN.equals(one.getApplicationConfig().getName())){
				appList.add(one.getApplicationConfig());
			}
			
			// 添加了对于registry和monitor的配置
			if (one.getRegistryConfig() != null){
				registryConfigList.add(one.getRegistryConfig());
			}
			
			if (one.getMonitorConfig() != null){
				monitorConfigList.add(one.getMonitorConfig());
			}
			
			if (one.getProtocolConfigList() != null && one.getProtocolConfigList().size() > 0){
				protocolConfigList.addAll(one.getProtocolConfigList());
			}
			
			if (one.getServiceConfigList() != null && one.getServiceConfigList().size() > 0){
				serviceConfigList.addAll(one.getServiceConfigList());
			}
			
			if (one.getReferenceConfigList() != null && one.getReferenceConfigList().size() > 0){
				referenceConfigList.addAll(one.getReferenceConfigList());
			}
		}
		
		// application config 必须有且只有一个，因为是全局信息
		if (appList.size() == 0){
			throw new ConfigInvalidException("no <application> xml tag found in "+namesStr);
		}
		if (appList.size() > 1){
			throw new ConfigInvalidException("more than one <application> found when merging "+namesStr+""
					+ ", make sure there is only one <application> in multiple config files.");
		}
		
		// registry的配置可以没有，有多个的情况下集群的地址必须一致
		if (registryConfigList.size() > 1){
			
			for (int i=1; i<registryConfigList.size(); i++){
				if (!registryConfigList.get(0).getAddress().equals(registryConfigList.get(i).getAddress())){
					throw new ConfigInvalidException("more than one <registry> found when merging"
							+ namesStr + ", but at least one address between these <registry> is not equal to others,"
							+ " check if your project have SSO included that will introduce <registry> tag, if so, remove"
							+ " you tag and set 'crow_registry=zkIp:zkPort' in your properties file.");
				}
			}
		}
		
		// monitor的配置可以没有，有的话只能一个
		if (monitorConfigList != null && monitorConfigList.size() > 1){
			throw new ConfigInvalidException("more than one <monitor> found when merging"
					+ namesStr + ", make sure there's only one <monitor> in multiple config files.");
		}
		
		if (registryConfigList.size() != 0){
			result.setRegistryConfig(registryConfigList.get(0));
		}
		
		if (monitorConfigList.size() == 1){
			result.setMonitorConfig(monitorConfigList.get(0));
		}
		
		result.setApplicationConfig(appList.get(0));
		result.setProtocolConfigList(protocolConfigList);
		result.setServiceConfigList(serviceConfigList);
		result.setReferenceConfigList(referenceConfigList);
		
		result.setDefaultValue();
		
		//合并后的CrowConfig再check一下
		result.check();
		
		return result;
	}

	public static List<URL> getSpringConfigStore() {
		return springConfigStore;
	}
	
}
