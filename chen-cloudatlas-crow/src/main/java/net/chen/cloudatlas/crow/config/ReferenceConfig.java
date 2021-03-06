package net.chen.cloudatlas.crow.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;

import org.springframework.util.StringUtils;
import org.tinylog.Logger;

import com.alibaba.fastjson.annotation.JSONField;

import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.DcType;
import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.common.cluster.FailType;
import net.chen.cloudatlas.crow.common.cluster.LoadBalanceType;
import net.chen.cloudatlas.crow.common.exception.ConfigException;
import net.chen.cloudatlas.crow.common.exception.ConfigInvalidException;
import net.chen.cloudatlas.crow.common.exception.MethodNotImplException;
import net.chen.cloudatlas.crow.common.utils.StringPropertyReplacer;
import net.chen.cloudatlas.crow.common.utils.ValidatorUtil;

@XmlAccessorType(XmlAccessType.FIELD)
public class ReferenceConfig<T> extends AbstractConfig {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@XmlAttribute
	private String serviceId;
	
	@XmlAttribute
	private String serviceVersion;
	
	@XmlAttribute
	private String urls;
	
	@XmlAttribute
	private LoadBalanceType loadBalanceStrategy;
	
	@XmlAttribute
	private FailType failStrategy;
	
	@XmlAttribute
	private int retries;
	
	@XmlAttribute
	private boolean oneway;
	
	@XmlAttribute
	private long timeout;
	
	@XmlAttribute
	private String weights;
	
	@XmlAttribute
	private int forks;
	
	@XmlAttribute
	@XmlIDREF
	private ProtocolConfig protocol = new ProtocolConfig();
	
	@XmlAttribute
	private String interfaceClass;
	
	@XmlAttribute(name="dc")
	private String dcStr;
	
	@XmlAttribute
	private String password;
	
	@XmlAttribute
	private boolean dcAutoSwitch = false;
	
	@XmlTransient
	private DcType dc;
	
	/**
	 * e.g. ip1:port1,ip2:port2|ip3:port3,ip4:port4
	 */
	@XmlTransient
	private Map<DcType,List<String>> urlGroupsMap;
	
	/**
	 * e.g. 10,10|20,25
	 */
	@XmlTransient
	private Map<DcType,List<String>> weightGroupsMap;
	
	@XmlTransient
	private int totalUrlCount;
	
	@XmlTransient
	private int totalWeightCount;
	
	@XmlTransient
	private List<URL> urlList;
	
	/**
	 * 该服务的dc调用策略
	 * 1.sh:true,bj:false 则只调用sh
	 * 2.sh:true,bj:ture 先默认调用sh，sh不可用时调用bj（自动切换）
	 * 3.sh:false,bj:true 则只调用bj
	 * 可以进行各种组合
	 * 顺序从前往后，为1则执行，为0则不执行，前面的1不可用才启用后面的1，
	 * 顺序有put的先后决定
	 */
	@XmlTransient
	private Map<DcType, Boolean> dcStrategy;
	
	/**
	 * 标记urls是否由于providers的变化而被改变过
	 */
	@XmlTransient
	private volatile boolean urlsModified = false;
	
	/**
	 * 如果时不符合规范的serviceId，就不要去zookeeper上watch
	 */
	private boolean isValidServiceName = true;
	
	private boolean isServiceTimeout = false;
	
	

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

	public String getUrls() {
		return urls;
	}

	public void setUrls(String urls) {
		this.urls = urls;
	}

	public LoadBalanceType getLoadBalanceStrategy() {
		return loadBalanceStrategy;
	}

	public void setLoadBalanceStrategy(LoadBalanceType loadBalanceStrategy) {
		this.loadBalanceStrategy = loadBalanceStrategy;
	}

	public FailType getFailStrategy() {
		return failStrategy;
	}

	public void setFailStrategy(FailType failStrategy) {
		this.failStrategy = failStrategy;
	}

	public int getRetries() {
		return retries;
	}

	public void setRetries(int retries) {
		this.retries = retries;
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

	public int getForks() {
		return forks;
	}

	public void setForks(int forks) {
		this.forks = forks;
	}

	public ProtocolConfig getProtocol() {
		return protocol;
	}

	public void setProtocol(ProtocolConfig protocol) {
		this.protocol = protocol;
	}

	public String getInterfaceClass() {
		return interfaceClass;
	}

	public void setInterfaceClass(String interfaceClass) {
		this.interfaceClass = interfaceClass;
	}

	public String getDcStr() {
		return dcStr;
	}

	public void setDcStr(String dcStr) {
		this.dcStr = dcStr;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isDcAutoSwitch() {
		return dcAutoSwitch;
	}

	public void setDcAutoSwitch(boolean dcAutoSwitch) {
		this.dcAutoSwitch = dcAutoSwitch;
	}

	public DcType getDc() {
		return dc;
	}

	public void setDc(DcType dc) {
		this.dc = dc;
	}

	public Map<DcType, List<String>> getUrlGroupsMap() {
		return urlGroupsMap;
	}

	public void setUrlGroupsMap(Map<DcType, List<String>> urlGroupsMap) {
		this.urlGroupsMap = urlGroupsMap;
	}

	public Map<DcType, List<String>> getWeightGroupsMap() {
		return weightGroupsMap;
	}

	public void setWeightGroupsMap(Map<DcType, List<String>> weightGroupsMap) {
		this.weightGroupsMap = weightGroupsMap;
	}

	@JSONField(serialize=false,deserialize=false)
	public int getTotalUrlCount() {
		return totalUrlCount;
	}

	public void setTotalUrlCount(int totalUrlCount) {
		this.totalUrlCount = totalUrlCount;
	}

	@JSONField(serialize=false,deserialize=false)
	public int getTotalWeightCount() {
		return totalWeightCount;
	}

	public void setTotalWeightCount(int totalWeightCount) {
		this.totalWeightCount = totalWeightCount;
	}

	public List<URL> getUrlList() {
		return urlList;
	}

	public void setUrlList(List<URL> urlList) {
		this.urlList = urlList;
	}

	public Map<DcType, Boolean> getDcStrategy() {
		return dcStrategy;
	}

	public void setDcStrategy(Map<DcType, Boolean> dcStrategy) {
		this.dcStrategy = dcStrategy;
	}

	@JSONField(serialize=false,deserialize=false)
	public boolean isUrlsModified() {
		return urlsModified;
	}

	public void setUrlsModified(boolean urlsModified) {
		this.urlsModified = urlsModified;
	}

	public boolean isValidServiceName() {
		return isValidServiceName;
	}

	public void setValidServiceName(boolean isValidServiceName) {
		this.isValidServiceName = isValidServiceName;
	}

	public boolean isServiceTimeout() {
		return isServiceTimeout;
	}

	public void setServiceTimeout(boolean isServiceTimeout) {
		this.isServiceTimeout = isServiceTimeout;
	}

	public void check() throws ConfigInvalidException {
		
		if (null != this.urlGroupsMap){
			
			for (Entry<DcType,List<String>> entry : this.urlGroupsMap.entrySet()){
				DcType key = entry.getKey();
				List<String> value = entry.getValue();
				for ( String url : value){
					
					if (!ValidatorUtil.validateIpAndPort(url)){
						throw new ConfigInvalidException("ip:prot pattern error");
					}
					
					// check weight
					for (String weight : this.weightGroupsMap.get(key)){
						
						try {
							Integer.parseInt(weight);
						} catch (NumberFormatException e){
							Logger.error("parseInt error.",e);
							throw new ConfigInvalidException("weight must be number");
						}
					}
				}
			}
		}
		
		if (null != dcStr){
			
			String[] dcs = dcStr.split(Constants.COMMA_SEPARATOR);
			if (1 == dcs.length){
				if (!(DcType.SHANGHAI.getText().equals(dcStr.trim()) || DcType.BEIJING.getText().equals(dcStr.trim()))){
					throw new ConfigInvalidException("dc must be " + DcType.SHANGHAI.getText() + " or " + DcType.BEIJING.getText());
				}
			} else if (dcs.length == 2){
				if (!(DcType.SHANGHAI.getText().equals(dcs[0].trim()) && DcType.BEIJING.getText().equals(dcs[1].trim()))
						|| (DcType.SHANGHAI.getText().equals(dcs[1].trim()) && DcType.BEIJING.getText().equals(dcs[0].trim()))){
					throw new ConfigInvalidException("dc must be " + DcType.SHANGHAI.getText() + " or " + DcType.BEIJING.getText());
				}
			}
		}
		
		/**
		 * 需要对非rpc的serviceId进行检查
		 */
		if (this.getRegistryConfig() == null || this.isRpc()){
			return;
		}
		if (this.serviceId != null && !this.serviceId.isEmpty()){
			
			String[] serviceArr = serviceId.split("_");
			if (serviceArr == null || serviceArr.length < 2){
				this.isValidServiceName = false;
				Logger.error("\n In crow 1, serviceId need to be unique from other app,\n"
						+ " which should be named like sys_app_yourservice. \n"
						+ " you should have at lease one '_' in your serviceId or your \n"
						+ " service will not be subscribed from zookeeper. \n");
			}
		}
	}

	@JSONField(serialize=false,deserialize=false)
	public void setDefaultValue() {
		
		if (this.dcStrategy == null){
			this.dcStrategy = new LinkedHashMap<>();
		}
		
		if (dcStr == null){
			dcStr = DcType.SHANGHAI.getText();
			dc = DcType.fromString(dcStr.trim());
		} else {
			if (dcStr.split(Constants.COMMA_SEPARATOR).length > 1){
				dc = DcType.ALL;
			} else {
				dc = DcType.fromString(dcStr.trim());
			}
		}
		
		if (this.urlGroupsMap == null){
			initMap();
		}
		
		if (!StringUtils.isEmpty(urls)){
			urls = StringPropertyReplacer.replaceProperties(urls);
			this.totalUrlCount = convert(this.urlGroupsMap, urls);
		}
		
		if (!StringUtils.isEmpty(this.weights)){
			weights = StringPropertyReplacer.replaceProperties(weights);
			this.totalWeightCount = convert(this.weightGroupsMap, weights);
		}
		
		// 支持${systemProperty=defaultValue}的格式，
		// 让客户自定义system property覆盖
		
		if (this.loadBalanceStrategy == null){
			this.loadBalanceStrategy = Constants.DEFAULT_LOADBALANCER_TYPE;
		}
		
		if (this.failStrategy == null){
			this.failStrategy = Constants.DEFAULT_FAIL_TYPE;
		}
		
		if (this.retries == 0){
			this.retries = Constants.DEFAULT_RETRIES;
		}
		
		if (timeout <= 0){
			this.isServiceTimeout = true;
			timeout = Constants.DEFAULT_NO_RESPONSE_TIMEOUT;
		}
		
		if (forks == 0){
			this.timeout = Constants.DEFAULT_FORKS;
		}
		
		// weights要与urls的元素个数相同（如果用户配置的数量与urls元素个数不同，则默认设置为相同）
		if (this.totalUrlCount != this.totalWeightCount){
			
			Logger.warn("the weight element count is expected to be equal to the urls element count ("
					+ this.totalUrlCount + "),"
					+ "crow will set weights to be average by default.");
			
			// 为用户自动设上平均的weights
			for (Entry<DcType, List<String>> entry : this.urlGroupsMap.entrySet()){
				DcType key = entry.getKey();
				List<String> value = entry.getValue();
				List<String> weightValue = this.weightGroupsMap.get(key);
				weightValue.clear();
				for (int j=0; j<value.size(); j++){
					weightValue.add("1");
				}
			}
		}
		
		if (this.serviceVersion == null){
			this.serviceVersion = Constants.DEFAULT_SERVICE_VERSION;
		}
	}
	
	private int convert(Map<DcType, List<String>> result, String str) {
		
		int count = 0;
		String[] groups = str.split(Constants.GROUP_SEPARATOR);
		String[] dcs = dcStr.split(Constants.COMMA_SEPARATOR);
		
		for (int i=0; i<groups.length; i++){
			
			if (null != groups[i]){
				String[] addrArr = groups[i].split(Constants.COMMA_SEPARATOR);
				if (addrArr.length == 0 || addrArr[0].equals("")){
					continue;
				}
				count += addrArr.length;
				result.get(DcType.fromString(dcs[i].trim())).addAll(Arrays.asList(addrArr));
			}
		}
		return count;
	}

	private void initMap() {
		
		String[] dcs = dcStr.split(Constants.COMMA_SEPARATOR);
		
		this.urlGroupsMap = new LinkedHashMap<>();
		this.weightGroupsMap = new LinkedHashMap<>();
		
		for (int i=0; i<dcs.length; i++){
			this.urlGroupsMap.put(DcType.fromString(dcs[i].trim()), new ArrayList<>());
			this.weightGroupsMap.put(DcType.fromString(dcs[i].trim()), new ArrayList<>());
			if (this.dcAutoSwitch || i==0){
				// 自动切换时，每个dc都设置为true，
				// 否则只有第一个dc设置为true
				this.dcStrategy.put(DcType.fromString(dcs[i].trim()), true);
			} else if (i>0){
				this.dcStrategy.put(DcType.fromString(dcs[i].trim()), false);
			}
		}
	}

	public boolean isRpc(){
		return interfaceClass != null && !StringUtils.isEmpty(interfaceClass.trim());
	}
	
	@JSONField(serialize=false,deserialize=false)
	public synchronized List<URL> getURLs(){
		//如果已有，则直接返回
		if (urlList != null && !urlsModified){
			return urlList;
		}
		
		//将匹配文件中的属性统一放入URL的paramters中
		urlList = new ArrayList<URL>();
		
		long heartbeatInterval = this.getProtocol().getHeartbeatInterval();
		
		for (Entry<DcType, List<String>> entry : urlGroupsMap.entrySet()){
			
			DcType key = entry.getKey();
			List<String> urlGroup = entry.getValue();
			List<String> weightGroup = weightGroupsMap.get(key);
			
			for (int i=0; i<urlGroup.size(); i++){
				
				Map<String, String> ps = new HashMap<String, String>();
				ps.put(Constants.HEARTBEAT_INTERVAL, String.valueOf(heartbeatInterval));
				ps.put(Constants.PROTOCOL, protocol.getCodec());
				ps.put(Constants.PROTOCOL_VERSION, protocol.getVersion());
				ps.put(Constants.SERIALIZATION_TYPE, protocol.getSerializationType().getText());
				ps.put(Constants.COMPRESS_ALGORITHM, protocol.getCompressAlgorithm().getText());
				ps.put(Constants.MAX_MSG_SIZE, String.valueOf(protocol.getMaxMsgSize()));
				ps.put(Constants.LOAD_BALANCE_STRATEGY, loadBalanceStrategy.getText());
				ps.put(Constants.FAIL_STRATEGY, failStrategy.getText());
				ps.put(Constants.RETRIES, String.valueOf(retries));
				ps.put(Constants.ONE_WAY, String.valueOf(oneway));
				ps.put(Constants.TIMEOUT, String.valueOf(timeout));
				ps.put(Constants.FORKS_KEY, String.valueOf(forks));
				ps.put(Constants.PRIORITY, String.valueOf(i));
				ps.put(Constants.APPLICATION, getApplicationConfig().getName());
				ps.put(Constants.SERVICE_VERSION, serviceVersion);
				ps.put(Constants.DC, key.getText());
				ps.put(Constants.SERVICE_ID, serviceId);
				if (getMonitorConfig() != null){
					ps.put(Constants.MONITOR_URLS, getMonitorConfig().getUrls());
					ps.put(Constants.MONITOR_INTERVAL, String.valueOf(getMonitorConfig().getMonitorInterval()));
				}
				ps.put(Constants.SIDE, Constants.CONSUMER);
				ps.put(Constants.WEIGHT, String.valueOf(weightGroup.get(i)));
				
				String urlStr = (String) urlGroup.get(i);
				String[] actualUrl = urlStr.split(Constants.IP_PORT_SEPERATOR);
				
				URL url = new URL(protocol.getCodec(),
						actualUrl[0].trim(),
						Integer.parseInt(actualUrl[1].trim()),
						interfaceClass == null ? serviceId : interfaceClass,
						ps);
				Logger.debug("add url: " + url);
				urlList.add(url);
			}
		}
		
		urlsModified = false;
		
		return urlList;
	}

	@JSONField(serialize=false,deserialize=false)
	public URL buildURLFromService(ServiceConfig sConfig){
		
		DcType dc = sConfig.getApplicationConfig().getDc();
		List<String> weightGroup = weightGroupsMap.get(dc);
		
		Map<String, String> ps = new HashMap<>();
		ps.put(Constants.HEARTBEAT_INTERVAL, String.valueOf(protocol.getHeartbeatInterval()));
		ps.put(Constants.PROTOCOL, protocol.getCodec());
		ps.put(Constants.PROTOCOL_VERSION, protocol.getVersion());
		ps.put(Constants.SERIALIZATION_TYPE, protocol.getSerializationType().getText());
		ps.put(Constants.COMPRESS_ALGORITHM, protocol.getCompressAlgorithm().getText());
		ps.put(Constants.MAX_MSG_SIZE, String.valueOf(protocol.getMaxMsgSize()));
		ps.put(Constants.LOAD_BALANCE_STRATEGY, loadBalanceStrategy.getText());
		ps.put(Constants.FAIL_STRATEGY, failStrategy.getText());
		ps.put(Constants.RETRIES, String.valueOf(retries));
		ps.put(Constants.ONE_WAY, String.valueOf(oneway));
		ps.put(Constants.TIMEOUT, String.valueOf(timeout));
		ps.put(Constants.FORKS_KEY, String.valueOf(forks));
		ps.put(Constants.PRIORITY, String.valueOf(weightGroup.indexOf(sConfig.getWeight())));
		ps.put(Constants.APPLICATION, getApplicationConfig().getName());
		ps.put(Constants.SERVICE_VERSION, serviceVersion);
		ps.put(Constants.DC, dc.getText());
		ps.put(Constants.SERVICE_ID, serviceId);
		if (getMonitorConfig() != null){
			ps.put(Constants.MONITOR_URLS, getMonitorConfig().getUrls());
			ps.put(Constants.MONITOR_INTERVAL, String.valueOf(getMonitorConfig().getMonitorInterval()));
		}
		ps.put(Constants.SIDE, Constants.CONSUMER);
		ps.put(Constants.WEIGHT, String.valueOf(sConfig.getWeight()));
		
		URL url = new URL(protocol.getCodec(),
				sConfig.getProtocol().getIp(),
				sConfig.getProtocol().getPort(),
				interfaceClass == null ? serviceId : interfaceClass,
				ps);
		
		return url;
	}
	
	@SuppressWarnings("unchecked")
	@JSONField(serialize=false,deserialize=false)
	public Class<T> getInterface(){
		
		Class<T> result = null;
		try{
			if (interfaceClass != null){
				result = (Class<T>) Class.forName(interfaceClass);
			}
		} catch (Exception e){
			throw new ConfigException(e);
		}
		return result;
	}
	
	@JSONField(serialize=false,deserialize=false)
	public String getRealServiceId(){
		
		String result = serviceId;
		if (interfaceClass != null && !interfaceClass.trim().isEmpty()){
			result = interfaceClass.trim();
		}
		
		return result;
	}
	
	@JSONField(serialize=false,deserialize=false)
	public DcType[] getRealDc(){
		
		throw new MethodNotImplException();
	}

	public void setDcStrategy(DcType dc, boolean value) {
		this.dcStrategy.put(dc, value);
	}
	
	public void setDcStrategy(DcType dc, int value) {
		
		if (value==0){
			// 0 表示没有经过手工切换，仍使用本地策略
			return;
		}
		
		this.dcStrategy.put(dc, value==1);
	}
}
