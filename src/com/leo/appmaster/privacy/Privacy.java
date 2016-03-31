package com.leo.appmaster.privacy;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.NotificationUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jasper on 2016/3/31.
 */
public abstract class Privacy<T> {
    private static final int NOTI_ID = 23456;
    private int mTotalCount;
    private int mProceedCount;

    private List<T> mDataList;

    private Context mContext;

    Privacy() {
        mContext = AppMasterApplication.getInstance();
    }
    /**
     * 获取新增个数
     * @return
     */
    public int getAddedCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    /**
     * 获取总数
     * @return
     */
    public int getTotalCount() {
        return mTotalCount;
    }

    void setAddedList(List<T> dataList) {
        if (mDataList == null) {
            mDataList = new ArrayList<T>();
        }

        if (dataList == null) {
            return;
        }

        mDataList.clear();
        mDataList.addAll(dataList);
    }

    /**
     * 获取已处理的个数
     * @return
     */
    public abstract int getProceedCount();

    public List<T> getAddedList() {
        if (mDataList == null) {
            mDataList = new ArrayList<T>();
        }
        return mDataList;
    }

    public abstract int getFoundStringId();
    public abstract int getAddedStringId();
    public abstract int getProceedStringId();

    public abstract int getNotificationTextId();
    public abstract int getNotificationSummaryId();
    public abstract int getNotificationIconId();

    public void showNotification() {
        Intent intent = new Intent(mContext, StatusBarEventService.class);
        intent.putExtra(Constants.PRIVACY_ENTER_SCAN_TYPE, getPrivacyType());
        intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, StatusBarEventService.EVENT_PRIVACY_STATUS);
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

        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "prilevel", getReportDescription());
    }

    /**
     * 获取弹通知的上限个数
     * @return
     */
    public abstract int getPrivacyLimit();
    public abstract int getPrivacyType();
    public abstract String getReportDescription();

    void setTotalCount(int totalCount) {
        mTotalCount = totalCount;
    }

    void setProceedCount(int proceedCount) {
        mProceedCount = proceedCount;
    }
}
