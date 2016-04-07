package com.leo.appmaster.privacy;

import android.app.Activity;
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
    public static final int NOTI_ID = 23456;

    /**
     * 新增
     */
    public static final int STATUS_NEW_ADD = 0;
    /**
     * 已处理
     */
    public static final int STATUS_PROCEED = 1;
    /**
     * 发现
     */
    public static final int STATUS_FOUND = 2;
    /**
     * 添加
     */
    public static final int STATUS_TOADD = 3;
    private int mTotalCount;
    private int mProceedCount;

    // 新增列表
    private List<T> mNewList;

    protected Context mContext;

    Privacy() {
        mContext = AppMasterApplication.getInstance();
    }
    /**
     * 获取新增个数
     * @return
     */
    public int getNewCount() {
        return mNewList == null ? 0 : mNewList.size();
    }

    /**
     * 获取总数
     * @return
     */
    public int getTotalCount() {
        return mTotalCount;
    }

    void setNewList(List<T> dataList) {
        if (mNewList == null) {
            mNewList = new ArrayList<T>();
        }

        if (dataList == null) {
            return;
        }

        mNewList.clear();
        mNewList.addAll(dataList);
    }

    /**
     * 获取已处理的个数
     * @return
     */
    public int getProceedCount() {
        return mProceedCount;
    }

    /**
     * 获取新增列表
     * @return
     */
    public List<T> getNewList() {
        if (mNewList == null) {
            mNewList = new ArrayList<T>();
        }
        return mNewList;
    }

    public boolean isDangerous() {
        int status = getStatus();
        return status == STATUS_NEW_ADD || status == STATUS_FOUND;
    }

    public int getStatus() {
        if (!isConsumed()) {
            if (getProceedCount() > 0) {
                return STATUS_PROCEED;
            } else {
                return STATUS_FOUND;
            }
        }

        if (getNewCount() > 0 && getTotalCount() != getNewCount()) {
            return STATUS_NEW_ADD;
        }

        if (getProceedCount() > 0) {
            return STATUS_PROCEED;
        }
        return STATUS_TOADD;
    }

    /**
     * 是否有消费过数据，例如进入过应用列表、图片/视频隐藏列表
     * @return
     */
    protected abstract boolean isConsumed();

    public abstract int getFoundStringId();
    public abstract int getNewStringId();
    public abstract int getProceedStringId();
    public abstract int getAddStringId();

    public int getDangerTipId() {
        return R.string.hd_pic_danger_tip;
    }

    public int getPrivacyTitleId() {
        int status = getStatus();
        if (status == STATUS_PROCEED) {
            return getProceedStringId();
        }

        if (status == STATUS_FOUND) {
            return getFoundStringId();
        }

        if (status == STATUS_TOADD) {
            return getAddStringId();
        }

        if (status == STATUS_NEW_ADD) {
            return getNewStringId();
        }

        return getAddStringId();
    }

    public String getPrivacyCountText() {
        int status = getStatus();
        switch (status) {
            case STATUS_FOUND:
                return getTotalCount() + "";
            case STATUS_NEW_ADD:
                return getNewCount() + "";
            case STATUS_PROCEED:
                return getProceedCount() + "";
        }

        return "null";
    }

    public boolean showPrivacyCount() {
        int status = getStatus();
        return status != STATUS_TOADD;
    }

    public abstract String getNotificationText();
    public abstract String getNotificationSummary();
    public abstract int getNotificationIconId();

    public void showNotification() {
    }

    public abstract boolean isNotifyOpen();

    /**
     * 获取弹通知的上限个数
     * @return
     */
    public abstract int getPrivacyLimit();
    public abstract int getPrivacyType();

    public abstract void jumpAction(Activity activity);

    /**
     * 展示上报
     */
    public abstract void reportExposure();

    public String getTag() {
        return "Privacy";
    }

    void setTotalCount(int totalCount) {
        mTotalCount = totalCount;
    }

    void setProceedCount(int proceedCount) {
        mProceedCount = proceedCount;
    }

    @Override
    public String toString() {
        return getTag() + " | danger: " + isDangerous()
                        + " | new: " + getNewCount()
                        + " | proceed: " + getProceedCount()
                        + " | total: " + getTotalCount();
    }
}
