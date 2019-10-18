package net.chen.cloudatlas.crow.remote.support.rmif;

import java.util.Map;

import net.chen.cloudatlas.crow.common.Protocols;
import net.chen.cloudatlas.crow.remote.MessageWrapper;
import net.chen.cloudatlas.crow.remote.RemoteException;
import net.chen.cloudatlas.crow.remote.Request;
import net.chen.cloudatlas.crow.remote.Response;

public class RMIFMessageWrapper extends MessageWrapper{

	@Override
	public String getName() {
		return Protocols.CROW_RMI;
	}

	@Override
	public Request wrapRequest(byte[] payload, Map<String, Object> attachments) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Request wrapRequest(Request request, Map<String, Object> attachments) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] decomposeResponse(Response response) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response wrapResponse(Request request, Map<String, Object> attachments) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response wrapResponse(Response resposne, Map<String, Object> attachments) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Request wrapHearbeat(String protocolVersion) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
