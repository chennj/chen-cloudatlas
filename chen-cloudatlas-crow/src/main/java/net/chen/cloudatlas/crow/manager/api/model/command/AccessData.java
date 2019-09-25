package net.chen.cloudatlas.crow.manager.api.model.command;

import java.io.Serializable;
import java.util.List;

/**
 * CommandType.ACCESS的节点的数据模型
 * @author chenn
 *
 */
public class AccessData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2711675823011483464L;

	private long updateTimeMillis;
	
	private boolean blacklist;
	
	private List<String> hosts;
	
	public AccessData(){
		super();
	}

	public long getUpdateTimeMillis() {
		return updateTimeMillis;
	}

	public void setUpdateTimeMillis(long updateTimeMillis) {
		this.updateTimeMillis = updateTimeMillis;
	}

	public boolean isBlacklist() {
		return blacklist;
	}

	public void setBlacklist(boolean blacklist) {
		this.blacklist = blacklist;
	}

	public List<String> getHosts() {
		return hosts;
	}

	public void setHosts(List<String> hosts) {
		this.hosts = hosts;
	}
	
	@Override
	public String toString(){
		return "AccessData [updateTimeMillis=" + updateTimeMillis + ", blacklist=" + blacklist + ", hosts=" + hosts + "]";
	}
}
