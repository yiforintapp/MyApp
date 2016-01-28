package com.leo.appmaster.ui;

import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.Animator.AnimatorListener;
import com.leo.tools.animator.ObjectAnimator;
import com.leo.tools.animator.PropertyValuesHolder;
import com.leo.tools.animator.ValueAnimator;
import com.leo.tools.animator.ValueAnimator.AnimatorUpdateListener;

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
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
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
	private float mTailDegree =  180f;
	private OnAfterImageDismissListener mListener2;

    private Paint mPaintTail;
	private RectF mRectF;
	private Paint mPaintShader;
	private boolean mIsAnimating = false;
	private boolean mIsTailAnimating = false;
	private float mArroundDegreeWhenAnimating = 0f;
	private OnArroundFinishListener mLinstener;
	private boolean mNeedCircleHead = true;
	private int mPaintAlpha = 0x99;
	private ObjectAnimator mOA;
	public float getTailDegree() {
        return mTailDegree;
    }

    public void setTailDegree(float mTailDegree) {
        this.mTailDegree = mTailDegree;
    }
	
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

	public void resetTailPaintAlpha() {
	    mPaintAlpha = 0xaa;
	}
	
	public CircleView(Context context) {
		super(context);
		mContext = context;
		init();
	}
	
	public void setShowWhich(int d) {
		mWitchToShow = d;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
//		canvas.drawColor(Color.BLUE);
//		if (!mIsAnimating && !mIsTailAnimating) {
//		    return;
//		}
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
        		canvas.drawArc(mRectF, degree, - (Math.min(degree % 180, mTailDegree)), false, mPaintShader);
        		double x = Math.cos(arcDegree) * (double)(FactorR);
            	double y = Math.sin(arcDegree) * (double)(FactorR);
            	if (mNeedCircleHead) {
            	    canvas.drawCircle((float)(mCenterX + x), (float)(mCenterY + y), 5, mPaintNormal);
            	}
            	
        	} 
//        	if ((mWitchToShow == SHOW_FRONT && degree > 180) && (degree - 180) < mTail) {
//        	    canvas.drawArc(mRectF, 180, - (mTail - (degree - 180)), false, mPaintTail);
//        	}
//        	if ((mWitchToShow == SHOW_BACK && degree <= 180) && degree < mTail) {
//                canvas.drawArc(mRectF, 0 , - (mTail - degree), false, mPaintTail);
//            }
        } 
        if (mIsTailAnimating && mPaintAlpha != 0) {
            canvas.drawArc(mRectF, 0, 360, false, mPaintTail);
            mPaintAlpha *= 0.90f;
            mPaintTail.setColor(Color.argb(mPaintAlpha, 0xff, 0xff, 0xff));
            if (mPaintAlpha < 0x10) {
                mPaintAlpha = 0;
                if (mListener2 != null) {
                    mListener2.onAfterImageDismiss();
                    mIsTailAnimating = false;
                    mListener2 = null;
                }
            } 
//                canvas.drawArc(mRectF, 0, 360, false, mPaintTail);
//                mPaintAlpha *= 0.98f;
//                mPaintTail.setColor(Color.argb(mPaintAlpha, 0xff, 0xff, 0xff));
//            }
        }
        invalidate();
	}

	public void startAfterImageDismissAnim(OnAfterImageDismissListener listener) {
	    mIsTailAnimating = true;
	    mListener2 = listener;
	}
	
	public void setFactorR(int r) {
		FactorR = r;
	}
	
	private void init() {
		mPaintNormal = new Paint();
		mPaintNormal.setColor(0xffffffff);
		mPaintNormal.setStyle(Style.FILL_AND_STROKE);
		int normalPaintWidth = DipPixelUtil.dip2px(mContext, 4);
//		RadialGradient rg = new RadialGradient(normalPaintWidth / 2, normalPaintWidth / 2, normalPaintWidth / 2, 0xffffffff, 0x33ffffff, Shader.TileMode.REPEAT);
//		RadialGradient rg = new RadialGradient(50, 50, 50, new int[] {0xffffffff, 0xbbffffff, 0x77ffffff, 0x22ffffff}, null, Shader.TileMode.REPEAT);
//		mPaintNormal.setShader(rg);
//		LinearGradient gradient = new LinearGradient(0, 0, (float) (Math.PI * 1 * FactorR), 100, 0xffffffff, 0x00ffffff, Shader.TileMode.MIRROR);  
//        mPaintShader.setShader(gradient);  
		mPaintNormal.setStrokeWidth(normalPaintWidth);
		mPaintNormal.setAntiAlias(true);
		mRotateMatrix = new Matrix();
		mCamera = new Camera();
		mPaintShader = new Paint();
		mPaintShader.setColor(0xffffffff);
		mPaintShader.setStyle(Style.STROKE);
		mPaintShader.setStrokeWidth(DipPixelUtil.dip2px(mContext, 3));
		mPaintShader.setAntiAlias(true);
		mPaintTail = new Paint();
		mPaintTail.setColor(Color.argb(mPaintAlpha, 0xff, 0xff, 0xff));
		mPaintTail.setStyle(Style.STROKE);
		mPaintTail.setStrokeWidth(10);
	}
	
	public void startAnim(float from, float delta, final OnArroundFinishListener listener, boolean needCircleHead, long duration) {
	    mNeedCircleHead = needCircleHead;
	    mFromDegree = from;
	    mArroundDeltaDegree = delta;
		mArroundDegreeWhenAnimating = mFromDegree;
		PropertyValuesHolder holder = PropertyValuesHolder.ofFloat("ArroundDegreeWhenAnimating", mFromDegree, mFromDegree += mArroundDeltaDegree);
		mOA = ObjectAnimator.ofPropertyValuesHolder(this, holder);
		mOA.setDuration(duration);
		mOA.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mIsAnimating = true;
            }
        });
		mOA.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mLinstener = listener;
                mIsAnimating = true;
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                mIsAnimating = false;
                Log.i("ttt", "set mIsAnimating false when Animation End");
                mArroundDegreeWhenAnimating = 0;
                if (mLinstener != null) {
                    mLinstener.onArroundFinish();
                    mLinstener = null;
                }
            }
            @Override
            public void onAnimationCancel(Animator animation) {
                mIsAnimating = false;
                Log.i("ttt", "set mIsAnimating false when Animation Cancel");
            }
        });
		mOA.start();
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
	
	public interface OnAfterImageDismissListener {
        public  void onAfterImageDismiss();
    }
}
