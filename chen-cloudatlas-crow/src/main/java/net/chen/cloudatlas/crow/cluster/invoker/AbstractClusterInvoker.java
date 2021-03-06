package net.chen.cloudatlas.crow.cluster.invoker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.cluster.Loadbalancer;
import net.chen.cloudatlas.crow.cluster.LoadbalancerFactory;
import net.chen.cloudatlas.crow.cluster.log.ClusterLoggerProxy;
import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.DcType;
import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.common.Version;
import net.chen.cloudatlas.crow.common.cluster.LoadBalanceType;
import net.chen.cloudatlas.crow.common.utils.NetUtil;
import net.chen.cloudatlas.crow.common.utils.UrlUtil;
import net.chen.cloudatlas.crow.config.ReferenceConfig;
import net.chen.cloudatlas.crow.remote.RemoteException;
import net.chen.cloudatlas.crow.remote.Request;
import net.chen.cloudatlas.crow.remote.Response;
import net.chen.cloudatlas.crow.rpc.Invocation;
import net.chen.cloudatlas.crow.rpc.Invoker;
import net.chen.cloudatlas.crow.rpc.Result;
import net.chen.cloudatlas.crow.rpc.RpcException;
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
	
	protected void insertToInvokerMap(Invoker invoker){
		
		DcType dc = DcType.fromString(UrlUtil.getParameter(invoker.getUrl(), Constants.DC, Constants.DEFAULT_DC));
		if (invokerMap.containsKey(dc) && invokerMap.get(dc) != null){
			
			if (invoker != null){
				while(invokerMap.get(dc).contains(invoker)){
					invokerMap.get(dc).remove(invoker);
				}
				invokerMap.get(dc).add(invoker);
			}
		}
	}
	
	protected void removeFromInvokerMap(Invoker invoker){
		
		DcType dc = DcType.fromString(UrlUtil.getParameter(invoker.getUrl(), Constants.DC, Constants.DEFAULT_DC));
		if (invokerMap.containsKey(dc) && invokerMap.get(dc) != null){
			
			if (invoker != null){
				while(invokerMap.get(dc).contains(invoker)){
					invokerMap.get(dc).remove(invoker);
				}
			}
		}
	}

	@Override
	public Response call(Request request) throws RemoteException {
		return doCall(request);
	}

	@Override
	public void acall(Request request) throws RemoteException {
		doAcall(request);
	}

	@Override
	public Result invoke(Invocation invocation) throws RpcException {
		
		long start = System.currentTimeMillis();
		Result result = doInvoke(invocation);
		long end = System.currentTimeMillis();
		Logger.debug("crow framework took {} millseconds to call this message",(end-start));
		return result;
	}
	
	protected abstract Response doCall(Request request) throws RemoteException;
	
	protected abstract void doAcall(Request request) throws RemoteException;
	
	protected abstract Result doInvoke(Invocation invocation) throws RpcException;
	
	protected void checkInvokers(List<Invoker> invokerList) throws RpcException{
		
		if (null == invokerList || invokerList.size() == 0){
			throw new RpcException("failed to invoke. no urls available on the consumer "
					+ NetUtil.getLocalHost()
					+ "using the crow version "
					+ Version.getPrettyString());
		}
	}
	
	protected void checkSubInvokers(List<Invoker> invokerList) throws RemoteException{
		
		if (null == invokerList || invokerList.size() == 0){
			throw new RemoteException("failed to invoke. no urls available on the consumer "
					+ NetUtil.getLocalHost()
					+ "using the crow version "
					+ Version.getPrettyString());
		}
	}
	
	protected Invoker<Invoker> select(List<Invoker> invokerList, List<Invoker> selected){
		
		if (null == invokerList || invokerList.size() == 0){
			this.lastSelectedInvoker = null;
			return null;
		}
		
		if (invokerList.size() == 1){
			Invoker result = invokerList.get(0);
			this.lastSelectedInvoker = result;
			return result;
		}
		
		// 如果只有两个url，退化成轮循
		if (invokerList.size() == 2 && selected != null && selected.size() > 0){
			Invoker result = selected.get(0) == invokerList.get(0) ? invokerList.get(1) : invokerList.get(0);
			this.lastSelectedInvoker = result;
			return result;
		}
		
		synchronized(this){
			if (null == loadbalancer){
				Logger.debug("loadbalancer is null,create one");
				loadbalancer = getLoadbalancer(invokerList);
			}
		}
		
		Invoker invoker = loadbalancer.select(invokerList);
		
		// 如果selected中包含则重试
		if (null != selected && selected.contains(invoker)){
			
			try {
				Invoker reinvoker = reselect(loadbalancer, invokerList, selected);
				if (null != reinvoker){
					invoker = reinvoker;
				} else {
					// 看下第一次选的位置，如果不是最后，选+1位置
					int index = invokerList.indexOf(invoker);
					try {
						// 最后在避免碰撞
						invoker = index < invokerList.size() - 1 ? (invokerList.get(index + 1)) : invoker;
					} catch (Exception e){
						Logger.warn(e);
						ClusterLoggerProxy.warn(e.getMessage() + "may because url list dynamic change, ignore.");
					}
				}
			} catch (Exception t){
				Logger.error(t);
				ClusterLoggerProxy.error("clustor reselect fail reason is :" + t.getMessage());
			}
		}
		
		this.lastSelectedInvoker = invoker;
		return invoker;
	}
	
	private Invoker<Invoker> reselect(Loadbalancer loadbalancer, List<Invoker> invokerList, List<Invoker> selected){
		
		// 预先分配一个，这个列表时一定会用到的
		List<Invoker> reselectInvokers = new ArrayList<Invoker>(
				invokerList.size() > 1 ? (invokerList.size() - 1) : invokerList.size());
		
		// 选全部非select
		for (Invoker invoker : invokerList){
			
			if (null == selected || !selected.contains(invoker)){
				reselectInvokers.add(invoker);
			}
		}
		
		if (reselectInvokers.size() > 0){
			return loadbalancer.select(reselectInvokers);
		}
		
		// 最后从select中选可用的
		{
			if (null != selected){
				
				for (Invoker invoker : selected){
					if (!reselectInvokers.contains(invoker)){
						reselectInvokers.add(invoker);
					}
				}
			}
			if (reselectInvokers.size()>0){
				return loadbalancer.select(reselectInvokers);
			}
		}
		
		return null;
	}
	
	private Loadbalancer getLoadbalancer(List<Invoker> invokerList){
		
		// 更具url中的loadbalance策略来决定用哪一种负载均衡器，只需取第一个即可
		LoadBalanceType type;
		if (null == invokerList || invokerList.size() == 0){
			type = Constants.DEFAULT_LOADBALANCER_TYPE;
		} else {
			type = LoadBalanceType.fromString(
					invokerList.get(0).getUrl().getParameter(Constants.LOAD_BALANCE_STRATEGY));
		}
		
		return LoadbalancerFactory.getLoadBalancer(type);
	}
	
	protected int getParameter(Invoker<Invoker> invoker, String key, int defaultValue){
		
		return UrlUtil.getParameter(invoker.getUrl(), key, defaultValue);
	}
	
	protected Map<DcType, List<Invoker>> getActiveInvokerGroup(List<Invoker> invokerList){
		
		Map<DcType, List<Invoker>> dcInvokerMap = new LinkedHashMap<DcType, List<Invoker>>();
		for (Invoker invoker : invokerList){
			
			if (!invoker.isAvailable()){
				continue;
			}
			
			DcType dc = DcType.fromString(UrlUtil.getParameter(invoker.getUrl(), Constants.DC, Constants.DEFAULT_DC));
			List<Invoker> list;
			if (dcInvokerMap.containsKey(dc)){
				list = dcInvokerMap.get(dc);
			} else {
				list = new ArrayList<Invoker>();
			}
			list.add(invoker);
			dcInvokerMap.put(dc, list);
		}
		
		return dcInvokerMap;
	}
	
	private List<Invoker> getActiveInvoker(){
		
		LinkedHashMap<DcType, Boolean> dcStrategy= this.getDcStrategy();
		
		Iterator it = dcStrategy.entrySet().iterator();
		List<Invoker> subList = new ArrayList<Invoker>();
		
		while (it.hasNext()){
			
			Map.Entry<DcType, Boolean> entry = (Map.Entry<DcType, Boolean>)it.next();
			// entry.getVAlue 判断该dc是否具有调用策略true为可以调用，false忽略
			if (entry.getValue() && invokerMap.containsKey(entry.getKey())){
				
				for (Invoker invoker : invokerMap.get(entry.getKey())){
					if (invoker.isAvailable()){
						subList.add(invoker);
					}
				}
			}
			
			if (subList.size() == 0){
				// 如果该中心没有任何可用的链路，则继续寻找其他中心
				continue;
			} else {
				// 如果该中心有可用的链路，则不在查找其他中心
				break;
			}
		}
		
		return subList;
	}
	
	protected List<Invoker> getActiveInvoker(DcType dc){
		
		if (null == dc){
			return this.getActiveInvoker();
		}
		
		List<Invoker> subList = new ArrayList<>();
		
		if (invokerMap.containsKey(dc) && invokerMap.get(dc) != null){
			// map中存在该dc，并且该dc的策略时可以访问
			List<Invoker> invokers = invokerMap.get(dc);
			for (Invoker invoker : invokers){
				if (invoker.isAvailable()){
					subList.add(invoker);
				}
			}
			return subList;
		} else {
			return null;
		}
	}
	
	protected List<Invoker> getAllInvoker(DcType dc){
		
		if (null != dc){
			return invokerMap.get(dc);
		}
		
		LinkedHashMap<DcType, Boolean> dcStrategy= this.getDcStrategy();
		
		Iterator it = dcStrategy.entrySet().iterator();
		List<Invoker> invokerList = new ArrayList<Invoker>();
		
		while (it.hasNext()){
			
			Map.Entry<DcType, Boolean> entry = (Map.Entry<DcType, Boolean>)it.next();
			// entry.getVAlue 判断该dc是否具有调用策略true为可以调用，false忽略
			if (entry.getValue() && invokerMap.containsKey(entry.getKey())){
				
				invokerList = invokerMap.get(entry.getKey());
			}
			
			if (invokerList.size() == 0){
				// 如果该中心没有任何可用的链路，则继续寻找其他中心
				continue;
			} else {
				// 如果该中心有可用的链路，则不在查找其他中心
				break;
			}
		}
		
		return invokerList;
		
	}
	
	private LinkedHashMap<DcType, Boolean> getDcStrategy(){
		
		LinkedHashMap<DcType, Boolean> dcStrategy = null;
		
		if (null == referenceConfig){
			dcStrategy = new LinkedHashMap<DcType, Boolean>();
			dcStrategy.put(DcType.SHANGHAI, true);
		} else {
			dcStrategy = (LinkedHashMap<DcType, Boolean>)referenceConfig.getDcStrategy();
		}
		
		return dcStrategy;
	}

	@Override
	public boolean isAvailable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Class getInterface() {
		
		return interfaceClass;
	}

	@Override
	public URL getUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insertInvoker(Invoker invoker) {
		
		if (null != invoker){
			
			while (invokers.contains(invoker)){
				invokers.remove(invoker);
			}
			invokers.add(invoker);
		}
		insertToInvokerMap(invoker);
	}

	@Override
	public void deleteInvoker(Invoker invoker) {
		invokers.remove(invoker);
		removeFromInvokerMap(invoker);
	}

	@Override
	public void setInterface(Class interfaceClass) {
		this.interfaceClass = interfaceClass;
	}
	
	@Override
	public void destroy(){
		
	}

	public List<Invoker> getInvokers() {
		return invokers;
	}

	public void setInvokers(List<Invoker> invokers) {
		this.invokers = invokers;
	}
	
	public Invoker getLastSelectedInvoker(){
		return this.lastSelectedInvoker;
	}
	
}
