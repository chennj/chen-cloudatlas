package net.chen.cloudatlas.crow.bootstrap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.Set;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.client.NoopChannelListener;
import net.chen.cloudatlas.crow.client.ServiceRegistry;
import net.chen.cloudatlas.crow.common.Constants;
import net.chen.cloudatlas.crow.common.Protocols;
import net.chen.cloudatlas.crow.common.Version;
import net.chen.cloudatlas.crow.common.exception.ConfigInvalidException;
import net.chen.cloudatlas.crow.common.thread.SingletonTimer;
import net.chen.cloudatlas.crow.common.utils.DuplicatChecker;
import net.chen.cloudatlas.crow.common.utils.NameableServiceLoader;
import net.chen.cloudatlas.crow.config.ConfigUtil;
import net.chen.cloudatlas.crow.config.CrowClientContext;
import net.chen.cloudatlas.crow.config.CrowConfig;
import net.chen.cloudatlas.crow.config.CrowConfigParser;
import net.chen.cloudatlas.crow.config.RegistryConfig;
import net.chen.cloudatlas.crow.manager.api.RegistryClient;
import net.chen.cloudatlas.crow.manager.api.RegistryManager;
import net.chen.cloudatlas.crow.monitor.api.MonitorFactory;
import net.chen.cloudatlas.crow.remote.ChannelListener;
import net.chen.cloudatlas.crow.server.AbstractServerPayloadListener;

/**
 * 
 * @author chenn
 *
 */
public class Bootstrap implements Bootable{
	
	private ClientSideBooter clientSideBooter;
	
	private ServerSideBooter serverSideBooter;
	
	protected CrowConfig config;
	
	private ChannelListener clientListener;
	
	private AbstractServerPayloadListener serverListener;
	
	private RegistryClient registryClient;

	/**
	 * 是否有zookeeper
	 */
	private boolean hasZk;
	
	private volatile boolean isStarted;
	
	public Bootstrap(){
		
	}
	
	public Bootstrap(AbstractServerPayloadListener serverListener){
		this.serverListener = serverListener;
	}
	
	/**
	 * 启动入口
	 */
	public synchronized void start() {
		
		if (isStarted){
			Logger.warn("'start' method should NOT be invoked twice!");
		}
		
		/**
		 * 检查是否有多个版本存在
		 */
		checkVersion();
		Logger.info(Version.getPrettyString());
		
		/**
		 * 将crow.properties的属性放入System.properties中
		 */
		initSystemProperties();
		
		/**
		 * 从System.properties中取出xml文件的路径，并组装为CrowConfig
		 */
		parseConfigFile();
		
		/**
		 * 将需要引用的服务放入缓存
		 */
		ServiceRegistry.init(config);
		
		/**
		 * 启动注册中心，并作为client与之连接
		 */
		startRegistry(config, true);
		
		/**
		 * 将本机作为一个服务提供者启动
		 */
		startAsServer(config);
		
		/**
		 * 添加关闭钩子
		 */
		addShutdownHook();
		
		isStarted = true;
		
	}

	private void checkVersion() {
		
		Logger.debug("Checking duplicate magpie version ...");
		
		if (!DuplicatChecker.check(Version.class)){
			throw new RuntimeException("multiple crow framework version exist, please checkyou classpath and make sure there's only one.");
		}
	}

	/**
	 * crow再启动前，先加载crow.properties;
	 */
	public void initSystemProperties(){
		
		Set<java.net.URL> urls = ConfigUtil.getAllFilesFromClasspath(
				System.getProperty(
						Constants.CROW_PROPERTIES_FILES_KEY,
						Constants.DEFAULT_CROW_PROPERTIES_FILE_KEY));
		
		Properties properties = System.getProperties();
		
		for (URL url : urls){
			
			Properties prop = new Properties();
			try{
				prop.load(url.openStream());
				properties.putAll(prop);
			} catch (FileNotFoundException e){
				Logger.error("FileNotFoundException", e);
			} catch (IOException e) {
				Logger.error("IOException", e);
			}
		}
	}
	
	protected void parseConfigFile(){
		
		try{
			config = CrowConfigParser.parse();
		} catch (ConfigInvalidException e){
			Logger.error(e);
			throw new RuntimeException("parseConfig error",e);
		}
	}
	
	/**
	 * 开始与registry建立连接
	 * @param config
	 * @param connectOnNodeCreated 在新provider注册后，是否立即去连接它
	 * @return
	 * @throws Exception 
	 */
	public RegistryClient startRegistry(final CrowConfig config, final boolean connectOnNodeCreated) {
		
		final RegistryConfig registryConfig = config.getRegistryConfig();
		this.hasZk = 
				registryConfig != null &&
				registryConfig.getAddress() !=null &&
				!registryConfig.getAddress().trim().isEmpty();
		
		startAsClient(config);
		
		if (this.hasZk){
			
			RegistryManager registryManager = NameableServiceLoader.getService(RegistryManager.class, registryConfig.getType());
			registryClient = registryManager.getRegistry(registryConfig.toURL());
			
			Logger.info("connecting to registry ..");
			
			registryClient.start(
					new CrowZkConnectionStateListener(config, registryClient, clientSideBooter, this, connectOnNodeCreated));
		}
		
		return registryClient;
	}
	
	public void initClientBooter(CrowConfig config){
		
		ChannelListener listener = clientListener == null ? new NoopChannelListener() : clientListener;
		clientSideBooter = new ClientSideBooter(config,listener);
		clientSideBooter.init();
	}

	public void startAsClient(CrowConfig config) {
		
		if (!ServiceRegistry.isInitialized()){
			ServiceRegistry.init(config);
		}
		
		if (!this.hasZk){
			ServiceRegistry.countDownAll();
		}
		
		// 缓存context
		CrowClientContext.init(config);
		
		ChannelListener listener = clientListener == null ? new NoopChannelListener() : clientListener;
		clientSideBooter = new ClientSideBooter(config, listener);
		clientSideBooter.start();
	}


	public void startAsServer(CrowConfig config) {
		
		serverSideBooter = new ServerSideBooter(config, serverListener);
	}
	
	private void addShutdownHook() {
		
		// 'kill pid' 将触发shutdown，优雅的的关闭，'kill -9 pid'则不会。
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable(){

			@Override
			public void run() {
				Logger.info("addShutdownHook called, shutDown now.");
				shutDown();
			}
			
		}, "crowShutdownHookThread"));
	}

	public void shutDown() {
		
		if (!isStarted){
			Logger.warn("'shutdown' method should not be invoked twice!");
			return;
		}
		
		if (serverSideBooter != null){
			serverSideBooter.shutDown();
		}
		
		if (clientSideBooter != null){
			clientSideBooter.shutDown();
		}
		
		if (registryClient != null){
			registryClient.shutdown();
		}
		
		SingletonTimer.getTimer().stop();
		
		MonitorFactory factory = NameableServiceLoader.getService(MonitorFactory.class, Protocols.CROW_RPC);
		factory.stopMonitor();
		isStarted = false;
	}

	public void setClientListener(ChannelListener clientListener) {
		this.clientListener = clientListener;
	}

	public void setServerListener(AbstractServerPayloadListener serverListener) {
		this.serverListener = serverListener;
	}

}
