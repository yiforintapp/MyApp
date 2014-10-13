package com.leo.appmaster.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class LeoViewPager extends ViewPager {

	public LeoViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		float preX = 0;
		float preY = 0;

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			preX = event.getX();
			preY = event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			Log.e("xxxx", "onInterceptTouchEvent: event.getY() - preY = "
					+ (event.getY() - preY));
			Log.e("xxxx", "onInterceptTouchEvent: event.getX() - preX = "
					+ (event.getX() - preX) + "\n\n");
//			if (Math.abs(event.getX() - preX) > 350) {
//				return true;
//			}
			break;
		default:
			break;
		}

		return super.onInterceptTouchEvent(event);
	}

}
