package net.chen.cloudatlas.crow.common;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * 压缩算法
 * @author chenn
 *
 */
@XmlType(name="compress-algorithm-type")
@XmlEnum
public enum CompressAlgorithmType {

	@XmlEnumValue("none")
	NONE("none"),
	
	@XmlEnumValue("snappy")
	SNAPPY("snappy"),
	
	@XmlEnumValue("gzip")
	GZIP("gzip"),
	
	@XmlEnumValue("zlib")
	ZLIB("zlib");
	
	private String text;
	
	CompressAlgorithmType(String text){
		this.text = text;
	}
	
	public String getText(){return text;}
	
	private static Map<String, CompressAlgorithmType> STRING_TO_ENUM = new HashMap<String, CompressAlgorithmType>();
	
	static {
		for (CompressAlgorithmType one : values()){
			STRING_TO_ENUM.put(one.getText(), one);
		}
	}
	
	public static CompressAlgorithmType fromString(String symbol){
		return STRING_TO_ENUM.get(symbol);
	}
	
	@Override
	public String toString(){
		return text;
	}
}
