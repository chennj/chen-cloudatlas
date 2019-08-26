package net.chen.cloudatlas.crow.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.alibaba.fastjson.annotation.JSONField;

import net.chen.cloudatlas.crow.common.CompressAlgorithmType;
import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.Protocols;
import net.chen.cloudatlas.crow.common.SerializationType;
import net.chen.cloudatlas.crow.common.exception.ConfigInvalidException;
import net.chen.cloudatlas.crow.common.utils.StringPropertyReplacer;
import net.chen.cloudatlas.crow.common.utils.ValidatorUtil;

@XmlAccessorType(XmlAccessType.FIELD)
public class ProtocolConfig<T> extends AbstractConfig {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@XmlAttribute
	@XmlID
	private String id;
	
	@XmlAttribute
	private String codec;
	
	@XmlAttribute
	private String version;
	
	@XmlTransient
	private int port;
	
	@XmlAttribute(name="port")
	private String portStr;
	
	@XmlTransient
	private int httpPort;
	
	@XmlAttribute(name="httpPort")
	private String httpPortStr;
	
	@XmlAttribute
	private String ip;
	
	@XmlAttribute
	private long heartbeatInterval;
	
	@XmlAttribute
	@XmlJavaTypeAdapter(SerializationTypeAdapter.class)
	private SerializationType serializationType;
	
	@XmlAttribute
	@XmlJavaTypeAdapter(CompressAlgorithmTypeAdapter.class)
	private CompressAlgorithmType compressAlgorithm;
	
	@XmlAttribute
	private int maxMsgSize;
	
	/**
	 * 暴露多个head服务时，单个的listener中无法区分不同的head服务，所以需要手工指定
	 */
	@XmlAttribute
	private String listener;
	
	@XmlAttribute
	private int maxThreads;
	
	private T listenerImpl;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCodec() {
		return codec;
	}

	public void setCodec(String codec) {
		this.codec = codec;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@JSONField(serialize = false, deserialize = false)
	public String getPortStr() {
		return portStr;
	}

	public void setPortStr(String portStr) {
		this.portStr = portStr;
	}

	public int getHttpPort() {
		return httpPort;
	}

	public void setHttpPort(int httpPort) {
		this.httpPort = httpPort;
	}

	public String getHttpPortStr() {
		return httpPortStr;
	}

	public void setHttpPortStr(String httpPortStr) {
		this.httpPortStr = httpPortStr;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public long getHeartbeatInterval() {
		return heartbeatInterval;
	}

	public void setHeartbeatInterval(long heartbeatInterval) {
		this.heartbeatInterval = heartbeatInterval;
	}

	public SerializationType getSerializationType() {
		return serializationType;
	}

	public void setSerializationType(SerializationType serializationType) {
		this.serializationType = serializationType;
	}

	public CompressAlgorithmType getCompressAlgorithm() {
		return compressAlgorithm;
	}

	public void setCompressAlgorithm(CompressAlgorithmType compressAlgorithm) {
		this.compressAlgorithm = compressAlgorithm;
	}

	public String getListener() {
		return listener;
	}

	public void setListener(String listener) {
		this.listener = listener;
	}

	public int getMaxThreads() {
		return maxThreads;
	}

	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}

	public T getListenerImpl() {
		return listenerImpl;
	}

	public void setListenerImpl(T listenerImpl) {
		this.listenerImpl = listenerImpl;
	}

	public int getMaxMsgSize() {
		return maxMsgSize;
	}

	public void setMaxMsgSize(int maxMsgSize) {
		this.maxMsgSize = maxMsgSize;
	}

	public void check() throws ConfigInvalidException {
		
		if (null != ip && !ValidatorUtil.validateIp(ip)){
			throw new ConfigInvalidException("protocolConfig config error",
					new Object[]{"ip is invalid"});
		}
		
		if (Protocols.CROW_RPC.equals(codec) && SerializationType.BINARY.equals(serializationType)){
			throw new ConfigInvalidException("protocolConfig config error",
					new Object[]{"serializationType must not be " + SerializationType.BINARY + 
							" when code is " + Protocols.CROW_RPC});
		}
	}

	@JSONField(serialize=false,deserialize=false)
	public void setDefaultValue() {

		if (null == codec){
			codec = Constants.DEFAULT_PROTOCOL;
		}
		
		if (null == serializationType){
			serializationType = Constants.DEFAULT_SERIALIZATION_TYPE;
		}
		
		if (null == compressAlgorithm){
			compressAlgorithm = Constants.DEFAULT_COMPRESS_ALGORITHM;
		}
		
		if (null == version){
			version = Constants.DEFAULT_PROTOCOL_VERSION;
		}
		
		if (0 == maxMsgSize){
			maxMsgSize = Constants.DEFAULT_MAX_MSG_SIZE;
		}
		
		if (ip != null){
			ip = StringPropertyReplacer.replaceProperties(ip);
		}
		
		if (null != portStr){
			port = Integer.valueOf(StringPropertyReplacer.replaceProperties(portStr));
		}
		
		if (null != httpPortStr){
			httpPort = Integer.valueOf(StringPropertyReplacer.replaceProperties(httpPortStr));
		}
		
		//如果protocol级别的heartbeatInterval没有设置，
		//则使用 <application>的heartbeatInterval值。
		if (0 == heartbeatInterval){
			heartbeatInterval = getApplicationConfig().getHeartbeatInterval();
		}
		
		if (0 == maxThreads){
			maxThreads = Constants.DEFAULT_CROW_NETTY_EXECUTOR_SIZE;
		}
		
	}

}
