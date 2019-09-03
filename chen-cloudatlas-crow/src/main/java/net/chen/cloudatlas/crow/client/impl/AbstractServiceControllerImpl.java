package net.chen.cloudatlas.crow.client.impl;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.common.cluster.FailType;
import net.chen.cloudatlas.crow.config.ReferenceConfig;
import net.chen.cloudatlas.crow.rpc.Invoker;
import net.chen.cloudatlas.crow.rpc.SubInvoker;
import net.chen.cloudatlas.crow.rpc.impl.OneToOneInvoker;

public abstract class AbstractServiceControllerImpl {

	private SubInvoker subInvoker;
	
	protected abstract ReferenceConfig getReferenceConfig();
	
	private volatile boolean urlsModified = false;
	
	public SubInvoker getClusterInvoker(){
		
		if (null != subInvoker){
			return subInvoker;
		}
		
		FailType failStrategy = getReferenceConfig().getFailStrategy();
		
		try {
			// class name rule is failtype name plus "Invoker"
			String className = "net.chen.cloudatlas.crow.cluster.invoker." + getFailTypeClassName(failStrategy) + "Invoker";
			Logger.debug("using failStrategy: "+className);
			subInvoker = (SubInvoker)Class.forName(className).getConstructor(
					List.class,ReferenceConfig.class)
					.newInstance(getInvokers(),getReferenceConfig());
		} catch (Exception e){
			Logger.error("can not find specific invoker implementsation",e);
		}
		
		return subInvoker;
	}
	
	public List<Invoker> getInvokers(){
		
		List<URL> urls = getReferenceConfig().getURLs();
		String serviceId = getReferenceConfig().getServiceId();
		List<Invoker> invokers = new CopyOnWriteArrayList<Invoker>();
		
		for (URL url : urls){
			invokers.add(new OneToOneInvokerWrapper(new OneToOneInvoker(serviceId,url)));
		}
		
		return invokers;
	}
	
	public void insertInvoker(URL url){
		String serviceId = getReferenceConfig().getServiceId();
		subInvoker.insertInvoker(new OneToOneInvokerWrapper(new OneToOneInvoker(serviceId,url)));
	}
	
	public void deleteInvoker(URL url){
		String serviceId = getReferenceConfig().getServiceId();
		subInvoker.deleteInvoker(new OneToOneInvokerWrapper(new OneToOneInvoker(serviceId,url)));
	}
	
	private String getFailTypeClassName(FailType failStrategy){
		String strategyName = failStrategy.getText();
		return strategyName.substring(0,1).toUpperCase()+strategyName.substring(1);
	}

	public boolean isUrlsModified() {
		return urlsModified;
	}

	public void setUrlsModified(boolean urlsModified) {
		this.urlsModified = urlsModified;
	}
	
}
