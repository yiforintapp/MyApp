package com.zlf.appmaster.model;

import com.zlf.appmaster.model.BaseInfo;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/8/26.
 */
public class LoginUser extends BaseInfo implements Serializable {

    private String phoneNumber;
    private String password;

    //用于URL传参和取参时的key
    public static String PHONENUMBER = "phoneNumber";
    public static String PASSWORD = "passWord";

    //构造函数
    public LoginUser(String phoneNumber, String password, String userName) {
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

}
