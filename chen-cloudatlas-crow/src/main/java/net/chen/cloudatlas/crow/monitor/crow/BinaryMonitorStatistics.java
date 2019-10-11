package net.chen.cloudatlas.crow.monitor.crow;

/**
 * 无法调用monitor rpc 服务的，需要将统计的数据写成基于该类的json形式
 * monitor可将json转化成该类的对象
 * @author chenn
 *
 */
public class BinaryMonitorStatistics {

	private String host = "";
	private String port = "";
	private String side = "";
	private String caller = "";
	private String dc = "";
	private String serviceId = "";
	private String serviceVersion = "";
	private String protocol = "";
	private String method = "";
	private int duration = 0;
	private int concurrent = 0;
	private int peakConcurrent = 0;
	private int totalRt = 0;
	private int peakRt = 0;
	private int lowRt = 0;
	private int succCount = 0;
	private int failCount = 0;
	
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getSide() {
		return side;
	}
	public void setSide(String side) {
		this.side = side;
	}
	public String getCaller() {
		return caller;
	}
	public void setCaller(String caller) {
		this.caller = caller;
	}
	public String getDc() {
		return dc;
	}
	public void setDc(String dc) {
		this.dc = dc;
	}
	public String getServiceId() {
		return serviceId;
	}
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public int getConcurrent() {
		return concurrent;
	}
	public void setConcurrent(int concurrent) {
		this.concurrent = concurrent;
	}
	public int getPeakConcurrent() {
		return peakConcurrent;
	}
	public void setPeakConcurrent(int peakConcurrent) {
		this.peakConcurrent = peakConcurrent;
	}
	public int getTotalRt() {
		return totalRt;
	}
	public void setTotalRt(int totalRt) {
		this.totalRt = totalRt;
	}
	public int getPeakRt() {
		return peakRt;
	}
	public void setPeakRt(int peakRt) {
		this.peakRt = peakRt;
	}
	public int getLowRt() {
		return lowRt;
	}
	public void setLowRt(int lowRt) {
		this.lowRt = lowRt;
	}
	public int getSuccCount() {
		return succCount;
	}
	public void setSuccCount(int succCount) {
		this.succCount = succCount;
	}
	public int getFailCount() {
		return failCount;
	}
	public void setFailCount(int failCount) {
		this.failCount = failCount;
	}
	public String getServiceVersion() {
		return serviceVersion;
	}
	public void setServiceVersion(String serviceVersion) {
		this.serviceVersion = serviceVersion;
	}
	
}
