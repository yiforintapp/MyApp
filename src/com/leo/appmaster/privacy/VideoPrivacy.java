package com.leo.appmaster.privacy;

import com.leo.appmaster.R;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.videohide.VideoItemBean;

/**
 * Created by Jasper on 2016/3/31.
 */
public class VideoPrivacy extends Privacy<VideoItemBean> {
    @Override
    public String getTag() {
        return "VideoPrivacy";
    }

    @Override
    protected boolean isConsumed() {
        return LeoSettings.getBoolean(PrefConst.KEY_VID_COMSUMED, false);
    }

    @Override
    public int getFoundStringId() {
        return R.string.hd_found_vid;
    }

    @Override
    public int getNewStringId() {
        return R.string.hd_new_vid;
    }

    @Override
    public int getProceedStringId() {
        return R.string.hd_hide_vid;
    }

    @Override
    public int getAddStringId() {
        return R.string.hd_add_hide_vid;
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
        return LeoSettings.getInteger(PrefConst.KEY_NOTIFY_VID_COUNT, 3);
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
