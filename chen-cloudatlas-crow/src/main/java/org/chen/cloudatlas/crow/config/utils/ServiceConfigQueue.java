package org.chen.cloudatlas.crow.config.utils;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.chen.cloudatlas.crow.config.ServiceConfig;

public class ServiceConfigQueue {

	private static volatile Queue<ServiceConfig> sConfigQueue;
	
	public static Queue<ServiceConfig> getInstance(){
		if (null == sConfigQueue){
			synchronized(ServiceConfigQueue.class){
				if (null == sConfigQueue){
					sConfigQueue = new ConcurrentLinkedQueue<ServiceConfig>();
				}
			}
		}
		return sConfigQueue;
	}
}
