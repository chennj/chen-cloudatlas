package net.chen.cloudatlas.crow.config.spring.config;

import java.util.Collection;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.config.CrowConfig;
import net.chen.cloudatlas.crow.config.ProtocolConfig;
import net.chen.cloudatlas.crow.config.ReferenceConfig;
import net.chen.cloudatlas.crow.config.ServiceConfig;
import net.chen.cloudatlas.crow.config.spring.boot.SpringBootstrapDelegate;

public class CrowConfigSpringImpl extends CrowConfig implements InitializingBean, ApplicationContextAware{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ApplicationContext applicationContext;
	
	public CrowConfigSpringImpl(){}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		
		// 将spring容器中的配置bean交给crow管理
		addConfigBeansFromContext();
		
		SpringBootstrapDelegate.setCrowConfig(this);
		
		// spring是否自动启动，默认是自动启动
		if (Constants.SPRINGAUTOSTART){
			SpringBootstrapDelegate.getSpringBootstrap().start();
		}
		
	}
	
	private void addConfigBeansFromContext(){
		
		this.getProtocolConfigList().clear();
		Collection<? extends ProtocolConfig> pconfigList = this.applicationContext.getBeansOfType(ProtocolConfig.class).values();
		this.getProtocolConfigList().addAll(pconfigList);
		
		this.getServiceConfigList().clear();
		Collection<? extends ServiceConfig> sconfigList = this.applicationContext.getBeansOfType(ServiceConfig.class).values();
		this.getServiceConfigList().addAll(sconfigList);
		
		this.getReferenceConfigList().clear();
		Collection<? extends ReferenceConfig> rconfigList = this.applicationContext.getBeansOfType(ReferenceConfig.class).values();
		this.getReferenceConfigList().addAll(rconfigList);
	}
}
