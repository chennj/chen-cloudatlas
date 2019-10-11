package net.chen.cloudatlas.crow.remote.support.crow;

import java.util.HashMap;
import java.util.Map;

public enum CrowStatus {

	/**
	 * 0x00
	 * 空
	 */
	NONE((byte)0x00),
	
	/**
	 * 0x20
	 * 服务调用成功
	 */
	OK((byte)0x20),
	
	/**
	 * 0x40
	 * client调用的服务未配置
	 */
	SERVICE_NOT_FOUND((byte)0x40),
	
	/**
	 * 0x41
	 * client发送的请求格式错误（crow报文协议字段值填写错误）
	 */
	BAD_REQUEST((byte)0x41),
	
	/**
	 * 0x42
	 * client发送超时
	 */
	CLIENT_TIMEOUT((byte)0x42),
	
	/**
	 * 0x50
	 * 服务端异常（所有位置错误）
	 */
	SERVER_ERROR((byte)0x50),
	
	/**
	 * 0x51
	 * 服务未启动
	 */
	SERVICE_NOT_STARTED((byte)0x51),
	
	/**
	 * 0x52
	 * 服务端处理超时
	 */
	SERVER_TIMEOUT((byte)0x52),
	
	/**
	 * 0x53
	 * 服务端流量控制，超过流量控制上限，被阻拦掉了
	 */
	SERVICE_EXCEEDTHROTTLE((byte)0x53),
	
	/**
	 * 0x54
	 * 服务端根据黑白名单，拒绝服务
	 */
	SERVICE_REJECTED((byte)0x54);
	
	private final byte value;
	
	private CrowStatus(final byte value){
		this.value = value;
	}
	
	public byte value(){
		return value;
	}
	
	private static final Map<Integer, CrowStatus> STRING_TO_ENUM = new HashMap<>();
	
	static{
		for(CrowStatus one : values()){
			STRING_TO_ENUM.put(Integer.valueOf(one.value),one);
		}
	}
	
	public static CrowStatus valueOf(byte statusByte){
		return STRING_TO_ENUM.get(Integer.valueOf(statusByte));
	}

	@Override
	public String toString(){
		return "0x" + Integer.toHexString(value) + "-" + this.name();
	}
}
