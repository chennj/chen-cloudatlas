package net.chen.cloudatlas.crow.config;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.common.KeyUtil;
import net.chen.cloudatlas.crow.common.utils.ValidatorUtil;

/**
 * 框架全局信息<br>
 * 未完成
 * @author chenn
 *
 */
public class CrowClientContext {

	private CrowClientContext(){}
	
	private static CrowConfig config;
	
	private static List<ReferenceConfig> referenceConfigList;
	
	private static Map<String, ReferenceConfig> referenceConfigMap;
	
	private static Map<String, ReferenceConfig> referenceConfigRpcMap;
	
	private static Map<String, String> referencePasswordMap;
	
	private static List<ServiceConfig> serviceConfigList;
	
	private static Map<String, ServiceConfig> serviceConfigMap;
	
	private static Map<String, ServiceConfig> serviceConfigRpcMap;
	
	private static String applicationName;
	
	private static Map<String, Boolean> isPasswordInitByRefMap;
	
	/**
	 * <p><string>未完成</string></p>
	 * 判断ipAndPort是否是远端
	 * @param ipAndPort
	 * @return
	 */
	public static boolean isRemoteEnd(String ipAndPort){
		
		if (!ValidatorUtil.validateIpAndPort(ipAndPort)){
			throw new IllegalArgumentException("param ipAndPort pattern error!");
		}
		
		boolean isRemote = false;
		
		return true;
	}

	public static String getApplicationName() {
		
		if (applicationName == null){
			applicationName = config.getApplicationConfig().getName();
		}
		return applicationName;
	}
	
	public static void setApplicationName(String applicationName){
		
		config.getApplicationConfig().setName(applicationName);
		applicationName = config.getApplicationConfig().getName();
	}

	public static synchronized void init(CrowConfig origin) {
		
		Logger.debug("init CrowClientContext");
		
		if (null == origin){
			throw new IllegalArgumentException("CrowConfig param CAN NOT be null!");
		}
		
		config = origin;
		
		referenceConfigList = config.getReferenceConfigList();
		referenceConfigMap = new ConcurrentHashMap<>();
		referenceConfigRpcMap = new ConcurrentHashMap<>();
		referencePasswordMap = new ConcurrentHashMap<>();
		isPasswordInitByRefMap = new ConcurrentHashMap<>();
		
		for (ReferenceConfig rc : referenceConfigList){
			
			if (rc.getInterfaceClass() != null){
				referenceConfigMap.put(KeyUtil.getServiceKey(rc.getInterfaceClass(), rc.getServiceVersion()), rc);
				referenceConfigRpcMap.put(KeyUtil.getServiceKey(rc.getInterfaceClass(), rc.getServiceVersion()), rc);
			} else {
				referenceConfigMap.put(KeyUtil.getServiceKey(rc.getServiceId(), rc.getServiceVersion()), rc);
			}
							
			if (rc.getPassword() == null || rc.getPassword().equals("")){
				isPasswordInitByRefMap.put(KeyUtil.getServiceKey(rc.getServiceId(), rc.getServiceVersion()), false);
			} else {
				isPasswordInitByRefMap.put(KeyUtil.getServiceKey(rc.getServiceId(), rc.getServiceVersion()), true);
			}
			
			referencePasswordMap.put(KeyUtil.getServiceKey(rc.getServiceId(), rc.getServiceVersion()), rc.getPassword());
		}
		
		//缓存 serviceConfig
		serviceConfigList = config.getServiceConfigList();
		serviceConfigMap = new ConcurrentHashMap<>();
		serviceConfigRpcMap = new ConcurrentHashMap<>();
		
		for(ServiceConfig sc : serviceConfigList){
			
			if (sc.getInterface() != null){
				serviceConfigMap.put(KeyUtil.getServiceKey(sc.getInterfaceClass(), sc.getInterfaceClass()), sc);
				serviceConfigRpcMap.put(KeyUtil.getServiceKey(sc.getInterfaceClass(), sc.getServiceVersion()), sc);
			} else {
				serviceConfigMap.put(KeyUtil.getServiceKey(sc.getServiceId(), sc.getServiceVersion()), sc);
			}
		}
	}

	public static CrowConfig getConfig() {
		return config;
	}
}
