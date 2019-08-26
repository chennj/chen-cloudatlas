package net.chen.cloudatlas.crow.remote.support.bthead;

import net.chen.cloudatlas.crow.remote.Message;

/**
 * bthead 协议的报文头对象<br>
 * <p>
 * <pre>
 * 报文分组ID：id_ 保留
 * 报文类型：version_
 * 整体报文跟踪ID：log_id_
 * 报文发送方：provider_[16]
 * MAGIC NUM：magic_num
 * 报文主体长度：content_len_
 * </pre>
 * </p>
 * @author chenn
 *
 */
public abstract class BtheadHeader implements Message{

	public static final String ID = "id";
	public static final String VERSION = "version";
	public static final String LOGID = "log_id";
	public static final String PROVIDER = "provider";
	public static final String MAGICNUM = "magic_num";
	public static final String BODYLEN = "body_len";
	
	public static final int PROVIDER_LENGTH = 16;
	/**
	 * id+version+logId+provider+magicNum+contentLen = 2+2+4+16+4+4 = 32
	 */
	public static final int BTHEAD_LENGTH = 32;	
	public static final int BASE = 100000000;
	public static final int SEED = 899999999;
	
	private short id = 0;
	private short version = 1;
	private int logId = generateLogId(); //bthead服务端会判断id范围：100000000-999999999
	private byte[] provider = new byte[PROVIDER_LENGTH];
	private int magicNum = 19810313;
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
