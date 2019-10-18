package net.chen.cloudatlas.crow.remote.thread;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

import org.tinylog.Logger;

import io.netty.channel.Channel;
import net.chen.cloudatlas.crow.common.utils.UrlUtil;
import net.chen.cloudatlas.crow.config.CrowClientContext;
import net.chen.cloudatlas.crow.remote.ChannelRegistry;
import net.chen.cloudatlas.crow.remote.RemoteException;
import net.chen.cloudatlas.crow.remote.impl.NettyClient;

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
				
				NettyClient client = null;
				try {
					client = NettyClient.getClient((InetSocketAddress)c.remoteAddress());
				} catch (RemoteException e){
					Logger.error("client connection to {} has not been established",ipAndPort);
				}
				
				if (client != null && client.isShutDown()){
					continue;
				}
				
				boolean success = true;
				try {
					Logger.info(address.toString()+" start to fix channel ...");
					client.reconnect();
				} catch (Exception e){
					Logger.warn("fail to fix channel "+address.toString(),e);
					success = false;
				}
				
				if (success){
					Logger.info(address.toString()+" channel fixed, removed from unavailable channels ...");
					ChannelRegistry.stopRetryChannel(c,true);
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}

}
