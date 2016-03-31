package com.leo.appmaster.privacy;

import com.leo.appmaster.model.AppItemInfo;

import java.util.List;

/**
 * Created by Jasper on 2016/3/31.
 */
public class LockPrivacy extends Privacy<AppItemInfo> {
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
    public List<AppItemInfo> getAddedList() {
        return null;
    }

    @Override
    public int getFoundStringId() {
        return 0;
    }

    @Override
    public int getAddedStringId() {
        return 0;
    }

    @Override
    public int getProceedStringId() {
        return 0;
    }
}
