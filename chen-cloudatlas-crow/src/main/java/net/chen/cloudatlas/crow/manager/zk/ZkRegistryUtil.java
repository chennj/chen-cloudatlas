package net.chen.cloudatlas.crow.manager.zk;

import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.utils.PathUtils;

import com.alibaba.fastjson.JSON;

import net.chen.cloudatlas.crow.common.DcType;
import net.chen.cloudatlas.crow.common.utils.NetUtil;
import net.chen.cloudatlas.crow.config.ProtocolConfig;
import net.chen.cloudatlas.crow.manager.api.RegistryConnectionState;
import net.chen.cloudatlas.crow.manager.api.RegistryEventType;
import net.chen.cloudatlas.crow.manager.api.support.CommandType;

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
	
	/**
	 * 获得某Provider具体路径
	 * @param dc
	 * @param serviceId
	 * @param hostPort
	 * @return
	 */
	public static final String getProviderPath(final DcType dc, final String serviceId, String hostPort){
		
		return ZkConst.ZK_PROVIDER
				.replace(ZkConst.ZK_PLACEHOLDER_CENTER, dc.toString())
				.replace(ZkConst.ZK_PLACEHOLDER_SERVICEID, serviceId)
				+ "/" + hostPort;
	}
	
	/**
	 * 获得某persistent provider具体路径
	 * @param dc
	 * @param serviceId
	 * @param hostPort
	 * @return
	 */
	public static final String getPersistentProviderPath(final DcType dc, final String serviceId, String hostPort){
		
		return ZkConst.ZK_PROVIDER_PERSISTENT
				.replace(ZkConst.ZK_PLACEHOLDER_CENTER, dc.toString())
				.replace(ZkConst.ZK_PLACEHOLDER_SERVICEID, serviceId)
				+ "/" + hostPort;
	}
	
	/**
	 * 获得某persistent parent provider具体路径
	 * @param dc
	 * @param serviceId
	 * @param hostPort
	 * @return
	 */
	public static final String getPersistentProviderParentPath(final DcType dc, final String serviceId){
		
		return ZkConst.ZK_PROVIDER_PERSISTENT
				.replace(ZkConst.ZK_PLACEHOLDER_CENTER, dc.toString())
				.replace(ZkConst.ZK_PLACEHOLDER_SERVICEID, serviceId);
	}

	/**
	 * 获得某backup provider具体路径
	 * @param dc
	 * @param serviceId
	 * @param hostPort
	 * @return
	 */
	public static final String getBackupProviderPath(final DcType dc, final String serviceId, String hostPort){
		
		return ZkConst.ZK_PROVIDER_BACKUP
				.replace(ZkConst.ZK_PLACEHOLDER_CENTER, dc.toString())
				.replace(ZkConst.ZK_PLACEHOLDER_SERVICEID, serviceId)
				+ "/" + hostPort;
	}
	
	/**
	 * 获得某backup provider的父节点路径
	 * @param dc
	 * @param serviceId
	 * @param hostPort
	 * @return
	 */
	public static final String getBackupProviderParentPath(final DcType dc, final String serviceId){
		
		return ZkConst.ZK_PROVIDER_BACKUP
				.replace(ZkConst.ZK_PLACEHOLDER_CENTER, dc.toString())
				.replace(ZkConst.ZK_PLACEHOLDER_SERVICEID, serviceId);
	}

	/**
	 * 获得某服务所有Provider的父节点路径
	 * @param dc
	 * @param serviceId
	 * @param hostPort
	 * @return
	 */
	public static final String getProviderParentPath(final DcType dc, final String serviceId){
		
		return ZkConst.ZK_PROVIDER
				.replace(ZkConst.ZK_PLACEHOLDER_CENTER, dc.toString())
				.replace(ZkConst.ZK_PLACEHOLDER_SERVICEID, serviceId);
	}
	
	/**
	 * 获得某consumer具体路径
	 * @param dc
	 * @param serviceId
	 * @param hostPort
	 * @return
	 */
	public static final String getConsumerPath(final DcType dc, final String serviceId, String hostPort){
		
		return ZkConst.ZK_CONSUMER
				.replace(ZkConst.ZK_PLACEHOLDER_CENTER, dc.toString())
				.replace(ZkConst.ZK_PLACEHOLDER_SERVICEID, serviceId)
				+ "/" + hostPort;
	}
	
	/**
	 * 获得所有Consumer父节点路径
	 * @param dc
	 * @param serviceId
	 * @param hostPort
	 * @return
	 */
	public static final String getConsumerParentPath(final DcType dc, final String serviceId){
		
		return ZkConst.ZK_CONSUMER
				.replace(ZkConst.ZK_PLACEHOLDER_CENTER, dc.toString())
				.replace(ZkConst.ZK_PLACEHOLDER_SERVICEID, serviceId);
	}
	
	/**
	 * 获得所有command节点路径
	 * @param dc
	 * @param serviceId
	 * @param hostPort
	 * @return
	 */
	public static final String getCommandPath(final DcType dc, final String serviceId, final CommandType commandType){
		
		return ZkConst.ZK_COMMAND
				.replace(ZkConst.ZK_PLACEHOLDER_CENTER, dc.toString())
				.replace(ZkConst.ZK_PLACEHOLDER_SERVICEID, serviceId)
				+ "/" + commandType.name().toLowerCase();
	}
	
	/**
	 * 序列化节点数据对象
	 * @param dataObject
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] serializeNodeData(Object dataObject) throws UnsupportedEncodingException{
		
		return JSON.toJSONString(dataObject).getBytes("utf-8");
	}
	
	/**
	 * 反序列化节点数据对象
	 * @param bytes
	 * @param clazz
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static <T> T deserializeNodeData(byte[] bytes, Class<T> clazz) throws UnsupportedEncodingException{
		
		return JSON.parseObject(new String(bytes, "utf-8"), clazz);
	}
	
	/**
	 * 获得consumer的节点key
	 * @param cfg
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String getProviderNodeKey(ProtocolConfig cfg){
		
		String cfgHost = cfg.getId();
		if (StringUtils.isEmpty(cfgHost)){
			cfgHost = NetUtil.getLocalAddress().getHostAddress();
		}
		
		return cfgHost + "-" + getPid();
	}
	
	/**
	 * state转换
	 * @param newState
	 * @return
	 */
	public static RegistryConnectionState stateConvert(ConnectionState newState){
		
		RegistryConnectionState state;
		
		switch (newState){
		case CONNECTED:
			state = RegistryConnectionState.CONNECTED;
			break;
		case SUSPENDED:
			state = RegistryConnectionState.SUSPENDED;
			break;
		case RECONNECTED:
			state = RegistryConnectionState.RECONNECTED;
			break;
		case LOST:
			state = RegistryConnectionState.LOST;
			break;
		case READ_ONLY:
			state = RegistryConnectionState.READ_ONLY;
			break;
		default:
			state = RegistryConnectionState.LOST;
			break;
		}
		return state;
	}

	/**
	 * Zk的事件到crow配置管理事件的转换
	 * @param eventType
	 * @return
	 */
	public static RegistryEventType eventTypeConvert(PathChildrenCacheEvent.Type eventType){
		
		RegistryEventType type;
		
		switch (eventType){
		case CHILD_ADDED:
			type = RegistryEventType.NODE_CREATED;
			break;
		case CHILD_REMOVED:
			type = RegistryEventType.NODE_REMOVED;
			break;
		case CHILD_UPDATED:
			type = RegistryEventType.NODE_DATA_UPDATE;
			break;
		case INITIALIZED:
			type = RegistryEventType.INITIALIZED;
			break;
		default:
			type = RegistryEventType.NODE_DATA_UPDATE;
			break;
		}
		
		return type;
	}
	
	/**
	 * 将znode节点路径由产生根到最终的路径
	 * @param path
	 * @return
	 */
	public static String[] nodePathArray(String path){
		
		PathUtils.validatePath(path);
		String[] nodes = path.split("/");
		List<String> pathes = new ArrayList<String>();
		
		// add root
		String lastNode = "";
		pathes.add("/");
		for (String node : nodes){
			if (!StringUtils.isEmpty(node)){
				lastNode = lastNode + "/" + node;
				pathes.add(lastNode);
			}
		}
		return pathes.toArray(new String[0]);
	}
	
	public static String getPid(){
		String name = ManagementFactory.getRuntimeMXBean().getName();
		String pid = name.split("@")[0];
		return pid;
	}

	public static String getConsumerNodeKey(ProtocolConfig protocol) {
		
		String host = protocol.getIp();
		
		if (StringUtils.isBlank(host)){
			host = NetUtil.getLocalAddress().getHostAddress();
		}
		return host+"-"+getPid();
	}
}
