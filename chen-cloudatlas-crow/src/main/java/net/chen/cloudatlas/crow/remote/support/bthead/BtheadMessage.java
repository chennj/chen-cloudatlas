package net.chen.cloudatlas.crow.remote.support.bthead;

import net.chen.cloudatlas.crow.common.Protocols;
import net.chen.cloudatlas.crow.remote.Request;
import net.chen.cloudatlas.crow.remote.Response;

/**
 * bthead整个报文（头+体）<br>
 * bthead请求报文与应答报文结构完全一致，所以将btheadRequest和btheadResponse统一成btheadMessage
 * @author chenn
 *
 */
public class BtheadMessage extends BtheadHeader implements Request, Response, Cloneable{

	/**
	 * 体(dk_array)
	 * 
	 * e.g. A:3:S:7:bill_noS:8:25986075S:1:06:resultS:1:0S:13:result_stringS:2:ok
	 */
	private byte[] content;
	
	public BtheadMessage(){
		
	}
	
	public BtheadMessage(byte[] content){
		this.content = content;
		this.setContentLen(content.length);
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
		this.setContentLen(content.length);
	}

	@Override
	public String getProtocol() {
		return Protocols.CROW_HEAD;
	}

	@Override
	public String getTokenKey() {
		return String.valueOf(getLogId());
	}

	@Override
	public byte[] getPayload() {
		return this.getContent();
	}

	@Override
	public void setPayload(byte[] payload) {
		this.setContent(payload);
	}

	@Override
	public boolean isHeartbeat() {
		// bthead没有心跳
		return false;
	}

	@Override
	public String getServiceId() {
		// 该协议没有设计serviceId
		return null;
	}

	@Override
	public String getServiceVersion() {
		// 该协议没有设计serviceVersion
		return null;
	}

	@Override
	public boolean isBinary() {
		// bthead时系列化对象协议
		return true;
	}
	
	
}
