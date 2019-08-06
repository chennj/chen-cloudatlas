package org.chen.cloudatlas.crow.client;

import java.util.Map;

import org.chen.cloudatlas.crow.remote.RemoteException;

/**
 * 
 * @author chenn
 *
 */
public interface ServiceController {

	/**
	 * 同步调用<br>
	 * @param requestBytes
	 * @return
	 * @throws RemoteException
	 */
	byte[] call(byte[] requestBytes) throws RemoteException;
	
	/**
	 * 同步调用<br>
	 * 带报文头参数，共不同协议Wrapper实现构造请求对象时带过去，赋值在报文头中。
	 * @param requestBytes
	 * @param attachments
	 * @return
	 * @throws RemoteException
	 */
	byte[] call(byte[] requestBytes, Map<String, Object> attachments) throws RemoteException;
	
	/**
	 * 异步调用<br>
	 * @param requestBytes
	 * @throws RemoteException
	 */
	void acall(byte[] requestBytes) throws RemoteException;
	
	/**
	 * 异步调用<br>
	 * 带报文头参数，共不同协议Wrapper实现构造请求对象时带过去，赋值在报文头中。
	 * @param requestBytes
	 * @param attachments
	 * @return
	 * @throws RemoteException
	 */
	byte[] acall(byte[] requestBytes, Map<String, Object> attachments) throws RemoteException;
}
