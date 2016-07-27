package com.zlf.appmaster.bootstrap;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;
import com.zlf.appmaster.ThreadManager;
import com.zlf.appmaster.utils.LeoLog;

import java.lang.reflect.Method;

/**
 * Created by Jasper on 2016/3/30.
 */
public class InitCoreDelayBootstrap extends com.zlf.appmaster.bootstrap.Bootstrap {
    private static final String TAG = "InitCoreDelayBootstrap";

    public Handler mHandler = new Handler(Looper.getMainLooper());


    public static ITelephony mITelephony;
    private AudioManager mAudioManager;

    @Override
    protected boolean doStrap() {
        LeoLog.d(TAG, "<ls> doStrap, curr_tid:" + Thread.currentThread().getId() + " | main_tid:" + ThreadManager.getMainThreadId());
        long start = SystemClock.elapsedRealtime();
        long end = SystemClock.elapsedRealtime();
        LeoLog.i(TAG, "cost, iniSDK: " + (end - start));

        start = SystemClock.elapsedRealtime();
        registerReceiveMessageCallIntercept();
        end = SystemClock.elapsedRealtime();
        LeoLog.i(TAG, "cost, registerReceiveMessageCallIntercept: " + (end - start));


        start = SystemClock.elapsedRealtime();
        end = SystemClock.elapsedRealtime();
        LeoLog.i(TAG, "cost, AppBusinessManager.getInstance.init: " + (end - start));

        return false;
    }

    @Override
    public String getClassTag() {
        return TAG;
    }

    /**
     * 短信拦截, 电话拦截
     */
    private void registerReceiveMessageCallIntercept() {
        ContentResolver cr = mApp.getContentResolver();
        if (cr != null) {

        }
        openEndCall();
    }

    // 打开endCall
    private void openEndCall() {
        mAudioManager = (AudioManager) mApp.getSystemService(Context.AUDIO_SERVICE);
        TelephonyManager mTelephonyManager = (TelephonyManager) mApp
                .getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method getITelephonyMethod = TelephonyManager.class.getDeclaredMethod("getITelephony",
                    (Class[]) null);
            getITelephonyMethod.setAccessible(true);
            mITelephony = (ITelephony) getITelephonyMethod.invoke(mTelephonyManager,
                    (Object[]) null);
        } catch (Exception e) {
        }
    }
}
