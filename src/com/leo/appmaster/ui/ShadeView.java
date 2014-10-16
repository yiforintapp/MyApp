package com.leo.appmaster.ui;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class ShadeView extends View implements AnimatorUpdateListener {

	public static int DEFAULT_COLOR_RED = 0x28;
	public static int DEFAULT_COLOR_GREEN = 0x93;
	public static int DEFAULT_COLOR_BLUE = 0xfe;
	
	private int mCurColor = DEFAULT_COLOR_RED;

	private int mGreen = DEFAULT_COLOR_GREEN, mRed = DEFAULT_COLOR_RED,
			mBlue = DEFAULT_COLOR_BLUE;

	private ValueAnimator gVa;
	private ValueAnimator rVa;
	private ValueAnimator bVa;

	public ShadeView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		canvas.drawColor(mCurColor);

		super.onDraw(canvas);
	}

	public void updateColor(int targetR, int targetG, int targetB, int duration) {
		AnimatorSet set = new AnimatorSet();
		rVa = ValueAnimator.ofInt(mRed, targetR);
		gVa = ValueAnimator.ofInt(mGreen, targetG);
		bVa = ValueAnimator.ofInt(mBlue, targetB);

		gVa.addUpdateListener(this);
		rVa.addUpdateListener(this);
		bVa.addUpdateListener(this);

		set.playTogether(gVa, rVa, bVa);
		set.setDuration(duration);
		set.setInterpolator(new LinearInterpolator());

		set.start();
	}

	@Override
	public void onAnimationUpdate(ValueAnimator va) {
		mGreen = (Integer) gVa.getAnimatedValue();
		mRed = (Integer) rVa.getAnimatedValue();
		mBlue = (Integer) bVa.getAnimatedValue();
		
		mCurColor = Color.rgb(mRed, mGreen, mBlue);

		postInvalidate();
	}
}
