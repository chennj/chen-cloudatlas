package net.chen.cloudatlas.crow.remote;

import net.chen.cloudatlas.crow.common.NameableService;

/**
 * 报文体编解码接口<br>
 * 可采用SPI动态扩展
 * 
 * @author chenn
 *
 */
public interface MessageBodyCodec extends NameableService{

	byte[] encode(Object body);
	
	Object decode(byte[] data);
}
