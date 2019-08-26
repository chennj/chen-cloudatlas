package net.chen.cloudatlas.crow.cluster.loadbalancer;

import java.util.List;

import net.chen.cloudatlas.crow.rpc.Invoker;

/**
 * 只取第一个，忽略其他，会导致其他服务器负载小，全部压在第一台服务器上
 * @author chenn
 *
 */
public class FirstLoadbalancer extends AbstractLoadbalancer{

	@Override
	protected Invoker doSelect(List invokerList) {
		
		return (Invoker)invokerList.get(0);
	}

}
