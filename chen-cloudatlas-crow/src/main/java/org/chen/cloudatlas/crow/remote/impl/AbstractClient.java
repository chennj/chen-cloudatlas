package org.chen.cloudatlas.crow.remote.impl;

import org.chen.cloudatlas.crow.common.URL;
import org.chen.cloudatlas.crow.remote.ChannelListener;
import org.chen.cloudatlas.crow.remote.Client;

public abstract class AbstractClient extends AbstractEndpoint implements Client{

	public AbstractClient(URL url, ChannelListener listener) {
		super(url, listener);
	}

}
