package com.leo.appmaster.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * Created by stone on 16/2/17.
 */
public class AdWrapperLayout extends LinearLayout {
    private boolean mNeedIntercept = true;

    public AdWrapperLayout(Context context) {
        super(context);
    }

    public AdWrapperLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdWrapperLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mNeedIntercept;
    }

    public void setNeedIntercept (boolean flag) {
        mNeedIntercept = flag;
    }
}
