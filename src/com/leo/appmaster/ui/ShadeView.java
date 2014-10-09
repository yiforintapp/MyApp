package com.leo.appmaster.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class ShadeView extends View {

	private int mColor = 12;

	public ShadeView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		canvas.drawColor(mColor);

		super.onDraw(canvas);
	}

	public void updateColor(int color) {
		mColor = color;
		invalidate();
	}

}
