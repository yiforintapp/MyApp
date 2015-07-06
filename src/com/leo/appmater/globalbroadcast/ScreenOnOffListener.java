
package com.leo.appmater.globalbroadcast;


import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class ScreenOnOffListener extends BroadcastListener {

    public static final String TAG = "SCREEN ON OFF";

    public final void onEvent(String action) {
        if (Intent.ACTION_SCREEN_OFF.equals(action) 
                || Intent.ACTION_SCREEN_ON.equals(action) ||Intent.ACTION_USER_PRESENT.equals(action)) {
            onScreenChanged(mIntent);
        }
    }

    @Override
    protected final IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        return filter;
    }

    /**
     * added, changed, removed
     */
    public void onScreenChanged(Intent intent) {
    }
}
