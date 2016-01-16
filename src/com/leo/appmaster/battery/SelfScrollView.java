package com.leo.appmaster.battery;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import com.leo.appmaster.utils.LeoLog;


public class SelfScrollView extends ScrollView {

    private ScrollBottomListener scrollBottomListener;

    public SelfScrollView(Context context) {
        super(context);
    }

    public SelfScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SelfScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    int top = 0;
    int oldTop = 0;

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {

        top = t;
        oldTop = oldt;

//        LeoLog.d("testBatteryView", "l : " + l);
//        LeoLog.d("testBatteryView", "t : " + t);
//
//        LeoLog.d("testBatteryView", "oldl : " + oldl);
//        LeoLog.d("testBatteryView", "oldt : " + oldt);

//        if (oldt != 0 && t == 0) {
//            if (scrollBottomListener != null) {
//                scrollBottomListener.scrollTop();
//            }
//        }

//        if(t + getHeight() >=  computeVerticalScrollRange()){
//            if(scrollBottomListener != null){
//                //ScrollView滑动到底部了
//                scrollBottomListener.scrollBottom();
//            }
//        }
    }

    public void setScrollBottomListener(ScrollBottomListener scrollBottomListener) {
        this.scrollBottomListener = scrollBottomListener;
    }

    public interface ScrollBottomListener {
        public void scrollBottom();

        public void scrollTop();
    }

    int startY;

    @Override
    public void scrollTo(int x, int y) {

        super.scrollTo(x, y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:// 手指按下屏幕
                startY = (int) event.getRawY();
//                LeoLog.d("testBatteryView", "startY : " + startY);
                break;
            case MotionEvent.ACTION_MOVE:// 手指在屏幕上移动
                int newY = (int) event.getRawY();
                int moveY = newY - startY;

                LeoLog.d("testBatteryView", "top : " + top);
                LeoLog.d("testBatteryView", "oldTop : " + oldTop);
//                LeoLog.d("testBatteryView", "newY : " + newY);
//                LeoLog.d("testBatteryView", "moveY : " + moveY);

                if (!BatteryViewFragment.isExpand && !BatteryViewFragment.mShowing && top > 0 && oldTop >= 0) {
                    if (scrollBottomListener != null) {
                        scrollBottomListener.scrollTop();
                    }
                }

                if (BatteryViewFragment.isExpand && !BatteryViewFragment.mShowing && top == 0 && oldTop != 0) {
                    if (scrollBottomListener != null) {
                        scrollBottomListener.scrollBottom();
                    }
                }


                break;
            case MotionEvent.ACTION_UP:// 手指离开屏幕一瞬间

                break;
        }
        return super.onTouchEvent(event);
    }
}
