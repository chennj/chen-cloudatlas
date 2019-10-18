package net.chen.cloudatlas.crow.rpc.crow;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.OutputStream;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.remote.Channel;
import net.chen.cloudatlas.crow.remote.Decodeable;
import net.chen.cloudatlas.crow.remote.RemoteException;
import net.chen.cloudatlas.crow.remote.exchange.ExchangeRequest;
import net.chen.cloudatlas.crow.rpc.impl.RpcInvocation;

public class DecodeableRpcInvocation extends RpcInvocation implements Decodeable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Channel channel;
	
	private byte serializationType;
	
	private InputStream inputStream;
	
	private ExchangeRequest request;
	
	private volatile boolean hasDecoded;
	
	public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
	
	public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
	
	public DecodeableRpcInvocation(Channel channel, ExchangeRequest request, InputStream inputStream, byte id){
		
		this.channel = channel;
		this.request = request;
		this.inputStream = inputStream;
		this.serializationType = id;
	}

	@Override
	public void decode() throws Exception {
		
		if (!hasDecoded && channel != null && inputStream != null){
			
			try {
				decode(channel,inputStream);
			} catch (Exception e){
				Logger.error("error while decoding input.",e);
			} finally{
				hasDecoded = true;
			}
			
		}
		
	}
	
	public void encode(Channel channel, OutputStream os, Object message) throws IOException{
		throw new UnsupportedOperationException();
	}
	
	public Object decode(Channel channel, InputStream is) throws IOException{
		
		ObjectInput in = new ObjectInputStream(is);
		
		setAttachment("crow", in.readUTF());
		setMethodName(in.readUTF());
		
		try {
			Object[] args = null;
			Class<?>[] pts = null;
			
			String desc = in.readUTF();
			if (desc.length() == 0){
				pts = EMPTY_CLASS_ARRAY;
				args = EMPTY_OBJECT_ARRAY;
			}
			
			setParameterTypes(pts);
			setArguments(args);
		} catch (Exception e){
			Logger.error("Exception while decoding.",e);
			throw new IOException("Exception while decoding.",e);
		}
		
		return this;
	}

	public static Object decodeInvocationArgument(
			Channel channel, 
			RpcInvocation inv, 
			Class<?>[] pts, 
			int paraIndex, 
			Object inObject) throws IOException{
		
		//如果是callback，则创建proxy到客户端，方法的执行可通过channel调用到client端的callback接口
		//decode时需要根据channel及env获取url
		URL url = null;
		try {
			url = CrowProtocol.getCrowProtocol().getInvoker(channel, inv).getUrl();
		} catch (RemoteException e){
			Logger.error("RemoteException.",e);
		}
		
		return inObject;
	}
}
