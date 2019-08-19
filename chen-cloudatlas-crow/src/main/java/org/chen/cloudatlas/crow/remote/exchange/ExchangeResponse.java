package org.chen.cloudatlas.crow.remote.exchange;

import org.chen.cloudatlas.crow.remote.codec.crow.CrowCodecVersion;
import org.chen.cloudatlas.crow.remote.support.crow.CrowResponse;

public class ExchangeResponse extends CrowResponse implements RpcData{

	/**
	 * payload中的对象
	 */
	private Object data;
	
	private String errorMsg;
	
	public ExchangeResponse(){
		super();
	}
	
	public ExchangeResponse(CrowCodecVersion version){
		super(version);
	}
	
	public ExchangeResponse(long requestId){
		this();
		this.setRequestId(requestId);
	}
	
	public ExchangeResponse(ExchangeRequest request){
		super(request);
	}
	
	@Override
	public Object getData() {
		return data;
	}

	@Override
	public void setData(Object data) {
		this.data = data;
	}

	@Override
	public boolean isBinary() {
		return false;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	@Override
	public String toString(){
		
		StringBuffer sb = new StringBuffer(super.toString());
		String separator = System.getProperty("line.separator");
		
		sb
		.append("=================")
		.append(this.getClass().getSimpleName()).append(" body")
		.append("=================")
		.append(separator)
		.append("data:                    [").append(data).append("]")
		.append(separator)
		.append("=================================================")
		.append(separator);
		
		return sb.toString();
	}
}
