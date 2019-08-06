package org.chen.cloudatlas.crow.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import org.chen.cloudatlas.crow.common.DcType;
import org.chen.cloudatlas.crow.common.exception.ConfigInvalidException;
import org.chen.cloudatlas.crow.common.exception.MethodNotImplException;

@XmlAccessorType(XmlAccessType.FIELD)
public class MonitorConfig extends AbstractConfig{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@XmlAttribute
	private String urls;
	
	@XmlAttribute
	private long monitorInterval;
	
	/**
	 * dc和dc下面的url
	 */
	private Map<DcType, HashSet<String>> urlMap = new HashMap<DcType, HashSet<String>>();
	
	/**
	 * 标记monitor url是否因为zookeeper产生变化
	 */
	private static boolean isModified = false;
	
	public String getUrls() {
		return urls;
	}

	public void setUrls(String urls) {
		this.urls = urls;
	}

	public long getMonitorInterval() {
		return monitorInterval;
	}

	public void setMonitorInterval(long monitorInterval) {
		this.monitorInterval = monitorInterval;
	}

	public Map<DcType, HashSet<String>> getUrlMap() {
		return urlMap;
	}

	public void setUrlMap(Map<DcType, HashSet<String>> urlMap) {
		this.urlMap = urlMap;
	}

	public static boolean isModified() {
		return isModified;
	}

	public static void setModified(boolean isModified) {
		MonitorConfig.isModified = isModified;
	}

	public void check() throws ConfigInvalidException {
		// TODO Auto-generated method stub
		throw new MethodNotImplException();
	}

	public void setDefaultValue() {
		// TODO Auto-generated method stub
		throw new MethodNotImplException();
	}
	
	/**
	 * 通过zookeeper添加进新的url，并更新urls，没有zookeeper时，urlset属性无效
	 * @param dc
	 * @param url
	 */
	public void addUrl(DcType dc, String url){
		
		if (url==null || url.trim().isEmpty()){
			return;
		}
		
		HashSet<String> urlset = urlMap.get(dc);
		if (urlset != null){
			urlset.add(url);
		} else {
			HashSet<String> newUrlSet = new HashSet<>();
			newUrlSet.add(url);
			urlMap.put(dc, newUrlSet);
		}
		
		buildUrl();
	}
	
	public void removeUrl(DcType dc, String url){
		
		if (url==null || url.trim().isEmpty()){
			return;
		}
		
		HashSet<String> urlset = urlMap.get(dc);
		if (urlset != null){
			urlset.remove(url);
		}
		
		buildUrl();
	}

	public void buildUrl() {
		
		urls = "";
		int i=0,j=0;
		for (DcType key : urlMap.keySet()){
			
			HashSet<String> newUrlSet = urlMap.get(key);
			String dcUrl = "";
			i = 0;
			for (String newurl : newUrlSet){
				dcUrl += (i==0 ? "" : ",") + newurl;
				i++;
			}
			urls += (j==0 ? "" : "|") + dcUrl;
			j++;
		}
	}

}
