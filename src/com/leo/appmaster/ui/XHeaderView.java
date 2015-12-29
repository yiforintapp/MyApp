package com.leo.appmaster.ui;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.leo.appmaster.utils.LeoLog;

/**
 * Created by Jasper on 2015/12/29.
 */
public class XHeaderView extends LinearLayout {
    public interface OnHeaderLayoutListener {
        public void onHeaderLayout(int height);
    }

    private int mHeight;

    private OnHeaderLayoutListener mListener;

    public XHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnHeaderLayoutListener(OnHeaderLayoutListener listener) {
        mListener = listener;
        if (mHeight > 0) {
            mListener.onHeaderLayout(mHeight);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        LeoLog.d("XHeaderView", "onLayout, l: " + l + " | t: " + t + " | r: " + r + " | b: " + b);
        mHeight = b - t;
        if (mListener != null) {
            mListener.onHeaderLayout(mHeight);
        }
    }
}
