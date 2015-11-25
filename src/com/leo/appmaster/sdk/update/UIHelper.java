
package com.leo.appmaster.sdk.update;

import java.text.SimpleDateFormat;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.AppTask;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.leo.analytics.update.IUIHelper;
import com.leo.analytics.update.UpdateManager;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NetWorkUtil;
import com.leo.appmaster.utils.NotificationUtil;
import com.leo.appmaster.utils.Utilities;

@SuppressLint("Instantiatable")
public class UIHelper extends BroadcastReceiver implements com.leo.analytics.update.IUIHelper {

    public final static String TAG = "UIHelper";
    public final static String TEST_TAG = "test_uihelper";
    private final static String ACTION_SHOW_REMIND_TIP = "com.leo.appmaster.update.remind";

    public static final String _LAST_SHOW_DAY = "last_show_day";
    public static final String KEY_LAST_SHOW_REMIND_TIME = "last_show_remind_time";
    public static final String KEY_CURRENT_REMIND_TIMES = "remind_count";
    public static final int RECONNECTTIME = 12 * 1000 * 60 * 60;
    // public static final int RECONNECTTIME = 1000 * 60;
    public static final int UPDATE_TIP_FRE = 10;
    public static final int UPDATE_UNLOCK_COUNT = 30;
    private static UIHelper sUIHelper = null;
    private Context mContext = null;
    private UpdateManager mManager = null;
    private OnStateChangeListener listener = null;

    // private static LeoTracker mTracker;

    private NotificationManager nm = null;
    // private RemoteViews updateRv = null;
    // private RemoteViews downloadRv = null;
    private Notification updateNotification = null;
    private Notification downloadNotification = null;

    /* modify these constants in different application */
    public final static String ACTION_NEED_UPDATE = "com.leo.appmaster.update";
    public final static String ACTION_CANCEL_UPDATE = "com.leo.appmaster.update.cancel";
    public final static String ACTION_DOWNLOADING = "com.leo.appmaster.download";
    public final static String ACTION_CANCEL_DOWNLOAD = "com.leo.appmaster.download.cancel";
    public final static String ACTION_DOWNLOAD_FAILED = "com.leo.appmaster.download.failed";
    public final static String ACTION_DOWNLOAD_FAILED_CANCEL = "com.leo.appmaster.download.failed.dismiss";

    private final static String PKG_SYSTEM_UI = "com.android.systemui";

    private final static int DOWNLOAD_NOTIFICATION_ID = 1001;
    private final static int UPDATE_NOTIFICATION_ID = 1002;
    private final static int DOWNLOAD_FAILED_NOTIFICATION_ID = 1003;

    private static final boolean DBG = false;

    private int mUIType = IUIHelper.TYPE_CHECKING;
    private int mUIParam = 0;
    private int mProgress = 0;
    /* 解锁成功的随机数 */
    public int mRandomCount;
    public static volatile boolean mUpdateTipIsFilterLock;

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
        /* show downloading diaLeoLog */
        filter = new IntentFilter();
        filter.addAction(ACTION_DOWNLOADING);
        mContext.registerReceiver(receive, filter);
        /* for cancel download */
        filter = new IntentFilter();
        filter.addAction(ACTION_CANCEL_DOWNLOAD);
        mContext.registerReceiver(receive, filter);
        /* for cancel download */
        filter = new IntentFilter();
        filter.addAction(ACTION_DOWNLOAD_FAILED);
        mContext.registerReceiver(receive, filter);
        /* for cancel download */
        filter = new IntentFilter();
        filter.addAction(ACTION_DOWNLOAD_FAILED_CANCEL);
        mContext.registerReceiver(receive, filter);
    }

    public static synchronized UIHelper getInstance(Context ctx) {
        if (sUIHelper == null) {
            sUIHelper = new UIHelper(ctx.getApplicationContext());
        }
        return sUIHelper;
    }

    /* all function needs UpdateManager have to invoke after this call */
    public void setManager(UpdateManager manager) {
        mManager = manager;
        nm = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        // buildUpdatedNotification();
        buildDownloadNotification();
    }

    public void setOnProgressListener(OnStateChangeListener l) {
        listener = l;
    }

    public void unregisterOnProgressListener(Activity activity){
        if(activity.equals(listener)){
            listener = null;
        }
    }

    @SuppressWarnings("deprecation")
    private void buildDownloadNotification() {
        String appName = mContext.getString(R.string.app_name);
        String downloadTip = mContext.getString(R.string.downloading, appName);
        CharSequence from = appName;
        CharSequence message = downloadTip;
        // downloadRv = new RemoteViews(mContext.getPackageName(),
        // R.layout.sdk_notification_download);
        // downloadRv.setTextViewText(R.id.tv_content, downloadTip);
        Intent intent = new Intent(ACTION_DOWNLOADING);
        PendingIntent contentIntent = PendingIntent.getBroadcast(mContext, 0,
                intent, 0);
        // go back to app - begin
        // Intent intent = new Intent(mContext, SDKUpdateActivity.class);
        // ComponentName componentName = new ComponentName(
        // mContext.getPackageName(), SDKUpdateActivity.class.getName());
        // intent.setComponent(componentName);
        // intent.setAction("android.intent.action.MAIN");
        // intent.addCategory("android.intent.category.LAUNCHER");
        // intent.addFlags(Notification.FLAG_ONGOING_EVENT);
        // PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
        // intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // go back to app - end
        downloadNotification = new Notification(
                R.drawable.ic_launcher_notification, downloadTip,
                System.currentTimeMillis());
        downloadNotification.setLatestEventInfo(mContext, from, message,
                contentIntent);
        NotificationUtil.setBigIcon(downloadNotification, R.drawable.ic_launcher_notification_big);

        downloadNotification.flags = Notification.FLAG_AUTO_CANCEL
                | Notification.FLAG_ONGOING_EVENT;
    }

    public void sendDownloadNotification(int progress) {
        LeoLog.d(TAG, "sendDownloadNotification called ");
        String appName = mContext.getString(R.string.app_name);
        // downloadRv.setProgressBar(R.id.pb_download, 100, progress, false);
        // downloadRv.setTextViewText(
        // R.id.tv_content,
        // mContext.getString(R.string.downloading_notification, appName,
        // progress) + "%");
        // downloadRv.setTextViewText(R.id.tv_progress, progress + "%");
        // downloadNotification.contentView = downloadRv;
        String title = mContext.getString(R.string.downloading, appName);
        String content = progress + "%";
        downloadNotification.setLatestEventInfo(mContext, title, content,
                downloadNotification.contentIntent);
        NotificationUtil.setBigIcon(downloadNotification, R.drawable.ic_launcher_notification_big);

        nm.notify(DOWNLOAD_NOTIFICATION_ID, downloadNotification);
    }

    public void cancelDownloadNotification() {
        nm.cancel(DOWNLOAD_NOTIFICATION_ID);
    }

//    private void recordRemind() {
//        SharedPreferences sp = PreferenceManager
//                .getDefaultSharedPreferences(mContext);
//        Editor editor = sp.edit();
//        editor.putLong(KEY_LAST_SHOW_REMIND_TIME, System.currentTimeMillis());
//        int remindCount = sp.getInt(KEY_CURRENT_REMIND_TIMES, 0);
//
//        LeoLog.d(TAG, "recordRemind: remindCount = " + remindCount);
//        editor.putInt(KEY_CURRENT_REMIND_TIMES, remindCount + 1);
//        editor.apply();
//    }

    @SuppressWarnings("deprecation")
    private void sendUpdateNotification() {
        if (!mManager.isFromUser()) {
            LeoLog.d(TAG, "sendUpdateNotification");
            mManager.recordRemind();
        }

        String appName = mContext.getString(R.string.app_name);
        String updateTip = mContext.getString(R.string.update_available,
                appName);
        String contentText = mContext.getString(R.string.version_found,
                mManager.getVersion());
        Intent intent = new Intent(ACTION_NEED_UPDATE);
        Intent dIntent = new Intent(UIHelper.ACTION_CANCEL_UPDATE);

        // go back to app - begin
        // Intent intent = new Intent(mContext, SDKUpdateActivity.class);
        // ComponentName componentName = new ComponentName(
        // mContext.getPackageName(), SDKUpdateActivity.class.getName());
        // intent.setComponent(componentName);
        // intent.setAction("android.intent.action.MAIN");
        // intent.addCategory("android.intent.category.LAUNCHER");
        // intent.addFlags(Notification.FLAG_ONGOING_EVENT);
        // PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
        // intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // go back to app - end

        // if (PushNotification.isFromPush_Update) {
        // PushNotification mUpdateNoti = new PushNotification(mContext);
        // mUpdateNoti.showUpdateNoti(intent, dIntent, updateTip, contentText);
        // } else {
        updateNotification = new Notification(
                R.drawable.ic_launcher_notification, updateTip,
                System.currentTimeMillis());
        PendingIntent contentIntent = PendingIntent.getBroadcast(mContext, 0,
                intent, 0);
        PendingIntent delIntent = PendingIntent.getBroadcast(mContext, 0,
                dIntent, 0);
        updateNotification.deleteIntent = delIntent;
        updateNotification.setLatestEventInfo(mContext, updateTip, contentText,
                contentIntent);
        NotificationUtil
                .setBigIcon(updateNotification, R.drawable.ic_launcher_notification_big);
        updateNotification.flags = Notification.FLAG_AUTO_CANCEL
                | Notification.FLAG_ONGOING_EVENT;
        nm.notify(UPDATE_NOTIFICATION_ID, updateNotification);
        // }
    }

    public void cancelUpdateNotification() {
        nm.cancel(UPDATE_NOTIFICATION_ID);
    }

    protected void sendDownloadFailedNotification() {
        String appName = mContext.getString(R.string.app_name);
        String failedTip = mContext.getString(R.string.download_error);
        Intent intent = new Intent(ACTION_DOWNLOAD_FAILED);
        PendingIntent contentIntent = PendingIntent.getBroadcast(mContext, 0,
                intent, 0);
        updateNotification = new Notification(
                R.drawable.ic_launcher_notification, failedTip,
                System.currentTimeMillis());
        Intent dIntent = new Intent(ACTION_DOWNLOAD_FAILED_CANCEL);
        PendingIntent delIntent = PendingIntent.getBroadcast(mContext, 0,
                dIntent, 0);
        updateNotification.deleteIntent = delIntent;
        String contentText = mContext.getString(R.string.version_found,
                mManager.getVersion());
        updateNotification.setLatestEventInfo(mContext, appName, failedTip,
                contentIntent);
        NotificationUtil.setBigIcon(updateNotification, R.drawable.ic_launcher_notification_big);

        updateNotification.flags = Notification.FLAG_AUTO_CANCEL
                | Notification.FLAG_ONGOING_EVENT;
        nm.cancel(DOWNLOAD_NOTIFICATION_ID);
        nm.notify(DOWNLOAD_FAILED_NOTIFICATION_ID, updateNotification);
    }

    public void cancelDownloadFailedNotification() {
        LeoLog.e(TAG, "cancelDownloadFailedNotification called ...........");
        nm.cancel(DOWNLOAD_NOTIFICATION_ID);
        nm.cancel(DOWNLOAD_FAILED_NOTIFICATION_ID);
    }

    @Override
    public void onNewState(int ui_type, int param) {
        mUIType = ui_type;
        mUIParam = param;
        /* 恢复记录强制升级标志的默认值 */
        AppMasterPreference.getInstance(mContext).setPGIsForceUpdate(false);
        if (ui_type == IUIHelper.TYPE_DOWNLOAD_DONE && param == UpdateManager.FORCE_UPDATE) {
            AppMasterApplication.getInstance().exitApplication();
            AppMasterPreference.getInstance(mContext).setPGIsForceUpdate(true);
        }
        if (ui_type == IUIHelper.TYPE_CHECK_NEED_UPDATE
                && !isAppOnTop(mContext) && !mManager.isFromUser()) {
            // || ui_type == IUIHelper.BACK_DOWNLOAD_DONE
            checkShowRemindNotification();
        } else {
            showUI(ui_type, param);
        }
        /* 每次发现更新升级，恢复升级提示为默认值 */
        boolean updateDefault = AppMasterPreference.getInstance(mContext)
                .getUpdateRecoveryDefaultData();
        if (!updateDefault) {
            setUnlockUpdateTipDefaultValue();
            AppMasterPreference.getInstance(mContext)
                    .setUpdateRecoveryDefaultData(true);
        }
    }

    private void checkShowRemindNotification() {
        sendUpdateNotification();
        
        /*
        LeoLog.d(TAG, "checkShowRemindNotification");
        if (mManager.isFromUser() || UpdateManager.isOneDayGetTwoNew) {
            LeoLog.d(TAG, "isFromUser, send right nows");
            sendUpdateNotification();
            UpdateManager.isOneDayGetTwoNew = false;
        } else {
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(mContext);
            int frequencyConfig = sp.getInt(UpdateManager.KEY_FREQUENCY_CONFIG,
                    0);
            int remindTimesConfig = sp.getInt(
                    UpdateManager.KEY_REMIND_TIMES_CONFIG, 0);

            int curRemindTimes = sp.getInt(KEY_CURRENT_REMIND_TIMES, 0);
            LeoLog.d(TAG, "curRemindTimes : " + curRemindTimes);

            long lastRemindTime;
            try {
                lastRemindTime = sp.getLong(KEY_LAST_SHOW_REMIND_TIME, 0);
            } catch (Exception e) {
                lastRemindTime = 0;
            }

            long currentTime = System.currentTimeMillis();
            long remindInterval = frequencyConfig * 24 * 60 * 60 * 1000;

            LeoLog.d(TAG, "not FromUser, frequencyConfig = " + frequencyConfig
                    + "    remindTimesConfig = " + remindTimesConfig);

            if (frequencyConfig <= 0 && remindTimesConfig <= 0) {
                sendUpdateNotification();
            } else {
                if (frequencyConfig > 0 && remindTimesConfig > 0) {
                    LeoLog.d(TAG, "consider frequency and remind times");
                    if (curRemindTimes >= remindTimesConfig) {
                        LeoLog.d(TAG, "curRemindCount = " + curRemindTimes
                                + "      remindTimesConfig =  "
                                + remindTimesConfig + "  so dont show remind");
                        return;
                    }
                    // consider remind times and frequency
                    if ((currentTime - lastRemindTime) < remindInterval) {
                        LeoLog.d(TAG,
                                "(currentTime - lastRemindTime) < remindInterval, so dont show remind");
                        // setRemidAlarm(0, currentTime + remindInterval
                        // - (currentTime - lastRemindTime));
                    } else if (shouldshow()) {
                        LeoLog.d(TAG,
                                "(currentTime - lastRemindTime) > remindInterval, send right now");
                        sendUpdateNotification();
                    }
                } else if (frequencyConfig > 0) { // only consider frequency
                    LeoLog.d(TAG, "only consider frequency");
                    if ((currentTime - lastRemindTime) < remindInterval) {
                        LeoLog.d(TAG,
                                "(currentTime - lastRemindTime) < remindInterval, so dont show remind");
                        // setRemidAlarm(0, currentTime + remindInterval
                        // - (currentTime - lastRemindTime));
                    } else if (shouldshow()) {
                        LeoLog.d(TAG,
                                "(currentTime - lastRemindTime) > remindInterval, send right now");
                        sendUpdateNotification();
                    }
                } else {// only consider remind times
                    LeoLog.d(TAG, "only consider frequency");
                    if (curRemindTimes > remindTimesConfig) {
                        Log.d(TAG, "curRemindCount = " + curRemindTimes
                                + "      remindTimesConfig =  "
                                + remindTimesConfig + "  so dont show remind");
                        return;
                    } else if (shouldshow()) {
                        LeoLog.d(TAG,
                                "curRemindTimes < remindTimesConfig, send right now");
                        sendUpdateNotification();
                    }
                }
            }
        }

        setLastShowDay();

        */
    }

    private void checkShowRemindActivity() {
        LeoLog.d(TAG, "checkShowRemindNotification");
        relaunchActivity(mUIType, mUIParam, true, false, null);
        
        /*
        if (mManager.isFromUser() || UpdateManager.isOneDayGetTwoNew) {
            LeoLog.d(TAG, "isFromUser, send right nows");
            relaunchActivity(mUIType, mUIParam, false, false, null);
            UpdateManager.isOneDayGetTwoNew = false;
        } else {
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(mContext);
            int frequencyConfig = sp.getInt(UpdateManager.KEY_FREQUENCY_CONFIG,
                    0);
            int remindTimesConfig = sp.getInt(
                    UpdateManager.KEY_REMIND_TIMES_CONFIG, 0);

            int curRemindTimes = sp.getInt(KEY_CURRENT_REMIND_TIMES, 0);
            long lastRemindTime = sp.getLong(KEY_LAST_SHOW_REMIND_TIME, 0);

            long currentTime = System.currentTimeMillis();
            long remindInterval = frequencyConfig * 24 * 60 * 60 * 1000;

            LeoLog.d(TAG, "not FromUser, frequencyConfig = " + frequencyConfig
                    + "    remindTimesConfig = " + remindTimesConfig);

            if (frequencyConfig <= 0 && remindTimesConfig <= 0) {
                relaunchActivity(mUIType, mUIParam, true, false, null);
            } else {
                if (frequencyConfig > 0 && remindTimesConfig > 0) {
                    LeoLog.d(TAG, "consider frequency and remind times");
                    if (curRemindTimes >= remindTimesConfig) {
                        LeoLog.d(TAG, "curRemindCount = " + curRemindTimes
                                + "      remindTimesConfig =  "
                                + remindTimesConfig + "  so dont show remind");
                        return;
                    }
                    // consider remind times and frequency
                    if ((currentTime - lastRemindTime) < remindInterval) {
                        LeoLog.d(TAG,
                                "(currentTime - lastRemindTime) < remindInterval, so dont show remind");
                        setRemidAlarm(1, currentTime + remindInterval
                                - (currentTime - lastRemindTime));
                    } else if (shouldshow()) {
                        LeoLog.d(TAG,
                                "(currentTime - lastRemindTime) > remindInterval, send right now");
                        relaunchActivity(mUIType, mUIParam, true, false, null);
                    }
                } else if (frequencyConfig > 0) { // only consider frequency
                    LeoLog.d(TAG, "only consider frequency");
                    if ((currentTime - lastRemindTime) < remindInterval) {
                        LeoLog.d(TAG,
                                "(currentTime - lastRemindTime) < remindInterval, so dont show remind");
                        setRemidAlarm(1, currentTime + remindInterval
                                - (currentTime - lastRemindTime));
                    } else if (shouldshow()) {
                        LeoLog.d(TAG,
                                "(currentTime - lastRemindTime) > remindInterval, send right now");
                        relaunchActivity(mUIType, mUIParam, true, false, null);
                    }
                } else {// only consider remind times
                    LeoLog.d(TAG, "only consider frequency");
                    if (curRemindTimes > remindTimesConfig) {
                        Log.d(TAG, "curRemindCount = " + curRemindTimes
                                + "      remindTimesConfig =  "
                                + remindTimesConfig + "  so dont show remind");
                        return;
                    } else if (shouldshow()) {
                        LeoLog.d(TAG,
                                "curRemindTimes < remindTimesConfig, send right now");
                        relaunchActivity(mUIType, mUIParam, true, false, null);
                    }
                }
            }
        }
        setLastShowDay();
        
        */
    }

    @SuppressLint("NewApi")
    private boolean isActivityOnTop(Context context) {
        if (!isAppOnTop(context)) {
            return false;
        }
        /* now our Application on top, check activity */
        if (Build.VERSION.SDK_INT > 19) {
            ActivityManager am = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            try {
                List<AppTask> tasks = am.getAppTasks();
                if (tasks != null && tasks.size() > 0) {
                    RecentTaskInfo rti = tasks.get(0).getTaskInfo();
                    if (rti != null) {
                        Intent intent = rti.baseIntent;
                        ComponentName cn = intent.getComponent();
                        if (cn != null && cn.getClassName().equals(this.getClass().getName())) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        } else {
            ActivityManager am = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
            if (cn.getClassName().equals(UpdateActivity.class.getName())) {
                return true;
            }
            return false;
        }
    }

    private boolean isAppOnTop(Context context) {
        if (Build.VERSION.SDK_INT > 19) {
            return isAppOnTopAfterLolipop(context);
        } else {
            return isAppOnTopBeforeLolipop(context);
        }
    }

    private boolean isAppOnTopBeforeLolipop(Context context) {
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

    private boolean isAppOnTopAfterLolipop(Context context) {
        // Android L and above
        String pkgName = null;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> list = am.getRunningAppProcesses();
        for (RunningAppProcessInfo pi : list) {
            if (pi.importance <= RunningAppProcessInfo.IMPORTANCE_VISIBLE // Foreground
                                                                          // or
                                                                          // Visible
                    && pi.importanceReasonCode == RunningAppProcessInfo.REASON_UNKNOWN // Filter
                                                                                       // provider
                                                                                       // and
                                                                                       // service
                    && (0x4 & pi.flags) > 0) { // Must have activities
                String pkgList[] = pi.pkgList;
                if (pkgList != null && pkgList.length > 0) {
                    if (pkgList[0].equals(PKG_SYSTEM_UI)) {
                        continue;
                    }
                    pkgName = pkgList[0];
                }
            }
        }

        if (pkgName == null || !pkgName.equals(mContext.getPackageName())) {
            return false;
        }

        return true;
    }

    public int getProgress() {
        return mProgress;
    }

    public int getComplete() {
        return mManager.getCurrentCompleteSzie();
    }

    public int getTotal() {
        return mManager.getTotalSize();
    }

    @Override
    public void onProgress(int complete, int total) {
        long c = complete;
        long t = total;
        mProgress = (total == 0) ? 0 : (int) (c * 100 / t);
        if (mProgress == 100) {
            cancelDownloadNotification();
            if (listener != null) {
                listener.onChangeState(TYPE_DISMISS, 0);
            }
            return;
        }/* download done */
        if (!isActivityOnTop(mContext)) {
            LeoLog.d(TAG, "sendDownloadNotification in onProgress of UIHelper");
            sendDownloadNotification(mProgress);
        } else {
            cancelDownloadNotification();
        }
        if (listener != null) {
            listener.onProgress(complete, total);
        }
    }

    private void showUI(int type, int param) {
        LeoLog.d(TAG, "type=" + type + "; param=" + param);
        if (isActivityOnTop(mContext) && listener != null) {
            LeoLog.d(TAG, "activity on top");
            listener.onChangeState(type, param);
        } else if (isAppOnTop(mContext) || mManager.isFromUser()) {
            // TODO check auto check update
            // relaunchActivity(type, param);
            checkShowRemindActivity();
        } else {
            showNotification(type);
        }
    }

    // private void showUI(int type, int param) {
    // LeoLog.d(TAG, "type=" + type + "; param=" + param);
    // if (isActivityOnTop(mContext) && listener != null) {
    // LeoLog.d(TAG, "activity on top");
    // listener.onChangeState(type, param);
    // } else if (isAppOnTop(mContext) || mManager.isFromUser()) {
    // if (param == UpdateManager.FORCE_UPDATE) {
    // checkShowRemindActivity();
    // } else {
    // checkShowRemindActivity();
    // }
    // } else {
    // showNotification(type);
    // }
    // }

    private void showNotification(int type) {
        switch (type) {
            case IUIHelper.TYPE_CHECK_NEED_UPDATE:
                // TODO check auto check update
                checkShowRemindNotification();
                break;
            case IUIHelper.TYPE_DOWNLOAD_FAILED:
                sendDownloadFailedNotification();
                break;
        }
    }

    private void relaunchActivity(int type, int param, boolean needRecord,
            boolean filterLockFlag/* 是否需要过滤锁 */, String lockPackage) {
        if (!mManager.isFromUser() && needRecord) {
            LeoLog.d(TAG, "relaunchActivity");
            mManager.recordRemind();
        }
        Intent i = new Intent();
        i.setClass(mContext, UpdateActivity.class);
        LeoLog.i(TAG, "升级对话框当前所在应用：" + lockPackage);
        if (lockPackage != null && !lockPackage.equals(mContext.getPackageName())) {
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            LeoLog.i(UIHelper.TAG, "需要过滤锁是启动的Activity方式！");
        } else {
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        if (filterLockFlag) {
            mUpdateTipIsFilterLock = true;
        }
        i.putExtra(LAYOUT_TYPE, type);
        i.putExtra(LAYOUT_PARAM, param);
        if(type == IUIHelper.TYPE_DOWNLOAD_FAILED){
            cancelDownloadFailedNotification();
        }
        mContext.startActivity(i);
    }

    BroadcastReceiver receive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            LeoLog.d(TAG, "onReceive action =" + action);
            try {
                if (action.equals(ACTION_NEED_UPDATE)) {
                    nm.cancel(UPDATE_NOTIFICATION_ID);
                    LeoLog.d(TAG, "recevie UPDATE_NOTIFICATION_ID");
                    relaunchActivity(IUIHelper.TYPE_UPDATE,
                            mManager.getReleaseType(), false, false, null);
                } else if (action.equals(ACTION_CANCEL_UPDATE)) {
                    mManager.onCancelUpdate();
                    if (listener != null) {
                        listener.onChangeState(TYPE_DISMISS, 0);
                    }
                } else if (action.equals(ACTION_DOWNLOADING)) {
                    LeoLog.d(TAG, "recevie UPDATE_NOTIFICATION_ID");
                    relaunchActivity(IUIHelper.TYPE_DOWNLOADING,
                            mManager.getReleaseType(), false, false, null);
                } else if (action.equals(ACTION_CANCEL_DOWNLOAD)) {
                    mManager.onCancelDownload();
                    if (listener != null) {
                        listener.onChangeState(TYPE_DISMISS, 0);
                    }
                } else if (action.equals(ACTION_DOWNLOAD_FAILED)) {
                    relaunchActivity(IUIHelper.TYPE_DOWNLOAD_FAILED, 0, false, false, null);
                } else if (action.equals(ACTION_DOWNLOAD_FAILED_CANCEL)) {
                    mManager.onCancelDownload();
                    if (listener != null) {
                        listener.onChangeState(TYPE_DISMISS, 0);
                    }
                }
            } catch (NullPointerException e) {
                // there's a situation that the application is killed by
                // notification still alive.
                // Nullpointer exception will happen in this case ,do nothing
                // when this happened
                e.printStackTrace();
            }
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

    @Override
    public void onBusy() {
        // String appname = mContext.getString(R.string.app_name);
        // Toast.makeText(mContext,
        // mContext.getString(R.string.downloading, appname),
        // Toast.LENGTH_SHORT).show();
        /* show UI corresponding to current state of download manager */
        showUI(mUIType, mUIParam);
    }

    @Override
    public void onUpdateChannel(int channel) {
        if (listener != null) {
            listener.onNotifyUpdateChannel(channel);
        }
    }

    private void setRemidAlarm(int type, long trigger) {
        LeoLog.d(TAG, "setRemidAlarm type = " + type + "    trigger = " + trigger);
        // dont need this alarm
        // AlarmManager am = (AlarmManager) mContext
        // .getSystemService(Context.ALARM_SERVICE);
        // Intent intent = new Intent(mContext, UIHelper.class);
        // intent.setAction(ACTION_SHOW_REMIND_TIP);
        // intent.putExtra("remind_type", type);
        // PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, intent,
        // 0);
        // am.cancel(pi);
        // am.set(AlarmManager.RTC_WAKEUP, trigger, pi);
    }

    // @Override
    // public void setPullUpdate() {
    // Log.d("testReceive", "setPullUpdate");
    // SharedPreferences sp = PreferenceManager
    // .getDefaultSharedPreferences(mContext);
    // sp.edit().putBoolean(UpdateManager.ISFIRSTTIME_SEND_BROCAST,
    // false).apply();
    // Intent intent = new Intent(mContext, CheckUpdateReceive.class);
    // intent.setAction(UpdateManager.RECEIVE_ACTION);
    // PendingIntent sender = PendingIntent
    // .getBroadcast(mContext, 0, intent, 0);
    //
    // // 开始时间
    // // long firstime = SystemClock.elapsedRealtime();
    // // AlarmManager am = (AlarmManager) mContext
    // // .getSystemService(Context.ALARM_SERVICE);
    // // am.cancel(sender);
    // // am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP
    // // , firstime, RECONNECTTIME, sender);
    // long firsttime = System.currentTimeMillis();
    // AlarmManager am = (AlarmManager) mContext
    // .getSystemService(Context.ALARM_SERVICE);
    // am.cancel(sender);
    // am.setRepeating(AlarmManager.RTC_WAKEUP
    // , firsttime, RECONNECTTIME, sender);
    // }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("testReceive", "action : " + action);
        if (ACTION_SHOW_REMIND_TIP.equals(action)) {
            int type = intent.getIntExtra("remind_type", -1);
            Log.d(TAG, "onReceive: type = " + type);
            if (type == 0) {
                sendUpdateNotification();
            } else if (type == 1) {
                boolean needRecord = intent.getBooleanExtra("need_record", true);
                relaunchActivity(mUIType, mUIParam, needRecord, false, null);
            }
        }
    }

    /* 解锁成功弹出升级提示 */
    public void unlockSuccessUpdateTip(String lockPackage) {
        AppMasterPreference amp = AppMasterPreference.getInstance(mContext);
        /* 是否为首次生成随机次数 */
        boolean isFirstRandow = amp.getUnlockUpdateFirstRandom();
        if (isFirstRandow) {
            /* 首次生成随机次数 */
            UIHelper.getInstance(mContext).mRandomCount = randomIn10within();
            amp.setUnlockSucessRandom(UIHelper.getInstance(mContext).mRandomCount);
            amp.setUnlockUpdateFirstRandom(false);
            LeoLog.i(TEST_TAG, "首次产生随机数：" + UIHelper.getInstance(mContext).mRandomCount);
        }
        String tipDate = amp.getUpdateTipDate();
        if (Utilities.isEmpty(tipDate)) {
            String date = getCurrentDate();
            LeoLog.i(TAG, "目前时间：" + date);
            /* 存储下当前日期 */
            amp.setUpdateTipDate(date);
            amp.setFirstUnlockCount((int) amp.getUnlockCount() - 1);
        }
        boolean isCountEnough = isUnlockCountEnough(lockPackage);
        if (isCountEnough) {
            LeoLog.i(TAG, "第" + (amp.getUnlockUpdateTipCount() + 1) + "次产生随机数！");
            /* 弹出升级对话框 */
            updateTipDialog(lockPackage);
            /* 除了首次其余的次数需要在升级提示后生成随机数 */
            UIHelper.getInstance(mContext).mRandomCount = randomIn10within();
            amp.setUnlockSucessRandom(UIHelper.getInstance(mContext).mRandomCount);
            LeoLog.i(
                    TEST_TAG,
                    "第" + (amp.getUnlockUpdateTipCount() + 1) + "次产生随机数为："
                            + UIHelper.getInstance(mContext).mRandomCount);
            /* 当天弹出次数累加 */
            amp.setUnlockUpdateTipCount(amp.getUnlockUpdateTipCount() + 1);
            /* 记录本次提示时解锁成功的次数 */
            amp.setRecordUpdateTipUnlockCount((int) amp.getUnlockCount());
        }
    }

    /* 是否满足升级提示条件 */
    @SuppressLint("SimpleDateFormat")
    private boolean isUnlockCountEnough(String lockPackage) {
        AppMasterPreference amp = AppMasterPreference.getInstance(mContext);
        int lockCount = (int) amp.getUnlockCount();
        LeoLog.i(TAG, "解锁成功次数：" + lockCount);
        int updateUnclockCount = amp.getRecordUpdateTipUnlockCount();
        int firstUnlockCount = amp.getFirstUnlockCount();
        int count = lockCount - updateUnclockCount;
        int differCount = lockCount - firstUnlockCount;
        /* 解锁成功后提示的次数 */
        int updateTipCount = amp.getUnlockUpdateTipCount();
        int random = UIHelper.getInstance(mContext).mRandomCount;
        String tipDate = amp.getUpdateTipDate();
        boolean dateChanage = true;
        if (!Utilities.isEmpty(tipDate)) {
            String date = getCurrentDate();
            /* 对比日期是否改变 */
            dateChanage = tipDate.equals(date);
        }
        if (count == random /* 判断是否达到随机次数 */
                && updateTipCount < 3 /* 是否提示小于3次 */
                && dateChanage /* 日期是否没有改变 */
                && differCount <= UPDATE_UNLOCK_COUNT/* 解锁成功是否小于30次 */) {
            return true;
        } else {
            if (!dateChanage) {
                /* 升级第二天是否提示过 */
                boolean secondTip = amp.getSecondDayTip();
                if (!secondTip) {
                    if (amp.getChanageDateUnlockCount() < 0) {
                        amp.setChanageDateUnlockCount(lockCount - 1);
                    }
                    if (amp.getRandomIn30Within() < 0) {
                        int random30 = randomIn30within();
                        if (random30 == 0) {
                            random30 = 1;
                        }
                        LeoLog.i(TEST_TAG, "升级第二天产生的随机数：" + random30);
                        amp.setRandomIn30Within(random30);
                    }
                    int difCount = lockCount - amp.getChanageDateUnlockCount();
                    int randowIn30Within = amp.getRandomIn30Within();
                    if (difCount == randowIn30Within) {
                        LeoLog.i(TAG, "第二天提示一次！");
                        amp.setSecondDayTip(true);
                        /* 弹出升级对话框 */
                        updateTipDialog(lockPackage);
                    }
                } else {
                    LeoLog.i(TAG, "第二天提示的也已经提示过了，不再提示等下个版本吧！");
                }
            }
        }
        return false;
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String date = sdf.format(System.currentTimeMillis());
        return date;
    }

    /* 每次发现更新升级，恢复升级提示为默认值 */
    private void setUnlockUpdateTipDefaultValue() {
        LeoLog.i(TAG, "初始化升级更新提示数据！");
        AppMasterPreference amp = AppMasterPreference.getInstance(mContext);
        /* 是否为首次生成随机次数 */
        amp.setUnlockUpdateFirstRandom(true);
        amp.setUnlockSucessRandom(0);
        /* 当天弹出次数累加 */
        amp.setUnlockUpdateTipCount(0);
        /* 记录本次提示时解锁成功的次数 */
        amp.setRecordUpdateTipUnlockCount(0);
        /* 为空时存下日期，不为空时不去存储，解决重复存储 */
        amp.setUpdateTipDate(null);
        /* 升级第二天是否提示过 */
        amp.setSecondDayTip(false);
        amp.setFirstUnlockCount(0);
        amp.setChanageDateUnlockCount(-1);
        amp.setRandomIn30Within(-1);

    }

    private int randomIn10within() {
        /* 解锁30次，随机弹3次，即生成10以内的随机整数,生成3次 */
        LeoLog.i(TAG, "开始产生随机数啦----------------(此处存在用于自己测试时设置的值，注意检查！)");
        int random = 1 + (int) (Math.random() * UPDATE_TIP_FRE);
        if (DBG) {
            random = 2;
        }
        return random;
    }

    private int randomIn30within() {
        /* 解锁30次，即生成30以内的随机整数 */
        LeoLog.i(TAG, "开始产生第二天的随机数啦----------------(此处存在用于自己测试时设置的值，注意检查！)");
        int random = 1 + (int) (Math.random() * UPDATE_UNLOCK_COUNT);
        if (DBG) {
            random = 2;
        }
        return random;
    }

    /* 弹出升级对话框 */
    public void updateTipDialog(String lockPackage) {
        /* 判断网络状态 */
        boolean netWorkStatus = NetWorkUtil.isNetworkAvailable(mContext);
        LeoLog.i(TAG, "当前是否有网络：" + netWorkStatus);
        /* 解锁成功弹出升级提示 */
        boolean isUnLockUpdateTip = AppMasterPreference.getInstance(mContext)
                .getVersionUpdateTipsAfterUnlockOpen();
        LeoLog.i(TEST_TAG, "是否显示升级提示对话框：" + isUnLockUpdateTip);
        if (DBG) {
            isUnLockUpdateTip = true;
        }
        LeoLog.i(TAG, "是否开启解锁成功升级提示-------(此处存在用于自己测试时设置的值，注意检查！)：" + isUnLockUpdateTip);
        if (netWorkStatus && isUnLockUpdateTip) {
            UpdateManager manager = mManager;
            try {
                if (manager != null) {
                    String appName = mContext.getString(R.string.app_name);
                    String version = manager.getVersion();
                    String feature = manager.getFeatureString();
                    int size = manager.getSize();
                    int updateType = manager.getReleaseType();

                    if (!Utilities.isEmpty(version)
                            && size > 0 && SDKWrapper.isUpdateAvailable()
                            && updateType != UpdateManager.NO_UPDATE/* 是否需要更新 */) {
                        mUIType = IUIHelper.TYPE_CHECK_NEED_UPDATE;
                        mUIParam = updateType;
                        relaunchActivity(IUIHelper.TYPE_CHECK_NEED_UPDATE,
                                mUIParam,
                                false, true, lockPackage);
                        if (UIHelper.mUpdateTipIsFilterLock) {
                            LeoLog.e(UIHelper.TEST_TAG, "应用名称：" + appName);
                            LeoLog.e(UIHelper.TEST_TAG, "版本号：" + version);
                            LeoLog.e(UIHelper.TEST_TAG, "大小：" + size);
                            if (!Utilities.isEmpty(feature)) {
                                LeoLog.e(UIHelper.TEST_TAG, "版本特性：" + feature);
                            } else {
                                LeoLog.e(UIHelper.TEST_TAG, "版本特性,为空");
                            }
                        }
                    } else {
                        LeoLog.i(TAG, "没有加载到更新日志，因此不去显示对话框！");
                    }
                }
            } catch (Exception e) {
                LeoLog.i(TAG, "没有检查到更新内容，有异常，不显示对话框！");
            }
        }
        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "update_unlock", "unlock");
    }
}
