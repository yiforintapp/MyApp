package com.leo.appmaster.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Jasper on 2016/3/10.
 */
public class DrawSafelyImageView extends ImageView {
    public DrawSafelyImageView(Context context) {
        super(context);
    }

    public DrawSafelyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawSafelyImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
