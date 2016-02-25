package com.leo.appmaster.battery;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.leo.appmaster.R;

/**
 * 广告上面的渐变遮罩
 * Created by Jasper on 2016/2/24.
 */
public class GradientMaskView extends View {
    private static final int MASK_LEN = 100;

    private Rect mMaskRect;
    private Shader mMaskShader;
    private Paint mPaint;

    public GradientMaskView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int mid = (int) ((BatteryBackground.sTop + BatteryBackground.sBottom) / 2f + 0.5);
        int midToBottom = BatteryBackground.sBottom - mid;

        int marginBottom = getContext().getResources().getDimensionPixelSize(
                R.dimen.batteryscreen_slidearea_height);

        int maskTop = getBottom() - MASK_LEN;
        int maskBottom = getBottom();

        int bottomColor = BatteryBackground.COLOR_BOTTOM;
        int midColor = BatteryBackground.COLOR_MID;

        int bottomColorR = Color.red(bottomColor);
        int bottomColorG = Color.green(bottomColor);
        int bottomColorB = Color.blue(bottomColor);

        int midColorR = Color.red(midColor);
        int midColorG = Color.green(midColor);
        int midColorB = Color.blue(midColor);

        float ratio = ((float)(midToBottom - marginBottom)) / (float)midToBottom;
        int r = (int) (midColorR + (bottomColorR - midColorR) * ratio);
        int g = (int) (midColorG + (bottomColorG - midColorG) * ratio);
        int b = (int) (midColorB + (bottomColorB - midColorB) * ratio);
        int toColor = Color.argb(255, r, g, b);

        ratio = (float)(midToBottom - marginBottom - MASK_LEN) / (float)midToBottom;
        r = (int) (midColorR + (bottomColorR - midColorR) * ratio);
        g = (int) (midColorG + (bottomColorG - midColorG) * ratio);
        b = (int) (midColorB + (bottomColorB - midColorB) * ratio);
        int fromColor = Color.argb(0, r, g, b);
        // from->to 从下往上
        mMaskRect = new Rect(0, maskTop, getRight() - getLeft(), maskBottom);
        mMaskShader = new LinearGradient(getLeft(), maskTop, getLeft(), maskBottom, fromColor, toColor, Shader.TileMode.REPEAT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);

        if (mPaint.getShader() == null) {
            mPaint.setShader(mMaskShader);
        }

        canvas.drawRect(mMaskRect, mPaint);
    }
}
