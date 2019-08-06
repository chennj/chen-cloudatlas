package org.chen.cloudatlas.crow.rpc;

/**
 * 
 * @author chenn
 *
 */
public interface Result {

	Object getValue();
	
	Throwable getException();
	
	boolean hasException();
	
	/**
	 * 在返回结果前插入一些处理<br>
	 * @return 最终结果
	 * @throws Throwable
	 */
	Object recreate() throws Throwable;
}
