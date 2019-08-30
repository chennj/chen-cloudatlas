package net.chen.cloudatlas.crow.cluster.invoker;

import java.util.List;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.cluster.log.ClusterLoggerProxy;
import net.chen.cloudatlas.crow.common.Version;
import net.chen.cloudatlas.crow.common.utils.NetUtil;
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
 * 		1、快速失败，只发起一次调用，失败后立即报错
 * 		2、通常用于非幂等性的写操作，比如新增记录
 * </pre>
 * @author chenn
 *
 */
public class FailfastInvoker extends AbstractClusterInvoker{

	public FailfastInvoker(List<Invoker> invokers){
		super(invokers);
	}
	
	public FailfastInvoker(List<Invoker> invokers,ReferenceConfig rConfig){
		super(invokers,rConfig);
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
			Logger.error("failfast invoke failed on consumer :",e);
			throw new RemoteException("failfast invoke failed on consumer " 
					+ NetUtil.getLocalHost() + " use crow version " + Version.getPrettyString()
					+ ". last error is: " + e.getMessage(), e.getCause() != null ? e.getCause() : e);
		}

	}

	@Override
	protected void doAcall(Request request) throws RemoteException {
		
		try {
			// 得到可用的url列表，去掉 不通的
			List<Invoker> invokerList = getActiveInvoker(dc);
			
			checkSubInvokers(invokerList);
			
			Invoker invoker = select(invokerList,null);
			((SubInvoker)invoker).call(request);
		} catch (Exception e){
			Logger.error("failfast async invoke failed on consumer :",e);
			throw new RemoteException("failfast async invoke failed on consumer " 
					+ NetUtil.getLocalHost() + " use crow version " + Version.getPrettyString()
					+ ". last error is: " + e.getMessage(), e.getCause() != null ? e.getCause() : e);
		}

	}

	@Override
	protected Result doInvoke(Invocation invocation) throws RpcException {

		try {
			// 得到可用的url列表，去掉 不通的
			List<Invoker> invokerList = getActiveInvoker(dc);
			
			checkInvokers(invokerList);
			
			Invoker invoker = select(invokerList,null);
			return invoker.invoke(invocation);
		} catch (Exception e){
			Logger.error("failfast invoke failed on consumer :",e);
			throw new RpcException("failfast invoke failed on consumer " 
					+ NetUtil.getLocalHost() + " use crow version " + Version.getPrettyString()
					+ ". last error is: " + e.getMessage(), e.getCause() != null ? e.getCause() : e);
		}

	}

}
