
package com.leo.appmaster.sdk.push;

import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;

import com.leo.appmaster.sdk.push.ui.PushUIHelper;
import com.leo.appmaster.utils.LeoLog;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
// import android.telephony.SmsManager;
// import android.telephony.TelephonyManager;

public class UserActManager {

    private final static String TAG = "UserActManager";

    public static final String ACTION_PERFORM_POLL = "_action_perform_poll_";
    public static final int ACTIVITY_POLL_INTERVAL = 4 * 60 * 60 * 1000; // 4
                                                                         // hours

    /**
     * show dialog when application running on foreground. show in statusbar
     * otherwise
     */
    public final static int SHOW_DIALOG_FIRST = 0;
    /**
     * show in statusbar no matter application on foreground or background
     */
    public final static int SHOW_STATUSBAR_ONLY = 1;

    private Context mContext = null;
    private PushUIHelper mUIHelper = null;

    private static UserActManager sInstance;

    private UserActManager(Context ctx, PushUIHelper helper) {
        mContext = ctx.getApplicationContext();
        mUIHelper = helper;
        if (mUIHelper != null) {
            mUIHelper.setManager(this);
            startPolling();
        }
    }

    public static UserActManager getInstance(Context ctx, PushUIHelper helper) {
        if (sInstance == null) {
            sInstance = new UserActManager(ctx, helper);
        }
        return sInstance;
    }

    private void startPolling() {
        LeoLog.d(TAG, "start to poll user activity every 4 hours");
        IntentFilter filter = new IntentFilter();
        filter.addAction(mContext.getPackageName() + ACTION_PERFORM_POLL);
        mContext.registerReceiver(mPushReceiver, filter);

        Intent intent = new Intent(mContext.getPackageName() + ACTION_PERFORM_POLL);
        PendingIntent pi = PendingIntent.getBroadcast(mContext, 0,
                intent, 0);
        AlarmManager alarmMgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 10 * 1000,
                // /* debug */30 * 1000, pi);
                ACTIVITY_POLL_INTERVAL, pi);
    }

    public void sendACK(String adID, String rewardedStr, String statusbarStr, String phoneNumber) {
        LeoLog.v(TAG, "sendACK to server: " + adID + rewardedStr + statusbarStr + phoneNumber);
        ArrayList<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
        pairs.add(new BasicNameValuePair("msg_id", adID));
        pairs.add(new BasicNameValuePair("phone_no", phoneNumber));
        pairs.add(new BasicNameValuePair("is_rewarded", rewardedStr));
        pairs.add(new BasicNameValuePair("is_statusbar", statusbarStr));
        LeoPollAckReq ack = new LeoPollAckReq(pairs);
        new Thread(ack).start();
    }

    private void doFetch() {
        LeoLog.d(TAG, "doFetch called");
//         if (true) { // TODO: use for debug
        if (hasGoogleAccount()) {
            LeoLog.d(TAG, "let's do fetch");
            ArrayList<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
            pairs.add(new BasicNameValuePair("msg_id", "un-used"));
            LeoPollReq pq = new LeoPollReq(pairs);
            pq.setOnPushListener(new OnPushListener() {
                @Override
                public void onPush(String id, String title, String content, int showType) {
                    /* an user activity is up now! */
                    if (mUIHelper != null) {
                        mUIHelper.onPush(id, title, content, showType);
                    }
                }
            });
            new Thread(pq).start();
        }
    }

    /* - new year activity stuff
    private boolean isSimAvailable() {
        try {
            TelephonyManager mgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            SmsManager smsManager = SmsManager.getDefault();
            return (TelephonyManager.SIM_STATE_READY == mgr.getSimState())
                    && (smsManager != null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    */

    private boolean hasGoogleAccount() {
        Account[] accounts =
                AccountManager.get(mContext).getAccountsByType("com.google");
        LeoLog.d(TAG, "account count=" + accounts.length);
        if (accounts.length > 0) {
            return true;
        } else {
            return false;
        }
    }

    private BroadcastReceiver mPushReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            String action = intent.getAction();
            if (action.equals(mContext.getPackageName() + ACTION_PERFORM_POLL)) {
                doFetch();
            }
        }
    };
}
