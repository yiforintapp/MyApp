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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.engine.BatteryComsuption;
import com.leo.appmaster.mgr.BatteryManager;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.BitmapUtils;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.LeoLog;


/**
 * Created by stone on 16/1/15.
 */
public class BatteryNotifyHelper {

    private static final String TAG = "BatteryNotifyHelper";
    private static final int NOTIFICATION_ID = 20160116;
    private static final int MAX_ICON_ACCOUNT = 6;
    private static final String ACTION_LEO_BATTERY_APP =
            "com.leo.appmaster.battery.notification.action";
    private static final int CHECK_INTERVAL = 3 * 60 * 60 * 1000;
//    private static final int CHECK_INTERVAL = 10 * 1000;
    private BatteryManager mManager;
    private Context mContext;

    public BatteryNotifyHelper(Context context, BatteryManager batteryManager) {
        mContext = context;
        mManager = batteryManager;

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_LEO_BATTERY_APP);
        mContext.registerReceiver(mReceiver, filter);

        fireTimerAction();
    }

    private void fireTimerAction() {
        long nextTime = CHECK_INTERVAL - SystemClock.elapsedRealtime()%CHECK_INTERVAL;
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
//                if (icon != null && icon instanceof BitmapDrawable) {
//                    bdicon = (BitmapDrawable) icon;
//                    if (bdicon != null) {
//                        view_custom.setImageViewBitmap(imageViewIds[i], bdicon.getBitmap());
//                    }
//                } else {
//                    bicon  = BitmapUtils.drawableToBitmap(icon);
//                    if (bicon != null) {
//                        view_custom.setImageViewBitmap(imageViewIds[i], bicon);
//                    }
//                }
            }
        }

        view_custom.setTextViewText(R.id.app_number, list.size()+"");

        view_custom.setTextViewText(R.id.app_detail,
                mContext.getApplicationContext().getString(R.string.batterymanage_label));

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setContent(view_custom)
                .setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
                .setTicker(mContext.getApplicationContext().getString(R.string.batterymanage_switch_noti))
                .setPriority(Notification.PRIORITY_DEFAULT)// 设置该通知优先级
                .setOngoing(false)// 不是正在进行的 true为正在进行 效果和.flag一样
                .setSmallIcon(R.drawable.statusbar_battery_icon)
                .setAutoCancel(true);

        Intent intent = new Intent(mContext,
                BatteryMainActivity.class);
        intent.putExtra("for_sdk", "for_sdk");
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
            if (mManager.shouldNotify()) {
                ThreadManager.executeOnAsyncThread(new Runnable() {
                    @Override
                    public void run() {
                        final List<BatteryComsuption> list = mManager.getBatteryDrainApps();
                        LeoLog.d(TAG, "apps count: " + list.size());
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
    };
}
