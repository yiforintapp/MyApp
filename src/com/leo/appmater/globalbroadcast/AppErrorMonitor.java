package com.leo.appmater.globalbroadcast;

import android.content.Intent;

import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
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
            checkLostPicAndVid();
            checkBatteryUsage();
        }
    }

    private void checkLostPicAndVid() {
        ThreadManager.executeOnAsyncThreadDelay(new Runnable() {
            @Override
            public void run() {
                PrivacyDataManager mPDManager = (PrivacyDataManager) MgrContext
                        .getManager(MgrContext.MGR_PRIVACY_DATA);
                mPDManager.checkLostPicAndVid();
            }
        }, 500);
    }

    private void checkBatteryUsage() {
//        long timeLastReport = LeoSettings.getLong()
    }

}
