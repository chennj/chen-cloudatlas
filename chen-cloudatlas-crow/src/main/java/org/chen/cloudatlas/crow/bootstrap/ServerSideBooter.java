package org.chen.cloudatlas.crow.bootstrap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chen.cloudatlas.crow.common.Constants;
import org.chen.cloudatlas.crow.common.DcType;
import org.chen.cloudatlas.crow.common.SerializationType;
import org.chen.cloudatlas.crow.common.SpringContextUtil;
import org.chen.cloudatlas.crow.common.URL;
import org.chen.cloudatlas.crow.config.CrowConfig;
import org.chen.cloudatlas.crow.config.CrowServerContext;
import org.chen.cloudatlas.crow.config.MonitorConfig;
import org.chen.cloudatlas.crow.config.ProtocolConfig;
import org.chen.cloudatlas.crow.config.ServiceConfig;
import org.chen.cloudatlas.crow.remote.RemoteException;
import org.chen.cloudatlas.crow.remote.Server;
import org.chen.cloudatlas.crow.remote.impl.ExchangeHttpServer;
import org.chen.cloudatlas.crow.remote.impl.NettyServer;
import org.chen.cloudatlas.crow.remote.impl.UndertowServer;
import org.chen.cloudatlas.crow.server.PayloadListener;
import org.tinylog.Logger;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author chenn
 *
 */
public class ServerSideBooter implements CrowBootable{

	private CrowConfig config;
	
	private PayloadListener serverPayloadListener;
	
	public ServerSideBooter(CrowConfig config, PayloadListener serverPayloadListener){
		this.config = config;
		this.serverPayloadListener = serverPayloadListener;
	}
	
	public void start() {
		
		bind();
	}

	private void bind() {
		
		if (config.getServiceConfigList().size() > 0){
			// 有service才有bind
			// cache context
			CrowServerContext.init(config);
			List<ProtocolConfig> protocols = config.getProtocolConfigList();
			// 如果server side暴露多个protocol，并且ip和port不同，则启动多个crow server
			for (ProtocolConfig one : protocols){
				
				Logger.info("Protocol:{}, SerializationType:{}",one.getCodec(),one.getSerializationType().getText());
				if (SerializationType.BINARY.equals(one.getSerializationType().getText())){
					// byte[] 形式
					// 对于clientSide来说，IP与port可以不填，所以这里排除掉clientSide
					if (one.getIp() != null && one.getPort() != 0){
						// 只有暴露了服务的才真正bind
						URL url = getURL(one);
						// 要么在ServerSideBooter中传入serverPayloadListener,
						// 要么在<protocol>中手工指定listener
						if (serverPayloadListener != null && !StringUtils.isEmpty(one.getListener())){
							
							throw new RuntimeException("you can not set serverPayloadListener in ServerSideBooter and listener in "
									+ " <protocol> simultaneously, choose one.");
						}
						// 如果是server，则serverPayloadHandler必须不为null
						if (serverPayloadListener == null && StringUtils.isEmpty(one.getListener())){
							
							throw new RuntimeException("serverListener must be set, either set listener in <protocol>"
									+ " or set serverPayloadListener in ServerSideBooter");
						}
						
						PayloadListener listener = null;
						if (!StringUtils.isEmpty(one.getListener())){
							
							if (config.getApplicationConfig().getSpringFeature()){
								
								try {
									listener = (PayloadListener) SpringContextUtil.getBean(Class.forName(one.getListener()));
								} catch (Exception e){
									try {
										listener = (PayloadListener) Class.forName(one.getListener()).newInstance();
									} catch (Exception el){
										throw new RuntimeException("error creating instance of PayloadListener.",el);
									}
								}
							} else {
								
								try {
									listener = (PayloadListener) Class.forName(one.getListener()).newInstance();
								} catch (Exception el){
									throw new RuntimeException("error creating instance of PayloadListener.",el);
								}								
							}
						} else {
							
							listener = serverPayloadListener;
						}
						
						one.setListener(listener.getClass().getName());
						one.setListenerImpl(listener);
						
						Server server = new NettyServer(url, listener);
						try {
							server.bind();
						} catch (Exception e){
							Logger.error(e);
						}
					}
				} else {
					// rpc 形式
					Map<String, ServiceConfig> serviceConfigRpcMap = CrowServerContext.getServiceConfigRpcMap();
					for (String key : serviceConfigRpcMap.keySet()){
						
						ServiceConfig sc = serviceConfigRpcMap.get(key);
						export(sc.getInterface(), sc.getServiceVersion(), sc.getApplicationConfig().getDc());
					}
				} // -- end if (serializationType.BINARY
				
				if (one.getHttpPort() > 0){
					new ExchangeHttpServer(new UndertowServer(one.getIp(), one.getHttpPort()), one);
				}
			} // -- end for (ProtocolConfig one
		}
	}

	private <T> void export(Class<T> interfaceClass, String serviceVersion, DcType dc) {
		
		ServiceConfig sConfig 	= CrowServerContext.getServiceConfigByInterface(interfaceClass.getName(), serviceVersion);
		ProtocolConfig pConfig	= sConfig.getProtocol();
		MonitorConfig mConfig	= sConfig.getMonitorConfig();
		
		Map<String, String> parameters = new HashMap<>();
		parameters.put(Constants.HEARTBEAT_INTERVAL, String.valueOf(pConfig.getHeartbeatInterval()));
		parameters.put(Constants.PROTOCOL_VERSION, pConfig.getVersion());
		parameters.put(Constants.PROTOCOL_ID, pConfig.getId());
		parameters.put(Constants.SERIALIZATION_TYPE, pConfig.getSerializationType().getText());
		parameters.put(Constants.COMPRESS_ALGORITHM, pConfig.getCompressAlgorithm().getText());
		parameters.put(Constants.MAX_MSG_SIZE, String.valueOf(pConfig.getMaxMsgSize()));
		parameters.put(Constants.APPLICATION, CrowServerContext.getApplicationName());
		parameters.put(Constants.DC, sConfig.getApplicationConfig().getDc().getText());
		parameters.put(Constants.GROUP, pConfig.getApplicationConfig().getDc().getText());
		parameters.put(Constants.SERVICE_VERSION, serviceVersion);
		
		if (null != mConfig){
			parameters.put(Constants.MONITOR_URLS, mConfig.getUrls());
			parameters.put(Constants.MONITOR_INTERVAL, String.valueOf(pConfig.getMaxThreads()));
		}
		
		parameters.put(Constants.CROW_NETTY_EXECUTOR_SIZE_KEY, String.valueOf(pConfig.getMaxThreads()));
		
		URL url = new URL(pConfig.getCodec(), pConfig.getIp(), pConfig.getPort(), interfaceClass.getName(), parameters);
		ServiceExport result = new ServiceExport<T>(sConfig);
		result.doExport(url);
	}

	/**
	 * 根据ProtocolConfig拼装URL
	 * @param one
	 * @return
	 */
	private URL getURL(ProtocolConfig pConfig) {

		Map<String, String> parameters = new HashMap<String, String>();
		MonitorConfig monitorConfig = pConfig.getMonitorConfig();
		
		parameters.put(Constants.HEARTBEAT_INTERVAL, String.valueOf(pConfig.getHeartbeatInterval()));
		parameters.put(Constants.PROTOCOL_VERSION, pConfig.getVersion());
		parameters.put(Constants.PROTOCOL_ID, pConfig.getId());
		parameters.put(Constants.SERIALIZATION_TYPE, pConfig.getSerializationType().getText());
		parameters.put(Constants.COMPRESS_ALGORITHM, pConfig.getCompressAlgorithm().getText());
		parameters.put(Constants.MAX_MSG_SIZE, String.valueOf(pConfig.getMaxMsgSize()));
		parameters.put(Constants.APPLICATION, CrowServerContext.getApplicationName());
		parameters.put(Constants.GROUP, pConfig.getApplicationConfig().getDc().getText());
		parameters.put(Constants.DC, pConfig.getApplicationConfig().getDc().getText());
		parameters.put(Constants.CROW_NETTY_EXECUTOR_SIZE_KEY, String.valueOf(pConfig.getMaxThreads()));
		
		if (null != monitorConfig){
			parameters.put(Constants.MONITOR_URLS, monitorConfig.getUrls());
			parameters.put(Constants.MONITOR_INTERVAL, String.valueOf(monitorConfig.getMonitorInterval()));
		}
		
		return new URL(pConfig.getCodec(), pConfig.getIp(), pConfig.getPort(), parameters);
	}

	public void shutDown() {
		
		NettyServer.shutDownAll();
	}

}
