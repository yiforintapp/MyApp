
package com.zlf.appmaster.ui;

import com.zlf.appmaster.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

/**
 * 仿iphone带进度的进度条，线程安全的View，可直接在线程中更新进度
 */
public class RoundProgressBar extends View {
    /**
     * 画笔对象的引用
     */
    protected Paint paint;

    /**
     * 圆环的颜色
     */
    protected int roundColor;

    /**
     * 圆环进度的颜色
     */
    protected int roundProgressColor;

    /**
     * 中间进度百分比的字符串的颜色
     */
    protected int textColor;

    /**
     * 中间进度百分比的字符串的字体
     */
    protected float textSize;

    /**
     * 圆环的宽度
     */
    protected float roundWidth;

    /**
     * 最大进度
     */
    protected int max;

    /**
     * 当前进度
     */
    protected int progress;
    /**
     * 是否显示中间的进度
     */
    protected boolean textIsDisplayable;

    /**
     * 进度的风格，实心或者空心
     */
    protected int style;

    protected int centerTextStyle;

    protected String progressText;

    public static final int STROKE = 0;
    public static final int FILL = 1;
    public static final int PERCENT = 0;
    public static final int CUSTOM = 1;
    private static final int MAX_LEVEL = 100;
    
    public RoundProgressBar(Context context) {
        this(context, null);
    }

    public RoundProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        paint = new Paint();

        TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
                R.styleable.RoundProgressBar);

        // 获取自定义属性和默认值
        roundColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundColor, Color.RED);
        roundProgressColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundProgressColor,
                Color.GREEN);
        textColor = mTypedArray.getColor(R.styleable.RoundProgressBar_textColor, Color.GREEN);
        textSize = mTypedArray.getDimension(R.styleable.RoundProgressBar_textSize, 15);
        roundWidth = mTypedArray.getDimension(R.styleable.RoundProgressBar_roundWidth, 5);
        max = mTypedArray.getInteger(R.styleable.RoundProgressBar_max, 100);
        textIsDisplayable = mTypedArray.getBoolean(R.styleable.RoundProgressBar_textIsDisplayable,
                false);
        style = mTypedArray.getInt(R.styleable.RoundProgressBar_style, STROKE);
        centerTextStyle = mTypedArray.getInt(R.styleable.RoundProgressBar_centerTextStyle,PERCENT);
        
        mTypedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawProgress(canvas);
    }

    public void drawProgress(Canvas canvas) {
        /**
         * 画最外层的大圆环
         */
        int centre = getWidth() / 2; // 获取圆心的x坐标
        int radius = (int) (centre - roundWidth / 2); // 圆环的半径
        paint.setColor(roundColor); // 设置圆环的颜色
        paint.setStyle(Paint.Style.STROKE); // 设置空心
        paint.setStrokeWidth(roundWidth); // 设置圆环的宽度
        paint.setAntiAlias(true); // 消除锯齿
        canvas.drawCircle(centre, centre, radius, paint); // 画出圆环

        /**
         * 画进度百分比
         */
        if (textIsDisplayable && style == STROKE) {
            paint.setStrokeWidth(0);
            paint.setColor(textColor);
            paint.setTextSize(textSize);
            paint.setTypeface(Typeface.DEFAULT_BOLD); // 设置字体
            if (centerTextStyle == PERCENT) {
                float percent = max > 0 ? (float) progress / (float) max : 0;
                 int level = (int) (percent * MAX_LEVEL);  // 中间的进度百分比，先转换成float在进行除法运算，不然都为0
                progressText = level + "%";
            }
            if (progressText != null && !progressText.equals("")) {
                float textWidth = paint.measureText(progressText); // 测量字体宽度，我们需要根据字体的宽度设置在圆环中间
                canvas.drawText(progressText, centre - textWidth / 2, centre + textSize / 2, paint); // 画出进度信息
            }
        }

        /**
         * 画圆弧 ，画圆环的进度
         */
        // 设置进度是实心还是空心
        paint.setStrokeWidth(roundWidth); // 设置圆环的宽度
        paint.setColor(roundProgressColor); // 设置进度的颜色
        RectF oval = new RectF(centre - radius, centre - radius, centre + radius, centre + radius); // 用于定义的圆弧的形状和大小的界限

        switch (style) {
            case STROKE: {
                paint.setStyle(Paint.Style.STROKE);
                // FIXME: 2015/9/15 AM-2408, max为0抛异常：ArithmeticException: divide by zero
                if (max != 0) {
                    canvas.drawArc(oval, 0, 360 * progress / max, false, paint); // 根据进度画圆弧
                }
                break;
            }
            case FILL: {
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                // FIXME: 2015/9/15 AM-2408, max为0抛异常：ArithmeticException: divide by zero
                if (progress != 0 && max != 0)
                    canvas.drawArc(oval, 0, 360 * progress / max, true, paint); // 根据进度画圆弧
                break;
            }
        }
    }

    public synchronized int getMax() {
        return max;
    }

    /**
     * 设置进度的最大值
     * 
     * @param max
     */
    public synchronized void setMax(int max) {
        if (max < 0) {
            throw new IllegalArgumentException("max not less than 0");
        }
        this.max = max;
    }

    /**
     * 获取进度.需要同步
     * 
     * @return
     */
    public synchronized int getProgress() {
        return progress;
    }

    /**
     * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步 刷新界面调用postInvalidate()能在非UI线程刷新
     * 
     * @param progress
     */
    public synchronized void setProgress(int progress) {
        
        if (progress < 0) {
            progress = 0;
        }
        if (progress > max) {
            progress = max;
        }
        if (progress <= max) {
            this.progress = progress;
            postInvalidate();
        }

    }

    public int getCricleColor() {
        return roundColor;
    }

    public void setCricleColor(int cricleColor) {
        this.roundColor = cricleColor;
    }

    public int getCricleProgressColor() {
        return roundProgressColor;
    }

    public void setCricleProgressColor(int cricleProgressColor) {
        this.roundProgressColor = cricleProgressColor;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public float getRoundWidth() {
        return roundWidth;
    }

    public void setRoundWidth(float roundWidth) {
        this.roundWidth = roundWidth;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {

        }
    }

    public int getCenterTextStyle() {
        return centerTextStyle;
    }

    public void setCenterTextStyle(int centerTextStyle) {
        this.centerTextStyle = centerTextStyle;
    }

    public String getProgressText() {
        return progressText;
    }

    public void setProgressText(String progressText) {
        this.progressText = progressText;
    }

    public boolean ifTextIsDisplayable() {
        return textIsDisplayable;
    }

    public void setTextIsDisplayable(boolean textIsDisplayable) {
        this.textIsDisplayable = textIsDisplayable;
    }

}
