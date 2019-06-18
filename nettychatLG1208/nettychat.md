#聊天系统-基于netty的局域网聊天系统
```
聊天系统类似于QQ、微信

设计分析
 功能点
   聊天模块：
   a.一对一聊天(单聊)
   b.群聊->所有的在线用户可以接受消息 
   c.好友列表  ->所有在线用户信息列表
   d.好友上线/下线通知
    
     单聊消息发送过程
     a.发送内容：消息内容、发送方、接收方
     b.接收方在线状态：接收方在线、不在线
    c.发行形式：发送方-接收方、**发送发-服务器（转发）-接收方（选用）**   -》C/S模型
   
   用户模块：
   发送方/接收方->用户基本信息 ->
   a.用户登录 ->拿用户基本信息（账号、密码）来校验用户是否存在（服务端）判断是否登录成功（用户查看）
   b.用户注册 ->基本信息录入，并产生用户ID
   c.修改密码 ->更新密码信息
   d.忘记密码 ->通过关键信息找回密码
   
   文件模块：
   a.发送/接受文件 ->接收方是否存在及是否在线等
 技术点
    网络通信框架：Netty
    Netty是基于NIO实现的异步非阻塞的IO模型
    应用场景：并发量高、业务逻辑简单的场景 ->聊天
    
    消息体格式：
    客户端登录、发消息等业务 ->流->服务器（区分不同业务的流）-》
    消息格式 -》XML、JSON  —》JSON格式来传输数据
    
    忘记密码：
    使用邮件功能
    
    xml:
    <login> 
        <name>张三</name>
        <passwd>123456</passwd>
    </login>
    
    json:
    {
      login:{
         name:张三，
         passwd:123456
      }
    }
    
 数据库设计
  用户表：（ID）、用户名、密码、邮箱 


项目的目录结构
|src
|-main   //项目源代码
|--java  //java源代码
|---com
|----tulun
|-----controller //接收请求（web页面首先请求到controller层）
|-----service  //业务逻辑处理（接口）
|------impl   //业务逻辑处理（实现类）
|-----dao     //数据库操作（接口）
|------impl   //数据库操作（实现类）
|-----bean    //自己定义的基础类型
|-----constant //常量、枚举等类型类
|-----util    //工具类
|--resources //配置信息存放
|--[webapp]  //前端页面存放
|-test   //测试用例代码


搭建系统
   两个项目
      服务端：nettychatServer
      客户端：nettychatClient

    父子工程搭建
    netty使用引用jar包，使用maven来管理项目jar包
  
  服务端项目代建
  a.netty依赖jar包
  
    <dependency>
        <groupId>io.netty</groupId>
        <artifactId>netty-all</artifactId>
        <version>4.1.5.Final</version>
    </dependency>
   
                  
```