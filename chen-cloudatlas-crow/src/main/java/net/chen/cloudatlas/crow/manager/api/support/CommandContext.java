package net.chen.cloudatlas.crow.manager.api.support;

import java.io.Serializable;

import net.chen.cloudatlas.crow.manager.api.RegistryEventType;

/**
 * Command 执行上下文
 * @author chenn
 *
 */
public class CommandContext<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3729977932663175720L;

	final RegistryEventType type;
	
	private String path;
	
	private T data;
	
	public CommandContext(final RegistryEventType type){
		super();
		this.type = type;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public RegistryEventType getType() {
		return type;
	}
	
	@Override
	public String toString(){
		return "CommandContext [type=" + type + ", path=" + path + ", data=" + data + "]";
	}
}
