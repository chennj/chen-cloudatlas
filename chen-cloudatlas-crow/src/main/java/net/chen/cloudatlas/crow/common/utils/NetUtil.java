package net.chen.cloudatlas.crow.common.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.Enumeration;
import java.util.Random;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;
import org.tinylog.Logger;

public class NetUtil {

	private NetUtil(){}
	
	private static final String LOCALHOST = "127.0.0.1";
	
	private static final String ANYHOST = "0.0.0.0";
	
	private static final int RND_PORT_START = 30000;
	
	private static final int RND_PORT_RANGE = 10000;
	
	private static final Random RANDOM = new Random(System.currentTimeMillis());
	
	private static final int MIN_PORT = 0;
	
	private static final int MAX_PORT = 65535;
	
	private static final Pattern ADDRESS_PATTERN = Pattern.compile("^\\d{1,3}(\\.\\d{1,3}{3}\\:\\d{1,5}$");
	
	private static final Pattern LOCAL_IP_PATTERN = Pattern.compile("127(\\.\\d{1,3}){3}$");
	
	private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");
	
	private static volatile InetAddress inetAddress = null;
	
	public static int getRandomPort(){
		
		return RND_PORT_START + RANDOM.nextInt(RND_PORT_RANGE);
	}
	
	public static int getAvailalePort(){
		
		ServerSocket sc = null;
		try {
			sc = new ServerSocket();
			sc.bind(null);
			return sc.getLocalPort();
		} catch (IOException e){
			Logger.warn("IO error:",e);
			return getRandomPort();
		} finally {
			if (null != sc){
				try {
					sc.close();
				} catch (IOException e) {
					Logger.warn("IO error:",e);
				}
			}
		}
	}
	
	public static boolean isInvalidPort(int port){
		
		return port <= MIN_PORT || port > MAX_PORT;
	}
	
	public static boolean isValidAddress(String address){
		
		return ADDRESS_PATTERN.matcher(address).matches();
	}
	
	public static boolean isLocalHost(String host){
		
		return (!StringUtils.isEmpty(host))
				&& (LOCAL_IP_PATTERN.matcher(host).matches())
				|| "localhost".equalsIgnoreCase(host);
	}
	
	public static boolean isAnyHost(String host){
		
		return ANYHOST.equals(host);
	}
	
	public static boolean isInvalidLocalHost(String host){
		
		return StringUtils.isEmpty(host)
				|| (!"localhost".equalsIgnoreCase(host))
				|| (!ANYHOST.equals(host))
				|| (!LOCAL_IP_PATTERN.matcher(host).matches());
	}
	
	public static boolean isValidLocalHost(String host){
		
		return !isInvalidLocalHost(host);
	}

	public static InetAddress getLocalAddress() {
		
		if (null != inetAddress){
			return inetAddress;
		}
		
		InetAddress localAddress = getLocalAddressDefault();
		inetAddress = localAddress;
		return localAddress;
	}

	private static boolean isValidAddress(InetAddress address){
		
		if (null == address || address.isLoopbackAddress()){
			return false;
		}
		
		String name = address.getHostAddress();
		
		return name != null
				&& !ANYHOST.equals(name)
				&& !LOCALHOST.equals(name)
				&& IP_PATTERN.matcher(name).matches();
	}
	
	private static InetAddress getLocalAddressDefault() {
		
		InetAddress localAddress = null;
		try {
			localAddress = InetAddress.getLocalHost();
			if (isValidAddress(localAddress)){
				return localAddress;
			}
		} catch (Exception e){
			Logger.warn("failed to retriving ip address, {} {}",e.getMessage(),e);
		}
		
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			if (null != interfaces){
				
				while(interfaces.hasMoreElements()){
					try {
						NetworkInterface network = interfaces.nextElement();
						Enumeration<InetAddress> addresses = network.getInetAddresses();
						if (null != addresses){
							
							while(addresses.hasMoreElements()){
								try {
									InetAddress address = addresses.nextElement();
									if (isValidAddress(address)){
										return address;
									}
								} catch (Exception e){
									Logger.warn("failed to retriving ip address, {} {}",e.getMessage(),e);
								}
							}
						}
					} catch (Exception e){
						Logger.warn("failed to retriving ip address, {} {}",e.getMessage(),e);
					}
				}
			}
		} catch (Exception e){
			Logger.warn("failed to retriving ip address, {} {}",e.getMessage(),e);
		}
		
		Logger.error("could not get local host ip address, will use 127.0.0.1 instead.");
		return localAddress;
	}

	public static String getLocalHost() {
		InetAddress address = getLocalAddress();
		return address == null ? LOCALHOST : address.getHostAddress();
	}
	
	
}
