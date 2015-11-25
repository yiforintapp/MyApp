package com.leo.appmaster.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class MulticolorRoundProgressBar extends RoundProgressBar {

    private String[] colorArray = {
            "#9ddf01", "#b2c508", "#d49c11", "#ef7b19"
    };
    
    public MulticolorRoundProgressBar(Context context) {
        this(context, null);
    }

    public MulticolorRoundProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MulticolorRoundProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context,attrs,defStyle);
    }

    @Override
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
        paint.setStrokeWidth(0);
        paint.setColor(textColor);
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.DEFAULT_BOLD); // 设置字体
        int percent = (int) (((float) progress / (float) max) * 100); // 中间的进度百分比，先转换成float在进行除法运算，不然都为0
        float textWidth = paint.measureText(percent + "%"); // 测量字体宽度，我们需要根据字体的宽度设置在圆环中间

        if (textIsDisplayable && percent != 0 && style == STROKE) {
            canvas.drawText(percent + "%", centre - textWidth / 2, centre + textSize / 2, paint); // 画出进度百分比
        }

        /**
         * 画圆弧 ，画圆环的进度
         */
        int mOneColor = Color.parseColor(colorArray[0]);
        int mFourColor = Color.parseColor(colorArray[3]);

        Paint mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE); // 设置空心
        mPaint.setStrokeWidth(roundWidth); // 设置圆环的宽度
        mPaint.setAntiAlias(true); // 消除锯齿
        RectF oval = new RectF(centre - radius, centre - radius, centre
                + radius, centre + radius); // 用于定义的圆弧的形状和大小的界限

        switch (style) {
            case STROKE: {
                paint.setStyle(Paint.Style.STROKE);

                LinearGradient mAGradient = new LinearGradient(centre - radius - 20, getHeight() / 2,
                        centre
                                + radius-20,
                        getHeight() / 2, new int[]
                        {
                                mFourColor,
                                mOneColor
                        }, new float[] {
                                0, 1
                        }, TileMode.CLAMP);
                mPaint.setShader(mAGradient);

                canvas.drawArc(oval, 270, 360 * progress / max, false,
                        mPaint); // 根据进度画圆弧
                break;
            }
            case FILL: {
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                if (progress != 0)
                    canvas.drawArc(oval, 270, 360 * progress / max, true, mPaint); // 根据进度画圆弧
                break;
            }
        }
    }

}
