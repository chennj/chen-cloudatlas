package net.chen.cloudatlas.crow.cluster.invoker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.common.Constants;
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

/**
 * <pre>
 * 	1、失败自动切换，重试其他服务器。（缺省）
 * 	2、通常用于读操作，但重试带来更长延迟
 * 	3、可通过retries=”2“来设置重试次数（不含第一次）
 * </pre>
 * @author chenn
 *
 */
public class FailoverInvoker extends AbstractClusterInvoker{

	public FailoverInvoker(List<Invoker> invokers){
		super(invokers);
	}
	
	public FailoverInvoker(List<Invoker> invokers, ReferenceConfig rConfig){
		super(invokers, rConfig);
	}
	
	@Override
	protected Response doCall(Request request) throws RemoteException {
		
		// 得到可用的url，去掉不通的
		List<Invoker> invokerList = this.getActiveInvoker(dc);
		
		this.checkSubInvokers(invokerList);
		
		int retries = getParameter(invokerList.get(0), Constants.RETRIES, Constants.DEFAULT_RETRIES) + 1;
		if (retries < 1){
			retries = 1;
		}
		
		RemoteException exception = null;
		Response result = null;
		
		List<Invoker> invoked = new ArrayList<Invoker>(invokerList.size());
		Set<String> providers = new HashSet<>(retries);
		
		int tried = 0;
		
		for (int i=0; i<retries; i++){
			
			tried++;
			
			Invoker invoker = select(invokerList, invoked);
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
				return result;
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

		// 最后都没成功，抛出异常
		throw new RemoteException("failed to invoke in the service "
				+ invokerList.get(0).getUrl().getPath()
				+ ". Tried" + tried + " times of the providers "
				+ providers
				+ "(" + providers.size() + "/" + invokerList.size() + ") "
				+ "on the consumer " + NetUtil.getLocalHost() + " using the crow version "
				+ Version.getPrettyString() + ". last error is:"
				+ (exception != null ? exception.getMessage() : ""),
				exception != null && exception.getCause() != null ? exception.getCause() : exception);

	}

	@Override
	protected void doAcall(Request request) throws RemoteException {
		
		List<Invoker> invokerList = getActiveInvoker(dc);
		
		checkSubInvokers(invokerList);
		
		int retries = getParameter(invokerList.get(0), Constants.RETRIES, Constants.DEFAULT_RETRIES);
		if (retries < 1){
			retries = 0;
		}
		
		RemoteException exception = null;
		List<Invoker> invoked = new ArrayList<>(invokerList.size());
		Set<String> providers = new HashSet<>(retries);
		
		int tried = 0;
		
		for (int i=0; i<retries; i++){
			
			tried++;
			
			Invoker invoker = select(invokerList, invoked);
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
				return;				
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
		
		// 最后都没成功，抛出异常
		throw new RemoteException("failed to invoke in the service "
				+ invokerList.get(0).getUrl().getPath()
				+ ". Tried" + tried + " times of the providers "
				+ providers
				+ "(" + providers.size() + "/" + invokerList.size() + ") "
				+ "on the consumer " + NetUtil.getLocalHost() + " using the crow version "
				+ Version.getPrettyString() + ". last error is:"
				+ (exception != null ? exception.getMessage() : ""),
				exception != null && exception.getCause() != null ? exception.getCause() : exception);

	}

	@Override
	protected Result doInvoke(Invocation invocation) throws RpcException {
		
		// 得到可用的url，去掉不通的
		List<Invoker> invokerList = this.getActiveInvoker(dc);
		
		this.checkInvokers(invokerList);
		
		int retries = getParameter(invokerList.get(0), Constants.RETRIES, Constants.DEFAULT_RETRIES) + 1;
		if (retries < 1){
			retries = 1;
		}
		
		RpcException exception = null;
		Result result = null;
		
		List<Invoker> invoked = new ArrayList<Invoker>(invokerList.size());
		Set<String> providers = new HashSet<>(retries);
		
		int tried = 0;
		
		for (int i=0; i<retries; i++){
			
			tried++;
			
			Invoker invoker = select(invokerList, invoked);
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
				return result;
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

		// 最后都没成功，抛出异常
		throw new RpcException("failed to invoke in the service "
				+ invokerList.get(0).getUrl().getPath()
				+ ". Tried" + tried + " times of the providers "
				+ providers
				+ "(" + providers.size() + "/" + invokerList.size() + ") "
				+ "on the consumer " + NetUtil.getLocalHost() + " using the crow version "
				+ Version.getPrettyString() + ". last error is:"
				+ (exception != null ? exception.getMessage() : ""),
				exception != null && exception.getCause() != null ? exception.getCause() : exception);


	}

}
