package net.chen.cloudatlas.crow.remote.codec.cwhead;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.Protocols;
import net.chen.cloudatlas.crow.remote.codec.AbstractDecoder;
import net.chen.cloudatlas.crow.remote.support.cwhead.CwheadHeader;
import net.chen.cloudatlas.crow.remote.support.cwhead.CwheadMessage;

/**
 * 
 * @author chenn
 *
 */
public class CwheadDecoderImpl extends AbstractDecoder{

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
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		
		//报文头没到齐，直接返回，等待下一次数据到来时再运行到这。
		if (in.readableBytes() < CwheadHeader.CWHEAD_LENGTH){
			return;
		}
		
		//mark
		in.markReaderIndex();
		
		//read header
		short id = in.readShortLE();//little-endian读取 LE表示little-endian
		short version = in.readShortLE();
		int logId = in.readIntLE();
		byte[] provider = new byte[CwheadHeader.PROVIDER_LENGTH];
		in.readBytes(provider);
		int magicNum = in.readIntLE();
		int contentLen = in.readIntLE();
		
		//content没到齐，直接返回，等待下一次数据到来时再运行
		if (in.readableBytes() < contentLen){
			in.resetReaderIndex();
			return;
		}
		
		//read body
		byte[] content = new byte[contentLen];
		in.readBytes(content);
		
		CwheadMessage response = new CwheadMessage(content);
		
	}

}
