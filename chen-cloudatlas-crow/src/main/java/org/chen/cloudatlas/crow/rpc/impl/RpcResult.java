package org.chen.cloudatlas.crow.rpc.impl;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.chen.cloudatlas.crow.common.Constants;
import org.chen.cloudatlas.crow.common.utils.DataTypeUtil;
import org.chen.cloudatlas.crow.rpc.Result;
import org.springframework.util.StringUtils;

public class RpcResult implements Result, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Object result;
	
	private Throwable exception;
	
	private Map<String, String> attachments = new HashMap<>();
	
	public RpcResult(){}
	
	public RpcResult(Object result){
		this.result = result;
	}	
	
	public RpcResult(Throwable exception){
		this.exception = exception;
	}	
	
	@Override
	public Object getValue() {
		return result;
	}
	
	public void setValue(Object value){
		result = value;
	}

	@Override
	public Throwable getException() {
		return exception;
	}
	
	public void  setException(Throwable e){
		exception = e;
	}

	@Override
	public boolean hasException() {
		return exception != null;
	}
	
	public Map<String, String> getAttachments(){
		return attachments;
	}
	
	public String getAttachment(String key){
		return attachments.get(key);
	}
	
	public String getAttachment(String key, String defaultValue){
		
		String resultStr = attachments.get(key);
		
		if (StringUtils.isEmpty(resultStr)){
			resultStr = defaultValue;
		}
		
		return resultStr;
	}
	
	public void setAttachments(Map<String,String> map){
		if (null != map && !map.isEmpty()){
			attachments.putAll(map);
		}
	}
	
	public void setAttachment(String key, String value){
		attachments.put(key, value);
	}

	@Override
	public Object recreate() throws Throwable {
		
		if (null != exception){
			
			if (exception instanceof Throwable){
				Class cls = exception.getClass();
				while (cls != null && (!cls.equals(Throwable.class))){
					cls = cls.getSuperclass();
				}
				if (cls != null && cls.equals(Throwable.class)){
					
					try {
						Field field = cls.getDeclaredField("cause");
						field.setAccessible(true);
						field.set(exception, null);
					} catch (Exception ex){
						throw new RuntimeException("Exception from server");
					}
				} else {
					throw new RuntimeException("Exception from server");
				}
				throw exception;
			} else if (exception instanceof Map){
				// hessian如果找不到类 可能序列化为Map
				Object msg = ((Map)exception).get("detailMessage");
				Object stackTrace = ((Map)exception).get("stackTrace");
				Exception ex = new Exception("Exception from server: " + (msg==null?"":msg.toString()));
				if (null != stackTrace){
					ex.setStackTrace((StackTraceElement[])stackTrace);
				}
				throw ex;
			}
		}
		
		return DataTypeUtil.basicDataTrans(result, attachments.get(Constants.RETURN_TYPE));
	}

	@Override
	public String toString(){
		return "RpcResult [result=" + result + ", exception=" + exception + "]";
	}
}
