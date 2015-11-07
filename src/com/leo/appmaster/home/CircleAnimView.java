
package com.leo.appmaster.home;

import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.leo.appmaster.R;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorListenerAdapter;
import com.leo.tools.animator.ValueAnimator;

public class CircleAnimView extends View {

    private final static int LOCK_ANIM_DURATION = 1000;

    private Paint mPaint;
    private FontMetrics mFontMetrics = new FontMetrics();

    private int mCount;

    private int mCountTextSize;
    private int mTipTextSize;
    private int mTextVerrticalPadding;

    private String mTipText;
    private int mRealCountTextSize;
    private int mRealTipTextSize;

    private Drawable mInnerCircle;
    private Drawable mOuterCircle;
    private Drawable mIcon;

    private int mIconSmallSize;
    private int mIconBigSize;

    private int mMaxAnimCircleR;
    private int mVerticalPadding;
    private Rect mInnerCircleBound = new Rect();
    private Rect mOuterCircleBound = new Rect();
    private Rect mAnimCircleBound = new Rect();
    private Rect mContentBound = new Rect();

    private Point mCountTextPoint = new Point();
    private Point mTipTextPoint = new Point();

    /* for animation and draw */
    private ValueAnimator mLockAnim;
    private int mRippleAlpha;
    private int mLockTextAlpha;
    private Rect mLockIconBound = new Rect();
    private Rect mLockIconFinalBound = new Rect();
    private Rect mLockIconInitBound = new Rect();

    private boolean mShoudDraw = false;
    private boolean mIsAnimating = false;

    public CircleAnimView(Context context) {
        this(context, null);
    }

    public CircleAnimView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleAnimView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        Resources res = getResources();

        mCountTextSize = res.getDimensionPixelSize(R.dimen.circle_count_text_size);
        mTipTextSize = res.getDimensionPixelSize(R.dimen.circle_tip_text_size);
        mVerticalPadding = res.getDimensionPixelSize(R.dimen.circle_vertical_padding);
        mIconSmallSize = res.getDimensionPixelSize(R.dimen.circle_lock_icon_small_size);
        mIconBigSize = res.getDimensionPixelSize(R.dimen.circle_lock_icon_big_size);
        mTextVerrticalPadding = res.getDimensionPixelSize(R.dimen.circle_text_vertical_padding);

        mInnerCircle = res.getDrawable(R.drawable.home_circle_inner);
        mOuterCircle = res.getDrawable(R.drawable.home_outer_circle);
        mIcon = res.getDrawable(R.drawable.home_lock_icon);

        mTipText = res.getString(R.string.circle_lock_tip_text);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mCountTextSize);
        mPaint.setColor(Color.WHITE);

        LockManager lockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
        List<String> list = lockManager.getCurLockList();
        mCount = list == null ? 0 : list.size();
        mRippleAlpha = 0;
        mLockTextAlpha = 255;

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (!mIsAnimating) {
            computeCirlceBounds(right - left, bottom - top);
        }
    }

    private void computeCirlceBounds(int width, int height) {
        int centerX = width / 2;
        int centerY = height / 2;
        int outerR = centerY - mVerticalPadding;
        mOuterCircleBound.set(centerX - outerR, centerY - outerR, centerX + outerR, centerY
                + outerR);
        int realOutSize = mOuterCircle.getIntrinsicHeight();
        float scale = ((float) mOuterCircleBound.height()) / realOutSize;
        int realInnerSize = mInnerCircle.getIntrinsicHeight();
        int innerSize = (int) (realInnerSize * scale);
        int innerR = innerSize / 2;
        mInnerCircleBound.set(centerX - innerR, centerY - innerR, centerX + innerR, centerY
                + innerR);
        mMaxAnimCircleR = outerR + (mVerticalPadding / 2);

        int iconDrawSize = (int) (innerSize * Math.cos(Math.toRadians(45)));
        int offset = (innerSize - iconDrawSize) / 2;
        mContentBound.set(mInnerCircleBound.left + offset, mInnerCircleBound.top + offset,
                mInnerCircleBound.right - offset, mInnerCircleBound.bottom - offset);
        int initR = mContentBound.width() / 4;
        mLockIconInitBound.set(centerX - initR, centerY - initR, centerX + initR, centerY + initR);

        if (mCount > 0) {
            String count = String.valueOf(mCount);
            mPaint.setStyle(Style.STROKE);
            mPaint.setTextSize(mCountTextSize);
            mPaint.setStrokeWidth(0);
            mPaint.setTypeface(Typeface.DEFAULT_BOLD);
            mPaint.getFontMetrics(mFontMetrics);
            int textOffset = (int) Math.ceil(mFontMetrics.descent - mFontMetrics.ascent) / 10;
            int textSize = computeTextSize(count, mCountTextSize, mContentBound.width()
                    - mIconSmallSize - mTextVerrticalPadding, mPaint);
            offset = (mContentBound.width() - textSize - mIconSmallSize - mTextVerrticalPadding) / 2;
            mCountTextPoint.set(mContentBound.left + offset,
                    mContentBound.top + (mContentBound.height() / 2) + textOffset);
            int iconLeft = mCountTextPoint.x + textSize + mTextVerrticalPadding;
            int iconTop = mContentBound.top + ((mContentBound.height()) / 2) - mIconSmallSize
                    + textOffset;
            mLockIconFinalBound.set(iconLeft, iconTop, iconLeft + mIconSmallSize, iconTop
                    + mIconSmallSize);
            mTipText = getResources().getString(R.string.circle_lock_tip_text);
        } else {
            int iconLeft = mContentBound.left + ((mContentBound.width() - mIconBigSize) / 2);
            int iconTop = mContentBound.top + ((mContentBound.height()) / 2) - mIconBigSize;
            mLockIconFinalBound.set(iconLeft, iconTop, iconLeft + mIconBigSize, iconTop
                    + mIconBigSize);
            mTipText = getResources().getString(R.string.circle_lock_empty_tip_text);
        }
        mRealCountTextSize = (int) mPaint.getTextSize();
        mLockIconBound.set(mLockIconFinalBound);
        mPaint.setStyle(Style.STROKE);
        mPaint.setTextSize(mTipTextSize);
        mPaint.setStrokeWidth(0);
        mPaint.setTypeface(Typeface.DEFAULT);
        int textSize = computeTextSize(mTipText, mTipTextSize, mContentBound.width(), mPaint);
        mPaint.getFontMetrics(mFontMetrics);
        int textH = (int) Math.ceil(mFontMetrics.descent - mFontMetrics.ascent) + 1;
        mTipTextPoint.set(mContentBound.left + (((mContentBound.width() - textSize) / 2)),
                mLockIconFinalBound.bottom + mTextVerrticalPadding + textH);
        mRealTipTextSize = (int) mPaint.getTextSize();
    }

    @Override
    public void draw(Canvas canvas) {
        int centerX = (int) mInnerCircleBound.exactCenterX();
        int centerY = (int) mInnerCircleBound.exactCenterY();

        if (mShoudDraw && mLockTextAlpha > 0) { // draw outer shadow circle
            mOuterCircle.setBounds(mOuterCircleBound);
            mOuterCircle.setAlpha(mLockTextAlpha);
            mOuterCircle.draw(canvas);
        }

        // draw inner content circle
        mInnerCircle.setBounds(mInnerCircleBound);
        mInnerCircle.draw(canvas);

        // for ripple animation
        if (mRippleAlpha > 0) {
            mPaint.setStrokeWidth(2);
            mPaint.setStyle(Style.STROKE);
            mPaint.setAlpha(mRippleAlpha);
            mPaint.setTypeface(Typeface.DEFAULT);
            canvas.drawCircle(centerX, centerY, mAnimCircleBound.width() / 2, mPaint);
        }

        // draw lock icon
        if (mShoudDraw) {
            mIcon.setBounds(mLockIconBound);
            mIcon.draw(canvas);
        }

        if (mShoudDraw && mLockTextAlpha > 0) { // draw text
            if (mCount > 0) {
                mPaint.setStyle(Style.STROKE);
                mPaint.setTextSize(mRealCountTextSize);
                mPaint.setStrokeWidth(0);
                mPaint.setTypeface(Typeface.DEFAULT_BOLD);
                mPaint.setAlpha(mLockTextAlpha);
                canvas.drawText(String.valueOf(mCount), mCountTextPoint.x, mCountTextPoint.y,
                        mPaint);
            }

            mPaint.setStyle(Style.STROKE);
            mPaint.setTextSize(mRealTipTextSize);
            mPaint.setStrokeWidth(0);
            mPaint.setTypeface(Typeface.DEFAULT);
            mPaint.setAlpha(mLockTextAlpha);
            canvas.drawText(mTipText, mTipTextPoint.x, mTipTextPoint.y, mPaint);
        }
    }

    private int computeTextSize(String text, int maxTextSize, int maxWidth, Paint paint) {
        int textSize = maxTextSize;
        paint.setTextSize(textSize);
        int textWidth = (int) paint.measureText(text);
        while (textWidth > maxWidth) {
            textSize = textSize - 1;
            paint.setTextSize(textSize);
            textWidth = (int) paint.measureText(text);
        }
        return textWidth;
    }

    public void setLockedCount(int count) {
        mCount = count;
        int width = getWidth();
        int height = getHeight();
        if (width > 0 && height > 0) {
            computeCirlceBounds(width, height);
            if (mShoudDraw) {
                invalidate();
            }
        }
    }

    public void invalidateDraw(boolean draw) {
        mShoudDraw = draw;
    }

    public void palyAnim() {
        mShoudDraw = true;
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        cancelAnim();
        mLockAnim = new ValueAnimator();
        mLockAnim.setDuration(LOCK_ANIM_DURATION);
        mLockAnim.setFloatValues(0.0f, 1.0f);
        mLockAnim.removeAllUpdateListeners();
        mLockAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                final float percent = (Float) animation.getAnimatedValue();
                if (percent < 0.4f) {
                    mLockTextAlpha = 0;
                } else {
                    mLockTextAlpha = (int) (255 * (percent - 0.4f) * 1.66f);
                }

                mRippleAlpha = (int) (255 * (1 - percent));

                if (percent < 0.2f) {
                    mLockIconBound.set(mLockIconInitBound);
                } else {
                    int fromSize = mLockIconInitBound.width();
                    int toSize = mLockIconFinalBound.width();
                    float realPercent = (percent - 0.2f) * 1.25f;
                    int left = mLockIconInitBound.left
                            + (int) Math
                                    .round(((mLockIconFinalBound.left - mLockIconInitBound.left) * realPercent));
                    int top = mLockIconInitBound.top
                            + (int) Math
                                    .round(((mLockIconFinalBound.top - mLockIconInitBound.top) * realPercent));
                    int size = fromSize + (int) Math.round((toSize - fromSize) * realPercent);
                    mLockIconBound.set(left, top, left + size, top + size);
                }

                int centerX = (int) mContentBound.exactCenterX();
                int centerY = (int) mContentBound.exactCenterY();
                int animR = mMaxAnimCircleR
                        + (int) Math.round((((mInnerCircleBound.width() / 2) - mMaxAnimCircleR) * percent));
                mAnimCircleBound.set(centerX - animR, centerY - animR, centerX + animR, centerY
                        + animR);

                invalidate();
            }
        });
        mLockAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIsAnimating = true;
            }

            public void onAnimationEnd(Animator animation) {
                mRippleAlpha = 0;
                mLockTextAlpha = 255;
                mLockIconBound.set(mLockIconFinalBound);
                mIsAnimating = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mRippleAlpha = 0;
                mLockTextAlpha = 255;
                mLockIconBound.set(mLockIconFinalBound);
                mIsAnimating = false;
            }
        });
        mLockAnim.start();
    }

    public void cancelAnim() {
        if (mLockAnim != null) {
            mLockAnim.end();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                int upX = (int) event.getX();
                int upY = (int) event.getY();
                if (mOuterCircleBound.contains(upX, upY)) {
                    performClick();
                }
                break;
        }
        return true;
    }
    
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {          
        }
    }

}
