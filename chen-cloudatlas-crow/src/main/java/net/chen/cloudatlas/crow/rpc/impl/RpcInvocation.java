package net.chen.cloudatlas.crow.rpc.impl;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import net.chen.cloudatlas.crow.rpc.Invocation;
import net.chen.cloudatlas.crow.rpc.Invoker;

public class RpcInvocation implements Invocation, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String methodName;
	
	private Class<?>[] parameterTypes;
	
	private Class<?> returnType;
	
	private Object[] arguments;
	
	private Map<String, String> attachments;
	
	private transient Invoker<?> invoker;
	
	public RpcInvocation(){
		
	}
	
	public RpcInvocation(Invocation invocation){
		this(
				invocation.getMethodName(),
				invocation.getParameterTypes(),
				invocation.getArguments(),
				invocation.getReturnType(),
				invocation.getAttachments(),
				invocation.getInvoker());
	}
	
	public RpcInvocation(Method method, Object[] arguments){
		this(method.getName(), method.getParameterTypes(), arguments, method.getReturnType(), null, null);
	}
	
	public RpcInvocation(Method method, Object[] arguments, Map<String, String> attachment){
		this(method.getName(), method.getParameterTypes(), arguments, method.getReturnType(), attachment, null);
	}
	
	public RpcInvocation(String methodName, Class<?>[] parameterTypes, Object[] arguments){
		this(methodName, parameterTypes, arguments, null, null, null);
	}
	
	public RpcInvocation(String methodName, Class<?>[] parameterTypes, Object[] arguments, Map<String, String> attachments){
		this(methodName, parameterTypes, arguments, null, attachments, null);
	}
	
	public RpcInvocation(String methodName, Class<?>[] parameterTypes, Object[] arguments, Class<?> returnType,
			Map<String, String> attachments, Invoker<?> invoker) {
		
		this.methodName = methodName;
		this.parameterTypes = parameterTypes == null ? new Class<?>[0] : parameterTypes;
		this.returnType = returnType;
		this.arguments = arguments == null ? new Object[0] :arguments;
		this.attachments = attachments == null ? new HashMap<String, String>() : attachments;
		this.invoker = invoker;
	}

	@Override
	public String getMethodName() {
		return methodName;
	}

	@Override
	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}

	@Override
	public Class<?> getReturnType() {
		return returnType;
	}

	@Override
	public Object[] getArguments() {
		return arguments;
	}

	@Override
	public Map<String, String> getAttachments() {
		return attachments;
	}

	@Override
	public String getAttachment(String key) {
		if (null == attachments){
			return null;
		}
		return attachments.get(key);
	}

	@Override
	public String getAttachment(String key, String defaultValue) {
		if (null == attachments){
			return defaultValue;
		}
		String val = attachments.get(key);
		if (StringUtils.isEmpty(val)){
			return defaultValue;
		}
		return val;
	}

	@Override
	public Invoker<?> getInvoker() {
		return invoker;
	}

	@Override
	public String toString(){
		String separator = System.getProperty("line.separator");
		String linespace = "                                       ";
		return "RpcInvocation [methodName"+separator+linespace
				+ "parameterTypes=" + Arrays.toString(parameterTypes) + separator + linespace
				+ "arguments=" + Arrays.toString(arguments) + separator + linespace
				+ "attachments=" + attachments + "]";
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public void setParameterTypes(Class<?>[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	public void setReturnType(Class<?> returnType) {
		this.returnType = returnType;
	}

	public void setArguments(Object[] arguments) {
		this.arguments = arguments == null ? new Object[0] : arguments;
	}

	public void setAttachments(Map<String, String> attachments) {
		this.attachments = attachments == null ? new HashMap<>() : attachments;
	}

	public void setInvoker(Invoker<?> invoker) {
		this.invoker = invoker;
	}
	
	public void setAttachment(String key, String value){
		if (null == attachments){
			attachments = new HashMap<>();
		}
		attachments.put(key, value);
	}
	
	public void setAttachmentIfAbsent(String key, String value){
		if (null == attachments){
			attachments = new HashMap<>();
		}
		if (!attachments.containsKey(key)){
			attachments.put(key, value);
		}
	}
	
	public void addAttachments(Map<String,String> attachments){
		if (null == attachments){
			return;
		}
		if (this.attachments == null){
			this.attachments = new HashMap<String,String>();
		}
		this.attachments.putAll(attachments);
	}
	
	public void addAttachmentsIfAbsent(Map<String, String> attachments){
		if  (null == attachments){
			return;
		}
		for (Map.Entry<String, String> entry : attachments.entrySet()){
			setAttachmentIfAbsent(entry.getKey(), entry.getValue());
		}
	}
}
