package net.chen.cloudatlas.crow.cluster.loadbalancer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.chen.cloudatlas.crow.rpc.Invoker;

/**
 * 循环负载平衡<br>
 * 轮循，按公约后的权重设置轮循比率。存在慢的提供者累计请求问题，比如：第二台机器很慢，但没挂，
 * 当请求调到他时就卡在那，久而久之，所有请求都卡在第二台上。
 * @author chenn
 *
 */
public class RoundRobinLoadbalancer extends AbstractLoadbalancer{

	private final ConcurrentMap<String, ThreadLocal<Integer>> sequences = new ConcurrentHashMap<>();
	
	private final ConcurrentMap<String, ThreadLocal<Integer>> weightSequences = new ConcurrentHashMap<>();
	
	@Override
	protected Invoker doSelect(List invokerList) {
		
		String key = getKey((Invoker)invokerList.get(0));
		
		ThreadLocal<Integer> localSequence	= sequences.get(key);
		ThreadLocal<Integer> localWeight	= weightSequences.get(key);
		
		if (null == localSequence){
			sequences.putIfAbsent(key, new ThreadLocal<Integer>(){

				@Override
				protected Integer initialValue() {
					return 0;
				}
				
			});
			localWeight = weightSequences.get(key);
		}
		
		List<Integer> weights = new ArrayList<Integer>();
		for (Object invoker : invokerList){
			weights.add(getWeight((Invoker)invoker));
		}
		
		int gcd 			= getGCD(weights);
		int maxWeight 		= getMaxWeight(weights);		
		int currentSequence	= localSequence.get();
		int currentWeight 	= localWeight.get();
		
		if (currentWeight > maxWeight){
			currentWeight = 0;
		}
		
		while (true){
			
			for (int i=currentSequence; i<weights.size(); i++){
				if (i == (weights.size() - 1)){
					localWeight.set((currentWeight+gcd)%maxWeight);
				}
				if (weights.get(i) >= currentWeight){
					localSequence.set((currentSequence+1)%weights.size());
					return (Invoker)invokerList.get(i);
				}
			}
			currentSequence = 0;
		}
	}

	/**
	 * 求所有权重的最大公约数
	 * @param weights
	 * @return
	 */
	private int getGCD(List<Integer> weights) {
		
		int gcd = 1;
		
		for (int i=0; i<weights.size(); i++){
			if (i==0){
				gcd = getGCD(weights.get(i), weights.get(i+1));
				i=i+1;
			} else {
				gcd = getGCD(weights.get(i), gcd);
			}
		}
		
		return gcd;
	}

	private int getGCD(int m, int n) {
		
		if (m<n){
			int temp = n;
			m = n;
			n = temp;
		}
		// 迭代法求最大公约数
		while (!(m%n==0)){
			int temp = m % n;
			m = n;
			n = temp;
		}
		return n;
	}

	private int getMaxWeight(List<Integer> weights) {
		
		int maxWeight = 0;
		for (int weight : weights){
			maxWeight = Math.max(maxWeight, weight);
		}
		
		return maxWeight;
	}

}
