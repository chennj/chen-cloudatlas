package net.chen.cloudatlas.crow.cluster.loadbalancer;

import java.util.List;
import java.util.Random;

import net.chen.cloudatlas.crow.rpc.Invoker;

/**
 * 随机，按权重设置随机概率。在一个截面上碰撞的概率高，但调用量越大越平均。
 * 而且，按概率使用权重后也比较平均，有利于动态调整提供者权重
 * @author chenn
 *
 */
public class RandomLoadbalancer extends AbstractLoadbalancer{

	private final Random random = new Random();
	
	@Override
	protected Invoker doSelect(List invokerList) {
		
		int length = invokerList.size();
		int totalWeight = 0;
		boolean sameWeight = true;
		
		int presumeTotalWeight = getWeight((Invoker)invokerList.get(0))*length;
		for (int i=0; i<length; i++){
			totalWeight += getWeight((Invoker)invokerList.get(i));
		}
		
		if (presumeTotalWeight != totalWeight){
			// 权重不全一样
			sameWeight = false;
		}
		
		if (totalWeight > 0 && !sameWeight){
			// 如果权重不相同且权重大于0，则按总权重数随机，
			// 并确定随机值落在那个片段上
			int offset = random.nextInt(totalWeight);
			for (int i=0; i<length; i++){
				offset -= getWeight((Invoker)invokerList.get(i));
				if (offset < 0){
					return (Invoker) invokerList.get(i);
				}
			}
		}
		
		// 如果权重相同或权重为0，则均等随机
		return (Invoker)invokerList.get(random.nextInt(length));
	}

}
