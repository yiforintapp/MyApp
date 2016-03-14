package com.leo.appmaster.battery;

import java.lang.reflect.Method;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.leo.appmaster.mgr.BatteryManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.LeoLog;

/**
 * Created by stone on 16/3/7.
 */
public class LeoBatteryBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equalsIgnoreCase(BatteryNotifyHelper.ACTION_LEO_SAVER_NOTIFI_CLICKEDX)) {
            LeoLog.d("BatteryNotifyHelper - LeoBatteryBroadcastReceiver", "receive: " + action);
            BatteryManager btm = (BatteryManager) MgrContext.getManager(MgrContext.MGR_BATTERY);
            btm.onSaverNotifiClick();
            collapseStatusBar(context.getApplicationContext());
        }
        SDKWrapper.addEvent(context.getApplicationContext(),
                SDKWrapper.P1, "batterypage", "notify_click");
    }

    private static void collapseStatusBar(Context context) {
        try {
            Object statusBarManager = context.getSystemService("statusbar");
            Method collapse;
            if (Build.VERSION.SDK_INT <= 16) {
                collapse = statusBarManager.getClass().getMethod("collapse");
            } else {
                collapse = statusBarManager.getClass().getMethod("collapsePanels");
            }
            collapse.invoke(statusBarManager);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }
}
