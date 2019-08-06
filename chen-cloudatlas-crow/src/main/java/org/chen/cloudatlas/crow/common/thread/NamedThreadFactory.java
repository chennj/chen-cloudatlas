package org.chen.cloudatlas.crow.common.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory{

	private final AtomicInteger threadNumber = new AtomicInteger(1);
	
	private final String namePrefix;
	
	private final boolean daemon;
	
	private final int priority;
	
	public NamedThreadFactory(String namePrefix){
		
		this(namePrefix,true,Thread.NORM_PRIORITY);
	}
	
	public NamedThreadFactory(String namePrefix, boolean isDaemon, int priority) {
		
		this.namePrefix = namePrefix;
		this.daemon = isDaemon;
		this.priority = priority;
	}

	public Thread newThread(Runnable r) {

		Thread t = new Thread(r, namePrefix + "-" + threadNumber.getAndIncrement());
		t.setDaemon(daemon);
		t.setPriority(priority);
		if (t.getPriority() != Thread.NORM_PRIORITY){
			t.setPriority(Thread.NORM_PRIORITY);
		}
		return t;
	}

}
