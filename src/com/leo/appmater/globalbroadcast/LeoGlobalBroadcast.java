
package com.leo.appmater.globalbroadcast;

import java.util.HashSet;
import java.util.Set;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.utils.LeoLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * global broadcast: package changed , sdcard, network, screen, and so on..
 * 
 * @author zhangwenyang
 */
public class LeoGlobalBroadcast {
    private static Set<BroadcastListener> sListenerList = new HashSet<BroadcastListener>();
    private static Set<String> sFilterListeners = new HashSet<String>();
    private static BasicReceiver sRecerver = new BasicReceiver();
    private static final String TAG = LeoGlobalBroadcast.class.getSimpleName();

    /**
     * register a broadcastListener , must unregister at other time.
     * 
     * @param lis
     */
    public static void registerBroadcastListener(BroadcastListener lis) {
        sListenerList.add(lis);

        if (!sFilterListeners.contains(lis.getClass().getName())) {
            sFilterListeners.add(lis.getClass().getName());
            AppMasterApplication.getInstance().registerReceiver(sRecerver, lis.getIntentFilter());
        }
    }

    /**
     * the unregister must be used with register
     * 
     * @param lis
     */
    public static void unregisterBroadcastListener(BroadcastListener lis) {
        if (sListenerList.isEmpty()) {
            sFilterListeners.clear();
            try {
                AppMasterApplication.getInstance().unregisterReceiver(sRecerver);
            } catch (Exception e) {
                if (e != null) {
                    LeoLog.e(TAG, e.getMessage());
                }
            }
        } else {
            sListenerList.remove(lis);
        }
    }

    public static class BasicReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null) {
                String action = intent.getAction();
                if(sListenerList != null) {
                    BroadcastListener[] listeners = sListenerList.toArray(new BroadcastListener[0]);
                    for (BroadcastListener lis : listeners) {
                        lis.setIntent(intent);
                        lis.onEvent(action);
                    }
                }
            }
        }
    }
}
