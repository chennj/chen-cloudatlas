package org.chen.cloudatlas.crow.common.cluster;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="loadbalance-type")
@XmlEnum
public enum FailType {
	
	@XmlEnumValue("failover")
	FAIL_OVER("failover"),
	
	@XmlEnumValue("failback")
	FAIL_BACK("failback"),
	
	@XmlEnumValue("failfast")
	FAIL_FAST("failfast"),
	
	@XmlEnumValue("failsafe")
	FAIL_SAFE("failsafe"),
	
	@XmlEnumValue("forking")
	FORKING("forking"),
	
	@XmlEnumValue("broadcast")
	BROADCAST("broadcast");
	
	private String text;
	
	FailType(String text){
		this.text = text;
	}
	
	public String getText() {
		
		return text;
	}	
	
	private static final Map<String, FailType> STRING_T0_ENUM = new HashMap<String, FailType>();
	static{
		for(FailType one : values()){
			STRING_T0_ENUM.put(one.getText(),one);
		}
	}

	public static FailType fromString(String text){
		return STRING_T0_ENUM.get(text);
	}
	
	@Override
	public  String toString(){
		return text;
	}
}
