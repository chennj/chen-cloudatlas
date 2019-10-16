package net.chen.cloudatlas.crow.remote.support.cwhead;

import net.chen.cloudatlas.crow.remote.Message;

/**
 * 
 * cwhead协议的报文头对象<br>
 * <pre>
 * 报文分组ID：id_ 目前不填
 * 报文类型: version_ 必填
 * 整体报文跟踪ID：log_id_ 必填
 * 报文发送方：provider_[16] 保留使用
 * MAGIC NUM：magic_num_ 必填
 * 报文体长度：content_len_ 必填
 * </pre>
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
	
	/**
	 * 2+2+4+16+4+4
	 */
	public static final int CWHEAD_LENGTH = 32;
	
	public static final int BASE = 100000000;
	
	public static final int SEED = 899999999;
	
	private short id = 0;
	
	private short version = 1;
	
	/**
	 * cwhead服务端会判断id范围：100000000 - 999999999
	 */
	private int logId = generateLogId();
	
	private byte[] provider = new byte[16];
	
	private int magicNum = 20191016;
	
	private int contentLen;
	
	private static volatile int baseId = BASE;
	
	public static synchronized int generateLogId(){
		
		if (++baseId > 999999999){
			baseId = BASE;
		}
		return baseId;
	}

	public short getId() {
		return id;
	}

	public void setId(short id) {
		this.id = id;
	}

	public short getVersion() {
		return version;
	}

	public void setVersion(short version) {
		this.version = version;
	}

	public int getLogId() {
		return logId;
	}

	public void setLogId(int logId) {
		this.logId = logId;
	}

	public byte[] getProvider() {
		return provider;
	}

	public void setProvider(byte[] provider) {
		this.provider = provider;
	}

	public int getMagicNum() {
		return magicNum;
	}

	public void setMagicNum(int magicNum) {
		this.magicNum = magicNum;
	}

	public int getContentLen() {
		return contentLen;
	}

	public void setContentLen(int contentLen) {
		this.contentLen = contentLen;
	}
	
	
}
