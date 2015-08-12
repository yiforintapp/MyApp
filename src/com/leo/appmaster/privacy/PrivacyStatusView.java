package com.leo.appmaster.privacy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.leo.appmaster.R;
import com.leo.appmaster.privacy.PrivacyHelper.Level;

public class PrivacyStatusView extends View {
    
    private final static int ONEDRAWTIME = 350;
    private final static int LEVELOFFSET = 5;
    
    private Level mLevel;
    
    private Paint mPaint;
    
    private ValueAnimator mAnimator;
    private boolean mAnimating;
    private boolean[] mDrawing  = new boolean[5];
    private int[] mAlpha = new int[5];
    
    private Drawable mStatusBg;
    private Drawable mStatusFrame;
    private Drawable mStatusOne;
    private Drawable mStatusTwo;
    private Drawable mStatusThree;
    private Drawable mStatusFour;
    private Drawable mStatusFive;
    
    public PrivacyStatusView(Context context) {
        this(context, null);
    }

    public PrivacyStatusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PrivacyStatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mLevel = Level.LEVEL_ONE;
        mPaint = new Paint();
        mPaint.setStyle(Style.FILL);
        Resources res = getResources();
        mStatusBg = res.getDrawable(R.drawable.level_bg_2);
        mStatusFrame = res.getDrawable(R.drawable.level_bg_1);
    }
    
    @Override
    public void draw(Canvas canvas) {
        Resources res = getResources();
        int width = getWidth();
        int height = getHeight();
        int delta = width / 5;
        mPaint.setAlpha(255);
        mStatusBg.setBounds(0, 0, width, height);
        mStatusBg.draw(canvas);
        if(mDrawing[4]) {
            if(mStatusFive == null) {
                mStatusFive = res.getDrawable(R.drawable.level_5);
            }
            mStatusFive.setBounds(delta * 4 - LEVELOFFSET, 0, width, height);
            mStatusFive.setAlpha(mAlpha[4]);
            mStatusFive.draw(canvas);
        }
        if(mDrawing[3]) {
            if(mStatusFour == null) {
                mStatusFour = res.getDrawable(R.drawable.level_4);
            }
            mStatusFour.setBounds(delta * 3 - LEVELOFFSET, 0, delta * 4, height);
            mStatusFour.setAlpha(mAlpha[3]);
            mStatusFour.draw(canvas);
        }
        if(mDrawing[2]) {
            if(mStatusThree == null) {
                mStatusThree = res.getDrawable(R.drawable.level_3);
            }
            mStatusThree.setBounds(delta * 2 - LEVELOFFSET, 0, delta * 3, height);
            mStatusThree.setAlpha(mAlpha[2]);
            mStatusThree.draw(canvas);
        }
        if(mDrawing[1]) {
            if(mStatusTwo == null) {
                mStatusTwo = res.getDrawable(R.drawable.level_2);
            }
            mStatusTwo.setBounds(delta - LEVELOFFSET, 0, delta * 2, height);
            mStatusTwo.setAlpha(mAlpha[1]);
            mStatusTwo.draw(canvas);
        }
        if(mDrawing[0]) {
            if(mStatusOne == null) {
                mStatusOne = res.getDrawable(R.drawable.level_1);
            }
            mStatusOne.setBounds(0, 0, delta, height);
            mStatusOne.setAlpha(mAlpha[0]);
            mStatusOne.draw(canvas);
        }
        
        mStatusFrame.setBounds(0, 0, width, height);
        mStatusFrame.draw(canvas);
    }

    public void invalidate(Level level, boolean init) {
        mLevel = level;
        if(init) {
            for(int i = 0; i < mDrawing.length; i++) {
                mDrawing[i] = false;
                mAlpha[i] = 0;
            }
        } else {
            updateLevel();
        }
        invalidate();
    }
    
    private void updateLevel() {
        int level = mLevel.ordinal();
        for(int i = 0; i < mDrawing.length; i++) {
            if(i <= level) {
                mDrawing[i] = true;
                mAlpha[i] = 255;
            } else {
                mDrawing[i] = false;
                mAlpha[i] = 0;
            }
        }
    }

    public void playAnim() {        
        if(mAnimating) {
            return;
        }
        if(mAnimator != null) {
            mAnimator.cancel();
        }
        final int length = mLevel.ordinal() + 1;
        mAnimator = new ValueAnimator();
        mAnimator.setDuration((length + 4) * ONEDRAWTIME);
        mAnimator.setIntValues(0, (length + 4) * ONEDRAWTIME);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.removeAllUpdateListeners();
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                int level = mLevel.ordinal();
                final int percent = (Integer) animation.getAnimatedValue();
               int num = percent / ONEDRAWTIME;
               if(num < level) {
                   for(int i = 0; i <= num; i++) {
                       mDrawing[i] = true;
                       int delta = percent - i * ONEDRAWTIME;
                       if(delta >=0 && delta < ONEDRAWTIME) {
                           mAlpha[i] = (int)(((float) delta) / ONEDRAWTIME * 255);
                       } else {
                           mAlpha[i] = 255;
                       }
                   }
               } else {
                   int delta = percent - num * ONEDRAWTIME;
                   int offset = (num - level) % 2;
                   mDrawing[level] = true;
                   if( offset == 0) {
                       mAlpha[level] =  (int)(((float) delta) / ONEDRAWTIME * 255);
                   } else {
                       mAlpha[level] =  255 - (int)(((float) delta) / ONEDRAWTIME * 255);
                   }
               }
               invalidate();
            }
        });
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimating = true;
                for(int i = 0; i < mDrawing.length; i++) {
                    mDrawing[i] = false;
                }
            }

            public void onAnimationEnd(Animator animation) {
                mAnimating = false;
                updateLevel();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mAnimating = false;
                updateLevel();
            }
        });
        mAnimator.start();
    }

    public void cancelAnimation() {
        if(mAnimator != null) {
            mAnimator.cancel();
        }
    }

}
