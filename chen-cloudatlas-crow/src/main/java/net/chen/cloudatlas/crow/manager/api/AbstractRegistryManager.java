package net.chen.cloudatlas.crow.manager.api;

import net.chen.cloudatlas.crow.manager.api.impl.DefaultRegistryLocalStore;

public abstract class AbstractRegistryManager implements RegistryManager{

	private static DefaultRegistryLocalStore store = new DefaultRegistryLocalStore();
	
	@Override
	public RegistryLocalStore getLocalStore(){
		return store;
	}
}
