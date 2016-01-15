package com.leo.appmaster.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.leo.appmaster.mgr.BatteryManager;

/**
 * Created by stone on 16/1/15.
 */
public class BatteryNotifyHelper {

    private BatteryManager mManager;
    private Context mContext;

    public BatteryNotifyHelper(Context context, BatteryManager batteryManager) {
        mContext = context;
        mManager = batteryManager;

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };
}
