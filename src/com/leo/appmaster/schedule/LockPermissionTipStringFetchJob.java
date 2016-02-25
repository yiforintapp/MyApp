package com.leo.appmaster.schedule;

import android.content.Context;

import com.android.volley.VolleyError;
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

    @Override
    protected void onFetchSuccess(Object response, boolean noMidify) {
        super.onFetchSuccess(response, noMidify);
        LeoLog.i(TAG, "success!");
    }
    
    @Override
    protected void onFetchFail(VolleyError error) {
        super.onFetchFail(error);
        LeoLog.i(TAG, "fail!");
    }
}
