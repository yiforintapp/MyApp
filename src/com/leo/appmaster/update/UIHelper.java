
package com.leo.appmaster.update;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Debug;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.leo.appmaster.R;
import com.leoers.leoanalytics.update.IUIHelper;
import com.leoers.leoanalytics.update.UpdateManager;

public class UIHelper implements IUIHelper {

    private final static String TAG = UIHelper.class.getSimpleName();

    private static UIHelper sUIHelper = null;
    private Context mContext = null;
    private UpdateManager mManager = null;
    private OnProgressListener listener = null;

    public final static String ACTION_NEED_UPDATE = "com.leo.appmaster.update";
    public final static String ACTION_CANCEL_UPDATE = "com.leo.appmaster.update.cancel";
    public final static String ACTION_DOWNLOADING = "com.leo.appmaster.download";
    public final static String ACTION_CANCEL_DOWNLOAD = "com.leo.appmaster.update.cancel";
    private final static int UPDATE_NOTIFICATION_ID = 1002;
    private boolean mIsUpdateNotifying = false;

    private int mUIType = IUIHelper.TYPE_CHECKING;
    private int mUIParam = 0;

    private UIHelper(Context ctx) {
        mContext = ctx;
        /* new version found */
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NEED_UPDATE);
        mContext.registerReceiver(receive, filter);
        /* for cancel update */
        filter = new IntentFilter();
        filter.addAction(ACTION_CANCEL_UPDATE);
        mContext.registerReceiver(receive, filter);
        /* show downloading dialog */
        filter = new IntentFilter();
        filter.addAction(ACTION_DOWNLOADING);
        mContext.registerReceiver(receive, filter);
        /* for cancel download */
        filter = new IntentFilter();
        filter.addAction(ACTION_CANCEL_DOWNLOAD);
        mContext.registerReceiver(receive, filter);
    }

    public static UIHelper getInstance(Context ctx) {
        if (sUIHelper == null) {
            sUIHelper = new UIHelper(ctx);
        }
        return sUIHelper;
    }

    /* all function needs UpdateManager have to invoke after this call */
    public void setManager(UpdateManager manager) {
        mManager = manager;
        buildUpdatedNotification();
    }

    public void setOnProgressListener(OnProgressListener l) {
        listener = l;
    }

    private NotificationManager nm = null;
    private RemoteViews rv = null;
    private Notification updateNotification = null;

    @SuppressWarnings("deprecation")
    private void buildUpdatedNotification() {
        String appName = mContext.getString(R.string.app_name);
        String updateTip = mContext.getString(R.string.update_available, appName);
        nm = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        rv = new RemoteViews(mContext.getPackageName(),
                R.layout.sdk_notification_update);
        Intent intent = new Intent(UIHelper.ACTION_NEED_UPDATE);
        PendingIntent contentIntent = PendingIntent.getBroadcast(mContext, 0,
                intent, 0);
        updateNotification = new Notification(R.drawable.ic_launcher,
                updateTip, System.currentTimeMillis());
        Intent dIntent = new Intent(UIHelper.ACTION_CANCEL_UPDATE);
        PendingIntent delIntent = PendingIntent.getBroadcast(mContext, 0,
                dIntent, 0);
        updateNotification.deleteIntent = delIntent;
        updateNotification.setLatestEventInfo(mContext, appName, updateTip, contentIntent);
        updateNotification.flags = Notification.FLAG_AUTO_CANCEL
                | Notification.FLAG_ONGOING_EVENT;
    }

    private void sendUpdateNotification() {
        Log.d(TAG, "sending update Notification, isNotifying="
                + mIsUpdateNotifying);
        if (mIsUpdateNotifying) {
            String appName = mContext.getString(R.string.app_name);
            rv.setTextViewText(R.id.tv_title,
                    mContext.getString(R.string.update_available, appName));
            rv.setTextViewText(R.id.tv_content, mContext.getString(R.string.version_found, mManager.getVersion()));
            updateNotification.contentView = rv;
            nm.notify(UPDATE_NOTIFICATION_ID, updateNotification);
        }
    }

    @Override
    public void onNewState(int ui_type, int param) {
        mIsUpdateNotifying = true;
        mUIType = ui_type;
        mUIParam = param;
        if (ui_type == IUIHelper.TYPE_CHECK_NEED_UPDATE && !isRunningForeground(mContext)) {
            Log.e(TAG, "runing on background, show update notification");
            sendUpdateNotification();
        } else {
            showUpdateActivity(ui_type, param);
        }
    }

    private boolean isRunningForeground(Context context) {
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

    @Override
    public void onProgress(int complete, int total) {
        if (listener != null) {
            listener.onProgress(complete, total);
        }
    }

    private void showUpdateActivity(int type, int param) {
        Log.e(TAG, "showing activity type=" + type);
        Intent i = new Intent();
        i.setClass(mContext, UpdateActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        i.putExtra(LAYOUT_TYPE, type);
        i.putExtra(LAYOUT_PARAM, param);
        mContext.startActivity(i);
    }

    BroadcastReceiver receive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive action =" + action);
            if (action.equals(ACTION_NEED_UPDATE)) {
                nm.cancel(UPDATE_NOTIFICATION_ID);
                mIsUpdateNotifying = false;
                showUpdateActivity(IUIHelper.TYPE_UPDATE, mManager.getReleaseType());
            } else if (action.equals(ACTION_CANCEL_UPDATE)) {
                mManager.onCancelUpdate();
            } else if (action.equals(ACTION_DOWNLOADING)) {
                showUpdateActivity(IUIHelper.TYPE_DOWNLOADING, mManager.getReleaseType());
            } else if (action.equals(ACTION_CANCEL_DOWNLOAD)) {
                mManager.onCancelDownload();
                // TODO: how to stop showming the last progress UI
            }/* done */
        }
    };

    @Override
    public int getLayoutType() {
        return mUIType;
    }

    @Override
    public int getLayoutParam() {
        return mUIParam;
    }

}
