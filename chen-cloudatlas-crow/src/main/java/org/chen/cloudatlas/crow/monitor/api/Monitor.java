package org.chen.cloudatlas.crow.monitor.api;

import org.chen.cloudatlas.crow.common.URL;

public interface Monitor {

	void collect(URL statistics);
}
