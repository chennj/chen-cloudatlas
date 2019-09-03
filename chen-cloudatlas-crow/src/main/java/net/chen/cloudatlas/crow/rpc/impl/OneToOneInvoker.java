package net.chen.cloudatlas.crow.rpc.impl;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.common.DcType;
import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.remote.ChannelRegistry;
import net.chen.cloudatlas.crow.remote.RemoteException;
import net.chen.cloudatlas.crow.remote.Request;
import net.chen.cloudatlas.crow.remote.Response;
import net.chen.cloudatlas.crow.remote.impl.NettyClient;
import net.chen.cloudatlas.crow.rpc.Invocation;
import net.chen.cloudatlas.crow.rpc.Invoker;
import net.chen.cloudatlas.crow.rpc.Result;
import net.chen.cloudatlas.crow.rpc.RpcException;
import net.chen.cloudatlas.crow.rpc.SubInvoker;
import net.chen.cloudatlas.crow.rpc.utils.ProtocolUtil;

/**
 * <pre>
 * 默认直连发送
 * </pre>
 * @author chenn
 *
 */
public class OneToOneInvoker implements SubInvoker{

	private String serviceId;
	private URL url;
	private String ipAndPort;
	
	protected OneToOneInvoker(){}
	
	public OneToOneInvoker(URL url){
		this("null", url);
	}
	
	public OneToOneInvoker(String serviceId, URL url){
		this.serviceId = serviceId;
		this.url = url;
		this.ipAndPort = url.getHostAndPort();
	}
	
	public String getServiceId(){
		return this.serviceId;
	}
	
	@Override
	public boolean isAvailable() {
		return ChannelRegistry.isChannelAvailable(this.ipAndPort);
	}

	@Override
	public Class getInterface() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result invoke(Invocation invocation) throws RpcException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URL getUrl() {
		return url;
	}

	@Override
	public void insertInvoker(Invoker invoker) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteInvoker(Invoker invoker) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setInterface(Class interfaceClass) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDc(DcType dc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getInvokeKey() {
		return ProtocolUtil.invokeKey(url);
	}

	@Override
	public Response call(Request request) throws RemoteException {	
		// 计算花费时间
		long start = System.currentTimeMillis();
		Response result = NettyClient.getClient(url).sendWithResult(request);
		long end = System.currentTimeMillis();
		Logger.debug("crow frameowrk took {} milliseconds to [call] this message to {}",(end-start),this.ipAndPort);
		return result;
	}

	@Override
	public void acall(Request request) throws RemoteException {
		// 计算花费时间
		long start = System.currentTimeMillis();
		NettyClient.getClient(url).send(request);
		long end = System.currentTimeMillis();
		Logger.debug("crow frameowrk took {} milliseconds to [call] this message to {}",(end-start),this.ipAndPort);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		
		if (null == obj){
			return false;
		} else if (obj == this){
			return true;
		} else if (obj instanceof OneToOneInvoker){
			return getInvokeKey().equals(((OneToOneInvoker)obj).getInvokeKey());
		}
		return false;
	}

	@Override
	public String toString() {
		return getServiceId() + " -> " + (getUrl()==null ? "" : getUrl().toString());
	}

	
}
