package net.chen.cloudatlas.crow.remote.exchange;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.chen.cloudatlas.crow.common.Protocols;
import net.chen.cloudatlas.crow.remote.codec.crow.CrowCodecVersion;
import net.chen.cloudatlas.crow.remote.codec.crow.CrowDecoderImpl;

public class ExchangeDecoderImpl extends CrowDecoderImpl{

	private ExchangeDecoderAdapter decoder = new ExchangeDecoderAdapter(this);

	@Override
	public String getName() {
		return Protocols.CROW_RPC + CrowCodecVersion.V10.getVersion();
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {		
		decoder.decode(ctx, in, out);
	}	
	
}
