package net.chen.cloudatlas.crow.client.impl;

import org.tinylog.Logger;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

import net.chen.cloudatlas.crow.common.cluster.FailType;
import net.chen.cloudatlas.crow.config.ReferenceConfig;
import net.chen.cloudatlas.crow.rpc.SubInvoker;

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
}
