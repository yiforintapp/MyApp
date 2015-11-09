package com.leo.appmaster.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.leo.appmaster.R;
import com.leo.appmaster.home.HomePrivacyFragment;
import com.leo.tools.animator.ObjectAnimator;
import com.leo.tools.animator.ValueAnimator;

/**
 * Created by Jasper on 2015/10/26.
 */
public class HomeUpArrow extends View implements SlidingUpPanelLayout.TapRectFunction {
    private static final float RATIO_REVERSE = 2f;

    public static final int FULL_COLOR = Color.parseColor("#f8f8f8");

    private BitmapDrawable mUpIcon;
    private Paint mPaint;

    private int mDividerColor;
    private int mBgAlpha = 0;

    // 0.5 ~ 1
    private float mAnimRatio;
    private Drawable mRedTip;

    private ObjectAnimator mUpAnim;
    private boolean mShowRedTip = true;
    private boolean mReversed;

    private GestureDetector mGesture;
    private boolean mHitRect;

    private int mTapWidth;
    private Rect mTapRect;

    public HomeUpArrow(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(FULL_COLOR);

        mDividerColor = context.getResources().getColor(R.color.divider_2);
        mUpIcon = (BitmapDrawable) context.getResources().getDrawable(R.drawable.ic_up_arrow);
        mRedTip = context.getResources().getDrawable(R.drawable.red_dot);

        mTapWidth = context.getResources().getDimensionPixelSize(R.dimen.home_more_arrow_width);
        mGesture = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
//                int x = (int) e.getX();
//                int y = (int) e.getY();
//                if (mTapRect.contains(x, y)) {
//                    return true;
//                }
                return super.onSingleTapUp(e);
            }

        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int iw = mUpIcon.getIntrinsicWidth();
        int ih = mUpIcon.getIntrinsicHeight();

        int l = getLeft() + (w - iw) / 2;
        int t = getTop() + (h - ih * 2) / 2;
        mUpIcon.setBounds(l, t, l + iw, t + ih);

        Rect rect = mUpIcon.getBounds();

        int rw = mRedTip.getIntrinsicWidth();
        int rh = mRedTip.getIntrinsicHeight();
        int rl = rect.right - rw / 2;
        int rt = rect.top;
        mRedTip.setBounds(rl, rt, rl + rw, rt + rh);

        int left = (getWidth() - mTapWidth) / 2;
        int right = left + mTapWidth;

        mTapRect = new Rect(left, getTop(), right, getBottom());
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        mPaint.setColor(FULL_COLOR);
        mPaint.setAlpha(mBgAlpha);
        canvas.drawRect(getLeft(), getTop(), getRight(), getBottom(), mPaint);

        mPaint.setColor(mDividerColor);
        canvas.drawLine(getLeft(), getBottom(), getRight(), getBottom(), mPaint);

        canvas.save();
        Rect bounds = mUpIcon.getBounds();
        if (mReversed) {
            canvas.rotate(180, bounds.centerX(), bounds.centerY());
        } else if (mAnimRatio < 1) {
            float ratio = mAnimRatio;
            canvas.scale(ratio, ratio, bounds.centerX(), bounds.centerY());

            int alpha = (int) (255f * ratio);
            mUpIcon.getPaint().setAlpha(alpha);
        }
        mUpIcon.draw(canvas);
        canvas.restore();

        canvas.save();
        canvas.translate(0, bounds.height());

        if (mReversed) {
            canvas.rotate(180, bounds.centerX(), bounds.centerY());
        } else if (mAnimRatio < 1) {
            float ratio = 1.5f - mAnimRatio;
            canvas.scale(ratio, ratio, bounds.centerX(), bounds.centerY());

            int alpha = (int) (255f * ratio);
            mUpIcon.getPaint().setAlpha(alpha);
        }
        mUpIcon.draw(canvas);
        canvas.restore();

        if (mShowRedTip) {
            mRedTip.draw(canvas);
        }
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        return mGesture.onTouchEvent(event);
//    }

    public void setAnimRatio(float animRatio) {
        mAnimRatio = animRatio;
        invalidate();
    }

    public void startUpAnimation() {
        if (mUpAnim != null) {
            mUpAnim.cancel();
        }

        mUpAnim = ObjectAnimator.ofFloat(this, "animRatio", 0.5f, 1f);
        mUpAnim.setDuration(500);
        mUpAnim.setInterpolator(new LinearInterpolator());
        mUpAnim.setRepeatCount(ValueAnimator.INFINITE);
        mUpAnim.setRepeatMode(ValueAnimator.REVERSE);
        mUpAnim.start();
    }

    public void release() {
        if (mUpAnim != null) {
            try {
                mUpAnim.end();
                mUpAnim.cancel();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mUpAnim = null;
        }
    }

    public void cancelUpAnimation() {
        if (mUpAnim != null) {
            mUpAnim.cancel();
        }
        mAnimRatio = 1f;
        invalidate();
    }

    public void reverse() {
        cancelUpAnimation();
        mReversed = true;
        invalidate();
    }

    public void reset() {
        mReversed = false;
        invalidate();
    }

    public void setBgAlpha(int alpha) {
        mBgAlpha = alpha;
        invalidate();
    }

    public void showRedTip(boolean show) {
        mShowRedTip = show;
        invalidate();
    }

    @Override
    public Rect getTapRect() {
        int width = HomePrivacyFragment.sScreenWidth;
        int left = (width - mTapWidth) / 2;
        int right = left + mTapWidth;

        int top = HomePrivacyFragment.sScreenHeight - getHeight();
        return new Rect(left, top, right, HomePrivacyFragment.sScreenHeight);
    }

}
