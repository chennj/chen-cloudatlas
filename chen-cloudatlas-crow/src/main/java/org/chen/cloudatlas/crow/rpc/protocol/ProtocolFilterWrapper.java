package org.chen.cloudatlas.crow.rpc.protocol;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.chen.cloudatlas.crow.common.Constants;
import org.chen.cloudatlas.crow.common.DcType;
import org.chen.cloudatlas.crow.common.URL;
import org.chen.cloudatlas.crow.common.utils.NameableServiceLoader;
import org.chen.cloudatlas.crow.rpc.Exporter;
import org.chen.cloudatlas.crow.rpc.Filter;
import org.chen.cloudatlas.crow.rpc.Invocation;
import org.chen.cloudatlas.crow.rpc.Invoker;
import org.chen.cloudatlas.crow.rpc.Protocol;
import org.chen.cloudatlas.crow.rpc.Result;
import org.chen.cloudatlas.crow.rpc.RpcException;

/**
 * Wrapper模式，在protocol的invoke动作基础上加入filter chain;
 * @author chenn
 *
 */
public class ProtocolFilterWrapper implements Protocol{

	private List<Filter> consumerFilterMap = NameableServiceLoader.getLoader(Filter.class).getActiveServices(Constants.CONSUMER);
	
	private List<Filter> providerFilterMap = NameableServiceLoader.getLoader(Filter.class).getActiveServices(Constants.PROVIDER);
	
	private Protocol protocol;
	
	public ProtocolFilterWrapper(Protocol protocol){
		if (null == protocol){
			throw new IllegalArgumentException("protocol is null");
		}
		this.protocol = protocol;
	}
	
	@Override
	public String getName() {
		return protocol.getName();
	}

	@Override
	public int getDefaultPort() {
		return protocol.getDefaultPort();
	}

	@Override
	public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
		return protocol.export(buildFilterChain(invoker,false));
	}

	@Override
	public <T> Invoker<T> refer(Class<T> type, URL url, CountDownLatch latch) throws RpcException {
		final Invoker<T> invoker = protocol.refer(type, url, latch);
		return buildFilterChain(invoker, true);
	}

	public <T> Invoker<T> buildFilterChain(final Invoker<T> invoker, boolean isConsumer) {
		
		List<Filter> list = isConsumer ? consumerFilterMap : providerFilterMap;
		Invoker<T> last = invoker;
		
		for (final Filter filter : list){
			
			final Invoker<T> next = last;
			last = new Invoker<T>(){

				@Override
				public boolean isAvailable() {
					return invoker.isAvailable();
				}

				@Override
				public Class<T> getInterface() {
					return invoker.getInterface();
				}

				@Override
				public Result invoke(Invocation invocation) throws RpcException {
					return filter.doFilter(next, invocation);
				}

				@Override
				public URL getUrl() {
					return invoker.getUrl();
				}

				@Override
				public void insertInvoker(Invoker<?> inv) {
					invoker.insertInvoker(inv);
				}

				@Override
				public void deleteInvoker(Invoker<?> inv) {
					invoker.deleteInvoker(inv);
				}

				@Override
				public void setInterface(Class<T> interfaceClass) {
					invoker.setInterface(interfaceClass);
				}

				@Override
				public void destroy() {
					invoker.destroy();
				}

				@Override
				public void setDc(DcType dc) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public String getInvokeKey() {
					return invoker.getInvokeKey();
				}

				@Override
				public int hashCode() {
					return super.hashCode();
				}

				@Override
				public boolean equals(Object obj) {
					return super.equals(obj);
				}
				
				
			};
		}
		
		return last;
	}

	@Override
	public void destroy() {
		protocol.destroy();
	}

}
