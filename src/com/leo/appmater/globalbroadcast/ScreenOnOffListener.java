
package com.leo.appmater.globalbroadcast;

import com.leo.appmaster.utils.LeoLog;

import android.content.Intent;
import android.content.IntentFilter;

public class ScreenOnOffListener extends BroadcastListener {

    public static final String TAG = "SCREEN ON OFF";

    public final void onEvent(String action) {
        if (Intent.ACTION_SCREEN_OFF.equals(action)
                || Intent.ACTION_SCREEN_ON.equals(action)) {
            onScreenChanged(mIntent);
        }
    }

    @Override
    protected final IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        return filter;
    }

    /**
     * added, changed, removed
     */
    public void onScreenChanged(Intent intent) {
        LeoLog.d(TAG, "onScreenChanged: " + intent.getAction());
    }
}
