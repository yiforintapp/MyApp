package com.leo.appmaster.battery;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.leo.appmaster.utils.LeoLog;


public class BatteryTestViewLayout extends RelativeLayout {

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
                LeoLog.d("testBatteryView", "Big R onDown");
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
                LeoLog.d("testBatteryView", "Big R onScroll");
                LeoLog.d("testBatteryView", "distanceY : " + distanceY);
//                if (e2.getY() < e1.getY()) {
                //防止太灵敏 三星note出现此情况
                if (distanceY > 6) {
                    if (!BatteryViewFragment.isExpand) {
                        if (!BatteryViewFragment.mShowing) {
                            scrollBottomListener.scrollTop();
                        }
                    } else {
                        if (BatteryViewFragment.mIsExtraLayout) {
                            return false;
                        } else {
                            return true;
                        }
                    }
                } else if (distanceY < -6) {
                    if (BatteryViewFragment.isExpand) {
                        if (!BatteryViewFragment.mShowing) {
                            if (!BatteryViewFragment.mIsExtraLayout) {
                                scrollBottomListener.scrollBottom();
                            } else {
                                return false;
                            }
                        }
                    }
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                LeoLog.d("testBatteryView", "Big R onFling");
                LeoLog.d("testBatteryView", "velocityY : " + velocityY);
                if (e2.getY() < e1.getY()) {
                    if (!BatteryViewFragment.isExpand) {
                        if (!BatteryViewFragment.mShowing) {
                            scrollBottomListener.scrollTop();
                        }
                    } else {
                        if (BatteryViewFragment.mIsExtraLayout) {
                            return false;
                        } else {
                            return true;
                        }
                    }
                } else {
                    if (BatteryViewFragment.isExpand) {
                        if (!BatteryViewFragment.mShowing) {
                            if (!BatteryViewFragment.mIsExtraLayout) {
                                scrollBottomListener.scrollBottom();
                            } else {
                                return false;
                            }
                        }
                    }
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
        boolean result = mDetector.onTouchEvent(ev);
        return result;
    }
}
