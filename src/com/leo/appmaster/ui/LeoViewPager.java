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
		// // TODO Auto-generated method stub
		// return super.onInterceptTouchEvent(arg0);
//		
		float preX = 0;
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			preX = event.getX();
			break;
		case MotionEvent.ACTION_MOVE:
			Log.e("xxxx", "onInterceptTouchEvent");
			if (Math.abs(event.getX() - preX) > 4) {
				return true;
			} else {
				preX = event.getX();
			}
			break;
		default:
			break;
		}
		
		return super.onInterceptTouchEvent(event);
	}

}
