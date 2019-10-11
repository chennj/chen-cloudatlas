package net.chen.cloudatlas.crow.monitor.crow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.common.CompressAlgorithmType;
import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.DcType;
import net.chen.cloudatlas.crow.common.Protocols;
import net.chen.cloudatlas.crow.common.SerializationType;
import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.common.cluster.FailType;
import net.chen.cloudatlas.crow.common.cluster.LoadBalanceType;
import net.chen.cloudatlas.crow.common.utils.NameableServiceLoader;
import net.chen.cloudatlas.crow.config.CrowClientContext;
import net.chen.cloudatlas.crow.monitor.api.Monitor;
import net.chen.cloudatlas.crow.monitor.api.MonitorFactory;
import net.chen.cloudatlas.crow.monitor.api.MonitorService;
import net.chen.cloudatlas.crow.remote.codec.crow.CrowCodecVersion;
import net.chen.cloudatlas.crow.rpc.Invoker;
import net.chen.cloudatlas.crow.rpc.Protocol;
import net.chen.cloudatlas.crow.rpc.proxy.JdkProxyFactory;

public class CrowMonitorFactory implements MonitorFactory{

	private static ConcurrentHashMap<String, Monitor> monitorMap = new ConcurrentHashMap<>();
	
	@Override
	public String getName() {
		return Protocols.CROW_RPC;
	}

	@Override
	public synchronized Monitor getMonitor(URL url) {
		
		// key的值全局固定为MonitorService，而不是每一个url一个key
		// 一个crow进程一个monitor实例，而不是每一个service一个monitor实例
		String key = MonitorService.class.getName();
		
		Monitor result = this.monitorMap.get(key);
		if (null == result || CrowClientContext.getConfig().getMonitorConfig().isModified()){
			CrowClientContext.getConfig().getMonitorConfig().setModified(false);
			result = createMonitor(url);
			if (null == result){
				return result;
			}
			this.monitorMap.put(key, result);
		}
		
		return result;
	}

	private Monitor createMonitor(URL url) {
		
		Logger.debug("createMonitor for "+url);
		
		Protocol protocol = NameableServiceLoader.getLoader(Protocol.class).getService(Protocols.CROW_RPC);
		
		long monitorInterval = url.getParameter(Constants.MONITOR_INTERVAL, Constants.DEFAULT_MONITOR_INTERVAL);
		
		// 不再通过invoker，获取monitor的url，monitor url 为是一个全局的配置，在这里直接获取
		String urls = CrowClientContext.getConfig().getMonitorConfig().getUrls();
		Map<DcType, HashSet<String>> urlMap = CrowClientContext.getConfig().getMonitorConfig().getUrlMap();
		List<Invoker> invokers = new ArrayList<>();
		
		int groupNo = 0;
		Iterator it = urlMap.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry entry = ((Map.Entry)it.next());
			DcType dc = (DcType)entry.getKey();
			HashSet<String> dcUrls = (HashSet<String>)entry.getValue();
			for (String monitorUrl : dcUrls){
				String ipPort[] = monitorUrl.split(Constants.IP_PORT_SEPERATOR);
				String monitorIp = ipPort[0];
				String monitorPort = ipPort[1];
				
				URL referUrl = new URL(
						Protocols.CROW_RPC,
						monitorIp,
						Integer.parseInt(monitorPort),
						MonitorService.class.getName(),
						Constants.MONITOR_INTERVAL, url.getParameter(Constants.MONITOR_INTERVAL),
						Constants.SERIALIZATION_TYPE, SerializationType.HESSIAN2.getText(),
						Constants.COMPRESS_ALGORITHM, CompressAlgorithmType.NONE.getText(),
						Constants.HEARTBEAT_INTERVAL, String.valueOf(Constants.DEFAULT_HEARTBEAT_INTERVAL),
						Constants.PROTOCOL_VERSION, CrowCodecVersion.V10.getVersion(),
						Constants.SERVICE_VERSION, Constants.DEFAULT_SERVICE_VERSION,
						Constants.FAIL_STRATEGY, FailType.FAIL_SAFE.getText(),
						Constants.LOAD_BALANCE_STRATEGY, LoadBalanceType.RANDOM.getText(),
						Constants.DC, dc.getText(),	//monitor 只部署在sh and bj
						Constants.IS_MONITOR, "true",
						Constants.ONE_WAY, "true",
						Constants.GROUP, String.valueOf(groupNo)
						);
				
				Logger.debug("refer "+referUrl);
				
				Invoker<MonitorService> monitorInvoker = protocol.refer(MonitorService.class, referUrl, null);
				
				invokers.add(monitorInvoker);
			}
			
			groupNo++;
		}
		
		if (invokers.size() == 0){
			return null;
		}
		
		Invoker<MonitorService> invoker;
		try {
			invoker = (Invoker)Class.forName("net.chen.cloudatlas.crow.cluster.invoker.GroupInvoker")
					.getConstructor(List.class).newInstance(invokers);
			invoker.setInterface(MonitorService.class);
		} catch (Exception e){
			throw new RuntimeException("create monitor FailoverInvoker error!",e);
		}
		
		MonitorService monitorService = new JdkProxyFactory().getProxy(invoker);
		
		return new CrowMonitor(invoker, monitorService, monitorInterval);
		
	}

	@Override
	public void stopMonitor() {
		
		Iterator<?> it = monitorMap.entrySet().iterator();
		while (it.hasNext()){
			
			Map.Entry entry = ((Map.Entry)it.next());
			Monitor monitor = (Monitor)entry.getValue();
			if (null != monitor && monitor instanceof CrowMonitor){
				((CrowMonitor)monitor).destroy();
			}
		}
	}

}
