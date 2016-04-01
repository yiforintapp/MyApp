package com.leo.appmaster.privacy;

import com.leo.appmaster.R;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.db.PrefTableHelper;
import com.leo.appmaster.imagehide.PhotoItem;
import com.leo.appmaster.utils.PrefConst;

import java.util.List;

/**
 * Created by Jasper on 2016/3/31.
 */
public class ImagePrivacy extends Privacy<PhotoItem> {

    @Override
    protected boolean isConsumed() {
        return LeoSettings.getBoolean(PrefConst.KEY_PIC_COMSUMED, false);
    }

    @Override
    public int getFoundStringId() {
        return R.string.hd_found_pic;
    }

    @Override
    public int getNewStringId() {
        return R.string.hd_new_pic;
    }

    @Override
    public int getProceedStringId() {
        return R.string.hd_hide_pic;
    }

    @Override
    public int getAddStringId() {
        return R.string.hd_add_hide_pic;
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
        return LeoSettings.getInteger(PrefConst.KEY_PRIVACY_IMAGE_LIMIT, 15);
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
