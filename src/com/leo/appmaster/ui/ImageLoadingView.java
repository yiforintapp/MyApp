package com.leo.appmaster.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.leo.appmaster.R;

/**
 * Created by Jasper on 2015/12/27.
 */
public class ImageLoadingView extends View {
    private LoadingLayer loadingLayer;
    private int padding;

    private Paint mPaint;

    public ImageLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        padding = context.getResources().getDimensionPixelSize(R.dimen.pp_img_loading_padding);
        mPaint = new Paint();

        Resources res = context.getResources();
        loadingLayer = new LoadingLayer(this);
        loadingLayer.setBarColor(res.getColor(R.color.c1));
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
        loadingLayer.draw(canvas);
        invalidate();
    }
}
