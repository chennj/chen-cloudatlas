package net.chen.cloudatlas.crow.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * 未完成<br>
 * 文件工具类<br>
 * <li>IO流赋值</li>
 * <li>从输入流中读取字节到字节数组缓存</li>
 * <li>文件的压缩与解压</li>
 * <li>文件、目录删除</li>
 * <li>文件、目录建立</li>
 * @author chenn
 *
 */
public class FileUtil {

	/**
	 * 复制输入、输出流<br>
	 * @param in
	 * @param out
	 * @param buffSize
	 * @param close
	 * @throws IOException
	 */
	public static void copyBytes(InputStream in, OutputStream out, int buffSize, boolean close) throws IOException{
		
		try {
			copyBytes(in,out,buffSize);
		} finally{
			if (close){
				out.close();
				in.close();
			}
		}
	}
	
	/**
	 * 复制输入、输出流<br>
	 * @param in
	 * @param out
	 * @param buffSize
	 * @throws IOException
	 */
	public static void copyBytes(InputStream in, OutputStream out, int buffSize) throws IOException{
		
		PrintStream ps = out instanceof PrintStream ? (PrintStream)out : null;
		byte buf[] = new byte[buffSize];
		int bytesRead = in.read(buf);
		while (bytesRead >= 0){
			out.write(buf, 0, bytesRead);
			if ((ps != null) && ps.checkError()){
				throw new IOException("unable to write to output stream.");
			}
			bytesRead = in.read(buf);
		}
	}
	
	/**
	 * 消除文件读取的1024缓冲BUG
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public static byte[] getBytes(String filePath) throws IOException{
		
		byte[] bytes = null;
		File file = new File(filePath);
		if (null != file){
			InputStream is = new FileInputStream(file);
			int length = (int)file.length();
			if (length > Integer.MAX_VALUE){
				throw new IOException("file size must be less than "+Integer.MAX_VALUE);
			}
			bytes = new byte[length];
			int offset = 0;
			int numRead = 0;
			while(
					offset < bytes.length
					&& (numRead = is.read(bytes, offset, bytes.length-offset))>=0){
				offset += numRead;
			}
			if (offset < bytes.length){
				throw new IOException("file read error");
			}
			is.close();
		}
		return bytes;
	}
}
