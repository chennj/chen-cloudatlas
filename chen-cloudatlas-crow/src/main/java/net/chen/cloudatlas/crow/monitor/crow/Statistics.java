package net.chen.cloudatlas.crow.monitor.crow;

import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.rpc.utils.RpcUtil;

/**
 * 
 * @author chenn
 *
 */
public class Statistics {

	private URL url;
	
	private String clientAddress;
	
	private String serverAddress;
	
	private String group;
	
	private String application;
	
	private String serviceId;
	
	private String method;
	
	private String dc;
	
	public Statistics(URL url){
		
		this.url = url;
		
		// 如果是provider端,不记录单个consumer，而是将多个consumer的结果合并起来
		this.clientAddress = RpcUtil.isConsumer(url) ? url.getHost() : "";
		this.serverAddress = RpcUtil.isConsumer(url) ? "" : url.getHostAndPort();
		this.group = url.getParameter(Constants.GROUP);
		this.application = url.getParameter(Constants.APPLICATION);
		this.serviceId = url.getPath();
		this.method = url.getParameter(Constants.METHOD);
		this.dc = url.getParameter(Constants.DC);
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public String getClientAddress() {
		return clientAddress;
	}

	public void setClientAddress(String clientAddress) {
		this.clientAddress = clientAddress;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getDc() {
		return dc;
	}

	public void setDc(String dc) {
		this.dc = dc;
	}
	
	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime*result + (this.application==null ? 0 : this.application.hashCode());
		result = prime*result + (this.clientAddress==null?0:this.clientAddress.hashCode());
		result = prime*result + (this.group==null?0:this.group.hashCode());
		result = prime*result + (this.method==null?0:this.method.hashCode());
		result = prime*result + (this.serverAddress==null?0:this.serverAddress.hashCode());
		result = prime*result + (this.serviceId==null?0:this.serviceId.hashCode());
		result = prime*result + (this.dc==null?0:this.dc.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object o){
		
		if (this == o){
			return true;
		}
		
		if (null == o){
			return false;
		}
		
		if (this.getClass() != o.getClass()){
			return false;
		}
		
		Statistics other = (Statistics)o;
		
		if (this.application == null){
			if (other.application != null)
				return false;
		} else if (!this.application.equals(other.application)){
			return false;
		}
		
		if (this.clientAddress == null){
			if (other.clientAddress != null) return false;
		} else if (!this.clientAddress.equals(other.clientAddress)){
			return false;
		}
		
		if (this.group == null){
			if (other.group != null) return false;
		} else if (!this.group.equals(other.group)){
			return false;
		}
		
		if (this.method == null){
			if (other.method != null) return false;
		} else if (!this.method.equals(other.method)){
			return false;
		}
		
		if (this.serverAddress == null){
			if (other.serverAddress != null) return false;
		} else if (!this.serverAddress.equals(other.serverAddress)){
			return false;
		}
		
		if (this.serviceId == null){
			if (other.serviceId != null) return false;
		} else if (!this.serviceId.equals(other.serviceId)){
			return false;
		}
		
		if (this.dc == null){
			if (other.dc != null) return false;
		} else if (!this.dc.equals(other.dc)){
			return false;
		}
		
		return true;
	}
	
	@Override
	public String toString(){
		return this.url.toString();
	}
}
