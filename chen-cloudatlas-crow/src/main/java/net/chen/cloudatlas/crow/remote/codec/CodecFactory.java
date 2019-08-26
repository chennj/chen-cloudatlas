package net.chen.cloudatlas.crow.remote.codec;

import io.netty.channel.ChannelHandler;
import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.common.utils.NameableServiceLoader;

/**
 * 
 * @author chenn
 *
 */
public class CodecFactory {

	public static ChannelHandler getEncoder(URL url){
		
		String protocolVersion = url.getParameter(
				Constants.PROTOCOL_VERSION == null ? Constants.DEFAULT_PROTOCOL_VERSION : url.getParameter(Constants.PROTOCOL_VERSION));
		
		return NameableServiceLoader
				.getService(AbstractEncoder.class, url.getProtocol()+protocolVersion).setUrl(url);
	}
	
	public static ChannelHandler getDecoder(URL url){
		
		String protocolVersion = url.getParameter(
				Constants.PROTOCOL_VERSION == null ? Constants.DEFAULT_PROTOCOL_VERSION : url.getParameter(Constants.PROTOCOL_VERSION));
		
		return NameableServiceLoader
				.getService(AbstractEncoder.class, url.getProtocol()+protocolVersion).setUrl(url);
	}
}
