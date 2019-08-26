package net.chen.cloudatlas.crow.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.common.exception.ConfigInvalidException;

@XmlAccessorType(XmlAccessType.FIELD)
public class RegistryConfig extends AbstractConfig{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@XmlAttribute
	private String address;
	
	@XmlAttribute
	private int connectionTimeoutMillis;
	
	@XmlAttribute
	private int sessionTimeoutMillis;
	
	/**
	 * 用户填写的字符串，必须匹配NameableService的名字
	 */
	@XmlAttribute
	private String type;
	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getConnectionTimeoutMillis() {
		return connectionTimeoutMillis;
	}

	public void setConnectionTimeoutMillis(int connectionTimeoutMillis) {
		this.connectionTimeoutMillis = connectionTimeoutMillis;
	}

	public int getSessionTimeoutMillis() {
		return sessionTimeoutMillis;
	}

	public void setSessionTimeoutMillis(int sessionTimeoutMillis) {
		this.sessionTimeoutMillis = sessionTimeoutMillis;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void check() throws ConfigInvalidException {
		// TODO Auto-generated method stub
		
	}

	public void setDefaultValue() {
		// TODO Auto-generated method stub
		
	}

	public URL toURL(){
		return new URL(
				getType(), null, 0, null,
				Constants.ADDRESS, getAddress(),
				Constants.CONNECTION_TIMEOUT_MS, String.valueOf(this.getConnectionTimeoutMillis()),
				Constants.SESSION_TIMEOUT_MS, String.valueOf(this.getSessionTimeoutMillis()));
	}
}
