package org.chen.cloudatlas.crow.rpc.crow;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.chen.cloudatlas.crow.common.URL;
import org.chen.cloudatlas.crow.remote.RemoteException;
import org.chen.cloudatlas.crow.remote.exchange.ExchangeChannel;
import org.chen.cloudatlas.crow.remote.exchange.ExchangeListener;
import org.chen.cloudatlas.crow.remote.exchange.ExchangeListenerAdapter;
import org.chen.cloudatlas.crow.remote.exchange.ExchangeServer;
import org.chen.cloudatlas.crow.rpc.Invocation;
import org.chen.cloudatlas.crow.rpc.Invoker;
import org.chen.cloudatlas.crow.rpc.RpcException;
import org.chen.cloudatlas.crow.rpc.protocol.AbstractProtocol;
import org.chen.cloudatlas.crow.rpc.protocol.Exporter;
import org.tinylog.Logger;

public class CrowProtocol extends AbstractProtocol{

	public static final int DEFAULT_PORT = 20880;
	
	private final Map<String, ExchangeServer> serverMap = new ConcurrentHashMap<>();
	
	private static final String IS_CALLBACK_SERVICE_INVOKE = "_isCallBackServiceInvoke";
	
	private static volatile CrowProtocol instance;
	
	public CrowProtocol(){
		instance = this;
	}
	
	private ExchangeListener requestListener = new ExchangeListenerAdapter(){

		@Override
		public Object reply(ExchangeChannel channel, Object message) throws RemoteException {
			
			if (message instanceof Invocation){
				
				Invocation inv = (Invocation)message;
				
				Logger.trace("channel: " + channel);
				
				Invoker<?> invoker = getInvoker(channel, inv);
				
				if (Boolean.TRUE.toString().equals(inv.getAttachments().get(IS_CALLBACK_SERVICE_INVOKE))){
					// 如果是callback需要处理高版本调用低版本的问题
					String methodsStr = invoker.getUrl().getParameters().get("methods");
					boolean hasMethod = false;
					if (null == methodsStr || methodsStr.indexOf(',') == -1){
						hasMethod = inv.getMethodName().equals(methodsStr);
					} else {
						String[] methods = methodsStr.split(",");
						for (String method : methods){
							if (inv.getMethodName().equals(method)){
								hasMethod = true;
								break;
							}
						}
					}
					
					if (!hasMethod){
						return null;
					}
				}
				
				// 在invoke之前将consumer端的地址信息放进threadlocal，让chain中的filter可以方便的取到consumer
				// 端的信息。
				Context.getContext().setRemoteAddress(channel.getRemoteAddress());
				return invoker.invoke(inv);
			}
			
			throw new RemoteException(channel, "Unsupported request: " + message == null? null : (message.getClass().getName()
					+ ": " + message
					+ ", channel: consumer: " + channel.getUrl().getHostAndPort() + " --> provider: "
					+ channel.getLocalAddress()));
		}
		
	};
	
	
	@Override
	public int getDefaultPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Invoker<T> refer(Class<T> type, URL url, CountDownLatch latch) throws RpcException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
