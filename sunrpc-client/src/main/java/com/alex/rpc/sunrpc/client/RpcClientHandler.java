package com.alex.rpc.sunrpc.client;

import com.alex.rpc.sunrpc.common.bean.RpcResponse;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse>{
	
	
	private RpcResponse response;
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
		
		this.response = msg;
	}
	
	public RpcResponse getResponse(){
		
		return this.response;
	}

}
