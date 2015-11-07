package com.leo.appmaster.ui;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

/**
 * 首页动画层基类
 * Created by Jasper on 2015/10/10.
 */
public abstract class AnimLayer {
    private Rect mBounds;
    protected View mParent;

    public AnimLayer(View view) {
        mBounds = new Rect();
        mParent = view;
    }

    public void setBounds(int left, int top, int right, int bottom) {
        mBounds.set(left, top, right, bottom);
        onSizeChanged();
    }

    protected void setBounds(Rect rect) {
        if (rect == null) return;

        mBounds.set(rect);
        onSizeChanged();
    }

    protected void onSizeChanged() {

    }

    public int centerX() {
        return mBounds.centerX();
    }

    public int centerY() {
        return mBounds.centerY();
    }

    public int getLeft() {
        return mBounds.left;
    }

    public int getTop() {
        return mBounds.top;
    }

    public int getRight() {
        return mBounds.right;
    }

    public int getBottom() {
        return mBounds.bottom;
    }

    public int getWidth() {
        return mBounds.width();
    }

    public int getHeight() {
        return mBounds.height();
    }

    protected boolean processTouch(MotionEvent event) {
        return false;
    }

    protected abstract void draw(Canvas canvas);
}
