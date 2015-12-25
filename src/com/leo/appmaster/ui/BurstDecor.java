package com.leo.appmaster.ui;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.LeoLog;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.Animator.AnimatorListener;
import com.leo.tools.animator.ObjectAnimator;
import com.leo.tools.animator.PropertyValuesHolder;
import com.leo.tools.animator.ValueAnimator;
import com.leo.tools.animator.ValueAnimator.AnimatorUpdateListener;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;

/**
 * 满分动画爆裂装饰
 * Created by Jasper on 2015/12/24.
 */
public class BurstDecor extends BaseDecor {
    private float mCanvasRotateDegree = 0f;
    private float mMostFarDistance = 400f;
    private double[] mBurstDegrees = {36d, 72d, 108d, 144d, 180d, 216d, 252d, 288d, 324d, 360d};
    private long mMaxDuration = 640;
    private float[] mDistanceFirstWave = new float[10];
    private float[] mDistanceSecondWave = new float[10];
    private float[] mDistanceThirdWave = new float[10];
    private float mCurrentProcess = 0;
    private Paint mPaint;
    public float getCurrentProcess() {
        return mCurrentProcess;
    }

    public void setCurrentProcess(float currentProcess) {
        this.mCurrentProcess = mCurrentProcess;
    }

    @Override
    public void applyDecor(Canvas canvas, Matrix matrix) {
        canvas.save();
        //描述进度的值为0或者超过1,return;
        if (mCurrentProcess == 0 ||mCurrentProcess >= 1) {
            return;
        }
        //初始化painter
        if (mPaint == null) {
            mPaint = new Paint();
        }
        mPaint.setAlpha(100);
        mPaint.setColor(Color.WHITE);  
        mPaint.setAntiAlias(true); 
        mPaint.setStyle(Style.STROKE);   
        mPaint.setStrokeWidth(15f);  
        //移动进度临近目标时，开始变透明
        if (mCurrentProcess >= 0.8f) {
            mPaint.setAlpha(mPaint.getAlpha() + 30);
        }
        //根据移动距离计算坐标并绘制圆点
        for (int i = 0; i < mDistanceFirstWave.length; i++) {
            canvas.drawCircle(mParent.centerX() + calculateCircleX((float) mBurstDegrees[i], mDistanceFirstWave[i]),
                                             mParent.centerY() + calculateCircleY((float) mBurstDegrees[i], mDistanceFirstWave[i]) , 2, mPaint);
        }
        for (int i = 0; i < mDistanceSecondWave.length; i++) {
            canvas.drawCircle(mParent.centerX() + calculateCircleX((float) mBurstDegrees[i], mDistanceSecondWave[i]),
                                             mParent.centerY() + calculateCircleY((float) mBurstDegrees[i], mDistanceSecondWave[i]) , 2, mPaint);
        }
        for (int i = 0; i < mDistanceThirdWave.length; i++) {
            canvas.drawCircle(mParent.centerX() + calculateCircleX((float) mBurstDegrees[i], mDistanceThirdWave[i]),
                    mParent.centerY() + calculateCircleY((float) mBurstDegrees[i], mDistanceThirdWave[i]) , 2, mPaint);
        }
    }
    
    //求临边 即X
    private float calculateCircleX(float degree, float distination) {
         return  (float) (distination * Math.cos(degree));
    }
  //求对边 即Y
    private float calculateCircleY(float degree, float distination) {
        return  (float) (distination * Math.sin(degree));
   }
    
    //
    private void calulateDistance() {
        float tempValue = mCurrentProcess * mMostFarDistance;
        for (int i = 0; i < mDistanceFirstWave.length; i++) {
            mDistanceFirstWave[i] = tempValue* 1.2f;
        }
        for (int i = 0; i < mDistanceSecondWave.length; i++) {
            mDistanceSecondWave[i] = tempValue * 1.0f;
        }
        for (int i = 0; i < mDistanceThirdWave.length; i++) {
            mDistanceThirdWave[i] = tempValue * 0.9f;
        }
    }
    
    
    public void startBurstAnim(long duration, final OnBurstEndListener listener) {
        LeoLog.i("tesi", "start Burst " );
        PropertyValuesHolder v1 = PropertyValuesHolder.ofFloat("currentProcess", 0f, 1f);
        final ObjectAnimator animatorI = ObjectAnimator.ofPropertyValuesHolder(this, v1);
        animatorI.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (Float) animation.getAnimatedValue("currentProcess");
                
                mCurrentProcess = animatedValue / 1f;
                LeoLog.i("tesi", "mCurrentProcess = " + mCurrentProcess);
                calulateDistance();
            }
        });
        animatorI.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mCurrentProcess = 0;
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
                
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentProcess = 0;
                if (listener != null) {
                    listener.OnBurstEnd();
                }
            }
            
            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentProcess = 0;
            }
        });
        animatorI.setDuration(duration);
        animatorI.start();
    }
    
    public interface OnBurstEndListener {
        public void OnBurstEnd();
    }
}
