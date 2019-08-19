package org.chen.cloudatlas.crow.remote.exchange;

import org.chen.cloudatlas.crow.remote.codec.crow.CrowCodecVersion;
import org.chen.cloudatlas.crow.remote.support.crow.CrowRequest;

public class ExchangeRequest extends CrowRequest implements RpcData{

	private Object data;
	
	/**
	 * 存放序列化时出现的异常，后续会在handler中处理
	 */
	private Exception exception;
	
	public ExchangeRequest(){
		super();
	}
	
	public ExchangeRequest(CrowCodecVersion version){
		super(version);
	}
	@Override
	public Object getData() {
		return data;
	}

	@Override
	public void setData(Object data) {
		this.data = data;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	@Override
	public String toString(){
		
		StringBuffer sb = new StringBuffer(super.toString());
		String separator = System.getProperty("line.separator");
		
		sb
		.append("=================")
		.append(this.getClass().getSimpleName()).append(" body")
		.append("================")
		.append(separator)
		.append("data:            [").append(data).append("]")
		.append(separator)
		.append("============================================")
		.append(separator);
		
		return sb.toString();
	}
}
