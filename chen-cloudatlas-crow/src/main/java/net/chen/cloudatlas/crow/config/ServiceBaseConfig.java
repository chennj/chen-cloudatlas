package net.chen.cloudatlas.crow.config;

public class ServiceBaseConfig {

	private String appName = "";
	private String contact = "";
	private String ipWhiteList = "";
	private String ipBlackList = "";
	
	/**
	 * 默认为启动，该status的级别高于provider自己的status，1为使用、2为不使用
	 * 避免出现老版本中不存在该项，默认值为0，则如果出现0，当作1
	 */
	private int status = 1;
	/**
	 * dcOn=0 表示使用本地的默认灾备策略,dcOn=1表示使用该中心，dcOn=2表示停用该中心
	 */
	private int dcOn = 0;
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getContact() {
		return contact;
	}
	public void setContact(String contact) {
		this.contact = contact;
	}
	public String getIpWhiteList() {
		return ipWhiteList;
	}
	public void setIpWhiteList(String ipWhiteList) {
		this.ipWhiteList = ipWhiteList;
	}
	public String getIpBlackList() {
		return ipBlackList;
	}
	public void setIpBlackList(String ipBlackList) {
		this.ipBlackList = ipBlackList;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getDcOn() {
		return dcOn;
	}
	public void setDcOn(int dcOn) {
		this.dcOn = dcOn;
	}
	
	
}
