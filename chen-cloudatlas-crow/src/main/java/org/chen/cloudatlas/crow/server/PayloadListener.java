package org.chen.cloudatlas.crow.server;

import org.chen.cloudatlas.crow.common.DcType;
import org.chen.cloudatlas.crow.remote.ChannelListener;

/**
 * 当采用byte数组方式（非RPC）通信时，server端业务系统实现该接口，
 * 来接收客户端发来的byte数组
 * @author chenn
 *
 */
public interface PayloadListener extends ChannelListener{

	/**
	 * 回调函数，crow框架会把收到的byte[]（报文数据）传递给应用处理。
	 * 
	 * @param serviceId 服务id，没有设计serviceId的协议，crow设置 为null
	 * 					否则可以暴露多个服务在一个端口上，通过serviceId来区分服务
	 * @param serviceVersion
	 * @param requestBytes 报文体数据
	 * @param sourceDc 带返回的response报文体数据
	 * @return
	 */
	byte[] handle(String serviceId, String serviceVersion, byte[] requestBytes, DcType sourceDc);
}
