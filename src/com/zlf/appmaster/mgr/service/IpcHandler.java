package com.zlf.appmaster.mgr.service;

import android.content.Intent;

/**
 * 请求处理接口
 * Created by Jasper on 2015/9/29.
 */
interface IpcHandler {
    public Intent handleRequest(IpcRequest request);
}
