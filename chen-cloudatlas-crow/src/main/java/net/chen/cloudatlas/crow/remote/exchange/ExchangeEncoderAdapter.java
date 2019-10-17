package net.chen.cloudatlas.crow.remote.exchange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.common.serialize.ObjectOutput;
import net.chen.cloudatlas.crow.common.serialize.Serializer;
import net.chen.cloudatlas.crow.remote.codec.AbstractEncoder;
import net.chen.cloudatlas.crow.remote.codec.crow.CrowEncoderAdapter;
import net.chen.cloudatlas.crow.remote.support.crow.CrowHeader;
import net.chen.cloudatlas.crow.remote.support.crow.CrowHeartbeatMessage;
import net.chen.cloudatlas.crow.remote.support.crow.CrowStatus;

public class ExchangeEncoderAdapter extends CrowEncoderAdapter{

	private final AbstractEncoder abstractEncoder;
	
	public ExchangeEncoderAdapter(AbstractEncoder abstractEncoder) {
		super(abstractEncoder);
		this.abstractEncoder = abstractEncoder;
	}

	@Override
	protected byte[] serializeData(CrowHeader message) {
		
		byte[] result = null;
		
		if (message.isHeartbeat()){
			result = new byte[0];
		} else if (message instanceof ExchangeResponse && CrowStatus.SERVER_ERROR.equals(message.getStatus())){
			//如果是response，并且status错误，则返回errMsg
			result = ((ExchangeResponse)message).getErrorMsg().getBytes();
		} else {
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			Serializer serializer = abstractEncoder.getSerializer();
			if (Logger.isDebugEnabled()){
				Logger.debug("serializer: "+serializer.getName());
			}
			try {
				ObjectOutput objOut = serializer.serialize(byteOut);
				objOut.writeObject(((RpcData)message).getData());
				objOut.flushBuffer();
				result = byteOut.toByteArray();
			} catch (IOException e){
				Logger.error("IOException while serialize ",e);
			}
		}
		
		return result;
	}

	@Override
	protected boolean isRequest(Object msg) {
		
		if (msg instanceof CrowHeartbeatMessage){
			return true;
		} else if (msg instanceof ExchangeRequest){
			return true;
		} else if (msg instanceof ExchangeResponse){
			return false;
		} else {
			throw new IllegalArgumentException("msg must be ExchangeRequest or ExchangeResponse, actual: "+msg);
		}
	}

	
}
