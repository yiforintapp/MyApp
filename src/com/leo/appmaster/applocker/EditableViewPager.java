
package com.leo.appmaster.applocker;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class EditableViewPager extends ViewPager {

    private boolean scrollable = true;;

    public EditableViewPager(Context context) {
        super(context);
    }

    public EditableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        return super.onInterceptTouchEvent(arg0);
    }

    public boolean isScrollable() {
        return scrollable;
    }

    public void setScrollable(boolean scrollable) {
        this.scrollable = scrollable;
    }

    @Override
    public void scrollTo(int x, int y) {
        if (scrollable) {
            super.scrollTo(x, y);
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
