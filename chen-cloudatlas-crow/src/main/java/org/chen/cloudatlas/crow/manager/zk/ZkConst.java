package org.chen.cloudatlas.crow.manager.zk;

public interface ZkConst {

	static final String ZK_URL = "";
	static final int ZK_SESSION_TIMEOUT_MS = 60000;
	static final int ZK_CONNECTION_TIMEOUT_MS = 15000;
	
	// 占位符
	static final String ZK_PLACEHOLDER_CENTER = "{center}";
	static final String ZK_PLACEHOLDER_SERVICEID = "{serviceId}";
	
	// 根
	static final String ZK_ROOT = "/crow";
	
	// zk树设置
	static final String ZK_SVC = ZK_ROOT + "/" + ZK_PLACEHOLDER_CENTER + "/service";
	static final String ZK_LOCK = ZK_ROOT + "/" + ZK_PLACEHOLDER_CENTER + "/lock";
	
	static final String ZK_SERVICE = ZK_SVC + "/" + ZK_PLACEHOLDER_SERVICEID;
	
	static final String ZK_PROVIDER = ZK_SVC + "/" + ZK_PLACEHOLDER_SERVICEID + "/provider";
	static final String ZK_PROVIDER_LOCK = ZK_LOCK + "/" + ZK_PLACEHOLDER_SERVICEID + "/provider";
	
	static final String ZK_CONSUMER = ZK_SVC + "/" + ZK_PLACEHOLDER_SERVICEID + "/consumer";
	static final String ZK_CONSUMER_LOCK = ZK_LOCK + "/" + ZK_PLACEHOLDER_SERVICEID + "/consumer";
	
	static final String ZK_COMMAND = ZK_SVC + "/" + ZK_PLACEHOLDER_SERVICEID + "/command";
	static final String ZK_COMMAND_LOCK = ZK_LOCK + "/" + ZK_PLACEHOLDER_SERVICEID + "/command";
	
	static final String ZK_PROVIDER_PERSISTENT = ZK_SVC + "/" + ZK_PLACEHOLDER_SERVICEID + "/persistent";
	static final String ZK_PROVIDER_BACKUP = ZK_SVC + "/" + ZK_PLACEHOLDER_SERVICEID + "/backup";
	
	static final byte[] ZK_EMPTY_NODE_DATA = "{}".getBytes();
}
