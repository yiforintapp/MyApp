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
    
    private float mFlipDegreeY = 0f;
    private int mCurrentStatus = 0;
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
//        setParentLayer(mParent);
        HomeAnimShieldLayer shieldLayer = (HomeAnimShieldLayer) mParent;
        Camera camera = new Camera();
        camera.rotateY(mFlipDegreeY);  
//        camera.ro
        camera.getMatrix(matrix);
//<<<<<<< Upstream, based on branch 'lishuai_dev_3.2' of http://gitlab.leoers.com/leo/appmaster.git
//        int centerX = mParent.centerX();
//=======
        int centerX = mParent.centerX() - shieldLayer.getMaxOffsetX();
//        int centerX = mParent.centerX();
//        int centerX = (int) shieldLayer.mCirclePx;
        LeoLog.i("tesiX", "centerX = " + centerX);
//>>>>>>> e809fac 满分动画3
        int centerY = mParent.centerY();
        matrix.preTranslate(-centerX, -centerY);  
        matrix.postTranslate(centerX, centerY); 
    }
    
    public void startFlipAnim(long duration, final OnFlipEndListener listener) {
        PropertyValuesHolder v1 = PropertyValuesHolder.ofFloat("flipDegreeY", 0f, 180f);
        final ObjectAnimator animatorI = ObjectAnimator.ofPropertyValuesHolder(this, v1);
        animatorI.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                LeoLog.i("tesi", "mFlipDegreeY = " + mFlipDegreeY);
                float animatedValue = (Float) animation.getAnimatedValue("flipDegreeY");
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
