package net.chen.cloudatlas.crow.common.utils;

import io.netty.util.internal.SystemPropertyUtil;

public class DebugUtil {

	private static final boolean DEBUG_ENABLED = 
			// 默认为true，最终版本设置false
			SystemPropertyUtil.getBoolean("crow.debug", true);
	
	public static boolean isDebugEnabled(){
		return DEBUG_ENABLED;
	}
	
	private DebugUtil(){}
	
	public static String dumpMemory(String title, byte[] buf){
		
		if (!isDebugEnabled()){
			return "";
		}
		
		StringBuffer sb = new StringBuffer();
		String separator = System.getProperty("line.separator");
		
		int i=0,j=0,k=0;
		byte c;
		int width = 16;
		sb.append(separator);
		sb.append(title).append("[").append(buf.length).append("]:").append(separator);
		
		while(j*width < buf.length){
			
			sb.append(String.format(" %04X: ", j*width));
			
			for (i=0; i<width; i++){
				
				if ((i+j*width) >= buf.length){
					break;
				}
				
				c = buf[i+j*width];
				sb.append(String.format("%02X ", c));
				if ((i+1)%8==0){
					sb.append(" ");
				}
			}
			
			for (k=i; k<width; k++){
				
				sb.append(" ");
				if ((k+1)%8==0){
					sb.append(" ");
				}
			}
			
			sb.append(" ");
			
			for (i=0; i<width; i++){
				
				if ((i+j*width) >= buf.length){
					break;
				}
				
				c = buf[i+j*width];
				if (c>=0x30 && c<=0x7a){
					sb.append(String.format("%c", c));
				} else {
					sb.append(String.format("%c", "."));
				}
			}
			
			sb.append(separator);
			j++;
		}
		
		return sb.toString();
	}
	
}
