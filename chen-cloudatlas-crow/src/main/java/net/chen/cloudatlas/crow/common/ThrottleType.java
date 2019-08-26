package net.chen.cloudatlas.crow.common;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="throttle-type")
@XmlEnum
public enum ThrottleType {

	@XmlEnumValue("qps")
	QPS("qps"),
	
	@XmlEnumValue("flow")
	FLOW("flow");
	
	private String text;
	
	ThrottleType(String text){
		this.text = text;
	}
	
	private String getText() {
		
		return text;
	}	
	
	private static final Map<String, ThrottleType> STRING_T0_ENUM = new HashMap<String, ThrottleType>();
	static{
		for(ThrottleType one : values()){
			STRING_T0_ENUM.put(one.getText(),one);
		}
	}

	public static ThrottleType fromString(String text){
		return STRING_T0_ENUM.get(text);
	}
	
	@Override
	public  String toString(){
		return text;
	}
}
