package net.chen.cloudatlas.crow.remote.exchange;

import net.chen.cloudatlas.crow.remote.codec.AbstractEncoder;
import net.chen.cloudatlas.crow.remote.codec.crow.CrowEncoderAdapter;
import net.chen.cloudatlas.crow.remote.support.crow.CrowHeader;

public class ExchangeEncoderAdapter extends CrowEncoderAdapter{

	private final AbstractEncoder abstractEncoder;
	
	public ExchangeEncoderAdapter(AbstractEncoder abstractEncoder) {
		super(abstractEncoder);
		this.abstractEncoder = abstractEncoder;
	}

	@Override
	protected byte[] serializeData(CrowHeader message) {
		// TODO Auto-generated method stub
		return super.serializeData(message);
	}

	@Override
	protected boolean isRequest(Object msg) {
		// TODO Auto-generated method stub
		return super.isRequest(msg);
	}

	
}
