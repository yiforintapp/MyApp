
package com.leo.appmaster.ui;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class MyHorizontalScrollView extends HorizontalScrollView {

    public MyHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent p_event)
    {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent p_event)
    {
        if (p_event.getAction() == MotionEvent.ACTION_MOVE && getParent() != null)
        {
            getParent().requestDisallowInterceptTouchEvent(true);
        }

        return super.onTouchEvent(p_event);
    }
    
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {
            
        }
    }
}
