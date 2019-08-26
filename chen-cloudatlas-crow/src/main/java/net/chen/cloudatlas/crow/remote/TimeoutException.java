package net.chen.cloudatlas.crow.remote;

public class TimeoutException extends RemoteException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2168742964414273873L;

	public static final int CLIENT_SIDE = 0;
	public static final int SERVER_SIDE = 1;
	private final int phase;
	
	public TimeoutException(boolean serverSide, Channel channel, String msg) {
		super(channel, msg);
		this.phase = serverSide ? SERVER_SIDE : CLIENT_SIDE;
	}
	
	public int getPhase(){
		return phase;
	}
	
	public boolean isServerSide(){
		return phase == 1;
	}
	
	public boolean isClientSide(){
		return phase == 0;
	}
}
