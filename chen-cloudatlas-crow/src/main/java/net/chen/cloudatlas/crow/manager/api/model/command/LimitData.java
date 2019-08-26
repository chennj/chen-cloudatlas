package net.chen.cloudatlas.crow.manager.api.model.command;

import java.io.Serializable;
import java.util.Map;

public class LimitData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3481470487536786945L;
	
	private long updateTimeMillis;
	
	private Map<String, String> limitMap;

	public LimitData(){
		
	}

	public long getUpdateTimeMillis() {
		return updateTimeMillis;
	}

	public void setUpdateTimeMillis(long updateTimeMillis) {
		this.updateTimeMillis = updateTimeMillis;
	}

	public Map<String, String> getLimitMap() {
		return limitMap;
	}

	public void setLimitMap(Map<String, String> limitMap) {
		this.limitMap = limitMap;
	}
	
	@Override
	public String toString(){
		return "LimitData [updateTimeMillis=" + updateTimeMillis + ", limitMap=" + limitMap + "]";
	}
}
