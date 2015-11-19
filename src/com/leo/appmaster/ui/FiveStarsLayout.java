package com.leo.appmaster.ui;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorListenerAdapter;
import com.leo.tools.animator.AnimatorSet;
import com.leo.tools.animator.ObjectAnimator;
import com.leo.tools.animator.PropertyValuesHolder;

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
    private ImageView mGradeGesture;
    private float mGestureY;
    private float mFifthStarCenterX;
    private Context mContext;
    private float mGestureDeltaY;
    private FrameLayout mFlFifthStar;
    private boolean mHasShowed = false;
    
    public FiveStarsLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FiveStarsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FiveStarsLayout(Context context) {
        this(context, null);
        mContext = context;
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View view = LayoutInflater.from(getContext()).inflate(R.layout.framelayout_five_stars, this, true);
        mOneStar = (ImageView) findViewById(R.id.one_star);
        mTwoStar = (ImageView) findViewById(R.id.two_star);
        mThreeStar = (ImageView) findViewById(R.id.three_star);
        mFourStar = (ImageView) findViewById(R.id.four_star);
        mFiveStar = (ImageView) findViewById(R.id.five_star);
        mGradeGesture = (ImageView) findViewById(R.id.grade_gesture);
        mFlFifthStar = (FrameLayout) findViewById(R.id.fl_fifthStar);
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        int height = getHeight();
        int width = getWidth();
        if(!mHasShowed) {
            mGestureY = mGradeGesture.getY();
//            mFifthStarCenterX = mFlFifthStar.getLeft();
            mGestureDeltaY = height / 2 - mGradeGesture.getHeight();
//            mGradeGesture.setLeft((int) mFifthStarCenterX);
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
        
        PropertyValuesHolder gInY = PropertyValuesHolder.ofFloat("y", mGestureY, mGestureY - mGestureDeltaY);
        PropertyValuesHolder gInAlpha = PropertyValuesHolder.ofFloat("alpha", 0f, 1.0f);
        
        PropertyValuesHolder gOutY = PropertyValuesHolder.ofFloat("y", mGestureY - mGestureDeltaY, mGestureY);
        PropertyValuesHolder gOutAlpha = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0f);
        
        final ObjectAnimator animatorI = ObjectAnimator.ofPropertyValuesHolder(mGradeGesture, gInY, gInAlpha);
        animatorI.setDuration(2000);
        final ObjectAnimator animatorO = ObjectAnimator.ofPropertyValuesHolder(mGradeGesture, gOutY, gOutAlpha);
        animatorO.setDuration(2000);

        AnimatorSet starAnimator = new AnimatorSet();
        starAnimator.playSequentially(oneStar, twoStar, threeStar, fourStar, fiveStar);

        ObjectAnimator emptyObjectAnimator = ObjectAnimator.ofFloat(mFiveStar, "alpha", 1f, 1f);
        emptyObjectAnimator.setDuration(1000);

        final AnimatorSet animatorSet = new AnimatorSet();
//        animatorSet.playSequentially(starAnimator);
        animatorSet.play(animatorI).before(starAnimator);
        animatorSet.play(starAnimator).with(animatorO);
        animatorSet.play(emptyObjectAnimator).after(starAnimator);

        animatorSet.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mGradeGesture.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                changeStarInvisible();
                animatorSet.start();
            }
        });
        animatorSet.start();
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
