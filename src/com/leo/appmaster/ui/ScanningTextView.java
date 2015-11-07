package com.leo.appmaster.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Jasper on 2015/10/20.
 */
public class ScanningTextView extends TextView {
    private float mScaleRatio;

    public ScanningTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float scale = mScaleRatio;

        float centerX = (getLeft() + getRight()) / 2;
        float centerY = (getTop() + getBottom()) / 2;
        if (scale < 1) {
            canvas.scale(scale, scale, centerX, centerY);
        }
        super.onDraw(canvas);
    }

    public void setScaleRatio(float scaleRatio) {
        mScaleRatio = scaleRatio;
        invalidate();
    }
}
