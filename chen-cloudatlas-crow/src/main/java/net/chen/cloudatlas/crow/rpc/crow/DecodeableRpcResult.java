package net.chen.cloudatlas.crow.rpc.crow;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.OutputStream;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.remote.Channel;
import net.chen.cloudatlas.crow.remote.Decodeable;
import net.chen.cloudatlas.crow.remote.exchange.ExchangeResponse;
import net.chen.cloudatlas.crow.remote.support.crow.CrowStatus;
import net.chen.cloudatlas.crow.rpc.Invocation;
import net.chen.cloudatlas.crow.rpc.impl.RpcResult;

public class DecodeableRpcResult extends RpcResult implements Decodeable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Channel channel;
	
	private byte serializationType;
	
	private InputStream inputStream;
	
	private ExchangeResponse response;
	
	private Invocation invocation;
	
	private volatile boolean hasDecoded;
	
	public static final byte RESPONSE_WITH_EXCEPTIONI = 0;
	
	public static final byte RESPONSE_VALUE = 1;
	
	public static final byte RESPONSE_NULL_VALUE = 2;
	
	public DecodeableRpcResult(Channel chennal, ExchangeResponse response, InputStream is, Invocation invocation, byte id){
		
		this.channel = channel;
		this.response = response;
		this.inputStream = is;
		this.invocation = invocation;
		this.serializationType = id;
		
	}

	public void encode(Channel channel, OutputStream os, Object message) throws IOException{
		throw new UnsupportedOperationException();
	}

	@Override
	public void decode() throws Exception {
		
		if (!hasDecoded && channel != null && inputStream != null){
			
			try {
				decode(channel, inputStream);
			} catch (Exception e){
				Logger.error("error while decoding input.",e);
				
				response.setStatus(CrowStatus.BAD_REQUEST);
				response.setErrorMsg(e.toString());
			} finally {
				hasDecoded = true;
			}
		}
	}

	public Object decode(Channel channel, InputStream is) throws IOException{
		
		ObjectInput in = new ObjectInputStream(is);
		
		byte flag = in.readByte();
		
		switch (flag){
		case RESPONSE_NULL_VALUE:
			break;
		case RESPONSE_VALUE:
			try {
				Object obj = in.readObject();
				setValue(obj);
			} catch (ClassNotFoundException e){
				Logger.error("ClassNotFoundException.",e);
				throw new IOException(e.toString());
			}
			break;
		case RESPONSE_WITH_EXCEPTIONI:
			try {
				Object o = in.readObject();
				if (!(o instanceof Throwable)){
					throw new IOException("Response data error, expect Throwable, but get "+o);
				}
				setException((Throwable)o);
			} catch (ClassNotFoundException e){
				Logger.error("ClassNotFoundException.",e);
				throw new IOException(e.toString());
			}
			break;
		default:
			throw new IOException("Unknown result flag,expect 0,1,2, get "+flag);
		}
		return this;
	}
	
}
