package net.chen.cloudatlas.crow.remote;

import java.net.InetSocketAddress;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.common.KeyUtil;
import net.chen.cloudatlas.crow.config.CrowServerContext;
import net.chen.cloudatlas.crow.config.ServiceConfig;
import net.chen.cloudatlas.crow.remote.support.crow.CrowHeader;
import net.chen.cloudatlas.crow.remote.support.crow.CrowRequest;
import net.chen.cloudatlas.crow.remote.support.crow.CrowResponse;
import net.chen.cloudatlas.crow.remote.support.crow.CrowStatus;

public class AbstractCrowControlListener {

	/**
	 * 是否被黑白名单拦截
	 * @param ctx
	 * @param message
	 * @param responseClass
	 * @return
	 */
	public boolean isRejected(Channel ctx, Object message, Class<?> responseClass){
		
		Message msg = (Message)message;
		
		String serviceKey = KeyUtil.getServiceKey(msg.getServiceId(), msg.getServiceVersion());
		
		if (!(msg instanceof Request)){
			return false;
		}
		
		Request request = (Request)msg;
		
		InetSocketAddress insocket = (InetSocketAddress)ctx.getRemoteAddress();
		String clientIP = insocket.getAddress().getHostAddress();
		if (CrowServerContext.getConfig() == null){
			return false;
		}
		
		ServiceConfig<?> sc = CrowServerContext.getServiceConfig(msg.getServiceId(),msg.getServiceVersion());
		
		if (IPFilter.isRejected(sc, clientIP)){
			
			try{
				CrowResponse response = (CrowResponse) responseClass.newInstance();
				response.setServiceId(serviceKey);
				response.setRequestId(((CrowRequest)request).getRequestId());
				response.setStatus(CrowStatus.SERVICE_REJECTED);
				response.setMajorVersion(((CrowRequest)request).getMajorVersion());
				response.setMinorVersion(((CrowRequest)request).getMinorVersion());
				ctx.send(response);
				return true;
			} catch (Exception e){
				Logger.error("reject status code send error", e);
				return false;
			}
		} else {
			return false;
		}
	}
	
	/**
	 * 是否被限流配置拦截
	 * @param ctx
	 * @param message
	 * @param responseClass
	 * @return
	 */
	public boolean isThrottled(Channel ctx, Object message, Class<?> responseClass){
		
		Message msg = (Message)message;
		
		String serviceKey = KeyUtil.getServiceKey(msg.getServiceId(), msg.getServiceVersion());
		
		if (!CrowServerContext.isThrottleOpen()){
			//有一个服务打开控制，就算启动了控制
			return false;
		}
		
		if (!(msg instanceof Request)){
			return false;
		}
		
		Request request = (Request) msg;
		
		if (request instanceof CrowRequest){
			//非CrowRequest协议不支持限流
			if (CrowThrottle.getInstance().getToken(((CrowHeader)request).getLength(), serviceKey)){
				//不需要拦截
				return false;
			} else {
				try{
					CrowResponse response = (CrowResponse)responseClass.newInstance();
					response.setServiceId(serviceKey);
					response.setRequestId(((CrowRequest)request).getRequestId());
					response.setStatus(CrowStatus.SERVICE_EXCEEDTHROTTLE);
					response.setMajorVersion(((CrowRequest)request).getMajorVersion());
					response.setMinorVersion(((CrowRequest)request).getMinorVersion());
					ctx.send(response);
					return true;
				} catch (Exception e){
					Logger.error("throttle status code send error",e);
					return false;
				}
			}
			
		} else {
			return false;
		}
	}
}
