package com.leo.appmaster.privacy;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.NotificationUtil;
import com.leo.appmaster.utils.PrefConst;

/**
 * Created by Jasper on 2016/3/31.
 */
public class LockPrivacy extends Privacy<AppItemInfo> {
    @Override
    public String getTag() {
        return "LockPrivacy";
    }

    @Override
    protected boolean isConsumed() {
        return LeoSettings.getBoolean(PrefConst.KEY_APP_COMSUMED, false);
    }

    @Override
    public int getFoundStringId() {
        return R.string.hd_found_app;
    }

    @Override
    public int getNewStringId() {
        return R.string.hd_new_app;
    }

    @Override
    public int getProceedStringId() {
        return R.string.hd_locked_app;
    }

    @Override
    public int getAddStringId() {
        return R.string.hd_add_locked_app;
    }

    @Override
    public int getDangerTipId() {
        return R.string.hd_app_danger_tip;
    }

    @Override
    public int getNotificationTextId() {
        return R.string.hd_lock_privacy_title;
    }

    @Override
    public int getNotificationSummaryId() {
        return R.string.hd_lock_privacy_summary;
    }

    @Override
    public int getNotificationIconId() {
        return R.drawable.noti_lock;
    }

    @Override
    public int getPrivacyLimit() {
        return LeoSettings.getInteger(PrefConst.KEY_NOTIFY_APP_COUNT, 5);
    }

    @Override
    public int getPrivacyType() {
        return PrivacyHelper.PRIVACY_APP_LOCK;
    }

    @Override
    public void showNotification() {
        Intent intent = new Intent(mContext, StatusBarEventService.class);
        intent.putExtra(Constants.PRIVACY_ENTER_SCAN_TYPE, getPrivacyType());
        intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, StatusBarEventService.EVENT_PRIVACY_APP);
        Notification notif = new Notification();
        PendingIntent contentIntent = PendingIntent.getService(mContext, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notif.icon = getNotificationIconId();

        String title = mContext.getString(getNotificationTextId());
        String content = mContext.getString(getNotificationSummaryId());
        notif.tickerText = mContext.getString(getNotificationTextId());
        notif.flags = Notification.FLAG_AUTO_CANCEL;
        notif.setLatestEventInfo(mContext, title, content, contentIntent);
        NotificationUtil.setBigIcon(notif, getNotificationIconId());
        notif.when = System.currentTimeMillis();
        NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTI_ID, notif);

        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "prilevel", "prilevel_notice_app");
    }

}
