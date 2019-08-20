package org.chen.cloudatlas.crow.rpc.crow;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang.StringUtils;
import org.chen.cloudatlas.crow.common.Constants;
import org.chen.cloudatlas.crow.common.Protocols;
import org.chen.cloudatlas.crow.common.URL;
import org.chen.cloudatlas.crow.remote.Channel;
import org.chen.cloudatlas.crow.remote.RemoteException;
import org.chen.cloudatlas.crow.remote.exchange.ExchangeChannel;
import org.chen.cloudatlas.crow.remote.exchange.ExchangeClient;
import org.chen.cloudatlas.crow.remote.exchange.ExchangeListener;
import org.chen.cloudatlas.crow.remote.exchange.ExchangeListenerAdapter;
import org.chen.cloudatlas.crow.remote.exchange.ExchangeServer;
import org.chen.cloudatlas.crow.remote.exchange.header.HeaderExchangeListener;
import org.chen.cloudatlas.crow.remote.exchange.header.HeaderExchangeServer;
import org.chen.cloudatlas.crow.remote.impl.NettyServer;
import org.chen.cloudatlas.crow.rpc.Context;
import org.chen.cloudatlas.crow.rpc.Invocation;
import org.chen.cloudatlas.crow.rpc.Invoker;
import org.chen.cloudatlas.crow.rpc.RpcException;
import org.chen.cloudatlas.crow.rpc.impl.RpcInvocation;
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

		@Override
		public void connected(Channel channel) throws RemoteException {
			invoke(channel, "onconnect");
		}

		private void invoke(Channel channel, String methodKey) {
			
			Invocation invocation = createInvocation(channel, channel.getUrl(), methodKey);
			if (null == invocation){
				try {
					received(channel, invocation);
				} catch (Exception e){
					Logger.warn("failed to invoke event method " + invocation.getMethodName() + "()", e);
				}
			}
		}

		private Invocation createInvocation(Channel channel, URL url, String methodKey) {
			
			String method = url.getParameter(methodKey);
			if (StringUtils.isEmpty(method)){
				return null;
			}
			RpcInvocation invocation = new RpcInvocation(method, new Class<?>[0],new Object[0]);
			invocation.setAttachment("path", url.getPath());
			return invocation;
		}

		@Override
		public void disconnected(Channel channel) throws RemoteException {
			invoke(channel, "ondisconnect");
		}

		@Override
		public void received(Channel channel, Object message) throws RemoteException {
			
			Logger.debug("CrowProtocol ExchangeListener received: " + message);
			
			if (message instanceof Invocation){
				reply((ExchangeChannel)channel, message);
			} else {
				super.received(channel, message);
			}
		}
		
	};
	
	public static CrowProtocol getCrowProtocol(){
		
		if (null == instance){
			synchronized(CrowProtocol.class){
				if (null == instance){
					new CrowProtocol();
				}
			}
		}
		
		return instance;
	}
	
	private boolean isClientSide(Channel channel){
		
		InetSocketAddress address = channel.getRemoteAddress();
		URL url = channel.getUrl();
		return url.getPort() == address.getPort();
	}
	
	protected Invoker<?> getInvoker(Channel channel, Invocation inv) throws RemoteException{
		
		String path = inv.getAttachments().get("path");
		int port	= channel.getLocalAddress().getPort();
		
		String serviceKey = serviceKey(port, path, inv.getAttachments().get(Constants.DC), inv.getAttachments().get(Constants.SERVICE_VERSION));
		
		CrowExporter<?> exporter = (CrowExporter<?>)exporterMap.get(serviceKey);
		
		if (null == exporter){
			
			port = channel.getRemoteAddress().getPort();
			Logger.trace("CrowProtocol getInvoker again, channel: " + channel + " port: " + port);
			serviceKey = serviceKey(port, path, inv.getAttachments().get(Constants.DC), inv.getAttachments().get(Constants.SERVICE_VERSION));
			exporter = (CrowExporter<?>)exporterMap.get(serviceKey);
			
			if (null == exporter){
				
				Logger.warn("not found exported service: " + serviceKey);
				throw new RemoteException(channel, "not found exported service: " + serviceKey + " in "
						+ exporterMap.keySet()
						+ ", may be version or group mismatch, channel: consumer: "
						+ channel.getUrl().getHostAndPort()
						+ " --> provider: " + channel.getLocalAddress()
						+ ", message: "
						+ inv);
			}
		}
		
		return exporter.getInvoker();
	}

	@Override
	public int getDefaultPort() {
		return DEFAULT_PORT;
	}

	@Override
	public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
		
		URL url 		= invoker.getUrl();
		String key 		= serviceKey(url);
		String oldKey 	= serviceKeyOld(url);
		
		CrowExporter<T> exporter = new CrowExporter<T>(invoker, key, exporterMap);
		exporterMap.put(key, exporter);
		// 兼容，若同一个interface有两个实现类，serviceVersion为1.0和2.0，则serviceKey可能
		// 产生覆盖的情况
		if (
				url.getParameter(Constants.SERVICE_VERSION) == null ||
				url.getParameter(Constants.SERVICE_VERSION).equals(Constants.DEFAULT_SERVICE_VERSION)){
			exporterMap.put(oldKey, exporter);
		}
		
		Logger.debug("CrowProtocol export serviceKey: " + key);
		
		openServer(url);
		
		return exporter;
	}

	private void openServer(URL url) {
		
		// find server
		String key = url.getHostAndPort();
		// client 也可以暴露一个只有server可以调用的服务
		boolean isServer = true;
		if (isServer){
			ExchangeServer server = serverMap.get(key);
			if (null == server){
				serverMap.put(key, createServer(url));
			} else {
				// server支持reset,配合override功能使用
				// server.reset(url);
			}
		}
	}

	private ExchangeServer createServer(URL url) {
		
		ExchangeServer server;
		try {
			// 获取URL中的heartbeatInterval
			NettyServer ns = new NettyServer(url, new HeaderExchangeListener(requestListener));
			server = new HeaderExchangeServer(ns);
			server.bind();
		} catch (Exception e){
			throw new RpcException("fail to start server(url: " + url + ")" + e.getMessage(), e);
		}
		return server;
	}

	@Override
	public <T> Invoker<T> refer(Class<T> serviceType, URL url, CountDownLatch latch) throws RpcException {
		
		CrowInvoker<T> invoker = new CrowInvoker<T>(serviceType, url, getClients(url,latch), invokers);
		invokers.remove(invoker);
		invokers.add(invoker);
		return invoker;
	}

	private ExchangeClient[] getClients(URL url, CountDownLatch latch) {
		
		// 是否共享连接
		int connections = 1;
		
		ExchangeClient[] clients = new ExchangeClient[connections];
		for (int i=0; i<clients.length; i++){
			clients[i] = initClient(url,latch);
		}
		
		return clients;
	}

	@Override
	public String getName() {
		return Protocols.CROW_RPC;
	}

	@Override
	public void destroy() {

		for (String key : serverMap.keySet()){
			
			ExchangeServer server = serverMap.remove(key);
			if (null == server){
				
				try {
					if (Logger.isInfoEnabled()){
						Logger.info("close crow server: " + server.getLocalAddress());
					}
				}
			}
		}
	}

}
