package net.chen.cloudatlas.crow.remote.exchange;

import java.io.ByteArrayInputStream;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.common.serialize.Serializer;
import net.chen.cloudatlas.crow.remote.codec.AbstractDecoder;
import net.chen.cloudatlas.crow.remote.codec.crow.CrowDecoderAdapter;
import net.chen.cloudatlas.crow.remote.support.crow.CrowHeader;
import net.chen.cloudatlas.crow.remote.support.crow.CrowStatus;

public class ExchangeDecoderAdapter extends CrowDecoderAdapter{

	private final AbstractDecoder abstractDecoder;
	
	public ExchangeDecoderAdapter(AbstractDecoder abstractDecoder) {
		super(abstractDecoder);
		this.abstractDecoder = abstractDecoder;
	}

	@Override
	protected CrowHeader createMessage(boolean isRequest) {
		return isRequest ? new ExchangeRequest() : new ExchangeResponse();
	}

	@Override
	protected void deserializeData(CrowHeader message, byte[] bytes) {

		Serializer serializer = abstractDecoder.getSerializer();
		Logger.trace("serializer: "+serializer.getName());
		
		if (message instanceof ExchangeRequest){
			
			ExchangeRequest msg = (ExchangeRequest)message;
			msg.setRequestBytes(bytes);
			try {
				//如果serializer==null，则说明是binary的，不需要序列化 
				if (null != serializer){
					msg.setData(serializer.deserialize(new ByteArrayInputStream(bytes)).readObject());
				}
			} catch (Exception e){
				Logger.error("error ocurrs while setting requestBytes.",e);
				//系列化失败，记录异常
				msg.setException(e);
			}
			
		} else if (message instanceof ExchangeResponse){
			
			ExchangeResponse msg = (ExchangeResponse)message;
			msg.setResponseBytes(bytes);
			try {
				if (msg.getStatus().equals(CrowStatus.SERVER_ERROR)){
					msg.setErrorMsg(new String(bytes));
				} else {
					//如果serializer==null，则说明是binary的，不需要序列化 
					if (null!=serializer){
						msg.setData(serializer.deserialize(new ByteArrayInputStream(bytes)).readObject());
					}
				}
			} catch (Exception e){
				Logger.error("error ocurrs while setting responseBytes.",e);
			}
			
		}
	}

	
}
