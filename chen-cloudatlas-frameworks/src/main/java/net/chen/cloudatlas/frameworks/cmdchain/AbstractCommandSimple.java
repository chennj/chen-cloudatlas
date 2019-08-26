package net.chen.cloudatlas.frameworks.cmdchain;

public abstract class AbstractCommandSimple extends AbstractCommand{

	@Override
	public IClassReflect initReflectUtil() {

		return ClassReflectUtil.instance();
	}

}
