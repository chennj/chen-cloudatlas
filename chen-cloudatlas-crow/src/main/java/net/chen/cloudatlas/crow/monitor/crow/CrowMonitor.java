package net.chen.cloudatlas.crow.monitor.crow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.common.thread.NamedThreadFactory;
import net.chen.cloudatlas.crow.monitor.api.Monitor;
import net.chen.cloudatlas.crow.monitor.api.MonitorService;
import net.chen.cloudatlas.crow.rpc.Invoker;

public class CrowMonitor implements Monitor{

	private static final int LENGTH = 7;
	
	private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(
			1,
			new NamedThreadFactory("crow-monitor-sendTimer", true));
	
	private final ScheduledFuture<?> sendFuture;
	
	private final Invoker<MonitorService> monitorInvoker;
	
	private final MonitorService monitorService;
	
	private final long monitorInterval;
	
	private final ConcurrentMap<Statistics, AtomicReference<long[]>> statisticsMap =
			new ConcurrentHashMap<Statistics, AtomicReference<long[]>>();
	
	public CrowMonitor(
			Invoker<MonitorService> monitorInvoker,
			MonitorService monitorService,
			long monitorInterval){
		
		this.monitorInvoker = monitorInvoker;
		this.monitorService = monitorService;
		this.monitorInterval = monitorInterval;
		
		Logger.debug("monitorInterval:"+this.monitorInterval);
		
		//启动统计信息收集定时器
		sendFuture = this.scheduledExecutorService.scheduleWithFixedDelay(
				new Runnable(){

					@Override
					public void run() {
						try {
							send();
						} catch (Exception e){
							Logger.error("unexpected error occur on send statistic", e);
						}
					}
					
				}, 
				this.monitorInterval, 
				this.monitorInterval,
				TimeUnit.MILLISECONDS);
	}
		
	/**
	 * 收集信息
	 */
	@Override
	public void collect(URL url) {
		
		int success = url.getParameter(Constants.SUCC_COUNT, 0);
		int fail = url.getParameter(Constants.FAIL_COUNT, 0);
		int rt = url.getParameter(Constants.TOTAL_RT, 0);
		int concurrent = url.getParameter(Constants.CONCURRENT, 0);
		
		Statistics statistics = new Statistics(url);
		AtomicReference<long[]> reference = statisticsMap.get(statistics);
		if (null == reference){
			this.statisticsMap.putIfAbsent(statistics, new AtomicReference<long[]>());
			reference = this.statisticsMap.get(statistics);
		}
		
		long[] current;
		long[] update = new long[LENGTH];
		do {
			current = reference.get();
			if (null == current){
				update[0] = success;
				update[1] = fail;
				update[2] = rt;
				update[3] = concurrent;
				update[4] = rt;
				update[5] = rt;
				update[6] = concurrent;
			} else {
				update[0] = current[0] + success;
				update[1] = current[1] + fail;
				update[2] = current[2] + rt;
				update[3] = current[3] + concurrent;
				update[4] = current[4] + rt;
				update[5] = current[5] + rt;
				update[6] = current[6] + concurrent;
			}
		} while (!reference.compareAndSet(current, update));
	}

	/**
	 * 从statisticsMap中取出统计信息，同意构建为URL形式，
	 * 并通过rpc服务monitorService发送给monitor server
	 */
	public void send(){
		
		String timestamp = String.valueOf(System.currentTimeMillis());
		List<URL> statisticsList = new ArrayList<>();
		for (Map.Entry<Statistics, AtomicReference<long[]>> entry : this.statisticsMap.entrySet()){
			// 获取已统计数据
			Statistics statistics = entry.getKey();
			AtomicReference<long[]> reference = entry.getValue();
			long[] numbers = reference.get();
			long success = numbers[0];
			long fail = numbers[1];
			long totalRt = numbers[2];
			long concurrent = numbers[3];
			long peakRt = numbers[4];
			long lowRt = numbers[5];
			long maxConcurrent = numbers[6];
			
			if (success == 0 && fail == 0 && totalRt == 0 && concurrent == 0 && peakRt == 0 && lowRt == 0 && maxConcurrent == 0){
				continue;
			}
			
			// 发送汇总信息
			URL url = statistics.getUrl().addParameters(
					Constants.TIMESTAMP, timestamp,
					Constants.SUCC_COUNT,String.valueOf(success),
					Constants.FAIL_COUNT,String.valueOf(fail),
					Constants.TOTAL_RT, String.valueOf(totalRt),
					Constants.CONCURRENT, String.valueOf(concurrent),
					Constants.PEAK_RT, String.valueOf(peakRt),
					Constants.LOW_RT, String.valueOf(lowRt),
					Constants.MAX_CONCURRENT, String.valueOf(maxConcurrent));
			
			statisticsList.add(url);
			Logger.debug("collecting:"+url);
			
			// 减掉已统计数据
			long[] current;
			long[] update = new long[LENGTH];
			do {
				current = reference.get();
				if (null == current){
					update[0] = 0;
					update[1] = 0;
					update[2] = 0;
					update[3] = 0;
					update[4] = 0;
					update[5] = 0;
					update[6] = 0;
				} else {
					update[0] = current[0] - success;
					update[1] = current[1] - fail;
					update[2] = current[2] - totalRt;
					update[3] = current[3] - concurrent;
				}
			} while (!reference.compareAndSet(current, update));
		}
		
		try {
			// 每个url对象大小为670byte左右，以方法为粒度进行统计发送
			// 若有1500个方法，一次统计调用发送消息大小则为1M左右，
			// 可以满足crow的单次调用4M上限的限制
			if (statisticsList.size() > 0){
				this.monitorService.collect(statisticsList);
				Logger.debug("collecting "+statisticsList.size()+" statistics at "+timestamp);
			}
		} catch (Exception e){
			Logger.error("unexpected error occur when send statisticsList at "
					+ timestamp + ", cause: "+e.getMessage(),e);
		}
	}
	
	public URL getUrl(){
		return this.monitorInvoker.getUrl();
	}
	
	public boolean isAvailable(){
		return this.monitorInvoker.isAvailable();
	}
	
	public void destroy(){
		try {
			sendFuture.cancel(true);
		} catch (Exception e){
			Logger.error("unexpected error occur at cancel sender timer, cause:"+e.getMessage(),e);
		}
		this.monitorInvoker.destroy();
	}
}
