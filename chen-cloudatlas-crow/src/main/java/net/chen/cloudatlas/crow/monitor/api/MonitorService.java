package net.chen.cloudatlas.crow.monitor.api;

import java.util.List;

import net.chen.cloudatlas.crow.common.URL;

/**
 * rpc service
 * @author chenn
 *
 */
public interface MonitorService {
	
	void collect(URL statistics);
	
	void collect(List<URL> statistics);
}
