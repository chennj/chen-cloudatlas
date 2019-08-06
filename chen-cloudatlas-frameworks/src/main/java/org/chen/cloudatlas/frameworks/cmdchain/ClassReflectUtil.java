package org.chen.cloudatlas.frameworks.cmdchain;

import java.util.Set;

import org.reflections.Reflections;

public class ClassReflectUtil implements IClassReflect{

	private static ClassReflectUtil self = new ClassReflectUtil();
	private ClassReflectUtil(){}
	static ClassReflectUtil instance(){return self;}

	@SuppressWarnings("unchecked")
	@Override
	public Set<Class<? extends AbstractCommandHandler>> getSonClass(Class<? extends AbstractCommandHandler> fatherClass) {
		
		String packageName = fatherClass.getPackage().getName();
		
		Reflections reflections = new Reflections(packageName);
		Set<?> classes = reflections.getSubTypesOf(fatherClass);
		return (Set<Class<? extends AbstractCommandHandler>>) classes;
	}}
