package com.tulun.dao.impl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tulun.bean.OfflineUser;
import com.tulun.bean.User;
import com.tulun.constant.EnMsgType;
import com.tulun.dao.UserDao;
import com.tulun.util.C3p0Util;
import com.tulun.util.JsonUtils;
import io.netty.channel.ChannelHandlerContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDaoImpl implements UserDao {
    @Override
    public User getUserByNameAndPwd(String name, String pwd) {
        //获取connect实例
        Connection connection = C3p0Util.getConnection();

        //返回对象
        User user = null;

        String sql = "select * from user where name=? and pwd=?";
        try {
            //获取Statement实例
            PreparedStatement statement = connection.prepareStatement(sql);

            //参数赋值
            statement.setString(1, name);
            statement.setString(2, pwd);

            //执行查询操作
            ResultSet resultSet = statement.executeQuery();

            //返回结果的封装
            while (resultSet.next()) {
                user = new User();
                user.setId(resultSet.getInt("id"));
                user.setName(resultSet.getString("name"));
                user.setPwd(resultSet.getString("pwd"));
                user.setEmail(resultSet.getString("email"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }


    @Override
    public int getRegisterByNameAndPwd(String name, String pwd, String email) {
        //获取connect实例
        Connection connection = C3p0Util.getConnection();

        //返回对象
        User user = null;

        String sql = "insert into user(name,pwd,email) values(?,?,?)";
        try {
            //获取Statement实例
            PreparedStatement statement = connection.prepareStatement(sql);

            //参数赋值
            statement.setString(1, name);
            statement.setString(2, pwd);
            statement.setString(3, email);

            //执行插入操作
            int result = statement.executeUpdate();

            if (result == 1) {
                return 1;//1表示插入成功
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean getUserByEmail(String email) {
        //获取connect实例
        Connection connection = C3p0Util.getConnection();

        //返回对象
        boolean result = false;

        String sql = "select * from user where email=?";
        try {
            //获取Statement实例
            PreparedStatement statement = connection.prepareStatement(sql);

            //参数赋值
            statement.setString(1, email);

            //执行查询操作
            ResultSet resultSet = statement.executeQuery();

            //返回结果的封装
            if (resultSet.next()) {
                result = true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }


    @Override
    public boolean getModifyByNameAndPwd(String name, String oldPwd, String newPwd) {
        //获取connect实例
        Connection connection = C3p0Util.getConnection();

        //返回对象
        User user = null;

        String sql = "update user set pwd=? where pwd=? and name=?";
        try {
            //获取Statement实例
            PreparedStatement statement = connection.prepareStatement(sql);

            //参数赋值
            statement.setString(1, newPwd);
            statement.setString(2, oldPwd);
            statement.setString(3, name);

            //执行插入操作
            int result = statement.executeUpdate();

            if (result == 1) {
                return true;//1表示插入成功
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public User getForgetByNameAndEmail(String name, String email) {
        //获取connect实例
        Connection connection = C3p0Util.getConnection();

        //返回对象
        User user = null;

        String sql = "select * from user where name=? and email=?";
        try {
            //获取Statement实例
            PreparedStatement statement = connection.prepareStatement(sql);

            //参数赋值
            statement.setString(1, name);
            statement.setString(2, email);

            //执行查询操作，寻找忘记的密码
            ResultSet resultSet = statement.executeQuery();

            //返回结果的封装
            while (resultSet.next()) {
                user = new User();
                user.setId(resultSet.getInt("id"));
                user.setName(resultSet.getString("name"));
                user.setPwd(resultSet.getString("pwd"));
                user.setEmail(resultSet.getString("email"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public User getUserByName(String name) {
        //获取connect实例
        Connection connection = C3p0Util.getConnection();

        //返回对象
        User user = null;

        String sql = "select * from user where name=?";
        try {
            //获取Statement实例
            PreparedStatement statement = connection.prepareStatement(sql);

            //参数赋值
            statement.setString(1, name);

            //执行查询操作
            ResultSet resultSet = statement.executeQuery();

            //返回结果的封装
            while (resultSet.next()) {
                user = new User();
                user.setId(resultSet.getInt("id"));
                user.setName(resultSet.getString("name"));
                user.setPwd(resultSet.getString("pwd"));
                user.setEmail(resultSet.getString("email"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public boolean user_NotOnline(int id,String toname, String fromname, int msgtype,String msg, int state) {
        //获取connect实例
        Connection connection = C3p0Util.getConnection();
        int flag;
        boolean resultSet = false;
        //返回对象
        String sql = "insert into cachemsg(id,toname,fromname,msgtype,msg,state) value(?,?,?,?,?,?)";
        try {
            //获取Statement实例
            PreparedStatement statement = connection.prepareStatement(sql);
            //参数赋值
            statement.setInt(1,id);
            statement.setString(2, toname);
            statement.setString(3, fromname);
            statement.setInt(4,msgtype);
            statement.setString(5, msg);
            statement.setInt(6, state);

            //执行插入操作
            flag = statement.executeUpdate();
            if (flag == 0) {
                resultSet = false;
            } else {
                resultSet = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultSet;
    }

    @Override
    public OfflineUser user_NotonlineMsg(String name) {
        //获取connect实例
        Connection connection = C3p0Util.getConnection();

        //返回对象
        OfflineUser offmsg = new OfflineUser();

        String sql = "select * from cachemsg where toName=? and msgtype=1 and state=1";
        try {
            //获取Statement实例
            PreparedStatement statement = connection.prepareStatement(sql);

            //参数赋值
            statement.setString(1, name);

            //执行查询并进行更新操作操作
            ResultSet rs = statement.executeQuery();
            //若存储过程被正常执行，并至少有一个结果集返回，则result=true;否则就会是result=false;
            //返回结果的封装
            while (rs.next()) {
                offmsg.setId(rs.getInt("id"));
                offmsg.setToName(rs.getString("toName"));
                offmsg.setFromName(rs.getString("fromName"));
                offmsg.setMsgType(rs.getInt("msgtype"));
                offmsg.setMsg(rs.getString("msg"));
                offmsg.setState(rs.getInt("state"));
                update_Msg(rs.getInt("id"), offmsg);
                break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return offmsg;
    }

    @Override
    public boolean update_Msg(int id, OfflineUser msg) {
        //获取connect实例
        Connection connection = C3p0Util.getConnection();
        boolean result = false;
        int flag = 0;
        String sql = "update cachemsg set state=? where id =? ";
        try {
            //获取Statement实例
            PreparedStatement statement = connection.prepareStatement(sql);
            //参数赋值
            if (msg == null) {
                statement.setInt(1, 3);
            } else {
                statement.setInt(1, 2);
            }

            statement.setInt(2, id);

            //执行插入操作
            flag = statement.executeUpdate();
            if (flag == 0) {
                result = false;
            } else {
                result = true;
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }


//    public static void main(String[] args) {
//        UserDaoImpl userDao = new UserDaoImpl();
//        int id=3;
//        String toname="nihao";
//        String fromname="hello";
//        int msgtype=000;
//        String msg="离线消息";
//        int state=1;
//        boolean result=userDao.user_NotOnline(id,toname,fromname,msgtype,msg,state);
//        System.out.println(result);
//    }
}
