
package com.leo.appmater.globalbroadcast;

import com.leo.appmaster.utils.LeoLog;

import android.content.Intent;
import android.content.IntentFilter;

public class SDcardListener extends BroadcastListener {

    public static final String TAG = "SDCARD STATE CHANGED";

    public final void onEvent(String action) {
        if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
            onSDcardMounted(mIntent);
        } else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
            onSDcardUnmounted(mIntent);
        } else if (Intent.ACTION_MEDIA_REMOVED.equals(action)
                || Intent.ACTION_MEDIA_BAD_REMOVAL.equals(action)) {
            onSDcardRemoved(mIntent);
        } else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
            onSDcardReady(mIntent);
        } else if (Intent.ACTION_MEDIA_SHARED.equals(action)) {
            onSDcardShared(mIntent);
        }
    }

    @Override
    protected final IntentFilter getIntentFilter() {
        // TODO Auto-generated method stub
        IntentFilter filter = new IntentFilter();
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);//
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);//
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);//
        filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);//
        filter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);//
        filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);//
        filter.addAction(Intent.ACTION_MEDIA_SHARED);//
        filter.addDataScheme("file");
        return filter;
    }

    public void onSDcardMounted(Intent intent) {
        LeoLog.d(TAG, "onSDcardMounted");
    }

    public void onSDcardUnmounted(Intent intent) {
        LeoLog.d(TAG, "onSDcardUnmounted");
    }

    public void onSDcardRemoved(Intent intent) {
        LeoLog.d(TAG, "onSDcardRemoved");
    }

    public void onSDcardReady(Intent intent) {
        LeoLog.d(TAG, "onSDcardReady");
    }

    public void onSDcardShared(Intent intent) {
        LeoLog.d(TAG, "onSDcardShared");
    }

}
