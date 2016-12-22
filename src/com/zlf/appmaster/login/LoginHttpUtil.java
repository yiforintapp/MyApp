package com.zlf.appmaster.login;

import android.content.Context;
import android.text.TextUtils;

import com.zlf.appmaster.Constants;
import com.zlf.appmaster.model.LoginUser;
import com.zlf.appmaster.utils.AppUtil;
import com.zlf.appmaster.utils.QLog;

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
    public static void sendLoginHttpRequest(Context context, final String address, String tag, String phoneNumber, String pwd, String userName, final HttpCallBackListener listener) throws UnsupportedEncodingException {
        if (!AppUtil.hasInternet(context)){
            //这里写相应的网络设置处理
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

        runnableStart(StringUrl.toString(), listener);

    }

    //封装的发送请求函数
    public static void sendBannerHttpRequest(Context context, final String address, final HttpCallBackListener listener) {
        if (!AppUtil.hasInternet(context)){
            //这里写相应的网络设置处理
            if (listener != null){
                listener.onFinish("网络连接错误");
            }
            return;
        }

        StringBuilder StringUrl = new StringBuilder(address);
        runnableStart(StringUrl.toString(), listener);

    }


    private static void runnableStart(final String urls, final HttpCallBackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                InputStream inputStream = null;
                InputStreamReader inputReader = null;
                BufferedReader reader = null;
                try{
                    URL url = new URL(urls);
                    QLog.e("adcb", urls);
                    //使用HttpURLConnection
                    connection = (HttpURLConnection) url.openConnection();
                    //设置方法和参数
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    //获取返回结果
                    inputStream = connection.getInputStream();
                    inputReader = new InputStreamReader(inputStream);
                    reader = new BufferedReader(inputReader);
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
                    try {
                        if (connection != null){
                            connection.disconnect();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (inputReader != null) {
                            inputReader.close();
                        }
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
