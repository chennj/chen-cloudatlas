package org.chen.cloudatlas.crow.remote.impl;

import org.chen.cloudatlas.crow.common.ApiRouting;

import com.alibaba.fastjson.JSON;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;

public class DefaultHttpHandler implements HttpHandler{

	private FormParserFactory formParserFactory = FormParserFactory.builder().build();
	
	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		
		String requestPath 		= exchange.getRequestPath();
		FormDataParser parser 	= formParserFactory.createParser(exchange);
		FormData formData 		= parser.parseBlocking();
		
		String[] localParameters 	= ApiRouting.getParameters(requestPath);
		Object[] invokeParameters 	= new Object[localParameters.length];
		
		for (int i=0; i < localParameters.length; i++){
			
			String parameter = localParameters[i];
			String value = formData.get(parameter).getFirst().getValue();
			invokeParameters[i] = JSON.parse(value);
		}
		
		Object result = ApiRouting.getApi(requestPath).invoke(invokeParameters);
	}

}
