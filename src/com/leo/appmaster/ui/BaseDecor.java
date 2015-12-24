package com.leo.appmaster.ui;

import android.content.Context;

/**
 * Created by Jasper on 2015/12/24.
 */
public abstract class BaseDecor implements LayerDecor {
    protected AnimLayer mParent;
    protected Context mContext;

    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    public void setParentLayer(AnimLayer layer) {
        mParent = layer;
    }
}
