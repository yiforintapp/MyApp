
package com.leo.appmaster.quickgestures.view;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.leo.appmaster.R;
import com.leo.appmaster.quickgestures.FloatWindowHelper;
import com.leo.appmaster.utils.DipPixelUtil;

/**
 * QuickGesturesAreaView
 * 
 * @author run
 */
public class QuickGesturesAreaView extends View {
    public static int viewWidth;
    public static int viewHeight;
    private boolean mIsShowReadTip;
    private int mDirectionFlag = -1;// 1:左边，2：右边
    private Paint mPaint;
    private float x, y;
    private int radius;
    private ValueAnimator mAnimator;
    private int mAlpha = 255;

    public QuickGesturesAreaView(Context context) {
        super(context);
        initUI(context);
    }

    public QuickGesturesAreaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initUI(context);
    }

    private void initPain(Context context) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.RED);
        mPaint.setAlpha(mAlpha);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        initPain(mContext);
        if (mIsShowReadTip) {
            if (mDirectionFlag > 0) {
                drawReadTip(canvas, mPaint, mDirectionFlag);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        y = getMeasuredHeight();
        x = getMeasuredWidth();

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @SuppressLint("ResourceAsColor")
    private void initUI(Context mContext) {
        this.mContext = mContext;
        setBackgroundResource(R.color.quick_gesture_switch_setting_hidden_color);
        // setBackgroundResource(R.color.quick_gesture_switch_setting_show_color);
        setFocusable(true);
        setClickable(true);
        radius = DipPixelUtil.dip2px(mContext, 4);
    }

    private void drawReadTip(Canvas canvas, Paint paint, int flag) {
        if (flag == 1) {
            // 左边View
            canvas.drawCircle(2 * radius, y - 2 * radius, radius, paint);
        } else if (flag == 2) {
            // 右边View
            canvas.drawCircle(x - 2 * radius, y - 2 * radius, radius, paint);
        } else if (flag == 3) {
            // 左侧中部
            canvas.drawCircle(2 * radius, y / 2, radius, paint);
        } else if (flag == 4) {
            // 右侧中部
            canvas.drawCircle(x - 2 * radius, y / 2, radius, paint);
        }
        canvas.save();
    }

    public void setIsShowReadTip(boolean flag, int directionFlag) {
        mIsShowReadTip = flag;
        mDirectionFlag = directionFlag;
        // invalidate();
        playAnim();
    }

    private void playAnim() {
        cancelAnim(true);
        mAnimator = new ValueAnimator();
        mAnimator.setDuration(800);
        mAnimator.setFloatValues(1.0f, 0.0f);
        mAnimator.removeAllUpdateListeners();
        mAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                final float percent = (Float) animation.getAnimatedValue();
                mAlpha = (int) (255 * percent);
                invalidate();
            }
        });
        mAnimator.start();
    }

    private void cancelAnim(boolean b) {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
    }
}
