package net.chen.cloudatlas.crow.remote;

import java.util.Map;

import net.chen.cloudatlas.crow.common.NameableService;
import net.chen.cloudatlas.crow.common.utils.NameableServiceLoader;

/**
 * 报文封装接口<br>
 * 将需要发送的字节数组封装为某种报文格式的报文<br>
 * 
 * @author chenn
 *
 */
public abstract class MessageWrapper implements NameableService{

	private static final Map<String, MessageWrapper> wrapperMap = NameableServiceLoader.getLoader(MessageWrapper.class).getServices();

	/**
	 * 根据协议类型获取不同的Wrapper
	 * @param protocol
	 * @return
	 */
	public static MessageWrapper get(String protocol){
		return wrapperMap.get(protocol);
	}
	
	/**
	 * 封装为请求报文
	 * 
	 * @param payload 用户需要封装的请求字节数组
	 * @param attachments 可以给协议传递个性化参数，共不同协议自行处理
	 * @return
	 * @throws RemoteException
	 */
	public abstract Request wrapRequest(byte[] payload, Map<String, Object> attachments) throws RemoteException;
	
	/**
	 * 对Request做一些额外的字段处理
	 * @param request
	 * @param attachments 可以给协议传递个性化参数，共不同协议自行处理
	 * @return
	 * @throws RemoteException
	 */
	public abstract Request wrapRequest(Request request, Map<String, Object> attachments) throws RemoteException;
	
	/**
	 * 从response对象中获取byte[]返回
	 * @param response
	 * @return
	 */
	public abstract byte[] decomposeResponse(Response response);
	
	/**
	 * 以request的信息为基础，构建response
	 * @param request
	 * @param attachments 可以给协议传递个性化参数，共不同协议自行处理
	 * @return
	 * @throws RemoteException
	 */
	public abstract Response wrapResponse(Request request, Map<String, Object> attachments) throws RemoteException;
	
	/**
	 * 以request的信息为基础，构建response
	 * 
	 * @param resposne
	 * @param attachments 可以给协议传递个性化参数，共不同协议自行处理
	 * @return
	 * @throws RemoteException
	 */
	public abstract Response wrapResponse(Response resposne, Map<String, Object> attachments) throws RemoteException;
	
	/**
	 * 构建心跳消息
	 * @param protocolVersion
	 * @return
	 * @throws RemoteException
	 */
	public abstract Request wrapHearbeat(String protocolVersion) throws RemoteException;
	
}
