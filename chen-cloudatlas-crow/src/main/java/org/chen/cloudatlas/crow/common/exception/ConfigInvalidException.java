package org.chen.cloudatlas.crow.common.exception;

import java.util.Arrays;

public class ConfigInvalidException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ConfigInvalidException(String msg){
		super(msg);
	}
	
	public ConfigInvalidException(String msg, Throwable cause){
		super(msg,cause);
	}

	public ConfigInvalidException(String msg, Object[] objs){
		super(msg+":"+Arrays.toString(objs));
	}
	
	public ConfigInvalidException(String msg, Object[] objs, Throwable cause){
		super(msg+":"+Arrays.toString(objs),cause);
	}
}
