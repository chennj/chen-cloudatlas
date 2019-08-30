package net.chen.cloudatlas.crow.client;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.client.impl.ServiceControllerImpl;
import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.DcType;
import net.chen.cloudatlas.crow.common.KeyUtil;
import net.chen.cloudatlas.crow.config.CrowClientContext;
import net.chen.cloudatlas.crow.config.CrowConfig;
import net.chen.cloudatlas.crow.config.ReferenceConfig;
import net.chen.cloudatlas.crow.rpc.protocol.ReferenceGet;

/**
 * 注册服务<br>
 * <b><font color=red>
 * 未完成
 * </font></b>
 * @author chenn
 *
 */
public class ServiceRegistry {

	private ServiceRegistry(){
		
	}
	
	private static ConcurrentMap<String, CountDownLatch> latchMap = new ConcurrentHashMap<>();
	private static ConcurrentMap<String, ServiceController> controllerMap = new ConcurrentHashMap<>();
	private static ConcurrentMap<String, ReferenceGet<?>> referenceGetMap = new ConcurrentHashMap<>();
	private static volatile boolean initialized;
	
	public static void init(CrowConfig config){
		
		for (ReferenceConfig<?> rc : config.getReferenceConfigList()){
			
			String key = KeyUtil.getServiceKey(rc.getServiceId(), rc.getServiceVersion());
			latchMap.putIfAbsent(key, new CountDownLatch(1));
		}
		
		initialized = true;
		
		Logger.debug("ServiceRegistry latchMap initialized.");
	}

	public static boolean isInitialized() {
		return initialized;
	}

	/**
	 * for binary service
	 * @param serviceId
	 * @return
	 */
	public static synchronized ServiceController getService(String serviceId){
		return getService(serviceId, Constants.DEFAULT_SERVICE_VERSION);
	}
	
	public static synchronized ServiceController getService(String serviceId, String serviceVersion){
		return getService(serviceId, serviceVersion, null);
	}
	
	public static synchronized ServiceController getService(String serviceId, String serviceVersion, DcType dc) {
		
		checkDcType(dc);
		
		String key = null;
		key = KeyUtil.getServiceKey(serviceId, serviceVersion);
		ReferenceConfig rc = CrowClientContext.getReferenceConfig(serviceId, serviceVersion);
		if (rc.getProtocol().getCodec() != null 
				|| (rc.getURLs().size() == 0 && rc.getURLs().trim().isEmpty())){
			latchAwait(key);
		}
		
		ServiceController result = controllerMap.get(key);
		if (null == result){
			controllerMap.putIfAbsent(key, new ServiceControllerImpl(serviceId, serviceVersion));
			result = controllerMap.get(key);
		}
		return result;
	}

	public static void checkDcType(DcType dc) {
		if (dc == DcType.ALL){
			throw new RuntimeException("the default target dc is all."
					+ "when using this getService method,"
					+ "you should set only one dc one time");
		}
	}

	public static void countDownAll() {
		
		for (CountDownLatch latch : latchMap.values()){
			latch.countDown();
		}
	}
}
