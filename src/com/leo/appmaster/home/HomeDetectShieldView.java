package com.leo.appmaster.home;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by RunLee on 2016/3/31.
 */
public class HomeDetectShieldView extends ImageView {

    private HomeShieldLayer mHomeShield;

    public HomeDetectShieldView(Context context) {
        this(context, null);
    }

    public HomeDetectShieldView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeDetectShieldView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mHomeShield = new HomeShieldLayer(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mHomeShield.draw(canvas);
    }
}
