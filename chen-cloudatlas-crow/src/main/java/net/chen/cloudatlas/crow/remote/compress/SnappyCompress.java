package net.chen.cloudatlas.crow.remote.compress;

import java.io.IOException;

import org.tinylog.Logger;
import org.xerial.snappy.Snappy;

import net.chen.cloudatlas.crow.common.CompressAlgorithmType;
import net.chen.cloudatlas.crow.remote.Compressor;

public class SnappyCompress implements Compressor{

	@Override
	public String getName() {
		return CompressAlgorithmType.SNAPPY.getText();
	}

	@Override
	public byte[] compress(byte[] source) throws IOException {
		byte[] result = Snappy.compress(source);
		if (Logger.isDebugEnabled()){
			Logger.debug("use snappy compres, {} bytes -> {} bytes",source.length, result.length);
		}
		return result;
	}

	@Override
	public byte[] uncompress(byte[] input, int offset, int totalLength) throws IOException {
		final byte[] output = new byte[Snappy.uncompressedLength(input,offset,totalLength)];
		Snappy.uncompress(input,offset,totalLength-offset,output,0);
		if (Logger.isDebugEnabled()){
			Logger.debug("use snappy uncompres, {} bytes -> {} bytes",input.length, output.length);
		}
		return output;
	}

}
