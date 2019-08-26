package net.chen.cloudatlas.crow.monitor.api;

import net.chen.cloudatlas.crow.common.NameableService;
import net.chen.cloudatlas.crow.common.URL;

public interface MonitorFactory extends NameableService{

	Monitor getMonitor(URL url);
	
	void stopMonitor();
}
