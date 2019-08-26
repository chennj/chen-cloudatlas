package net.chen.cloudatlas.crow.common;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * 系列化类型
 * @author chenn
 *
 */
@XmlType(name = "serialization-type")
@XmlEnum
public enum SerializationType {
	
	@XmlEnumValue("binary")
	BINARY("binary"),
	
	@XmlEnumValue("jdk")
	JDK("jdk"),
	
	@XmlEnumValue("hessian2")
	HESSIAN2("hessian2"),
	
	@XmlEnumValue("kryo")
	KRYO("kryo");
	
	private String text;
	
	SerializationType(String text){
		this.text = text;
	}
	
	public String getText(){return text;}
	
	private static final Map<String, SerializationType> STRING_TO_ENUM = new HashMap<String, SerializationType>();
	
	static {
		for (SerializationType one : values()){
			STRING_TO_ENUM.put(one.getText(), one);
		}
	}
	
	public static SerializationType fromString(String text){
		return STRING_TO_ENUM.get(text);
	}
	
	@Override
	public String toString(){
		return text;
	}
}
