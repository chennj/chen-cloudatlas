package org.chen.cloudatlas.crow.manager.zk;

public interface ZkConst {

	static final String ZK_URL = "";
	
	// 占位符
	static final String ZK_PLACEHOLDER_CENTER = "{center}";
	static final String ZK_PLACEHOLDER_SERVICEID = "{serviceId}";
	
	// 根
	static final String ZK_ROOT = "/crow";
	
	// zk树设置
	static final String ZK_SVC = ZK_ROOT + "/" + ZK_PLACEHOLDER_CENTER + "/service";
	static final String ZK_LOCK = ZK_ROOT + "/" + ZK_PLACEHOLDER_CENTER + "/lock";
	
	static final String ZK_SERVICE = ZK_SVC + "/" + ZK_PLACEHOLDER_SERVICEID;
}
