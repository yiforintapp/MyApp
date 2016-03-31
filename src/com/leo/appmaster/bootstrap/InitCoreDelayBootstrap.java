package com.leo.appmaster.bootstrap;

import android.content.ContentResolver;
import android.content.Context;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;
import com.leo.appmaster.appmanage.business.AppBusinessManager;
import com.leo.appmaster.backup.AppBackupRestoreManager;
import com.leo.appmaster.mgr.DeviceManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.privacycontact.PrivacyContactReceiver;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.privacycontact.PrivacyMessageContentObserver;
import com.leo.appmaster.utils.LeoLog;

import java.lang.reflect.Method;

/**
 * Created by Jasper on 2016/3/30.
 */
public class InitCoreDelayBootstrap extends Bootstrap {
    private static final String TAG = "InitCoreDelayBootstrap";

    public Handler mHandler = new Handler(Looper.getMainLooper());

    private PrivacyMessageContentObserver mMessageObserver;
    private PrivacyMessageContentObserver mCallLogObserver;
    private PrivacyMessageContentObserver mContactObserver;
    private PrivacyContactReceiver mPrivacyReceiver;

    private ITelephony mITelephony;
    private AudioManager mAudioManager;

    @Override
    protected boolean doStrap() {
        long start = SystemClock.elapsedRealtime();
        registerReceiveMessageCallIntercept();
        long end = SystemClock.elapsedRealtime();
        LeoLog.i(TAG, "cost, registerReceiveMessageCallIntercept: " + (end - start));

        AppBackupRestoreManager.getInstance(mApp);

        start = SystemClock.elapsedRealtime();
        AppBusinessManager.getInstance(mApp).init();
        end = SystemClock.elapsedRealtime();
        LeoLog.i(TAG, "cost, AppBusinessManager.getInstance.init: " + (end - start));

        //init DeviceImp
        DeviceManager deviceManager = (DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE);
        deviceManager.init();
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
            mMessageObserver = new PrivacyMessageContentObserver(mApp, mHandler,
                    PrivacyMessageContentObserver.MESSAGE_MODEL);
            cr.registerContentObserver(PrivacyContactUtils.SMS_INBOXS, true,
                    mMessageObserver);
            mCallLogObserver = new PrivacyMessageContentObserver(mApp, mHandler,
                    PrivacyMessageContentObserver.CALL_LOG_MODEL);
            cr.registerContentObserver(PrivacyContactUtils.CALL_LOG_URI, true, mCallLogObserver);
            mContactObserver = new PrivacyMessageContentObserver(mApp, mHandler,
                    PrivacyMessageContentObserver.CONTACT_MODEL);
            cr.registerContentObserver(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, true,
                    mContactObserver);
        }
        openEndCall();
        mPrivacyReceiver = new PrivacyContactReceiver(mITelephony, mAudioManager);
        IntentFilter filter = new IntentFilter();
        filter.setPriority(Integer.MAX_VALUE);
        filter.addAction(PrivacyContactUtils.MESSAGE_RECEIVER_ACTION);
        filter.addAction(PrivacyContactUtils.MESSAGE_RECEIVER_ACTION2);
        filter.addAction(PrivacyContactUtils.MESSAGE_RECEIVER_ACTION3);
        filter.addAction(PrivacyContactUtils.CALL_RECEIVER_ACTION);
        filter.addAction(PrivacyContactUtils.SENT_SMS_ACTION);
        filter.addAction(PrivacyContactUtils.NEW_OUTGOING_CALL);
        mApp.registerReceiver(mPrivacyReceiver, filter);
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
