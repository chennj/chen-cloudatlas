package net.chen.cloudatlas.crow.remote.support.crow;

import net.chen.cloudatlas.crow.remote.Request;
import net.chen.cloudatlas.crow.remote.codec.crow.CrowCodecVersion;

public class CrowRequest extends CrowHeader implements Request{

	protected byte[] requestBytes;
	
	public CrowRequest(){
		super();
		this.setRequest(true);
		this.requestId = SEED.getAndIncrement();
	}
	
	public CrowRequest(CrowCodecVersion version){
		this();
		this.setMajorVersion(version.getMajorByte());
		this.setMinorVersion(version.getMinorByte());
	}

	public CrowRequest(byte[] requestBytes){
		this();
		setRequestBytes(requestBytes);
	}
	
	public CrowRequest(CrowCodecVersion version, byte[] requestBytes){
		this();
		this.setMajorVersion(version.getMajorByte());
		this.setMinorVersion(version.getMinorByte());
		setRequestBytes(requestBytes);
	}
	
	public byte[] getRequestBytes() {
		return requestBytes;
	}

	public void setRequestBytes(byte[] requestBytes) {
		this.requestBytes = requestBytes;
		this.length = requestBytes.length;
	}
	
	@Override
	public byte[] getPayload(){
		return this.getRequestBytes();
	}
	
	@Override
	public void setPayload(byte[] payload){
		this.setRequestBytes(payload);
	}
	
	@Override
	public String toString(){
		return super.toString();
	}	
	
}
