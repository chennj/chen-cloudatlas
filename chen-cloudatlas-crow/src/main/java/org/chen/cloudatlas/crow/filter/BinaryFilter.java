package org.chen.cloudatlas.crow.filter;

import org.chen.cloudatlas.crow.common.NameableService;
import org.chen.cloudatlas.crow.remote.RemoteException;
import org.chen.cloudatlas.crow.remote.Request;
import org.chen.cloudatlas.crow.remote.Response;
import org.chen.cloudatlas.crow.rpc.SubInvoker;

/**
 * filter for crow_binary
 * @author chenn
 *
 */
public interface BinaryFilter extends NameableService{

	Response doFilter(SubInvoker subInvoker, Request request, BinaryFilterChain chain) throws RemoteException;
}
