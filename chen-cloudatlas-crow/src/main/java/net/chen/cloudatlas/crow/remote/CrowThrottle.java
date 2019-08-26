package net.chen.cloudatlas.crow.remote;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import net.chen.cloudatlas.crow.common.ThrottleType;

/**
 * 访问流量，频率控制<br>
 * <b><font color=red>
 * 有待完成
 * </font></b>
 * @author chenn
 *
 */
public class CrowThrottle {

	private static volatile CrowThrottle _instance;
	
	private final Map<String, ThrottleType> serviceThrottleTypeConfigMap = new ConcurrentHashMap<>();
	
	private final Map<String, Long> serviceThrottleValueConfigMap = new ConcurrentHashMap<>();
	
	private final Map<String, AtomicLong> serviceTokenBucketMap = new ConcurrentHashMap<>();
	
	/**
	 * 全局控制
	 */
	private final AtomicLong tokenBucket = new AtomicLong();
	
	private CrowThrottle(){
		
	}
	
	public static CrowThrottle getInstance(){
		
		if (null == _instance){
			synchronized(CrowThrottle.class){
				if (null == _instance){
					_instance = new CrowThrottle();
				}
			}
		}
		
		return _instance;
	}
	
	public synchronized boolean getToken(int msgLength, String serviceKey){
		
		ThrottleType throttleType = serviceThrottleTypeConfigMap.get(serviceKey);
		
		if (null == throttleType){
			//不进行控制
			return true;
		}
		
		Long throttleValue = serviceThrottleValueConfigMap.get(serviceKey);
		
		if (throttleValue <= 0){
			return true;
		}
		
		AtomicLong bucket;
		if (serviceKey == null){
			//暂时不考虑全局控制
			bucket = tokenBucket;
		} else {
			bucket = serviceTokenBucketMap.get(serviceKey);
		}
		
		if (bucket == null){
			return true;
		}
		
		if (throttleType.toString().equals(ThrottleType.QPS.toString())){
			//处理访问频率
			//减少令牌桶中令牌数量，判断令牌桶中数量是否大于零
			return bucket.getAndDecrement() > 0;
		} else if (throttleType.toString().equals(ThrottleType.FLOW.toString())){
			//处理访问流量
			Long delta = 0L - msgLength;
			return bucket.getAndAdd(delta) > 0;
		} else {
			return true;
		}
	}
}
