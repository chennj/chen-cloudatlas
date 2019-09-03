package net.chen.cloudatlas.crow.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.client.impl.ServiceControllerImpl;
import net.chen.cloudatlas.crow.cluster.invoker.BroadcastInvoker;
import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.DcType;
import net.chen.cloudatlas.crow.common.KeyUtil;
import net.chen.cloudatlas.crow.config.CrowClientContext;
import net.chen.cloudatlas.crow.config.CrowConfig;
import net.chen.cloudatlas.crow.config.ReferenceConfig;
import net.chen.cloudatlas.crow.rpc.protocol.ReferenceGet;

/**
 * 注册服务<br>
 * 
 * @author chenn
 *
 */
public class ServiceRegistry {

	private ServiceRegistry(){
		
	}
	
	private static ConcurrentMap<String, CountDownLatch> latchMap = new ConcurrentHashMap<>();
	private static ConcurrentMap<String, ServiceController> controllerMap = new ConcurrentHashMap<>();
	private static ConcurrentMap<String, ReferenceGet> referenceGetMap = new ConcurrentHashMap<>();
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
	
	public static synchronized ServiceController getService(String serviceId, DcType dc){
		return getService(serviceId, Constants.DEFAULT_SERVICE_VERSION, dc);
	}
	
	public static synchronized ServiceController getService(String serviceId, String serviceVersion, DcType dc) {
		
		checkDcType(dc);
		
		String key = null;
		key = KeyUtil.getServiceKey(serviceId, serviceVersion);
		ReferenceConfig rc = CrowClientContext.getReferenceConfig(serviceId, serviceVersion);
		if (rc.getProtocol().getCodec() != null 
				|| (rc.getURLs().size() == 0 && rc.getUrls().trim().isEmpty())){
			latchAwait(key);
		}
		
		ServiceController result = controllerMap.get(key);
		if (null == result){
			controllerMap.putIfAbsent(key, new ServiceControllerImpl(serviceId, serviceVersion));
			result = controllerMap.get(key);
		}
		return result;
	}

	public static synchronized <T> T getService(Class<T> interfaceClass){
		return getService(interfaceClass, Constants.DEFAULT_SERVICE_VERSION, null);
	}
	
	public static synchronized <T> T getService(Class<T> interfaceClass, String serviceVersion){
		return getService(interfaceClass, serviceVersion, null);
	}
	
	public static synchronized <T> T getService(Class<T> interfaceClass, DcType dc){
		return getService(interfaceClass, Constants.DEFAULT_SERVICE_VERSION, dc);
	}
	
	public static synchronized <T> T getService(Class<T> interfaceClass, String serviceVersion, DcType dc){
		
		checkDcType(dc);
		
		String interfaceName = interfaceClass.getName();
		String key = KeyUtil.getServiceKey(interfaceName, serviceVersion);
		
		ReferenceGet rc = referenceGetMap.get(key);
		if (null == rc){
			ReferenceConfig rConfig = CrowClientContext.getReferenceConfigByInterface(interfaceName, serviceVersion);
			referenceGetMap.putIfAbsent(key, new ReferenceGet(rConfig));
			rc = referenceGetMap.get(key);
		}
		
		if (rc.getReferenceConfig().getProtocol().getCodec() == null 
				|| (rc.getReferenceConfig().getURLs().size() == 0 
				&& rc.getReferenceConfig().getUrls().trim().isEmpty())
				|| !ReferenceGet.checkURLs(rc.getReferenceConfig().getURLs(), rc.getReferenceConfig())){
			
			latchAwait(key);
		}
		
		T service = (T) rc.get(dc);
		if (null == service){
			Logger.error(key + " was not found in the dc you select");
		}
		
		return service;
	}
	
	public static void latchAwait(String key){
		
		CountDownLatch latch = latchMap.get(key);
		if (null != latch){
			
			try {
				latch.await(Constants.REGISTRY_CONNECTION_LATCH_TIMEOUT, TimeUnit.MICROSECONDS);
			} catch (InterruptedException e){
				Logger.error("no response from registry, please check if registry is ok!",e);
			}
		}
	}
	
	public static void checkDcType(DcType dc) {
		if (dc == DcType.ALL){
			throw new RuntimeException("the default target dc is all."
					+ "when using this getService method,"
					+ "you should set only one dc one time");
		}
	}
	
	public static ConcurrentMap<String, ServiceController> getControllerMap(){
		return controllerMap;
	}
	
	public static ConcurrentMap<String, ReferenceGet> getReferenceGetMap(){
		return referenceGetMap;
	}
	
	public static void countDown(String serviceId){
		
		CountDownLatch latch = latchMap.get(serviceId);
		if (null != latch){
			latch.countDown();
		}
	}

	public static void countDownAll() {
		
		for (CountDownLatch latch : latchMap.values()){
			latch.countDown();
		}
	}
	
	public static Map<String, Object> getBroadCastResult(){
		return BroadcastInvoker.getResultMap();
	}
}
