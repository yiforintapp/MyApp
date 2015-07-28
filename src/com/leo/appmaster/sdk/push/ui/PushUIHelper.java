
package com.leo.appmaster.sdk.push.ui;

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
import com.leo.appmaster.sdk.push.UserActManager;
import com.leo.appmaster.utils.LeoLog;

public class PushUIHelper {

    private final static String TAG = "PushUIHelper";
    private final static int PUSH_NOTIFICATION_ID = 2001;
    private final static String ACTION_CHECK_SUFFIX = ".leoappmaster.push.check";
    private final static String ACTION_IGNORE_SUFFIX = ".leoappmaster.push.ignore";
    private static String ACTION_CHECK_PUSH = "";
    private static String ACTION_IGNORE_PUSH = "";

    public final static String EXTRA_TITLE = "leoappmaster.push.title";
    public final static String EXTRA_CONTENT = "leoappmaster.push.content";
    public final static String EXTRA_WHERE = "leoappmaster.push.fromwhere";
    public final static String EXTRA_AD_ID = "leoappmaster.push.adid";

    private Context mContext = null;
    private NotificationManager nm = null;
    private static PushUIHelper sPushUIHelper = null;
    private UserActManager mManager = null;
    private String mTitle = null;
    private String mContent = null;
    private boolean mIsLockScreen = false;
    private NewActListener mListener;

    public static PushUIHelper getInstance(Context ctx) {
        if (sPushUIHelper == null) {
            sPushUIHelper = new PushUIHelper(ctx);
        }
        return sPushUIHelper;
    }

    private PushUIHelper(Context ctx) {
        mContext = ctx.getApplicationContext();
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

    public synchronized void setIsLockScreen(boolean flag) {
        mIsLockScreen = flag;
    }

    /* all methods which need manager MUST call after this */
    public void setManager(UserActManager manager) {
        mManager = manager;
    }

    public interface NewActListener {
        public void onNewAct(final boolean fromStatusbar, final String adID, final String title, final String content);
    }

    public void registerNewActListener(NewActListener l) {
        mListener = l;
    }

    public void unRegisterNewActListener(NewActListener l) {
        mListener = null;
    }

    public void onPush(String adID, String title, String content, int showType) {
        LeoLog.d(TAG, "title=" + title + "; content=" + content);
        mTitle = title;
        mContent = content;
        String activityName = NormalPushActivity.class.getName();

        if (showType == UserActManager.SHOW_DIALOG_FIRST && isActivityOnTop(mContext, activityName)) {
            LeoLog.d(TAG, "push activity already on top, re-layout");
            if (nm != null) {
                nm.cancel(PUSH_NOTIFICATION_ID);
            }
            if (mListener != null) {
                mListener.onNewAct(false, adID, title, content);
            }
        } else if (showType == UserActManager.SHOW_DIALOG_FIRST && isAppOnTop(mContext)
                && !mIsLockScreen) {
            LeoLog.d(TAG, "notify user with dialog");
            if (nm != null) {
                nm.cancel(PUSH_NOTIFICATION_ID);
            }
            if (mListener != null) {
                mListener.onNewAct(false, adID, title, content);
            }
            showPushActivity(adID, title, content, false);
        } else {
            LeoLog.d(TAG, "notify user with status bar");
            sendPushNotification(adID, title, content);
        }
    }

    private void showPushActivity(String id, String title, String content, boolean isFromStatusBar) {
        // Intent i = new Intent(mContext, NormalPushActivity.class);
        Intent i = null;
        i = new Intent(mContext, NormalPushActivity.class);
        i.putExtra(EXTRA_WHERE, isFromStatusBar);
        i.putExtra(EXTRA_TITLE, title);
        i.putExtra(EXTRA_CONTENT, content);
        i.putExtra(EXTRA_AD_ID, id);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mContext.startActivity(i);
    }

    /* this will be called by Activity for Push UI */
    public void sendACK(String adID, boolean isRewarded, boolean isStatusBar, String phoneNumber) {
        String rewardedStr = isRewarded ? "Y" : "N";
        String statusbarStr = isStatusBar ? "Y" : "N";
        sendACK(adID, rewardedStr, statusbarStr, phoneNumber);
    }

    private void sendACK(String adID, String rewardedStr, String statusbarStr, String phoneNumber) {
        if (mManager != null) {
            mManager.sendACK(adID, rewardedStr, statusbarStr, phoneNumber);
        }
    }

    @SuppressWarnings("deprecation")
    private void sendPushNotification(String id, String title, String content) {
        Intent intent = new Intent(ACTION_CHECK_PUSH);
        intent.putExtra(EXTRA_AD_ID, id);
        int requestCode = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        PendingIntent contentIntent = PendingIntent.getBroadcast(mContext, requestCode,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        int iconRes = R.drawable.ic_launcher_notification;

        Notification pushNotification = new Notification(
                iconRes, content,
                System.currentTimeMillis());

        Intent dIntent = new Intent(ACTION_IGNORE_PUSH);
        dIntent.putExtra(EXTRA_AD_ID, id);
        PendingIntent delIntent = PendingIntent.getBroadcast(mContext, 1,
                dIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        pushNotification.setLatestEventInfo(mContext, title, content,
                contentIntent);
        pushNotification.deleteIntent = delIntent;
        pushNotification.flags = Notification.FLAG_AUTO_CANCEL
                | Notification.FLAG_ONLY_ALERT_ONCE;
        nm.notify(PUSH_NOTIFICATION_ID, pushNotification);
    }

    private boolean isActivityOnTop(Context context, String name) {
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        if (cn.getClassName().equals(name)) {
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
            String adID = intent.getStringExtra(EXTRA_AD_ID);
            LeoLog.d(TAG, "adID=" + adID + "mTitle=" + mTitle + "; mContent= " + mContent);
            if (adID == null) {
                adID = "unknown-id";
            }
            if (action.equals(ACTION_CHECK_PUSH)) {
                nm.cancel(PUSH_NOTIFICATION_ID);
                if (mListener != null) {
                    mListener.onNewAct(true, adID, mTitle, mContent);
                }
                showPushActivity(adID, mTitle, mContent, true);
            } else if (action.equals(ACTION_IGNORE_PUSH)) {
                nm.cancel(PUSH_NOTIFICATION_ID);
                sendACK(adID, "N", "Q", "");
            }
        }
    };

}
