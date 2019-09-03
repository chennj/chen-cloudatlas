package net.chen.cloudatlas.crow.rpc.protocol;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.DcType;
import net.chen.cloudatlas.crow.common.Protocols;
import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.common.cluster.FailType;
import net.chen.cloudatlas.crow.config.ReferenceConfig;
import net.chen.cloudatlas.crow.config.RegistryConfig;
import net.chen.cloudatlas.crow.rpc.Invocation;
import net.chen.cloudatlas.crow.rpc.Invoker;
import net.chen.cloudatlas.crow.rpc.Protocol;
import net.chen.cloudatlas.crow.rpc.ProxyFactory;
import net.chen.cloudatlas.crow.rpc.Result;
import net.chen.cloudatlas.crow.rpc.proxy.JdkProxyFactory;

/**
 * 
 * @author chenn
 *
 */
public class ReferenceGet<T> {

	ReferenceConfig<T> config;
	
	private T ref;
	private Invoker<T> finalInvoker;
	private Protocol refProtocol;
	private ProxyFactory proxyFactory;
	
	public ReferenceGet(ReferenceConfig<T> config){
		this.config = config;
	}
	
	public ReferenceConfig<T> getReferenceConfig(){
		return this.config;
	}
	
	public synchronized T get(){
		
		if (null == ref){
			ref = createProxy();
		}
		return ref;
	}
	
	public synchronized T get(DcType dc){
		
		if (null == ref){
			ref = createProxy();
		}
		if (null != finalInvoker){
			finalInvoker.setDc(dc);
		}
		return ref;
	}
	
	private T createProxy(){
		
		proxyFactory = new JdkProxyFactory();
		Invoker<T> invoker = createInvoker();
		
		if (!hasZK()){
			
			try {
				// check if the invoker has any services available
				invoker.isAvailable();
			} catch (RuntimeException e){
				Logger.error("no service is available");
				return null;
			}
		}
		
		if (null == invoker){
			return null;
		}
		
		return (T)proxyFactory.getProxy(invoker);
	}
	
	private Invoker<T> createInvoker(){
		
		Invoker<T> invoker = null;
		try {
			// 后续考虑使用service loader机制，反射不优雅
			if ("rmi".equals(config.getProtocol().getCodec())){
				refProtocol = (Protocol)Class.forName("net.chen.cloudatlas.crow.rpc.rmi.RmiProtocol").newInstance();
				((AbstractProxyProtocol)refProtocol).setProxyFactory(proxyFactory);
				CountDownLatch latch = new CountDownLatch(1);
				invoker = refProtocol.refer(config.getInterface(), (URL)config.getURLs().get(0), latch);
			} else if (Protocols.CROW_RPC.equals(config.getProtocol().getCodec())){
				Class cls = Class.forName("net.chen.cloudatlas.crow.rpc.crow.CrowProtocol");
				Method method = cls.getMethod("getCrowProtocol", new Class[0]);
				Object obj = method.invoke(cls, new Object[0]);
				refProtocol = new ProtocolFilterWrapper((Protocol)obj);
				
				List<Invoker> invokers = new CopyOnWriteArrayList<Invoker>();
				List<URL> urls = config.getURLs();
				if (!checkURLs(urls, config)){
					return null;
				}
				
				CountDownLatch latch = new CountDownLatch(urls.size());
				for (URL url : urls){
					Invoker one = refProtocol.refer(config.getInterface(), url, latch);
					invokers.add(one);
				}
				
				if (Boolean.valueOf(
						System.getProperty(
								Constants.WAIT_ALL_URLS_CONNECTED,
								Constants.DEFAULT_WAIT_ALL_URLS_CONNECTED))){
					
					try {
						Logger.debug("DEFAULT_MAX_WAIT_RETRY_CONNECT_INTERVAL is "
								+ Constants.DEFAULT_MAX_WAIT_RETRY_CONNECT_INTERVAL
								+ "ms");
						boolean waitForLatch = latch
								.await(Constants.DEFAULT_MAX_WAIT_RETRY_CONNECT_INTERVAL, TimeUnit.MICROSECONDS);
						if (!waitForLatch){
							Logger.warn("Time on connecting to {} has elapsed {}ms, current thread no longer wait",
									urls,
									Constants.DEFAULT_MAX_WAIT_RETRY_CONNECT_INTERVAL);
						}
					} catch (InterruptedException e){
						throw new RuntimeException("the current waiting thread is interrupted while waiting for connecting to " + urls);
					}
				}
				
				FailType failStrategy = config.getFailStrategy();
				
				if (FailType.FAIL_OVER.equals(failStrategy)){
					invoker = (Invoker)Class.forName("net.chen.cloudatlas.crow.cluster.invoker.FailoverInvoker").getConstructor(
							List.class,
							ReferenceConfig.class).newInstance(invokers, config);
				} else if (FailType.FAIL_FAST.equals(failStrategy)){
					invoker = (Invoker)Class.forName("net.chen.cloudatlas.crow.cluster.invoker.FailfastInvoker").getConstructor(
							List.class,
							ReferenceConfig.class).newInstance(invokers, config);
				} else if (FailType.FAIL_BACK.equals(failStrategy)){
					invoker = (Invoker)Class.forName("net.chen.cloudatlas.crow.cluster.invoker.FailbackInvoker").getConstructor(
							List.class,
							ReferenceConfig.class).newInstance(invokers, config);
				} else if (FailType.FAIL_SAFE.equals(failStrategy)){
					invoker = (Invoker)Class.forName("net.chen.cloudatlas.crow.cluster.invoker.FailsafeInvoker").getConstructor(
							List.class,
							ReferenceConfig.class).newInstance(invokers, config);
				} else if (FailType.FORKING.equals(failStrategy)){
					invoker = (Invoker)Class.forName("net.chen.cloudatlas.crow.cluster.invoker.ForkingInvoker").getConstructor(
							List.class,
							ReferenceConfig.class).newInstance(invokers, config);
				} else if (FailType.BROADCAST.equals(failStrategy)){
					invoker = (Invoker)Class.forName("net.chen.cloudatlas.crow.cluster.invoker.BroadcastInvoker").getConstructor(
							List.class,
							ReferenceConfig.class).newInstance(invokers, config);
				}
			}
		} catch (Exception e){
			throw new RuntimeException("exception occurs while creating proxy", e);
		}
		
		this.finalInvoker = invoker;
		if (null == this.finalInvoker){
			this.finalInvoker.setInterface(config.getInterface());
		}
		
		return invoker;
	}
	
	public static boolean checkURLs(List<URL> urls, ReferenceConfig rConfig){
		
		for (URL url : urls){
			if ((Boolean)rConfig.getDcStrategy().get(DcType.fromString(url.getParameter(Constants.DC)))){
				return true;
			}
		}
		return false;
	}
	
	public Invoker<T> getFinalInvoker(){
		return finalInvoker;
	}
	
	public void  insertInvoker(URL url){
		Invoker invoker = refProtocol.refer(config.getInterface(), url, null);
		this.finalInvoker.insertInvoker(invoker);
	}
	
	public void deleteInvoker(URL url){
		
		Invoker invoker = new AbstractInvoker(config.getInterface(),url){

			@Override
			protected Result doInvoke(Invocation invocation) throws Exception {
				return null;
			}
			
		};
		this.finalInvoker.deleteInvoker(invoker);
	
	}
	
	public boolean hasZK(){
		
		final RegistryConfig rConfig = config.getRegistryConfig();
		boolean hasZK = rConfig != null && rConfig.getAddress() != null && !rConfig.getAddress().trim().isEmpty();
		return hasZK;
	}
}
