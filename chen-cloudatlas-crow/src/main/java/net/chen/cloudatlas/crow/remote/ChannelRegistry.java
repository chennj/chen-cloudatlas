package net.chen.cloudatlas.crow.remote;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import org.tinylog.Logger;

import io.netty.channel.Channel;
import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.common.utils.UrlUtil;

/**
 * 
 * @author chenn
 *
 */
public class ChannelRegistry {

	/**
	 * 存放可用的链接channel
	 */
	private static ConcurrentMap<String, Channel> availableChannels = new ConcurrentHashMap<String, Channel>();
	
	/**
	 * 存放不可用的链接channel
	 */
	private static ConcurrentMap<String, Channel> unavailableChannels = new ConcurrentHashMap<String, Channel>();
	
	private static ReentrantLock lock = new ReentrantLock();
	
	public static void registerChannel(Channel channel){
		registerChannel(channel,false);
	}

	/**
	 * register channel and save it in channelsMap, thread-safe
	 * @param channel
	 * @param checkAvailability
	 */
	public static void registerChannel(Channel channel, final boolean checkAvailability) {
		
		if (null == channel){
			throw new IllegalArgumentException("channel object is null");
		}
		
		if (channel.remoteAddress() == null){
			throw new IllegalArgumentException("channel remote address is null");
		}
		
		String key = UrlUtil.getAddressKey((InetSocketAddress) channel.remoteAddress());
		Logger.trace("Register Channel for key:{}, result:{}", key, channel);
		if (checkAvailability){
			lock.lock();
			try{
				if (channel.isActive()){
					availableChannels.put(key, channel);
				} else {
					Logger.error("Register Channel which is not connected!!!" + key);
				}
			} finally{
				lock.unlock();
			}
		} else {
			availableChannels.put(key, channel);
		}
	}

	public static List<Channel> getUnavailableChannels() {
		
		return new ArrayList<Channel>(unavailableChannels.values());
	}

	/**
	 * 
	 * @param c
	 */
	public static void stopRetryChannel(Channel c) {
		stopRetryChannel(c,false);
	}

	public static void stopRetryChannel(Channel c, final boolean checkAvailability) {
		
		if (null == c){
			throw new IllegalArgumentException("channel object is null");
		}
		
		if (null == c.remoteAddress()){
			throw new IllegalArgumentException("channel remote address is null");
		}
		
		String key = UrlUtil.getAddressKey((InetSocketAddress)c.remoteAddress());
		stopRetryChannel(key,checkAvailability);
	}

	/**
	 * remove channel from availableChannels and put it on unavailableChannels fro retry
	 * @param nettyChannel
	 */
	public static void invalidateChannel(Channel nettyChannel) {
		
		if (null != nettyChannel){
			String key = UrlUtil.getAddressKey((InetSocketAddress)nettyChannel.remoteAddress());
			lock.lock();
			try{
				availableChannels.remove(key);
				unavailableChannels.putIfAbsent(key, nettyChannel);
			} finally{
				lock.unlock();
			}
		}
	}
	
	/**
	 * remove channel from availableChannels and put it on unavailableChannels for retry <br>
	 * use SocketAddress as input parameter install Channel object
	 * 
	 * @param address
	 */
	public static void invalidateChannel(SocketAddress address){
		
		if (null != address){
			
			String key = UrlUtil.getAddressKey((InetSocketAddress)address);
			lock.lock();
			try {
				Channel invalChannel = availableChannels.remove(key);
				if (null != invalChannel){
					unavailableChannels.putIfAbsent(key, invalChannel);
				}
			} finally{
				lock.unlock();
			}
		}
	}

	public static Channel getChannel(SocketAddress socketAddress) {
		
		String key = UrlUtil.getAddressKey((InetSocketAddress)socketAddress);
		return getChannel(key);
	}

	public static Channel getChannel(String addressKey) {
		
		Channel result = availableChannels.get(addressKey);
		Logger.trace("getting channel for key:{},result:{}", addressKey, result);
		return result;
	}

	public static void unregisterChannel(URL url) {
		
		if (null != url){
			unregisterChannel((InetSocketAddress)url.getSocketAddress());
		}
	}

	public static void unregisterChannel(InetSocketAddress remoteAddress) {
		
		if (null != remoteAddress){
			
			String key = UrlUtil.getAddressKey(remoteAddress);
			
			lock.lock();
			try {
				availableChannels.remove(key);
				unavailableChannels.remove(key);
			} finally{
				lock.unlock();
			}
		}
	}

	public static void unregisterChannel(Channel channel) {
		
		if (null != channel){
			unregisterChannel((InetSocketAddress)channel.remoteAddress());
		}
	}

	public static boolean isChannelAvailable(String addressKey) {
		return availableChannels.containsKey(addressKey);
	}

	public static void stopRetryChannel(String addressKey, final boolean checkAvailability) {
		
		if (checkAvailability){
			lock.lock();
			try {
				Channel goodChannel = availableChannels.get(addressKey);
				if (null != goodChannel && goodChannel.isActive()){
					unavailableChannels.remove(addressKey);
				}
			} finally {
				lock.unlock();
			}
		} else {
			unavailableChannels.remove(addressKey);
		}
	}
	
	public static void removeChannel(SocketAddress address){
		
		if (null != address){
			String key = UrlUtil.getAddressKey((InetSocketAddress)address);
			lock.lock();
			try {
				availableChannels.remove(key);
				unavailableChannels.remove(key);
			} finally {
				lock.unlock();
			}
		}
	}
	
	public static List<Channel> getAvailableChannels(){
		return new ArrayList<Channel>(availableChannels.values());
	}
	
	public static List<String> getAvailableKeys(){
		return new ArrayList<String>(availableChannels.keySet());
	}
	
	public static void clearUnavailableChannels(){
		unavailableChannels.clear();
	}
	
	public static void clearAvailableChannels(){
		availableChannels.clear();
	}
}
