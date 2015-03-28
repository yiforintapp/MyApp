package com.leo.appmaster.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

public class CricleView extends View {

	Bitmap mBackground, mCenter;
	RectF mRectCenter, mRectColor;
	Paint mPaint;
	private float mCenterX, mCenterY;
	private float mWidth, mHeight;

	private float mStroke;

	private float mDegrees = 230;
	private SweepGradient mShader;

	public CricleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setAntiAlias(true);
	}

	public void updateDegrees(float d) {
		mDegrees = d;
		postInvalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {

		if (mStroke == 0) {
			mWidth = getWidth();
			mHeight = getHeight();
			mCenterX = mWidth / 2;
			mCenterY = mHeight / 2;

			mStroke = mWidth * 0.1047f;
			mPaint.setStrokeWidth(mStroke);

			mShader = new SweepGradient(mCenterX, mCenterY, new int[] {
					Color.GREEN, Color.YELLOW }, null);

			mRectColor = new RectF(mStroke / 2, mStroke / 2, mWidth - mStroke
					/ 2, mHeight - mStroke / 2);

		}

		int sc = canvas.saveLayer(0, 0, mWidth, mHeight, null,
				Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG
						| Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
						| Canvas.FULL_COLOR_LAYER_SAVE_FLAG
						| Canvas.CLIP_TO_LAYER_SAVE_FLAG);

		canvas.save();
		canvas.rotate(-90f, mCenterX, mCenterY);
		mPaint.setShader(mShader);
		canvas.drawArc(mRectColor, 0, mDegrees, false, mPaint);

		canvas.restore();
		canvas.restoreToCount(sc);

		super.onDraw(canvas);
	}
	
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {
            
        }
    }

}
