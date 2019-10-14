package net.chen.cloudatlas.crow.remote.codec.crow;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import net.chen.cloudatlas.crow.common.Protocols;
import net.chen.cloudatlas.crow.remote.codec.AbstractDecoder;

public class CrowDecoderImpl extends AbstractDecoder{

	private final CrowDecoderAdapter decoderAdapter = new CrowDecoderAdapter(this);
	
	
	@Override
	public String getName() {
		return Protocols.CROW_BINARY + CrowCodecVersion.V10.getVersion();
	}

	@Override
	public LengthFieldBasedFrameDecoder getLengthFieldBasedFrameDecoder(int maxFrameLength) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		
		decoderAdapter.decode(ctx, in, out);
	}

}
