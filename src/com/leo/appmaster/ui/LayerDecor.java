package com.leo.appmaster.ui;

import android.graphics.Canvas;
import android.graphics.Matrix;

/**
 * Created by Jasper on 2015/12/24.
 */
public interface LayerDecor {
    public void setParentLayer(AnimLayer layer);
    public void applyDecor(Canvas canvas, Matrix matrix);
}
