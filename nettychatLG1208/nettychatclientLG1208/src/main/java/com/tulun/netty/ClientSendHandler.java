package com.tulun.netty;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tulun.constant.Constant;
import com.tulun.constant.EnMsgType;
import com.tulun.service.FileSendService;
import com.tulun.util.JsonUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.io.File;
import java.util.Scanner;

/**
 * desc:客户端发送消息类
 * @user:gongdezhe
 * @date:2019/5/12
 */

public class ClientSendHandler {
    //该属性接收键盘输入信息
    private static Scanner in = new Scanner(System.in);

    private Channel channel = null;

    //登录用户名
    private String loginName=null;

    public ClientSendHandler(Channel channel) {
        this.channel = channel;
    }



    public void send() {
        //channel
        channel = channel;

//        //接收键盘输入的数据
//        in.useDelimiter("\n");

        while (true) {
            showLoginMemu();
            System.out.println("请选择");
            int num = in.nextInt();
            in.nextLine();
            switch (num) {
                case 1:
                    //登录操作
                    doLogin();
                    break;
                case 4:
                    System.exit(0);
                case 2:
                    //注册操作
                    register();
                    break;
                case 3:
                    //忘记密码
                    forgetPwd();
                    break;
                default:
                    System.out.println("输入有误");
                    break;
            }
        }
    }

    /**
     * 用户登录显示页面
     */
    private void showLoginMemu() {
        System.out.println("======================");
        System.out.println("1.登录");
        System.out.println("2.注册");
        System.out.println("3.忘记密码");
        System.out.println("4.退出系统");
        System.out.println("======================");
    }

    /**
     * 登录操作
     */
    private void doLogin() {
        System.out.println("请输入账号：");
        String name = in.nextLine();
        System.out.println("请输入密码:");
        String passwd = in.nextLine();

        //封装给服务端发送数据
        ObjectNode objectNode = JsonUtils.getObjectNode();
        objectNode.put("msgtype", String.valueOf(EnMsgType.EN_MSG_LOGIN));
        objectNode.put("name", name);
        objectNode.put("passwd", passwd);
        String msg = objectNode.toString();

        //给服务端发送消息
        channel.writeAndFlush(msg);

        //等服务端的返回消息

        int resultCode = 0 ;
        try {
            resultCode = ClientRecvHandler.squeue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (200 == resultCode) {
            //返回成功则到主菜单页面
            System.out.println("登录成功");
            //用户登录成功，将用户名设置为全局变量
            loginName=name;
          // is_Offline(loginName);//离线消息
            doMainMemu();
        } else {
            //返回失败进入到用户账号界面
            System.out.println("登录失败");
        }
    }

    /**
     * 主菜单页面
     */
    private void showMainMemu() {
        System.out.println("====================系统使用说明====================");
        System.out.println("                         注：输入多个信息用\":\"分割");
        System.out.println("1.输入modifypwd:username 表示该用户要修改密码");
        System.out.println("2.输入getallusers 表示用户要查询所有人员信息");
        System.out.println("3.输入username:xxx 表示一对一聊天");
        System.out.println("4.输入all:xxx 表示发送群聊消息");
        System.out.println("5.输入sendfile:xxx 表示发送文件请求:[sendfile][接收方用户名][发送文件路径]");
        System.out.println("6.输入quit 表示该用户下线，注销当前用户重新登录");
        System.out.println("7.输入help查看系统菜单");
        System.out.println("================================================");
    }

    /**
     * 主菜单功能
     */
    private void doMainMemu(){
        showMainMemu();
        System.out.println("请选择操作：");
        while (true) {
            String line = in.nextLine();
            if ("quit".equals(line)) {
                //重新登录页面
                break;
            }
            if("help".equals(line)){
                //查看帮助系统菜单
                showMainMemu();
                continue;
            }
            if("getallusers".equals(line)){
                //查询所有人员信息，获取用户列表
                get_All_Users();
                continue;
            }
            String[] strings=line.split(":");

            if("modifypwd".equals(strings[0])){
                //修改密码操作
                modifyPwd();
                continue;
            }
           if("all".equals(strings[0])){
                //群聊
               get_All_Users();
               //获取到所有在线的人员列表，然后给在线的发送消息，相当于变相的单聊
               continue;
           }
           if("sendfile".equals(strings[0])){
                //发送文件
               String s=strings[2]+":"+strings[3];
               System.out.println(s);
               sendFile(strings[1],s);
               continue;
           }else {
               //单聊
               singleChat(strings[0],strings[1]);
           }
        }
    }
    /*
   发送文件
    */
    private void sendFile(String userName,String fileName){

        //参数校验
        File file=new File(fileName);
        if(!(file.exists() && file.isFile())){
            System.out.println("文件名不合法");
            return;
        }

        //封装数据  发送文件请求获取服务端可绑定接口
        ObjectNode objectNode=JsonUtils.getObjectNode();
        objectNode.put("msgtype",String.valueOf(EnMsgType.EN_MSG_TRANSFER_FILE));
        objectNode.put("toname",userName);
        objectNode.put("fromname",loginName);
        String json=objectNode.toString();

        //给服务端发送消息
        ByteBuf buf = Unpooled.buffer(Constant.MSG_DEFAULT_SIZE);
        buf.writeBytes(json.getBytes());
        channel.writeAndFlush(buf);

        //服务端返回
        int port=0;
        try {
            port=ClientRecvHandler.squeue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(port == 0){
            System.out.println("[log]:未获取到服务端接收文件端口，请再次操作");
            return;
        }

        if(port>0){
            //返回成功，子线程连接服务器，并发送文件
            new FileSendService(port,file).start();
        }else{
            System.out.println("服务端未返回端口。。。。。。");
        }
    }

    /*
    单聊操作
     */
    public void singleChat(String toName,String msg){
        //封装给服务端发送数据
        ObjectNode objectNode = JsonUtils.getObjectNode();
        objectNode.put("msgtype", String.valueOf(EnMsgType.EN_MSG_CHAT));
        objectNode.put("fromName",loginName);
        objectNode.put("toName", toName);
        objectNode.put("msg", msg);
        String message = objectNode.toString();

        //给服务端发送消息
        channel.writeAndFlush(message);

    }

    /*
    注册操作
     */
    private void register() {
        System.out.println("请输入你注册设置的用户名：");
        String name = in.nextLine();
        System.out.println("请输入你注册设置的密码:");
        String passwd = in.nextLine();
        System.out.println("请输入你注册的邮箱账号：");
        String email = in.nextLine();

        //封装给服务端发送数据
        ObjectNode objectNode = JsonUtils.getObjectNode();
        objectNode.put("msgtype", String.valueOf(EnMsgType.EN_MSG_REGISTER));
        objectNode.put("name", name);
        objectNode.put("passwd", passwd);
        objectNode.put("email",email);
        String msg = objectNode.toString();

        //给服务端发送消息
        channel.writeAndFlush(msg);

        //等服务端的返回消息
        int resultCode = 0 ;
        try {
            resultCode = ClientRecvHandler.squeue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (200 == resultCode) {
            System.out.println("恭喜用户注册成功。。。。。。");
        } else {
            //返回失败进入到第一次的用户显示界面
            System.out.println("对不起，注册失败。。。。。。");
            doMainMemu();
        }
    }

    /*
    修改密码操作
     */
    public void modifyPwd(){
        String name=loginName;
        System.out.println("请输入你的旧密码：");
        String oldPwd = in.nextLine();
        System.out.println("请输入你设置的新密码:");
        String newPwd = in.nextLine();


        //封装给服务端发送数据
        ObjectNode objectNode = JsonUtils.getObjectNode();
        objectNode.put("msgtype", String.valueOf(EnMsgType.EN_MSG_MODIFY_PWD));
        objectNode.put("name",name);
        objectNode.put("oldPwd", oldPwd);
        objectNode.put("newPwd", newPwd);
        String msg = objectNode.toString();

        //给服务端发送消息
        channel.writeAndFlush(msg);

        //等服务端的返回消息
        int resultCode = 0 ;
        try {
            resultCode = ClientRecvHandler.squeue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (200 == resultCode) {
            System.out.println("恭喜用户修改密码成功。。。。。。");
        } else {
            //返回失败进入到第一次的用户显示界面
            System.out.println("修改密码失败");
            showMainMemu();
        }
    }

    /*
    忘记密码操作
     */
    public boolean forgetPwd(){
        System.out.println("请输入你的用户名：");
        String name = in.nextLine();
        System.out.println("请输入你的邮箱：");
        String email = in.nextLine();

        //封装给服务端发送数据
        ObjectNode objectNode = JsonUtils.getObjectNode();
        objectNode.put("msgtype", String.valueOf(EnMsgType.EN_MSG_FORGET_PWD
        ));
        objectNode.put("name", name);
        objectNode.put("email", email);
        String msg = objectNode.toString();

        //给服务端发送消息
        channel.writeAndFlush(msg);

        //等服务端的返回消息
        int resultCode = 0 ;
        try {
            resultCode = ClientRecvHandler.squeue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (200 == resultCode) {
            System.out.println("密码已经发送到你的邮箱。。。。。");
            showLoginMemu();
        } else {
            //返回失败进入到第一次的用户显示界面
            System.out.println("操作失败");
            showLoginMemu();
        }
        return false;
    }

    /**
     * 检查是否有离线消息
     */
    private void is_Offline(String loginName) {
        //封装消息
        ObjectNode nodes = JsonUtils.getObjectNode();
        nodes.put("msgtype", String.valueOf(EnMsgType.EN_MSG_OFFLINE_MSG_EXIST));
        nodes.put("to_name", loginName);
        String msg1 = nodes.toString();
        //发送
        channel.writeAndFlush(msg1);
    }

    /**
     * 请求所有在线用户操作
     */
    private void get_All_Users() {
        ObjectNode node = JsonUtils.getObjectNode();
        node.put("msgtype", String.valueOf(EnMsgType.EN_MSG_GET_ALL_USERS));
        String msg = node.toString();

        //给服务端发送消息
        channel.writeAndFlush(msg);
    }

    /**
     * 请求下线操作
     */
    private void user_out() {
        ObjectNode node = JsonUtils.getObjectNode();
        node.put("msgtype", String.valueOf(EnMsgType.EN_MSG_OFFLINE));
        node.put("name", loginName);
        String msg = node.toString();

        //给服务端发送消息
        channel.writeAndFlush(msg);
        //等服务端的返回消息

        int resultCode = 0;
        try {
            resultCode = ClientRecvHandler.squeue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (200 == resultCode) {
            //返回成功
            System.out.println("下线成功，期待您的下次登录!");
        } else {
            //返回失败
            System.out.println("输入信息有误，下线失败！");
        }
    }
}
