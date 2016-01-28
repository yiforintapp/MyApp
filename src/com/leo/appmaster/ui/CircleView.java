package com.leo.appmaster.ui;

import com.leo.tools.animator.Animator;
import com.leo.tools.animator.Animator.AnimatorListener;
import com.leo.tools.animator.ObjectAnimator;
import com.leo.tools.animator.PropertyValuesHolder;
import com.leo.tools.animator.ValueAnimator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

public class CircleView extends View {
	private Paint mPaintNormal;
	private int mTotalW;
	private int mTotalH;
	private int mCenterX;
	private int mCenterY;
	private int FactorR;
	private Matrix mRotateMatrix;
	private Context mContext;
	private Camera mCamera;
	private float mDegree = 80f;
	private float mTail =  270f;
	private Paint mPaintTail;
	private RectF mRectF;
	private Paint mPaintShader;
	private boolean mIsAnimating = false;
	private boolean mIsTailAnimating = false;
	private float mArroundDegreeWhenAnimating = 0f;
	private OnArroundFinishListener mLinstener;
	private boolean mNeedCircleHead = true;
	private int mPaintAlpha = 0x88;
	
	public float getArroundDegreeWhenAnimating() {
        return mArroundDegreeWhenAnimating;
    }

    public void setArroundDegreeWhenAnimating(float mArroundDegreeWhenAnimating) {
        this.mArroundDegreeWhenAnimating = mArroundDegreeWhenAnimating;
    }

    public static final int SHOW_FRONT = 1;
	public static final int SHOW_BACK = 2;
	private int mWitchToShow = 1;
	private float mFromDegree;
	private float mArroundDeltaDegree;
	
	public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;
		init();
	}

	public CircleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public CircleView(Context context) {
		super(context);
		mContext = context;
		init();
	}
	
	public void setShowWhich(int d) {
		mWitchToShow = d;
	}
	
	public void setTailDegree(float degree) {
	    mTail =  degree;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
//		canvas.drawColor(Color.BLUE);
		Matrix matrix = canvas.getMatrix();
		int save1 = canvas.save();
		mCamera.save();
		mCamera.rotateX(mDegree);
		mCamera.getMatrix(mRotateMatrix);
		mCamera.restore();
		mRotateMatrix.preTranslate(-mCenterX, -mCenterY);  
		mRotateMatrix.postTranslate(mCenterX, mCenterY); 
        matrix.postConcat(mRotateMatrix);
        canvas.concat(matrix);
        float degree = mArroundDegreeWhenAnimating % 360f;
        if (mIsAnimating) {
        	float arcDegree = (float) (((mArroundDegreeWhenAnimating % 360f) * Math.PI) / 180);
        	if ((mWitchToShow == SHOW_FRONT && degree >= 0 && degree < 180) ||(mWitchToShow == SHOW_BACK && degree >= 180 && degree < 360)) {
        		canvas.drawArc(mRectF, degree, - (Math.min(degree % 180, mTail)), false, mPaintShader);
        		double x = Math.cos(arcDegree) * (double)(FactorR);
            	double y = Math.sin(arcDegree) * (double)(FactorR);
            	canvas.drawCircle((float)(mCenterX + x), (float)(mCenterY + y), 5, mPaintNormal);
            	
        	} 
//        	if ((mWitchToShow == SHOW_FRONT && degree > 180) && (degree - 180) < mTail) {
//        	    canvas.drawArc(mRectF, 180, - (mTail - (degree - 180)), false, mPaintTail);
//        	}
//        	if ((mWitchToShow == SHOW_BACK && degree <= 180) && degree < mTail) {
//                canvas.drawArc(mRectF, 0 , - (mTail - degree), false, mPaintTail);
//            }
        } 
        else if (mIsTailAnimating) {
            canvas.drawArc(mRectF, 0, 360, false, mPaintTail);
            mPaintAlpha *= 0.98f;
            mPaintTail.setColor(Color.argb(mPaintAlpha, 0xff, 0xff, 0xff));
//            if (mPaintAlpha < 0x10) {
//                mPaintAlpha = 0;
//            } else {
//                canvas.drawArc(mRectF, 0, 360, false, mPaintTail);
//                mPaintAlpha *= 0.98f;
//                mPaintTail.setColor(Color.argb(mPaintAlpha, 0xff, 0xff, 0xff));
//            }
        }
        invalidate();
	}

	public void setFactorR(int r) {
		FactorR = r;
	}
	
	private void init() {
		mPaintNormal = new Paint();
		mPaintNormal.setColor(0xffffffff);
		mPaintNormal.setStyle(Style.FILL_AND_STROKE);
		mPaintNormal.setStrokeWidth(13);
		mPaintNormal.setAntiAlias(true);
		mRotateMatrix = new Matrix();
		mCamera = new Camera();
		mPaintShader = new Paint();
		mPaintShader.setColor(0xffffffff);
		mPaintShader.setStyle(Style.STROKE);
		mPaintShader.setStrokeWidth(10);
		mPaintShader.setAntiAlias(true);
		mPaintTail = new Paint();
		mPaintTail.setColor(Color.argb(mPaintAlpha, 0xff, 0xff, 0xff));
		mPaintTail.setStyle(Style.STROKE);
		mPaintTail.setStrokeWidth(10);
	}
	
	public void startAnim(float from, float delta, final OnArroundFinishListener l, boolean needCircleHead, long duration) {
	    mNeedCircleHead = needCircleHead;
	    mFromDegree = from;
	    mArroundDeltaDegree = delta;
		mArroundDegreeWhenAnimating = mFromDegree;
		PropertyValuesHolder holder = PropertyValuesHolder.ofFloat("ArroundDegreeWhenAnimating", mFromDegree, mFromDegree += mArroundDeltaDegree);
		ObjectAnimator va = ObjectAnimator.ofPropertyValuesHolder(this, holder);
		va.setDuration(duration);
		va.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mLinstener = l;
                mIsAnimating = true;
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                mIsAnimating = false;
                mIsTailAnimating = true;
                mArroundDegreeWhenAnimating = 0;
                if (mLinstener != null) {
                    mLinstener.onArroundFinish();
                    mLinstener = null;
                }
            }
            @Override
            public void onAnimationCancel(Animator animation) {
                mIsAnimating = false;
            }
        });
		va.start();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mTotalW = w;
		mTotalH = h;
		FactorR = ((Math.min(mTotalH, mTotalW) *4) / 9);
		mCenterX = mTotalW / 2;
		mCenterY = mTotalH / 2;
		mRectF = new RectF(mCenterX - FactorR, mCenterY - FactorR, mCenterX + FactorR, mCenterY + FactorR);
		LinearGradient gradient = new LinearGradient(0, 0, (float) (Math.PI * 1 * FactorR), 100, 0xffffffff, 0x00ffffff, Shader.TileMode.MIRROR);  
        mPaintShader.setShader(gradient);  
	}
	
	public interface OnArroundFinishListener {
	    public  void onArroundFinish();
	}
}
