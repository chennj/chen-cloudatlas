package net.chen.cloudatlas.crow.remote.codec;

import org.tinylog.Logger;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import net.chen.cloudatlas.crow.common.CompressAlgorithmType;
import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.NameableService;
import net.chen.cloudatlas.crow.common.SerializationType;
import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.common.serialize.Serializer;
import net.chen.cloudatlas.crow.common.utils.NameableServiceLoader;
import net.chen.cloudatlas.crow.remote.Compressor;

/**
 * 
 * @author chenn
 *
 */
public abstract class AbstractEncoder extends MessageToMessageEncoder implements NameableService{

	private URL url;
	
	private Serializer serializer;
	
	private Compressor compressor;
	
	public AbstractEncoder setUrl(URL url){
		
		this.url = url;
		
		String serializationType = url.getParameter(Constants.SERIALIZATION_TYPE);
		String compressAlgorithm = url.getParameter(Constants.COMPRESS_ALGORITHM);
		
		Logger.trace("url:"+url);
		Logger.trace("serializationType:"+serializationType);
		Logger.trace("compressAlgorithm:"+compressAlgorithm);
		
		if (!SerializationType.BINARY.getText().equals(serializationType)){
			serializer = NameableServiceLoader.getLoader(Serializer.class).getService(serializationType);
		}
		
		if (!CompressAlgorithmType.NONE.getText().equals(compressAlgorithm)){
			compressor = NameableServiceLoader.getLoader(Compressor.class).getService(compressAlgorithm);
		}
		
		return this;
	}
	
	public URL getUrl(){
		return this.url;
	}

	public Serializer getSerializer() {
		return serializer;
	}

	public Compressor getCompressor() {
		return compressor;
	}

	public abstract LengthFieldBasedFrameDecoder getLengthFieldBasedFrameDecoder(int maxFrameLength);
}
