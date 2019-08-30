package net.chen.cloudatlas.crow.cluster.invoker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.cluster.log.ClusterLoggerProxy;
import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.DcType;
import net.chen.cloudatlas.crow.common.Version;
import net.chen.cloudatlas.crow.common.utils.NetUtil;
import net.chen.cloudatlas.crow.config.ReferenceConfig;
import net.chen.cloudatlas.crow.remote.RemoteException;
import net.chen.cloudatlas.crow.remote.Request;
import net.chen.cloudatlas.crow.remote.Response;
import net.chen.cloudatlas.crow.remote.TimeoutException;
import net.chen.cloudatlas.crow.rpc.Invocation;
import net.chen.cloudatlas.crow.rpc.Invoker;
import net.chen.cloudatlas.crow.rpc.Result;
import net.chen.cloudatlas.crow.rpc.RpcException;
import net.chen.cloudatlas.crow.rpc.SubInvoker;
import net.chen.cloudatlas.crow.rpc.impl.RpcResult;

/**
 * <pre>
 * 	1、为monitor定制的invoker
 * 	2、用于分组发送，只随机发送一个
 * </pre>
 * @author chenn
 *
 */
public class GroupInvoker extends AbstractClusterInvoker{

	public GroupInvoker(List<Invoker> invokers){
		super(invokers);
	}
	
	public GroupInvoker(List<Invoker> invokers, ReferenceConfig rConfig){
		super(invokers, rConfig);
	}
	
	@Override
	protected Response doCall(Request request) throws RemoteException {
		
		// 得到可用的url，去掉不通的
		Map<DcType, List<Invoker>> groupMap = getActiveInvokerGroup(invokers);
		if (null == groupMap || groupMap.isEmpty()){
			throw new RemoteException("no service in any group");
		}
		
		Iterator it = groupMap.entrySet().iterator();
		Response result = null;
		RemoteException exception = null;
		
		while(it.hasNext()){
			
			List<Invoker> invokerList = (List<Invoker>)((Map.Entry)it.next()).getValue();
			List<Invoker> copyurls = invokerList;
			checkInvokers(copyurls);
			
			// 为monitor定制的，重试次数 为1次
			int retries = getParameter(invokerList.get(0), Constants.RETRIES, Constants.DEFAULT_RETRIES);
			if (retries <= 0){
				retries = 1;
			}
			
			List<Invoker> invoked = new ArrayList<Invoker>(copyurls.size());
			Set<String> providers = new HashSet<String>(retries);
			
			int tried = 0;
			
			for (int i=0; i<retries; i++){
				
				tried++;
				
				Invoker invoker = select(copyurls, invoked);
				invoked.add(invoker);
				
				try {
					result = ((SubInvoker)invoker).call(request);
					if (null != exception){
						Logger.warn("although retry  the service {} was successful by the provider {}, "
								+ "but there have been failed providers {} ({}/{}) on the consumer {} "
								+ "using the crow version {}. last error is: {}",
								invoker.getUrl().getPath(),
								invoker.getUrl().getHostAndPort(),
								providers,
								providers.size(),
								invokerList.size(),
								NetUtil.getLocalHost(),
								Version.getPrettyString(),
								exception.getMessage());
					}
				} catch (RemoteException e){
					Logger.error("failover invoke failed on consumer:",e);
					exception = e;
					
					// 如果超时，则不再重试，直接抛出错误，让应用自行处理
					if (e instanceof TimeoutException){
						Logger.debug("exception is {}, fast fail",TimeoutException.class.getSimpleName());
						break;
					}
				} catch (Exception e){
					Logger.error("failover invoke failed on consumer:",e);
					exception = new RemoteException(e.getMessage(),e);
				} finally{
					providers.add(invoker.getUrl().getHostAndPort());
				}
			}
		}
		
		if (null == result){
			throw new RemoteException("failed to invoke any the service in any group "
					+ "using the crow version"
					+ Version.getPrettyString() + ". last error is:"
					+ (exception != null ? exception.getMessage() : ""),
					exception != null && exception.getCause() != null ? exception.getCause() : exception);
		}
		
		// 每个组都要发，不需要结果
		return result;
	}

	@Override
	protected void doAcall(Request request) throws RemoteException {
		
		// 得到可用的url，去掉不通的
		Map<DcType, List<Invoker>> groupMap = getActiveInvokerGroup(invokers);
		if (null == groupMap || groupMap.isEmpty()){
			throw new RemoteException("no service in any group");
		}
		
		Iterator it = groupMap.entrySet().iterator();
		boolean hasResult = false;
		RemoteException exception = null;
		
		while(it.hasNext()){
			
			List<Invoker> invokerList = (List<Invoker>)((Map.Entry)it.next()).getValue();
			List<Invoker> copyurls = invokerList;
			checkInvokers(copyurls);
			
			// 为monitor定制的，重试次数 为1次
			int retries = getParameter(invokerList.get(0), Constants.RETRIES, Constants.DEFAULT_RETRIES);
			if (retries <= 0){
				retries = 1;
			}
			
			List<Invoker> invoked = new ArrayList<Invoker>(copyurls.size());
			Set<String> providers = new HashSet<String>(retries);
			
			int tried = 0;
			
			for (int i=0; i<retries; i++){
				
				tried++;
				
				Invoker invoker = select(copyurls, invoked);
				invoked.add(invoker);
				
				try {
					((SubInvoker)invoker).acall(request);
					if (null != exception){
						Logger.warn("although retry  the service {} was successful by the provider {}, "
								+ "but there have been failed providers {} ({}/{}) on the consumer {} "
								+ "using the crow version {}. last error is: {}",
								invoker.getUrl().getPath(),
								invoker.getUrl().getHostAndPort(),
								providers,
								providers.size(),
								invokerList.size(),
								NetUtil.getLocalHost(),
								Version.getPrettyString(),
								exception.getMessage());
					}
					hasResult = true;
				} catch (RemoteException e){
					Logger.error("failover invoke failed on consumer:",e);
					exception = e;
					
					// 如果超时，则不再重试，直接抛出错误，让应用自行处理
					if (e instanceof TimeoutException){
						Logger.debug("exception is {}, fast fail",TimeoutException.class.getSimpleName());
						break;
					}
				} catch (Exception e){
					Logger.error("failover invoke failed on consumer:",e);
					exception = new RemoteException(e.getMessage(),e);
				} finally{
					providers.add(invoker.getUrl().getHostAndPort());
				}
			}
		}
		
		if (!hasResult){
			throw new RemoteException("failed to invoke any the service in any group "
					+ "using the crow version"
					+ Version.getPrettyString() + ". last error is:"
					+ (exception != null ? exception.getMessage() : ""),
					exception != null && exception.getCause() != null ? exception.getCause() : exception);
		}
		
	}

	@Override
	protected Result doInvoke(Invocation invocation) throws RpcException {
		
		// 得到可用的url，去掉不通的
		Map<DcType, List<Invoker>> groupMap = getActiveInvokerGroup(invokers);
		if (null == groupMap || groupMap.isEmpty()){
			Logger.error("no service in any group");
			return new RpcResult();
		}
		
		Iterator it = groupMap.entrySet().iterator();
		Result result = null;
		RpcException exception = null;
		
		while(it.hasNext()){
			
			List<Invoker> invokerList = (List<Invoker>)((Map.Entry)it.next()).getValue();
			List<Invoker> copyurls = invokerList;
			checkInvokers(copyurls);
			
			// 为monitor定制的，重试次数 为1次
			int retries = getParameter(invokerList.get(0), Constants.RETRIES, Constants.DEFAULT_RETRIES);
			if (retries <= 0){
				retries = 1;
			}
			
			List<Invoker> invoked = new ArrayList<Invoker>(copyurls.size());
			Set<String> providers = new HashSet<String>(retries);
			
			int tried = 0;
			
			for (int i=0; i<retries; i++){
				
				tried++;
				
				Invoker invoker = select(copyurls, invoked);
				invoked.add(invoker);
				
				try {
					result = invoker.invoke(invocation);
					if (null != exception){
						Logger.warn("although retry  the service {} was successful by the provider {}, "
								+ "but there have been failed providers {} ({}/{}) on the consumer {} "
								+ "using the crow version {}. last error is: {}",
								invoker.getUrl().getPath(),
								invoker.getUrl().getHostAndPort(),
								providers,
								providers.size(),
								invokerList.size(),
								NetUtil.getLocalHost(),
								Version.getPrettyString(),
								exception.getMessage());
					}
				} catch (RpcException e){
					Logger.error("failover invoke failed on consumer:",e);
					exception = e;
					
					// 如果超时，则不再重试，直接抛出错误，让应用自行处理
					if (e.getCause() instanceof TimeoutException){
						Logger.debug("exception is {}, fast fail",TimeoutException.class.getSimpleName());
						break;
					}
				} catch (Exception e){
					Logger.error("failover invoke failed on consumer:",e);
					exception = new RpcException(e.getMessage(),e);
				} finally{
					providers.add(invoker.getUrl().getHostAndPort());
				}
			}
		}
		
		if (null == result){
			throw new RpcException("failed to invoke any the service in any group "
					+ "using the crow version"
					+ Version.getPrettyString() + ". last error is:"
					+ (exception != null ? exception.getMessage() : ""),
					exception != null && exception.getCause() != null ? exception.getCause() : exception);
		}
		
		// 每个组都要发，不需要结果
		result = new RpcResult();
		return result;
		
	}

}
