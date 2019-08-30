package net.chen.cloudatlas.crow.cluster.invoker;

import java.util.List;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.cluster.log.ClusterLoggerProxy;
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
 * 	1、失败安全，出现异常时，直接忽略 
 * 	2、通常用于写入审计日志等操作
 * </pre>
 * @author chenn
 *
 */
public class FailsafeInvoker extends AbstractClusterInvoker{

	public FailsafeInvoker(List<Invoker> invokers){
		super(invokers);
	}
	
	public FailsafeInvoker(List<Invoker> invokers, ReferenceConfig rConfig){
		super(invokers, rConfig);
	}
	
	@Override
	protected Response doCall(Request request) throws RemoteException {
		
		try {
			// 得到可用的url，去掉不通的
			List<Invoker> invokerList = getActiveInvoker(dc);
			checkSubInvokers(invokerList);
			Invoker invoker = select(invokerList, null);
			return ((SubInvoker)invoker).call(request);
		} catch (Exception e){
			Logger.error("failsafe ignore exception:",e);
			ClusterLoggerProxy.error("failsafe ignore exception:"+e.getMessage());
			return null;
		}
				
	}

	@Override
	protected void doAcall(Request request) throws RemoteException {
		
		try {
			// 得到可用的url，去掉不通的
			List<Invoker> invokerList = getActiveInvoker(dc);
			checkSubInvokers(invokerList);
			Invoker invoker = select(invokerList, null);
			((SubInvoker)invoker).acall(request);
		} catch (Exception e){
			Logger.error("failsafe ignore exception:",e);
			ClusterLoggerProxy.error("failsafe ignore exception:"+e.getMessage());
		}
		
	}

	@Override
	protected Result doInvoke(Invocation invocation) throws RpcException {
		
		try {
			// 得到可用的url，去掉不通的
			List<Invoker> invokerList = getActiveInvoker(dc);
			checkInvokers(invokerList);
			Invoker invoker = select(invokerList, null);
			return invoker.invoke(invocation);
		} catch (Exception e){
			Logger.error("failsafe ignore exception:",e);
			ClusterLoggerProxy.error("failsafe ignore exception:"+e.getMessage());
			return new RpcResult();
		}
		
	}

}
