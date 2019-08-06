package org.chen.cloudatlas.crow.remote;

import java.io.IOException;

public interface Codec {

	void encode(Channel channel, Object message) throws IOException;
	
	Object decode(Channel channel) throws IOException;
}
