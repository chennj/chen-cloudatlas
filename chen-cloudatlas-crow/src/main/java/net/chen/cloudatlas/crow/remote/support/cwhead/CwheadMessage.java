package net.chen.cloudatlas.crow.remote.support.cwhead;

import net.chen.cloudatlas.crow.remote.Request;
import net.chen.cloudatlas.crow.remote.Response;

public class CwheadMessage extends CwheadHeader implements Request, Response, Cloneable{

	private byte[] content;
	
	public CwheadMessage(){}
	
	public CwheadMessage(byte[] content){
		this.content = content;
		
	}
	
	@Override
	public String getProtocol() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTokenKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getPayload() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPayload(byte[] payload) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isHeartbeat() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getServiceId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServiceVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isBinary() {
		// TODO Auto-generated method stub
		return false;
	}

	
}
