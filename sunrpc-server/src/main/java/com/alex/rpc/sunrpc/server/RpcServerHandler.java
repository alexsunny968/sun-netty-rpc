package com.alex.rpc.sunrpc.server;

import java.util.Map;

import com.alex.rpc.sunrpc.common.bean.RpcRequest;
import com.alex.rpc.sunrpc.common.bean.RpcResponse;
import com.alex.rpc.sunrpc.common.utils.StringUtil;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
	
	
	private final Map<String, Object> handlerMap;
	
    public RpcServerHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
		
	       RpcServer.submit(new Runnable(){
				@Override
				public void run() {
					// 创建并初始化 RPC 响应对象
			        RpcResponse response = new RpcResponse();
			        response.setRequestId(msg.getRequestId());
			        try {
			        	System.out.println("-------server receiver request -------"+msg);
			            Object result = handle(msg);
			            System.out.println("-------server send response -------"+result);
			            response.setResult(result);
			        } catch (Exception e) {
			            
			            response.setException(e);
			        }
			        // 写入 RPC 响应对象并自动关闭连接
			        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);				
				}
	        	
	        });
		
	}
	
	
	
    private Object handle(RpcRequest request) throws Exception {
        // 获取服务对象
        String serviceName = request.getInterfaceName();
        String serviceVersion = request.getServiceVersion();
        if (StringUtil.isNotEmpty(serviceVersion)) {
            serviceName += "-" + serviceVersion;
        }
        Object serviceBean = handlerMap.get(serviceName);
        if (serviceBean == null) {
            throw new RuntimeException(String.format("can not find service bean by key: %s", serviceName));
        }
        // 获取反射调用所需的参数
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();
        // 执行反射调用
//        Method method = serviceClass.getMethod(methodName, parameterTypes);
//        method.setAccessible(true);
//        return method.invoke(serviceBean, parameters);
        // 使用 CGLib 执行反射调用
        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
        return serviceFastMethod.invoke(serviceBean, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //LOGGER.error("server caught exception", cause);
    	
        ctx.close();
    }

}
