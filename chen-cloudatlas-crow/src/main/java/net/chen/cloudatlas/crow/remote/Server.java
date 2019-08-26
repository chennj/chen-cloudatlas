package net.chen.cloudatlas.crow.remote;

public interface Server extends Endpoint{

	void bind();
	boolean isBound();
	void setChannelListener(ChannelListener listener);
}
