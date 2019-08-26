package net.chen.cloudatlas.crow.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.chen.cloudatlas.crow.remote.RemoteException;
import net.chen.cloudatlas.crow.remote.Request;
import net.chen.cloudatlas.crow.remote.Response;
import net.chen.cloudatlas.crow.rpc.SubInvoker;

/**
 * filter chain for crow_binary
 * @author chenn
 *
 */
public class BinaryFilterChain {

	private List<BinaryFilter> filters = new ArrayList<>();
	
	private int index = 0;
	
	public BinaryFilterChain addLast(Collection<BinaryFilter> filters){
		this.filters.addAll(filters);
		return this;
	}
	
	public BinaryFilterChain addLast(BinaryFilter filter){
		this.filters.add(filter);
		return this;
	}
	
	public Response doFilter(SubInvoker subInvoker, Request request, BinaryFilterChain chain) throws RemoteException{
		
		if (index < filters.size()){
			return filters.get(index++).doFilter(subInvoker, request, chain);
		}
		return null;
	}
}
