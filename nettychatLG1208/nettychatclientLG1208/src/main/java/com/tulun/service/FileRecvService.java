package com.tulun.service;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/*
文件接收端业务
 */
public class FileRecvService extends Thread {
    private int port;
    public FileRecvService(int port) {
        this.port = port;
    }
    @Override
    public void run() {
        //创建socket实例
        Socket socket = new Socket();

        //连接服务器
        try {
            socket.connect(new InetSocketAddress("127.0.0.1", port));

            //接收服务单返回消息
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

            //解析文件名
            String fileName = dataInputStream.readUTF();

            String savePath = getDeafultPath() + File.separator + fileName;
            System.out.println("文件存储路径：" + savePath);
            FileOutputStream fileOutputStream = new FileOutputStream(savePath);

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = dataInputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, len);
            }

            //关闭流
            fileOutputStream.close();
            dataInputStream.close();
            System.out.println(fileName + "文件接收成功");
        }  catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //获取文件默认存储路径
    public static String getDeafultPath() {
        return new File("").getAbsolutePath();
    }
}
