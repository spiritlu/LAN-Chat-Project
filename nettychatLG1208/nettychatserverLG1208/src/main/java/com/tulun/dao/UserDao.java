package com.tulun.dao;

import com.tulun.bean.OfflineUser;
import com.tulun.bean.User;

/**
 * desc:用户相关数据库操作
 * @user:gongdezhe
 * @date:2019/5/16
 */

public interface UserDao {
    /**
     * 通过用户名和密码查询用户信息    用于登录操作
     * @param name
     * @param pwd
     * @return
     */
    User getUserByNameAndPwd(String name, String pwd);

    /*
    注册操作：通过用户名、密码以及邮箱返回注册的信息
     */
    int getRegisterByNameAndPwd(String name,String pwd,String email);

    /*
    通过email来查看数据库中是否存在同名的邮箱
     */
    boolean getUserByEmail(String email);

    /*
    修改密码：通过提供旧的密码以及新的密码来重新修改密码
     */
    boolean getModifyByNameAndPwd(String name,String oldPwd, String newPwd);

    /*
    忘记密码：通过用户名和邮箱找寻密码
     */
    User getForgetByNameAndEmail(String name, String email);

    /*
    在单聊之前，首先需要确定数据库中是否有该用户的存在
     */
    User getUserByName(String name);

    /*
    单聊的时候，用户不在线的时候，将消息缓存到数据库中
     */
    boolean user_NotOnline(int id,String toname, String fromname, int msgtype,String msg, int state);

    OfflineUser user_NotonlineMsg(String name);

    boolean update_Msg(int id, OfflineUser  msg);

}
