package com.alex.rpc.sunrpc.server;



import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.alex.rpc.sunrpc.common.bean.RpcRequest;
import com.alex.rpc.sunrpc.common.bean.RpcResponse;
import com.alex.rpc.sunrpc.common.codec.RpcDecoder;
import com.alex.rpc.sunrpc.common.codec.RpcEncoder;
import com.alex.rpc.sunrpc.common.utils.StringUtil;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

@Component
public class RpcServer implements ApplicationContextAware, InitializingBean {
	
	//private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    private String serviceAddress;
    /**
     * 存放 服务名 与 服务对象 之间的映射关系
     */
    private Map<String, Object> handlerMap = new HashMap<String, Object>();
    

    private static ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(16);

    
    public RpcServer(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }
    
	
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {

		Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcService.class);
	    if (MapUtils.isNotEmpty(serviceBeanMap)) {
	            for (Object serviceBean : serviceBeanMap.values()) {
	                RpcService rpcService = serviceBean.getClass().getAnnotation(RpcService.class);
	                String serviceName = rpcService.value().getName();
	                String serviceVersion = rpcService.version();
	                if (StringUtil.isNotEmpty(serviceVersion)) {
	                    serviceName += "-" + serviceVersion;
	                }
	                handlerMap.put(serviceName, serviceBean);
	                
	                System.out.println("Server handlerMap "+ handlerMap);
	            }
	   }
		
	}
	
	public void afterPropertiesSet() throws Exception {

		EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // 创建并初始化 Netty 服务端 Bootstrap 对象
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel channel) throws Exception {
                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addLast(new RpcDecoder(RpcRequest.class)); // 解码 RPC 请求
                    pipeline.addLast(new RpcEncoder(RpcResponse.class)); // 编码 RPC 响应
                    pipeline.addLast(new RpcServerHandler(handlerMap)); // 处理 RPC 请求
                    
                }
            });
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            // 获取 RPC 服务器的 IP 地址与端口号
            String[] addressArray = StringUtil.split(serviceAddress, ":");
            String ip = addressArray[0];
            int port = Integer.parseInt(addressArray[1]);
            // 启动 RPC 服务器
            ChannelFuture future = bootstrap.bind(ip, port).sync();
            // 注册 RPC 服务地址
            /*
            if (serviceRegistry != null) {
                for (String interfaceName : handlerMap.keySet()) {
                    serviceRegistry.register(interfaceName, serviceAddress);
                    LOGGER.debug("register service: {} => {}", interfaceName, serviceAddress);
                }
            }*/
            System.out.printf("real server started on port {}", port);
            // 关闭 RPC 服务器
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
        
	}
	
	
	 /**
     * 提交任务 
     */
    public static void submit(Runnable task){
    	
        threadPoolExecutor.submit(task);
    }

}
