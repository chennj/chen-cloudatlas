package net.chen.cloudatlas.crow.cluster;

import java.util.List;

import net.chen.cloudatlas.crow.rpc.Invoker;

public interface Loadbalancer {

	@SuppressWarnings("rawtypes")
	Invoker select(List invokerList);
}
