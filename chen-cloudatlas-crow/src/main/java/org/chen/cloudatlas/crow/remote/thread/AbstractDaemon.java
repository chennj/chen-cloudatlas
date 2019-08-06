package org.chen.cloudatlas.crow.remote.thread;

import org.chen.cloudatlas.crow.common.utils.AtomicPositiveInteger;

/**
 * 守护线程基类
 * @author chenn
 *
 */
public abstract class AbstractDaemon implements Runnable{

	/**
	 * 执行守护线程的间隔时间
	 */
	private final long wakeupInterval;
	
	private AtomicPositiveInteger tick;
	
	public AbstractDaemon(final long wakeupInterval){
		this.wakeupInterval = wakeupInterval;
		this.tick = new AtomicPositiveInteger();
	}

	public long getWakeupInterval() {
		return wakeupInterval;
	}

	public int getTick() {
		return tick.get();
	}
	
	public void run(){
		this.tick.incrementAndGet();
		this.execute();
	}
	
	protected abstract void execute();
}
