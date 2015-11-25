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
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.home.HomePrivacyFragment;
import com.leo.appmaster.home.SimpleAnimatorListener;
import com.leo.appmaster.utils.LeoLog;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorSet;
import com.leo.tools.animator.ObjectAnimator;
import com.leo.tools.animator.ValueAnimator;

/**
 * Created by Jasper on 2015/10/26.
 */
public class HomeUpArrow extends View implements SlidingUpPanelLayout.TapRectFunction {
    private static final String TAG ="HomeUpArrow";

    public static final int FULL_COLOR = Color.parseColor("#f8f8f8");
//    public static final int FULL_COLOR = Color.parseColor("#ff00ff");

    private BitmapDrawable mUpIcon;
    private Paint mPaint;

    private int mDividerColor;
    private int mBgAlpha = 0;

    private Drawable mRedTip;

    private AnimatorSet mUpAnim;
    private boolean mShowRedTip = true;
    private boolean mReversed;

    private int mTapWidth;

    private int mArrowAlpha;
    private int mTranslateY;
    private int mMaxTranslateY;
    private boolean mCanceled;

    public HomeUpArrow(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(FULL_COLOR);

        mDividerColor = context.getResources().getColor(R.color.c5);
        mUpIcon = (BitmapDrawable) context.getResources().getDrawable(R.drawable.ic_up_arrow);
        mRedTip = context.getResources().getDrawable(R.drawable.red_dot);

        mTapWidth = context.getResources().getDimensionPixelSize(R.dimen.home_more_arrow_width);
        mMaxTranslateY = context.getResources().getDimensionPixelSize(R.dimen.arrow_translate);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int iw = mUpIcon.getIntrinsicWidth();
        int ih = mUpIcon.getIntrinsicHeight();

        int l = getLeft() + (w - iw) / 2;
        int t = getTop() + (h - ih) / 2;
        mUpIcon.setBounds(l, t, l + iw, t + ih);

        Rect rect = mUpIcon.getBounds();

        int rw = mRedTip.getIntrinsicWidth();
        int rh = mRedTip.getIntrinsicHeight();
        int rl = rect.right - rw / 2;
        int rt = rect.top;
        mRedTip.setBounds(rl, rt, rl + rw, rt + rh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(FULL_COLOR);
        mPaint.setAlpha(mBgAlpha);
        canvas.drawRect(getLeft(), getTop(), getRight(), getBottom(), mPaint);

        mPaint.setColor(mDividerColor);
        canvas.drawLine(getLeft(), getBottom(), getRight(), getBottom(), mPaint);
        canvas.save();
        if (mReversed) {
            Rect bounds = mUpIcon.getBounds();
            canvas.rotate(180, bounds.centerX(), bounds.centerY());
            mUpIcon.getPaint().setAlpha(255);
        } else {
            canvas.translate(0, -mTranslateY);
            if (mTranslateY < mMaxTranslateY) {
                mUpIcon.getPaint().setAlpha(255);
            } else {
                mUpIcon.getPaint().setAlpha(mArrowAlpha);
            }
        }
        mUpIcon.draw(canvas);
        if (mShowRedTip) {
            mRedTip.draw(canvas);
        }
        canvas.restore();

    }

    public void startUpAnimation() {
        if (mUpAnim != null) {
            mUpAnim.cancel();
        }

        ObjectAnimator tranAnim = ObjectAnimator.ofInt(this, "translateY", 0, mMaxTranslateY);
        tranAnim.setDuration(600);

        ObjectAnimator alphaAnim = ObjectAnimator.ofInt(this, "arrowAlpha", 255, 0);
        alphaAnim.setDuration(400);
        alphaAnim.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mCanceled) return;

                LeoLog.i(TAG, "alpha anim end.");
                ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startUpAnimation();
                    }
                }, 400);
            }
        });
        mUpAnim = new AnimatorSet();
        mUpAnim.playSequentially(tranAnim, alphaAnim);
        mUpAnim.start();
    }

    public void release() {
        if (mUpAnim != null) {
            try {
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
        mArrowAlpha = 255;
        mTranslateY = 0;
        mCanceled = true;
        invalidate();
    }

    public void reverse() {
        cancelUpAnimation();
        mReversed = true;
        invalidate();
    }

    public void reset() {
        cancelUpAnimation();
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

    public void setTranslateY(int translateY) {
        mTranslateY = translateY;
        invalidate();
    }

    public void setArrowAlpha(int arrowAlpha) {
        mArrowAlpha = arrowAlpha;
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
