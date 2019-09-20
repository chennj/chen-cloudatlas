package net.chen.cloudatlas.crow.config.spring.schema;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import net.chen.cloudatlas.crow.common.exception.ConfigInvalidException;
import net.chen.cloudatlas.crow.config.ApplicationConfig;
import net.chen.cloudatlas.crow.config.spring.config.CrowConfigSpringImpl;

public class CrowDefinitionParser extends AbstractBeanDefinitionParser{

	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		
		BeanDefinitionBuilder  parent = BeanDefinitionBuilder.rootBeanDefinition(CrowConfigSpringImpl.class);
		
		parent.setScope(BeanDefinition.SCOPE_SINGLETON);
		parent.setLazyInit(false);
		
		parseChildApplication(element, parent, parserContext);
		return null;
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
	}

}
