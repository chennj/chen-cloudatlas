package net.chen.cloudatlas.crow.remote.codec.crow;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.tinylog.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.TooLongFrameException;
import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.utils.SM4Util;
import net.chen.cloudatlas.crow.common.utils.UrlUtil;
import net.chen.cloudatlas.crow.config.CrowClientContext;
import net.chen.cloudatlas.crow.config.CrowServerContext;
import net.chen.cloudatlas.crow.remote.Compressor;
import net.chen.cloudatlas.crow.remote.RemoteException;
import net.chen.cloudatlas.crow.remote.codec.AbstractDecoder;
import net.chen.cloudatlas.crow.remote.support.crow.CrowHeader;
import net.chen.cloudatlas.crow.remote.support.crow.CrowRequest;
import net.chen.cloudatlas.crow.remote.support.crow.CrowResponse;
import net.chen.cloudatlas.crow.remote.support.crow.CrowStatus;

/**
 * crow_binary decoder adapter
 * 
 * @author chenn
 *
 */
public class CrowDecoderAdapter {

	private static final int DECODE_VERSION_TOTAL_SIZE = 3;
	
	private static final int DECODE_VERSION_OFFSET = 2;
	
	private static final int DECODE2_BUFFER_LENGTH_OFFSET = 3;
	
	private final AbstractDecoder abstractDecoder;
	
	public CrowDecoderAdapter(AbstractDecoder decoder){
		this.abstractDecoder = decoder;
	}
	
	public Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buffer) throws Exception{
		
		int readable = buffer.readableBytes();
		
		if (readable < DECODE_VERSION_TOTAL_SIZE){
			return null;
		}
		
		//读取header
		int version = buffer.getByte(buffer.readerIndex()+DECODE_VERSION_OFFSET);
		byte majorByte = (byte)((version >>> 4) & 0x0F);
		byte minorByte = (byte)(version & 0x0F);
		
		String protocolVersionStr = Constants.DEFAULT_PROTOCOL_VERSION;
		if (
				this.abstractDecoder != null
				&& this.abstractDecoder.getUrl() != null
				&& this.abstractDecoder.getUrl().getParameter(Constants.PROTOCOL_VERSION) != null){
			
			protocolVersionStr = this.abstractDecoder.getUrl().getParameter(Constants.PROTOCOL_VERSION);
		}
		
		if (majorByte == CrowCodecVersion.V10.getMajorByte() && minorByte == CrowCodecVersion.V10.getMinorByte()){
			
			if (!CrowCodecVersion.V10.getVersion().equals(protocolVersionStr)){
				Logger.debug("crow decoder compatibility works. the decoder's version is "
						+ protocolVersionStr + ", but the request header's protocol version is"
						+ CrowCodecVersion.V10.getVersion() + ". now use protocol version "
						+ CrowCodecVersion.V10.getVersion() + " to decode");
			}
			
			return decode1(ctx, channel, buffer);
			
		} else if (majorByte == CrowCodecVersion.V20.getMajorByte() && minorByte == CrowCodecVersion.V20.getMinorByte()){
			
			if (!CrowCodecVersion.V20.getVersion().equals(protocolVersionStr)){
				Logger.debug("crow decoder compatibility works. the decoder's version is "
						+ protocolVersionStr + ", but the request header's protocol version is"
						+ CrowCodecVersion.V20.getVersion() + ". now use protocol version "
						+ CrowCodecVersion.V20.getVersion() + " to decode");
			}
			
			return decode2(ctx, channel, buffer);
			
		} else {
			Logger.warn("CrowDecode invalid crow protocol version. majorbyte:"+majorByte+",minorbyte:"+minorByte);
			return decode1(ctx, channel, buffer);
		}
	}

	/**
	 * use crow binary protocol 1.* version to decode
	 * @param ctx
	 * @param channel
	 * @param buffer
	 * @return
	 */
	protected Object decode1(ChannelHandlerContext ctx, Channel channel, ByteBuf buffer) throws Exception{
		
		int readable = buffer.readableBytes();
		
		if (readable < Constants.SIZE_TOTAL){
			return null;
		}
		
		buffer.markReaderIndex();
		
		//读取header
		short magic = buffer.readShort();
		
		//判断前两个字节是否是1B1B，如果不是，则属于非法报文，丢弃
		if (magic != Constants.MAGIC){
			Logger.error("received error magic number, exception:{}. ignore this wrong data","0x"+Integer.toHexString(Constants.MAGIC));
			channel.close();
			throw new Exception("magic error, the channel will be close");
		}
		
		int version = buffer.readByte();
		
		//5位保留位
		//[位保留位(5位)][心跳(1位)][单向(1位)][请求(1位)] = 8位 = 一个字节
		byte flag = buffer.readByte();
		
		byte heartbeatByte = (byte)((flag >> 2) & 0x01);
		byte onewayByte = (byte)((flag >> 1) & 0x01);
		byte requestByte = (byte)((flag >> 0) & 0x01);
		
		boolean isHeartbeat = heartbeatByte == 1 ? true : false;
		boolean isOneway = onewayByte == 1 ? true : false;
		boolean isRequest = requestByte == 1 ? true : false;
		
		byte statusByte = buffer.readByte();
		CrowStatus status = CrowStatus.valueOf(statusByte);
		
		//收到应答时，校验应答的status是否合法
		if (!isHeartbeat && !isRequest && null == status){
			//非法的应答码
			buffer.resetReaderIndex();
			
			SocketAddress remoteAddress = ctx.channel().remoteAddress();
			String address = "";
			if (remoteAddress instanceof InetSocketAddress){
				address = UrlUtil.getAddressKey((InetSocketAddress)remoteAddress);
			}
			
			String statusStr = "0x" + Integer.toHexString(statusByte);
			Logger.error("illegal status code {} in response from {}", statusStr,address);
			throw new RemoteException("illegal status code "+statusStr+"in response from "+address);
		}
		
		long requestId = buffer.readLong();
		String callerId = new String(buffer.readBytes(Constants.SIZE_CALLERID).array()).trim();
		String serviceId = new String(buffer.readBytes(Constants.SIZE_SERVICEID).array()).trim();
		int length = buffer.readInt();
		
		if (readable < length + Constants.SIZE_TOTAL){
			//数据长度不足
			buffer.resetReaderIndex();
			return null;
		}
		
		//组装message
		byte[] bytes = new byte[length];
		
		buffer.readBytes(bytes);
		
		CrowHeader message = createMessage(isRequest);
		
		message.setMagic(magic);
		message.setMajorVersion((byte)((version >>> 4) & 0x0F));
		message.setMinorVersion((byte)((version >>> 0) & 0x0F));
		
		message.setHeartbeat(isHeartbeat);
		message.setOneWay(isOneway);
		message.setRequest(isRequest);
		message.setRequestId(requestId);
		message.setCallerId(callerId);
		message.setServiceId(serviceId);
		message.setServiceVersion(Constants.DEFAULT_SERVICE_VERSION);
		message.setLength(length);
		
		if (null != status){
			message.setStatus(status);
		}
		
		if (isHeartbeat){
			Logger.debug("received heartbeat from " + channel.remoteAddress());
		} else {
			// step1. 先解压缩数据
			bytes = uncompressData(bytes);
			// step2. 反序列化数据
			deserializeData(message, bytes);
			// step3. 打印
			if (Constants.LOGGIND_MESSAGE){
				Logger.info(message);
			} else {
				Logger.debug(message);
			}
		}
		
		return message;
	}
	
	/**
	 * use crow binary protocol 2.* version to decode
	 * @param ctx
	 * @param channel
	 * @param buffer
	 * @return
	 */
	protected Object decode2(ChannelHandlerContext ctx, Channel channel, ByteBuf buffer) throws Exception{

		boolean isValidate = lengthFieldBasedFrameDecoder(ctx, buffer);
		if (!isValidate){
			return null;
		}
		
		buffer.markReaderIndex();
		
		//读取header
		short magic = buffer.readShort();
		
		//判断前两个字节是否是1B1B，如果不是，则属于非法报文，丢弃
		if (magic != Constants.MAGIC){
			Logger.error("received error magic number, exception:{}. ignore this wrong data","0x"+Integer.toHexString(Constants.MAGIC));
			channel.close();
			throw new Exception("magic error, the channel will be close");
		}
		
		int version = buffer.readByte();
		byte majorByte = (byte)((version >>> 4) & 0x0F);
		byte minorByte = (byte)((version >>> 0) & 0x0F);
		
		boolean iscrowVersionInvalid = false;
		if (majorByte != CrowCodecVersion.V20.getMajorByte() || minorByte != CrowCodecVersion.V20.getMinorByte()){
			Logger.error("invalid crow protocol version. expected: " + CrowCodecVersion.V20.getVersion());
			iscrowVersionInvalid = true;
		}
		
		int contentLen = buffer.readInt();
		
		if (iscrowVersionInvalid){
			buffer.skipBytes(contentLen);
			return null;
		}
		
		//5位保留位
		//[位保留位(5位)][心跳(1位)][单向(1位)][请求(1位)] = 8位 = 一个字节
		byte flag = buffer.readByte();
		
		byte heartbeatByte = (byte)((flag >> 2) & 0x01);
		byte onewayByte = (byte)((flag >> 1) & 0x01);
		byte requestByte = (byte)((flag >> 0) & 0x01);
		
		boolean isHeartbeat = heartbeatByte == 1 ? true : false;
		boolean isOneway = onewayByte == 1 ? true : false;
		boolean isRequest = requestByte == 1 ? true : false;
		
		byte statusByte = buffer.readByte();
		CrowStatus status = CrowStatus.valueOf(statusByte);
		
		//收到应答时，校验应答的status是否合法
		if (!isHeartbeat && !isRequest && null == status){
			//非法的应答码
			buffer.resetReaderIndex();
			
			SocketAddress remoteAddress = ctx.channel().remoteAddress();
			String address = "";
			if (remoteAddress instanceof InetSocketAddress){
				address = UrlUtil.getAddressKey((InetSocketAddress)remoteAddress);
			}
			
			String statusStr = "0x" + Integer.toHexString(statusByte);
			Logger.error("illegal status code {} in response from {}", statusStr,address);
			throw new RemoteException("illegal status code "+statusStr+"in response from "+address);
		}
		
		long requestId = buffer.readInt();
		byte sourceDc = buffer.readByte();
		short serviceIdLen = buffer.readShort();
		String serviceId = new String(buffer.readBytes(serviceIdLen).array()).trim();
		String serviceVersion = new String(buffer.readBytes(Constants.SIZE_SERVICE_VERSION).array()).trim();
		short traceLen = buffer.readShort();
		String traceId = new String(buffer.readBytes(traceLen).array()).trim();
		int length = buffer.readInt();
		
		//组装message
		byte[] bytes = new byte[length];
		
		buffer.readBytes(bytes);
		
		CrowHeader message = createMessage(isRequest);
		
		message.setMagic(magic);
		message.setMajorVersion(majorByte);
		message.setMinorVersion(minorByte);
		
		message.setSourceDc(sourceDc);
		
		message.setHeartbeat(isHeartbeat);
		message.setOneWay(isOneway);
		message.setRequest(isRequest);
		message.setRequestId(requestId);
		message.setCallerId("");
		message.setServiceId(serviceId);
		message.setServiceVersion(serviceVersion);
		message.setTraceId(traceId);
		message.setLength(length);
		
		if (null != status){
			message.setStatus(status);
		}
		
		if (isHeartbeat){
			Logger.debug("received heartbeat from " + channel.remoteAddress());
		} else {
			// step1. 进行解密（如果存在解密要求）
			String pass = null;
			if (isRequest){
				pass = CrowServerContext.getPassByService(serviceId, serviceVersion);
			} else {
				pass = CrowClientContext.getPassByService(serviceId, serviceVersion);
			}
			bytes = decryptData(bytes, pass);
			// step2. 先解压缩数据
			bytes = uncompressData(bytes);
			// step3. 反序列化数据
			deserializeData(message, bytes);
			// step4. 打印
			if (Constants.LOGGIND_MESSAGE){
				Logger.info(message);
			} else {
				Logger.debug(message);
			}
		}
		
		return message;
	}
	
	protected CrowHeader createMessage(boolean isRequest){
		return isRequest ? new CrowRequest() : new CrowResponse();
	}
	
	/**
	 * crow binary 协议报文体反系列化
	 * @param message request 对象
	 * @param bytes 报文体内容
	 */
	protected void deserializeData(CrowHeader message, byte[] bytes){
		
		if (message instanceof CrowRequest){
			((CrowRequest)message).setRequestBytes(bytes);
		} else if (message instanceof CrowResponse){
			((CrowResponse)message).setResponseBytes(bytes);
		}
	}
	
	/**
	 * corw binary 协议报文体解压缩
	 * @param bytes
	 * @return
	 */
	protected byte[] uncompressData(byte[] bytes){
		
		byte[] result = bytes;
		Compressor compressor = abstractDecoder.getCompressor();
		if (null != compressor){
			try {
				result = compressor.uncompress(bytes, 0, bytes.length);
			} catch (IOException e){
				Logger.error("compress error.",e);
			}
		}
		
		return result;
	}
	
	protected boolean lengthFieldBasedFrameDecoder(ChannelHandlerContext ctx, ByteBuf buffer){
		
		int actualLengthFieldOffset = buffer.readerIndex() + DECODE2_BUFFER_LENGTH_OFFSET;
		int lengthFieldEndOffset = DECODE2_BUFFER_LENGTH_OFFSET + 4;
		
		if (buffer.readableBytes() < lengthFieldEndOffset){
			return false;
		}
		
		long frameLength = buffer.getUnsignedInt(actualLengthFieldOffset);
		frameLength += lengthFieldEndOffset;
		int frameLengthInt = (int)frameLength;
		if (buffer.readableBytes() < frameLengthInt){
			return false;
		}
		
		int contentLength = Constants.SIZE_FLAG + Constants.SIZE_STATUS_CODE + Constants.SIZE_REQUEST_ID 
				+ Constants.SIZE_DC + Constants.SIZE_SERVICEID_LEN + 0 
				+ 0 + Constants.SIZE_TRACE_ID_LEN
				+ 0 + Constants.SIZE_PAYLOAD_LEN;
		if (frameLengthInt - lengthFieldEndOffset < contentLength){
			return false;
		}
		
		int maxFrameLength = Constants.DEFAULT_MAX_MSG_SIZE;
		if (
				abstractDecoder.getUrl() != null
				&& abstractDecoder.getUrl().getParameter(Constants.MAX_MSG_SIZE) != null){
			maxFrameLength = Integer.parseInt(abstractDecoder.getUrl().getParameter(Constants.MAX_MSG_SIZE));
		}
		
		if (frameLength > maxFrameLength){
			ctx.fireExceptionCaught(
					new TooLongFrameException("adjusted frame length exceeds " + maxFrameLength + ": " + frameLength + " - d"));
		}
		
		return true;

	}
	
	/**
	 * crow binary 2 解密
	 * @param sourceData
	 * @param password
	 * @return
	 * @throws Exception
	 */
	protected byte[] decryptData(byte[] sourceData, String password) throws Exception{
		
		if (null == password || password.isEmpty()){
			return sourceData;
		}
		
		return SM4Util.decryptMessageBySM4(sourceData, password);
	}
}
