package org.chen.cloudatlas.crow.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.StringUtils;
import org.chen.cloudatlas.crow.common.Constants;
import org.chen.cloudatlas.crow.common.DcType;
import org.chen.cloudatlas.crow.common.exception.ConfigInvalidException;
import org.chen.cloudatlas.crow.common.utils.ValidatorUtil;


/**
 * 
 * @author chenn
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ApplicationConfig extends AbstractConfig{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@XmlAttribute
	private String name;
	
	@XmlAttribute
	private long heartbeatInterval;
	
	@XmlAttribute(name = "dc")
	private String dcStr;
	
	@XmlAttribute
	private String description;
	
	@XmlAttribute
	private String contact;
	
	@XmlAttribute
	private int nettyBossCount;
	
	@XmlAttribute
	private int nettyWorkerCount;
	
	@XmlAttribute
	private boolean springFeature;
	
	@XmlTransient
	private DcType dc;
		
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getHeartbeatInterval() {
		return heartbeatInterval;
	}

	public void setHeartbeatInterval(long heartbeatInterval) {
		this.heartbeatInterval = heartbeatInterval;
	}

	public String getDcStr() {
		return dcStr;
	}

	public void setDcStr(String dcStr) {
		this.dcStr = dcStr;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getConteact() {
		return contact;
	}

	public void setConteact(String conteact) {
		this.contact = conteact;
	}

	public int getNettyBossCount() {
		return nettyBossCount;
	}

	public void setNettyBossCount(int nettyBossCount) {
		this.nettyBossCount = nettyBossCount;
	}

	public int getNettyWorkerCount() {
		return nettyWorkerCount;
	}

	public void setNettyWorkerCount(int nettyWorkerCount) {
		this.nettyWorkerCount = nettyWorkerCount;
	}

	public boolean getSpringFeature() {
		return springFeature;
	}

	public void setSpringFeature(boolean springFeature) {
		this.springFeature = springFeature;
	}

	public DcType getDc() {
		return dc;
	}

	public void setDc(DcType dc) {
		this.dc = dc;
	}

	public void check() throws ConfigInvalidException {
		
		//一个服务要么位于上海，要么位于北京
		if (!(DcType.SHANGHAI.getText().equals(dcStr) || DcType.BEIJING.getText().equals(dcStr))){
			throw new ConfigInvalidException("application config error",
					new Object[]{"dc must be " + DcType.SHANGHAI.getText() + " or " + DcType.BEIJING.getText()});
		}
		
		if (!StringUtils.isEmpty(contact)){
			String[] str = contact.split(";");
			if (null == str || str.length != 3){
				throw new ConfigInvalidException("application config error",
						new Object[]{"contact must be in this format: \"name;phone;email\""});
			} else {
				if (!ValidatorUtil.validateEmail(str[2])){
					throw new ConfigInvalidException("application config error",
							new Object[]{"email invalidate"});
				}
				if (!ValidatorUtil.validatePhone(str[1])){
					throw new ConfigInvalidException("application config error",
							new Object[]{"phone invalidate"});
				}
			}
		}
	}

	public void setDefaultValue() {
		
		if (null == name){
			name = Constants.UNKNOWN;
		}
		
		if (0 == heartbeatInterval){
			heartbeatInterval = Constants.DEFAULT_HEARTBEAT_INTERVAL;
		}
		
		if (null == dcStr){
			dcStr = DcType.SHANGHAI.getText();
		}
		
		dc = DcType.fromString(dcStr);
		
		if (null == description){
			description = "";
		}
	}

}
