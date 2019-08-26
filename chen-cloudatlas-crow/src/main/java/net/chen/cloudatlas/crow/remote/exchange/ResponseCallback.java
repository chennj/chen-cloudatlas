package net.chen.cloudatlas.crow.remote.exchange;

public interface ResponseCallback {

	void done(Object response);
	
	void caught(Throwable exception);
}
