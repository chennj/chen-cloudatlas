package net.chen.cloudatlas.crow.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

/**
 * @author chenn
 *
 */
public class CollectionUtil {

	private CollectionUtil(){}
	
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
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> sort(List<T> list){
		
		if (null!=list && list.size()>0){
			Collections.sort((List) list);
		}
		return list;
	}
	
	private static final Comparator<String> SIMPLE_NAME_COMPARATOR = new Comparator<String>(){

		@Override
		public int compare(String o1, String o2) {
			
			if (null==o1 && null==o2){
				return 0;
			}
			
			if (null==o1){
				return -1;
			}
			if (null==o2){
				return 1;
			}
			
			int i1 = o1.lastIndexOf(".");
			if (i1>=0){
				o1 = o1.substring(i1+1);
			}
			int i2 = o2.lastIndexOf(".");
			if (i2>=0){
				o2 = o2.substring(i2+1);
			}
			return o1.compareToIgnoreCase(o2);
		}
		
	};
	
	public static List<String> sortSimpleName(List<String> list){
		
		if (null!=list && list.size()>0){
			Collections.sort(list,CollectionUtil.SIMPLE_NAME_COMPARATOR);
		}
		return list;
	}
	
	public static Map<String, Map<String, String>> splitAll(Map<String, List<String>> list, String separator){
		
		if (null == list){
			return null;
		}
		
		Map<String, Map<String, String>> result = new HashMap<>();
		
		for (Map.Entry<String, List<String>> entry : list.entrySet()){
			result.put(entry.getKey(), split(entry.getValue(), separator));
		}
		
		return result;
	}
	
	public static Map<String, List<String>> joinAll(Map<String, Map<String, String>> map, String separator){
		
		if (null==map){
			return null;
		}
		
		Map<String, List<String>> result = new HashMap<>();
		for (Map.Entry<String, Map<String, String>> entry : map.entrySet()){
			result.put(entry.getKey(), join(entry.getValue(), separator));
		}
		
		return result;
	}
	
	public static Map<String, String> split(List<String> list, String separator){
		
		if (null==list){
			return null;
		}
		
		Map<String, String> map = new HashMap<>();
		if (null==list || list.size()==0){
			return map;
		}
		
		for (String item : list){
			int index = item.indexOf(separator);
			if (index == -1){
				map.put(item, "");
			} else {
				map.put(item.substring(0,index), item.substring(index+1));
			}
		}
		return map;
	}
	
	public static List<String> join(Map<String, String> map, String separator){
		
		if (null==map){
			return null;
		}
		
		List<String> list = new ArrayList<String>();
		
		if (map.size() == 0){
			return list;
		}
		
		for (Map.Entry<String, String> entry : map.entrySet()){
			String key = entry.getKey();
			String value = entry.getValue();
			if (StringUtils.isEmpty(value)){
				list.add(key);
			} else {
				list.add(key+separator+value);
			}
		}
		
		return list;
	}
	
	public static String join(List<String> list, String separator){
		
		StringBuilder sb = new StringBuilder();
		for (String s : list){
			if (sb.length()>0){
				sb.append(separator);
			}
			sb.append(s);
		}
		return sb.toString();
	}
	
	public static boolean mapEquals(Map<?,?> map1, Map<?,?> map2){
		
		if (null==map1 && null==map2){
			return true;
		}
		
		if (null==map1 || null==map2){
			return false;
		}
		
		if (map1.size() != map2.size()){
			return false;
		}
		
		for (Map.Entry<?, ?> entry : map1.entrySet()){
			Object key = entry.getKey();
			Object v1 = entry.getValue();
			Object v2 = map2.get(key);
			if (!objectEquals(v1,v2)){
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean objectEquals(Object obj1, Object obj2){
		
		if (null==obj1 && null==obj2){
			return true;
		}
		if (null==obj1 || null==obj2){
			return false;
		}
		
		return obj1.equals(obj2);
	}
	
	public static <K, V> Map<K, V> toMap(Object... pairs){
		
		Map<K, V> result = new HashMap<>();
		
		if (null==pairs || pairs.length == 0){
			return result;
		}
		
		if (pairs.length%2 != 0){
			throw new IllegalArgumentException("Map pairs can not be odd number.");
		}
		
		int len = pairs.length / 2;
		for (int i=0; i<len; i++){
			result.put((K)pairs[2*i], (V)pairs[2*i+1]);
		}
		
		return result;
	}
}
