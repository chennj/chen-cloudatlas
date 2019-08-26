package net.chen.cloudatlas.crow.common.utils;

import net.chen.cloudatlas.crow.common.exception.MethodNotImplException;

/**
 * 属性{xx=xx}工具
 * <p><font color=red>
 * 待完成
 * </font></p>
 * @author chenn
 *
 */
public class StringPropertyReplacer {

	public static String replaceProperties(String str) {
		return replaceProperties(str, null);
	}

	public static String replaceProperties(String str, Object object) {
		throw new MethodNotImplException(StringPropertyReplacer.class);
	}

}
