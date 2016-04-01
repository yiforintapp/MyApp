package com.leo.appmaster.privacy;

import android.app.Activity;
import android.content.Intent;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.db.PrefTableHelper;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.utils.PrefConst;

import java.util.List;

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
        return LeoSettings.getInteger(PrefConst.KEY_PRIVACY_APP_LIMIT, 5);
    }

    @Override
    public int getPrivacyType() {
        return PrivacyHelper.PRIVACY_APP_LOCK;
    }

    @Override
    public String getReportDescription() {
        return "prilevel_notice_app";
    }

}
