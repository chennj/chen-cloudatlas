package net.chen.cloudatlas.crow.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.chen.cloudatlas.crow.common.KeyUtil;
import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.common.utils.ValidatorUtil;

/**
 * 存放框架全局信息<br>
 * @author chenn
 *
 */
public class CrowServerContext {

	private CrowServerContext(){}
	
	private static CrowConfig config;
	private static String applicationName;
	
	private static List<ServiceConfig> serviceConfigList;
	private static Map<String, ServiceConfig> serviceConfigMap;
	private static Map<String, ServiceConfig> serviceConfigRpcMap;
	
	private static Map<String, String> serviceThrottleMap;
	private static Map<String, String> servicePasswordMap;
	
	private static List<ReferenceConfig> referenceConfigList;
	private static Map<String, ReferenceConfig> referenceConfigMap;
	
	private static List<ProtocolConfig> protocolConfigList;
	private static Map<ProtocolConfig, List<ServiceConfig>> protocolToServiceMap;
	
	/**
	 * 如果用户完全没有配置流量控制，则直接在后面的判断中跳过，<br>
	 * 不用在之后判断各个服务有木有单独开启流量控制，提高效率
	 */
	private static boolean isThrottleOpen = false;
	
	public static CrowConfig getConfig(){
		return config;
	}

	

	public static String getApplicationName() {
		if (null == applicationName){
			if (null ==  config || config.getApplicationConfig() == null){
				return "";
			}
			applicationName = config.getApplicationConfig().getName();
		}
		return applicationName;
	}

	/**
	 * <b><font color=red>测试用</font></b>
	 * @param applicationName
	 */
	public static void setApplicationName(String applicationName) {
		CrowServerContext.applicationName = applicationName;
	}

	public static boolean isThrottleOpen() {
		return isThrottleOpen;
	}

	public static void setThrottleOpen(boolean isThrottleOpen) {
		CrowServerContext.isThrottleOpen = isThrottleOpen;
	}

	public static Map<String, String> getServiceThrottleMap() {
		return serviceThrottleMap;
	}


	public static ServiceConfig getServiceConfig(String serviceId, String serviceVersion) {

		ServiceConfig result = serviceConfigMap.get(KeyUtil.getServiceKey(serviceId, serviceVersion));
		return result;
	}

	public static synchronized void init(CrowConfig origin) {
		
		if (null == origin){
			throw new IllegalArgumentException("CrowConfig param can not be null!");
		}
		
		config = origin;
		
		/**
		 * cache referenceConfig
		 */
		referenceConfigList = config.getReferenceConfigList();
		referenceConfigMap = new ConcurrentHashMap<>();
		for (ReferenceConfig rc : referenceConfigList){
			
			referenceConfigMap.put(
					KeyUtil.getServiceKey(rc.getServiceId(), rc.getServiceVersion()), 
					rc);
		}
		
		/**
		 * cache serviceConfig
		 */
		serviceConfigList = config.getServiceConfigList();
		serviceConfigMap = new ConcurrentHashMap<>();
		serviceConfigRpcMap = new ConcurrentHashMap<>();
		serviceThrottleMap = new ConcurrentHashMap<>();
		servicePasswordMap = new ConcurrentHashMap<>();		
		for (ServiceConfig sc : serviceConfigList){
			
			serviceConfigMap.put(
					KeyUtil.getServiceKey(sc.getServiceId(), sc.getServiceVersion()), 
					sc);
			
			if (null != sc.getInterfaceClass() 
					&& !"".equals(sc.getInterfaceClass().trim())){
				serviceConfigRpcMap.put(
						KeyUtil.getServiceKey(sc.getInterfaceClass(), sc.getServiceVersion()), 
						sc);
			}
			
			if (sc.getThrottleValue() > 0){
				isThrottleOpen = true;
			}
			
			serviceThrottleMap.put(
					KeyUtil.getServiceKey(sc.getServiceId(), sc.getServiceVersion()), 
					sc.getThrottleType().toString() + ":" + sc.getThrottleValue());
			servicePasswordMap.put(
					KeyUtil.getServiceKey(sc.getServiceId(), sc.getServiceVersion()), 
					sc.getPassword());
		}
		
		/**
		 * cache protocolConfig
		 */
		protocolConfigList = config.getProtocolConfigList();
		protocolToServiceMap = new ConcurrentHashMap<>();
		for (ProtocolConfig pc : protocolConfigList){
			
			protocolToServiceMap.put(pc, new ArrayList<ServiceConfig>());
		}
		for (ServiceConfig sc1 : serviceConfigList){
			
			List<ServiceConfig> scList = protocolToServiceMap.get(sc1.getProtocol());
			scList.add(sc1);
		}
	}

	public static Map<String, ServiceConfig> getServiceConfigRpcMap() {
		
		return serviceConfigRpcMap;
	}

	public static List<ServiceConfig> getServiceListByProtocol(ProtocolConfig protocolConfig) {

		return protocolToServiceMap.get(protocolConfig);
	}

	public static ServiceConfig getServiceConfigByInterface(String interfaceCalss, String serviceVersion) {
		
		return serviceConfigRpcMap.get(
				KeyUtil.getServiceKey(interfaceCalss, serviceVersion)
				);
	}
	
	public static boolean isRemoteEnd(String ipAndPort){
		
		if (!ValidatorUtil.validateIpAndPort(ipAndPort)){
			throw new IllegalArgumentException("param ipAndPort pattern error!");
		}
		
		boolean result = false;
		
		if (null != referenceConfigList){
			
			outerTag: for (ReferenceConfig c : referenceConfigList){
				List<URL> urls = c.getURLs();
				for (URL url : urls){
					
					if (url.getHostAndPort().equals(ipAndPort)){
						result = true;
						break outerTag;
					}
				}
			}
		}
		
		return result;
	}

	public static List<ReferenceConfig> getReferenceConfigList() {
		return referenceConfigList;
	}

	public static void setReferenceConfigList(List<ReferenceConfig> referenceConfigList) {
		CrowServerContext.referenceConfigList = referenceConfigList;
	}

	public static Map<String, ReferenceConfig> getReferenceConfigMap() {
		return referenceConfigMap;
	}

	public static List<ServiceConfig> getServiceConfigList() {
		return serviceConfigList;
	}
	
	public static String getPassByService(String serviceId, String serviceVersion){
		
		if (null == servicePasswordMap){
			return null;
		}
		
		return servicePasswordMap.get(KeyUtil.getServiceKey(serviceId, serviceVersion));
	}
}
