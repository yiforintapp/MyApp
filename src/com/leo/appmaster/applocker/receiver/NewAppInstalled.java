
package com.leo.appmaster.applocker.receiver;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.cleanmemory.HomeBoostActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.LeoLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NewAppInstalled extends BroadcastReceiver {

    public static final long CLICKINTERVAl = 1000 * 60 * 60 * 2;

    @Override
    public void onReceive(Context context, Intent intent) {
        // 接收广播：系统启动完成后运行程序
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
        }
        // 接收广播：设备上新安装了一个应用程序包后自动启动新安装应用程序。
        if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
            String packageName = intent.getDataString().substring(8);
            // 2小时内有点击过ad icon，打点
            long mNowTime = System.currentTimeMillis();
            long mAdClickTime = AppMasterPreference.getInstance(context).getAdClickTime();
            if (mNowTime - mAdClickTime < CLICKINTERVAl) {
                Toast.makeText(context, "packageName : " + packageName, 0).show();
                SDKWrapper.addEvent(context, SDKWrapper.P1,
                        "app_act", "adunlocktop_" + packageName);
            }
        }
        // 接收广播：设备上删除了一个应用程序包。
        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
        }
    }
}
