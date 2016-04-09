package com.leo.appmater.globalbroadcast;

import android.content.Intent;

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
    public void onScreenChanged(Intent intent) {
        String action = intent.getAction();
        LeoLog.d(TAG, "<ls> onScreenChanged...action: " + action);
        if (Intent.ACTION_SCREEN_ON.equals(action)) {
            screenOn();
        }
    }

    private void screenOn() {

    }
}
