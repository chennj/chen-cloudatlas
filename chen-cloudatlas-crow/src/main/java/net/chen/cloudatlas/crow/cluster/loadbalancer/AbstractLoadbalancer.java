package net.chen.cloudatlas.crow.cluster.loadbalancer;

import java.util.List;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.cluster.Loadbalancer;
import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.utils.UrlUtil;
import net.chen.cloudatlas.crow.config.CrowClientContext;
import net.chen.cloudatlas.crow.rpc.Invoker;

public abstract class AbstractLoadbalancer implements Loadbalancer{

	@Override
	public Invoker select(List invokerList) {
		
		Invoker result = null;
		
		if (null == invokerList || invokerList.size()==0){
			return null;
		}
		
		if (invokerList.size() == 1){
			return (Invoker) invokerList.get(0);
		}
		
		result = doSelect(invokerList);
		
		Logger.debug("Selected: " + result.getUrl().toIdentityString());
		return result;
	}

	protected int getWeight(Invoker invoker){
		return UrlUtil.getParameter(invoker.getUrl(), Constants.WEIGHT, Constants.DEFAULT_WEIGHT);
	}
	
	protected int getPriority(Invoker invoker){
		
		// 如果存在zk，则客户端无法通过url先后顺序确定优先级，此时采用服务器的权重信息
		// 作为其优先级的判断依据
		if (CrowClientContext.hasZk()){
			return Constants.DEFAULT_MAXWEIGHT
					- UrlUtil.getParameter(invoker.getUrl(), Constants.WEIGHT, Constants.DEFAULT_WEIGHT);
		} else {
			return UrlUtil.getParameter(invoker.getUrl(), Constants.PRIORITY, Constants.DEFAULT_PRIORITY);
		}
	}
	
	protected String getKey(Invoker invoker){
		
		return invoker.getUrl().getHostAndPort();
	}
	
	protected String getParameter(Invoker invoker, String key){
		
		return UrlUtil.getParameter(invoker.getUrl(), key, null);
	}
	
	protected abstract Invoker doSelect(List invokerList);
}
