package net.chen.cloudatlas.crow.common;

import java.util.regex.Pattern;

import net.chen.cloudatlas.crow.common.cluster.FailType;
import net.chen.cloudatlas.crow.common.cluster.LoadBalanceType;

public final class Constants {

	public static final String PROJECT_NAME = "CROW";
	
	public static final String DC = "dc";
	public static final String SERIALIZATION_TYPE = "serializationtype";
	public static final String COMPRESS_ALGORITHM = "compressAlgorithm";
	public static final String FAIL_STRATEGY = "failStrategy";
	public static final String LOAD_BALANCE_STRATEGY = "loadbalanceStrategy";
	public static final String TIMEOUT = "timeout";
	public static final String RETRIES = "retries";
	public static final String WEIGHT = "weight";
	public static final String FORKS_KEY = "forks";
	public static final String HEARTBEAT_INTERVAL = "heartbeatInterval";
	public static final String PROTOCOL_ID = "protocolId";
	public static final String PROTOCOL_VERSION = "protocolVersion";
	public static final String ONE_WAY = "oneway";
	public static final String SERVICE_VERSION = "serviceVersion";
	//优先级，按urls中的顺序来，int值越小，优先级越高
	public static final String PRIORITY = "priority";
	//组，在crow.xml中，没有单独的attribute，是在urls中用竖线分割
	public static final String GROUP = "group";
	public static final String MAX_MSG_SIZE = "maxMsgSize";
	
	/**
	 * registry
	 */
	public static final String ADDRESS = "address";
	public static final String CONNECTION_TIMEOUT_MS = "connectionTimeoutMs";
	public static final String SESSION_TIMEOUT_MS = "sessionTimeoutMs";
	//阻塞等待zookeeper客户端建立好连接的时间
	public static final String REGISTRY_CONNECTION_BLOCK_TIMEOUT = "registryConnectionBlockTimeoutMs";
	
	/**
	 * attachments key
	 */
	public static final String PROTOCOL = "protocol";
	public static final String IP_AND_PORT = "ipAndPort";
	public static final String SERVICE_ID = "serviceId";
	public static final String CALLER_ID = "callerId";
	
	public static final String GBK = "GBK";
	public static final String UNKNOWN = "UNKNOWN";
	public static final String IS_MONITOR = "false";
	
	public static final String TIMESTAMP = "timestamp";
	public static final String MONITOR = "monitor";
	public static final String APPLICATION = "application";
	public static final String INTERFACE = "interface";
	public static final String METHOD = "method";
	public static final String SUCC_COUNT = "succ_count";
	public static final String FAIL_COUNT = "fail_count";
	public static final String TOTAL_RT = "total_rt";
	public static final String PEAK_RT = "peak_rt";
	public static final String LOW_RT = "low_rt";
	public static final String CONCURRENT = "concurrent";
	public static final String MAX_CONCURRENT = "max_concurrent";
	public static final String SIDE = "side";
	public static final String REMOTE_HOST = "remote_host";
	public static final String PROVIDER = "provider";
	public static final String CONSUMER = "consumer";
	public static final String LOCAL_HOST = "local_host";
	public static final String LOCAL_PORT = "local_port";
	public static final String MONITOR_URLS = "monitor_urls";
	public static final String MONITOR_INTERVAL = "monitor_interval";
	
	public static final String IP_PORT_SEPERATOR = ":";
	
	public static final String DEFAULT_SERVICE_VERSION = "1.0";
	
	// regex
	public static final Pattern PATTERN_IP_AND_PORT = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):(\\d{1,5})");
	public static final Pattern PATTERN_EMAIL = Pattern.compile("^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$");
	public static final Pattern PATTERN_PHONE = Pattern.compile("-?[1-9]\\d*");
	public static final Pattern PATTERN_IP = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})");
	public static final Pattern PATTERN_PORT = Pattern.compile("(\\d{1,5})");
	public static final int DEFAULT_MAXWEIGHT = 1000;
	
	/**
	 * message header
	 * 1b=ESC
	 */
	public static final short MAGIC = (short)0x1b1b;
	public static final byte MAGIC_HIGH = (byte)0x1b;
	public static final byte MAGIC_LOW = (byte)0x1b;
	
	/**
	 * 协议
	 */
	public static final String DEFAULT_PROTOCOL = "crow_binary";
	public static final SerializationType DEFAULT_SERIALIZATION_TYPE = SerializationType.HESSIAN2;
	public static final CompressAlgorithmType DEFAULT_COMPRESS_ALGORITHM = CompressAlgorithmType.NONE;
	public static final String DEFAULT_PROTOCOL_VERSION = "1.0.0";
	public static final boolean DEFAULT_USE_RPC = false;
	public static final boolean DEFAULT_ONEWAY = false;
	public static final boolean DEFAULT_COMPRESS = false;
	
	public static final int DEFAULT_HEARTBEAT_INTERVAL = 60000;
	public static final int DEFAULT_FIX_INTERVAL = 5000;
	public static final int DEFAULT_MAX_MSG_SIZE = 4 * 1024 * 1024;
	public static final int DEFAULT_FIX_PROVIDER = 30 * 1000;
	
	/**
	 * log error
	 */
	public static final String ERR_800 = "err:800 CAN NOT connect to ";
	public static final String ERR_801 = "err:801 Service id:{}, version:{} DOES NOT exists!";
	public static final String ERR_802 = "err:802 CAN NOT connect to registry";
	
	/**
	 * crow 系统参数 
	 */
	public static final String CROW_PROPERTIES_FILE_KEY = "crow.properties.file";
	public static final String DEFAULT_CROW_PROPERTIES_FILE_KEY = "crow.properties";
	
	public static final String CROW_XSD_FILE_KEY = "crow.xsd.file";
	public static final String DEFAULT_SCHEMA_NAME = "crow-1_0.xsd";
	
	public static final String CROW_CONFIG_FILE_KEY = "crow.config.file";
	public static final String DEFAULT_CROW_CONFIG_FILE_NAME = "crow.xml";
	
	public static final String WAIT_ALL_URLS_CONNECTED = "waitAllUrlsConnected";
	public static final String DEFAULT_WAIT_ALL_URLS_CONNECTED = "true";
	
	public static final String CROW_NETTY_EXECUTOR_SIZE_KEY = "crow.netty.executor.size";
	public static final int DEFAULT_CROW_NETTY_EXECUTOR_SIZE = Integer.parseInt(
			System.getProperty(CROW_NETTY_EXECUTOR_SIZE_KEY, "100"));
	
	
	public static final String CROW_NETTY_BOSS_COUNT_KEY = "crow.netty.boss.count";
	public static final int DEFAULT_CROW_NETTY_BOSS_COUNT_KEY = Integer.parseInt(
			System.getProperty(CROW_NETTY_BOSS_COUNT_KEY, "1"));
	
	public static final String CROW_NETTY_WORKER_NUM_KEY = "crow.netty.worker.count";
	public static final int DEFAULT_CROW_NETTY_WORKER_NUM_KEY = Integer.parseInt(
			System.getProperty(CROW_NETTY_WORKER_NUM_KEY, "0"));
	
	public static final int DEFAULT_IO_THREADS = Runtime.getRuntime().availableProcessors() * 2;

	public static final int DEFAULT_RETRY_CONNECT_INTERVAL = 
			Integer.parseInt(
					System.getProperty(
							"DEFAULT_RETRY_CONNECT_INTERVAL",
							String.valueOf(1 * 10 * 1000)));
	public static final int DEFAULT_MAX_WAIT_RETRY_CONNECT_INTERVAL = 
			Integer.parseInt(
					System.getProperty(
							"DEFAULT_MAX_WAIT_RETRY_CONNECT_INTERVAL",
							String.valueOf(DEFAULT_RETRY_CONNECT_INTERVAL + 5 * 1000)));
	
	public static final int DEFAULT_NO_RESPONSE_TIMEOUT = 1 * 60 * 1000;
	
	public static final int DEFAULT_CONNECTION_AWAIT_TIMEOUT = 
			Integer.parseInt(
					System.getProperty("DEFAULT_CONNECTION_AWAIT_TIMEOUT", String.valueOf(10 * 1000)));
	
	public static final int DEFAULT_SOCKET_TIMEOUT = 
			Integer.parseInt(
					System.getProperty("DEFAULT_SOCKET_TIMEOUT",String.valueOf(5 * 1000)));

	public static final String RETURN_TYPE = "returnType";

	public static final int DEFAULT_REGISTRY_SESSION_TIMEOUT_MS = 1 * 30 * 1000;

	public static final int DEFAULT_REGISTRY_SLEEP_MS_BETWEEN_RETRIES = 
			Integer.parseInt(System.getProperty("DEFAULT_REGISTRY_SLEEP_MS_BETWEEN_RETRIES",String.valueOf("3 * 1000")));

	public static final int DEFAULT_REGISTRY_CONNECTION_TIMEOUT_MS = 10 * 1000;

	public static final int REGISTRY_CONNECTION_BLOCK = 10 * 1000;
	
	public static final LoadBalanceType DEFAULT_LOADBALANCER_TYPE = LoadBalanceType.RANDOM;

	public static final int DEFAULT_WEIGHT = 0;
	public static final int DEFAULT_PRIORITY = 0;
	public static final int DEFAULT_GROUP = 1;
	public static final String DEFAULT_DC = "sh";

	public static final int DEFAULT_RETRIES = 2;

	public static final int DEFAULT_FORKS = 2;

	public static final int REGISTRY_CONNECTION_BLOCK_TIME = 10*1000;
	// RPC GETsERVICE时阻塞，直到zk connected或连接不上
	public static final long REGISTRY_CONNECTION_LATCH_TIMEOUT = REGISTRY_CONNECTION_BLOCK_TIME + 2*1000;

	public static final String SPRINGAUTOSTART_KEY = "springAutoStart";
	public static final boolean SPRINGAUTOSTART = Boolean.parseBoolean(System.getProperty(SPRINGAUTOSTART_KEY,"true"));

	public static long DEFAULT_MONITOR_INTERVAL = 1 * 60 * 1000; //1分钟

	public static String COMMA_SEPARATOR = ",";

	public static FailType DEFAULT_FAIL_TYPE = FailType.FAIL_OVER;

	public static String GROUP_SEPARATOR = "\\|";
}
