package com.leo.appmaster.battery;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.LeoLog;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorListenerAdapter;
import com.leo.tools.animator.ObjectAnimator;


public class BatteryTestViewLayout extends RelativeLayout {

    private boolean isTouch = true;

    public BatteryTestViewLayout(Context context) {
        super(context);
    }

    public BatteryTestViewLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BatteryTestViewLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        return isTouch;
    }

    public void setScrollView(boolean flag) {
        isTouch = flag;
    }
}
