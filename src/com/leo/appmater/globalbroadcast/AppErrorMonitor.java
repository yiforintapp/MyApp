package com.leo.appmater.globalbroadcast;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.Constants;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.engine.BatteryComsuption;
import com.leo.appmaster.mgr.BatteryManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;

import java.util.List;

/**
 * Created by Jasper on 2016/4/9.
 */
public class AppErrorMonitor implements ScreenOnOffListener.ScreenChangeListener {
    private static final String TAG = "AppErrorMonitor";
    private static AppErrorMonitor mMonitor;

    private Context mContext;

    public static synchronized AppErrorMonitor get() {
        if (mMonitor == null) {
            mMonitor = new AppErrorMonitor();
        }

        return mMonitor;
    }

    private AppErrorMonitor() {
        mContext = AppMasterApplication.getInstance();
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
            checkBatteryAndReport();
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

    private void checkBatteryAndReport() {
        ThreadManager.executeOnAsyncThreadDelay(new Runnable() {
            @Override
            public void run() {
                long timeLastReport = LeoSettings.getLong(PrefConst.KEY_BATTERY_TS, 0);
                long currentTs = SystemClock.elapsedRealtime();
                if (currentTs - timeLastReport <= Constants.TIME_ONE_DAY) {
                    LeoLog.d(TAG, "<ls> check time is not hit.");
                    return;
                }
                int times = LeoSettings.getInteger(PrefConst.KEY_ACTIVITY_TIMES, 0);
                long foregroundTime = LeoSettings.getLong(PrefConst.KEY_ACTIVITY_TS, 0);

                // 重置前台展示的时间和次数
                LeoSettings.setInteger(PrefConst.KEY_ACTIVITY_TIMES, 0);
                LeoSettings.setLong(PrefConst.KEY_ACTIVITY_TS, 0);

                BatteryManager batteryManager = (BatteryManager) MgrContext.getManager(MgrContext.MGR_BATTERY);
                List<BatteryComsuption> apps = batteryManager.getBatteryDrainApps();
                if (apps == null || apps.isEmpty()) {
                    LeoLog.d(TAG, "<ls> check app list is empty.");
                    return;
                }

                StringBuilder sb = new StringBuilder();
                BatteryComsuption leoBattery = null;
                for (BatteryComsuption comsuption : apps) {
                    String pkgName = comsuption.getDefaultPackageName();
                    if (pkgName != null && pkgName.equals(mContext.getPackageName())) {
                        double percent = comsuption.getPercentOfTotal();
                        if (percent > 3) {
                            leoBattery = comsuption;
                        }
                        break;
                    }
                    int percent = (int) comsuption.getPercentOfTotal();
                    sb.append(pkgName).append("-").append(percent).append(";");
                }
                LeoLog.d(TAG, "<ls> checkBatteryAndReport, apps: " + sb.toString());
                int percent = leoBattery == null ? 0 : (int) leoBattery.getPercentOfTotal();
                if (leoBattery != null) {
                    LeoLog.d(TAG, "<ls> checkBatteryAndReport, do report battery error. ");
                    batteryManager.reportBatteryError(percent, times, foregroundTime, sb.toString());
                }

                // 存储电量上报的时间
                LeoSettings.setLong(PrefConst.KEY_BATTERY_TS, currentTs);

            }
        }, 500);
    }

}
