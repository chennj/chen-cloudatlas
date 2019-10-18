package net.chen.cloudatlas.crow.remote.support.crow;

import java.util.Map;

import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.Protocols;
import net.chen.cloudatlas.crow.config.CrowClientContext;
import net.chen.cloudatlas.crow.remote.MessageWrapper;
import net.chen.cloudatlas.crow.remote.RemoteException;
import net.chen.cloudatlas.crow.remote.Request;
import net.chen.cloudatlas.crow.remote.Response;
import net.chen.cloudatlas.crow.remote.codec.crow.CrowCodecVersion;

/**
 * crow协议报文封装
 * @author chenn
 *
 */
public class CrowMessageWrapper extends MessageWrapper{

	@Override
	public String getName() {
		return Protocols.CROW_BINARY;
	}

	@Override
	public Request wrapRequest(byte[] payload, Map<String, Object> attachments) throws RemoteException {
		//填充
		CrowRequest req = new CrowRequest(payload);
		return req;
	}

	@Override
	public Request wrapRequest(Request request, Map<String, Object> attachments) throws RemoteException {
		
		if (request instanceof CrowRequest){
			
			CrowRequest req = (CrowRequest)request;
			
			if (null != attachments){
				//设置corw协议的版本
				if (attachments.get(Constants.PROTOCOL_VERSION) != null){
					String protocolVersion = (String) attachments.get(Constants.PROTOCOL_VERSION);
					CrowCodecVersion codecVersion = CrowCodecVersion.getCodecVersion(protocolVersion);
					req.setMajorVersion(codecVersion.getMajorByte());
					req.setMinorVersion(codecVersion.getMinorByte());
				}
				//如果request的serviceId没有设置，就自动设置上。
				if (req.getServiceId() == null 
						|| "".equals(req.getServiceId().trim())
						&& attachments.get(Constants.SERVICE_ID)!=null){
					
					String serviceId = (String)attachments.get(Constants.SERVICE_ID);
					req.setServiceId(serviceId);
				}
				//如果request的serviceVersion没有设置，就自动设置上。
				if (req.getServiceVersion() == null 
						|| "".equals(req.getServiceVersion().trim())
						&& attachments.get(Constants.SERVICE_VERSION)!=null){
					
					String serviceVersion = (String)attachments.get(Constants.SERVICE_VERSION);
					req.setServiceVersion(serviceVersion);
				}
				//如果request的callerId没有设置，就自动设置上。
				if (req.getCallerId() == null 
						|| "".equals(req.getCallerId().trim())
						&& attachments.get(Constants.CALLER_ID)!=null){
					
					String callerId = (String)attachments.get(Constants.CALLER_ID);
					req.setCallerId(callerId);
				}
				//将oneway设置到crow的request对象中去。
				//因为crow协议中有“是否oneway“字段，对端会根据该字段判断是否需要返回
				if (attachments.get(Constants.ONE_WAY) != null){
					boolean oneWay = (boolean)attachments.get(Constants.ONE_WAY);
					req.setOneWay(oneWay);
				}
				//把request的来源dc加入
				req.setSourceDc((byte)CrowClientContext.getConfig().getApplicationConfig().getDc().toInt());
				
			}
			
		}
		
		return request;
	}

	@Override
	public byte[] decomposeResponse(Response response) {
		if (response instanceof CrowResponse){
			return ((CrowResponse)response).getResponseBytes();
		} else {
			throw new RuntimeException("response must be CrowResponse");
		}
	}

	@Override
	public Response wrapResponse(Request request, Map<String, Object> attachments) throws RemoteException {

		Response response = null;
		if (request instanceof CrowRequest){
			
			boolean oneWay = (Boolean)attachments.get(Constants.ONE_WAY);
			((CrowRequest)request).setOneWay(oneWay);
			response = new CrowResponse((CrowRequest)request);
			
		}
		return response;
	}

	@Override
	public Response wrapResponse(Response response, Map<String, Object> attachments) throws RemoteException {
		
		String ipAndPort = (String)attachments.get(Constants.IP_AND_PORT);
		
		//check return code from server is ok or not
		CrowStatus returnStatus = ((CrowResponse)response).getStatus();
		String serviceId = ((CrowResponse)response).getServiceId();
		
		if (CrowStatus.NONE.equals(returnStatus)){
			throw new RemoteException("illegal status code "+returnStatus.toString()+" in response from "+ipAndPort);
		} else if (CrowStatus.SERVICE_NOT_FOUND.equals(returnStatus)){
			throw new RemoteException("service "+serviceId+" not found on "+ipAndPort);
		} else if (CrowStatus.SERVER_ERROR.equals(returnStatus)){
			throw new RemoteException("server "+ipAndPort+" error ");
		} else if (CrowStatus.SERVICE_NOT_STARTED.equals(returnStatus)){
			throw new RemoteException("service "+serviceId+" is not started on "+ipAndPort);
		} else if (CrowStatus.SERVER_TIMEOUT.equals(returnStatus)){
			throw new RemoteException("service "+serviceId+" timeout on "+ipAndPort);
		} else if (CrowStatus.SERVICE_EXCEEDTHROTTLE.equals(returnStatus)){
			throw new RemoteException("service "+serviceId+" exceeds the max throttleValue on "+ipAndPort);
		} else if (CrowStatus.SERVICE_REJECTED.equals(returnStatus)){
			throw new RemoteException("service "+serviceId+" was rejected by "+ipAndPort);
		}
		
		//对于crowMessage来说，前面只做校验，这里原样返回
		return response;
	}

	@Override
	public Request wrapHearbeat(String protocolVersion) throws RemoteException {

		CrowHeartbeatMessage message = new CrowHeartbeatMessage();
		CrowCodecVersion codecVersion = CrowCodecVersion.getCodecVersion(protocolVersion);
		message.setMajorVersion(codecVersion.getMajorByte());
		message.setMinorVersion(codecVersion.getMinorByte());
		return message;
	}

}
