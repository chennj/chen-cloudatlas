package net.chen.cloudatlas.crow.client;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.common.KeyUtil;
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
	private static ConcurrentMap<String, ServiceController> controllMap = new ConcurrentHashMap<>();
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

	public static void countDownAll() {
		
		for (CountDownLatch latch : latchMap.values()){
			latch.countDown();
		}
	}
}
