package net.chen.cloudatlas.crow.remote.support.cwhead;

import java.util.Map;

import net.chen.cloudatlas.crow.common.Protocols;
import net.chen.cloudatlas.crow.remote.MessageWrapper;
import net.chen.cloudatlas.crow.remote.RemoteException;
import net.chen.cloudatlas.crow.remote.Request;
import net.chen.cloudatlas.crow.remote.Response;

public class CwheadMessageWrapper extends MessageWrapper{

	@Override
	public String getName() {
		return Protocols.CROW_HEAD;
	}

	@Override
	public Request wrapRequest(byte[] payload, Map<String, Object> attachments) throws RemoteException {
		return new CwheadMessage(payload);
	}

	@Override
	public Request wrapRequest(Request request, Map<String, Object> attachments) throws RemoteException {
		// TODO Auto-generated method stub
		return request;
	}

	@Override
	public byte[] decomposeResponse(Response response) {
		if (response instanceof CwheadMessage){
			return ((CwheadMessage)response).getContent();
		} else {
			throw new RuntimeException("response must be CwheadMessage");
		}
	}

	@Override
	public Response wrapResponse(Request request, Map<String, Object> attachments) throws RemoteException {
		return (Response)request;
	}

	@Override
	public Response wrapResponse(Response response, Map<String, Object> attachments) throws RemoteException {
		return response;
	}

	@Override
	public Request wrapHearbeat(String protocolVersion) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
