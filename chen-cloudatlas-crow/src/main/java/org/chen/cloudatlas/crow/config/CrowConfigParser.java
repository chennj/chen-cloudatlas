package org.chen.cloudatlas.crow.config;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.chen.cloudatlas.crow.common.exception.ConfigInvalidException;
import org.chen.cloudatlas.crow.common.exception.MethodNotImplException;

/**
 * xml parser using jaxb
 * <b><font color=red>
 * 未完成
 * </font></b>
 * @author chenn
 *
 */
public class CrowConfigParser {

	private static List<URL> springConfigStore = new LinkedList<URL>();
	
	private CrowConfigParser(){
		
	}

	public static CrowConfig parse() throws ConfigInvalidException{
		throw new MethodNotImplException();
	}
}
