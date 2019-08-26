package net.chen.cloudatlas.crow.rpc.protocol;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.tinylog.Logger;

import net.chen.cloudatlas.crow.common.DcType;
import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.remote.ChannelRegistry;
import net.chen.cloudatlas.crow.rpc.Invocation;
import net.chen.cloudatlas.crow.rpc.Invoker;
import net.chen.cloudatlas.crow.rpc.Result;
import net.chen.cloudatlas.crow.rpc.RpcException;
import net.chen.cloudatlas.crow.rpc.impl.RpcInvocation;
import net.chen.cloudatlas.crow.rpc.impl.RpcResult;
import net.chen.cloudatlas.crow.rpc.utils.ProtocolUtil;

public abstract class AbstractInvoker<T> implements Invoker<T> {

	private final Class<T> type;
	
	private final URL url;
	
	private final Map<String, String> attachment;
	
	private volatile boolean available = true;
	
	private volatile boolean destroyed = false;
	
	public AbstractInvoker(Class<T> type, URL url){
		this(type, url, (Map<String, String>)null);
	}

	public AbstractInvoker(Class<T> type, URL url, String[] keys){
		this(type, url, convertAttachment(url, keys));
	}

	public AbstractInvoker(Class<T> type, URL url, Map<String, String> attachment) {
		
		if (null == type){
			throw new IllegalArgumentException("service type is null");
		}
		
		if (null == url){
			throw new IllegalArgumentException("service url is null");
		}
		
		this.type = type;
		this.url = url;
		this.attachment = attachment == null ? null : Collections.unmodifiableMap(attachment);
	}
	
	private static Map<String, String> convertAttachment(URL url, String[] keys) {
		
		if (null == keys || keys.length == 0){
			return null;
		}
		
		Map<String, String> attachment = new HashMap<>();
		for (String key : keys){
			String value = url.getParameter(key);
			if (StringUtils.isEmpty(value)){
				attachment.put(key, value);
			}
		}
		
		return attachment;
	}
	
	public Result invoke(Invocation inv) throws RpcException{
		
		if (destroyed){
			throw new RpcException("Rpc invoker for service "
					+ this + " on consumer "
					+ " use crow is destroyed, can not be invoked any more!");
		}
		
		RpcInvocation invocation = (RpcInvocation)inv;
		invocation.setInvoker(this);
		if (null != attachment && !attachment.isEmpty()){
			invocation.addAttachmentsIfAbsent(attachment);
		}
		
		try {
			return doInvoke(invocation);
		} catch (InvocationTargetException e){
			Logger.error("InvocationTargetException while invoking ", e);
			Throwable t = e.getTargetException();
			if (null == t){
				return new RpcResult(e);
			} else {
				if (t instanceof RpcException){
					((RpcException)t).setCode(RpcException.BIZ_EXCEPTION);
				}
				return new RpcResult(t);
			}
		} catch (RpcException e){
			Logger.error("RpcException while invoking ",e);
			if (e.isBiz()){
				return new RpcResult(e);
			} else {
				throw e;
			}
		} catch (Exception e){
			Logger.error("Exception while invoking ", e);
			return new RpcResult(e);
		}
	}
	
	protected abstract Result doInvoke(Invocation invocation) throws Exception;

	@Override
	public boolean isAvailable() {
		available = ChannelRegistry.isChannelAvailable(url.getHostAndPort());
		return available;
	}
	
	public void  setAvailable(boolean available){
		this.available = available;
	}

	@Override
	public Class<T> getInterface() {
		return type;
	}

	@Override
	public URL getUrl() {
		return url;
	}

	@Override
	public void insertInvoker(Invoker<?> invoker) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteInvoker(Invoker<?> invoker) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setInterface(Class<T> interfaceClass) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destroy() {

		if (isDestroyed()){
			return;
		}
		destroyed = true;
		setAvailable(false);
	}
	
	public boolean isDestroyed(){
		return destroyed;
	}

	@Override
	public void setDc(DcType dc) {
		
	}

	@Override
	public String getInvokeKey() {
		return ProtocolUtil.invokeKey(url);
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
		} else if (obj instanceof Invoker){
			return getInvokeKey().equals(((Invoker)obj).getInvokeKey());
		}
		return false;
	}

	@Override
	public String toString() {
		return getInterface() + " -> " + (getUrl()==null?"":getUrl().toString());
	}
	
}
