package net.chen.cloudatlas.crow.remote.log;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class CrowLoggerFactory extends InternalLoggerFactory{

	@Override
	protected InternalLogger newInstance(String name) {		
		return new CrowLogger(name);
	}

}
