package net.chen.cloudatlas.crow.rpc.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.common.utils.DataTypeUtil;
import net.chen.cloudatlas.crow.rpc.Invoker;
import net.chen.cloudatlas.crow.rpc.RpcException;

public class JdkProxyFactory extends AbstractProxyFactory{

	@Override
	public <T> Invoker<T> getInvoker(T proxy, Class<T> interfaceClass, URL url) throws RpcException {
		
		return new AbstractProxyInvoker<T>(proxy, interfaceClass, url){

			@Override
			protected Object doInvoke(T proxy, String methodName, Class<?>[] parameterTypes, Object[] arguments)
					throws Exception {
				arguments = argumentsCheck(arguments, parameterTypes);
				Method method = proxy.getClass().getMethod(methodName, parameterTypes);
				return method.invoke(proxy, arguments);
			}
			
		};
	}

	/**
	 * 处理序列化过程中，发生过类型转换的参数
	 * @param arguments
	 * @param parameterTypes
	 * @return
	 */
	public Object[] argumentsCheck(Object[] arguments, Class<?>[] parameterTypes) {
		
		if (arguments.length == parameterTypes.length){
			
			Object[] newArgumentsCheck = new Object[arguments.length];
			for (int i=0; i<arguments.length; i++){
				newArgumentsCheck[i] = DataTypeUtil.basicDataTrans(arguments[i], parameterTypes[i].getName());
			}
			return newArgumentsCheck;
		} else {
			return arguments;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getProxy(Invoker<T> invoker, Class<?>[] interfaces) {
		
		return (T) Proxy.newProxyInstance(
				Thread.currentThread().getContextClassLoader(), 
				interfaces,
				new InvokerInvocationHandler(invoker));
	}

}
