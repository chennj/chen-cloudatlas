package net.chen.cloudatlas.crow.filter.support;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.Protocols;
import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.common.annotation.Activate;
import net.chen.cloudatlas.crow.common.utils.NameableServiceLoader;
import net.chen.cloudatlas.crow.config.CrowClientContext;
import net.chen.cloudatlas.crow.config.CrowServerContext;
import net.chen.cloudatlas.crow.config.MonitorConfig;
import net.chen.cloudatlas.crow.config.ProtocolConfig;
import net.chen.cloudatlas.crow.config.ServiceConfig;
import net.chen.cloudatlas.crow.filter.BinaryFilter;
import net.chen.cloudatlas.crow.filter.BinaryFilterChain;
import net.chen.cloudatlas.crow.monitor.api.Monitor;
import net.chen.cloudatlas.crow.monitor.api.MonitorFactory;
import net.chen.cloudatlas.crow.remote.RemoteException;
import net.chen.cloudatlas.crow.remote.Request;
import net.chen.cloudatlas.crow.remote.Response;
import net.chen.cloudatlas.crow.remote.support.crow.CrowRequest;
import net.chen.cloudatlas.crow.rpc.RpcException;
import net.chen.cloudatlas.crow.rpc.SubInvoker;
import net.chen.cloudatlas.crow.rpc.utils.RpcUtil;

/**
 * 应用在consumer端。拦截call，在call后收集该次调用的统计信息
 * @author chenn
 *
 */
@Activate(side={Constants.CONSUMER, Constants.PROVIDER})
public class BinaryMonitorFilter implements BinaryFilter{

	private static final Map<Integer, String> portServiceIdMap = new ConcurrentHashMap<>();
	
	/**
	 * 记录当前并发。<serviceId, AtomicInteger>
	 */
	private ConcurrentHashMap<String, AtomicInteger> concurrentMap = new ConcurrentHashMap<>();
	
	private MonitorFactory factory = NameableServiceLoader.getService(MonitorFactory.class, Protocols.CROW_RPC);
	
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response doFilter(SubInvoker subInvoker, Request request, BinaryFilterChain chain) throws RemoteException {
		
		Logger.trace(this.getClass().getSimpleName()+"#doFilter called!");
		
		// 只有配置了monitor server之后才启用
		
		MonitorConfig mConfig = CrowClientContext.getConfig().getMonitorConfig();
		
		if (null != mConfig && mConfig.getUrls() != null && !mConfig.getUrls().trim().isEmpty()){
			// 针对服务端的url，需要做较多处理，因为binary服务器可能没有
			// serviceId，request中也可能没有serviceId
			URL url = subInvoker.getUrl();
			if (url.getParameter(Constants.SERVICE_ID) == null){
				// 服务调用时url中没有serviceId和path
				if (request instanceof CrowRequest){
					url = url.addParameters(
							Constants.SERVICE_ID,request.getServiceId(),
							Constants.DEFAULT_SERVICE_VERSION,request.getServiceVersion());
					url.setPath(request.getServiceId());
				} else {
					// 其他协议的request可能没有serviceId，通过protocol来配置
					int port = url.getPort();
					String serviceId = portServiceIdMap.get(port);
					if (null == serviceId){
						List<ServiceConfig> services 	= CrowServerContext.getConfig().getServiceConfigList();
						List<ProtocolConfig> protocols 	= CrowServerContext.getConfig().getProtocolConfigList();
						for (ProtocolConfig p : protocols){
							
							if (p.getPort() == port){
								for (ServiceConfig s : services){
									if (s.getProtocol().getId().equals(p.getId())){
										serviceId = s.getServiceId();
										portServiceIdMap.put(port, serviceId);
									}
								}
							}
						}
					}
					url = url.addParameters(
							Constants.SERVICE_ID, serviceId,
							Constants.SERVICE_VERSION,Constants.DEFAULT_SERVICE_VERSION);
				}
			}
			
			Response result;
			long start = System.currentTimeMillis();
			int concurrent = getConcurrent(url).incrementAndGet();//并发+1
			try {
				result = chain.doFilter(subInvoker, request, chain);
				collect(url, true, start, concurrent);
			} catch (RpcException e){
				collect(url, false, start, concurrent);
				throw e;
			} finally{
				getConcurrent(url).decrementAndGet();//并发-1
			}
			return result;
		} else {
			return chain.doFilter(subInvoker, request, chain);
		}
	
	}

	private AtomicInteger getConcurrent(URL url){
		String key = url.getPath();
		AtomicInteger result = concurrentMap.get(key);
		if (null == result){
			result = new AtomicInteger(0);
			concurrentMap.put(key, result);
		}
		return result;
	}
	
	private void collect(URL url,  boolean success, long start, int concurrent){
		
		long end = System.currentTimeMillis();
		Monitor monitor = this.factory.getMonitor(url);
		if (null == monitor){
			return ;
		}
		
		String side;
		int localPort;
		
		if (RpcUtil.isConsumer(url)){
			localPort = 0;
			side = Constants.CONSUMER;
		} else {
			localPort = url.getPort();
			side = Constants.PROVIDER;
		}
		
		URL statistics = new URL(
				url.getProtocol(),
				url.getHost(),
				url.getPort(),
				url.getPath(),
				Constants.SIDE,side,
				Constants.GROUP,url.getParameter(Constants.GROUP),
				Constants.APPLICATION,url.getParameter(Constants.APPLICATION),
				Constants.DC,url.getParameter(Constants.DC),
				Constants.SERVICE_ID,url.getParameter(Constants.SERVICE_ID),
				Constants.SERVICE_VERSION,url.getParameter(Constants.SERVICE_VERSION),
				Constants.METHOD,"",
				Constants.SUCC_COUNT,success?"1":"0",
				Constants.FAIL_COUNT,success?"0":"1",
				Constants.TOTAL_RT,String.valueOf(end-start),
				Constants.CONCURRENT,String.valueOf(concurrent),
				Constants.MONITOR_INTERVAL,url.getParameter(Constants.MONITOR_INTERVAL));
		
		monitor.collect(statistics);

	}
}
