package com.tulun.service.impl;

import com.tulun.bean.OfflineUser;
import com.tulun.bean.User;
import com.tulun.dao.UserDao;
import com.tulun.dao.impl.UserDaoImpl;
import com.tulun.service.UserService;
import com.tulun.util.EmailUtils;

public class UserServiceImpl implements UserService {
   //user的dao层实例
    private UserDao userDao = new UserDaoImpl();

    @Override
    public boolean doLogin(String name, String pwd) {
        boolean result = false;
        User user = userDao.getUserByNameAndPwd(name, pwd);
        if (user != null) {
            result = true;
        }

        System.out.println("user:"+name+", pwd:"+pwd+", result:"+result);

        return result;
    }

    @Override
    public boolean doRegister(String name, String pwd, String email) {
        boolean result = false;
        boolean use= userDao.getUserByEmail(email);//判断数据库中是否存在同名的用户名和邮箱
        User user=userDao.getUserByName(name);
        if(!use) {
            if(user==null){
            int i=userDao.getRegisterByNameAndPwd(name,pwd,email);
            if(i==1) {
                result = true;
            }
        }
        }else{
            System.out.println("注册失败。。。。。。");
            result=false;
        }

        System.out.println("user:"+name+", pwd:"+pwd+", result:"+result);

        return result;
    }

    @Override
    public boolean doModifyPwd(String name,String oldPwd, String newPwd) {
        boolean result = false;
        boolean user= userDao.getModifyByNameAndPwd(name,oldPwd, newPwd);
        if (user) {
            result = true;
        }
        System.out.println("旧的密码为:"+oldPwd+", 新的密码为："+newPwd+", result:"+result);

        return result;
    }

    @Override
    public boolean doForgetPwd(String name, String email) {
        boolean result = false;
        User user= userDao.getForgetByNameAndEmail(name, email);
        if (user!=null) {
            String toEmail = user.getEmail();
            String pwd = user.getPwd();
            try {
                EmailUtils.sendEmail(toEmail,pwd);
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("name:"+name+", email："+email+", result:"+result);
        return result;
    }

    @Override
    public User doIsExist(String name) {
        User result=null;
        User user= userDao.getUserByName(name);
        if (user!=null) {
            result = user;
        }
        System.out.println("name:"+name +",  "  +  "result:"+result);
        return result;
    }

    @Override
    public boolean user_NotOnline(int id, String toname, String fromname, int msgtype, String msg, int state) {
        boolean result = false;
        result = userDao.user_NotOnline(id,toname,fromname,msgtype,msg,state);
        return result;
    }

    @Override
    public OfflineUser user_NotonlineMsg(String name) {
        OfflineUser offmsg = userDao.user_NotonlineMsg(name);
        if (offmsg == null) {
            return  null;
        }
        return offmsg;
    }
}
