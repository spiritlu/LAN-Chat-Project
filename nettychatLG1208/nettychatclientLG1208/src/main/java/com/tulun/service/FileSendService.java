package com.tulun.service;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/*
发送端发送文件处理逻辑
 */
public class FileSendService extends Thread {
    private int port;
    private File file;

    public FileSendService(int port, File file) {
        this.file = file;
        this.port = port;
    }

    @Override
    public void run() {
        //创建socket实例
        Socket socket = new Socket();

        try {
            //连接服务器
            socket.connect(new InetSocketAddress("127.0.0.1", port));//出错，连接失败？？？
            //socket.bind(new InetSocketAddress("127.0.0.1",port));
            System.out.println(Thread.currentThread().getName() + " 发送文件子线程连接服务器成功");

            //文件发送
            //读取磁盘文件
            FileInputStream inputStream = new FileInputStream(file);
            String name = file.getName();

            //发送流：socket
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeUTF(name);

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = inputStream.read(bytes)) != -1) {
                dataOutputStream.write(bytes, 0, len);
            }

            dataOutputStream.close();
            inputStream.close();
            System.out.println(Thread.currentThread().getName() + " 发送文件子线程发送成功");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    //关闭资源
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
