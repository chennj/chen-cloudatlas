package net.chen.cloudatlas.crow.common.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * <b><font color=red>
 * 未完成
 * </font></b>
 * @author chenn
 *
 */
public class CollectionUtil {

	public static Map<String,String> toStringMap(String... pairs) {
		
		Map<String, String> parameters = new HashMap<>();
		if (pairs.length > 0){
			
			if (pairs.length % 2 != 0){
				throw new IllegalArgumentException("pairs must be even");
			} 
			
			for (int i=0; i<pairs.length; i=i+2){
				parameters.put(pairs[i], pairs[i+1]);
			}
		}
		return parameters;
	}
	
}
