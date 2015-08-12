
package com.leo.appmaster.applocker.receiver;

import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.service.TaskDetectService;

public class LockReceiver extends BroadcastReceiver {

    public static final String ALARM_LOCK_ACTION = "com.leo.appmaster.alarmlock";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        //开机启动服务，开始获取，计算流量
        if(Intent.ACTION_BOOT_COMPLETED.equals(action)){
//            AppMasterPreference preferences = AppMasterPreference.getInstance(context);
//            preferences.setFirstIn(true);
             Intent intent2=new Intent(context, TaskDetectService.class);
             context.startService(intent2);
        }
        
        
        if (ALARM_LOCK_ACTION.equals(action)) {
            AppMasterPreference pref = AppMasterPreference.getInstance(context);
            if (pref.getRecommendLockPercent() >= 0.5f) {
                intent = new Intent();
                intent.setAction(LockReceiver.ALARM_LOCK_ACTION);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                pref.setLastAlarmSetTime(calendar.getTimeInMillis());
                calendar.add(Calendar.DATE, 3);
                PendingIntent pi = PendingIntent.getBroadcast(context, 0,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager am = (AlarmManager) context
                        .getSystemService(Context.ALARM_SERVICE);
                am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
                return;
            } else {
                showNotification(context);
                AppMasterPreference.getInstance(context).setReminded(true);
            }

        } else if (Intent.ACTION_SCREEN_ON.equals(action)
                || Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_USER_PRESENT.equals(action)
                || "com.leo.appmaster.restart".equals(action)) {
            if (AppMasterPreference.getInstance(context).getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
                // Intent serviceIntent = new Intent(context,
                // TaskDetectService.class);
                // serviceIntent.putExtra("lock_service", true);
                // serviceIntent.putExtra(TaskDetectService.EXTRA_STARTUP_FROM,
                // action);
                // context.startService(serviceIntent);

                LockManager.getInstatnce().startLockService();
            }
        }
    }

    private void showNotification(Context ctx) {
//        NotificationManager nm = (NotificationManager) ctx
//                .getSystemService(Context.NOTIFICATION_SERVICE);
//        String content = ctx.getString(R.string.lock_remind);
//        Intent intent;
//        AppMasterPreference pref = AppMasterPreference.getInstance(ctx);
//        if (pref.getLockType() == AppMasterPreference.LOCK_TYPE_NONE) {
//            intent = new Intent(ctx, LockSettingActivity.class);
//            intent.putExtra(LockScreenActivity.EXTRA_LOCK_MODE,
//                    LockManager.LOCK_MODE_FULL);
////            intent.putExtra(LockScreenActivity.EXTRA_TO_ACTIVITY,
////                    AppLockListActivity.class.getName());
//        } else {
//            intent = new Intent(ctx, LockScreenActivity.class);
////            intent.putExtra(LockScreenActivity.EXTRA_TO_ACTIVITY,
////                    AppLockListActivity.class.getName());
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                    | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
//            intent.putExtra(LockScreenActivity.EXTRA_LOCK_MODE,
//                    LockManager.LOCK_MODE_FULL);
//        }
//
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//        Notification notification = new Notification(R.drawable.ic_launcher_notification,
//                content, System.currentTimeMillis());
//        notification.tickerText = content;
//        notification.flags = Notification.FLAG_AUTO_CANCEL;
//        notification.setLatestEventInfo(ctx, "Privacy Guard", content,
//                pendingIntent);
//        NotificationUtil.setBigIcon(notification, R.drawable.ic_launcher_notification_big);
//        nm.notify(0, notification);
    }
}
