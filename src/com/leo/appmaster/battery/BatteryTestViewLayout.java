package com.leo.appmaster.battery;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.LeoLog;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorListenerAdapter;
import com.leo.tools.animator.ObjectAnimator;


public class BatteryTestViewLayout extends RelativeLayout {

    private boolean isScrollable = false;

    private GestureDetector mDetector;
    private ScrollBottomListener scrollBottomListener;

    public interface ScrollBottomListener {
        public void scrollBottom();

        public void scrollTop();
    }

    public BatteryTestViewLayout(Context context) {
        super(context);
    }

    public BatteryTestViewLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BatteryTestViewLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mDetector = new GestureDetector(new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (e2.getY() < e1.getY()) {
                    scrollBottomListener.scrollTop();
                } else {
                    scrollBottomListener.scrollBottom();
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e2.getY() < e1.getY()) {
                    scrollBottomListener.scrollTop();
                } else {
                    scrollBottomListener.scrollBottom();
                }
                return true;
            }
        });
    }

    public void setScrollBottomListener(ScrollBottomListener scrollBottomListener) {
        this.scrollBottomListener = scrollBottomListener;
    }

    public ScrollBottomListener getScrollBottomListener() {
        return scrollBottomListener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isScrollable) {
            return false;
        }
        boolean result = mDetector.onTouchEvent(ev);
        return result;
//        if (!result) {
//            return super.onInterceptTouchEvent(ev);
//        }
//
//        return true;
    }

    public void setScrollable(boolean flag) {
        isScrollable = flag;
    }
}
