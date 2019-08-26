package net.chen.cloudatlas.crow.remote.impl;

import net.chen.cloudatlas.crow.common.URL;
import net.chen.cloudatlas.crow.remote.ChannelListener;
import net.chen.cloudatlas.crow.remote.Client;

public abstract class AbstractClient extends AbstractEndpoint implements Client{

	public AbstractClient(URL url, ChannelListener listener) {
		super(url, listener);
	}

}
