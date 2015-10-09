package com.leo.appmaster.ui;

import android.content.Context;  
import android.os.Parcelable;
import android.support.v4.view.ViewPager;  
import android.util.AttributeSet;  
import android.view.MotionEvent;  
  
public class CustomViewPager extends ViewPager {  
  
    private boolean isCanScroll = true;  
  
    public CustomViewPager(Context context) {  
        super(context);  
    }  
  
    public CustomViewPager(Context context, AttributeSet attrs) {  
        super(context, attrs);  
    }  
  
    public void setScanScroll(boolean isCanScroll) {  
        this.isCanScroll = isCanScroll;  
    }  
  
    @Override  
    public void scrollTo(int x, int y) {  
        super.scrollTo(x, y);  
    }  
  
    
    
    @Override  
    public boolean onTouchEvent(MotionEvent arg0) {  
        // TODO Auto-generated method stub  
        if (isCanScroll) {  
            return super.onTouchEvent(arg0);  
        } else {  
            return false;  
        }  
  
    }  
  
    @Override  
    public void setCurrentItem(int item, boolean smoothScroll) {  
        // TODO Auto-generated method stub  
        super.setCurrentItem(item, smoothScroll);  
    }  
  
    @Override  
    public void setCurrentItem(int item) {  
        // TODO Auto-generated method stub  
        super.setCurrentItem(item);  
    }  
  
    @Override  
    public boolean onInterceptTouchEvent(MotionEvent arg0) {  
        // TODO Auto-generated method stub  
        if (isCanScroll) {  
            return super.onInterceptTouchEvent(arg0);  
        } else {  
            return false;  
        }  
  
    }  
    
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {
            
        }
    }
}  
