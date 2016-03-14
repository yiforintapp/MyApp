package com.leo.appmaster.appmanage.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.widget.LinearLayout;

public class GestureLayout extends LinearLayout{

    private IGestureListener mListener;
    
    private int mSnapVelocity = 300; // SUPPRESS CHECKSTYLE
    /** v tracker */
    private VelocityTracker mVelocityTracker;
    
    public interface IGestureListener {
        public void onScroll(int direction);  //left :direction = -1,right : direction = 1;
    }
    
    public GestureLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mVelocityTracker = VelocityTracker.obtain();
    }


    public void setListener(IGestureListener listener) {
        mListener = listener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mVelocityTracker.addMovement(ev);
        return super.dispatchTouchEvent(ev);
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev){

        return super.onInterceptTouchEvent(ev);

    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;

            case MotionEvent.ACTION_MOVE:

                break;

            case MotionEvent.ACTION_UP:
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000); // SUPPRESS
                                                              // CHECKSTYLE
                float velocityX = velocityTracker.getXVelocity();
                if (velocityX > mSnapVelocity) {
                    if (mListener != null) {
                        mListener.onScroll(-1);
                    }
                } else if (velocityX < -mSnapVelocity) {
                    if (mListener != null) {
                        mListener.onScroll(1);
                    }
                }
                
                reset();
                break;

            case MotionEvent.ACTION_CANCEL:

                reset();
                break;
            default:
        }

        return true;
    }
    
    private void reset() {
        mVelocityTracker.clear();
    }
}
