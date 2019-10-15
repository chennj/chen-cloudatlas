package net.chen.cloudatlas.crow.remote.codec.rmif;

import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.Protocols;
import net.chen.cloudatlas.crow.remote.codec.AbstractEncoder;

/**
 * RMIF 报文格式
 * <pre>
 * +----------------+----------+-----------+--------------------+--------------+-------+------+-------+---
 * | 数据部分的长度    | 报文类型    | 系列化类型   | 是否使用snappy压缩    | 是否忽略响应    | 保留2 | 保留3 | 数据    |
 * +----------------+----------+-----------+--------------------+--------------+-------+------+-------+---
 * </pre>
 * @author chenn
 *
 */
public class RMIFEncoderImpl extends AbstractEncoder{

	@Override
	public String getName() {
		return Protocols.CROW_RMI + Constants.DEFAULT_PROTOCOL_VERSION;
	}

	@Override
	public LengthFieldBasedFrameDecoder getLengthFieldBasedFrameDecoder(int maxFrameLength) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, List out) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
