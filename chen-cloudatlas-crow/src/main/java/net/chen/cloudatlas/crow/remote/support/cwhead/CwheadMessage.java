package net.chen.cloudatlas.crow.remote.support.cwhead;

import net.chen.cloudatlas.crow.common.Protocols;
import net.chen.cloudatlas.crow.remote.Request;
import net.chen.cloudatlas.crow.remote.Response;

/**
 * cwhead报文（头+体）<br>
 * cwhead请求报文与应答报文结构完全一致，所以将CwheadRequest和CwheadResponse统一成CwheadMessage.
 * @author chenn
 *
 */
public class CwheadMessage extends CwheadHeader implements Request, Response, Cloneable{

	/**
	 * 报文体
	 * e.g. A:3:S:7:bill_noS:8:25986075S:6:resultS:1:0S:13:result_stringS:2:ok
	 */
	private byte[] content;
	
	public CwheadMessage(){}
	
	public CwheadMessage(byte[] content){
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
	public String toString(){
		StringBuilder sb = new StringBuilder();
		String separator = System.getProperty("line.separator");
		sb.append(separator);
		sb.append("=============").append(this.getClass().getSimpleName()+" header").append("===============").append(separator);
		sb.append("id:          ["+getId()+"]").append(separator);
		sb.append("version:     ["+getVersion()+"]").append(separator);
		sb.append("logId:       ["+getLogId()+"]").append(separator);
		sb.append("provider:    ["+getProvider()+"]").append(separator);
		sb.append("magicNum:    ["+getMagicNum()+"]").append(separator);
		sb.append("contentLen:  ["+getContentLen()+"]").append(separator);
		sb.append("content:     ["+new String(content)+"]").append(separator);
		sb.append("============================").append(separator);
		return sb.toString();
	}
	
	@Override
	public CwheadMessage clone(){
		CwheadMessage result = new CwheadMessage();
		result.setId(getId());
		result.setVersion(getVersion());
		result.setLogId(getLogId());
		result.setProvider(getProvider());
		result.setMagicNum(getMagicNum());
		result.setContentLen(getContentLen());
		result.setContent(getContent());
		return result;
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
		// cwhead没有心跳，直接返回false
		return false;
	}

	@Override
	public String getServiceId() {
		// cwhead协议没有设计serviceId，直接返回null
		return null;
	}

	@Override
	public String getServiceVersion() {
		// cwhead协议没有设计serviceVersion，直接返回null
		return null;
	}

	@Override
	public boolean isBinary() {
		return true;
	}

	
}
