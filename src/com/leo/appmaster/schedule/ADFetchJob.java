package com.leo.appmaster.schedule;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.applocker.manager.ADShowTypeRequestManager;

public class ADFetchJob extends FetchScheduleJob {

    @Override
    protected void work() {
        ADShowTypeRequestManager.getInstance(AppMasterApplication.getInstance()).loadADCheckShowType();
    }
}
