package net.chen.cloudatlas.crow.cluster.invoker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.cluster.log.ClusterLoggerProxy;
import net.chen.cloudatlas.crow.common.thread.NamedThreadFactory;
import net.chen.cloudatlas.crow.config.ReferenceConfig;
import net.chen.cloudatlas.crow.remote.RemoteException;
import net.chen.cloudatlas.crow.remote.Request;
import net.chen.cloudatlas.crow.remote.Response;
import net.chen.cloudatlas.crow.rpc.Invocation;
import net.chen.cloudatlas.crow.rpc.Invoker;
import net.chen.cloudatlas.crow.rpc.Result;
import net.chen.cloudatlas.crow.rpc.RpcException;
import net.chen.cloudatlas.crow.rpc.SubInvoker;
import net.chen.cloudatlas.crow.rpc.impl.RpcResult;

/**
 * <pre>
 * 失败自动恢复，后台纪录失败请求，定时重发，通常用于消息通知操作
 * </pre>
 * @author chenn
 *
 */
public class FailbackInvoker extends AbstractClusterInvoker{

	private static final long RETRY_FAILED_PERIOD = 5 * 1000;
	
	private final ScheduledExecutorService scheduledExecutorService =
			Executors.newScheduledThreadPool(2, new NamedThreadFactory("failback-cluster-timer"));
	
	private volatile ScheduledFuture<?> retryFuture;
	
	private final ConcurrentMap<AbstractClusterInvoker, Request> subFailed = 
			new ConcurrentHashMap<>();
	
	private final ConcurrentMap<AbstractClusterInvoker, Invocation> failed =
			new ConcurrentHashMap<>();
	
	public FailbackInvoker(List<Invoker> invokers){
		super(invokers);
	}
	
	public FailbackInvoker(List<Invoker> invokers, ReferenceConfig rConfig){
		super(invokers, rConfig);
	}
	
	@Override
	protected Response doCall(Request request) throws RemoteException {
		
		try {
			// 得到可用的url列表，去掉 不通的
			List<Invoker> invokerList = getActiveInvoker(dc);
			
			checkSubInvokers(invokerList);
			
			Invoker invoker = select(invokerList,null);
			return ((SubInvoker)invoker).call(request);
		} catch (Exception e){
			Logger.error("failback to invoker, wait for retry in background. ignored exception :",e);
			ClusterLoggerProxy.error("failback to invoker, wait for retry in background. ignored exception :"+e.getMessage());
			addSubFailed(this,request);
			return null;
		}
	}

	private void addSubFailed(FailbackInvoker failbackInvoker, Request request) {
		
		if (null == retryFuture){
			
			synchronized(this){
				if (null == retryFuture){
					retryFuture = scheduledExecutorService.scheduleWithFixedDelay(new Runnable(){

						@Override
						public void run() {
							// 收集统计信息
							try {
								retrySubFailed();
							} catch (Exception e){
								Logger.error("unexceptioned error occur at collect statistic",e);
								ClusterLoggerProxy.error("unexceptioned error occur at collect statistic"+e.getMessage());
							}
						}
						
					},this.RETRY_FAILED_PERIOD, this.RETRY_FAILED_PERIOD, TimeUnit.MILLISECONDS);
				}
			}
		}
	}

	protected void retrySubFailed() {
		
		if (subFailed.size() == 0){
			return;
		}
		
		for (Map.Entry<AbstractClusterInvoker, Request> entry : new HashMap<>(subFailed).entrySet()){
			
			AbstractClusterInvoker invoker = entry.getKey();
			Request request = entry.getValue();
			
			try {
				((SubInvoker)invoker).call(request);
				subFailed.remove(invoker);
			} catch (Exception e){
				Logger.error("failed retry to invoker, waiting again .. ",e);
				ClusterLoggerProxy.error("failed retry to invoker, waiting again .. "+e.getMessage());
			}
			
		}
	}

	@Override
	protected void doAcall(Request request) throws RemoteException {
		
		try {
			// 得到可用的url列表，去掉 不通的
			List<Invoker> invokerList = getActiveInvoker(dc);
			
			checkSubInvokers(invokerList);
			
			Invoker invoker = select(invokerList,null);
			((SubInvoker)invoker).acall(request);
		} catch (Exception e){
			Logger.error("failback to async invoker, wait for retry in background. ignored exception :",e);
			ClusterLoggerProxy.error("failback to async invoker, wait for retry in background. ignored exception :"+e.getMessage());
			addSubFailed(this,request);
		}
	}

	@Override
	protected Result doInvoke(Invocation invocation) throws RpcException {
		
		try {
			// 得到可用的url列表，去掉不通的
			List<Invoker> invokerList = getActiveInvoker(dc);
			
			checkInvokers(invokerList);
			
			Invoker invoker = select(invokerList,null);
			return invoker.invoke(invocation);
		} catch (Exception e){
			Logger.error("failed to invoke, wait for retry in background, ignore exceptin :",e);;
			ClusterLoggerProxy.error("failed to invoke, wait for retry in background, ignore exceptin :"+e.getMessage());
			addSubFailed(this,invocation);		
			return new RpcResult();
		}
	}

	private void addSubFailed(FailbackInvoker failbackInvoker, Invocation invocation) {
		
		if (null == retryFuture){
			
			synchronized(this){
				if (null == retryFuture){
					retryFuture = scheduledExecutorService.scheduleWithFixedDelay(new Runnable(){

						@Override
						public void run() {
							// 收集统计信息
							try {
								retryFailed();
							} catch (Exception e){
								Logger.error("unexceptioned error occur at collect statistic",e);
								ClusterLoggerProxy.error("unexceptioned error occur at collect statistic"+e.getMessage());
							}
						}
						
					},this.RETRY_FAILED_PERIOD, this.RETRY_FAILED_PERIOD, TimeUnit.MILLISECONDS);
				}
			}
		}
		
	}

	protected void retryFailed() {
		
		if (failed.size() == 0){
			return;
		}
		
		for (Map.Entry<AbstractClusterInvoker, Invocation> entry : new HashMap<>(failed).entrySet()){
			
			AbstractClusterInvoker invoker = entry.getKey();
			Invocation invocation = entry.getValue();
			
			try {
				invoker.invoke(invocation);
				subFailed.remove(invoker);
			} catch (Exception e){
				Logger.error("failed retry to invoker, waiting again .. ",e);
				ClusterLoggerProxy.error("failed retry to invoker, waiting again .. "+e.getMessage());
			}
			
		}

	}

}
