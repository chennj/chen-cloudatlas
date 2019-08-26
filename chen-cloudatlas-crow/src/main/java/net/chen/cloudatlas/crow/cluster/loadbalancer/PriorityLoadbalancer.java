package net.chen.cloudatlas.crow.cluster.loadbalancer;

import java.util.List;

import net.chen.cloudatlas.crow.rpc.Invoker;

/**
 * 按优先级<br>
 * 比如server端机器由4台，优先级分别为1，2，3，4，那么按优先级的策略时：
 * 始终发往第一台，直到第一台坏掉，才选择第二高优先级的机器
 * @author chenn
 *
 */
public class PriorityLoadbalancer extends AbstractLoadbalancer{

	@Override
	protected Invoker doSelect(List invokerList) {
		
		// 直接拿列表中权重最高的，这里所有的url都是经过筛选的理论可用的url
		// 经过父类方法处理，这里最骚由两个可用的url
		Invoker maxInvoker = (Invoker)invokerList.get(0);
		int maxPriority = getPriority(maxInvoker);
		
		for (int i=1; i<invokerList.size(); i++){
			Invoker invoker = (Invoker)invokerList.get(i);
			int priority = getPriority(invoker);
			if (priority < maxPriority){
				maxPriority = priority;
				maxInvoker = invoker;
			}
		}
		
		return maxInvoker;
	}

}
