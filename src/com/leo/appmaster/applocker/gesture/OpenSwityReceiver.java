package com.leo.appmaster.applocker.gesture;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.leo.appmaster.utils.LeoLog;

/**
 * Created by Jasper on 2015/11/19.
 */
public class OpenSwityReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        LeoLog.i("OpenSwityReceiver", "onReceive: action: " + intent.getAction());
    }
}
