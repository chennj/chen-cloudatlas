package net.chen.cloudatlas.crow.manager.api.model.command;

import java.io.Serializable;

public class TokenData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -350999669856808719L;

	private long updateTimeMillis;
	
	private String token;
	
	private String lastToken;
	
	private int windowDuration;
	
	public TokenData(){
		super();
	}

	public long getUpdateTimeMillis() {
		return updateTimeMillis;
	}

	public void setUpdateTimeMillis(long updateTimeMillis) {
		this.updateTimeMillis = updateTimeMillis;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getLastToken() {
		return lastToken;
	}

	public void setLastToken(String lastToken) {
		this.lastToken = lastToken;
	}

	public int getWindowDuration() {
		return windowDuration;
	}

	public void setWindowDuration(int windowDuration) {
		this.windowDuration = windowDuration;
	}
	
	@Override
	public String toString(){
		return "TokenData [updateTimeMillis=" + updateTimeMillis + ", token=" + token + ", lastToken" +
				", windowDuration=" + windowDuration + "]";
	}
}
