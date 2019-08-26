package net.chen.cloudatlas.crow.remote.codec;

import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.NameableService;
import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.common.serialize.Serializer;
import net.chen.cloudatlas.crow.common.utils.NameableServiceLoader;
import net.chen.cloudatlas.crow.remote.Compressor;

public abstract class AbstractDecoder extends ByteToMessageDecoder implements NameableService{

	private URL url;
	
	private Serializer serializer;
	private Compressor compressor;
	
	public AbstractDecoder setUrl(URL url){
		
		this.url = url;
		serializer = NameableServiceLoader.getLoader(Serializer.class).getService(url.getParameter(Constants.SERIALIZATION_TYPE));
		compressor = NameableServiceLoader.getLoader(Compressor.class).getService(url.getParameter(Constants.COMPRESS_ALGORITHM));
		return this;
	}
	
	public URL getUrl(){
		
		return this.url;
	}
	
	public Serializer getSerializer(){
		
		return serializer;
	}
	
	public Compressor getCompressor(){
		
		return compressor;
	}
	
	public abstract LengthFieldBasedFrameDecoder getLengthFieldBasedFrameDecoder(int maxFrameLength);
}
