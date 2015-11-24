package com.leo.appmaster.ui;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorListenerAdapter;
import com.leo.tools.animator.AnimatorSet;
import com.leo.tools.animator.ObjectAnimator;
import com.leo.tools.animator.PropertyValuesHolder;
import com.leo.tools.animator.ValueAnimator;
import com.leo.tools.animator.ValueAnimator.AnimatorUpdateListener;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class FiveStarsLayout extends FrameLayout{

    /** 评分星星 */
    private ImageView mOneStar;
    private ImageView mTwoStar;
    private ImageView mThreeStar;
    private ImageView mFourStar;
    private ImageView mFiveStar;
    private ImageView mFiveStarBg;
    private ImageView mGradeGesture;
    private float mGestureY;
    private float mGestureX;
    private float mFifthStarCenterX;
    private float mGRightX;
    private float mGestureDeltaY;
    private FrameLayout mFlFifthStar;
    private boolean mHasShowed = false;
    private boolean mNeedRepeat = true;
    private AnimatorSet mAsMain;
    
    public FiveStarsLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FiveStarsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FiveStarsLayout(Context context) {
        this(context, null);
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View view = LayoutInflater.from(getContext()).inflate(R.layout.framelayout_five_stars, this, true);
        mOneStar = (ImageView) findViewById(R.id.one_star);
        mTwoStar = (ImageView) findViewById(R.id.two_star);
        mThreeStar = (ImageView) findViewById(R.id.three_star);
        mFourStar = (ImageView) findViewById(R.id.four_star);
        mFiveStarBg = (ImageView) findViewById(R.id.five_star_bg);
        mFiveStar = (ImageView) findViewById(R.id.five_star);
        mGradeGesture = (ImageView) findViewById(R.id.grade_gesture);
        mFlFifthStar = (FrameLayout) findViewById(R.id.fl_fifthStar);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mNeedRepeat = false;
        mAsMain.cancel();
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        int height = getHeight();
        int width = getWidth();
        if(!mHasShowed) {
            mGestureY = mGradeGesture.getY();
            mGestureX = mGradeGesture.getX();
            mFifthStarCenterX = mFlFifthStar.getLeft() + mFlFifthStar.getWidth() / 2 - DipPixelUtil.dip2px(mContext, 7);
            mGestureDeltaY = height / 2 - mGradeGesture.getHeight();
            float dx = (float)DipPixelUtil.dip2px(mContext, 12);
            mGRightX = mFifthStarCenterX + dx;
            mGradeGesture.setX(mGRightX);
            LeoLog.i("testtt", "before mGRightX = "+mGRightX);
            LeoLog.i("testtt", "before mFifthStarCenterX = "+mFifthStarCenterX);
            LeoLog.i("testtt", "before dx = "+dx);
            showStarAnimation();
        }
    }
    
    /***
     * 开始动画
     */
    private void showStarAnimation() {
        mHasShowed = true;
        ObjectAnimator oneStar = getObjectAnimator(mOneStar);
        ObjectAnimator twoStar = getObjectAnimator(mTwoStar);
        ObjectAnimator threeStar = getObjectAnimator(mThreeStar);
        ObjectAnimator fourStar = getObjectAnimator(mFourStar);
        ObjectAnimator fiveStar = getObjectAnimator(mFiveStar);
        
        PropertyValuesHolder gInY = PropertyValuesHolder.ofFloat("y", mGestureY, mGestureY - mGestureDeltaY / 2, mGestureY - mGestureDeltaY, mGestureY - mGestureDeltaY);
        PropertyValuesHolder gInX = PropertyValuesHolder.ofFloat("x", mGRightX, mGRightX / 2 + mFifthStarCenterX / 2, mFifthStarCenterX, mFifthStarCenterX);
        
        LeoLog.i("testtt", "mGRightX = "+mGRightX);
        LeoLog.i("testtt", "mFifthStarCenterX = "+mFifthStarCenterX);
        
        PropertyValuesHolder gInAlpha = PropertyValuesHolder.ofFloat("alpha", 0f, 1.0f);
        
        PropertyValuesHolder gOutY = PropertyValuesHolder.ofFloat("y", mGestureY - mGestureDeltaY, mGestureY);
        PropertyValuesHolder gOutX = PropertyValuesHolder.ofFloat("x", mFifthStarCenterX, mGRightX);
        PropertyValuesHolder gOutAlpha = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0f);
        
        final ObjectAnimator animatorI = ObjectAnimator.ofPropertyValuesHolder(mGradeGesture, gInY, gInX, gInAlpha);
        animatorI.setDuration(2000);
        animatorI.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                LeoLog.i("testing", " running!!  ");
            }
        });
        
        final ObjectAnimator animatorO = ObjectAnimator.ofPropertyValuesHolder(mGradeGesture, gOutY, gOutX, gOutAlpha);
        animatorO.setDuration(2000);

        PropertyValuesHolder starScaleXAnim = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0.7f, 1.0f);
        PropertyValuesHolder starScaleYAnim = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0.7f, 1.0f);
        final ObjectAnimator animatorScale = ObjectAnimator.ofPropertyValuesHolder(mFiveStarBg, starScaleXAnim, starScaleYAnim);
        animatorScale.setDuration(500);
        
        AnimatorSet starAnimator = new AnimatorSet();
        starAnimator.playSequentially(oneStar, twoStar, threeStar, fourStar, fiveStar);

        ObjectAnimator emptyObjectAnimator = ObjectAnimator.ofFloat(mFiveStar, "alpha", 1f, 1f);
        emptyObjectAnimator.setDuration(1000);

        if(mAsMain == null) {
            mAsMain = new AnimatorSet();
        }
//        animatorSet.playSequentially(starAnimator);
        mAsMain.play(animatorI).before(starAnimator);
        mAsMain.play(animatorScale).after(animatorI);
        
        mAsMain.play(starAnimator).with(animatorO).after(animatorScale);
        mAsMain.play(emptyObjectAnimator).after(starAnimator);
        
        mAsMain.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mGradeGesture.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                changeStarInvisible();
                if(mNeedRepeat) {
                    mAsMain.start();
                }
            }
        });
        mAsMain.start();
    }
    
    private ObjectAnimator getObjectAnimator(ImageView img) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(img, "alpha", 0f, 1f);
        objectAnimator.setDuration(250);
        objectAnimator.addListener(new ObjectAnimStartListener(img));
        return  objectAnimator;
    }
    
    /**每个星星动画开始监听*/
    private class ObjectAnimStartListener extends AnimatorListenerAdapter {

        private ImageView theView;
        public ObjectAnimStartListener (ImageView imageView) {
            this.theView = imageView;
        }

        @Override
        public void onAnimationStart(Animator animation) {
            super.onAnimationStart(animation);
            theView.setVisibility(View.VISIBLE);
        }
    }
    
    /***
     * 设置所有星星不可见
     */
    private void changeStarInvisible() {
        mOneStar.setVisibility(View.INVISIBLE);
        mTwoStar.setVisibility(View.INVISIBLE);
        mThreeStar.setVisibility(View.INVISIBLE);
        mFourStar.setVisibility(View.INVISIBLE);
        mFiveStar.setVisibility(View.INVISIBLE);
    }
}
