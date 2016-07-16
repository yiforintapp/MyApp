package com.leo.appmaster.mgr.service;

import android.content.Intent;
import android.text.TextUtils;

import com.leo.appmaster.AppMasterApplication;

/**
 * Created by Jasper on 2016/1/29.
 */
public class ThemeIpcHandler implements IpcHandler {
    @Override
    public Intent handleRequest(IpcRequest request) {
        String themePkg = request.pkgName;
        if (TextUtils.isEmpty(themePkg)) {
            return null;
        }

        AppMasterApplication.setSharedPreferencesValue(themePkg);
        return null;
    }
}
