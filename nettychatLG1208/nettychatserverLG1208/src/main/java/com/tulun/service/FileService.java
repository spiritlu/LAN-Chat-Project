package com.tulun.service;

import com.tulun.util.PortUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
/**
服务端文件传输处理类
 */
public class FileService extends Thread {
    private int fromPort;
    private int toPort;

    public FileService(int fromPort, int toPort) {
        this.fromPort = fromPort;
        this.toPort = toPort;

    }

    @Override
    public void run() {
        ServerSocket serverSocketFrom = null;
        ServerSocket serverSocketTo = null;
        try {
            //创建serverSocket实例,并绑定端口
            serverSocketFrom = new ServerSocket(fromPort);
            serverSocketTo = new ServerSocket(toPort);
            System.out.println(Thread.currentThread().getName()+"文件传输子线程启动并绑定端口");

            //接收客户端的连接
            Socket socketFrom = serverSocketFrom.accept();//阻塞在这里！！！
            Socket socketTo = serverSocketTo.accept();

            //文件转发
            DataInputStream dataInputStream = new DataInputStream(socketFrom.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socketTo.getOutputStream());

            String fileName = dataInputStream.readUTF();
            dataOutputStream.writeUTF(fileName);

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = dataInputStream.read(bytes)) != -1) {
                dataOutputStream.write(bytes,0,len);
            }

            //关闭流
            dataOutputStream.close();
            dataInputStream.close();

            System.out.println(socketFrom.getRemoteSocketAddress()+" 发文件："+fileName+" 给"+socketTo.getRemoteSocketAddress()+ " 转发成功");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocketFrom != null) {
                try {
                    serverSocketFrom.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (serverSocketTo != null) {
                try {
                    serverSocketTo.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //释放端口
            PortUtils.closePort(toPort);
            PortUtils.closePort(fromPort);
        }
    }
}

