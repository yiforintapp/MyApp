package com.leo.appmaster.ui;

/**
 * Created by Jasper on 2015/12/24.
 */
public abstract class BaseDecor implements LayerDecor {
    protected AnimLayer mParent;
    @Override
    public void setParentLayer(AnimLayer layer) {
        mParent = layer;
    }
}
