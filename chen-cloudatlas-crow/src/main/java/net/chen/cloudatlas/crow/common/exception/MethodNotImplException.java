package net.chen.cloudatlas.crow.common.exception;

public class MethodNotImplException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public MethodNotImplException(){
		super("方法还没有实现，有待完成");
	}
	
	public MethodNotImplException(Class<?> clz){
		super(clz.getName()+":方法还没有实现，有待完成");
	}
	
	public MethodNotImplException(String msg){
		super(msg);
	}

}
