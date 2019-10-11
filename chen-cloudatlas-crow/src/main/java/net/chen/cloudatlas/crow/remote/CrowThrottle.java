package net.chen.cloudatlas.crow.remote;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.ThrottleType;
import net.chen.cloudatlas.crow.common.thread.NamedThreadFactory;
import net.chen.cloudatlas.crow.config.CrowServerContext;
import net.chen.cloudatlas.crow.config.ServiceConfig;
import net.chen.cloudatlas.crow.config.utils.ServiceConfigQueue;

/**
 * 访问流量，频率控制<br>
 * @author chenn
 *
 */
public class CrowThrottle {

	private static volatile CrowThrottle _instance;
	
	/**
	 * 全局控制
	 */
	private final AtomicLong tokenBucket = new AtomicLong();
	
	private final ConcurrentMap<String, AtomicLong> serverTokenBucketMap = new ConcurrentHashMap<>();
	
	private final Map<String, ThrottleType> serviceThrottleTypeConfigMap = new ConcurrentHashMap<>();
	
	private final Map<String, Long> serviceThrottleValueConfigMap = new ConcurrentHashMap<>();
	
	private final List<ServiceConfig> serviceConfigList = CrowServerContext.getServiceConfigList();
	
	private final Queue<ServiceConfig> crowConfigQueue = ServiceConfigQueue.getInstance();
	
	private ScheduledExecutorService svc;
	
	private static final int fillBucketPeriod = Constants.DEFAULT_THROTTLE_PERIOD;
	
	private CrowThrottle(){
		
		init(serviceConfigList);
		
		if (CrowServerContext.isThrottleOpen()){
			//如果没有开启流量控制，则不启动该线程
			svc = Executors.newScheduledThreadPool(
					2,
					new NamedThreadFactory("crow-trafficControl-daemon",true));
			svc.scheduleAtFixedRate(
					new Runnable(){

						@Override
						public void run() {
							produceToken();
						}
						
					}, 0, fillBucketPeriod, TimeUnit.MICROSECONDS);
		}
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
			bucket = serverTokenBucketMap.get(serviceKey);
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
	
	private void fillTheBucket(){
		
		try {
			if (!crowConfigQueue.isEmpty()){
				ServiceConfig serviceConfig = crowConfigQueue.poll();
				String serviceKey = serviceConfig.getServiceKey();
				serviceThrottleTypeConfigMap.put(serviceKey, serviceConfig.getThrottleType());
				serviceThrottleValueConfigMap.put(serviceKey, serviceConfig.getThrottleValue());
			}
			for (Map.Entry<String, ThrottleType> one : serviceThrottleTypeConfigMap.entrySet()){
				String serviceKey = one.getKey().toString();
				String serviceValue = one.getValue().toString();
				long serviceThrottle = serviceThrottleValueConfigMap.get(serviceKey);
				// qps(每秒处理完成请求次数)转换为q per fillBucketPeriod
				serviceThrottle = (long)Math.ceil(serviceThrottle/(1000.00/fillBucketPeriod));
				if (serviceValue != null && serviceThrottle > 0){
					AtomicLong throttleValue = serverTokenBucketMap.putIfAbsent(serviceKey, new AtomicLong(serviceThrottle));
					if (throttleValue != null){
						throttleValue.set(serviceThrottle);
					}
				}
			}
		} catch (Exception e){
			Logger.error("throttle value setting error");
		}
	}
	
	private void init(List<ServiceConfig> sConfigList){
		
		try {
			
			for (ServiceConfig sConfig : sConfigList){
				String serviceKey = sConfig.getServiceKey();
				ThrottleType type = sConfig.getThrottleType();
				Long value = sConfig.getThrottleValue();
				if (null == type || null == value){
					serviceThrottleTypeConfigMap.put(serviceKey, null);
					serviceThrottleValueConfigMap.put(serviceKey, null);
				} else {
					serviceThrottleTypeConfigMap.put(serviceKey, type);
					serviceThrottleValueConfigMap.put(serviceKey, value);
				}
			}
		} catch (Exception e){
			Logger.error("throttle value init error");
		}
		
		this.fillTheBucket();
	}
	
	/**
	 * 生成token
	 */
	private void produceToken(){
		this.fillTheBucket();
	}
}
