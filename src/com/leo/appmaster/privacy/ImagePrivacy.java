package com.leo.appmaster.privacy;

import com.leo.appmaster.R;
import com.leo.appmaster.db.PrefTableHelper;
import com.leo.appmaster.imagehide.PhotoItem;

import java.util.List;

/**
 * Created by Jasper on 2016/3/31.
 */
public class ImagePrivacy extends Privacy<PhotoItem> {
    @Override
    public int getAddedCount() {
        return 0;
    }

    @Override
    public int getTotalCount() {
        return 0;
    }

    @Override
    public int getProceedCount() {
        return 0;
    }

    @Override
    public List<PhotoItem> getAddedList() {
        return null;
    }

    @Override
    public int getFoundStringId() {
        return R.string.hd_found_pic;
    }

    @Override
    public int getAddedStringId() {
        return R.string.hd_new_pic;
    }

    @Override
    public int getProceedStringId() {
        return R.string.hd_hide_pic;
    }

    @Override
    public int getNotificationTextId() {
        return R.string.hd_hide_pic_privacy_title;
    }

    @Override
    public int getNotificationSummaryId() {
        return R.string.hd_hide_pic_privacy_summary;
    }

    @Override
    public int getNotificationIconId() {
        return R.drawable.noti_pic;
    }

    @Override
    public int getPrivacyLimit() {
        return PrefTableHelper.getImagePrivacyLimit();
    }

    @Override
    public int getPrivacyType() {
        return PrivacyHelper.PRIVACY_HIDE_PIC;
    }

    @Override
    public String getReportDescription() {
        return "prilevel_notice_pic";
    }
}
