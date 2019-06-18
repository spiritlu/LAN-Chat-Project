package com.tulun.netty;

import com.tulun.controller.DispatcherController;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ServerHandler extends SimpleChannelInboundHandler<String> {
        //创建分发实例
      private  DispatcherController controller = DispatcherController.getInstance();


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {

    }

    /**
     * desc:用户端发送的消息接收方法
     * @user:gongdezhe
     * @date:2019/5/12
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf msg1 = (ByteBuf) msg;
        byte[] bytes = new byte[msg1.readableBytes()];
        msg1.readBytes(bytes);
        String info = new String(bytes);

        //不做具体业务逻辑处理，主要交给分发实例处理
        String recvMsg = controller.process(ctx, info);

//        System.out.println(ctx.channel().remoteAddress()+"[recv] :"+info);
        if (!"".equals(recvMsg)) {
        ctx.channel().writeAndFlush(recvMsg);
//            System.out.println(ctx.channel().remoteAddress()+"[send] :"+recvMsg);
        }
    }

    /**
     * desc:有用户连接时触发的方法：用户上线
     * @user:gongdezhe
     * @date:2019/5/12
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress()+" 上线了");

    }

    /**
     * desc:用户断开连接时触发的方法：用户下线
     * @user:gongdezhe
     * @date:2019/5/12
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //用户下线删除缓存

        DispatcherController.removeCache(ctx);

        System.out.println(ctx.channel().remoteAddress()+" 下线了");
    }
}
