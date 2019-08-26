package net.chen.cloudatlas.crow.monitor.api;

import net.chen.cloudatlas.crow.common.URL;

public interface Monitor {

	void collect(URL statistics);
}
