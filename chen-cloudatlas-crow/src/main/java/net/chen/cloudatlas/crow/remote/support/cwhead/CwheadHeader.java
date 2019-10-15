package net.chen.cloudatlas.crow.remote.support.cwhead;

import net.chen.cloudatlas.crow.remote.Message;

/**
 * 未完成
 * @author chenn
 *
 */
public abstract class CwheadHeader implements Message{

	public static final String ID = "id";
	
	public static final String VERSION = "version";
	
	public static final String LOGID = "log_id";
	
	public static final String PROVIDER = "provider";
	
	public static final String MAGICNUM = "magic_num";
	
	public static final String BODYLEN = "body_len";
	
	
	public static final int PROVIDER_LENGTH = 16;
	
	public static final int CWHEAD_LENGTH = 32;
	
	public static final int BASE = 100000000;
	
	public static final int SEED = 899999999;
	
	private short id = 0;
	
	private short version = 1;
}
