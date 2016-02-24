package com.leo.appmaster.battery;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * 充电屏保背景
 * Created by Jasper on 2016/2/24.
 */
public class BatteryBackground extends RelativeLayout {
    private static final int COLOR_TOP = Color.parseColor("#221970");
    private static final int COLOR_MID = Color.parseColor("#4644a1");
    private static final int COLOR_BOTTOM = Color.parseColor("#487dcc");

    private Paint mTopPaint;

    private LinearGradient mBgShader;

    public BatteryBackground(Context context) {
        this(context, null);
    }

    public BatteryBackground(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public BatteryBackground(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private void init() {
        mTopPaint = new Paint();
        mTopPaint.setColor(Color.BLACK);
        mTopPaint.setAntiAlias(true);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mBgShader = new LinearGradient(getLeft(), getTop(), getLeft(), getBottom(),
                new int[] {COLOR_TOP, COLOR_MID, COLOR_BOTTOM}, null, Shader.TileMode.REPEAT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mTopPaint.setShader(mBgShader);
        canvas.drawRect(getLeft(), getTop(), getRight(), getBottom(), mTopPaint);
    }
}
