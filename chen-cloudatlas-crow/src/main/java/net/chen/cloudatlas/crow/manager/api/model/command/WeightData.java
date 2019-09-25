package net.chen.cloudatlas.crow.manager.api.model.command;

import java.io.Serializable;

/**
 * CommandType.WEIGHT的节点的数据模型
 * 
 * @author chenn
 *
 */
public class WeightData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5698018376510059798L;

	private long updateTimeMillis;
	
	private int weight;
	
	public WeightData(){
		super();
	}

	public long getUpdateTimeMillis() {
		return updateTimeMillis;
	}

	public void setUpdateTimeMillis(long updateTimeMillis) {
		this.updateTimeMillis = updateTimeMillis;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}
	
	@Override
	public String toString(){
		return "WeightData [updateTimeMillis=" + updateTimeMillis + ", weight=" + weight + "]";
	}
}
