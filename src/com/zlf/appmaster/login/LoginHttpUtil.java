package com.zlf.appmaster.login;

import android.content.Context;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.zlf.appmaster.Constants;
import com.zlf.appmaster.model.LoginUser;
import com.zlf.appmaster.utils.AppUtil;
import com.zlf.appmaster.utils.LeoLog;
import com.zlf.appmaster.utils.QLog;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/8/26.
 */
public class LoginHttpUtil {
    //封装的发送请求函数
    public static void sendLoginHttpRequest(Context context, final String address, String tag, String phoneNumber, String pwd, String userName, final HttpCallBackListener listener) throws UnsupportedEncodingException {
        if (!AppUtil.hasInternet(context)) {
            //这里写相应的网络设置处理
            return;
        }

        String phone = "";
        try {
            DesUtils des = new DesUtils(Constants.KEY_TAG);//自定义密钥
            phone = des.encrypt(phoneNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constants.FEEDBACK_TYPE, tag);
        params.put(LoginUser.PHONENUMBER, phone);
        params.put(LoginUser.PASSWORD, pwd);
        params.put(LoginUser.USERNAME, userName);

        String encode = "utf-8";
        byte[] data = getRequestData(params, encode).toString().getBytes();
        runPost(address, data, listener);


//        //设置编码
//        final String encode = "UTF-8";
//        final StringBuilder StringUrl = new StringBuilder(address);
//        StringUrl.append(tag);

//        String phone = "";
//        try {
//            DesUtils des = new DesUtils(Constants.KEY_TAG);//自定义密钥
//            phone = des.encrypt(phoneNumber);
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }

//        StringUrl.append(LoginUser.PHONENUMBER)
//                .append("=")
//                .append(URLEncoder.encode(phone, encode))
//                .append("&")
//                .append(LoginUser.PASSWORD)
//                .append("=")
//                .append(URLEncoder.encode(pwd, encode));
//        if (!TextUtils.isEmpty(userName)) {
//            StringUrl.append("&")
//                    .append(LoginUser.USERNAME)
//                    .append("=")
//                    .append(URLEncoder.encode(userName, encode));
//        }

//        runnableStart(StringUrl.toString(), listener);


    }

    private static void runPost(final String address, final byte[] data, final HttpCallBackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(address);
                    Log.d("testUrl", address);
                    //使用HttpURLConnection
                    connection = (HttpURLConnection) url.openConnection();
                    //设置方法和参数
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setUseCaches(false);               //使用Post方式不能使用缓存

                    //设置请求体的类型是文本类型
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                    //设置请求体的长度
                    connection.setRequestProperty("Content-Length", String.valueOf(data.length));
                    //获得输出流，向服务器写入数据
                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(data);

                    int response = connection.getResponseCode();            //获得服务器的响应码
                    if (response == HttpURLConnection.HTTP_OK) {
                        Log.d("testUrl", "HTTP_OK");
                        //成功则回调onFinish
                        if (listener != null) {
                            InputStream inptStream = connection.getInputStream();
                            String result = dealResponseResult(inptStream);
                            listener.onFinish(result);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    //出现异常则回调onError
                    if (listener != null) {
                        listener.onError(e);
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    //封装的发送请求函数
    public static void sendBannerHttpRequest(Context context, final String address, final HttpCallBackListener listener) {
        if (!AppUtil.hasInternet(context)) {
            //这里写相应的网络设置处理
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
                try {
                    URL url = new URL(urls);
                    Log.d("testUrl", urls);
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
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    //成功则回调onFinish
                    if (listener != null) {
                        listener.onFinish(response.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //出现异常则回调onError
                    if (listener != null) {
                        listener.onError(e);
                    }
                } finally {
                    try {
                        if (connection != null) {
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

    /*
* Function  :   封装请求体信息
* Param     :   params请求体内容，encode编码格式
*/
    public static StringBuffer getRequestData(Map<String, String> params, String encode) {
        StringBuffer stringBuffer = new StringBuffer();        //存储封装好的请求体信息
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                stringBuffer.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), encode))
                        .append("&");
            }
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);    //删除最后的一个"&"
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer;
    }


    /*
* Function  :   处理服务器的响应结果（将输入流转化成字符串）
* Param     :   inputStream服务器的响应输入流
*/
    public static String dealResponseResult(InputStream inputStream) {
        String resultData = null;      //存储处理结果
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        try {
            while ((len = inputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, len);
            }
            resultData = new String(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.flush();
                    byteArrayOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resultData;
    }
}
