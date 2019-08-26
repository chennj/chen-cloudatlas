package net.chen.cloudatlas.crow.config;

import java.util.HashSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;

import com.alibaba.fastjson.annotation.JSONField;

import net.chen.cloudatlas.crow.common.SpringContextUtil;
import net.chen.cloudatlas.crow.common.ThrottleType;
import net.chen.cloudatlas.crow.common.exception.ConfigException;
import net.chen.cloudatlas.crow.common.exception.ConfigInvalidException;
import net.chen.cloudatlas.crow.common.exception.MethodNotImplException;

@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceConfig<T> extends AbstractConfig {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@XmlAttribute
	private String serviceId;
	
	@XmlAttribute
	private String serviceVersion;
	
	@XmlAttribute
	private String description;
	
	@SuppressWarnings("rawtypes")
	@XmlAttribute
	@XmlIDREF
	private ProtocolConfig protocol;
	
	@XmlAttribute
	private String interfaceClass;
	
	@XmlAttribute
	private String implClass;
	
	@XmlAttribute
	private String proxyFactory;
	
	@XmlAttribute
	private int weight = -1;
	
	@XmlAttribute
	private boolean oneway;
	
	@XmlAttribute
	private long timeout;
	
	@XmlAttribute
	private long throttleValue;
	
	@XmlAttribute
	private ThrottleType throttleType;
	
	@XmlAttribute
	private String ipWhiteList;
	
	@XmlAttribute
	private String ipBlackList;
	
	@XmlAttribute
	private String password;
	
	@XmlAttribute
	private boolean local = false;
	
	private boolean springFeature;
	
	private int dcStrategy;
	
	private int status = -1;
	
	private boolean isValidServiceName = true;	//service name 是否符合校验，如果不符合不在zookeeper上注册
	
	/**
	 * provider 保存ip list 避免每次判断时，需解析黑白名单字符串
	 */
	private HashSet<String> ipWhiteSet = new HashSet<String>();
	private HashSet<String> ipBlackSet = new HashSet<String>();

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getServiceVersion() {
		return serviceVersion;
	}

	public void setServiceVersion(String serviceVersion) {
		this.serviceVersion = serviceVersion;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@SuppressWarnings("rawtypes")
	public ProtocolConfig getProtocol() {
		return protocol;
	}

	@SuppressWarnings("rawtypes")
	public void setProtocol(ProtocolConfig protocol) {
		this.protocol = protocol;
	}

	public String getInterfaceClass() {
		return interfaceClass;
	}

	public void setInterfaceClass(String interfaceClass) {
		this.interfaceClass = interfaceClass;
	}

	public String getImplClass() {
		return implClass;
	}

	public void setImplClass(String implClass) {
		this.implClass = implClass;
	}

	public String getProxyFactory() {
		return proxyFactory;
	}

	public void setProxyFactory(String proxyFactory) {
		this.proxyFactory = proxyFactory;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public boolean isOneway() {
		return oneway;
	}

	public void setOneway(boolean oneway) {
		this.oneway = oneway;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public long getThrottleValue() {
		return throttleValue;
	}

	public void setThrottleValue(long throttleValue) {
		this.throttleValue = throttleValue;
	}

	public ThrottleType getThrottleType() {
		return throttleType;
	}

	public void setThrottleType(ThrottleType throttleType) {
		this.throttleType = throttleType;
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isLocal() {
		return local;
	}

	public void setLocal(boolean local) {
		this.local = local;
	}

	public boolean isSpringFeature() {
		return springFeature;
	}

	public void setSpringFeature(boolean springFeature) {
		this.springFeature = springFeature;
	}

	public int getDcStrategy() {
		return dcStrategy;
	}

	public void setDcStrategy(int dcStrategy) {
		this.dcStrategy = dcStrategy;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public boolean isValidServiceName() {
		return isValidServiceName;
	}

	public void setValidServiceName(boolean isValidServiceName) {
		this.isValidServiceName = isValidServiceName;
	}

	public HashSet<String> getIpWhiteSet() {
		return ipWhiteSet;
	}

	public void setIpWhiteSet(HashSet<String> ipWhiteSet) {
		this.ipWhiteSet = ipWhiteSet;
	}

	public HashSet<String> getIpBlackSet() {
		return ipBlackSet;
	}

	public void setIpBlackSet(HashSet<String> ipBlackSet) {
		this.ipBlackSet = ipBlackSet;
	}

	@JSONField(serialize=false,deserialize=false)
	public String getRealServiceId(){
		String result = serviceId;
		if (interfaceClass != null && !"".equals(interfaceClass.trim())){
			result = interfaceClass.trim();
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@JSONField(serialize=false,deserialize=false)
	public Class<T> getInterface(){
		Class<T> result = null;
		try{
			if (null != interfaceClass){
				result = (Class<T>)Class.forName(interfaceClass);
			}
		} catch(Exception e){
			throw new ConfigException(e);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@JSONField(serialize=false,deserialize=false)
	public T getImpl(){
		T result = null;
		
		try{
			if (springFeature){
				try{
					result = (T)SpringContextUtil.getBean(Class.forName(implClass));
				} catch (Exception e){
					result = (T) Class.forName(implClass).newInstance();
				}
			} else if (null != implClass){
				result = (T) Class.forName(implClass).newInstance();
			}
		} catch (Exception e){
			throw new ConfigException(e);
		}
		
		return result;
	}
	
	public void check() throws ConfigInvalidException {
		// TODO Auto-generated method stub
		throw new MethodNotImplException();
	}

	public void setDefaultValue() {
		// TODO Auto-generated method stub
		throw new MethodNotImplException();
	}

}
