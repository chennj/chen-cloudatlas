package net.chen.cloudatlas.crow.remote.support.crow;

public class CrowHeartbeatMessage extends CrowRequest{

	public CrowHeartbeatMessage(){
		this.heartbeat = true;
		this.requestBytes = new byte[0];
	}

	@Override
	public void setRequestBytes(byte[] requestBytes) {
		throw new RuntimeException("setRequestBytes method should not be called because message is heartbeat.");
	}
	
	
}
