package com.tulun.netty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tulun.constant.EnMsgType;
import com.tulun.service.FileRecvService;
import com.tulun.util.JsonUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;
import java.util.concurrent.SynchronousQueue;

public class ClientRecvHandler extends SimpleChannelInboundHandler<String> {

    //服务端返回数据在工作线程中，发送数据在主线程中，利用该队列在父子线程之间通信，
    public static SynchronousQueue<Integer> squeue = new SynchronousQueue <>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf msg1 = (ByteBuf) msg;
        byte[] bytes = new byte[msg1.readableBytes()];
        msg1.readBytes(bytes);
        String info = new String(bytes);
        ObjectNode objectNode = JsonUtils.getObjectNode(info);
        String msgtype = objectNode.get("msgtype").asText();

        if (String.valueOf(EnMsgType.EN_MSG_ACK).equals(msgtype)) {
            String srctype = objectNode.get("srctype").asText();

            if (String.valueOf(EnMsgType.EN_MSG_LOGIN).equals(srctype)) {
                //返回登录的消息
                int code = objectNode.get("code").asInt();
                //将返回状态码发给父线程处理
                squeue.put(code);
            } else if (String.valueOf(EnMsgType.EN_MSG_REGISTER).equals(srctype)) {
                //返回注册的消息
                int code = objectNode.get("code").asInt();
                ////将返回状态码发给父线程处理
                squeue.put(code);
            } else if (String.valueOf(EnMsgType.EN_MSG_MODIFY_PWD).equals(srctype)) {
                //返回修改密码的消息
                int code = objectNode.get("code").asInt();
                //将返回状态码发给父线程处理
                squeue.put(code);
            } else if (String.valueOf(EnMsgType.EN_MSG_FORGET_PWD).equals(srctype)) {
                //返回忘记密码的消息
                int code = objectNode.get("code").asInt();
                //将返回状态码发给父线程处理
                squeue.put(code);
            } else if (String.valueOf(EnMsgType.EN_MSG_CHAT).equals(srctype)) {
                //返回一对一聊天的消息
                int code = objectNode.get("code").asInt();
                //将返回状态码发给父线程处理
                if (code == 300) {
                    System.out.println("接收方不存在。。。。。。");
                } else if (code == 400) {
                    System.out.println("接收方不在线，已经成功将消息放入数据库的缓存中");
                } else {
                    System.out.println("恭喜该用户，消息发送成功");
                }
            } else if (String.valueOf(EnMsgType.EN_MSG_OFFLINE).equals(srctype)) {
                //返回注册的消息
                int code = objectNode.get("code").asInt();
                //将返回状态码发给父线程处理
                squeue.put(code);
            } else if (String.valueOf(EnMsgType.EN_MSG_GET_ALL_USERS).equals(srctype)) {
                //返回的消息
                int code = objectNode.get("code").asInt();
                if (200 == code) {
                    //返回成功
                    System.out.println("获取成功!");
                    String msg2 = objectNode.get("users").asText();
                    System.out.println("在线用户:" + msg2);
                } else {
                    //返回失败
                    System.out.println("信息有误，获取失败!");
                }
            }else if (String.valueOf(EnMsgType.EN_MSG_TRANSFER_FILE).equals(srctype)) {
                //服务端给发送方返回的消息   发送邮件
                int code = objectNode.get("code").asInt();
                if (code == 200) {
                    //接收方在线
                    int port = objectNode.get("port").asInt();
                    squeue.put(port);
                } else {
                    //接收端不在线  300 ？？？？
                    System.out.println("接收方不在线");

                    int port = objectNode.get("port").asInt();

                    squeue.put(port);
                }
            }
        }else if(String.valueOf(EnMsgType.EN_MSG_CHAT).equals(msgtype)) {
                //接收方接收到单聊消息
                String msg2 = objectNode.get("msg").asText();
                String fromName = objectNode.get("fromName").asText();
                System.out.println(fromName + "发送消息为：" + msg2);
        }else if(String.valueOf(EnMsgType.EN_MSG_TRANSFER_FILE).equals(msgtype)){
            //服务端给接收方发送消息
            int port=objectNode.get("port").asInt();
            System.out.println("发送文件操作，服务端推送接口："+port);
            new FileRecvService(port).start();
        } else if(String.valueOf(EnMsgType.EN_MSG_OFFLINE_MSG_EXIST).equals(msgtype)){
            //留言信息的获取
            String message=objectNode.get("msg").asText();
            String fromName=objectNode.get("fromName").asText();
            System.out.println(fromName+"给你留言："+message);
        } else if(String.valueOf(EnMsgType.EN_MSG_OFFLINE_FILE_EXIST).equals(msgtype)){
            //离线邮件的获取
            int port=objectNode.get("port").asInt();
            System.out.println("发送离线文件操作，服务端推送离线文件的接口为："+port);
            new FileRecvService(port).start();
        }
    }
}

