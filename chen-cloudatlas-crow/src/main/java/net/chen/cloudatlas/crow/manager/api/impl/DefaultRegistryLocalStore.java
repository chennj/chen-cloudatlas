package net.chen.cloudatlas.crow.manager.api.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.tinylog.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import net.chen.cloudatlas.crow.manager.api.RegistryData;
import net.chen.cloudatlas.crow.manager.api.RegistryLocalStore;

/**
 * RegistryLocalStore默认实现
 * @author chenn
 *
 */
public class DefaultRegistryLocalStore implements RegistryLocalStore{

	public static final String STORE_ROOT_DIR = System.getProperty("user.home") + File.separator + ".crow";
	public static final String STORE_FILE_NAME = "registry.json";
	
	@Override
	public void save(RegistryData data) throws Exception {
		
		final String dirPath = STORE_ROOT_DIR + File.separator + data.getApplicationName();
		File dir = new File(dirPath);
		
		if (!dir.exists() && !dir.mkdir()){
			final String errMsg = "failed to find or create local store directory:"+dirPath;
			Logger.error(errMsg);
			throw new RuntimeException(errMsg);
		}
		
		final String filePath = dirPath + File.separator + STORE_FILE_NAME;
		Logger.debug("saving RegistryData to {}", filePath);
		
		final String jsonStr = JSON.toJSONString(data, SerializerFeature.PrettyFormat);
		Logger.trace("saving RegistryData={}",jsonStr);
		
		final File file = new File(filePath);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			fos.write(jsonStr.getBytes());
			Logger.info("RegistryData successfully saved to {}",filePath);
		} catch (Exception e){
			Logger.error("error saving RegistryData to {} {}",filePath,e);
			throw e;
		} finally{
			if (null != fos){
				fos.close();
			}
		}
	}

	@Override
	public RegistryData load(String applicationName) throws Exception {
		
		final String filePath = STORE_ROOT_DIR + File.separator + applicationName + File.separator + STORE_FILE_NAME;
		Logger.debug("loading RegistryData from " + filePath);
		File file = new File(filePath);
		
		if (!file.exists()){
			Logger.warn(filePath + " does not exists!");
			return null;
		}
		
		if (!file.canRead()){
			Logger.warn(filePath + " can not read!");
			return null;
		}
		
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			//一次读完
			byte[] buffer = new byte[fis.available()];
			fis.read(buffer);
			String jsonStr = new String(buffer);
			Logger.trace("loading RegistryData={}",jsonStr);
			RegistryData data = JSON.parseObject(jsonStr, RegistryData.class);
			Logger.trace("deserialized " + data.toString());
			return data;
		} catch (Exception e){
			Logger.error("error loading RegistryData from " + filePath,e);
			throw e;
		} finally{
			if (null != fis){
				fis.close();
			}
		}
	}

}
