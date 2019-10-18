package net.chen.cloudatlas.crow.remote.support.exchange;

import net.chen.cloudatlas.crow.common.Protocols;
import net.chen.cloudatlas.crow.remote.support.crow.CrowMessageWrapper;

public class ExchangeMessageWrapper extends CrowMessageWrapper{

	@Override
	public String getName(){
		return Protocols.CROW_RPC;
	}
}
