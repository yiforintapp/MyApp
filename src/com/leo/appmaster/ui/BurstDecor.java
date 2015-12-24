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
    private float[] mPositionFirstWave = new float[10];
    private float[] mPositionSecondWave = new float[10];
    private float[] mPositionThirdWave = new float[10];
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
//        canvas.rotate(90f, mParent.centerX(), mParent.centerY());
        if (mPaint == null) {
            mPaint = new Paint();
        }
        mPaint.setAlpha(100);
        mPaint.setColor(Color.WHITE);    //设置画笔的颜色为白色
        mPaint.setAntiAlias(true);    //消除锯齿
        mPaint.setStyle(Style.STROKE);    //设置画笔风格为描边
        mPaint.setStrokeWidth(15f);    //设置描边的宽度为3
        if (mCurrentProcess >= 1) {
            return;
        }
        
        if (mCurrentProcess >= 0.8f) {
            mPaint.setAlpha(mPaint.getAlpha() + 30);
        }
        
        for (int i = 0; i < mPositionFirstWave.length; i++) {
//            canvas.drawCircle(cx, cy, radius, paint)
            canvas.drawCircle(mParent.centerX() + mPositionFirstWave[i] * (float)Math.cos(mBurstDegrees[i]), 
                    mParent.centerY() + mPositionFirstWave[i] * (float)Math.sin(mBurstDegrees[i]), 2, mPaint);
        }
        for (int i = 0; i < mPositionSecondWave.length; i++) {
//            if (mCurrentProcess >= 0.8f) {
//                paint.setAlpha(paint.getAlpha() + 30);
//            }
            canvas.drawCircle(mParent.centerX() + mPositionSecondWave[i] * (float)Math.cos(mBurstDegrees[i]), 
                    mParent.centerY() + mPositionSecondWave[i] * 0.95f *(float)Math.sin(mBurstDegrees[i]),2, mPaint);
        }
        for (int i = 0; i < mPositionThirdWave.length; i++) {
//            if (mCurrentProcess >= 0.8f) {
//                paint.setAlpha(paint.getAlpha() + 30);
//            }
            canvas.drawCircle(mParent.centerX() + mPositionThirdWave[i] * 0.95f * (float)Math.cos(mBurstDegrees[i]), 
                    mParent.centerY() + mPositionThirdWave[i] * (float)Math.sin(mBurstDegrees[i]),2, mPaint);
        }
        
//        mCurrentProcess += 0.02f;
//        calculatePosition();
//        canvas.drawPoint(mParent.centerX(), mParent.centerX()+40, paint);
////        canvas.drawPoint(mParent.centerX() + 30, mParent.centerX() + 60, paint);
//        canvas.drawPoint(mParent.centerX() + 60, mParent.centerX() +420, paint);
//        canvas.restore();
//        canvas.drawb
//        mParent.
    }
    
    private void calculatePosition() {
        float tempValue = mCurrentProcess * mMostFarDistance;
        for (int i = 0; i < mPositionFirstWave.length; i++) {
            mPositionFirstWave[i] = tempValue* 1.2f;
        }
        for (int i = 0; i < mPositionSecondWave.length; i++) {
            mPositionSecondWave[i] = tempValue * 1.0f;
        }
        for (int i = 0; i < mPositionThirdWave.length; i++) {
            mPositionThirdWave[i] = tempValue * 0.9f;
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
                calculatePosition();
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
