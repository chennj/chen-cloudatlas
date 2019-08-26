package net.chen.cloudatlas.crow.filter;

import net.chen.cloudatlas.crow.common.NameableService;
import net.chen.cloudatlas.crow.remote.RemoteException;
import net.chen.cloudatlas.crow.remote.Request;
import net.chen.cloudatlas.crow.remote.Response;
import net.chen.cloudatlas.crow.rpc.SubInvoker;

/**
 * filter for crow_binary
 * @author chenn
 *
 */
public interface BinaryFilter extends NameableService{

	Response doFilter(SubInvoker subInvoker, Request request, BinaryFilterChain chain) throws RemoteException;
}
