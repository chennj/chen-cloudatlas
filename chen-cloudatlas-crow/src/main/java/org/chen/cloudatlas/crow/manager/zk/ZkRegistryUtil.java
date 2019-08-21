package org.chen.cloudatlas.crow.manager.zk;

import org.chen.cloudatlas.crow.common.DcType;

public class ZkRegistryUtil {

	/**
	 * 获取某service具体路径
	 * @param dc
	 * @param serviceId
	 * @return
	 */
	public static final String getServicePath(final DcType dc, final String serviceId){
		
		return ZkConst.ZK_SERVICE
				.replace(ZkConst.ZK_PLACEHOLDER_CENTER, dc.toString())
				.replace(ZkConst.ZK_PLACEHOLDER_SERVICEID, serviceId);
	}
}
