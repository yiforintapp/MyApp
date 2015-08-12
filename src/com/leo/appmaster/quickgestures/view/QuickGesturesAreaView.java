
package com.leo.appmaster.quickgestures.view;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.provider.Contacts;
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
    private ValueAnimator mAnimator, mTranAnim;
    private int mAlpha = 255;
    private float redPointX;// 红点的X位置
    private float redPointY;// 红点的Y位置
    private float redPointBgX;// 红点背景的X位置
    private float redPointBgY;// 红点背景的Y位置
    private int redWidth;// 红点宽
    private int redHeight;// 红点高
    /* 显示红点光标志 */
    private boolean mShowRedPointBg;

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
        // mPaint.setColor(Color.RED);
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
        Paint paintBg = new Paint();
        paintBg.setAntiAlias(true);
        paintBg.setStyle(Paint.Style.FILL);
        paintBg.setAlpha(mAlpha);
        // 获取红点Bitmap
        Bitmap redPoint = drawRedPointBitmp();
        if (mShowRedPointBg) {
            // 获取红点光Bitmp
            drawRedPointBackGroudBitmap(canvas, paintBg);
        }
        if (flag == 1) {
            // 左边View
            // canvas.drawCircle(2 * radius, y - 2 * radius, radius, paint);
            canvas.drawBitmap(redPoint, redPointX, redPointY, mPaint);
        } else if (flag == 2) {
            // 右边View
            // canvas.drawCircle(x - 2 * radius, y - 2 * radius, radius, paint);
            canvas.drawBitmap(redPoint, redPointX, redPointY, mPaint);
        } else if (flag == 3) {
            // 左侧中部
            // canvas.drawCircle(2 * radius, y / 2, radius, paint);
            canvas.drawBitmap(redPoint, redPointX, redPointY, mPaint);
        } else if (flag == 4) {
            // 右侧中部
            // canvas.drawCircle(x - 2 * radius, y / 2, radius, paint);
            canvas.drawBitmap(redPoint, redPointX, redPointY, mPaint);
        }
        canvas.save();
    }

    private void drawRedPointBackGroudBitmap(Canvas canvas, Paint paintBg) {
        Bitmap redPointBg = getBackGroudBitmap();
        redPointBgX = x / 2 - (redPointBg.getWidth() / 2);
        redPointBgY = y / 2 + (y / 2 - (redPointBg.getHeight() + redPointBgX));
        canvas.drawBitmap(redPointBg, redPointBgX, redPointBgY, paintBg);
    }

    private Bitmap drawRedPointBitmp() {
        Bitmap redPoint = getRedPointBitmap();
        if (redWidth <= 0) {
            redWidth = 1;
        }
        if (redHeight <= 0) {
            redHeight = 1;
        }
        redPoint = Bitmap.createScaledBitmap(redPoint, redWidth, redHeight, true);
        return redPoint;
    }

    private Bitmap getRedPointBitmap() {
        return BitmapFactory.decodeResource(getResources(),
                R.drawable.gesture_redpoint_corner);
    }

    private Bitmap getBackGroudBitmap() {
        return BitmapFactory.decodeResource(getResources(),
                R.drawable.gesture_redpoint__corner_light);
    }

    public void setIsShowReadTip(boolean flag, int directionFlag) {
        mIsShowReadTip = flag;
        mDirectionFlag = directionFlag;
        playAnim();
    }

    private void playAnim() {
        cancelAnim(true);
        final Bitmap redPoint = getRedPointBitmap();
        final Bitmap redPointBg = getBackGroudBitmap();
        final int redPointWidth = redPoint.getWidth();
        final int redPointHeight = redPoint.getHeight();
        // final float pointX = x / 2 - (redPoint.getWidth() / 2);
        // final float pointY = y / 2 + (y / 2 - (redPoint.getHeight() +
        // redPointX));
        // float redPointBgX = x / 2 - (redPointBg.getWidth() / 2);
        // float redPointBgY = y / 2 + (y / 2 - (redPointBg.getHeight() +
        // redPointBgX));
        // 红点光闪烁动画
        mAnimator = new ValueAnimator();
        mAnimator.setDuration(500);
        mAnimator.setFloatValues(1.0f, 0.0f);
        mAnimator.removeAllUpdateListeners();
        mAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mAnimator.setRepeatCount(5);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                final float percent = (Float) animation.getAnimatedValue();
                mAlpha = (int) (255 * percent);
                invalidate();
            }
        });
        // 红点移动动画
        mTranAnim = new ValueAnimator();
        mTranAnim.setDuration(320);
        mTranAnim.setFloatValues(0.0f, 1.0f);
        mTranAnim.removeAllUpdateListeners();
        mTranAnim.setRepeatCount(0);
        mTranAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                redWidth = (int) (redPointWidth * value);
                redHeight = (int) (redPointHeight * value);
                switch (mDirectionFlag) {
                    case 1:
                        redPointX = (x / 2 - (redPoint.getWidth() / 2)) * value;
                        break;
                    case 2:
                        redPointX = x - (x / 2 - (redPoint.getWidth() / 2) + redPointWidth) * value;
                        break;
                    case 3:
                        redPointX = (x / 2 - (redPoint.getWidth() / 2)) * value;
                        break;
                    case 4:
                        redPointX = x - (x / 2 - (redPoint.getWidth() / 2) + redPointWidth) * value;
                        break;

                    default:
                        break;
                }

                redPointY = y - (x / 2 - (redPoint.getWidth() / 2) + redPointHeight) * value;
                invalidate();
            }
        });
        mTranAnim.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mShowRedPointBg = true;
                mAnimator.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // TODO Auto-generated method stub
            }
        });
        mTranAnim.start();
    }

    private void cancelAnim(boolean b) {
        if (mAnimator != null) {
            mAnimator.cancel();
            mTranAnim.cancel();
        }
    }
}
