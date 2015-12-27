package com.leo.appmaster.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.leo.appmaster.R;

/**
 * Created by Jasper on 2015/12/27.
 */
public class ImageLoadingView extends View {
        public static final int FULL_COLOR = Color.parseColor("#ff00ff");
    private LoadingLayer loadingLayer;
    private int padding;

    private Paint mPaint;

    public ImageLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        padding = context.getResources().getDimensionPixelSize(R.dimen.pp_img_loading_padding);
        mPaint = new Paint();

        Resources res = context.getResources();
        mPaint.setAntiAlias(true);
//        mPaint.setColor(res.getColor(R.color.pp_img_load_bg));
        mPaint.setColor(FULL_COLOR);

        loadingLayer = new LoadingLayer(this);
//        loadingLayer.setBarColor(res.getColor(R.color.c1));
        loadingLayer.setBarStokeWidth(res.getDimensionPixelSize(R.dimen.pp_img_loading_stroke));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        loadingLayer.setBounds(getLeft(), getTop(), getRight(), getBottom());
        loadingLayer.setLoadingSize(getWidth() - padding * 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(getLeft(), getTop(), getRight(), getBottom(), mPaint);
//
        int centerX = loadingLayer.centerX();
        int centerY = loadingLayer.centerY();
        canvas.drawCircle(centerX, centerY, getWidth() / 2, mPaint);
        loadingLayer.draw(canvas);
        invalidate();
    }
}
