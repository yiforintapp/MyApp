package com.zlf.appmaster.login;

/**
 * Created by Administrator on 2016/8/26.
 */
public interface HttpCallBackListener {

    void onFinish(String response);

    void onError(Exception e);
}

