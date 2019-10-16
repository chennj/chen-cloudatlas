package net.chen.cloudatlas.crow.remote.compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.common.CompressAlgorithmType;
import net.chen.cloudatlas.crow.common.utils.FileUtil;
import net.chen.cloudatlas.crow.remote.Compressor;

public class GZIPCompress implements Compressor{

	@Override
	public String getName() {
		return CompressAlgorithmType.GZIP.getText();
	}

	@Override
	public byte[] compress(byte[] source) throws IOException {
		
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
		gzipOutputStream.write(source);
		gzipOutputStream.close();
		byte[] output = byteArrayOutputStream.toByteArray();
		if (Logger.isDebugEnabled()){
			Logger.debug("use gzip compress. {} bytes -> {} bytes",source.length,output.length);
		}
		return output;
	}

	@Override
	public byte[] uncompress(byte[] input, int offset, int totalLength) throws IOException {
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayInputStream in = new ByteArrayInputStream(input,offset,totalLength-offset);
		FileUtil.copyBytes(new GZIPInputStream(in), out, BUFF_SIZE, true);
		byte[] output = out.toByteArray();
		if (Logger.isDebugEnabled()){
			Logger.debug("use gzip uncompress. {} bytes -> {} bytes",output.length,input.length);
		}
		return output;
	}

}
