package org.chen.cloudatlas.crow.rpc;

import java.util.Map;

public interface Invocation {

	String getMethodName();
	
	Class<?>[] getParameterTypes();
	
	Class<?> getReturnType();
	
	Object[] getArguments();
	
	Map<String, String> getAttachments();
	
	String getAttachment(String key);
	
	String getAttachment(String key, String defaultValue);
	
	Invoker<?> getInvoker();
}
