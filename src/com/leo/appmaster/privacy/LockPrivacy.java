package com.leo.appmaster.privacy;

import com.leo.appmaster.R;
import com.leo.appmaster.db.PrefTableHelper;
import com.leo.appmaster.model.AppItemInfo;

import java.util.List;

/**
 * Created by Jasper on 2016/3/31.
 */
public class LockPrivacy extends Privacy<AppItemInfo> {

    @Override
    public int getProceedCount() {
        return 0;
    }

    @Override
    protected boolean isConsumed() {
        return false;
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
        return 0;
    }

    @Override
    public int getPrivacySummaryId() {
        return 0;
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
        return PrefTableHelper.getLockPrivacyLimit();
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
