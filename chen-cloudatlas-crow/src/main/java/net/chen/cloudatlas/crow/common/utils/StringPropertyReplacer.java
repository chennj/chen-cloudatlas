package net.chen.cloudatlas.crow.common.utils;

import java.io.File;
import java.util.Properties;

/**
 * 属性{xx=xx}工具
 * <p><font color=red>
 * 待完成
 * </font></p>
 * @author chenn
 *
 */
public class StringPropertyReplacer {

	private static final String FILE_SEPARATOR = File.separator;
	
	private static final String PATH_SEPARATOR = File.pathSeparator;
	
	private static final String FILE_SEPARATOR_ALIAS = "/";
	
	private static final String PATH_SEPARATOR_ALIAS = ":";
	
	private static final char DEFAULT_VALUE_SEPARATOR = '=';
	
	private static final int NORMAL = 0;
	private static final int SEEN_DOLLAR = 1;
	private static final int IN_BRACKET = 2;
	
	/**
	 * 
	 * @param str
	 * @return
	 */
	public static String replaceProperties(String str) {
		return replaceProperties(str, null);
	}

	/**
	 * 遍历输入字符串，替换${p}用System.getProperty(p)的值，如果没有这样的属性 p 被定义，则${p}
	 * 被原样保留。<br>
	 * 如果是${p|v}这种形式，没有这样的属性 p 被定义,则用v代替<br>
	 * 如果是${p1,p2}或者${p1,p2|v}这种形式，那么 p1 p2被用Syste.getProperty(p1,2)代替，如果没有用v替换<br>
	 * 如果是${/},使用System.getProperty("File.separator")替换<br>
	 * 如果是${:},使用System.getProperty("path.separator")替换<br>
	 * @param string
	 * @param props
	 * @return
	 */
	public static String replaceProperties(final String string, final Properties props) {
		
		final char[] chars = string.toCharArray();
		StringBuffer buffer = new StringBuffer();
		boolean properties = false;
		int state = NORMAL;
		int start = 0;
		
		for (int i=0; i<chars.length; i++){
			
			char c = chars[i];
			
			// 括号外的美元符号
			if (c=='$' && state!=IN_BRACKET){
				state = SEEN_DOLLAR;
			}			
			// 美元符号后紧跟左大括号{
			else if (c=='{' && state==SEEN_DOLLAR){
				buffer.append(string.substring(start, i-1));
				state = IN_BRACKET;
				start = i-1;
			}
			// 美元符号后紧跟的不是左大括号{
			else if (state == SEEN_DOLLAR){
				state = NORMAL;
			}
			// 跟在左括号后面的右括号
			else if (c == '}' && state == IN_BRACKET){
				
				if (start+2 == i){
					// 没有内容
					buffer.append("${}");
				} else {
					// 有内容,collect the system property
					String value = null;
					
					String key = string.substring(start+2, i);
					
					// check for alias
					if (FILE_SEPARATOR_ALIAS.equals(key)){
						value = FILE_SEPARATOR;
					} else if (PATH_SEPARATOR_ALIAS.equals(key)){
						value = PATH_SEPARATOR;
					} else {
						
						if (null != props){
							value = props.getProperty(key);
						} else {
							value = System.getProperty(key);
						}
						
						if (value == null){
							// check for a default value ${key=default}
							int separator = key.indexOf(DEFAULT_VALUE_SEPARATOR);
							if (separator > 0){
								
								String realKey = key.substring(0,separator);
								
								if (null != props){
									value = props.getProperty(realKey);
								} else {
									value = System.getProperty(realKey);
								}
								
								if (null == value){
									// check for a composite key, "key1,key2"
									value = resolveCompositeKey(realKey, props);
									
									// not a composite key either,use the specified default
									if (null == value){
										value = key.substring(separator + 1);
									}
								}
								
							}
							else {
								// no default, check fro composite key, "key1,key2"
								value = resolveCompositeKey(key, props);
							}
						}
					}	// -- end if (FILE_SEPARATOR_ALIAS.equals(key))
					
					if (null != value){
						properties = true;
						buffer.append(value);
					} else {
						buffer.append("${");
						buffer.append(key);
						buffer.append('}');
					}
				}
				
				start = i+1;
				state = NORMAL;
				
			} // --end if (c == '}' && state == IN_BRACKET)
		}
		
		// no properties
		if (!properties){
			return string;
		}
		
		// collect the trailing characters
		if (start != chars.length){
			buffer.append(string.substring(start, chars.length));
		}
		
		// done
		return buffer.toString();
	}

	private static String resolveCompositeKey(String key, Properties props) {
		
		String value = null;
		
		// look for the comma
		int comma = key.indexOf(',');
		if (comma > -1){
			// if we have a first part, try resolve it
			if (comma > 0){
				// check the first part
				String key1 = key.substring(0, comma);
				if (null != props){
					value = props.getProperty(key1);
				} else {
					value = System.getProperty(key1);
				}
			}
			// check the second part, if there is one and first lookup failed
			if (null == value && comma < key.length() - 1){
				String key2 = key.substring(comma + 1);
				if (null != props){
					value = props.getProperty(key2);
				} else {
					value = System.getProperty(key2);
				}
			}
		}
		
		return value;
	}

}
