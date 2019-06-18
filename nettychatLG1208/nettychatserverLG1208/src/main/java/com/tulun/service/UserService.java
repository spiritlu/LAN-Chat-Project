package com.tulun.service;

import com.tulun.bean.OfflineUser;
import com.tulun.bean.User;

public interface UserService {
    /**
     *d登录操作
     * @param name
     * @param pwd
     * @return true:用户存在，即可以登录  false:用户不存在
     */
    boolean doLogin(String name , String pwd);

    boolean doRegister(String name , String pwd,String email);

    boolean doModifyPwd(String name,String oldPwd,String newPwd);

    boolean doForgetPwd(String name, String email);

    //通过用户名查看用户是否存在
    User doIsExist(String name);

    boolean user_NotOnline(int id,String toname, String fromname,int msgtype, String msg, int state);

    OfflineUser user_NotonlineMsg(String name);

}
