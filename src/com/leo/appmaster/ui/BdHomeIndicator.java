/**
 * Filename:    BdHomeIndicator.java
 * Description:
 * Copyright:   Baidu MIC Copyright(c)2013
 * @author:     Rambow
 * @version:    1.0
 * Create at:   Jul 16, 2013 1:29:51 PM
 * 
 * Modification History:
 * Date         Author      Version     Description
 * ------------------------------------------------------------------
 * Jul 16, 2013    Rambow      1.0         1.0 Version
 */
package com.leo.appmaster.ui;


import com.leo.appmaster.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

/**
 *home indicator
 */
public class BdHomeIndicator extends View {
	/** count */
	private int mCount;
	/** normal status bmp */
	private Bitmap mNormal;
	/** focus status bmp */
	private Bitmap mFocus;
	/** icon width */
	private int mIconWidth;
	/** icon height */
	private int mIconHeight;
	/** x of focus bmp */
	private int mFocusX;
	/** current index */
	private int mCurIndex;
	/** gap */
	private int mGap;
	/** item width */
	private int mItemWidth;

	/** paint */
	private Paint mPaint;
	
	/**
	 * Constructor
	 * @param aContext context
	 * @param aCount item count
	 * @param aDefaultIndex default index
	 */
	public BdHomeIndicator(Context aContext, int aCount, int aDefaultIndex) {
		super(aContext);
		mCount = aCount;
		mCurIndex = aDefaultIndex;
//		mNormal = BitmapFactory.decodeResource(getResources(), R.drawable.indicator_normal);
		mIconWidth = mNormal.getWidth();
		mIconHeight = mNormal.getHeight();
//		mFocus = BitmapFactory.decodeResource(getResources(), R.drawable.indicator_select);
		Resources res = aContext.getResources();
		mPaint = new Paint();
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		//重置
		mGap = 0;
		mFocusX = 0;
		
		int width = this.getWidth();
		mItemWidth = width / mCount;

		// 计算gap
		mGap = (mItemWidth - mIconWidth) / 2;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int width = this.getWidth();
		int height = this.getHeight();
		int x = mGap;
		int y = height - mIconHeight;
		if (x > 0) {
			for (int i = 0; i < mCount; i++) {
				x = (int) (i * width / (float) mCount) + mGap;
				canvas.drawBitmap(mNormal, x, y, mPaint);
			}
		} else {
		}
		if (mFocusX == 0) {
			mFocusX = (int) (mCurIndex * width / (float) mCount) + mGap;
		}
		canvas.drawBitmap(mFocus, mFocusX, y, mPaint);
		
	}
	
	/**
	 * set focus x
	 * @param aRate 比例
	 */
	public void setFocusChangeX(float aRate) {
		if (mGap != 0) {
			mFocusX = (int) (aRate * this.getWidth()) + mGap;
			
			int min = mGap;
			int max = this.getWidth() - mIconWidth - mGap;
			if (mFocusX < min) {
				mFocusX = min;
			} else if (mFocusX > max) {
				mFocusX = max;
			}
			this.postInvalidate();
		}
	}
	
	/**
	 * set current index
	 * @param aIndex index
	 */
	public void setCurIndex(int aIndex) {
		mCurIndex = aIndex;
		if (aIndex == mCount - 1) {
		}
	}
	
	/**
	 * 设置指示符数
	 * @param aCount 个数
	 */
	public void setCount(int aCount) {
		if (mCount != aCount) {
			mCount = aCount;
			requestLayout();
			postInvalidate();
		}
	}
	
	/**
	 * 获取指示符总个数
	 * @return 个数
	 */
	public int getCount() {
		return mCount;
	}
	
	
	
	/**
	 * 动画Handler
	 */
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
		}
	};
	
	/**
	 * 动画invalidate
	 */
	private void invalidateForAnim() {
		mHandler.sendEmptyMessage(0);
	}
	
}
