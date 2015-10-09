package com.leo.appmaster.appmanage.view;

import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

public class NoneAction implements DecorateAction {

	@Override
	public int getActionType() {
		return ACTION_NONE;
	}

	@Override
	public void draw(Canvas canvas, View view) {

	}

	@Override
	public boolean onTouchEventCheck(MotionEvent event) {
		return false;
	}

	@Override
	public void setAlpha(int alpha) {

	}

}
