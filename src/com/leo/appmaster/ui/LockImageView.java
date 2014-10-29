package com.leo.appmaster.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class LockImageView extends ImageView {
	private RectF mRect;

	private boolean mLocked;
	
	public LockImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mLocked) {
			if (mRect == null) {
				mRect = new RectF();
				int width = this.getMeasuredWidth();
				int height = this.getMeasuredHeight();
				mRect.left = 0;
				mRect.top = 0;
				mRect.right = width;
				mRect.bottom = height;
			}

			canvas.drawARGB(180, 0, 0, 0);
		}
	}
	
	public void setLocked(boolean locked) {
		mLocked = locked;
		invalidate();
	}
}
