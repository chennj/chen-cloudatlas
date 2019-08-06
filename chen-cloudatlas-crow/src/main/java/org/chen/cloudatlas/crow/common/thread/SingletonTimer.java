package org.chen.cloudatlas.crow.common.thread;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;

public class SingletonTimer {

	private SingletonTimer(){
		
	}
	
	private static volatile Timer timer;
	
	public static Timer getTimer(){
		
		if (null == timer){
			synchronized(SingletonTimer.class){
				if (null == timer){
					timer = new HashedWheelTimer();
				}
			}
		}
		
		return timer;
	}
}
