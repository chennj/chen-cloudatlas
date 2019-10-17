package net.chen.cloudatlas.crow.remote.exchange;

import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import net.chen.cloudatlas.crow.common.Protocols;
import net.chen.cloudatlas.crow.remote.codec.crow.CrowCodecVersion;
import net.chen.cloudatlas.crow.remote.codec.crow.CrowEncoderImpl2;

public class ExchangeEncoderImpl2 extends CrowEncoderImpl2{

	private ExchangeEncoderAdapter encoderAdapter = new ExchangeEncoderAdapter(this);

	@Override
	public String getName() {
		return Protocols.CROW_RPC + CrowCodecVersion.V20.getVersion();
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, List out) throws Exception {
		encoderAdapter.encode(ctx, msg, out);
	}
	
	
}
