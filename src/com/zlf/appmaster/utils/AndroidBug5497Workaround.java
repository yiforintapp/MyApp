package com.zlf.appmaster.utils;

//Workaround to get adjustResize functionality for input methos when the fullscreen mode is on
//found by Ricardo
//taken from http://stackoverflow.com/a/19494006

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.zlf.appmaster.R;


public class AndroidBug5497Workaround {
    private static final String TAG =  AndroidBug5497Workaround.class.getSimpleName();
    // For more information, see https://code.google.com/p/android/issues/detail?id=5497
    // To use this class, simply invoke assistActivity() on an Activity that already has its content view set.

    /**
     * 要在头部加入状态栏view，传入的contentViewID必须为linearLayout
     * @param activity
     * @param contentViewID
     * @param statusBarColor 状态条颜色
     */

    public static void assistActivity (Activity activity, int contentViewID, int statusBarColor) {
        new AndroidBug5497Workaround(activity, contentViewID, statusBarColor);
    }

    /**
     * 默认状态条颜色
     * @param activity
     * @param contentViewID
     */
    public static void assistActivity (Activity activity, int contentViewID) {
        assistActivity(activity, contentViewID, activity.getResources().getColor(R.color.title_bar_bg));
    }
    /**
     * 不在头部加入状态栏view
     * @param activity
     */
    public static void assistActivity (Activity activity) {
        assistActivity(activity, 0);
    }

    private View mChildOfContent;
    private int usableHeightPrevious;
    private FrameLayout.LayoutParams frameLayoutParams;
    private Context mContext;

    private AndroidBug5497Workaround(Activity activity, int contentViewID, int statusBarColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mContext = activity;
            FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);
            mChildOfContent = content.getChildAt(0);
            mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                   possiblyResizeChildOfContent();
                }
            });
            frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();
            usableHeightPrevious = frameLayoutParams.height;

            if (contentViewID != 0)
                compat(activity, contentViewID, statusBarColor);
        }
    }

    /**
     *
     */
    private void possiblyResizeChildOfContent() {
        int usableHeightNow = computeUsableHeight();
        QLog.i(TAG, "usableHeightNow: " + usableHeightNow);
        if (usableHeightNow != usableHeightPrevious) {
            int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
            QLog.i(TAG, "usableHeightSansKeyboard: " + usableHeightSansKeyboard); // 1920
            int heightDifference = usableHeightSansKeyboard - usableHeightNow;
            QLog.i(TAG, "heightDifference: " + heightDifference);
            if (heightDifference > (usableHeightSansKeyboard/4)) {
                // keyboard probably just became visible
                frameLayoutParams.height = usableHeightNow;
            } else {
                // keyboard probably just became hidden
                /*frameLayoutParams.height = usableHeightSansKeyboard;*/
                // 考虑到有些手机有虚拟键盘的情况，这里也直接用高度comment by Deping Huang 2016/3/7
                frameLayoutParams.height = usableHeightNow;
            }
            mChildOfContent.requestLayout();
            usableHeightPrevious = usableHeightNow;
        }
    }

    private int computeUsableHeight() {
        Rect r = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(r);
        //return (r.bottom - r.top);    // 全屏模式，显示区域不需要减去状态栏高度（而且有的手机r.top也为0）
        return r.bottom;
    }


    private static final int COLOR_DEFAULT = Color.parseColor("#00000000");
    public static void compat(Activity activity, int content_layout, int statusBarColor) {

            View statusBarView = new View(activity);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    DipPixelUtil.getStatusBarHeight(activity));
            statusBarView.setBackgroundColor(statusBarColor);
            LinearLayout contentLayout = (LinearLayout)activity.findViewById(content_layout);
            contentLayout.addView(statusBarView, 0, lp);

    }



}
