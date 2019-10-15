package net.chen.cloudatlas.crow.remote.codec.crow;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.tinylog.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.utils.SM4Util;
import net.chen.cloudatlas.crow.config.CrowClientContext;
import net.chen.cloudatlas.crow.config.CrowServerContext;
import net.chen.cloudatlas.crow.remote.Compressor;
import net.chen.cloudatlas.crow.remote.codec.AbstractEncoder;
import net.chen.cloudatlas.crow.remote.support.crow.CrowHeader;
import net.chen.cloudatlas.crow.remote.support.crow.CrowHeartbeatMessage;
import net.chen.cloudatlas.crow.remote.support.crow.CrowRequest;
import net.chen.cloudatlas.crow.remote.support.crow.CrowResponse;

public class CrowEncoderAdapter {

	private final AbstractEncoder abstractEncoder;
	
	public CrowEncoderAdapter(AbstractEncoder abstractEncoder){
		this.abstractEncoder = abstractEncoder;
	}
	
	public Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception{
		
		CrowHeader message = (CrowHeader)msg;
		
		byte majorByte = message.getMajorVersion();
		byte minorByte = message.getMinorVersion();
		
		String protocolVersionStr = Constants.DEFAULT_PROTOCOL_VERSION;
		if (
				abstractEncoder != null 
				&& abstractEncoder.getUrl() != null
				&& abstractEncoder.getUrl().getParameter(Constants.PROTOCOL_VERSION) != null){
			
			protocolVersionStr = abstractEncoder.getUrl().getParameter(Constants.PROTOCOL_VERSION);
		}
		
		if (	majorByte == CrowCodecVersion.V10.getMajorByte()
				&& minorByte == CrowCodecVersion.V10.getMinorByte()){
			
			if (!CrowCodecVersion.V10.getVersion().equals(protocolVersionStr)){
				Logger.debug("crow encoder compatibility works. the encoder's version is "
						+ protocolVersionStr + ", but the request header's protocol version is"
						+ CrowCodecVersion.V10.getVersion() + ". now use protocol version "
						+ CrowCodecVersion.V10.getVersion() + " to encode");
			}
			
			return encode1(ctx, channel, msg);
			
		} else if (
				majorByte == CrowCodecVersion.V20.getMajorByte()
				&& minorByte == CrowCodecVersion.V20.getMinorByte()){
			
			if (!CrowCodecVersion.V20.getVersion().equals(protocolVersionStr)){
				Logger.debug("crow encoder compatibility works. the encoder's version is "
						+ protocolVersionStr + ", but the request header's protocol version is"
						+ CrowCodecVersion.V20.getVersion() + ". now use protocol version "
						+ CrowCodecVersion.V20.getVersion() + " to encode");
			}
			
			return encode2(ctx, channel, msg);
			
		} else {
			
			Logger.warn("CrowEncode invalid crow protocol version. majorbyte:"+majorByte+",minorbyte:"+minorByte);
			return encode1(ctx, channel, msg);
		}
		
	}

	/**
	 * protocol version 1
	 * @param ctx
	 * @param channel
	 * @param msg
	 * @return
	 * @throws Exception
	 */
	protected Object encode1(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception{
		
		ByteBuf result = ByteBufAllocator.DEFAULT.buffer();
		CrowHeader message = (CrowHeader)msg;
		
		byte[] bytes = serializeData(message);
		bytes = compressData(bytes);
		if (null == bytes){
			bytes = new byte[0];
		}
		message.setLength(bytes.length);
		if (Constants.LOGGIND_MESSAGE){
			Logger.info(message);
		} else {
			Logger.debug(message);
		}
		
		//header
		writeHeader1(result, message);
		//body
		result.writeBytes(bytes);
		
		return result;
	}
	
	/**
	 * protocol version 2
	 * @param ctx
	 * @param channel
	 * @param msg
	 * @return
	 * @throws Exception
	 */
	protected Object encode2(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception{
		
		ByteBuf result = ByteBufAllocator.DEFAULT.buffer();
		CrowHeader message = (CrowHeader)msg;
		
		byte[] bytes = serializeData(message);
		bytes = compressData(bytes);
		
		String pass = null;
		
		if (message.isRequest()){
			pass = CrowClientContext.getPassByService(message.getServiceId(), message.getServiceVersion());
		} else {
			pass = CrowServerContext.getPassByService(message.getServiceId(), message.getServiceVersion());
		}
		bytes = encryptData(bytes,pass);
		if (bytes == null){
			bytes = new byte[0];
		}
		
		message.setLength(bytes.length);
		if (Constants.LOGGIND_MESSAGE){
			Logger.info(message);
		} else {
			Logger.debug(message);
		}
		
		//header
		writeHeader2(result,message);
		//body
		result.writeBytes(bytes);
		
		return result;
	}
	
	protected void writeHeader1(ByteBuf out, CrowHeader message) throws IOException{
		
		out.writeShort(message.getMagic());
		out.writeByte((byte)((message.getMajorVersion() << 4) & 0xF0) | (message.getMinorVersion() & 0x0F));
		
		byte flag = 0x00;
		//3-7位保留
		flag = (byte) (((message.isHeartbeat() 	? 1 : 0) & 0x01) << 2 | flag);
		flag = (byte) (((message.isOneWay() 	? 1 : 0) & 0x01) << 1 | flag);
		flag = (byte) (((message.isRequest() 	? 1 : 0) & 0x01) << 0 | flag);
		
		out.writeByte(flag);
		
		if (isRequest(message)){
			out.writeByte((byte)0);
		} else {
			out.writeByte((((CrowResponse)message).getStatus()).value());
		}
		out.writeLong(message.getRequestId());
		if (Constants.SIZE_CALLERID - message.getCallerId().length() < 0){
			throw new IllegalArgumentException("wrong callerId size " + Constants.SIZE_CALLERID);
		}
		out.writeBytes(StringUtils.rightPad(message.getCallerId(), Constants.SIZE_CALLERID, "").getBytes());
		if (message.getServiceId().length() > Constants.SIZE_SERVICEID){
			Logger.error("serviceId can not be longer than 20 bytes in crow 1.0 protocol,"
					+ " use a shorter serviceId or crow 2. protocol instead.");
		}
		out.writeBytes(StringUtils.rightPad(message.getServiceId(), Constants.SIZE_SERVICEID, "").getBytes());
		out.writeInt(message.getLength());
	}
	
	protected void writeHeader2(ByteBuf out, CrowHeader message) throws IOException{
		
		out.writeShort(message.getMagic());
		out.writeByte((byte)((message.getMajorVersion() << 4) & 0xF0) | ((message.getMinorVersion()) & 0x0F));
		
		byte[] serviceIdBytes = message.getServiceId() == null ? "".getBytes() : message.getServiceId().getBytes();
		if (Constants.SIZE_SERVICE_VERSION - message.getServiceVersion().length() < 0){
			throw new IllegalArgumentException("wrong service version size " + Constants.SIZE_SERVICE_VERSION);
		}
		byte[] serviceVersionBytes = StringUtils.rightPad(message.getServiceVersion(), Constants.SIZE_SERVICE_VERSION, "").getBytes();
		//traceId先写死为0，等跟踪系统ok后再修改
		byte[] traceId = new byte[0];
		
		int contentLen = Constants.SIZE_FLAG + Constants.SIZE_STATUS_CODE + Constants.SIZE_REQUEST_ID 
				+ Constants.SIZE_DC + Constants.SIZE_SERVICEID_LEN + serviceIdBytes.length
				+ serviceVersionBytes.length + Constants.SIZE_TRACE_ID_LEN
				+ traceId.length + Constants.SIZE_PAYLOAD_LEN + message.getLength();
		
		out.writeInt(contentLen);
		
		byte flag = 0x00;
		//3-7位保留
		flag = (byte) (((message.isHeartbeat() 	? 1 : 0) & 0x01) << 2 | flag);
		flag = (byte) (((message.isOneWay() 	? 1 : 0) & 0x01) << 1 | flag);
		flag = (byte) (((message.isRequest()	? 1 : 0) & 0x01) << 0 | flag);
		
		out.writeByte(flag);
		if (isRequest(message)){
			out.writeByte(0);
		} else {
			out.writeByte((((CrowResponse)message).getStatus()).value());
		}
		
		out.writeInt((int)message.getRequestId());
		out.writeByte(message.getSourceDc());
		out.writeShort(serviceIdBytes.length);
		out.writeBytes(serviceIdBytes);
		out.writeBytes(serviceVersionBytes);
		out.writeShort(traceId.length);
		out.writeBytes(traceId);
		out.writeInt(message.getLength());
	}
	
	/**
	 * 报文体系列化 
	 * @param message
	 * @return
	 */
	protected byte[] serializeData(CrowHeader message){
		if (isRequest(message)){
			return ((CrowRequest)message).getRequestBytes();
		} else {
			return ((CrowResponse)message).getResponseBytes();
		}
	}
	
	/**
	 * 报文体压缩
	 * @param bytes
	 * @return
	 */
	protected byte[] compressData(byte[] bytes){
		byte[] result = bytes;
		Compressor compressor = abstractEncoder.getCompressor();
		if (null != compressor){
			try {
				result = compressor.compress(bytes);
			} catch (IOException e){
				Logger.error("compress error,",e);
			}
		}
		return result;
	}
	
	/**
	 * 加密
	 * @param sourceData
	 * @param password
	 * @return
	 * @throws Exception
	 */
	protected byte[] encryptData(byte[] sourceData, String password) throws Exception{
		
		if (null == password || password.isEmpty()){
			return sourceData;
		}
		return SM4Util.encryptMessageBySM4(sourceData, password);
	}
	
	protected boolean isRequest(Object msg){
		
		if (msg instanceof CrowHeartbeatMessage){
			return true;
		} else if (msg instanceof CrowRequest){
			return true;
		} else if (msg instanceof CrowResponse){
			return false;
		} else {
			throw new IllegalArgumentException("msg must be CrowRequest or CrowResposne, actual:"+msg);
		}
	}

	@SuppressWarnings("unchecked")
	public void encode(ChannelHandlerContext ctx, Object msg, @SuppressWarnings("rawtypes") List out) throws Exception{
		
		CrowHeader message = (CrowHeader)msg;
		
		byte majorByte = message.getMajorVersion();
		byte minorByte = message.getMinorVersion();
		
		String protocolVersionStr = Constants.DEFAULT_PROTOCOL_VERSION;
		if (
				abstractEncoder != null 
				&& abstractEncoder.getUrl() != null
				&& abstractEncoder.getUrl().getParameter(Constants.PROTOCOL_VERSION) != null){
			
			protocolVersionStr = abstractEncoder.getUrl().getParameter(Constants.PROTOCOL_VERSION);
		}
		
		if (	majorByte == CrowCodecVersion.V10.getMajorByte()
				&& minorByte == CrowCodecVersion.V10.getMinorByte()){
			
			if (!CrowCodecVersion.V10.getVersion().equals(protocolVersionStr)){
				Logger.debug("crow encoder compatibility works. the encoder's version is "
						+ protocolVersionStr + ", but the request header's protocol version is"
						+ CrowCodecVersion.V10.getVersion() + ". now use protocol version "
						+ CrowCodecVersion.V10.getVersion() + " to encode");
			}
			
			out.add(encode1(ctx, ctx.channel(), msg));
			
		} else if (
				majorByte == CrowCodecVersion.V20.getMajorByte()
				&& minorByte == CrowCodecVersion.V20.getMinorByte()){
			
			if (!CrowCodecVersion.V20.getVersion().equals(protocolVersionStr)){
				Logger.debug("crow encoder compatibility works. the encoder's version is "
						+ protocolVersionStr + ", but the request header's protocol version is"
						+ CrowCodecVersion.V20.getVersion() + ". now use protocol version "
						+ CrowCodecVersion.V20.getVersion() + " to encode");
			}
			
			out.add(encode2(ctx, ctx.channel(), msg));
			
		} else {
			
			Logger.warn("CrowEncode invalid crow protocol version. majorbyte:"+majorByte+",minorbyte:"+minorByte);
			out.add(encode1(ctx, ctx.channel(), msg));
		}
	}
}
