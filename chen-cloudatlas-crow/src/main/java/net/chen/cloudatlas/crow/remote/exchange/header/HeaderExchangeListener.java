package net.chen.cloudatlas.crow.remote.exchange.header;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.common.DcType;
import net.chen.cloudatlas.crow.config.CrowServerContext;
import net.chen.cloudatlas.crow.remote.AbstractCrowControlListener;
import net.chen.cloudatlas.crow.remote.Channel;
import net.chen.cloudatlas.crow.remote.RemoteException;
import net.chen.cloudatlas.crow.remote.exchange.DefaultFuture;
import net.chen.cloudatlas.crow.remote.exchange.ExchangeChannel;
import net.chen.cloudatlas.crow.remote.exchange.ExchangeListener;
import net.chen.cloudatlas.crow.remote.exchange.ExchangeRequest;
import net.chen.cloudatlas.crow.remote.exchange.ExchangeResponse;
import net.chen.cloudatlas.crow.remote.support.crow.CrowStatus;

public class HeaderExchangeListener extends AbstractCrowControlListener implements ExchangeListener{

	private final ExchangeListener listener;
	
	public HeaderExchangeListener(ExchangeListener listener){
		
		if (null == listener){
			throw new IllegalArgumentException("listener is null");
		}
		this.listener = listener;
	}
	
	protected ExchangeResponse handleRequest(ExchangeChannel channel, ExchangeRequest req) throws RemoteException{
		
		ExchangeResponse res = new ExchangeResponse(req.getRequestId());
		res.setMajorVersion(req.getMajorVersion());
		res.setMinorVersion(req.getMinorVersion());
		res.setServiceId(req.getServiceId());
		res.setServiceVersion(req.getServiceVersion());
		
		Object msg = req.getData();
		try {
			// 如果req中exception不为空，说明反序列化ExchangeRequest时报错，
			// 比如server端classNotFound的情况。
			// 这种情况还没有到reply那一步
			if (null == msg && req.getException()!=null){
				// 抛出，让下面抓住，构建ExchangeResponse对象
				throw new Exception(req.getException());
			}
			
			// 处理data
			Object result = listener.reply(channel, msg);
			res.setStatus(CrowStatus.OK);
			res.setData(result);
		} catch (Exception e){
			Logger.error("Exception while replying msg ", e);
			res.setStatus(CrowStatus.SERVER_ERROR);
			res.setErrorMsg(e.toString());
		}
		
		return res;
	}
	
	protected static void handleResponse(Channel channel, ExchangeResponse res) throws RemoteException{
		
		if (null != res){
			DefaultFuture.received(channel, res);
		}
	}
	
	protected static void handleChannelError(Channel channel) throws RemoteException{
		
		DefaultFuture.received(channel, null);
	}
	
	@Override
	public void connected(Channel channel) throws RemoteException {
		
		ExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);
		try {
			listener.connected(exchangeChannel);
		} finally {
			HeaderExchangeChannel.removeChannelIfDisconnected(channel);
		}
	}

	@Override
	public void disconnected(Channel channel) throws RemoteException {
		
		ExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);
		try {
			listener.disconnected(exchangeChannel);
		} finally {
			HeaderExchangeChannel.removeChannelIfDisconnected(channel);
			handleChannelError(channel);
		}
	}

	@Override
	public void sent(Channel channel, Object message) throws RemoteException {
		
		Exception exception = null;
		try {
			ExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);
			try {
				listener.sent(exchangeChannel, message);
			} finally {
				HeaderExchangeChannel.removeChannelIfDisconnected(channel);
			}
		} catch (Exception e){
			Logger.error("error while sending message {}",e);
			exception =  e;
		}
		
		if (message instanceof ExchangeRequest){
			ExchangeRequest req = (ExchangeRequest) message;
			DefaultFuture.sent(channel, req);
		}
		
		if (null != exception){
			
			if (exception instanceof RuntimeException){
				throw (RuntimeException)exception;
			} else if (exception instanceof RemoteException){
				throw (RemoteException) exception;
			} else {
				throw new RemoteException(channel.getLocalAddress(),channel.getRemoteAddress(),exception.getMessage(),exception);
			}
		}
	}

	@Override
	public void received(Channel channel, Object message) throws RemoteException {
		
		ExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);
		try {
			if (isRejected(channel, message, ExchangeResponse.class)){
				// 被黑名单拦截
				return;
			}
			
			if (isThrottled(channel, message, ExchangeResponse.class)){
				// 被流量限制拦截
				return;
			}
			
			if (message instanceof ExchangeRequest){
				ExchangeRequest req = (ExchangeRequest) message;
				if (req.isHeartbeat()){
					Logger.debug("heart beat received from {}",channel.getRemoteAddress());
				} else if (!req.isOneWay()){
					ExchangeResponse res = handleRequest(exchangeChannel, req);
					if (null != CrowServerContext.getConfig()){
						res.setSourceDc((byte)CrowServerContext.getConfig().getApplicationConfig().getDc().toInt());
					} else {
						res.setSourceDc((byte)DcType.SHANGHAI.toInt());
					}
					channel.send(res);
				} else {
					listener.received(exchangeChannel, req.getData());
				}
			} else if (message instanceof ExchangeResponse){
				handleResponse(channel, (ExchangeResponse)message);
			} else {
				listener.received(exchangeChannel, message);
			}
		} finally {
			HeaderExchangeChannel.removeChannelIfDisconnected(channel);
		}
	}

	@Override
	public void caught(Channel channel, Throwable exception) throws RemoteException {
		
		ExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);
		try {
			listener.caught(exchangeChannel, exception);
		} finally {
			HeaderExchangeChannel.removeChannelIfDisconnected(channel);
			handleChannelError(channel);
		}
	}

	@Override
	public Object reply(ExchangeChannel channel, Object request) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
