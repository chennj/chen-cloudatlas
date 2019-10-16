package net.chen.cloudatlas.crow.remote.codec.cwhead;

import java.util.List;

import org.tinylog.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.Protocols;
import net.chen.cloudatlas.crow.remote.codec.AbstractEncoder;
import net.chen.cloudatlas.crow.remote.support.cwhead.CwheadHeader;
import net.chen.cloudatlas.crow.remote.support.cwhead.CwheadMessage;

public class CwheadEncoderImpl extends AbstractEncoder{

	@Override
	public String getName() {
		return Protocols.CROW_HEAD + Constants.DEFAULT_PROTOCOL_VERSION;
	}

	@Override
	public LengthFieldBasedFrameDecoder getLengthFieldBasedFrameDecoder(int maxFrameLength) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 使用little-endian模式
	 */
	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, List out) throws Exception {
		
		ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
		
		CwheadMessage request = (CwheadMessage)msg;
		if (Logger.isDebugEnabled()){
			Logger.debug(request.toString());
		}
		
		//header
		buffer.writeShortLE(request.getId());
		buffer.writeShortLE(request.getVersion());
		buffer.writeIntLE(request.getLogId());
		
		byte[] provider = request.getProvider();		
		if (provider.length > CwheadHeader.PROVIDER_LENGTH){
			throw new RuntimeException("provider length must be less than "+CwheadHeader.PROVIDER_LENGTH);
		} else {
			buffer.writeBytes(provider);
		}
		
		buffer.writeIntLE(request.getMagicNum());
		buffer.writeIntLE(request.getContent().length);
		
		//body
		buffer.writeBytes(request.getContent());
		
		out.add(buffer);
		
	}

}
