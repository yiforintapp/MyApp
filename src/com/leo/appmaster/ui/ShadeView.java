package com.leo.appmaster.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

public class ShadeView extends View {

	public static int mGreenColor = 0x47;
	public static int mRedColor = 0x87;

	private int mCurColor = mRedColor;

	private int mGreen = mGreenColor, mRed = mRedColor;

	public ShadeView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		canvas.drawColor(mCurColor);

		super.onDraw(canvas);
	}

	public void setColor(int color) {
		mCurColor = color;
		invalidate();
	}

	public void updateColor(int step) {
		mGreen += step;
		mRed -= step;
		setColor(Color.rgb(mRed, mGreen, 10));
	}
}
