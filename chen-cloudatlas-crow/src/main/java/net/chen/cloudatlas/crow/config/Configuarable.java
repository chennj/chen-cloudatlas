package net.chen.cloudatlas.crow.config;

import net.chen.cloudatlas.crow.common.exception.ConfigInvalidException;

/**
 * 
 * @author chenn
 *
 */
public interface Configuarable {

	void check() throws ConfigInvalidException;
	
	void setDefaultValue();
}
