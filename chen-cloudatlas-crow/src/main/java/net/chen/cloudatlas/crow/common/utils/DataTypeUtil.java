package net.chen.cloudatlas.crow.common.utils;

import org.springframework.util.StringUtils;

public class DataTypeUtil {

	public static Object basicDataTrans(Object data, String type) {
		
		if (null == data || StringUtils.isEmpty(type)){
			return data;
		}
		
		if ("char".equals(type)){
			// 序列化过程中 char 被升级为 String
			char[] charArr = data.toString().toCharArray();
			if (null != charArr && charArr.length == 1){
				return charArr[0];
			}
		} else if ("float".equals(type)){
			return Float.parseFloat(data.toString());
		} else if ("short".equals(type)){
			return Short.parseShort(data.toString());
		} else if ("byte".equals(type)){
			return Byte.parseByte(data.toString());
		} else if ("[C".equals(type)){
			// char[] 被升级为String[]
			return data.toString().toCharArray();
		}
		return data;
	}

}
