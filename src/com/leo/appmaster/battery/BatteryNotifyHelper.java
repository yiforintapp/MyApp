package com.leo.appmaster.battery;

import java.util.List;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.engine.BatteryComsuption;
import com.leo.appmaster.home.DeskProxyActivity;
import com.leo.appmaster.mgr.BatteryManager;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.BitmapUtils;
import com.leo.appmaster.utils.LeoLog;


/**
 * Created by stone on 16/1/15.
 */
public class BatteryNotifyHelper {

    private static final String TAG = "BatteryNotifyHelper";
    private static final int NOTIFICATION_ID = 20160116;
    private static final int SAVER_NOTIFICATION_ID = 16030110;
    private static final int MAX_ICON_ACCOUNT = 6;
    private static final String ACTION_LEO_BATTERY_APP =
            "com.leo.appmaster.battery.notification.action";
    private static final String ACTION_LEO_SAVER_NOTIFI_CLICKED =
            "com.leo.appmaster.battery.saver.notifi.click.action";
    public static final String ACTION_LEO_SAVER_NOTIFI_CLICKEDX =
            "com.leo.appmaster.battery.saver.notifi.click.action.x";
    private static final int CHECK_INTERVAL = 3 * 60 * 60 * 1000;
    //    private static final int CHECK_INTERVAL = 10 * 1000;
    private BatteryManager mManager;
    private Context mContext;

    private boolean mNeedUpdateLevel = false;

    private static BatteryNotifyHelper sInstance;
    private BatteryNotifyHelper(Context context, BatteryManager batteryManager) {
        mContext = context;
        mManager = batteryManager;

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_LEO_BATTERY_APP);
        filter.addAction(ACTION_LEO_SAVER_NOTIFI_CLICKED);
        mContext.registerReceiver(mReceiver, filter);

        fireTimerAction();
    }

    public static BatteryNotifyHelper getInstance (Context context, BatteryManager batteryManager) {
        if (sInstance == null) {
            sInstance = new BatteryNotifyHelper(context, batteryManager);
        }
        return sInstance;
    }

    private void fireTimerAction() {
        long nextTime = CHECK_INTERVAL - SystemClock.elapsedRealtime() % CHECK_INTERVAL;
        LeoLog.d(TAG, "check after " + nextTime + " ms");
        Intent intent = new Intent(ACTION_LEO_BATTERY_APP);
        PendingIntent sendIntent = PendingIntent.getBroadcast(mContext, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sendIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                nextTime, CHECK_INTERVAL, sendIntent);
    }

    private void notifyAppConsumption(List<BatteryComsuption> list) {
        if (!AppUtil.notifyAvailable()) {
            return;
        }
        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "batterypage", "comsuption_ntf");
        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        RemoteViews view_custom;
        view_custom = new RemoteViews(mContext.getPackageName(), R.layout.battery_apps_notify);
        int[] imageViewIds = {
                R.id.iv_app1, R.id.iv_app2, R.id.iv_app3,
                R.id.iv_app4, R.id.iv_app5, R.id.iv_app6
        };

        if (list != null) {
            Drawable icon = null;
            Bitmap bitmap = null;
            int finalSize = Math.min(MAX_ICON_ACCOUNT, list.size());
            for (int i = 0; i < finalSize; i++) {
                icon = list.get(i).getIcon();
                bitmap = BitmapUtils.drawableToBitmap(icon);
                if (bitmap != null) {
                    view_custom.setImageViewBitmap(imageViewIds[i], bitmap);
                }
            }
        }

        view_custom.setTextViewText(R.id.app_number, list.size() + "");

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setContent(view_custom)
                .setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
                .setTicker(mContext.getApplicationContext().getString(R.string.batterymanage_switch_noti))
                .setPriority(Notification.PRIORITY_DEFAULT)// 设置该通知优先级
                .setOngoing(false)// 不是正在进行的 true为正在进行 效果和.flag一样
                .setSmallIcon(R.drawable.statusbar_battery_icon)
                .setAutoCancel(true);

        Intent intent = new Intent(mContext,
                DeskProxyActivity.class);
        intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, DeskProxyActivity.IDX_ELEC);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(mContext, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);

        // mNotificationManager.notify(notifyId, mBuilder.build());
        Notification notify = mBuilder.build();
        notify.contentView = view_custom;
        mNotificationManager.notify(NOTIFICATION_ID, notify);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equalsIgnoreCase(ACTION_LEO_SAVER_NOTIFI_CLICKED)) {
                /* 屏保通知点击广播 */
                LeoLog.d(TAG, "receive ACTION_LEO_SAVER_NOTIFI_CLICKED");
                SDKWrapper.addEvent(context.getApplicationContext(),
                        SDKWrapper.P1, "batterypage", "notify_click");
                mManager.onSaverNotifiClick();
            } else if (action.equalsIgnoreCase(ACTION_LEO_BATTERY_APP)) {
                /* 后台耗电定时广播 */
                if (mManager.shouldNotify()) {
                    ThreadManager.executeOnAsyncThread(new Runnable() {
                        @Override
                        public void run() {
                            final List<BatteryComsuption> list = mManager.getBatteryDrainApps();
                            LeoLog.d(TAG, "apps count: " + list.size()
                                    + "/" + mManager.getAppThreshold());
                            if (list.size() > mManager.getAppThreshold()) {
                                ThreadManager.executeOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        notifyAppConsumption(list);
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
    };

    public void showNotificationForScreenSaver(int level) {
        showNotificationForScreenSaver(level, false);
    }

    /* 3.3.2 充电屏保通知 */
    public void showNotificationForScreenSaver(int level, boolean onSystemLockScreen) {
        LeoLog.d("stone_saver", "in showNotificationForScreenSaver, level = " + level);
        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        RemoteViews view_custom;
        view_custom = new RemoteViews(mContext.getPackageName(), R.layout.battery_saver_notify);

        view_custom.setTextViewText(R.id.tv_charge_percent, level + "");

        if (level == 100) {
            view_custom.setTextViewText(R.id.tv_charge_tip,
                    mContext.getString(R.string.screen_protect_charing_text_four));
        } else {
            view_custom.setTextViewText(R.id.tv_charge_tip,
                    mContext.getString(R.string.screen_protect_charing_text_two) + "...");
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setContent(view_custom)
                .setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
                .setTicker(mContext.getApplicationContext().getString(R.string.batterymanage_switch_screen))
                .setPriority(Notification.PRIORITY_DEFAULT)// 设置该通知优先级
                .setOngoing(true)// 不是正在进行的 true为正在进行 效果和.flag一样
                .setSmallIcon(R.drawable.icon_charge)
                .setAutoCancel(false);

        if (onSystemLockScreen) {
            view_custom.setOnClickPendingIntent(R.id.notify_whole, PendingIntent.getBroadcast(
                    mContext, 0, new Intent(ACTION_LEO_SAVER_NOTIFI_CLICKEDX), 0
            ));
        } else {
            Intent intent = new Intent(ACTION_LEO_SAVER_NOTIFI_CLICKED);
            PendingIntent pendingIntent =
                    PendingIntent.getBroadcast(mContext, 0, intent, 0);
            mBuilder.setContentIntent(pendingIntent);
        }

        Notification notify = mBuilder.build();
        notify.contentView = view_custom;
        mNotificationManager.notify(SAVER_NOTIFICATION_ID, notify);
        mNeedUpdateLevel = true;
    }

    public void dismissScreenSaverNotification() {
        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(SAVER_NOTIFICATION_ID);
        }
        mNeedUpdateLevel = false;
    }

    /***
     * 更新通知栏电量值
     *
     * @param level
     */
    public void updateNotificationLevel(int level) {
        if (mNeedUpdateLevel) {
            showNotificationForScreenSaver(level);
        }
    }
}
