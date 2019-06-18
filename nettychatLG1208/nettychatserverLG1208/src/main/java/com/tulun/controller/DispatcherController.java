package com.tulun.controller;


import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tulun.bean.OfflineUser;
import com.tulun.bean.User;
import com.tulun.constant.EnMsgType;
import com.tulun.dao.UserDao;
import com.tulun.dao.impl.UserDaoImpl;
import com.tulun.netty.ServerHandler;
import com.tulun.service.FileService;
import com.tulun.service.UserService;
import com.tulun.service.impl.UserServiceImpl;
import com.tulun.util.C3p0Util;
import com.tulun.util.EmailUtils;
import com.tulun.util.JsonUtils;
import com.tulun.util.PortUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.io.File;
import java.net.SocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * desc:具体请求分发类
 * @user:gongdezhe
 * @date:2019/5/16
 */

public class DispatcherController {
    //通过单例模式实例化对象
    private static DispatcherController controller = new DispatcherController();

    //建立两个缓存，维护用户在线缓存
    private static ConcurrentHashMap<ChannelHandlerContext,String> userOnlineChanelName=new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String,ChannelHandlerContext> userOnlineNameChanel=new ConcurrentHashMap<>();

    //和客户端建立通信的channelHandler实例
    private ChannelHandlerContext ctx;

    /**
     * 供controller层回调网络层的接口进行网络通信服务
     */
    private ServerHandler chatServerHandler;

    //对外提供的实例化方法
    public static DispatcherController getInstance() {
        return controller;
    }

    //用户相关的service
    private UserService userService = new UserServiceImpl();

    /**
     * 分类类的核心方法
     * @param ctx
     * @param msg
     * @return
     */
    public String process(ChannelHandlerContext ctx, String msg) {
        this.ctx = ctx;

        //解析消息
        ObjectNode jsonNodes = JsonUtils.getObjectNode(msg);
        String msgtype = jsonNodes.get("msgtype").asText();

        if (String.valueOf(EnMsgType.EN_MSG_LOGIN).equals(msgtype)) {
            //登录操作
            return doLogin(jsonNodes);
        } else if(String.valueOf(EnMsgType.EN_MSG_REGISTER).equals(msgtype)){
            //注册操作
            return register(jsonNodes);
        } else if(String.valueOf(EnMsgType.EN_MSG_MODIFY_PWD).equals(msgtype)){
            //修改密码操作
            return modifyPwd(jsonNodes);
        }else if(String.valueOf(EnMsgType.EN_MSG_FORGET_PWD).equals(msgtype)){
            //忘记密码操作
            return forgetPwd(jsonNodes);
        } else if(String.valueOf(EnMsgType.EN_MSG_CHAT).equals(msgtype)){
            //单聊操作
            return singleChat(jsonNodes);
        }else if(String.valueOf(EnMsgType.EN_MSG_TRANSFER_FILE).equals(msgtype)){
            //发送文件操作
            return sendFile(jsonNodes);
        }else if (String.valueOf(EnMsgType.EN_MSG_OFFLINE_MSG_EXIST).equals(msgtype)) {
            //查看是否有离线信息
            return send_NotonlineMsg(jsonNodes);
        } else if (String.valueOf(EnMsgType.EN_MSG_OFFLINE).equals(msgtype)) {
            //用户请求下线
            return user_out(jsonNodes);
        }else if (String.valueOf(EnMsgType.EN_MSG_GET_ALL_USERS).equals(msgtype)) {
            //请求所有在线用户
            return get_All_Users(jsonNodes);
        }else if(String.valueOf(EnMsgType.EN_MSG_OFFLINE_FILE_EXIST).equals(msgtype)){
            //离线文件操作
            return send_NotonlineFile(jsonNodes);
        }
        return "";
    }

    /*
    发送文件的逻辑
     */
    private String sendFile(ObjectNode nodes){
        //解析参数
        String toName=nodes.get("toname").asText();
        String fromName=nodes.get("fromname").asText();

        //封装返回消息
        ObjectNode objectNode=JsonUtils.getObjectNode();
        objectNode.put("msgtype",String.valueOf(EnMsgType.EN_MSG_ACK));
        objectNode.put("srctype",String.valueOf(EnMsgType.EN_MSG_TRANSFER_FILE));
        objectNode.put("code",200);

        //是否在线，查缓存
        ChannelHandlerContext toNameChannel=getChannelByName(toName);

        int fromPort=PortUtils.getFreePort();
        int toPort=PortUtils.getFreePort();

        if(toNameChannel!=null){
            //接收方在线
//            int fromPort=PortUtils.getFreePort();
//            int toPort=PortUtils.getFreePort();

            //子线程  绑定端口并且转发文件  ----》端口号
            /*
            发送端：端口1
            接收端：端口2
                     serversocket
                     bind()
                     accept
                     socket1
                     socket2
             */
            new FileService(fromPort,toPort).start();

            //接收方发送消息
            ObjectNode sendToRecvNode=JsonUtils.getObjectNode();
            sendToRecvNode.put("msgtype",String.valueOf(EnMsgType.EN_MSG_TRANSFER_FILE));
            sendToRecvNode.put("port",toPort);
            System.out.println("recv: port:"+toPort);

            //给接收端主动推送消息
            toNameChannel.channel().writeAndFlush(sendToRecvNode.toString());
            SocketAddress address = toNameChannel.channel().remoteAddress();
            System.out.println(address);

            objectNode.put("port",fromPort);

        }else{
            objectNode.put("code",300);
            //不在线  300
            //将消息存放入缓存数据库中，将IP地址和端口号作为getMsgType
            User user = new User();
            user = userService.doIsExist(toName);//检查用户是否存在
            if(user!=null) {
                boolean isSave = userService.user_NotOnline(user.getId(), toName, fromName,fromPort, "离线文件", 1);
                if (isSave) {
                    System.out.println("消息已经放到缓存数据库中。。。。。。");
                }
            }

        }
        return objectNode.toString();

    }
    /*
    离线文件的操作
    当用户上线之后如果有离线的文件，将离线的文件进行发送
     */
    public String send_NotonlineFile(ObjectNode objectNode){
        //解析toName
        String name = objectNode.get("to_name").asText();

        //封装返回数据
        ObjectNode resultNode = JsonUtils.getObjectNode();
        resultNode.put("msgtype", String.valueOf(EnMsgType.EN_MSG_OFFLINE_FILE_EXIST));

        //检查自己是否有离线文件存在
        OfflineUser  offmsg = new OfflineUser ();
        offmsg=userService.user_NotonlineMsg(name);
        if(offmsg==null){ //为空说明不存在离线文件，直接返回
            return null;
        }

        //从数据库中拿到离线消息的IP地址和端口号
        int fromPort=offmsg.getMsgType();
        int toPort=PortUtils.getFreePort();//用户的端口号

        new FileService(fromPort,toPort).start();

        return resultNode.toString();
    }

    /**
     * 显示所有在线用户操作
     *
     * @param nodes
     * @return
     */
    private String get_All_Users(ObjectNode nodes) {

        //封装返回数据
        ObjectNode resultNode = JsonUtils.getObjectNode();
        resultNode.put("msgtype", String.valueOf(EnMsgType.EN_MSG_ACK));
        resultNode.put("srctype", String.valueOf(EnMsgType.EN_MSG_GET_ALL_USERS));

        ArrayList a = get_Users();

        if (a != null) {
            //在线则直接发送   code:200 成功
            resultNode.put("users", a.toString());
            resultNode.put("code", 200);
        } else {
            //有错误    code:400 失败
            resultNode.put("code", 400);
        }

        return resultNode.toString();
    }
    /**
     * 离线消息发送功能
     * 当用户上线之后如果有离线消息，将离线消息进行发送
     */
    public String send_NotonlineMsg(ObjectNode nodes) {
        //解析toName
        String name = nodes.get("to_name").asText();

        //封装返回数据
        ObjectNode resultNode = JsonUtils.getObjectNode();
        resultNode.put("msgtype", String.valueOf(EnMsgType.EN_MSG_OFFLINE_MSG_EXIST));
        OfflineUser  offmsg = new OfflineUser ();
        //获取用户管道
        do{
            offmsg = userService.user_NotonlineMsg(name);
            if(offmsg==null){
                return null;
            }
            ChannelHandlerContext ct = getChannelByName(offmsg.getToName());
            resultNode.put("fromName", offmsg.getFromName());
            resultNode.put("msg", offmsg.getMsg());
            ct.channel().writeAndFlush(resultNode.toString());
        }while (offmsg!=null);
        return resultNode.toString();
    }
    /**
     * 下线功能
     *
     */
    public String user_out(ObjectNode nodes){
        //解析name
        String name = nodes.get("name").asText();

        //封装返回数据
        ObjectNode resultNode = JsonUtils.getObjectNode();
        resultNode.put("msgtype", String.valueOf(EnMsgType.EN_MSG_ACK));
        resultNode.put("srctype", String.valueOf(EnMsgType.EN_MSG_OFFLINE));
        removeCache(name);
        if (!userOnlineChanelName.containsKey(name)) {
            //   code:200 成功
            resultNode.put("code", 200);
        } else {
            // code:400 失败
            resultNode.put("code", 400);
        }
        return resultNode.toString();
    }

    /**
     * 登录操作
     * @param nodes
     * @return
     */
    private String doLogin(ObjectNode nodes){
        //解析参数
        String name = nodes.get("name").asText();
        String passwd = nodes.get("passwd").asText();

        //查询数据库数据，判断用户是否存在
        boolean isExist = userService.doLogin(name, passwd);

        //封装返回消息
        ObjectNode objectNode = JsonUtils.getObjectNode();
        objectNode.put("msgtype", String.valueOf(EnMsgType.EN_MSG_ACK));
        objectNode.put("srctype", String.valueOf(EnMsgType.EN_MSG_LOGIN));
        if (isExist) {
            //添加用户在线缓存
            addCache(ctx,name);
            //用户存在
            objectNode.put("code", 200); //200 表示登录成功
        }else {
            //用户不存在
            objectNode.put("code", 300); //300 表示登录失败
        }

        String resultMsg = objectNode.toString();
        return resultMsg;
    }

    /*
    注册操作
     */
    private String register(ObjectNode nodes){
        //解析参数
        String name = nodes.get("name").asText();
        String passwd = nodes.get("passwd").asText();
        String email=nodes.get("email").asText();

        //查询数据库数据，判断用户是否存在
        boolean isSuccessRegister=userService.doRegister(name,passwd,email);

        //封装返回消息
        ObjectNode objectNode = JsonUtils.getObjectNode();
        objectNode.put("msgtype", String.valueOf(EnMsgType.EN_MSG_ACK));
        objectNode.put("srctype", String.valueOf(EnMsgType.EN_MSG_REGISTER));
        if (isSuccessRegister) {
            objectNode.put("code", 200); //200 表示注册成功
        } else {
            //用户已经存在
            objectNode.put("code", 300); //300 表示注册失败
        }
        String resultMsg = objectNode.toString();
        return resultMsg;
    }

    /*
    修改密码操作
     */
    public String modifyPwd(ObjectNode nodes){
        //解析参数
        String name=nodes.get("name").asText();
        String oldPwd = nodes.get("oldPwd").asText();
        String newPwd = nodes.get("newPwd").asText();

        //判断修改密码是否成功
        boolean isSuccessModify=userService.doModifyPwd(name,oldPwd,newPwd);

        //封装返回消息
        ObjectNode objectNode = JsonUtils.getObjectNode();
        objectNode.put("msgtype", String.valueOf(EnMsgType.EN_MSG_ACK));
        objectNode.put("srctype", String.valueOf(EnMsgType.EN_MSG_MODIFY_PWD));

        if (isSuccessModify) {
            objectNode.put("code", 200); //200 表示修改密码成功
        } else {
            objectNode.put("code", 300); //300 表示修改密码操作失败
        }
        String resultMsg = objectNode.toString();
        return resultMsg;
    }
    /*
    忘记密码操作
     */
    public String forgetPwd(ObjectNode nodes){
        //解析参数
        String name = nodes.get("name").asText();
        String email = nodes.get("email").asText();

        //忘记密码操作获取密码
        boolean issuccess=userService.doForgetPwd(name,email);

        //封装返回消息
        ObjectNode objectNode = JsonUtils.getObjectNode();
        objectNode.put("msgtype", String.valueOf(EnMsgType.EN_MSG_ACK));
        objectNode.put("srctype", String.valueOf(EnMsgType.EN_MSG_FORGET_PWD));

        if (issuccess) {
            System.out.println("已成功将密码发送到邮箱。。。。");
            objectNode.put("code", 200); //200 表示忘记密码操作成功。
        } else {
            //用户已经存在
            System.out.println("忘记密码操作失败。。。。。。");
            objectNode.put("code", 300); //300 表示失败
        }
        String resultMsg = objectNode.toString();
        return resultMsg;
    }

    /*
    一对一聊天
     */
    public String singleChat(ObjectNode nodes){
        //解析参数
        String toName = nodes.get("toName").asText();
        String fromName=nodes.get("fromName").asText();
        String msg=nodes.get("msg").asText();
        int msgtype=nodes.get("msgtype").asInt();

        //封装返回消息
        ObjectNode objectNode = JsonUtils.getObjectNode();
        objectNode.put("msgtype", String.valueOf(EnMsgType.EN_MSG_ACK));
        objectNode.put("srctype", String.valueOf(EnMsgType.EN_MSG_CHAT));

        //数据库查看用户是否存在，是否合法
        if(userService.doIsExist(toName)==null){
            //用户不存在
            objectNode.put("code",300);
            return objectNode.toString();
        }
        //判断用户是否在线
        ChannelHandlerContext ct=getChannelByName(toName);
        //发送给离线用户的信息是否被存入数据库中
        User user = new User();
        user = userService.doIsExist(fromName);
        if(ct!=null){
            //在线直接转发消息  200
            ct.channel().writeAndFlush(nodes.toString());
            System.out.println("用户在线，可以直接转发消息。。。。。。");
            objectNode.put("code",200);
        }else{
            //不在线的时候   400  将fromName,toName,msg缓存到数据库中。
            boolean isSave = userService.user_NotOnline(user.getId(),toName, fromName, msgtype,msg, 1);
            if(isSave) {
                System.out.println("消息已经放到缓存数据库中。。。。。。");
            }
            objectNode.put("code",400);
        }
        return objectNode.toString();
    }

    //缓存用户上线信息
    public void addCache(ChannelHandlerContext ct,String name){
      userOnlineChanelName.put(ct,name);
      userOnlineNameChanel.put(name,ct);
    }
    //查询用户缓存
    public ChannelHandlerContext getChannelByName(String toName){
        return userOnlineNameChanel.get(toName);
    }
    //删除用户缓存
    public void removeCache(String toName){
        ChannelHandlerContext ct=userOnlineNameChanel.get(toName);
        userOnlineNameChanel.remove(ct);
        userOnlineChanelName.remove(toName);
    }
    //删除用户缓存
    public static void removeCache(ChannelHandlerContext ct){
        String toName=userOnlineChanelName.get(ct);
        userOnlineNameChanel.remove(toName);
        userOnlineChanelName.remove(ct);
    }
    //获取所有用户在线信息
    public ArrayList get_Users() {
        ArrayList<String> a = new ArrayList();
        int i = 0;
        Set<Map.Entry<String, ChannelHandlerContext>> ms = userOnlineNameChanel.entrySet();
        for (Map.Entry entry : ms) {
            a.add((String) entry.getKey());

        }
        return a;
    }
}
