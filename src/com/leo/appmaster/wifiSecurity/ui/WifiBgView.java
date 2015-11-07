package com.leo.appmaster.wifiSecurity.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.RelativeLayout;


/**
 * Created by qili on 15-10-27.
 */
public class WifiBgView extends RelativeLayout {
    private WifiBgLayer bgLayer;

    public WifiBgView(Context context) {
        super(context);
        init();
    }

    public WifiBgView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WifiBgView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        bgLayer = new WifiBgLayer(this);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        bgLayer.setBounds(getLeft(), getTop(), getRight(), getBottom());

    }

    @Override
    protected void onDraw(Canvas canvas) {
        bgLayer.draw(canvas);
    }

    public void setBgColor(int colorLevel) {
        bgLayer.setBgColor(colorLevel);
        invalidate();
    }
}
