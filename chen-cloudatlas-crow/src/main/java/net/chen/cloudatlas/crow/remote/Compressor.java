package net.chen.cloudatlas.crow.remote;

import java.io.IOException;

import net.chen.cloudatlas.crow.common.NameableService;

public interface Compressor extends NameableService{

	/**
	 * I/O stream copy buffer size
	 */
	static final int BUFF_SIZE = 1024;
	
	/**
	 * 压缩
	 * @param source
	 * @return
	 * @throws IOException
	 */
	byte[] compress(final byte[] source) throws IOException;
	
	/**
	 * 解压
	 * @param input
	 * @param offset
	 * @param totalLength
	 * @return
	 * @throws IOException
	 */
	byte[] uncompress(final byte[] input, final int offset, final int totalLength) throws IOException;
}
