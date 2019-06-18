package com.tulun.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * desc:Netty框架主类
 * @user:gongdezhe
 * @date:2019/5/12
 */

public class NettyServer {
    public static void start() {
        //通过读取配置形式获取端口信息
        InputStream resource = NettyServer.class.getClassLoader().getResourceAsStream("server.properties");
        Properties properties = new Properties();
        NioEventLoopGroup boss = null;
        NioEventLoopGroup worker = null;
        try {
            properties.load(resource);

            Integer port = Integer.valueOf(properties.getProperty("port"));

            //实例化主事件循环组，负责接收客户端连接
            boss = new NioEventLoopGroup(1);

            //实例化工作线程组，负责处理具体业务逻辑
            worker = new NioEventLoopGroup(5);

            //服务端启动辅助类
            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap
                    .group(boss,worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new ServerHandler());
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                        }
                    });

            //同步启动服务端
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            System.out.println("服务端："+port+" 启动了");

            //关闭服务端
//            channelFuture.sync().channel().close();
            channelFuture.channel().closeFuture().sync();
            System.out.println("执行结束。。。。");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //关闭事件循环组
            if (boss != null) {
                boss.shutdownGracefully();
            }
            if (worker != null) {
                worker.shutdownGracefully();
            }
        }
    }
}
