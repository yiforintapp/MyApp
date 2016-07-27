package com.zlf.appmaster.mgr.service;

import android.content.Intent;

/**
 * Created by Jasper on 2015/9/29.
 */
public class IpcRequest {
    private static final String TAG = "IpcRequest";
    /**
     * 唯一标示一次请求
     */
    public int id;
    /**
     * 命令字，标示请求什么业务接口
     */
    public String command;

    /**
     * 请求者包名
     */
    public String pkgName;
    /**
     * 请求参数
     */
    public Intent data;

    @Override
    public String toString() {
        return "id: " + id + " | cmd: " + command + " | pkg: " + pkgName + " | data: " + data;
    }
}
