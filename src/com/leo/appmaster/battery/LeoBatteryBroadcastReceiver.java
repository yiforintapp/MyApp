package com.leo.appmaster.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.mgr.BatteryManager;
import com.leo.appmaster.mgr.Manager;
import com.leo.appmaster.mgr.MgrContext;
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
        }
    }
}
