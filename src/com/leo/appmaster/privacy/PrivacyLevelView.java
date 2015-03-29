
package com.leo.appmaster.privacy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.leo.appmaster.R;

public class PrivacyLevelView extends View {
    private  final static long LOCK_ANIM_DURATION = 800;

    private final static float SMALL_TEXT_POS_PERCENT = 0.27f;
    private final static float SMALL_TEXT_WIDTH_PERCENT = 0.25f;
    private final static float BIG_TEXT_WIDTH_PERCENT = 0.56f;
    private final static float SEPRATOR_WIDTH_PERCENT = 0.4f;

    private Paint mPaint;
    private FontMetrics mFontMetrics = new FontMetrics();

    private Drawable mIcon;
    private Bitmap mAnim;
    private Drawable mSeprator;

    private int mSepratorPadding;
    private int mDrawPadding;
    
    private Rect mIconDrawBount = new Rect();
    private Rect mLastIconDrawBount = new Rect();
    private Rect mSepratorBound = new Rect();
    
    private Point mSmallTextPoint = new Point();
    private Point mBigTextPoint = new Point();
    
    private int mSmallTextSize;
    private int mBigTextSize;
    
    private int mRealSmallTextSize;
    private int mRealBigTextSize;

    private String mLevelText;
    
    private int mColor = -1;
    
    private ValueAnimator mAnimator;
    private int mAlpha = 255;
    protected boolean mIsAnimating;

    public PrivacyLevelView(Context context) {
        this(context, null);
    }

    public PrivacyLevelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PrivacyLevelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        Resources res = getResources();

        mDrawPadding = res.getDimensionPixelSize(R.dimen.privacy_level_padding);
        mSepratorPadding = res.getDimensionPixelSize(R.dimen.privacy_level_seprator_padding);
        mSmallTextSize = res.getDimensionPixelSize(R.dimen.privacy_level_small_text_size);
        mBigTextSize = res.getDimensionPixelSize(R.dimen.privacy_level_big_text_size);
        
        mLevelText = res.getString(R.string.privacy_level);

        mIcon = res.getDrawable(R.drawable.privacy_level_bg);
        mAnim = BitmapFactory.decodeResource(res, R.drawable.privacy_level_bg_anim);
        mSeprator = res.getDrawable(R.drawable.privacy_level_bg_line);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        computeDrawBounds(right - left, bottom - top);
    }

    private void computeDrawBounds(int width, int height) {
        int centerX = width / 2;
        int centerY = height / 2;
        int contentW = width - 2 * mDrawPadding;
        int contentH = height - 2 * mDrawPadding;
        int iconW = mIcon.getIntrinsicWidth();
        int iconH = mIcon.getIntrinsicHeight();
        float scaleW = (float) contentW / iconW;
        float scaleH = (float) contentH / iconH;
        float scale = scaleW < scaleH ? scaleW : scaleH;
        
        int drawW = (int) (iconW * scale);
        int drawH = (int) (iconH * scale);
        
        mIconDrawBount.left = centerX - drawW / 2;
        mIconDrawBount.right = mIconDrawBount.left + drawW;
        mIconDrawBount.top = centerY - drawH / 2;
        mIconDrawBount.bottom =  mIconDrawBount.top + drawH;
        
        if(!mLastIconDrawBount.equals(mIconDrawBount)) {
            int maxTextWidth = (int)(drawW * SMALL_TEXT_WIDTH_PERCENT);
            int textSize = computeTextSize(mLevelText, mSmallTextSize, maxTextWidth, mPaint);
            mRealSmallTextSize = (int)mPaint.getTextSize();
            mPaint.getFontMetrics(mFontMetrics);
            int offset =  (int) Math.ceil(mFontMetrics.descent - mFontMetrics.ascent) + 1;
            mSmallTextPoint.set(mIconDrawBount.left + (drawW - textSize) / 2, mIconDrawBount.top + ((int)(drawH * SMALL_TEXT_POS_PERCENT)) + offset);
            
            int sepratorW = (int)(drawW * SEPRATOR_WIDTH_PERCENT);       
            mSepratorBound.left = centerX - sepratorW / 2;
            mSepratorBound.right = mSepratorBound.left + sepratorW;
            mSepratorPadding = (int)(mSepratorPadding * scale);
            if(mSepratorPadding < 5) {
                mSepratorPadding = 5;
            }
            mSepratorBound.top = mSmallTextPoint.y + mSepratorPadding;
            mSepratorBound.bottom =  mSepratorBound.top + mSeprator.getIntrinsicHeight();
            
            mLastIconDrawBount.set(mIconDrawBount);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        // background
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Style.FILL);
        canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
        
        // icon background
        PrivacyHelper ph = PrivacyHelper.getInstance(getContext());
        mPaint.setColor(mColor == -1 ? ph.getCurLevelColor().toIntColor() : mColor);
        mPaint.setStyle(Style.FILL);
        canvas.drawRect(mIconDrawBount, mPaint);
        
        // icon
        mIcon.setBounds(mIconDrawBount);
        mIcon.draw(canvas);
        
        // small text
        mPaint.setTextSize(mRealSmallTextSize);
        mPaint.setStyle(Style.FILL);
        mPaint.setColor(Color.WHITE);
        canvas.drawText(mLevelText, mSmallTextPoint.x, mSmallTextPoint.y, mPaint);
        
        //separator
        mSeprator.setBounds(mSepratorBound);
        mSeprator.draw(canvas);
        
        // big text
        int drawW = mIconDrawBount.width();
        int maxTextWidth = (int)(drawW * BIG_TEXT_WIDTH_PERCENT);
        String text = ph.getLevelDescription(ph.getPrivacyLevel());
        int textSize = computeTextSize(text, mBigTextSize, maxTextWidth, mPaint);
        mRealBigTextSize = (int)mPaint.getTextSize();
        mPaint.getFontMetrics(mFontMetrics);
        int offset =  (int) Math.abs(mFontMetrics.ascent) - 2;
        mBigTextPoint.set(mIconDrawBount.left + (drawW - textSize) / 2, mSepratorBound.top + mSepratorPadding + offset);
        mPaint.setTextSize(mRealBigTextSize);
        mPaint.setStyle(Style.FILL);
        mPaint.setColor(Color.WHITE);
        canvas.drawText(text, mBigTextPoint.x, mBigTextPoint.y, mPaint);
        
        //animation
        mPaint.setAlpha(mAlpha);
        canvas.drawBitmap(mAnim, null, mIconDrawBount, mPaint);
        mPaint.setAlpha(255);
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
    
    public void invalidate(int color) {
        mColor = color;
        invalidate();
    }
    
    public void palyAnim() {
        if(getWidth()  <= 0 || getHeight() <= 0) {
            return;
        }
        cancelAnim();
        mAnimator = new ValueAnimator();
        mAnimator.setDuration(LOCK_ANIM_DURATION);
        mAnimator.setFloatValues(1.0f, 0.0f);
        mAnimator.removeAllUpdateListeners();
        mAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                final float percent = (Float) animation.getAnimatedValue();
                mAlpha = (int)(255 * percent);
                invalidate();
            }
        });
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                 mIsAnimating = true;
            }
            
            public void onAnimationEnd(Animator animation) {
                mAlpha = 255;
                mIsAnimating = false;
            }
            
            @Override
            public void onAnimationCancel(Animator animation) {
                mAlpha = 255;
                mIsAnimating = false;
            }
        });
        mAnimator.start();
    }
    
    public void cancelAnim() {
        if(mAnimator != null) {
            mAnimator.end();
            mAnimator = null;
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                int upX = (int) event.getX();
                int upY = (int) event.getY();
                if (mIconDrawBount.contains(upX, upY)) {
                    performClick();
                }
                break;
        }
        cancelAnim();
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
