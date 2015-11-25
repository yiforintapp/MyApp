package com.leo.appmaster.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.LeoLog;

/**
 * Created by qili on 15-10-26.
 */
public class LoadingView extends View {

    private LoadingLayer loadingLayer;

    public LoadingView(Context context) {
        super(context);
        init();
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        loadingLayer = new LoadingLayer(this);
        loadingLayer.setBarColor(getContext().getResources().getColor(R.color.wifi_loading_color));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        LeoLog.d("testanimation", "left:" + getLeft());
        loadingLayer.setBounds(getLeft(), getTop(), getRight(), getBottom());
//        mLoadingLayer.setBounds(getLeft(), getTop(), getRight(), getBottom());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        loadingLayer.draw(canvas);
//        mLoadingLayer.draw(canvas);
        invalidate();
    }
}
