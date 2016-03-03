package com.leo.appmaster.ui;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.LeoLog;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.Animator.AnimatorListener;
import com.leo.tools.animator.ObjectAnimator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CircleArroundView extends View {
	private Paint mPaintNormal;
	private int mTotalW;
	private int mTotalH;
	private int mCenterX;
	private int mCenterY;
	private Matrix mRotateMatrix;
	private Context mContext;
	private Camera mCamera;
	private float mDegreeX = 80f;
	
	private float mCircleRotateDegreeZ;
	private OnArroundFinishListener mArroundFinishListener;

	private Bitmap mBmCircle;
	private boolean mIsAnimating = false;
	public int SHOW_FRONT = 1;
	public int SHOW_BACK = 2;
	private float[] mMatrixValue;
	
	public float getCircleRotateDegreeZ() {
		return mCircleRotateDegreeZ;
	}
	
	public void setCircleRotateDegreeZ(float circleRotateDegreeZ) {
		this.mCircleRotateDegreeZ = circleRotateDegreeZ;
	}
	
	public CircleArroundView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;
		init();
	}

	public CircleArroundView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public CircleArroundView(Context context) {
		super(context);
		mContext = context;
		init();
	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Matrix matrix = canvas.getMatrix();
		int save1 = canvas.save();
		LeoLog.i("CircleArroundView", "mIsAnimating = " + mIsAnimating);
		//绘制内层半圆弧
		if (mIsAnimating) {
			mCamera.save();
			mCamera.setLocation(0, 0, -10);
			mCamera.rotate(mDegreeX, 0, mCircleRotateDegreeZ);
//		Log.i("poha", mCamera.getLocationX() + " " + mCamera.getLocationY() + " " + mCamera.getLocationZ());
			mCamera.getMatrix(mRotateMatrix);
			mCamera.restore();
			mRotateMatrix.preTranslate(-mCenterX, -mCenterY);  
			mRotateMatrix.postTranslate(mCenterX, mCenterY); 
			matrix.postConcat(mRotateMatrix);
			canvas.concat(mRotateMatrix);
			canvas.drawBitmap(mBmCircle, (mTotalW - mBmCircle.getWidth()) / 2, (mTotalH - mBmCircle.getHeight()) / 2, mPaintNormal);
			invalidate();
		}
	}

	private void init() {
		mMatrixValue = new float[9];
		mPaintNormal = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintNormal.setAntiAlias(true);
		mRotateMatrix = new Matrix();
		mCamera = new Camera();
	}
	
	public void startAnim(float from, float to, long duration, OnArroundFinishListener listener) {
	    mArroundFinishListener = listener;
		mIsAnimating = true;
		ObjectAnimator oa = ObjectAnimator.ofFloat(this, "CircleRotateDegreeZ", from, to);
		oa.setDuration(duration);
		oa.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
				mIsAnimating = true;
				invalidate();
			}
			@Override
			public void onAnimationRepeat(Animator animation) {
				mIsAnimating = true;
			}
			@Override
			public void onAnimationEnd(Animator animation) {
				mIsAnimating = false;
				if (mArroundFinishListener != null) {
				    mArroundFinishListener.onArroundFinish();
				    mArroundFinishListener = null;
				}
			}
			@Override
			public void onAnimationCancel(Animator animation) {
				mIsAnimating = false;
			}
		});
		oa.start();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mTotalW = w;
		mTotalH = h;
		mCenterX = mTotalW / 2;
		mCenterY = mTotalH / 2;
		mBmCircle = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.circle_battery); 
		int min = Math.min(mTotalW, mTotalH);
		mBmCircle = Bitmap.createScaledBitmap(mBmCircle, (int)((float)(min * 0.9f)), (int)((float)(min * 0.9f)), true);
	}
	
	public interface OnArroundFinishListener {
        public  void onArroundFinish();
    }
	
}
