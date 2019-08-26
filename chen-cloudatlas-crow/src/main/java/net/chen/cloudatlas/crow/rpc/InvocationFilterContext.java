package net.chen.cloudatlas.crow.rpc;

import java.util.Map;

import org.tinylog.Logger;

public class InvocationFilterContext {

	private static final ThreadLocal<InvocationFilterContext> LOCAL = new ThreadLocal<InvocationFilterContext>(){

		@Override
		protected InvocationFilterContext initialValue() {
			return new InvocationFilterContext();
		}
		
	};
	
	private Map<String, String> subAttachments;

	public static InvocationFilterContext getContext(){
		return LOCAL.get();
	}
	
	public static void removeContext(){
		LOCAL.remove();
	}
	
	public Map<String, String> getSubAttachments(){
		return subAttachments;
	}
	
	/**
	 * 这个map的key不能是path,dc,serviceVersion,version,serviceId,protocolVersion<br>
	 * 这些crow会使用
	 * @param attachments
	 */
	public void setSubAttachments(Map<String,String> attachments){
		this.subAttachments = attachments;
		Logger.debug("SubAttachments: {}",attachments.toString());
	}
}
