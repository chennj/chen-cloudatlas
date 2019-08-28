package net.chen.cloudatlas.crow.cluster.invoker;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import net.chen.cloudatlas.crow.common.thread.NamedThreadFactory;
import net.chen.cloudatlas.crow.config.ReferenceConfig;
import net.chen.cloudatlas.crow.remote.RemoteException;
import net.chen.cloudatlas.crow.remote.Request;
import net.chen.cloudatlas.crow.remote.Response;
import net.chen.cloudatlas.crow.rpc.Invocation;
import net.chen.cloudatlas.crow.rpc.Invoker;
import net.chen.cloudatlas.crow.rpc.Result;
import net.chen.cloudatlas.crow.rpc.RpcException;

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void doAcall(Request request) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Result doInvoke(Invocation invocation) throws RpcException {
		// TODO Auto-generated method stub
		return null;
	}

}
