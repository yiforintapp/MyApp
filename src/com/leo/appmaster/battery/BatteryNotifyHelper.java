package com.leo.appmaster.battery;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;

import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.engine.BatteryComsuption;
import com.leo.appmaster.mgr.BatteryManager;
import com.leo.appmaster.utils.LeoLog;

import java.util.List;

/**
 * Created by stone on 16/1/15.
 */
public class BatteryNotifyHelper {

    private static final String TAG = "BatteryNotifyHelper";

    private static final String ACTION_LEO_BATTERY_APP =
            "com.leo.appmaster.battery.notification.action";
    private static final int CHECK_INTERVAL = 3 * 60 * 60 * 1000;

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

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mManager.shouldNotify()) {
                // TODO get app list and sendNotification
                ThreadManager.executeOnAsyncThread(new Runnable() {
                    @Override
                    public void run() {
                        final List<BatteryComsuption> list = mManager.getBatteryDrainApps();
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
