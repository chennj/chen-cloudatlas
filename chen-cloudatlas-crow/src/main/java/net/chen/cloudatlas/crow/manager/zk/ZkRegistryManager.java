package net.chen.cloudatlas.crow.manager.zk;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.manager.api.AbstractRegistryManager;
import net.chen.cloudatlas.crow.manager.api.RegistryClient;

public class ZkRegistryManager extends AbstractRegistryManager{

	public static final String EXT_NAME = "zookeeper";
	
	private final ConcurrentMap<String, RegistryClient> clientMap = new ConcurrentHashMap<>();
	
	public ZkRegistryManager(){
		
	}
	
	@Override
	public RegistryClient getRegistry(URL url) {
		
		final String key = url.getParameter(Constants.ADDRESS);

		RegistryClient result = clientMap.get(key);		
		if (null == result){
			result  = new ZkRegistryClient(url);
			RegistryClient old = clientMap.putIfAbsent(key, result);
			result = null == old ? result : old;
		}
		
		return result;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return EXT_NAME;
	}

}
