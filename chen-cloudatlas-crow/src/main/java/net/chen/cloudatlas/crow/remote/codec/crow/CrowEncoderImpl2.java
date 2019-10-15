package net.chen.cloudatlas.crow.remote.codec.crow;

import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import net.chen.cloudatlas.crow.common.Protocols;
import net.chen.cloudatlas.crow.remote.codec.AbstractEncoder;

public class CrowEncoderImpl2 extends AbstractEncoder{

	private final CrowEncoderAdapter encoderAdapter = new CrowEncoderAdapter(this);
	
	@Override
	public String getName() {
		return Protocols.CROW_BINARY + CrowCodecVersion.V20.getVersion();
	}

	@Override
	public LengthFieldBasedFrameDecoder getLengthFieldBasedFrameDecoder(int maxFrameLength) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, List out) throws Exception {
		encoderAdapter.encode(ctx, msg, out);
	}

}
