package com.leo.appmaster.appmanage.view;

import android.graphics.Bitmap;

public interface IViewPressed {
		
	public void setStayPressed(boolean stayPressed);
	
	public void clearPressedOrFocusedBackground();
	
	public Bitmap getPressedOrFocusedBackground();
	
	public int getPressedOrFocusedBackgroundPadding();
	
	public int getLeft();
	
	public int getTop();

}
