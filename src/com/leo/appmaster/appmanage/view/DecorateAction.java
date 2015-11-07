package com.leo.appmaster.appmanage.view;

import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

public interface DecorateAction {

	public static final int ACTION_NONE = 0;
	/** Icon status */
	public static final int ACTION_STATUS = 1;
	
	public int getActionType();
	
	public void draw(Canvas canvas, View view);
	
	public boolean onTouchEventCheck(MotionEvent event);
	
	
	public void setAlpha(int alpha);
}