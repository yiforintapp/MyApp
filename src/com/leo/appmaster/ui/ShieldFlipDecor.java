package com.leo.appmaster.ui;

import com.leo.appmaster.home.SimpleAnimatorListener;
import com.leo.appmaster.utils.LeoLog;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.Animator.AnimatorListener;
import com.leo.tools.animator.ObjectAnimator;
import com.leo.tools.animator.PropertyValuesHolder;
import com.leo.tools.animator.ValueAnimator;
import com.leo.tools.animator.ValueAnimator.AnimatorUpdateListener;

import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;

/**
 * 满分动画盾牌翻转装饰
 * Created by Jasper on 2015/12/24.
 */
public class ShieldFlipDecor extends BaseDecor {
    private Matrix mMatrix;
    
    private float mFlipDegreeY = 0f;
    private int mCurrentStatus = 0;

    private float mCurrDegree;

    public ShieldFlipDecor() {
        super();
        mMatrix = new Matrix();
    }
    public int getNeedFlipScore() {
        return mCurrentStatus;
    }

    public void setNeedFlipScore(int currentStatus) {
        this.mCurrentStatus = currentStatus;
    }

    public float getFlipDegreeY() {
        return mFlipDegreeY;
    }
    
    public void setFlipDegreeY(float degree) {
        mFlipDegreeY = degree;
    }
    
    @Override
    public void applyDecor(Canvas canvas, Matrix matrix) {
        if (matrix == null || mCurrDegree == 0f || mCurrDegree == 180f) {
            return;
        }
        mMatrix.set(Matrix.IDENTITY_MATRIX);
        int centerX = mParent.centerX();
        int centerY = mParent.centerY();

        Camera camera = new Camera();
        camera.rotateY(mFlipDegreeY);
        camera.getMatrix(mMatrix);
        mMatrix.preTranslate(-centerX, -centerY);
        mMatrix.postTranslate(centerX, centerY);

        matrix.postConcat(mMatrix);
    }
    
    public void startFlipAnim(long duration, final OnFlipEndListener listener) {
        PropertyValuesHolder v1 = PropertyValuesHolder.ofFloat("flipDegreeY", 0f, 180f);
        final ObjectAnimator animatorI = ObjectAnimator.ofPropertyValuesHolder(this, v1);
        animatorI.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                LeoLog.i("tesi", "mFlipDegreeY = " + mFlipDegreeY);
                float animatedValue = (Float) animation.getAnimatedValue("flipDegreeY");
                mCurrDegree = animatedValue;
                if (animatedValue == 90f) {
                    mCurrentStatus ++;
                }
            }
        });
        animatorI.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }
            
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
            
            @Override
            public void onAnimationEnd(Animator animation) {
                if (listener != null) {
                    listener.OnFlipEnd();
                }
            }
            
            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
        animatorI.setDuration(duration);
        animatorI.start();
    }
    
    public interface OnFlipEndListener {
        public void OnFlipEnd();
    }
}
