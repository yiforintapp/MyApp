
package com.leo.appmaster.download;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.widget.RemoteViews;

import java.util.HashMap;
import java.util.Set;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NotificationUtil;
import com.leo.appmaster.utils.PhoneInfoStateManager;
import com.leo.appmaster.utils.StorageUtil;

public class DownloadNotifManager {
    static final String TAG = "DownloadNotification";

    private HashMap<Long, NotificationItem> mProgressNotifMap;
    private HashMap<Long, NotificationItem> mSuccessNotifMap;
    private HashMap<Long, NotificationItem> mFailNotifMap;

    Context mContext;
    DownloadReceiver receiver;

    private static DownloadNotifManager mInstance;

    public static class NotificationItem {
        /** mtitles. */
        private String mTitle; // download title.
        /** Ticker text */
        public Intent mIntent = null;
    }

    public static synchronized DownloadNotifManager getInstance(Context aContext) {
        if (mInstance == null) {
            mInstance = new DownloadNotifManager(aContext);
        }
        return mInstance;
    }

    public void init() {
        receiver = new DownloadReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_DOWNLOAD_PROGRESS);
        filter.addAction(Constants.ACTION_DOWNLOAD_COMPOLETED);
        mContext.registerReceiver(receiver, filter);
    }

    public void unInit() {
        mContext.unregisterReceiver(receiver);
    }

    private DownloadNotifManager(Context ctx) {
        mContext = ctx;
        mProgressNotifMap = null;
        mSuccessNotifMap = null;
        mFailNotifMap = null;
    }

    private static class DownloadReceiver extends BroadcastReceiver {
        public static final String TAG = "DownloadReceiver";

        @Override
        public void onReceive(final Context context, Intent intent) {
            final String action = intent.getAction();
            long id = intent.getLongExtra(Constants.EXTRA_ID, -1);
            if (id != -1) {
                if (action.equals(Constants.ACTION_DOWNLOAD_PROGRESS)) {
                    long total = intent.getLongExtra(Constants.EXTRA_TOTAL, 0);
                    long current = intent.getLongExtra(Constants.EXTRA_CURRENT,
                            0);

                    DownloadNotifManager.getInstance(context)
                            .updateProgressNotification(id, total, current);
                }
                if (action.equals(Constants.ACTION_DOWNLOAD_COMPOLETED)) {
                    int result = intent.getIntExtra(Constants.EXTRA_RESULT, -1);
                    DownloadNotifManager.getInstance(context)
                            .updateCompletedNotification(id, result);
                }
            }
        }
    }

    /**
     * add notification when need, you can specify which notification should be
     * show, and the intent when the notification be clicked.
     * 
     * @param aId : download task id
     * @param aShowProgressNotif : does the progress notification should be show
     * @param aProgressIntent : intent when the progress notification be
     *            clicked,if it be null, there will be a default intent, which
     *            will show a DownloadAlert.
     * @param aShowSuccessNotif : does the download success notification should
     *            be show
     * @param aSuccessIntent : the intent, if it be null, nothing will happen
     *            when click the notification
     * @param aShowFailNotif : does the download fail notification should be
     *            show
     * @param aFailIntent : the intent, if it be null, nothing will happen when
     *            click the notification
     */
    public void addNotification(final long aId,
            final boolean aShowProgressNotif, Intent aProgressIntent,
            final boolean aShowSuccessNotif, final Intent aSuccessIntent,
            final boolean aShowFailNotif, final Intent aFailIntent) {

        LeoLog.e(TAG, "add " + aId);
        if (aId >= 0) {

            String[] projection = new String[] {
                    Constants.COLUMN_DOWNLOAD_TITLE,
                    Constants.COLUMN_DOWNLOAD_MIME_TYPE,
                    Constants.COLUMN_DOWNLOAD_TOTAL_SIZE,
                    Constants.COLUMN_DOWNLOAD_CURRENT_SIZE
            };

            String selection = Constants.ID + "=" + aId;

            Cursor cursor = mContext.getContentResolver().query(
                    Constants.DOWNLOAD_URI, projection, selection, null,
                    "_id DESC");

            String title = "", mime_type = "";
            long totalSize = 0, currentSize = 0;

            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                title = cursor.getString(cursor
                        .getColumnIndex(Constants.COLUMN_DOWNLOAD_TITLE));
                mime_type = cursor.getString(cursor
                        .getColumnIndex(Constants.COLUMN_DOWNLOAD_MIME_TYPE));

                totalSize = cursor.getLong(cursor
                        .getColumnIndex(Constants.COLUMN_DOWNLOAD_TOTAL_SIZE));
                currentSize = cursor.getLong(cursor
                        .getColumnIndex(Constants.COLUMN_DOWNLOAD_CURRENT_SIZE));
            }

            if (cursor != null) {
                cursor.close();
            }

            if (aShowProgressNotif) {
                if (mProgressNotifMap == null) {
                    mProgressNotifMap = new HashMap<Long, NotificationItem>();
                }
                if (aProgressIntent == null) {
//                    aProgressIntent = new Intent(mContext,
//                            DownloadActivity.class);

                    aProgressIntent.putExtra(Constants.EXTRA_TITLE, title);
                    aProgressIntent.putExtra(Constants.EXTRA_ID, aId);
                    aProgressIntent.putExtra(Constants.EXTRA_TOTAL, totalSize);
                    aProgressIntent.putExtra(Constants.EXTRA_CURRENT,
                            currentSize);
                    aProgressIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                addNotification(aId, title, aProgressIntent, mProgressNotifMap);
            }

            if (aShowSuccessNotif) {
                if (mSuccessNotifMap == null) {
                    mSuccessNotifMap = new HashMap<Long, NotificationItem>();
                }
                addNotification(aId, title, aSuccessIntent, mSuccessNotifMap);
            }

            if (aShowFailNotif) {
                if (mFailNotifMap == null) {
                    mFailNotifMap = new HashMap<Long, NotificationItem>();
                }
                addNotification(aId, title, aFailIntent, mFailNotifMap);
            }
        }
    }

    /**
     * show progress notification atOnce
     * 
     * @param aId
     */
    public void showProgressNotifAtOnce(long aId) {
        if (!StorageUtil.IsSdCardMounted()
                || !PhoneInfoStateManager.isNetworkConnectivity(mContext)) {
            LeoLog.e(TAG, "sd card not mounted or network not connected");
            return;

        }
        updateProgressNotification(aId, 100, 0);
    }

    private void addNotification(long id, String title, Intent intent,
            HashMap<Long, NotificationItem> notifMap) {
        if (notifMap == null)
            return;
        if (intent == null) {
            intent = new Intent();
        }
        NotificationItem item = new NotificationItem();
        item.mTitle = title;
        item.mIntent = intent;
        notifMap.put(id, item);
    }

    public void updateProgressNotification(long aId, long total, long current) {
        cancelInitFialedNotif();
        if (mProgressNotifMap == null)
            return;
        int progress = -1;
        if (total > 0) {
            if (total > current) {
                progress = (int) (current * 100 / total);
            }
        }
        if (progress < 0) {
            return;
        }

        NotificationItem ni = mProgressNotifMap.get(aId);
        if (ni != null) {

            int iconResource = R.drawable.ic_launcher_notification;
            Notification notif = new Notification();

            ni.mIntent.putExtra(Constants.EXTRA_PROGRESS, progress);
            ni.mIntent.putExtra(Constants.EXTRA_CURRENT, current);
            ni.mIntent.putExtra(Constants.EXTRA_TOTAL, total);
            PendingIntent contentIntent = PendingIntent.getActivity(mContext,
                    0, ni.mIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            notif.icon = iconResource;
            String appName = mContext.getString(R.string.app_name);
            notif.tickerText = mContext
                    .getString(R.string.downloading, appName);
            notif.contentIntent = contentIntent;
            notif.flags = Notification.FLAG_ONGOING_EVENT
                    | Notification.FLAG_AUTO_CANCEL;

//            RemoteViews downloadRv = new RemoteViews(mContext.getPackageName(),
//                    R.layout.sdk_notification_download);
//            downloadRv.setTextViewText(R.id.tv_title, notif.tickerText);
//            downloadRv.setTextViewText(
//                    R.id.tv_content,
//                    mContext.getString(R.string.downloading_notification,
//                            appName, progress) + "%");
//            downloadRv.setProgressBar(R.id.pb_download, 100, progress, false);
            notif.setLatestEventInfo(mContext, notif.tickerText,
                    mContext.getString(R.string.downloading_notification,
                            appName, progress) + "%", contentIntent);
            NotificationUtil.setBigIcon(notif, R.drawable.ic_launcher_notification_big);
            notif.when = System.currentTimeMillis();
            NotificationManager nm = (NotificationManager) mContext
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify((int) aId, notif);
        }
    }

    public void canlelNotif(long aId) {
        if (mProgressNotifMap != null && mProgressNotifMap.containsKey(aId)) {
            mProgressNotifMap.remove(aId);
        }

        NotificationManager nm = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel((int) aId);
    }

    public void cancelCompleteNotif(long aId) {
        if (mSuccessNotifMap != null && mSuccessNotifMap.containsKey(aId)) {
            mSuccessNotifMap.remove(aId);
        }
        if (mFailNotifMap != null && mFailNotifMap.containsKey(aId)) {
            mFailNotifMap.remove(aId);
        }
    }

    public void cancelInitFialedNotif() {
        canlelNotif(0);
    }

    public void cancelAllFailedNotif() {
        if (mFailNotifMap != null) {
            Set<Long> set = mFailNotifMap.keySet();
            for (Long aid : set) {
                canlelNotif(aid);
            }
            mFailNotifMap.clear();
        }
    }

    public void updateCompletedNotification(long aId, int aResult) {
        canlelNotif(aId);

        if (aResult == Constants.RESULT_FAILED_SDCARD
                || aResult == Constants.RESULT_CANCELLED
                || aResult == Constants.RESULT_FAILED_NO_NETWORK
                || aResult == Constants.RESULT_FAILED_SDCARD_INSUFFICIENT) {
            return;
        }

        HashMap<Long, NotificationItem> notifMap = null;
        if (aResult == Constants.RESULT_SUCCESS) {
            notifMap = mSuccessNotifMap;
        } else if (aResult == Constants.RESULT_FAILED) {
            notifMap = mFailNotifMap;
        }

        if (notifMap == null)
            return;

        NotificationItem ni = notifMap.get(aId);
        if (ni != null) {
            String ticker = "", text = "";
            if (aResult == Constants.RESULT_FAILED) {
                text = mContext.getString(R.string.downloaded_failure);
                ticker = ni.mTitle
                        + mContext.getResources().getString(
                                R.string.downloaded_failure, "");
            } else if (aResult == Constants.RESULT_SUCCESS) {
                text = mContext.getString(R.string.downloaded_success);
                ticker = ni.mTitle
                        + mContext.getResources().getString(
                                R.string.downloaded_success, "");
                mContext.startActivity(ni.mIntent);
                cancelCompleteNotif(aId);
                return;
            }
            int iconResource = R.drawable.ic_launcher_notification;
            Notification notification = new Notification(iconResource, ticker,
                    System.currentTimeMillis());
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            PendingIntent pendingIntent = null;
            if (ni.mIntent != null) {
                pendingIntent = PendingIntent.getActivity(mContext, 0,
                        ni.mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
            notification.setLatestEventInfo(mContext, ni.mTitle, text,
                    pendingIntent);
            NotificationUtil.setBigIcon(notification, R.drawable.ic_launcher_notification_big);

            NotificationManager nm = (NotificationManager) mContext
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify((int) aId, notification);
        }
        cancelCompleteNotif(aId);
    }
}
