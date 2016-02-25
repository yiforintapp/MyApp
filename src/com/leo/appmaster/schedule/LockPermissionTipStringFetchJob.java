package com.leo.appmaster.schedule;

import android.content.Context;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.HttpRequestAgent;
import com.leo.appmaster.schedule.FetchScheduleJob.FetchScheduleListener;
import com.leo.appmaster.utils.LeoLog;

public class LockPermissionTipStringFetchJob extends FetchScheduleJob {
    private static final String TAG = "LockPermissionTipStringFetchJob";
    @Override
    protected void work() {
        LeoLog.i(TAG, "do work.....");
        Context ctx = AppMasterApplication.getInstance();
        FetchScheduleListener listener = newJsonArrayListener();
        HttpRequestAgent.getInstance(ctx).requestAppUsageStateGuideString(listener, listener);
    }

}
