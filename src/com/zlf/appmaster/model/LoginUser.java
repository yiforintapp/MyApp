package com.zlf.appmaster.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/8/26.
 */
public class LoginUser extends BaseInfo implements Serializable {

    private String phoneNumber;
    private String password;

    public static String IS_REGISTER = "is_register";  // "0" : 注册 "1" : 登录
    public static String PHONENUMBER = "username";
    public static String PASSWORD = "psw";

    //构造函数
    public LoginUser(String phoneNumber, String password, String userName) {
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

}
