package org.chen.cloudatlas.crow.remote.thread;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.chen.cloudatlas.crow.common.thread.NamedThreadFactory;

/**
 * 守护线程调度器
 * @author chenn
 *
 */
public class ScheduleService<T extends AbstractDaemon> {

	private ScheduledExecutorService suc;
	
	/**
	 * 初始化调度线程池
	 */
	public ScheduleService(){
		suc = Executors.newScheduledThreadPool(1, new NamedThreadFactory("crow-channel-fix-daemon"));
	}
	
	/**
	 * 设置定时运行守护线程，并立即开始
	 * @param daemon
	 */
	public void schedule(T daemon){
		suc.scheduleAtFixedRate(daemon, 0, daemon.getWakeupInterval(), TimeUnit.MILLISECONDS);
	}
	
	/**
	 * 设置定时运行守护线程，延迟delay毫秒开始
	 * @param daemon
	 * @param delay
	 */
	public void schedule(T daemon, final long delay){
		suc.scheduleAtFixedRate(daemon, delay, daemon.getWakeupInterval(), TimeUnit.MILLISECONDS);
	}
	
	public void stop(){
		if (!suc.isShutdown()){
			suc.shutdown();
		}
	}
}
