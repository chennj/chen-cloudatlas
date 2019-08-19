package org.chen.cloudatlas.crow.remote.exchange.header;

import org.chen.cloudatlas.crow.remote.AbstractCrowControlListener;
import org.chen.cloudatlas.crow.remote.Channel;
import org.chen.cloudatlas.crow.remote.RemoteException;
import org.chen.cloudatlas.crow.remote.exchange.ExchangeChannel;
import org.chen.cloudatlas.crow.remote.exchange.ExchangeListener;
import org.chen.cloudatlas.crow.remote.exchange.ExchangeRequest;
import org.chen.cloudatlas.crow.remote.exchange.ExchangeResponse;
import org.chen.cloudatlas.crow.remote.support.crow.CrowStatus;
import org.tinylog.Logger;

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
	public void connected(Channel context) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disconnected(Channel context) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sent(Channel context, Object message) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void received(Channel context, Object message) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void caught(Channel context, Throwable exception) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object reply(ExchangeChannel context, Object request) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
