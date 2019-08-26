package net.chen.cloudatlas.crow.rpc.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import net.chen.cloudatlas.crow.rpc.Invoker;
import net.chen.cloudatlas.crow.rpc.impl.RpcInvocation;

public class InvokerInvocationHandler implements InvocationHandler{

	private final Invoker<?> invoker;
	
	public InvokerInvocationHandler(Invoker<?> handler){
		if (null == handler){
			throw new IllegalArgumentException("handler is null");
		}
		this.invoker = handler;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		
		String methodName = method.getName();
		Class<?>[] parameterTypes = method.getParameterTypes();
		
		if (method.getDeclaringClass() == Object.class){
			return method.invoke(invoker, args);
		}
		
		if ("toString".equals(methodName) && parameterTypes.length == 0){
			return invoker.toString();
		}
		
		if ("hashCode".equals(methodName) && parameterTypes.length == 0){
			return invoker.hashCode();
		}
		
		if ("equals".equals(methodName) && parameterTypes.length == 1){
			return invoker.equals(args[0]);
		}
		
		return invoker.invoke(new RpcInvocation(method, args)).recreate();
	}

}
