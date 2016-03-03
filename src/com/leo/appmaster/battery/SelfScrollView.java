package com.leo.appmaster.battery;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import com.leo.appmaster.utils.LeoLog;


public class SelfScrollView extends ScrollView {

    public boolean isCanScrool = true;

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
    }

    private BatteryTestViewLayout mParent;

    public void setParent(BatteryTestViewLayout layout) {
        mParent = layout;
    }


    @Override
    public void scrollTo(int x, int y) {

        super.scrollTo(x, y);
    }

    private int firstTab = 0;

    public void setScrollEnabled(boolean scrollabled) {
        isCanScrool = scrollabled;
        firstTab = 0;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isCanScrool) {
            return true;
        } else {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:// 手指按下屏幕
                    firstTab = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:// 手指在屏幕上移动

                    int newY = (int) event.getRawY();

                    if (firstTab == 0) {
                        firstTab = (int) event.getRawY();
                    }

//                    LeoLog.d("testBatteryView", "newY : " + newY);
//                    LeoLog.d("testBatteryView", "startX : " + firstTab);
                    int moveY = newY - firstTab;

                    LeoLog.d("testBatteryView", "moveY : " + moveY);

                    if (BatteryViewFragment.isExpand && !BatteryViewFragment.mShowing &&
                            top == 0 && moveY > 50) {
                        LeoLog.d("testBatteryView", "scrollBottom");
                        mParent.getScrollBottomListener().scrollBottom();
                    }
                    break;
                case MotionEvent.ACTION_UP:// 手指离开屏幕一瞬间
                    firstTab = 0;
                    break;
                case MotionEvent.ACTION_CANCEL:
                    firstTab = 0;
                    break;
            }
            return super.onTouchEvent(event);
        }
    }
}
