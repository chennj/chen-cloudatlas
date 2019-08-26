package net.chen.cloudatlas.crow.remote.support.crow;

import net.chen.cloudatlas.crow.config.CrowServerContext;
import net.chen.cloudatlas.crow.remote.Response;
import net.chen.cloudatlas.crow.remote.codec.crow.CrowCodecVersion;

public class CrowResponse extends CrowHeader implements Response{

	protected byte[] responseBytes;
	
	public CrowResponse(){
		super();
		this.setRequest(false);
	}
	
	/**
	 * 特定版本
	 * @param version
	 */
	public CrowResponse(CrowCodecVersion version){
		this();
		this.setMajorVersion(version.getMajorByte());
		this.setMinorVersion(version.getMinorByte());
	}
	
	public CrowResponse(byte[] result){
		this();
		this.setResponseBytes(result);
	}

	public CrowResponse(CrowRequest request){
		this.magic = request.getMagic();
		this.majorVersion = request.getMajorVersion();
		this.minorVersion = request.getMinorVersion();
		this.heartbeat = request.isHeartbeat();
		this.oneWay = request.isOneWay();
		this.request = false;
		this.requestId = request.getRequestId();
		this.length = 0;
		this.callerId = CrowServerContext.getApplicationName();
		this.serviceId = request.serviceId;
		this.serviceVersion = request.serviceVersion;
		this.responseBytes = new byte[0];
		this.status = CrowStatus.OK;
	}
	
	public byte[] getResponseBytes() {
		return responseBytes;
	}

	public void setResponseBytes(byte[] responseBytes) {
		this.responseBytes = responseBytes;
	}

	@Override
	public byte[] getPayload() {
		return this.getResponseBytes();
	}

	@Override
	public void setPayload(byte[] payload) {
		this.setResponseBytes(payload);
	}

	@Override
	public boolean isBinary() {
		return true;
	}
	
	@Override
	public String toString(){
		return super.toString();
	}
}
