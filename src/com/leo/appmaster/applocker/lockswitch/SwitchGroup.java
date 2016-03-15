package com.leo.appmaster.applocker.lockswitch;

import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.ThreadManager;

/**
 * 应用锁开关抽象类
 *
 * @author nic
 */
public abstract class SwitchGroup {
    private static final String TAG = "SwitchGroup";

    protected AppMasterApplication mApp;

    SwitchGroup() {
        mApp = AppMasterApplication.getInstance();
    }

    protected abstract boolean doStrap();

}
