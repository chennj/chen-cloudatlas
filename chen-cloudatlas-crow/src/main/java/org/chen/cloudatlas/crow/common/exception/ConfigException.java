package org.chen.cloudatlas.crow.common.exception;

import java.util.Arrays;

public class ConfigException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ConfigException(Throwable cause){
		super(cause);
	}
	
	public ConfigException(String msg){
		super(msg);
	}
	
	public ConfigException(String msg, Throwable cause){
		super(msg,cause);
	}

	public ConfigException(String msg, Object[] objs){
		super(msg+":"+Arrays.toString(objs));
	}
	
	public ConfigException(String msg, Object[] objs, Throwable cause){
		super(msg+":"+Arrays.toString(objs),cause);
	}
}
