package net.chen.cloudatlas.crow.cluster;

import net.chen.cloudatlas.crow.common.cluster.LoadBalanceType;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.cluster.loadbalancer.FirstLoadbalancer;
import net.chen.cloudatlas.crow.cluster.loadbalancer.PriorityLoadbalancer;
import net.chen.cloudatlas.crow.cluster.loadbalancer.RandomLoadbalancer;
import net.chen.cloudatlas.crow.cluster.loadbalancer.RoundRobinLoadbalancer;
import net.chen.cloudatlas.crow.common.Constants;

public class LoadbalancerFactory {

	public static Loadbalancer getLoadBalancer(LoadBalanceType type){
		
		Loadbalancer result = null;
		if (null == type){
			type = Constants.DEFAULT_LOADBALANCER_TYPE;
		}
		
		if (type.equals(LoadBalanceType.FIRST)){
			result = new FirstLoadbalancer();
		}
		
		if (type.equals(LoadBalanceType.RANDOM)){
			result = new RandomLoadbalancer();
		}
		
		if (type.equals(LoadBalanceType.ROUNDROBIN)){
			result = new RoundRobinLoadbalancer();
		}
		
		if (type.equals(LoadBalanceType.PRIORITY)){
			result = new PriorityLoadbalancer();
		}
		
		Logger.debug("Using LoadBalanceType: " + type.getText());
		
		return result;
	}
}
