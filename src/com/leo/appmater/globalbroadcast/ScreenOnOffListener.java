
package com.leo.appmater.globalbroadcast;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.LostSecurityManagerImpl;
import com.leo.appmaster.schedule.BlackListFileFetchJob;
import com.leo.appmaster.schedule.BlackUploadFetchJob;
import com.leo.appmaster.utils.LeoLog;

import java.util.ArrayList;
import java.util.List;

public class ScreenOnOffListener extends BroadcastListener {

    public static interface ScreenChangeListener {
        public void onScreenChanged(Intent intent);
    }

    public static final String TAG = "ScreenOnOffListener";
    public static final long mTwoDay = 48 * 60 * 60 * 1000;

    private static List<ScreenChangeListener> listeners = new ArrayList<ScreenChangeListener>();

    private static ScreenOnOffListener sInstance;
    private static boolean sIsScreenOn;

    public static ScreenOnOffListener instance() {
        if (sInstance == null) {
            sInstance = new ScreenOnOffListener();
        }

        return sInstance;
    }

    public static void addListener(ScreenChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static void removeListener(ScreenChangeListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    private ScreenOnOffListener() {
        AppMasterApplication ctx = AppMasterApplication.getInstance();
        PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
        sIsScreenOn = pm.isScreenOn();
    }

    public static boolean isScreenOn() {
        return sIsScreenOn;
    }

    public final void onEvent(String action) {
        if (Intent.ACTION_SCREEN_OFF.equals(action)
                || Intent.ACTION_SCREEN_ON.equals(action)
                || Intent.ACTION_USER_PRESENT.equals(action)) {

            if (Intent.ACTION_SCREEN_ON.equals(action) || Intent.ACTION_USER_PRESENT.equals(action)) {
                sIsScreenOn = true;
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                sIsScreenOn = false;
            }
            onScreenChanged(mIntent);
            for (ScreenChangeListener listener : listeners) {
                if (listener != null) {
                    listener.onScreenChanged(mIntent);
                }
            }
        }
    }

    @Override
    protected final IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        return filter;
    }

    /**
     * added, changed, removed
     */
    public void onScreenChanged(Intent intent) {
        /*检测SIM是否更换*/
        simChanagae(intent);
//        loadWifiData(intent);
        /*黑名单请求*/
        blackRequestJob(intent);

    }


    /*检测SIM是否更换*/
    public void simChanagae(Intent intent) {
        if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            /*检测SIM是否更换*/
            ThreadManager.executeOnAsyncThread(new Runnable() {
                @Override
                public void run() {
                    LostSecurityManagerImpl mgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
                    mgr.getIsSimChange();
                }
            });

        }
    }

    /**
     * 黑名单请求
     *
     * @param intent
     */
    public void blackRequestJob(Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        LeoLog.d(TAG, "blackRequestJob, action: " + action);
        if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            LeoLog.d(TAG, "blackRequestJob, exe screen on.");
            ThreadManager.executeOnAsyncThread(new Runnable() {
                @Override
                public void run() {
                    BlackUploadFetchJob.startImmediately(false);
                    BlackListFileFetchJob.startImmediately(false);
                }
            });

        }
    }
}
