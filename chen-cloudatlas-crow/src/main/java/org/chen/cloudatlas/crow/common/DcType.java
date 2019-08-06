package org.chen.cloudatlas.crow.common;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * 注册中心站点
 * @author chenn
 *
 */
@XmlType(name="dc")
@XmlEnum
public enum DcType {

	@XmlEnumValue("sh")
	SHANGHAI("sh", 1),
	
	@XmlEnumValue("bj")
	BEIJING("bj", 2),
	
	@XmlEnumValue("all")
	ALL("all", 0);
	
	private String text;
	private int dcId;
	
	DcType(String text, int dcId){
		this.text = text;
		this.dcId = dcId;
	}

	public String getText() {
		return text;
	}

	public int getDcId() {
		return dcId;
	}
	
	private static final Map<String, DcType> STRING_TO_ENUM = new HashMap<String, DcType>();
	private static final Map<Integer, DcType> INT_TO_ENUM = new HashMap<Integer, DcType>();
	
	static {
		for (DcType one : values()){
			STRING_TO_ENUM.put(one.getText(), one);
			INT_TO_ENUM.put(one.getDcId(), one);
		}
	}
	
	public static DcType fromString(String text){
		return STRING_TO_ENUM.get(text);
	}
	
	public static DcType fromInt(int id){
		return INT_TO_ENUM.get(id);
	}
	
	public int toInt(){
		return dcId;
	}
	
	@Override
	public String toString(){
		return text;
	}
}
