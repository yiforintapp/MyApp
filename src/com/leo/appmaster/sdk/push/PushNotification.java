
package com.leo.appmaster.sdk.push;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NotificationUtil;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class PushNotification {

    // public static boolean isFromPush_Update = false;
    private Context mContext;
    private int mUid = 129;
    private Notification notif;

    public PushNotification(Context ctx) {
        this.mContext = ctx;
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
