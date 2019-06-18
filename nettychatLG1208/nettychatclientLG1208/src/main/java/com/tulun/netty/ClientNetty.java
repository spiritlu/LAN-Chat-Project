package com.tulun.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * desc:Netty客户端主类
 * @user:gongdezhe
 * @date:2019/5/12
 */

public class ClientNetty {
    public static void start() {
        //通过读取配置来获取host和port信息
        InputStream resource = ClientNetty.class.getClassLoader().getResourceAsStream("client.properties");
        Properties properties = new Properties();

        //事件循环组
        NioEventLoopGroup loopGroup = null;
        try {
            properties.load(resource);

            String host = properties.getProperty("host");
            Integer port = Integer.valueOf(properties.getProperty("port"));

            loopGroup = new NioEventLoopGroup(1);

            //启动辅助类
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(loopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();

                            pipeline.addLast(new ClientRecvHandler());
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                        }
                    });

            //和服务端同步建立连接
            Channel channel = bootstrap.connect(host, port).sync().channel();
            System.out.println("客户端连接上服务器了");

            //和服务端进行通信
            ClientSendHandler sendHandler = new ClientSendHandler(channel);
            sendHandler.send();
//            ClientSendHandler.send(channel);

            //关闭通道
            channel.closeFuture().sync();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (loopGroup != null) {
                loopGroup.shutdownGracefully();
            }
        }
    }
}
