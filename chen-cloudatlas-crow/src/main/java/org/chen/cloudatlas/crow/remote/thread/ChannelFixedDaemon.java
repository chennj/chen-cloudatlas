package org.chen.cloudatlas.crow.remote.thread;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

import org.chen.cloudatlas.crow.common.utils.UrlUtil;
import org.chen.cloudatlas.crow.config.CrowClientContext;
import org.chen.cloudatlas.crow.remote.ChannelRegistry;
import org.tinylog.Logger;

import io.netty.channel.Channel;

/**
 * 守护线程<br>
 * 检测远程服务是否在线，是否正常，否则注销本地引用
 * @author chenn
 *
 */
public class ChannelFixedDaemon extends AbstractDaemon{

	public ChannelFixedDaemon(long wakeupInterval) {
		super(wakeupInterval);
	}

	@Override
	protected void execute() {
		
		Logger.trace("ChannelFixedDaemon execute() been called!");
		
		try{
			List<Channel> retryList = ChannelRegistry.getUnavailableChannels();
			
			for (Channel c : retryList){
				
				Logger.debug("do with channel:" + c);
				
				SocketAddress address = c.remoteAddress();
				String ipAndPort = UrlUtil.getAddressKey((InetSocketAddress)address);
				
				if (!CrowClientContext.isRemoteEnd(ipAndPort)){
					ChannelRegistry.stopRetryChannel(c);
					continue;
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}

}
