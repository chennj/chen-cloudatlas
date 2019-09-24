package net.chen.cloudatlas.crow.config.spring.schema;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class CrowNamespaceHandler extends NamespaceHandlerSupport{

	@Override
	public void init() {
		this.registerBeanDefinitionParser("crow", new CrowDefinitionParser());
	}

}
