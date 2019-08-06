package org.chen.cloudatlas.crow.remote.support.crow;

import org.chen.cloudatlas.crow.common.Constants;
import org.chen.cloudatlas.crow.common.Protocols;
import org.chen.cloudatlas.crow.remote.Message;
import org.chen.cloudatlas.crow.remote.codec.crow.CrowCodecVersion;

/**
 * Crow协议报文头定义
 * @author chenn
 *
 */
public abstract class CrowHeader implements Message{

	protected static final int MAXLEN = 200;
	
	protected short magic = Constants.MAGIC;
	
	protected byte majorVersion = CrowCodecVersion.getDefault().getMajorByte();
	
	protected byte minorVersion = CrowCodecVersion.getDefault().getMinorByte();
	
	/**
	 * 1表示心跳报文
	 * 0表示业务报文
	 */
	protected boolean heartbeat = false;
	
	/**
	 * 1：只发送请求，不接收返回结果，只接收报文头<BR>
	 * 0：发送请求，同步等待返回结果
	 */
	protected boolean oneWay = false;
	
	/**
	 * 1：请求报文
	 * 0：响应报文
	 */
	protected boolean request;
	
	/**
	 * 服务请求的状态信息
	 */
	protected CrowStatus status = CrowStatus.NONE;
	
	/**
	 * 请求一方一次请求的惟一标识号，<BR>
	 * 由调用方生成，并在一段时间内保持唯一，为了与应答报文进行对应。
	 */
	protected long requestId;
	
	/**
	 * 长度：8 char<BR>
	 * 调用方标识，一般标识调用方的应用节点。<BR>
	 * (可复用zdogs统一定义的系统编码<BR>
	 * 比如crmgm_01，不足后续补空串。
	 */
	protected String callerId = "";
	
	/**
	 * 消息来源所在的dc<BR>
	 * 1：上海，2：北京。
	 */
	protected byte sourceDc = 1;
	
	/**
	 * 长度：20 char<BR>
	 * <b>子系统标识+下划线+服务标识</b> <br>
	 * 不足补空格
	 */
	protected String serviceId = "";
	
	/**
	 * 长度：5 char <br>
	 * 服务的版本号
	 */
	protected String serviceVersion = "";
	
	/**
	 * traceId
	 */
	protected String traceId = "";
	
	/**
	 * 报文体数据长度
	 */
	protected int length;

	
	public short getMagic() {
		return magic;
	}

	public void setMagic(short magic) {
		this.magic = magic;
	}

	public byte getMajorVersion() {
		return majorVersion;
	}

	public void setMajorVersion(byte majorVersion) {
		this.majorVersion = majorVersion;
	}

	public byte getMinorVersion() {
		return minorVersion;
	}

	public void setMinorVersion(byte minorVersion) {
		this.minorVersion = minorVersion;
	}

	public boolean isOneWay() {
		return oneWay;
	}

	public void setOneWay(boolean oneWay) {
		this.oneWay = oneWay;
	}

	public boolean isRequest() {
		return request;
	}

	public void setRequest(boolean request) {
		this.request = request;
	}

	public CrowStatus getStatus() {
		return status;
	}

	public void setStatus(CrowStatus status) {
		this.status = status;
	}

	public long getRequestId() {
		return requestId;
	}

	public void setRequestId(long requestId) {
		this.requestId = requestId;
	}

	public String getCallerId() {
		return callerId;
	}

	public void setCallerId(String callerId) {
		this.callerId = callerId;
	}

	public byte getSourceDc() {
		return sourceDc;
	}

	public void setSourceDc(byte sourceDc) {
		this.sourceDc = sourceDc;
	}

	public String getTraceId() {
		return traceId;
	}

	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public boolean isHeartbeat(){
		return heartbeat;
	}
	
	public void setHeartbeat(boolean heartbeat) {
		this.heartbeat = heartbeat;
	}

	public String getServiceId(){
		return serviceId;
	}
	
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getServiceVersion(){
		return serviceVersion;
	}
	
	public void setServiceVersion(String serviceVersion) {
		this.serviceVersion = serviceVersion;
	}

	@Override
	public String getTokenKey() {
		return String.valueOf(this.getRequestId());
	}
	
	@Override
	public String getProtocol(){
		return Protocols.CROW_BINARY;
	}

	@Override
	public String toString(){
		
		StringBuffer sb = new StringBuffer();
		
		String separator = System.getProperty("line.separator");
		
		sb
		.append(separator)
		.append("===============").append(this.getClass().getSimpleName()+"header").append("===============")
		.append(separator)
		.append("magic:                  [" + "0x" + Integer.toHexString(magic) + "]").append(separator)
		.append("majorVersion:           [" + majorVersion + "]").append(separator)
		.append("minorVersion:           [" + minorVersion + "]").append(separator)
		.append("heartbeat:              [" + heartbeat + "]").append(separator)
		.append("oneWay:                 [" + oneWay + "]").append(separator)
		.append("request:                [" + request + "]").append(separator)
		.append("status:                 [" + status + "]").append(separator)
		.append("requestId:              [" + requestId + "]").append(separator)
		.append("sourceDc:               [" + sourceDc + "]").append(separator)
		.append("serviceId:              [" + serviceId + "]").append(separator)
		.append("traceId:                [" + traceId + "]").append(separator)
		.append("length:                 [" + length + "]").append(separator)
		.append("==========================================").append(separator);
		
		return sb.toString();
			
	}
	
	
}
