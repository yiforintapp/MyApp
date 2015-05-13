
package com.leo.appmaster.quickgestures.receiver;

import com.leo.appmaster.quickgestures.QuickSwitchManager;
import com.leo.appmaster.utils.LeoLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class QuickSwitchReceiver extends BroadcastReceiver {
    public static final String WIFI_STATE_CHANGED = "android.net.wifi.WIFI_STATE_CHANGED";
    public static final String BLUETOOTH_STATE_CHANGED = "android.bluetooth.adapter.action.STATE_CHANGED";
    private static final String BLUETOOTH_ACTION = "android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED";
    private static final String AIRPLANE_MODE = "android.intent.action.AIRPLANE_MODE";
    private static final String NETWORK_CHANGE = "android.intent.action.ANY_DATA_STATE";
    public static final String RINGER_MODE_CHANGED = "android.media.RINGER_MODE_CHANGED";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WIFI_STATE_CHANGED.equals(action)) {
            LeoLog.d("QuickSwitchReceiver", "wifi change");
            QuickSwitchManager.getInstance(context).wlan();
        } else if (BLUETOOTH_STATE_CHANGED.equals(action) || BLUETOOTH_ACTION.equals(action)) {
            LeoLog.d("QuickSwitchReceiver", "蓝牙 change");
            QuickSwitchManager.getInstance(context).blueTooth();
        } else if (AIRPLANE_MODE.equals(action)) {
            LeoLog.d("QuickSwitchReceiver", "飞行 change");
            QuickSwitchManager.getInstance(context).flyMode();
        } else if (NETWORK_CHANGE.equals(action)) {
            LeoLog.d("QuickSwitchReceiver", "数据网络 change");
            QuickSwitchManager.getInstance(context).mobileData();
        } else if (RINGER_MODE_CHANGED.equals(action)) {
            LeoLog.d("QuickSwitchReceiver", "音量 change");
            QuickSwitchManager.getInstance(context).sound();
        }
    }

}
