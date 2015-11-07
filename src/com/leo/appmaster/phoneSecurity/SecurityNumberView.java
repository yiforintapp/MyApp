package com.leo.appmaster.phoneSecurity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;

/**
 * Created by runlee on 15-10-16.
 */
public class SecurityNumberView extends View {
    private static final String TAG = "SecurityNumberView";
    private Paint mPainBackGround, mPainText;
    private String mNumberText;
    private float mNumberTextSize;
    private int mViewBackGroundColor;
    private int mNumberTextColor;

    public SecurityNumberView(Context context) {
        super(context);
        init();
    }

    public SecurityNumberView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPainBackGround = new Paint();
        mPainText = new Paint();
        mViewBackGroundColor = Color.TRANSPARENT;
        mNumberTextColor = Color.TRANSPARENT;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制圆
        LeoLog.i(TAG, "绘制");
        mPainBackGround.setColor(mViewBackGroundColor);
        mPainBackGround.setAntiAlias(true);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, mPainBackGround);
        //绘制园内数字

        if (!Utilities.isEmpty(mNumberText)) {
            mPainText.setColor(mNumberTextColor);
            if (mNumberTextSize > 0) {
                mPainText.setTextSize(mNumberTextSize);
            }
            mPainText.setTextAlign(Paint.Align.CENTER);
            /*计算文字的显示水平线*/
            Paint.FontMetrics fontMetrics = mPainText.getFontMetrics();
            float fontMetriceHeight = fontMetrics.bottom - fontMetrics.top;
            float baseY = getHeight() - (getHeight() - fontMetriceHeight) / 2 - fontMetrics.bottom;
            canvas.drawText(mNumberText, getWidth() / 2, baseY, mPainText);
        }
//        canvas.save();
    }

    /*设置view内文字*/
    public void setView(String text, float size, int backGroundColor, int textColor) {
        mNumberText = text;
        mNumberTextSize = size;
        mViewBackGroundColor = backGroundColor;
        mNumberTextColor =textColor;
        invalidate();
    }

    public void setText(int text) {
        mNumberText = getResources().getString(text);
        invalidate();
    }
    public void setText(String text){
        mNumberText = text;
        invalidate();
    }
    public void setTextSize(float size) {
        mNumberTextSize = size;
        invalidate();
    }

    public void setViewBackGroundColor(int color) {
        mViewBackGroundColor = color;
        invalidate();
    }

    public void setTextColor(int textColor) {
        mNumberTextColor = textColor;
        invalidate();

    }

}
