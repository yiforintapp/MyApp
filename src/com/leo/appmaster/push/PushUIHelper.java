
package com.leo.appmaster.push;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.LeoLog;
import com.leoers.leoanalytics.push.IPushUIHelper;
import com.leoers.leoanalytics.push.PushManager;

public class PushUIHelper implements IPushUIHelper {

    private final static String TAG = "PushUIHelper";
    private final static int PUSH_NOTIFICATION_ID = 2001;
    private final static String ACTION_CHECK_SUFFIX = ".leoappmaster.push.check";
    private final static String ACTION_IGNORE_SUFFIX = ".leoappmaster.push.ignore";
    private static String ACTION_CHECK_PUSH = "";
    private static String ACTION_IGNORE_PUSH = "";

    public final static String EXTRA_TITLE = "leoappmaster.push.title";
    public final static String EXTRA_CONTENT = "leoappmaster.push.content";
    public final static String EXTRA_WHERE = "leoappmaster.push.fromwhere";

    private Context mContext = null;
    private NotificationManager nm = null;
    private static PushUIHelper sPushUIHelper = null;
    private PushManager mManager = null;
    private String mTitle = null;
    private String mContent = null;
    /* had status bar shown? */
    private boolean mStatusBar = false;

    public static PushUIHelper getInstance(Context ctx) {
        if (sPushUIHelper == null) {
            sPushUIHelper = new PushUIHelper(ctx);
        }
        return sPushUIHelper;
    }

    private PushUIHelper(Context ctx) {
        mContext = ctx;
        nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        ACTION_CHECK_PUSH = ctx.getPackageName() + ACTION_CHECK_SUFFIX;
        ACTION_IGNORE_PUSH = ctx.getPackageName() + ACTION_IGNORE_SUFFIX;

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CHECK_PUSH);
        mContext.registerReceiver(mReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(ACTION_IGNORE_PUSH);
        mContext.registerReceiver(mReceiver, filter);
    }

    /* all methods which need manager MUST call after this */
    @Override
    public void setManager(PushManager manager) {
        mManager = manager;
    }

    @Override
    public void onPush(String title, String content) {
        LeoLog.d(TAG, "title=" + title + "; content=" + content);
        mTitle = title;
        mContent = content;
        if (isActivityOnTop(mContext)) {
            LeoLog.d(TAG, "push activity already on top, do nothing");
        } else if (isAppOnTop(mContext)) {
            mStatusBar = false;
            showPushActivity(title, content, false);
        } else {
            mStatusBar = true;
            sendPushNotification(title, content);
        }
    }

    private void showPushActivity(String title, String content, boolean isFromStatusBar) {
        Intent i = new Intent(mContext, PushActivity.class);
        i.putExtra(EXTRA_WHERE, isFromStatusBar);
        i.putExtra(EXTRA_TITLE, title);
        i.putExtra(EXTRA_CONTENT, content);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mContext.startActivity(i);
    }

    /* this will be called by Activity for Push UI */
    public void sendACK(boolean isRewarded, String phoneNumber) {
        String rewardedStr = isRewarded ? "Y" : "N";
        String statusbarStr = mStatusBar ? "Y" : "N";
        sendACK(rewardedStr, statusbarStr, phoneNumber);
    }

    private void sendACK(String rewardedStr, String statusbarStr, String phoneNumber) {
        if (mManager != null) {
            mManager.sendACK(rewardedStr, statusbarStr, phoneNumber);
        }
    }

    @SuppressWarnings("deprecation")
    private void sendPushNotification(String title, String content) {
        Intent intent = new Intent(ACTION_CHECK_PUSH);
        PendingIntent contentIntent = PendingIntent.getBroadcast(mContext, 0,
                intent, 0);

        Notification pushNotification = new Notification(
                R.drawable.ic_launcher_notification, content,
                System.currentTimeMillis());

        Intent dIntent = new Intent(ACTION_IGNORE_PUSH);
        PendingIntent delIntent = PendingIntent.getBroadcast(mContext, 0,
                dIntent, 0);

        pushNotification.deleteIntent = delIntent;
        pushNotification.setLatestEventInfo(mContext, title, content,
                contentIntent);
        pushNotification.flags = Notification.FLAG_AUTO_CANCEL
                // | Notification.FLAG_ONGOING_EVENT;
                | Notification.FLAG_ONLY_ALERT_ONCE;
        nm.notify(PUSH_NOTIFICATION_ID, pushNotification);
    }

    private boolean isActivityOnTop(Context context) {
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        if (cn.getClassName().equals(PushActivity.class.getName())) {
            return true;
        }
        return false;
    }

    private boolean isAppOnTop(Context context) {
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        String currentPackageName = cn.getPackageName();
        if (!TextUtils.isEmpty(currentPackageName)
                && currentPackageName.equals(context.getPackageName())) {
            return true;
        }
        return false;
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_CHECK_PUSH)) {
                nm.cancel(PUSH_NOTIFICATION_ID);
                LeoLog.d(TAG, "mTitle=" + mTitle + "; mContent= " + mContent);
                showPushActivity(mTitle, mContent, true);
            } else if (action.equals(ACTION_IGNORE_PUSH)) {
                nm.cancel(PUSH_NOTIFICATION_ID);
                sendACK("N", "Q", "");
            }
        }
    };

}
