package net.chen.cloudatlas.crow.cluster.invoker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.cluster.log.ClusterLoggerProxy;
import net.chen.cloudatlas.crow.config.ReferenceConfig;
import net.chen.cloudatlas.crow.remote.RemoteException;
import net.chen.cloudatlas.crow.remote.Request;
import net.chen.cloudatlas.crow.remote.Response;
import net.chen.cloudatlas.crow.remote.support.crow.CrowResponse;
import net.chen.cloudatlas.crow.rpc.Invocation;
import net.chen.cloudatlas.crow.rpc.Invoker;
import net.chen.cloudatlas.crow.rpc.Result;
import net.chen.cloudatlas.crow.rpc.RpcException;
import net.chen.cloudatlas.crow.rpc.SubInvoker;

/**
 * <pre>
 * 1. 广播调用所有提供者，逐个调用，任意一台报错则报错.<br>
 * 2. 通常用于通知所有提供者更新缓存或日志等本地资源信息.
 * </pre>
 * @author chenn
 *
 */
public class BroadcastInvoker extends AbstractClusterInvoker{

	private static ThreadLocal<Map> resultMap = new ThreadLocal<>();
	
	private CountDownLatch latch;
	
	public BroadcastInvoker(List<Invoker> invokers){
		super(invokers);
	}
	
	public BroadcastInvoker(List<Invoker> invokers, ReferenceConfig rConfig){
		super(invokers,rConfig);
	}
	
	public static Map getResultMap(){
		Map map = resultMap.get();
		resultMap.remove();
		return map;
	}
	
	@Override
	protected Response doCall(Request request) throws RemoteException {
		
		List<Invoker> invokerList = getAllInvoker(dc);
		Map<String, Object> resultList = new HashMap<>();
		
		checkSubInvokers(invokerList);
		
		RemoteException exception = null;
		Response result = null;
		StringBuffer sb = new StringBuffer();
		int error = 0;
		
		for (Invoker invoker : invokerList){
			
			try {
				result = ((SubInvoker)invoker).call(request);
				if (result instanceof CrowResponse){
					resultList.put(invoker.getUrl().getHostAndPort(), ((CrowResponse)result).getResponseBytes());
				}
			} catch (RemoteException e){
				exception = e;
				resultList.put(invoker.getUrl().getHostAndPort(), exception);
				sb.append(invoker.getUrl().getHostAndPort() + ",");
				error++;
				Logger.warn("broadcast ignore exception: ",e);
				ClusterLoggerProxy.warn("broadcast ignore exception: " + e.getMessage());
			} catch (Exception e){
				exception = new RemoteException(e.getMessage(),e);
				sb.append(invoker.getUrl().getHostAndPort() + ",");
				error++;
				Logger.warn("broadcast ignore exception: ",e);
				ClusterLoggerProxy.warn("broadcast ignore exception: " + e.getMessage());
			}
		}
		
		resultMap.set(resultList);
		
		if (null != exception){
			throw new RemoteException("broadcast " + error + "/" + invokerList.size() + " error occured: " +sb.toString());
		}
		
		return result;
	}

	@Override
	protected void doAcall(Request request) throws RemoteException {
		
		List<Invoker> invokerList = getAllInvoker(dc);
		
		checkSubInvokers(invokerList);
		
		RemoteException exception = null;
		StringBuffer sb = new StringBuffer();
		int error = 0;
		
		for (Invoker invoker: invokerList){
			
			try {
				((SubInvoker)invoker).acall(request);
			} catch (RemoteException e){
				exception = e;
				sb.append(invoker.getUrl().getHostAndPort()+",");
				error++;
				Logger.warn("broadcast ignore exception: ",e);
				ClusterLoggerProxy.warn("broadcast ignore exception: " + e.getMessage());				
			} catch (Exception e){
				exception = new RemoteException(e.getMessage(),e);
				sb.append(invoker.getUrl().getHostAndPort()+",");
				error++;
				Logger.warn("broadcast ignore exception: ",e);
				ClusterLoggerProxy.warn("broadcast ignore exception: " + e.getMessage());				
			}
		}
		
		if (null != exception){
			throw new RemoteException("broadcast " + error + "/" + invokerList.size() + " error occured: " +sb.toString());
		}
	}

	@Override
	protected Result doInvoke(Invocation invocation) throws RpcException {
		
		List<Invoker> invokerList = getAllInvoker(dc);
		
		Map<String, Object> resultList = new HashMap<>();
		
		checkInvokers(invokerList);
		
		RpcException exception = null;
		Result result = null;
		StringBuffer sb = new StringBuffer();
		int error = 0;
		boolean isOneWay = false;
		
		for (Invoker invoker : invokerList){
			
			try {
				result = invoker.invoke(invocation);
				if (null != result.getValue()){
					resultList.put(invoker.getUrl().getHostAndPort(), result.getValue());
				} else if (null != result.getException()){
					resultList.put(invoker.getUrl().getHostAndPort(), result.getException());
				}
			} catch (Exception e){
				exception = new RpcException(e);
				resultList.put(invoker.getUrl().getHostAndPort(), exception);
				sb.append(invoker.getUrl().getHostAndPort()+",");
				error++;
				Logger.warn("broadcast ignore exception: ",e);
				ClusterLoggerProxy.warn("broadcast ignore exception: " + e.getMessage());
			}
			
			if (result.getValue() == null && result.getException() == null && exception == null){
				isOneWay = true;
			}
			
		}
		
		if (!isOneWay){
			resultMap.set(resultList);
		}
		
		if (null != exception){
			throw new RpcException("broadcast " + error + "/" + invokerList.size() + " error occured: " +sb.toString());
		}
		
		return result;
	}

}
