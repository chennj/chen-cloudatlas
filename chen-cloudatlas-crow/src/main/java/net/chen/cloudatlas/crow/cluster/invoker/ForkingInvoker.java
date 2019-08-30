package net.chen.cloudatlas.crow.cluster.invoker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import net.chen.cloudatlas.crow.common.Constants;
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

/**
 * <pre>
 * 	1、并行调用多个服务器，只要一个成功即返回
 * 	2、通常用于实时性要求较高的读操作
 * </pre>
 * @author chenn
 *
 */
public class ForkingInvoker extends AbstractClusterInvoker{

	private final ExecutorService executor = 
			Executors.newCachedThreadPool(new NamedThreadFactory("forking-cluster-timer"));
	
	public ForkingInvoker(List<Invoker> invokers){
		super(invokers);
	}
	
	public ForkingInvoker(List<Invoker> invokers, ReferenceConfig rConfig){
		super(invokers, rConfig);
	}
	
	@Override
	protected Response doCall(Request request) throws RemoteException {
		
		// 得到可用的url，去掉不通的
		List<Invoker> invokerList = getActiveInvoker(dc);
		
		checkSubInvokers(invokerList);
		
		final List<Invoker> selected;
		final int forks = getParameter(invokerList.get(0), Constants.FORKS_KEY, Constants.DEFAULT_FORKS);
		final int timeout = getParameter(invokerList.get(0), Constants.TIMEOUT, Constants.DEFAULT_NO_RESPONSE_TIMEOUT);
		final Request req = request;
		
		if (forks <= 0 || forks >= invokerList.size()){
			selected = invokerList;
		} else {
			selected = new ArrayList<Invoker>();
			for (int i=0; i<forks; i++){
				// 在invoker列表（排除selected）后，如果没有选够，则存在重复循环问题，见select实现
				Invoker invoker = select(invokerList, selected);
				// 防止重复添加invoker
				if (!selected.contains(invoker)){
					selected.add(invoker);
				}
			}
		}
		
		final AtomicInteger count = new AtomicInteger();
		final BlockingQueue<Object> ref = new LinkedBlockingQueue<>();
		for (final Invoker invoker : selected){
			
			executor.execute(new Runnable(){

				@Override
				public void run() {
					try {
						Response result = ((SubInvoker)invoker).call(req);
						ref.offer(result);
					} catch (Exception e){
						int value = count.incrementAndGet();
						if (value >= selected.size()){
							ref.offer(e);
						}
					}
				}
				
			});
		}
		
		try {
			Object ret = ref.poll(timeout, TimeUnit.MICROSECONDS);
			if (ret instanceof Throwable){
				
				Throwable e = (Throwable)ret;
				throw new RemoteException(
						"failed to forking invoke provider "
						+ selected
						+ ", but no luck to perform the invocation, last error is: "
						+ e.getMessage(),
						e.getCause() != null ? e.getCause() : e);
			}
			return (Response)ret;
		} catch (InterruptedException e){
			throw new RemoteException("failed to forking invoke provider "
					+ selected
					+ ", but no luck to perform the invocation, last error is: "
					+ e.getMessage(),e);
		}
				
	}

	@Override
	protected void doAcall(Request request) throws RemoteException {
		
		// 得到可用的url，去掉不通的
		List<Invoker> invokerList = getActiveInvoker(dc);
		
		checkSubInvokers(invokerList);
		
		final List<Invoker> selected;
		final int forks = getParameter(invokerList.get(0), Constants.FORKS_KEY, Constants.DEFAULT_FORKS);
		final int timeout = getParameter(invokerList.get(0), Constants.TIMEOUT, Constants.DEFAULT_NO_RESPONSE_TIMEOUT);
		final Request req = request;
		
		if (forks <= 0 || forks >= invokerList.size()){
			selected = invokerList;
		} else {
			selected = new ArrayList<Invoker>();
			for (int i=0; i<forks; i++){
				// 在invoker列表（排除selected）后，如果没有选够，则存在重复循环问题，见select实现
				Invoker invoker = select(invokerList, selected);
				// 防止重复添加invoker
				if (!selected.contains(invoker)){
					selected.add(invoker);
				}
			}
		}
		
		final AtomicInteger count = new AtomicInteger();
		final BlockingQueue<Object> ref = new LinkedBlockingQueue<>();
		for (final Invoker invoker : selected){
			
			executor.execute(new Runnable(){

				@Override
				public void run() {
					try {
						((SubInvoker)invoker).acall(req);
					} catch (Exception e){
						int value = count.incrementAndGet();
						if (value >= selected.size()){
							ref.offer(e);
						}
					}
				}
				
			});
		}
		
		try {
			Object ret = ref.poll(timeout, TimeUnit.MICROSECONDS);
			if (ret instanceof Throwable){
				
				Throwable e = (Throwable)ret;
				throw new RemoteException(
						"failed to forking invoke provider "
						+ selected
						+ ", but no luck to perform the invocation, last error is: "
						+ e.getMessage(),
						e.getCause() != null ? e.getCause() : e);
			}
		} catch (InterruptedException e){
			throw new RemoteException("failed to forking invoke provider "
					+ selected
					+ ", but no luck to perform the invocation, last error is: "
					+ e.getMessage(),e);
		}
		
	}

	@Override
	protected Result doInvoke(Invocation invocation) throws RpcException {
		
		// 得到可用的url，去掉不通的
		List<Invoker> invokerList = getActiveInvoker(dc);
		
		checkInvokers(invokerList);
		
		final List<Invoker> selected;
		final int forks = getParameter(invokerList.get(0), Constants.FORKS_KEY, Constants.DEFAULT_FORKS);
		final int timeout = getParameter(invokerList.get(0), Constants.TIMEOUT, Constants.DEFAULT_NO_RESPONSE_TIMEOUT);
		final Invocation inv = invocation;
		
		if (forks <= 0 || forks >= invokerList.size()){
			selected = invokerList;
		} else {
			selected = new ArrayList<Invoker>();
			for (int i=0; i<forks; i++){
				// 在invoker列表（排除selected）后，如果没有选够，则存在重复循环问题，见select实现
				Invoker invoker = select(invokerList, selected);
				// 防止重复添加invoker
				if (!selected.contains(invoker)){
					selected.add(invoker);
				}
			}
		}
		
		final AtomicInteger count = new AtomicInteger();
		final BlockingQueue<Object> ref = new LinkedBlockingQueue<>();
		for (final Invoker invoker : selected){
			
			executor.execute(new Runnable(){

				@Override
				public void run() {
					try {
						Result result = invoker.invoke(inv);
						ref.offer(result);
					} catch (Exception e){
						int value = count.incrementAndGet();
						if (value >= selected.size()){
							ref.offer(e);
						}
					}
				}
				
			});
		}
		
		try {
			Object ret = ref.poll(timeout, TimeUnit.MICROSECONDS);
			if (ret instanceof Throwable){
				
				Throwable e = (Throwable)ret;
				throw new RpcException(
						"failed to forking invoke provider "
						+ selected
						+ ", but no luck to perform the invocation, last error is: "
						+ e.getMessage(),
						e.getCause() != null ? e.getCause() : e);
			}
			return (Result)ret;
		} catch (InterruptedException e){
			throw new RpcException("failed to forking invoke provider "
					+ selected
					+ ", but no luck to perform the invocation, last error is: "
					+ e.getMessage(),e);
		}
		
	}

}
