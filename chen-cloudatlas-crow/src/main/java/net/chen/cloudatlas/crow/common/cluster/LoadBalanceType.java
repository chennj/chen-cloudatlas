package net.chen.cloudatlas.crow.common.cluster;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="loadbalance-type")
@XmlEnum
public enum LoadBalanceType {
	
	@XmlEnumValue("random")
	RANDOM("random"),
	
	@XmlEnumValue("first")
	FIRST("first"),
	
	@XmlEnumValue("priority")
	PRIORITY("priority"),
	
	@XmlEnumValue("roundrobin")
	ROUNDROBIN("roundrobin");
	
	private String text;
	
	LoadBalanceType(String text){
		this.text = text;
	}
	
	public String getText() {
		
		return text;
	}	
	
	private static final Map<String, LoadBalanceType> STRING_T0_ENUM = new HashMap<String, LoadBalanceType>();
	static{
		for(LoadBalanceType one : values()){
			STRING_T0_ENUM.put(one.getText(),one);
		}
	}

	public static LoadBalanceType fromString(String text){
		return STRING_T0_ENUM.get(text);
	}
	
	@Override
	public  String toString(){
		return text;
	}
}
