package com.leo.appmaster.privacy;

import com.leo.appmaster.R;
import com.leo.appmaster.db.PrefTableHelper;
import com.leo.appmaster.videohide.VideoItemBean;

import java.util.List;

/**
 * Created by Jasper on 2016/3/31.
 */
public class VideoPrivacy extends Privacy<VideoItemBean> {
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
    public List<VideoItemBean> getAddedList() {
        return null;
    }

    @Override
    public int getFoundStringId() {
        return R.string.hd_found_vid;
    }

    @Override
    public int getAddedStringId() {
        return R.string.hd_new_vid;
    }

    @Override
    public int getProceedStringId() {
        return R.string.hd_hide_vid;
    }

    @Override
    public int getNotificationTextId() {
        return R.string.hd_hide_vid_privacy_title;
    }

    @Override
    public int getNotificationSummaryId() {
        return R.string.hd_hide_vid_privacy_summary;
    }

    @Override
    public int getNotificationIconId() {
        return R.drawable.noti_video;
    }

    @Override
    public int getPrivacyLimit() {
        return PrefTableHelper.getVideoPrivacyLimit();
    }

    @Override
    public int getPrivacyType() {
        return PrivacyHelper.PRIVACY_HIDE_VID;
    }

    @Override
    public String getReportDescription() {
        return "prilevel_notice_vid";
    }
}
