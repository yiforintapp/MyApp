
package com.leo.appmaster.sdk.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.leo.appmaster.R;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NotificationUtil;
import com.leo.appmaster.utils.TimeUtil;

public class PushNotification {
    private static final String NOTIFY_LAST_SHOW_DAY = "notify_last_show_day";
    public static final int NOTI_THEME = 1;
    public static final int NOTI_HOTAPP = 2;
    public static final int NOTI_BACKUP = 3;
    public static final int NOTI_TRAFFIC = 4;
    public static final int NOTI_PRIVACYSTATUS = 5;


    // public static boolean isFromPush_Update = false;
    private Context mContext;
    private int mUid = 129;
    private Notification notif;

    public PushNotification(Context ctx) {
        this.mContext = ctx;
    }

    public void showNotification(Intent intent, String title, String content, int iconId, int type) {
        long lastShowDay = PreferenceTable.getInstance().getLong(NOTIFY_LAST_SHOW_DAY, 0);
        long nowDay = System.currentTimeMillis();
        LeoLog.d("testTrafficNoti", "lastShowDay:" + lastShowDay + "--nowDay:" + nowDay);
        if (!TimeUtil.isSameDay(lastShowDay, nowDay)) {
            if (type == NOTI_THEME) {
                showNewThemeNoti(intent, title, content);
            } else if (type == NOTI_HOTAPP) {
                showNewAppNoti(intent, title, content);
            } else if (type == NOTI_BACKUP) {
                showBackupApp(intent, title, content, iconId);
            } else if (type == NOTI_TRAFFIC) {
                showTrafficNoti(intent, title, content, iconId);
            } else if (type == NOTI_PRIVACYSTATUS) {
                showPrivacyStatusNoti(intent, title, content, iconId);
            }
            PreferenceTable.getInstance().putLong(NOTIFY_LAST_SHOW_DAY, nowDay);
        }else{
            LeoLog.d("testTrafficNoti", "same day");
        }
    }

    public void showBackupApp(Intent intent, String title, String content, int iconId) {
        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "backup", "backup_notify");

        notif = new Notification();
        PendingIntent contentIntent = PendingIntent.getService(mContext, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notif.icon = R.drawable.noti_backup;
        notif.tickerText = title;
        notif.flags = Notification.FLAG_AUTO_CANCEL;
        notif.setLatestEventInfo(mContext, title, content, contentIntent);
        NotificationUtil.setBigIcon(notif, R.drawable.noti_backup);
        notif.when = System.currentTimeMillis();
        NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(mUid, notif);
    }

    public void showTrafficNoti(Intent intent, String title, String content, int iconId) {
        notif = new Notification();
        PendingIntent contentIntent = PendingIntent.getService(mContext, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notif.icon = R.drawable.noti_liuliang;
        notif.tickerText = title;
        notif.flags = Notification.FLAG_AUTO_CANCEL;
        notif.setLatestEventInfo(mContext, title, content, contentIntent);
        NotificationUtil.setBigIcon(notif, R.drawable.noti_liuliang);
        notif.when = System.currentTimeMillis();
        NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(mUid, notif);
    }

    public void showPrivacyStatusNoti(Intent intent, String title, String content, int iconId) {
        notif = new Notification();
        PendingIntent contentIntent = PendingIntent.getService(mContext, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notif.icon = iconId;
        notif.tickerText = title;
        notif.flags = Notification.FLAG_AUTO_CANCEL;
        notif.setLatestEventInfo(mContext, title, content, contentIntent);
        NotificationUtil.setBigIcon(notif, iconId);
        notif.when = System.currentTimeMillis();
        NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(mUid, notif);

        String description = null;
        if (iconId == R.drawable.noti_lock) {
            description = "prilevel_notice_app";
        } else if (iconId == R.drawable.noti_pic) {
            description = "prilevel_notice_pic";
        } else if (iconId == R.drawable.noti_video) {
            description = "prilevel_notice_vid";
        }
        if (description != null) {
            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "prilevel", description);
        }
    }

    public void showNewAppNoti(Intent intent, String title, String content) {
        LeoLog.d("PushNotification", "showNewAppNoti");
        notif = new Notification();
        PendingIntent contentIntent = PendingIntent.getService(mContext, 1,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notif.icon = R.drawable.ic_launcher_notification;
        notif.tickerText = title;
        notif.flags = Notification.FLAG_AUTO_CANCEL;
        notif.setLatestEventInfo(mContext, title, content, contentIntent);
        NotificationUtil.setBigIcon(notif, R.drawable.ic_launcher_notification_big);
        notif.when = System.currentTimeMillis();
        NotificationManager nm = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(mUid, notif);
    }

    public void showNewThemeNoti(Intent intent, String title, String content) {
        LeoLog.d("PushNotification", "showNewThemeNoti");
        notif = new Notification();
        PendingIntent contentIntent = PendingIntent.getService(mContext, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notif.icon = R.drawable.noti_theme;
        notif.tickerText = title;
        notif.flags = Notification.FLAG_AUTO_CANCEL;
        notif.setLatestEventInfo(mContext, title, content, contentIntent);
        NotificationUtil.setBigIcon(notif, R.drawable.noti_theme);
        notif.when = System.currentTimeMillis();
        NotificationManager nm = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(mUid, notif);
    }

//    private int[] getCurrentTime() {
//        int[] is = {
//                0, 0, 0, 0, 0, 0
//        };
//        Time time = new Time();
//        time.setToNow();
//        is[0] = time.year;
//        is[1] = time.month + 1;
//        is[2] = time.monthDay;
//        is[3] = time.hour;
//        is[4] = time.minute;
//        is[5] = time.second;
//        return is;
//    }

    public void showUpdateNoti(Intent intent, Intent dIntent, String updateTip, String contentText) {
        // LeoLog.d("PushNotification", "showUpdateNoti");
        // notif = new Notification(
        // R.drawable.ic_launcher_notification, updateTip,
        // System.currentTimeMillis());
        // PendingIntent contentIntent = PendingIntent.getBroadcast(mContext, 0,
        // intent, 0);
        // PendingIntent delIntent = PendingIntent.getBroadcast(mContext, 0,
        // dIntent, 0);
        // notif.deleteIntent = delIntent;
        // notif.setLatestEventInfo(mContext, updateTip, contentText,
        // contentIntent);
        // NotificationUtil
        // .setBigIcon(notif, R.drawable.ic_launcher_notification_big);
        // NotificationManager nm = (NotificationManager) mContext
        // .getSystemService(Context.NOTIFICATION_SERVICE);
        //
        // notif.flags = Notification.FLAG_AUTO_CANCEL
        // | Notification.FLAG_ONGOING_EVENT;
        // nm.notify(mUid, notif);

        // isFromPush_Update = false;
    }

}
