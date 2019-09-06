package net.chen.cloudatlas.crow.config;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.util.StringUtils;

import net.chen.cloudatlas.crow.common.exception.MethodNotImplException;

/**
 * <b><font color=red>
 * 有待完成
 * </font></b>
 * @author chenn
 *
 */
public class ConfigUtil {

	public static final String FILE_FOLDER = "appCfg";

	public static Set<URL> getAllFilesFromClasspath(String property) {
		
		throw new MethodNotImplException();
	}

	/**
	 * 仅仅获取非rpc的reference列表
	 * @param referenceConfigs
	 * @return
	 */
	public static List<net.chen.cloudatlas.crow.common.URL> getBinaryReferenceUrls(
			List<ReferenceConfig> referenceConfigs) {
		
		List<net.chen.cloudatlas.crow.common.URL> result = new ArrayList<>();
		for (ReferenceConfig one : referenceConfigs){
			if (StringUtils.isEmpty(one.getInterfaceClass())){
				result.addAll(one.getURLs());
			}
		}
		return result;
	}
	
	
}
