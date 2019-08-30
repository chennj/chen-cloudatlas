package net.chen.cloudatlas.crow.remote.log;

import org.tinylog.Logger;

import io.netty.util.internal.logging.AbstractInternalLogger;

public class CrowLogger extends AbstractInternalLogger{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected CrowLogger(String name) {
		super(name);
	}

	@Override
	public boolean isTraceEnabled() {
		return Logger.isTraceEnabled();
	}

	@Override
	public void trace(String msg) {
		Logger.trace(msg);		
	}

	@Override
	public void trace(String format, Object arg) {
		Logger.trace(format,arg);
	}

	@Override
	public void trace(String format, Object argA, Object argB) {
		Logger.trace(format,argA,argB);
	}

	@Override
	public void trace(String format, Object... arguments) {
		Logger.trace(format,arguments);
	}

	@Override
	public void trace(String msg, Throwable t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isDebugEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void debug(String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void debug(String format, Object arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void debug(String format, Object argA, Object argB) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void debug(String format, Object... arguments) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void debug(String msg, Throwable t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isInfoEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void info(String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void info(String format, Object arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void info(String format, Object argA, Object argB) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void info(String format, Object... arguments) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void info(String msg, Throwable t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isWarnEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void warn(String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void warn(String format, Object arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void warn(String format, Object... arguments) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void warn(String format, Object argA, Object argB) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void warn(String msg, Throwable t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isErrorEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void error(String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void error(String format, Object arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void error(String format, Object argA, Object argB) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void error(String format, Object... arguments) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void error(String msg, Throwable t) {
		// TODO Auto-generated method stub
		
	}

}
