package org.chen.cloudatlas.crow.common.utils;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.chen.cloudatlas.crow.common.NameableService;
import org.chen.cloudatlas.crow.common.annotation.Activate;

/**
 * 服务加载工具，能够通过名称（name）加载不同的SPI（Service Provider Interface）服务
 * <ul>
 * <li>在类路径中为每个ServiceLoader实例中的指定接口加载所有实现</li>
 * <li>ServiceLoader static loader map cache ServiceLoader instance by class type</li>
 * <li>ServiceLoader instance cache all impls in map using keys by calling getName()</li>
 * <li>when getService(serviceName) called,corresponding cached impl instance will be returned</li>
 * </ul>
 * 
 * <b><font color=red>
 * 有待完成
 * </font></b>
 * 
 * @author chenn
 *
 * @param <T>
 */
public class NameableServiceLoader<T extends NameableService> {

	/**
	 * 按不同的class类型缓存ServiceLoader
	 */
	private static final ConcurrentMap<Class<?>, NameableServiceLoader<?>> 
		loaders = new ConcurrentHashMap<>();
	
	private ConcurrentHashMap<String, T> services;
	
	public NameableServiceLoader(Class<T> type) {
		
		this.services = new ConcurrentHashMap<>();
		ServiceLoader<T> loader = ServiceLoader.load(type);
		Iterator<T> svcs = loader.iterator();
		
		while(svcs.hasNext()){
			T svc = svcs.next();
			this.services.putIfAbsent(svc.getName(), svc);
		}
	}

	/**
	 * 按Service实现名来获取Service实现实例
	 * @param serviceName
	 * @return
	 */
	public T getService(String serviceName){
		return this.services.get(serviceName);
	}
	
	public Map<String, T> getServices(){
		return services;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends NameableService> NameableServiceLoader<T> getLoader(final Class<T> type){
		
		if (null == type){
			throw new IllegalArgumentException("service type == null");
		}
		
		if (!type.isInterface() && !Modifier.isAbstract(type.getModifiers())){
			throw new IllegalArgumentException("service type(" + type + ") is not interface or abstract class!");
		}
		
		NameableServiceLoader<T> loader = (NameableServiceLoader<T>) loaders.get(type);
		
		if (null == loader){
			// 并发安全考虑，双重检测
			synchronized(NameableServiceLoader.class){
				if (null == loaders.get(type)){
					loaders.putIfAbsent(type, new NameableServiceLoader<T>(type));
				}
			}
			loader = (NameableServiceLoader<T>) loaders.get(type);
		}
		
		return loader;
	}
	
	public List<T> getActiveServices(String side){
		
		List<T> result = new ArrayList<T>();
		
		Collection<T> values = services.values();
		Iterator<T> iterator = values.iterator();
		
		while(iterator.hasNext()){
			
			T t = iterator.next();
			Activate annotation = t.getClass().getAnnotation(Activate.class);
			String[] sides = annotation.side();
			
			for (String one : sides){
				if (side.equals(one)){
					result.add(t);
				}
			}
		}
		
		/**
		 * sort with {@code org.chen.cloudatlas.crow.common.annotation.Activate#order}
		 */
		Collections.sort(result, new Comparator<Object>(){

			@Override
			public int compare(Object o1, Object o2) {

				Activate annotation1 = o1.getClass().getAnnotation(Activate.class);
				int order1 = annotation1.order();
				
				Activate annotation2 = o2.getClass().getAnnotation(Activate.class);
				int order2 = annotation2.order();
				
				return order1 - order2;
			}
			
		});
		
		return result;
	}

	/**
	 * 获取服务新的实例<br>
	 * 
	 * @param type 接口或者虚拟类
	 * @param serviceName 服务ID
	 * @return
	 */
	public static <T extends NameableService> T getService(final Class<T> service, String serviceName) {
		
		// 获取service新的加载器
		ServiceLoader<T> loader = ServiceLoader.load(service);
		
		// 获取加载器的迭代器
		Iterator<T> it = loader.iterator();
		
		// 查找serviceName的服务
		while (it.hasNext()){
			 
			T t = it.next();
			
			if (serviceName.equals(t.getName())){
				return t;
			}
		}
		
		return null;
	}
}
