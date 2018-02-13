package com.alex.rpc.sunrpc.sample.server;

import com.alex.rpc.sunrpc.sample.api.HelloService;
import com.alex.rpc.sunrpc.server.RpcService;

@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService{

	
	 @Override
	    public String hello(String name) {
	        return "Hello! " + name;
	    }
}
