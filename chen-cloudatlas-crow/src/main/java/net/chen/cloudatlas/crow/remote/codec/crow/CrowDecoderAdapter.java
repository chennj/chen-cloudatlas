package net.chen.cloudatlas.crow.remote.codec.crow;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.tinylog.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.exception.MethodNotImplException;
import net.chen.cloudatlas.crow.common.utils.UrlUtil;
import net.chen.cloudatlas.crow.remote.RemoteException;
import net.chen.cloudatlas.crow.remote.codec.AbstractDecoder;
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
		
		throw new MethodNotImplException();
	}
	
	/**
	 * use crow binary protocol 2.* version to decode
	 * @param ctx
	 * @param channel
	 * @param buffer
	 * @return
	 */
	protected Object decode2(ChannelHandlerContext ctx, Channel channel, ByteBuf buffer) throws Exception{
		// TODO Auto-generated method stub
		throw new MethodNotImplException();
	}
}
