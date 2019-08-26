package net.chen.cloudatlas.crow.cluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.DcType;
import net.chen.cloudatlas.crow.common.utils.UrlUtil;
import net.chen.cloudatlas.crow.config.ReferenceConfig;
import net.chen.cloudatlas.crow.rpc.Invoker;
import net.chen.cloudatlas.crow.rpc.SubInvoker;

public abstract class AbstractClusterInvoker implements SubInvoker{

	private volatile Loadbalancer loadbalancer;
	
	private Class interfaceClass;
	
	protected DcType dc = null;
	
	protected List<Invoker> invokers;
	
	protected ReferenceConfig referenceConfig;
	
	protected Map<DcType, CopyOnWriteArrayList<Invoker>> invokerMap = new HashMap<>();
	
	/**
	 * only for test
	 */
	private Invoker lastSelectedInvoker;
	
	@Override
	public void setDc(DcType dc){
		this.dc = dc;
	}
	
	@Override
	public String getInvokeKey(){
		return null;
	}
	
	public AbstractClusterInvoker(){
		
	}
	
	public AbstractClusterInvoker(List<Invoker> invokers){
		this.invokers = invokers;
		buildInvokerMap(invokers);
	}
	
	public AbstractClusterInvoker(List<Invoker> invokers, ReferenceConfig referenceConfig){
		this.invokers = invokers;
		this.referenceConfig = referenceConfig;
		buildInvokerMap(invokers);
	}
	
	protected void initInvokerMap(){
		
		for (DcType dc : DcType.values()){
			
			if (dc.getDcId() != 0){
				invokerMap.put(dc, new CopyOnWriteArrayList<Invoker>());
			}
		}
	}
	
	protected void buildInvokerMap(List<Invoker> invokers){
		
		invokerMap.clear();
		initInvokerMap();
		
		for (Invoker inv : invokers){
			
			DcType dc = DcType.fromString(UrlUtil.getParameter(inv.getUrl(), Constants.DC, Constants.DEFAULT_DC));
			if (invokerMap.containsKey(dc) && invokerMap.get(dc) != null){
				List<Invoker> list = invokerMap.get(dc);
				list.add(inv);
			}
		}
	}
}
