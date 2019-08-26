package net.chen.cloudatlas.crow.manager.api.support;

import net.chen.cloudatlas.crow.config.ServiceConfig;

/**
 * 
 * @author chenn
 *
 */
public interface ServiceProvider {

	ServiceConfig<?> getConfig();
}
