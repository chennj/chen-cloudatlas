package net.chen.cloudatlas.crow.config.spring.schema;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.ReaderContext;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.io.Resource;
import org.springframework.util.xml.DomUtils;
import org.tinylog.Logger;
import org.w3c.dom.Element;

import net.chen.cloudatlas.crow.common.CompressAlgorithmType;
import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.DcType;
import net.chen.cloudatlas.crow.common.SerializationType;
import net.chen.cloudatlas.crow.common.ThrottleType;
import net.chen.cloudatlas.crow.common.cluster.FailType;
import net.chen.cloudatlas.crow.common.cluster.LoadBalanceType;
import net.chen.cloudatlas.crow.common.exception.ConfigInvalidException;
import net.chen.cloudatlas.crow.config.ApplicationConfig;
import net.chen.cloudatlas.crow.config.MonitorConfig;
import net.chen.cloudatlas.crow.config.ProtocolConfig;
import net.chen.cloudatlas.crow.config.ReferenceConfig;
import net.chen.cloudatlas.crow.config.RegistryConfig;
import net.chen.cloudatlas.crow.config.ServiceConfig;
import net.chen.cloudatlas.crow.config.spring.config.CrowConfigSpringImpl;
import net.chen.cloudatlas.crow.config.utils.CrowConfigParser;

public class CrowDefinitionParser extends AbstractBeanDefinitionParser{

	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		
		BeanDefinitionBuilder  parent = BeanDefinitionBuilder.rootBeanDefinition(CrowConfigSpringImpl.class);
		
		parent.setScope(BeanDefinition.SCOPE_SINGLETON);
		parent.setLazyInit(false);
		
		parseChildApplication(element, parent, parserContext);
		parseChildRegistry(element, parent, parserContext);
		parseChildMonitor(element, parent, parserContext);
		parseChildProtocols(element, parserContext);
		parseChildServices(element, parserContext);
		parseChildReferences(element, parserContext);
		
		ReaderContext readerContext = parserContext.getReaderContext();
		Resource resource = readerContext.getResource();
		if (resource.getFilename().equals(Constants.DEFAULT_CROW_CONFIG_FILE_NAME)){
			
			try {
				if (!CrowConfigParser.getSpringConfigStore().contains(resource.getURL())){
					CrowConfigParser.getSpringConfigStore().add(resource.getURL());
				}
			} catch (IOException e){
				Logger.error(e);
			}
		}
		
		return parent.getBeanDefinition();
		
	}

	private static void parseChildApplication(Element element, BeanDefinitionBuilder parent, ParserContext parserContext) {
		
		Element application = DomUtils.getChildElementByTagName(element, "application");
		if (null == application){
			return;
		}
		try {
			parserApplication(application,parserContext,parent);
		} catch (ConfigInvalidException e){
			e.printStackTrace();
		}
	}

	private static void parserApplication(Element application, ParserContext parserContext, BeanDefinitionBuilder parent) 
			throws ConfigInvalidException{
		
		BeanDefinitionBuilder component = BeanDefinitionBuilder.rootBeanDefinition(ApplicationConfig.class);
		
		String name = application.getAttribute("name");
		String heartbeatInterval = application.getAttribute("heartbeatInterval");
		String description = application.getAttribute("description");
		String dcStr = application.getAttribute("dc");
		String contact = application.getAttribute("contact");
		String nettyBossCount = application.getAttribute("nettyBossCount");
		String nettyWorkerCount = application.getAttribute("nettyWorkerCount");
		String springFeature = application.getAttribute("springFeature");
		
		if (StringUtils.isNotBlank(name)){
			component.addPropertyValue("name", name);			
		}
		
		if (StringUtils.isNotBlank(heartbeatInterval)){
			component.addPropertyValue("heartbeatInterval", heartbeatInterval);			
		}
		
		if (StringUtils.isNotBlank(description)){
			component.addPropertyValue("description", description);			
		}
		
		if (StringUtils.isNotBlank(contact)){
			component.addPropertyValue("contact", contact);			
		}
		
		if (StringUtils.isNotBlank(nettyBossCount)){
			component.addPropertyValue("nettyBossCount", nettyBossCount);			
		}
		
		if (StringUtils.isNotBlank(nettyWorkerCount)){
			component.addPropertyValue("nettyWorkerCount", nettyWorkerCount);			
		}
		
		if (StringUtils.isNotBlank(springFeature)){
			component.addPropertyValue("springFeature", springFeature);			
		} else {
			component.addPropertyValue("springFeature", true);
		}
		
		if (StringUtils.isNotBlank(dcStr)){
			component.addPropertyValue("dcStr", dcStr);
			DcType dcType = null;
			if (DcType.SHANGHAI.toString().equals(dcStr.trim())){
				dcType = DcType.SHANGHAI;
			}
			if (DcType.BEIJING.toString().equals(dcStr.trim())){
				dcType = DcType.BEIJING;
			}
			String[] dcs = dcStr.split(Constants.COMMA_SEPARATOR);
			if (dcs.length > 1){
				dcType = DcType.ALL;
			}
			component.addPropertyValue("dc", dcType);
		}
		component.setScope(BeanDefinition.SCOPE_SINGLETON);
		component.setLazyInit(false);
		
		if (parserContext.getRegistry().containsBeanDefinition(ApplicationConfig.class.getName())){
			throw new ConfigInvalidException("more than one <application> found when parse ApplicationConfig from xml of spring");
		}
		
		parserContext.getRegistry().registerBeanDefinition(
				ApplicationConfig.class.getName(), 
				component.getBeanDefinition());
		
		parent.addPropertyReference("applicationConfig", ApplicationConfig.class.getName());
	}
	
	private static void parseChildRegistry(Element element, BeanDefinitionBuilder parent, ParserContext parserContext){
		
		Element registry = DomUtils.getChildElementByTagName(element, "registry");
		if (null == registry){
			return;
		}
		try {
			parseRegistry(registry, parserContext, parent);
		} catch (ConfigInvalidException e){
			Logger.error(e);
			System.exit(1);
		}
	}

	private static void parseRegistry(Element registry, ParserContext parserContext, BeanDefinitionBuilder parent) {
		
		BeanDefinitionBuilder component = BeanDefinitionBuilder.rootBeanDefinition(RegistryConfig.class);
		
		String addresses = registry.getAttribute("addresses");
		String connectionTimeoutMs = registry.getAttribute("connectionTimeoutMs");
		String sessionTimeoutMs = registry.getAttribute("sessionTimeoutMs");
		String type = registry.getAttribute("type");
		
		if (StringUtils.isNotBlank(addresses)){
			component.addPropertyValue("addresses", addresses);
		}
		
		if (StringUtils.isNotBlank(connectionTimeoutMs)){
			component.addPropertyValue("connectionTimeoutMs", connectionTimeoutMs);
		}
		
		if (StringUtils.isNotBlank(sessionTimeoutMs)){
			component.addPropertyValue("sessionTimeoutMs", sessionTimeoutMs);
		}
		
		if (StringUtils.isNotBlank(type)){
			component.addPropertyValue("type", type);
		}
		
		component.setScope(BeanDefinition.SCOPE_SINGLETON);
		component.setLazyInit(false);
		
		if (parserContext.getRegistry().containsBeanDefinition(RegistryConfig.class.getName())){
			throw new ConfigInvalidException("more than one <registry> found parse RegistryConfig from xml of spring");
		}
		
		parserContext.getRegistry().registerBeanDefinition(
				RegistryConfig.class.getName(), 
				component.getBeanDefinition());
		
		parent.addPropertyReference("registryConfig", RegistryConfig.class.getName());
	}

	private static void parseChildMonitor(Element element, BeanDefinitionBuilder parent, ParserContext parserContext){
		
		Element monitor = DomUtils.getChildElementByTagName(element, "monitor");
		if (null == monitor){
			return;
		}
		try {
			parseMonitor(monitor, parserContext, parent);
		} catch (ConfigInvalidException e){
			Logger.error(e);
			System.exit(1);
		}
	}

	private static void parseMonitor(Element monitor, ParserContext parserContext, BeanDefinitionBuilder parent) {
		
		BeanDefinitionBuilder component = BeanDefinitionBuilder.rootBeanDefinition(MonitorConfig.class);
		
		String urls = monitor.getAttribute("urls");
		String monitorInterval = monitor.getAttribute("monitorInterval");
		
		if (StringUtils.isNotBlank(urls)){
			component.addPropertyValue("urls", urls);
		}
		
		if (StringUtils.isNotBlank(monitorInterval)){
			component.addPropertyValue("monitorInterval", monitorInterval);
		}
		
		component.setScope(BeanDefinition.SCOPE_SINGLETON);
		component.setLazyInit(false);
		
		if (parserContext.getRegistry().containsBeanDefinition(MonitorConfig.class.getName())){
			throw new ConfigInvalidException("more than one <monitor> found when parse MonitorConfig from xml of spring");
		}
		
		parserContext.getRegistry().registerBeanDefinition(
				MonitorConfig.class.getName(),
				component.getBeanDefinition());
		
		parent.addPropertyReference("monitorConfig", MonitorConfig.class.getName());
	}
	
	private static void parseChildProtocols(Element element, ParserContext parserContext){
		
		List<Element> childElements = DomUtils.getChildElementsByTagName(element, "protocol");
		if (childElements != null && childElements.size() > 0){
			
			for (int i=0; i<childElements.size(); i++){
				Element childElement = (Element)childElements.get(i);
				try {
					BeanDefinitionBuilder protocol = parseProtocol(childElement,parserContext);
				} catch (ConfigInvalidException e){
					Logger.error(e);
				}
			}
		}
	}

	private static BeanDefinitionBuilder parseProtocol(Element element, ParserContext parserContext) 
		throws ConfigInvalidException{
		
		BeanDefinitionBuilder component = BeanDefinitionBuilder.rootBeanDefinition(ProtocolConfig.class);
		
		String id = element.getAttribute("id");
		String codec = element.getAttribute("codec");
		String version = element.getAttribute("version");
		String port = element.getAttribute("port");
		String ip = element.getAttribute("ip");
		String serializationType = element.getAttribute("serializationType");
		String compressAlgorithm = element.getAttribute("compressAlgorithm");
		String maxMsgSize = element.getAttribute("maxMsgSize");
		String listener = element.getAttribute("listener");
		String heartbeatInterval = element.getAttribute("heartbeatInterval");
		String maxThreds = element.getAttribute("maxThreads");
		
		if (StringUtils.isNotBlank(id)){
			component.addPropertyValue("id", id);
		}
		
		if (StringUtils.isNotBlank(codec)){
			component.addPropertyValue("codec", codec);
		}
		
		if (StringUtils.isNotBlank(version)){
			component.addPropertyValue("version", version);
		}
		
		if (StringUtils.isNotBlank(id)){
			component.addPropertyValue("id", id);
		}
		
		if (StringUtils.isNotBlank(port)){
			component.addPropertyValue("portStr", port);
		}
		
		if (StringUtils.isNotBlank(ip)){
			component.addPropertyValue("ip", ip);
		}
		
		if (StringUtils.isNotBlank(serializationType)){
			SerializationType st = null;
			if (serializationType.equals(SerializationType.BINARY.getText())){
				st = SerializationType.BINARY;
			}
			if (serializationType.equals(SerializationType.HESSIAN2.getText())){
				st = SerializationType.HESSIAN2;
			}
			if (serializationType.equals(SerializationType.JDK.getText())){
				st = SerializationType.JDK;
			}
			if (serializationType.equals(SerializationType.KRYO.getText())){
				st = SerializationType.KRYO;
			}
			component.addPropertyValue("serializationType", st);
		}
		
		if (StringUtils.isNotBlank(compressAlgorithm)){
			CompressAlgorithmType ca = null;
			if (compressAlgorithm.equals(CompressAlgorithmType.GZIP.getText())){
				ca = CompressAlgorithmType.GZIP;
			}
			if (compressAlgorithm.equals(CompressAlgorithmType.NONE.getText())){
				ca = CompressAlgorithmType.NONE;
			}
			if (compressAlgorithm.equals(CompressAlgorithmType.SNAPPY.getText())){
				ca = CompressAlgorithmType.SNAPPY;
			}
			if (compressAlgorithm.equals(CompressAlgorithmType.ZLIB.getText())){
				ca = CompressAlgorithmType.ZLIB;
			}
			component.addPropertyValue("compressAlgorithm", ca);
		}
		
		if (StringUtils.isNotBlank(maxMsgSize)){
			component.addPropertyValue("maxMsgSize", maxMsgSize);
		}
		
		if (StringUtils.isNotBlank(listener)){
			component.addPropertyValue("listener", listener);
		}
		
		if (StringUtils.isNotBlank(heartbeatInterval)){
			component.addPropertyValue("heartbeatInterval", heartbeatInterval);
		}
		
		if (StringUtils.isNotBlank(maxThreds)){
			component.addPropertyValue("maxThreds", maxThreds);
		}
		
		component.setScope(BeanDefinition.SCOPE_SINGLETON);
		component.setLazyInit(false);
		
		if (parserContext.getRegistry().containsBeanDefinition(id)){
			throw new ConfigInvalidException("duplicate protocol id " + id + " found when parse ProtocolConfig from xml of spring");
		}
		
		parserContext.getRegistry().registerBeanDefinition(id, component.getBeanDefinition());
		return component;
	}
	
	private static void parseChildServices(Element element, ParserContext parserContext){
		
		List<Element> childElements = DomUtils.getChildElementsByTagName(element, "service");
		if (childElements != null && childElements.size() > 0){
			
			for (int i=0; i<childElements.size(); i++){
				Element childElement = (Element)childElements.get(i);
				try {
					BeanDefinitionBuilder service = parseService(childElement, parserContext);
				} catch (ConfigInvalidException e){
					Logger.error(e);
				}
			}
		}
	}

	private static BeanDefinitionBuilder parseService(Element element, ParserContext parserContext) 
		throws ConfigInvalidException{
		
		BeanDefinitionBuilder component = BeanDefinitionBuilder.rootBeanDefinition(ServiceConfig.class);
		
		String serviceId = element.getAttribute("serviceId");
		String serviceVersion = element.getAttribute("serviceVersion");
		String description = element.getAttribute("description");
		String interfaceClass = element.getAttribute("interfaceClass");
		String implClass = element.getAttribute("implClass");
		String proxyFactory = element.getAttribute("proxyFactory");
		String weight = element.getAttribute("weight");
		String oneway = element.getAttribute("oneway");
		String timeout = element.getAttribute("timeout");
		String throttleValue = element.getAttribute("throttleValue");
		String throttleType = element.getAttribute("throttleType");
		String protocol = element.getAttribute("protocol");
		String local = element.getAttribute("local");
		
		if (StringUtils.isNotBlank(serviceId)){
			component.addPropertyValue("serviceId", serviceId);
		}
		
		if (StringUtils.isNotBlank(serviceId)){
			component.addPropertyValue("serviceId", serviceId);
		}
		
		if (StringUtils.isNotBlank(serviceVersion)){
			component.addPropertyValue("serviceVersion", serviceVersion);
		}
		
		if (StringUtils.isNotBlank(description)){
			component.addPropertyValue("description", description);
		}
		
		if (StringUtils.isNotBlank(interfaceClass)){
			component.addPropertyValue("interfaceClass", interfaceClass);
		}
		
		if (StringUtils.isNotBlank(implClass)){
			component.addPropertyValue("implClass", implClass);
		}
		
		if (StringUtils.isNotBlank(proxyFactory)){
			component.addPropertyValue("proxyFactory", proxyFactory);
		}
		
		if (StringUtils.isNotBlank(weight)){
			component.addPropertyValue("weight", weight);
		}
		
		if (StringUtils.isNotBlank(timeout)){
			component.addPropertyValue("timeout", timeout);
		}
		
		if (StringUtils.isNotBlank(throttleValue)){
			component.addPropertyValue("throttleValue", throttleValue);
		}
		
		if (StringUtils.isNotBlank(throttleType)){
			ThrottleType tt = null;
			if (tt.equals(ThrottleType.FLOW.toString())){
				tt = ThrottleType.FLOW;
			} else {
				tt = ThrottleType.QPS;
			}
			component.addPropertyValue("throttleType", tt);
		}
		
		if (StringUtils.isNotBlank(protocol)){
			component.addPropertyValue("protocol", protocol);
		}
		
		if (StringUtils.isNotBlank(local)){
			component.addPropertyValue("local", local);
		}
		
		component.setScope(BeanDefinition.SCOPE_SINGLETON);
		component.setLazyInit(false);
		
		if (parserContext.getRegistry().containsBeanDefinition(ServiceConfig.class.getName() + "#" + serviceId + "#" + serviceVersion)){
			throw new ConfigInvalidException("duplicate service " + serviceId + "#" + serviceVersion
					+ " found when parse ServiceConfig from xml from spring");
		}
		
		parserContext.getRegistry().registerBeanDefinition(
				ServiceConfig.class.getName() + "#" + serviceId + "#" + serviceVersion, 
				component.getBeanDefinition());
		
		return component;
	}
	
	private static void parseChildReferences(Element element, ParserContext parserContext){
		
		List<Element> childElements = DomUtils.getChildElementsByTagName(element, "reference");
		if (childElements != null && childElements.size() > 0){
			
			for (int i=0; i<childElements.size(); i++){
				Element childElement = childElements.get(i);
				try {
					BeanDefinitionBuilder reference = parseReference(childElement, parserContext);
				} catch (ConfigInvalidException e){
					Logger.error(element);
				}
			}
		}
	}

	private static BeanDefinitionBuilder parseReference(Element element, ParserContext parserContext) 
		throws ConfigInvalidException{
		
		BeanDefinitionBuilder component = BeanDefinitionBuilder.rootBeanDefinition(ReferenceConfig.class);
		
		String serviceId = element.getAttribute("serviceId");
		String serviceVersion = element.getAttribute("serviceVersion");
		String urls = element.getAttribute("urls");
		String loadbalanceStrategy = element.getAttribute("loadbalanceStrategy");
		String failStrategy = element.getAttribute("failStrategy");
		String retries = element.getAttribute("retries");
		String oneway = element.getAttribute("oneway");
		String dcAutoSwitch = element.getAttribute("dcAutoSwitch");
		String timeout = element.getAttribute("timeout");
		String weights = element.getAttribute("weights");
		String forks = element.getAttribute("forks");
		String interfaceClass = element.getAttribute("interfaceClass");
		String dcStr = element.getAttribute("dc");
		String protocol = element.getAttribute("protocol");
		
		if (StringUtils.isNotBlank(serviceId)){
			component.addPropertyValue("serviceId", serviceId);
		}
		
		if (StringUtils.isNotBlank(serviceVersion)){
			component.addPropertyValue("serviceVersion", serviceVersion);
		}
		
		if (StringUtils.isNotBlank(urls)){
			component.addPropertyValue("urls", urls);
		}
		
		if (StringUtils.isNotBlank(loadbalanceStrategy)){
			LoadBalanceType bt = null;
			if (loadbalanceStrategy.equals(LoadBalanceType.FIRST.toString())){
				bt = LoadBalanceType.FIRST;
			}
			if (loadbalanceStrategy.equals(LoadBalanceType.PRIORITY.toString())){
				bt = LoadBalanceType.PRIORITY;
			}
			if (loadbalanceStrategy.equals(LoadBalanceType.RANDOM.toString())){
				bt = LoadBalanceType.RANDOM;
			}
			if (loadbalanceStrategy.equals(LoadBalanceType.ROUNDROBIN.toString())){
				bt = LoadBalanceType.ROUNDROBIN;
			}
			component.addPropertyValue("loadbalanceStrategy", bt);
		}
		
		if (StringUtils.isNotBlank(failStrategy)){
			FailType ft = null;
			if (failStrategy.equals(FailType.FAIL_BACK.toString())){
				ft = FailType.FAIL_BACK;
			}
			if (failStrategy.equals(FailType.FAIL_FAST.toString())){
				ft = FailType.FAIL_FAST;
			}
			if (failStrategy.equals(FailType.FAIL_OVER.toString())){
				ft = FailType.FAIL_OVER;
			}
			if (failStrategy.equals(FailType.FORKING.toString())){
				ft = FailType.FORKING;
			}
			if (failStrategy.equals(FailType.BROADCAST.toString())){
				ft = FailType.BROADCAST;
			}
			component.addPropertyValue("failStrategy", ft);
		}
		
		if (StringUtils.isNotBlank(retries)){
			component.addPropertyValue("retries", retries);
		}
		
		if (StringUtils.isNotBlank(oneway)){
			component.addPropertyValue("oneway", oneway);
		}
		
		if (StringUtils.isNotBlank(dcAutoSwitch)){
			component.addPropertyValue("dcAutoSwitch", dcAutoSwitch);
		}
		
		if (StringUtils.isNotBlank(timeout)){
			component.addPropertyValue("timeout", timeout);
		}
		
		if (StringUtils.isNotBlank(weights)){
			component.addPropertyValue("weights", weights);
		}
		
		if (StringUtils.isNotBlank(forks)){
			component.addPropertyValue("forks", forks);
		}
		
		if (StringUtils.isNotBlank(interfaceClass)){
			component.addPropertyValue("interfaceClass", interfaceClass);
		}
		
		if (StringUtils.isNotBlank(dcStr)){
			component.addPropertyValue("dcStr", dcStr);
			DcType dt = null;
			if (DcType.SHANGHAI.toString().equals(dcStr.trim())){
				dt = DcType.SHANGHAI;
			}
			if (DcType.BEIJING.toString().equals(dcStr.trim())){
				dt = DcType.BEIJING;
			}
			String[] dcs = dcStr.split(Constants.COMMA_SEPARATOR);
			if (dcs.length > 1){
				dt = DcType.ALL;
			}
			component.addPropertyValue("dc", dt);
		}
		
		if (StringUtils.isNotBlank(protocol)){
			component.addPropertyValue("protocol", protocol);
		}
		
		component.setScope(BeanDefinition.SCOPE_SINGLETON);
		component.setLazyInit(false);
		
		if (parserContext.getRegistry().containsBeanDefinition(ReferenceConfig.class.getName() + "#" + serviceId + "#" + serviceVersion)){
			throw new ConfigInvalidException("duplicate service " + serviceId + "#" + serviceVersion
					+ " found when parse ReferenceConfig from xml from spring");
		}
		
		parserContext.getRegistry().registerBeanDefinition(
				ReferenceConfig.class.getName() + "#" + serviceId + "#" + serviceVersion, 
				component.getBeanDefinition());
		
		return component;
	}
}
