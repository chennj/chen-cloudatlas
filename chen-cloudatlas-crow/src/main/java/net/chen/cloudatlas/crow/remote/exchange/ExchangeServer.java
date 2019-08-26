package net.chen.cloudatlas.crow.remote.exchange;

import java.net.InetSocketAddress;

import net.chen.cloudatlas.crow.remote.Server;

public interface ExchangeServer extends Server{

	ExchangeChannel getExchangeChannel(InetSocketAddress remoteAddress);
}
