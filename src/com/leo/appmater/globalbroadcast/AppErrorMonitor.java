package com.leo.appmater.globalbroadcast;

import com.leo.appmaster.utils.LeoLog;

/**
 * Created by Jasper on 2016/4/9.
 */
public class AppErrorMonitor implements ScreenOnOffListener.ScreenChangeListener {
    private static final String TAG = "AppErrorMonitor";
    private static AppErrorMonitor mMonitor;

    public static synchronized AppErrorMonitor get() {
        if (mMonitor == null) {
            mMonitor = new AppErrorMonitor();
        }

        return mMonitor;
    }

    public void startMonitor() {
        ScreenOnOffListener.addListener(this);
    }

    @Override
    public void onScreenOn() {
        LeoLog.d(TAG, "<ls> onScreenOn...");
    }

    @Override
    public void onScreenOff() {
        LeoLog.d(TAG, "<ls> onScreenOff...");
    }

    @Override
    public void onUserPresent() {
        LeoLog.d(TAG, "<ls> onUserPresent...");
    }
}
