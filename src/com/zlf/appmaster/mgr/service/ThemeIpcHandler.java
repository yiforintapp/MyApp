package com.zlf.appmaster.mgr.service;

import android.content.Intent;
import android.text.TextUtils;

import com.zlf.appmaster.AppMasterApplication;

/**
 * Created by Jasper on 2016/1/29.
 */
public class ThemeIpcHandler implements IpcHandler {
    @Override
    public Intent handleRequest(com.zlf.appmaster.mgr.service.IpcRequest request) {
        String themePkg = request.pkgName;
        if (TextUtils.isEmpty(themePkg)) {
            return null;
        }

        AppMasterApplication.setSharedPreferencesValue(themePkg);
        return null;
    }
}
