package com.leo.appmaster.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Scroll中可以包AdapterView，在部分区域，ScrollView响应滚动事件，部分区域AdapterView响应
 * Created by Jasper on 2015/10/16.
 */
public class AdapterScrollView extends ScrollView {

    public AdapterScrollView(Context context) {
        super(context);
    }

    public AdapterScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdapterScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }
}
