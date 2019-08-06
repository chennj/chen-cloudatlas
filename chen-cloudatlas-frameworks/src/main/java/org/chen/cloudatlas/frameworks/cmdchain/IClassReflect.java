package org.chen.cloudatlas.frameworks.cmdchain;

import java.util.Set;

public interface IClassReflect {

	public Set<Class<? extends AbstractCommandHandler>> getSonClass(Class<? extends AbstractCommandHandler> fatherClass);
}
