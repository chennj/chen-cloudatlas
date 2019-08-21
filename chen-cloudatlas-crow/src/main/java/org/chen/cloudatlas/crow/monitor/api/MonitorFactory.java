package org.chen.cloudatlas.crow.monitor.api;

import org.chen.cloudatlas.crow.common.NameableService;
import org.chen.cloudatlas.crow.common.URL;

public interface MonitorFactory extends NameableService{

	Monitor getMonitor(URL url);
	
	void stopMonitor();
}
