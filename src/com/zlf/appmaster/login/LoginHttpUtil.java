package com.zlf.appmaster.login;

import android.text.TextUtils;
import android.util.Log;

import com.zlf.appmaster.Constants;
import com.zlf.appmaster.model.LoginUser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Administrator on 2016/8/26.
 */
public class LoginHttpUtil {
    //封装的发送请求函数
    public static void sendHttpRequest(final String address, String tag, String phoneNumber, String pwd, String userName, final HttpCallBackListener listener) throws UnsupportedEncodingException {
        if (!isNetworkAvailable()){
            //这里写相应的网络设置处理
            if (listener != null){
                listener.onFinish("网络连接错误");
            }
            return;
        }

        //设置编码
        final String encode = "UTF-8";
        final StringBuilder StringUrl = new StringBuilder(address);
        StringUrl.append(tag);

        String phone = "";
        try {
            DesUtils des = new DesUtils(Constants.KEY_TAG);//自定义密钥
            phone = des.encrypt(phoneNumber);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        StringUrl.append(LoginUser.PHONENUMBER)
                .append("=")
                .append(URLEncoder.encode(phone, encode))
                .append("&")
                .append(LoginUser.PASSWORD)
                .append("=")
                .append(URLEncoder.encode(pwd, encode));
        if (!TextUtils.isEmpty(userName)) {
            StringUrl.append("&")
                    .append(LoginUser.USERNAME)
                    .append("=")
                    .append(URLEncoder.encode(userName, encode));
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try{
                    URL url = new URL(StringUrl.toString());
                    Log.e("shsdfhs", StringUrl.toString());
                    //使用HttpURLConnection
                    connection = (HttpURLConnection) url.openConnection();
                    //设置方法和参数
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    //获取返回结果
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null){
                        response.append(line);
                    }
                    //成功则回调onFinish
                    if (listener != null){
                        listener.onFinish(response.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //出现异常则回调onError
                    if (listener != null){
                        listener.onError(e);
                    }
                }finally {
                    if (connection != null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    //判断当前网络是否可用
    public static boolean isNetworkAvailable(){
        //这里检查网络，后续再添加
        return true;
    }
}
