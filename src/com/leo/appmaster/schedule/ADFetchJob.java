package com.leo.appmaster.schedule;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.applocker.manager.ADShowTypeRequestManager;
import com.leo.appmaster.utils.LeoLog;

/**
 * 广告拉去，父类维护启动类别，通过反射调用
 */
public class ADFetchJob extends FetchScheduleJob {

    @Override
    protected void work() {
        LeoLog.i(getJobKey(), "do work...");
        FetchScheduleListener listener = newJsonObjListener();
        ADShowTypeRequestManager.getInstance(AppMasterApplication.getInstance()).loadADCheckShowType(listener);
    }

//    @Override
//    protected int getPeriod() {
//        return 10 * 1000;
//    }
//
//    @Override
//    protected int getFailPeriod() {
//        return 10 * 1000;
//    }
}
