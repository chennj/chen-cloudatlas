package net.chen.cloudatlas.crow.manager.api;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.thread.NamedThreadFactory;

public class RegistryFixDaemonService {

	private static volatile ScheduledExecutorService executorService;
	
	public static synchronized void start(Runnable runnable){
		
		if (null==executorService || executorService.isTerminated()){
			executorService = Executors.newSingleThreadScheduledExecutor(
					new NamedThreadFactory("DEFAULT_FIX_PROVIDER",true));
			executorService.scheduleWithFixedDelay(runnable, 30*1000, Constants.DEFAULT_FIX_PROVIDER, TimeUnit.MICROSECONDS);
		}
		
	}
	
	public static synchronized void stop(){
		
		if (null!=executorService || !executorService.isTerminated()){
			executorService.shutdown();
		}
	}
}
