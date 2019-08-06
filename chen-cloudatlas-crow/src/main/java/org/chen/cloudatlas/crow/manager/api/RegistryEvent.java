package org.chen.cloudatlas.crow.manager.api;

import java.io.Serializable;
import java.util.List;

import org.chen.cloudatlas.crow.common.DcType;

public class RegistryEvent<T> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5372972594791965699L;

	private final RegistryEventType type;
	
	private DcType dc;
	
	private String path;
	
	private T nodeData;
	
	private List<T> initData;
	
	public RegistryEvent(final RegistryEventType type){
		super();
		this.type = type;
	}

	public DcType getDc() {
		return dc;
	}

	public void setDc(DcType dc) {
		this.dc = dc;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public T getNodeData() {
		return nodeData;
	}

	public void setNodeData(T nodeData) {
		this.nodeData = nodeData;
	}

	/**
	 * initialized事件触发时此方法才会返回数据，否则为null。
	 * @return
	 */
	public List<T> getInitData() {
		return initData;
	}

	public void setInitData(List<T> initData) {
		this.initData = initData;
	}

	public RegistryEventType getType() {
		return type;
	}

	@Override
	public String toString(){
		return "RegistryEvent [ type=" + type + ", dc=" + dc + ", path=" + path +
				", nodeData=" + nodeData + ", initData=" + initData + "]";
	}
}
