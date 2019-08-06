package org.chen.cloudatlas.crow.config;

import org.chen.cloudatlas.crow.common.exception.ConfigInvalidException;

/**
 * 
 * @author chenn
 *
 */
public interface Configuarable {

	void check() throws ConfigInvalidException;
	
	void setDefaultValue();
}
