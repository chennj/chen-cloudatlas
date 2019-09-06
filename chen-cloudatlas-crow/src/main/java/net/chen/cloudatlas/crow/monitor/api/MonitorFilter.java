package net.chen.cloudatlas.crow.monitor.api;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.util.StringUtils;

import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.Protocols;
import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.common.annotation.Activate;
import net.chen.cloudatlas.crow.common.utils.NameableServiceLoader;
import net.chen.cloudatlas.crow.common.utils.NetUtil;
import net.chen.cloudatlas.crow.config.CrowClientContext;
import net.chen.cloudatlas.crow.config.MonitorConfig;
import net.chen.cloudatlas.crow.rpc.Filter;
import net.chen.cloudatlas.crow.rpc.Invocation;
import net.chen.cloudatlas.crow.rpc.Invoker;
import net.chen.cloudatlas.crow.rpc.Result;
import net.chen.cloudatlas.crow.rpc.RpcException;
import net.chen.cloudatlas.crow.rpc.utils.RpcUtil;

/**
 * 同时用在consumer端和provider端。<br>
 * 拦截invoke，再invoke后收集该次invoke的统计信息。<br>
 * <p>
 * 统计信息统一为URL形式：protocol://localhost:localPort/serviceId?remoteKey=xxx&remoteAddress=xxx&group=xxx&
 * application=xxx&serviceId=xxx&method=xxx&success=1&fail=1&elapse=xxx&monitorInterval=5000
 * </p>
 * 统计要素有：<br>
 * <ul>
 * <li>remoteHost</li>
 * <li>remotePort</li>
 * <li>localHost</li>
 * <li>group</li>
 * <li>application</li>
 * <li>serviceId</li>
 * <li>method</li>
 * <li>success</li>
 * <li>fail</li>
 * <li>elapse</li>
 * <li>concurrent</li>
 * <li>monitorInterval</li>
 * </ul>
 * @author chenn
 *
 */
@Activate(side={Constants.CONSUMER,Constants.PROVIDER})
public class MonitorFilter implements Filter{

	/**
	 * 记录当前并发。<interface#method,AtomicInteger>
	 */
	private ConcurrentMap<String, AtomicInteger> concurrentMap = new ConcurrentHashMap<>();
	
	private MonitorFactory factory = NameableServiceLoader.getService(MonitorFactory.class, Protocols.CROW_RPC);
	
	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public Result doFilter(Invoker<?> invoker, Invocation invocation) throws RpcException {
		
		//只有配置了monitor server信息后才启用
		MonitorConfig mConfig = CrowClientContext.getConfig().getMonitorConfig();
		
		if (null!=mConfig && !StringUtils.isEmpty(mConfig.getUrls())){
			Result result;
			long start = System.currentTimeMillis();
			int concurrent = getConcurrent(invoker,invocation).incrementAndGet();//并发+1
			try {
				result = invoker.invoke(invocation);
				collect(invoker, invocation, true, start, concurrent);
			} catch (RpcException e){
				collect(invoker, invocation, false, start, concurrent);
				throw e;
			} finally{
				getConcurrent(invoker, invocation).decrementAndGet();//并发-1
			}
			return result;
		} else {
			return invoker.invoke(invocation);
		}
	}

	/**
	 * 组装URL，传递给monitor处理
	 * @param invoker
	 * @param invocation
	 * @param success
	 * @param start
	 * @param concurrent
	 */
	private void collect(Invoker<?> invoker, Invocation invocation, boolean success, long start, int concurrent){
		
		long end = System.currentTimeMillis();
		URL url = invoker.getUrl();
		Monitor monitor = factory.getMonitor(url);
		if (null==monitor){
			return;
		}
		
		String remoteKey;
		String remoteAddress;
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
				NetUtil.getLocalHost(),
				localPort,
				invoker.getInterface().getName(),
				Constants.SIDE,side,
				Constants.GROUP,url.getParameter(Constants.GROUP),
				Constants.APPLICATION,url.getParameter(Constants.APPLICATION),
				Constants.DC,url.getParameter(Constants.DC),
				Constants.SERVICE_ID,url.getParameter(Constants.SERVICE_ID),
				Constants.SERVICE_VERSION,url.getParameter(Constants.SERVICE_VERSION),
				Constants.METHOD,invocation.getMethodName(),
				Constants.SUCC_COUNT,success?"1":"0",
				Constants.FAIL_COUNT,success?"0":"1",
				Constants.TOTAL_RT,String.valueOf(end-start),
				Constants.CONCURRENT,String.valueOf(concurrent),
				Constants.MONITOR_INTERVAL,url.getParameter(Constants.MONITOR_INTERVAL));
		
		monitor.collect(statistics);
	}
	
	private AtomicInteger getConcurrent(Invoker<?> invoker, Invocation invocation){
		
		String key = invoker.getInterface().getName() + "#" + invocation.getMethodName();
		AtomicInteger result = concurrentMap.get(key);
		if (null == result){
			result = new AtomicInteger(0);
			concurrentMap.put(key, result);
		}
		return result;
	}
	
}
