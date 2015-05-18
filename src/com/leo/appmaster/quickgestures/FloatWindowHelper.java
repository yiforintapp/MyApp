
package com.leo.appmaster.quickgestures;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.graphics.drawable.ColorDrawable;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.quickgestures.ui.QuickGesturePopupActivity;
import com.leo.appmaster.quickgestures.view.SectorQuickGestureContainer;
import com.leo.appmaster.quickgestures.view.QuickGesturesAreaView;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;

/**
 * QuickGestureWindowManager
 * 
 * @author run
 */
@SuppressLint("InflateParams")
public class FloatWindowHelper {
    public static final String QUICK_GESTURE_SETTING_DIALOG_RADIO_FINISH_NOTIFICATION = "quick_gesture_setting_dialog_radio_finish_notification";
    public static final String QUICK_GESTURE_SETTING_DIALOG_RADIO_SLIDE_TIME_SETTING_FINISH_NOTIFICATION = "quick_gesture_setting_dialog_radio_slide_time_setting_finish_notification";
    public static final int ONTUCH_LEFT_FLAG = -1;
    public static final int ONTUCH_RIGHT_FLAG = 1;
    public static final String QUICK_GESTURE_MSM_TIP = "quick_gesture_msm_tip";
    private static QuickGesturesAreaView mLeftBottomView, mLeftCenterView, mLeftTopView,
            mLeftCenterCenterView;
    private static RelativeLayout mMiuiTipRl;
    private static QuickGesturesAreaView mRightBottomView, mRightCenterView, mRightTopView,
            mRightCenterCenterView;
    private static LayoutParams mLeftBottomParams, mLeftCenterParams, mLeftTopParams,
            mLeftCenterCenterParams, mMiuiTipParams;
    private static LayoutParams mRightBottomParams, mRightCenterParams, mRightTopParams,
            mRightCenterCenterParams;
    private static WindowManager mWindowManager;

    public static boolean mGestureShowing = false;
    public static boolean mEditQuickAreaFlag = false;
    private static float startX;
    private static float startY;
    // private static WindowManager windowManager;
    private static boolean isMoveIng = false;
    public static boolean isFanShowing = false;
    // 左下宽度
    private static float mLeftBottomWidth = 200;
    // 左下高度
    private static float mLeftBottomHeight = 100;
    // 左中宽度
    private static float mLeftCenterWidth = 100;
    // 左中高度
    private static float mLeftCenterHeight = 200;
    // 左侧中部高度
    private static float mLeftCenterCenterHeight = 800;
    // 左上宽度
    private static float mLeftTopWidth = 50;
    // 左上高度
    private static float mLeftTopHeight = 300;
    // 右下宽度
    private static float mRightBottomWidth = 200;
    // 右下高度
    private static float mRightBottomHeight = 100;
    // 右中宽度
    private static float mRightCenterWidth = 100;
    // 右中高度
    private static float mRightCenterHeight = 200;
    // 右侧中部高度
    private static float mRightCenterCenterHeight = 800;
    // 右上宽度
    private static float mRightTopWidth = 50;
    // 右上高度
    private static float mRightTopHeight = 300;
    // 距离底部的距离
    private static float mMarginBottom = 200;

    private static final int LEFT_BOTTOM_FLAG = 1;
    private static final int LEFT_CENTER_FLAG = 2;
    private static final int LEFT_TOP_FLAG = 3;
    private static final int LEFT_CENTER_CENTER_FLAG = 4;
    private static final int RIGHT_BOTTOM_FLAG = -1;
    private static final int RIGHT_CENTER_FLAG = -2;
    private static final int RIGHT_CENTER_CENTER_FLAG = -3;
    private static final int RIGHT_TOP_FLAG = -4;

    // TODO
    private void slidRemoveArea(int flag) {
        if (LEFT_BOTTOM_FLAG == flag) {

        } else if (LEFT_CENTER_FLAG == flag) {

        } else if (LEFT_TOP_FLAG == flag) {

        } else if (LEFT_CENTER_CENTER_FLAG == flag) {

        } else if (RIGHT_BOTTOM_FLAG == flag) {

        } else if (RIGHT_CENTER_FLAG == flag) {

        } else if (RIGHT_CENTER_CENTER_FLAG == flag) {

        } else if (RIGHT_TOP_FLAG == flag) {

        }
    }

    public static void initFloatWindwo(final Context mContext, int value, View view,
            LayoutParams layoutParams, int flag) {
        final WindowManager windowManager = getWindowManager(mContext);
        if (view == null) {
            view = new QuickGesturesAreaView(mContext);
            view.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_OUTSIDE:
                            break;
                        case MotionEvent.ACTION_DOWN:
                            startX = event.getRawX();
                            startY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float moveX = Math.abs(startX - event.getRawX());
                            float moveY = Math.abs(startY - event.getRawY());
                            if ((moveX > mLeftBottomParams.width / 4 || moveY > mLeftBottomParams.width / 4)
                                    && !isMoveIng) {
                                isMoveIng = true;
                                removeAllFloatWindow(mContext);
                                mGestureShowing = true;
                                onTouchAreaShowQuick(-1);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            if (Math.abs(startX - event.getRawX()) < 5
                                    || Math.abs(startY - event.getRawY()) < 5) {
                                removeSwipWindow(mContext, 1);
                            }
                            break;
                    }
                    return false;
                }
            });

            if (layoutParams == null) {
                int width = windowManager.getDefaultDisplay().getWidth();
                int height = windowManager.getDefaultDisplay().getHeight();
                layoutParams = new LayoutParams();
                layoutParams.width = (int) ((mLeftBottomWidth / 2) + (value / 2)) * 2;
                layoutParams.height = (int) ((mLeftBottomHeight / 2) + (value)) * 2;
                layoutParams.x = -(width / 2);
                layoutParams.y = (height / 2) - value;
                layoutParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
                layoutParams.format = PixelFormat.RGBA_8888;
                layoutParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            }
            windowManager.addView(view, layoutParams);
        }
    }

    // 左下
    /**
     * must call in UI thread
     * 
     * @param context
     */
    public static void createFloatLeftBottomWindow(final Context mContext, int value) {
        final WindowManager windowManager = getWindowManager(mContext);
        if (mLeftBottomView == null) {
            mLeftBottomView = new QuickGesturesAreaView(mContext);
            if (LockManager.getInstatnce().isShowSysNoReadMessage
                    && LockManager.getInstatnce().onTuchGestureFlag == -1) {
                mLeftBottomView.setIsShowReadTip(true, 1);
            }
            mLeftBottomView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_OUTSIDE:
                            break;
                        case MotionEvent.ACTION_DOWN:
                            isMoveIng = false;
                            startX = event.getRawX();
                            startY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float moveX = Math.abs(startX - event.getRawX());
                            float moveY = Math.abs(startY - event.getRawY());
                            if (((moveX > mLeftBottomParams.width / 7 || moveY > mLeftBottomParams.height / 5)
                            && !isMoveIng)) {
                                isMoveIng = true;
                                removeAllFloatWindow(mContext);
                                mGestureShowing = true;
                                onTouchAreaShowQuick(-1);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            if (Math.abs(startX - event.getRawX()) < 10
                                    || Math.abs(startY - event.getRawY()) < 10) {
                                removeSwipWindow(mContext, 1);
                            }
                            break;

                        case MotionEvent.ACTION_CANCEL:
                            isMoveIng = false;
                            break;
                    }
                    return false;
                }
            });
            int width = windowManager.getDefaultDisplay().getWidth();
            int height = windowManager.getDefaultDisplay().getHeight();
            int flag = Utilities.isScreenType(mContext);
            if (mLeftBottomParams == null) {
                mLeftBottomParams = new LayoutParams();
                mLeftBottomParams.width = (int) ((mLeftBottomWidth / 2) + (value / 2)) * 2;
                mLeftBottomParams.height = (int) ((mLeftBottomHeight / 2) + (value)) * 2;
                mLeftBottomParams.x = -(width / 2);
                mLeftBottomParams.y = (height / 2) - value;
                mLeftBottomParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
                mLeftBottomParams.format = PixelFormat.RGBA_8888;
                mLeftBottomParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;

            } else {
                mLeftBottomParams.x = -(width / 2);
                mLeftBottomParams.y = (height / 2) - value;
            }
            if (!mGestureShowing) {
                windowManager.addView(mLeftBottomView, mLeftBottomParams);
            } else {
                mLeftBottomView = null;
            }
        }
    }

    // 左中
    /**
     * must call in UI thread
     * 
     * @param context
     */
    public static void createFloatLeftCenterWindow(final Context mContext, int value) {
        final WindowManager windowManager = getWindowManager(mContext);
        if (mLeftCenterView == null) {
            mLeftCenterView = new QuickGesturesAreaView(mContext);
            mLeftCenterView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_OUTSIDE:
                            break;
                        case MotionEvent.ACTION_DOWN:
                            isMoveIng = false;
                            startX = event.getRawX();
                            startY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float moveX = Math.abs(startX - event.getRawX());
                            float moveY = Math.abs(startY - event.getRawY());
                            if ((moveX > mLeftCenterParams.width / 5 || moveY > mLeftCenterParams.width / 5)
                                    && !isMoveIng) {
                                isMoveIng = true;
                                removeAllFloatWindow(mContext);
                                mGestureShowing = true;
                                onTouchAreaShowQuick(-1);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            if (Math.abs(startX - event.getRawX()) < 10
                                    || Math.abs(startY - event.getRawY()) < 10) {
                                removeSwipWindow(mContext, 2);
                            }
                            break;
                    }
                    return false;
                }
            });
            int width = windowManager.getDefaultDisplay().getWidth();
            int height = windowManager.getDefaultDisplay().getHeight();
            if (mLeftCenterParams == null) {
                mLeftCenterParams = new LayoutParams();
                mLeftCenterParams.x = (int) -(width / 2);
                mLeftCenterParams.y = (int) ((height / 2)
                        - ((mLeftCenterHeight / 2) + mLeftBottomParams.height) - 10) - value;
                mLeftCenterParams.width = (int) ((mLeftCenterWidth / 2) + (value / 2)) * 2;
                mLeftCenterParams.height = (int) ((mLeftCenterHeight / 2) + (value)) * 2;
                mLeftCenterParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
                mLeftCenterParams.format = PixelFormat.RGBA_8888;
                mLeftCenterParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            } else {
                mLeftCenterParams.x = (int) -(width / 2);
                mLeftCenterParams.y = (int) ((height / 2)
                        - ((mLeftCenterHeight / 2) + mLeftBottomParams.height) - 10) - value;
            }

            if (!mGestureShowing) {
                windowManager.addView(mLeftCenterView, mLeftCenterParams);
            } else {
                mLeftCenterView = null;
            }
        }
    }

    // 左侧中部
    /**
     * must call in UI thread
     * 
     * @param context
     */
    public static void createFloatLeftCenterCenterWindow(final Context mContext, int value) {
        final WindowManager windowManager = getWindowManager(mContext);
        if (mLeftCenterCenterView == null) {
            mLeftCenterCenterView = new QuickGesturesAreaView(mContext);
            if (LockManager.getInstatnce().isShowSysNoReadMessage
                    && LockManager.getInstatnce().onTuchGestureFlag == 1 && mLeftBottomView == null) {
                mLeftCenterCenterView.setIsShowReadTip(true, 3);
            }
            mLeftCenterCenterView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_OUTSIDE:
                            break;
                        case MotionEvent.ACTION_DOWN:
                            isMoveIng = false;
                            startX = event.getRawX();
                            startY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float moveX = Math.abs(startX - event.getRawX());
                            float moveY = Math.abs(startY - event.getRawY());
                            if ((moveX > mLeftCenterCenterParams.width / 5 || moveY > mLeftCenterCenterParams.width / 5)
                                    && !isMoveIng) {
                                isMoveIng = true;
                                removeAllFloatWindow(mContext);
                                mGestureShowing = true;
                                onTouchAreaShowQuick(-1);

                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            if (Math.abs(startX - event.getRawX()) < 10
                                    || Math.abs(startY - event.getRawY()) < 10) {
                                removeSwipWindow(mContext, 4);
                            }
                            break;
                    }
                    return false;
                }
            });
            int width = windowManager.getDefaultDisplay().getWidth();
            int height = windowManager.getDefaultDisplay().getHeight();
            int leftBottomHeight = (int) ((mLeftBottomHeight / 2) + (value)) * 2;
            if (mLeftCenterCenterParams == null) {
                mLeftCenterCenterParams = new LayoutParams();
                mLeftCenterCenterParams.x = (int) -(width / 2);
                mLeftCenterCenterParams.y = (int) ((height / 2)
                        - ((mLeftCenterHeight / 2) + leftBottomHeight) - 10) - value;
                mLeftCenterCenterParams.width = (int) ((mLeftCenterWidth / 2) + (value / 2)) * 2;
                mLeftCenterCenterParams.height = (int) ((mLeftCenterCenterHeight / 2) + (value)) * 2;
                mLeftCenterCenterParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
                mLeftCenterCenterParams.format = PixelFormat.RGBA_8888;
                mLeftCenterCenterParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            } else {
                mLeftCenterCenterParams.x = (int) -(width / 2);
                mLeftCenterCenterParams.y = (int) ((height / 2)
                        - ((mLeftCenterHeight / 2) + leftBottomHeight) - 10) - value;
            }

            if (!mGestureShowing) {
                windowManager.addView(mLeftCenterCenterView, mLeftCenterCenterParams);
            } else {
                mLeftCenterCenterView = null;
            }
        }
    }

    // 左上
    /**
     * must call in UI thread
     * 
     * @param context
     */
    public static void createFloatLeftTopWindow(final Context mContext, int value) {
        final WindowManager windowManager = getWindowManager(mContext);
        if (mLeftTopView == null) {
            mLeftTopView = new QuickGesturesAreaView(mContext);
            mLeftTopView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_OUTSIDE:
                            break;
                        case MotionEvent.ACTION_DOWN:
                            isMoveIng = false;
                            startX = event.getRawX();
                            startY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float moveX = Math.abs(startX - event.getRawX());
                            float moveY = Math.abs(startY - event.getRawY());
                            if ((moveX > mLeftTopParams.width / 5 || moveY > mLeftTopParams.width / 5)
                                    && !isMoveIng) {
                                isMoveIng = true;
                                removeAllFloatWindow(mContext);
                                mGestureShowing = true;
                                onTouchAreaShowQuick(-1);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            if (Math.abs(startX - event.getRawX()) < 10
                                    || Math.abs(startY - event.getRawY()) < 10) {
                                removeSwipWindow(mContext, 3);
                            }
                            break;
                    }
                    return false;
                }
            });
            int width = windowManager.getDefaultDisplay().getWidth();
            int height = windowManager.getDefaultDisplay().getHeight();
            if (mLeftTopParams == null) {
                mLeftTopParams = new LayoutParams();
                mLeftTopParams.x = -(width / 2);
                mLeftTopParams.y = (int) ((height / 2) - ((mLeftTopHeight / 2)
                        + mLeftBottomParams.height + mLeftCenterParams.height))
                        - value;
                mLeftTopParams.width = (int) ((mLeftTopWidth / 2) + (value / 2)) * 2;
                mLeftTopParams.height = (int) ((mLeftTopHeight / 2) + (value)) * 2;
                mLeftTopParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
                mLeftTopParams.format = PixelFormat.RGBA_8888;
                mLeftTopParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            } else {
                mLeftTopParams.x = -(width / 2);
                mLeftTopParams.y = (int) ((height / 2) - ((mLeftTopHeight / 2)
                        + mLeftBottomParams.height + mLeftCenterParams.height))
                        - value;
            }

            if (!mGestureShowing) {
                windowManager.addView(mLeftTopView, mLeftTopParams);
            } else {
                mLeftTopView = null;
            }
        }
    }

    // 右下
    /**
     * must call in UI thread
     * 
     * @param context
     */
    public static void createFloatRightBottomWindow(final Context mContext, int value) {
        final WindowManager windowManager = getWindowManager(mContext);
        if (mRightBottomView == null) {
            mRightBottomView = new QuickGesturesAreaView(mContext);
            if (LockManager.getInstatnce().isShowSysNoReadMessage
                    && LockManager.getInstatnce().onTuchGestureFlag == 1) {
                mRightBottomView.setIsShowReadTip(true, 2);
            }
            mRightBottomView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_OUTSIDE:
                            break;
                        case MotionEvent.ACTION_DOWN:
                            isMoveIng = false;
                            startX = event.getRawX();
                            startY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float moveX = Math.abs(startX - event.getRawX());
                            float moveY = Math.abs(startY - event.getRawY());
                            if ((moveX > mRightBottomParams.width / 7 || moveY > mRightBottomParams.height / 5)
                                    && !isMoveIng) {
                                isMoveIng = true;
                                removeAllFloatWindow(mContext);
                                mGestureShowing = true;
                                onTouchAreaShowQuick(1);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            if (Math.abs(startX - event.getRawX()) < 10
                                    || Math.abs(startY - event.getRawY()) < 10) {
                                removeSwipWindow(mContext, -1);
                            }
                            break;
                    }
                    return false;
                }
            });
            int width = windowManager.getDefaultDisplay().getWidth();
            int height = windowManager.getDefaultDisplay().getHeight();
            if (mRightBottomParams == null) {
                mRightBottomParams = new LayoutParams();
                mRightBottomParams.width = (int) ((mRightBottomWidth / 2) + (value / 2)) * 2;
                mRightBottomParams.height = (int) ((mRightBottomHeight / 2) + (value)) * 2;
                mRightBottomParams.x = +(width / 2);
                mRightBottomParams.y = (height / 2) - value;
                mRightBottomParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
                mRightBottomParams.format = PixelFormat.RGBA_8888;
                mRightBottomParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            } else {
                mRightBottomParams.x = +(width / 2);
                mRightBottomParams.y = (height / 2) - value;
            }

            if (!mGestureShowing) {
                windowManager.addView(mRightBottomView, mRightBottomParams);
            } else {
                mRightBottomView = null;
            }
        }
    }

    // 右中
    /**
     * must call in UI thread
     * 
     * @param context
     */
    public static void createFloatRightCenterWindow(final Context mContext, int value) {
        final WindowManager windowManager = getWindowManager(mContext);
        if (mRightCenterView == null) {
            mRightCenterView = new QuickGesturesAreaView(mContext);
            mRightCenterView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_OUTSIDE:
                            break;
                        case MotionEvent.ACTION_DOWN:
                            isMoveIng = false;
                            startX = event.getRawX();
                            startY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float moveX = Math.abs(startX - event.getRawX());
                            float moveY = Math.abs(startY - event.getRawY());
                            if ((moveX > mRightCenterParams.width / 5 || moveY > mRightCenterParams.width / 5)
                                    && !isMoveIng) {
                                isMoveIng = true;
                                removeAllFloatWindow(mContext);
                                mGestureShowing = true;
                                onTouchAreaShowQuick(1);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            if (Math.abs(startX - event.getRawX()) < 10
                                    || Math.abs(startY - event.getRawY()) < 10) {
                                removeSwipWindow(mContext, -2);
                            }
                            break;
                    }
                    return false;
                }
            });
            int width = windowManager.getDefaultDisplay().getWidth();
            int height = windowManager.getDefaultDisplay().getHeight();
            if (mRightCenterParams == null) {
                mRightCenterParams = new LayoutParams();
                mRightCenterParams.width = (int) ((mRightCenterWidth / 2) + (value / 2)) * 2;
                mRightCenterParams.height = (int) ((mRightCenterHeight / 2) + (value)) * 2;
                mRightCenterParams.x = (int) (width / 2);
                mRightCenterParams.y = (int) ((height / 2)
                        - ((mRightCenterHeight / 2) + mRightBottomParams.height) - 10) - value;
                mRightCenterParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
                mRightCenterParams.format = PixelFormat.RGBA_8888;
                mRightCenterParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            } else {
                mRightCenterParams.x = (int) (width / 2);
                mRightCenterParams.y = (int) ((height / 2)
                        - ((mRightCenterHeight / 2) + mRightBottomParams.height) - 10) - value;
            }

            if (!mGestureShowing) {
                windowManager.addView(mRightCenterView, mRightCenterParams);
            } else {
                mRightCenterView = null;
            }
        }
    }

    // 右侧中部
    /**
     * must call in UI thread
     * 
     * @param context
     */
    public static void createFloatRightCenterCenterWindow(final Context mContext, int value) {
        final WindowManager windowManager = getWindowManager(mContext);
        if (mRightCenterCenterView == null) {
            mRightCenterCenterView = new QuickGesturesAreaView(mContext);
            if (LockManager.getInstatnce().isShowSysNoReadMessage
                    && LockManager.getInstatnce().onTuchGestureFlag == 1
                    && mRightBottomView == null) {
                mRightCenterCenterView.setIsShowReadTip(true, 4);
            }
            mRightCenterCenterView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_OUTSIDE:
                            break;
                        case MotionEvent.ACTION_DOWN:
                            isMoveIng = false;
                            startX = event.getRawX();
                            startY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float moveX = Math.abs(startX - event.getRawX());
                            float moveY = Math.abs(startY - event.getRawY());
                            if ((moveX > mRightCenterCenterParams.width / 5 || moveY > mRightCenterCenterParams.width / 5)
                                    && !isMoveIng) {
                                isMoveIng = true;
                                removeAllFloatWindow(mContext);
                                mGestureShowing = true;
                                onTouchAreaShowQuick(1);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            if (Math.abs(startX - event.getRawX()) < 10
                                    || Math.abs(startY - event.getRawY()) < 10) {
                                removeSwipWindow(mContext, -4);
                            }
                            break;
                    }
                    return false;
                }
            });
            int width = windowManager.getDefaultDisplay().getWidth();
            int height = windowManager.getDefaultDisplay().getHeight();
            int rightBottomHeight = (int) ((mRightBottomHeight / 2) + (value)) * 2;
            if (mRightCenterCenterParams == null) {
                mRightCenterCenterParams = new LayoutParams();
                mRightCenterCenterParams.width = (int) ((mRightCenterWidth / 2) + (value / 2)) * 2;
                mRightCenterCenterParams.height = (int) ((mRightCenterCenterHeight / 2) + (value)) * 2;
                mRightCenterCenterParams.x = (int) (width / 2);
                mRightCenterCenterParams.y = (int) ((height / 2)
                        - ((mRightCenterHeight / 2) + rightBottomHeight) - 10) - value;
                mRightCenterCenterParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
                mRightCenterCenterParams.format = PixelFormat.RGBA_8888;
                mRightCenterCenterParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            } else {
                mRightCenterCenterParams.x = (int) (width / 2);
                mRightCenterCenterParams.y = (int) ((height / 2)
                        - ((mRightCenterHeight / 2) + rightBottomHeight) - 10) - value;
            }

            if (!mGestureShowing) {
                windowManager.addView(mRightCenterCenterView, mRightCenterCenterParams);
            } else {
                mRightCenterCenterView = null;
            }
        }
    }

    // 右上
    /**
     * must call in UI thread
     * 
     * @param context
     */
    public static void createFloatRightTopWindow(final Context mContext, int value) {
        final WindowManager windowManager = getWindowManager(mContext);
        if (mRightTopView == null) {
            mRightTopView = new QuickGesturesAreaView(mContext);
            mRightTopView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_OUTSIDE:
                            break;
                        case MotionEvent.ACTION_DOWN:
                            isMoveIng = false;
                            startX = event.getRawX();
                            startY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float moveX = Math.abs(startX - event.getRawX());
                            float moveY = Math.abs(startY - event.getRawY());
                            if ((moveX > mRightTopParams.width / 5 || moveY > mRightTopParams.width / 5)
                                    && !isMoveIng) {
                                isMoveIng = true;
                                removeAllFloatWindow(mContext);
                                mGestureShowing = true;
                                onTouchAreaShowQuick(1);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            if (Math.abs(startX - event.getRawX()) < 10
                                    || Math.abs(startY - event.getRawY()) < 10) {
                                removeSwipWindow(mContext, -3);
                            }
                            break;
                    }
                    return false;
                }
            });
            int width = windowManager.getDefaultDisplay().getWidth();
            int height = windowManager.getDefaultDisplay().getHeight();
            if (mRightTopParams == null) {
                mRightTopParams = new LayoutParams();
                mRightTopParams.width = (int) ((mRightTopWidth / 2) + (value / 2)) * 2;
                mRightTopParams.height = (int) ((mRightTopHeight / 2) + (value)) * 2;
                mRightTopParams.x = (width / 2);
                mRightTopParams.y = (int) ((height / 2) - ((mRightTopHeight / 2)
                        + mRightBottomParams.height + mRightCenterParams.height))
                        - value;
                mRightTopParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
                mRightTopParams.format = PixelFormat.RGBA_8888;
                mRightTopParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            } else {
                mRightTopParams.x = (width / 2);
                mRightTopParams.y = (int) ((height / 2) - ((mRightTopHeight / 2)
                        + mRightBottomParams.height + mRightCenterParams.height))
                        - value;
            }

            if (!mGestureShowing) {
                windowManager.addView(mRightTopView, mRightTopParams);
            } else {
                mRightTopView = null;
            }
        }
    }

    /**
     * 移除悬浮窗口 must call in UI thread
     * 
     * @param context
     */
    public static void removeSwipWindow(Context context, int flag) {
        WindowManager windowManager = getWindowManager(context);
        if (LEFT_BOTTOM_FLAG == flag) {
            // 左下
            if (mLeftBottomView != null) {
                windowManager.removeView(mLeftBottomView);
                mLeftBottomView = null;
            }
        } else if (LEFT_CENTER_FLAG == flag) {
            // 左中
            if (mLeftCenterView != null) {
                windowManager.removeView(mLeftCenterView);
                mLeftCenterView = null;
            }
        } else if (LEFT_TOP_FLAG == flag) {
            // 左上
            if (mLeftTopView != null) {
                windowManager.removeView(mLeftTopView);
                mLeftTopView = null;
            }
        } else if (LEFT_CENTER_CENTER_FLAG == flag) {
            // 左侧中部
            if (mLeftCenterCenterView != null) {
                windowManager.removeView(mLeftCenterCenterView);
                mLeftCenterCenterView = null;
            }
        } else if (RIGHT_BOTTOM_FLAG == flag) {
            // 右下
            if (mRightBottomView != null) {
                windowManager.removeView(mRightBottomView);
                mRightBottomView = null;
            }
        } else if (RIGHT_CENTER_FLAG == flag) {
            // 右中
            if (mRightCenterView != null) {
                windowManager.removeView(mRightCenterView);
                mRightCenterView = null;
            }
        } else if (RIGHT_CENTER_CENTER_FLAG == flag) {
            // 右上
            if (mRightTopView != null) {
                windowManager.removeView(mRightTopView);
                mRightTopView = null;
            }
        } else if (RIGHT_TOP_FLAG == flag) {
            // 右侧中部
            if (mRightCenterCenterView != null) {
                windowManager.removeView(mRightCenterCenterView);
                mRightCenterCenterView = null;
            }
        }
    }

    // 删除底部所有悬浮窗
    public static void removeAllFloatWindow(Context context) {
        removeSwipWindow(context, 1);
        removeSwipWindow(context, 2);
        removeSwipWindow(context, 3);
        removeSwipWindow(context, 4);
        removeSwipWindow(context, -1);
        removeSwipWindow(context, -2);
        removeSwipWindow(context, -3);
        removeSwipWindow(context, -4);
    }

    // 删除显示红点提示的悬浮窗
    public static void removeShowReadTipWindow(Context context) {
        removeSwipWindow(context, 1);
        removeSwipWindow(context, 4);
        removeSwipWindow(context, -1);
        removeSwipWindow(context, -4);
    }

    /**
     * must call in UI thread
     * 
     * @param context
     */
    public static void updateView(Context context, int value) {
        WindowManager windowManager = getWindowManager(context);
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        int height = display.getHeight();
        int width = display.getWidth();
        // 左下
        if (mLeftBottomParams != null) {
            mLeftBottomParams.width = (int) ((mLeftBottomWidth / 2) + (value / 2)) * 2;
            mLeftBottomParams.height = (int) ((mLeftBottomHeight / 2) + (value)) * 2;
            mLeftBottomParams.x = -(width / 2);
            mLeftBottomParams.y = (height / 2) - value;
        }
        // 左中
        if (mLeftCenterParams != null) {
            mLeftCenterParams.x = (int) -(width / 2);
            mLeftCenterParams.y = (int) ((height / 2)
                    - ((mLeftCenterHeight / 2) + mLeftBottomParams.height) - 10) - value;
            mLeftCenterParams.width = (int) ((mLeftCenterWidth / 2) + (value / 2)) * 2;
            mLeftCenterParams.height = (int) ((mLeftCenterHeight / 2) + (value)) * 2;
        }
        // 左边中部
        if (mLeftCenterCenterParams != null) {
            mLeftCenterCenterParams.x = (int) -(width / 2);
            mLeftCenterCenterParams.y = (int) ((height / 2)
                    - ((mLeftCenterHeight / 2) + mLeftBottomParams.height) - 10) - value;
            mLeftCenterCenterParams.width = (int) ((mLeftCenterWidth / 2) + (value / 2)) * 2;
            mLeftCenterCenterParams.height = (int) ((mLeftCenterCenterHeight / 2) + (value)) * 2;
        }
        // 左上
        if (mLeftTopParams != null) {
            mLeftTopParams.x = -(width / 2);
            mLeftTopParams.y = (int) ((height / 2) - ((mLeftTopHeight / 2)
                    + mLeftBottomParams.height + mLeftCenterParams.height))
                    - value;
            mLeftTopParams.width = (int) ((mLeftTopWidth / 2) + (value / 2)) * 2;
            mLeftTopParams.height = (int) ((mLeftTopHeight / 2) + (value)) * 2;
        }
        // 右下
        if (mRightBottomParams != null) {
            mRightBottomParams.width = (int) ((mRightBottomWidth / 2) + (value / 2)) * 2;
            mRightBottomParams.height = (int) ((mRightBottomHeight / 2) + (value)) * 2;
            mRightBottomParams.x = +(width / 2);
            mRightBottomParams.y = (height / 2) - value;
        }
        // 右中
        if (mRightCenterParams != null) {
            mRightCenterParams.x = (int) (width / 2);
            mRightCenterParams.y = (int) ((height / 2)
                    - ((mRightCenterHeight / 2) + mRightBottomParams.height) - 10) - value;
            mRightCenterParams.width = (int) ((mRightCenterWidth / 2) + (value / 2)) * 2;
            mRightCenterParams.height = (int) ((mRightCenterHeight / 2) + (value)) * 2;
        }
        // 右上
        if (mRightTopParams != null) {
            mRightTopParams.x = (width / 2);
            mRightTopParams.y = (int) ((height / 2) - ((mRightTopHeight / 2)
                    + mRightBottomParams.height + mRightCenterParams.height))
                    - value;
            mRightTopParams.width = (int) ((mRightTopWidth / 2) + (value / 2)) * 2;
            mRightTopParams.height = (int) ((mRightTopHeight / 2) + (value)) * 2;
        }
        // 右侧中部
        if (mRightCenterCenterParams != null) {
            mRightCenterCenterParams.x = (int) (width / 2);
            mRightCenterCenterParams.y = (int) ((height / 2)
                    - ((mRightCenterHeight / 2) + mRightBottomParams.height) - 10) - value;
            mRightCenterCenterParams.width = (int) ((mRightCenterWidth / 2) + (value / 2)) * 2;
            mRightCenterCenterParams.height = (int) ((mRightCenterCenterHeight / 2) + (value)) * 2;
        }
        // 更新左边
        if (mLeftBottomView != null) {
            mWindowManager.updateViewLayout(mLeftBottomView, mLeftBottomParams);
        }
        if (mLeftCenterView != null) {
            mWindowManager.updateViewLayout(mLeftCenterView, mLeftCenterParams);
        }
        if (mLeftTopView != null) {
            mWindowManager.updateViewLayout(mLeftTopView, mLeftTopParams);
        }
        if (mLeftCenterCenterView != null) {
            mWindowManager.updateViewLayout(mLeftCenterCenterView, mLeftCenterCenterParams);
        }
        // 更新右边
        if (mRightBottomView != null) {
            mWindowManager.updateViewLayout(mRightBottomView, mRightBottomParams);
        }
        if (mRightCenterView != null) {
            mWindowManager.updateViewLayout(mRightCenterView, mRightCenterParams);
        }
        if (mRightTopView != null) {
            mWindowManager.updateViewLayout(mRightTopView, mRightTopParams);
        }
        if (mRightCenterCenterView != null) {
            mWindowManager.updateViewLayout(mRightCenterCenterView, mRightCenterCenterParams);
        }
    }

    public static boolean isLeftBottomShowing() {
        return mLeftBottomView != null;
    }

    public static boolean isLeftCenterShowing() {
        return mLeftCenterView != null;
    }

    public static boolean isLeftTopShowing() {
        return mLeftTopView != null;
    }

    public static boolean isLeftCenterCenterShowing() {
        return mLeftCenterCenterView != null;
    }

    public static boolean isRightBottomShowing() {
        return mRightBottomView != null;
    }

    public static boolean isRightCenterShowing() {
        return mRightCenterView != null;
    }

    public static boolean isRightTopShowing() {
        return mRightTopView != null;
    }

    public static boolean isRightCenterCenterShowing() {
        return mRightCenterCenterView != null;
    }

    private static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

    /**
     * must call in UI thread
     * 
     * @param context
     */
    public static void createFloatWindow(Context context, int value) {
        if (!FloatWindowHelper.isLeftBottomShowing()
                || !FloatWindowHelper.isLeftCenterShowing()
                || !FloatWindowHelper.isLeftTopShowing()
                || !FloatWindowHelper.isRightBottomShowing()
                || !FloatWindowHelper.isRightCenterShowing()
                || !FloatWindowHelper.isRightTopShowing()
                || !FloatWindowHelper.isLeftCenterCenterShowing()
                || !FloatWindowHelper.isRightCenterCenterShowing()) {
            AppMasterPreference pre = AppMasterPreference
                    .getInstance(context);
            // 透明悬浮窗
            // 左侧底部
            if (pre.getDialogRadioLeftBottom()) {
                FloatWindowHelper
                        .createFloatLeftBottomWindow(context, value);
                FloatWindowHelper
                        .createFloatLeftCenterWindow(context, value);
                FloatWindowHelper
                        .createFloatLeftTopWindow(context, value);
            } else {
                // FloatWindowHelper.removeSwipWindow(context, 1);
                // FloatWindowHelper.removeSwipWindow(context, 2);
                // FloatWindowHelper.removeSwipWindow(context, 3);
            }
            // 右侧底部
            if (pre.getDialogRadioRightBottom()) {
                FloatWindowHelper
                        .createFloatRightBottomWindow(context, value);
                FloatWindowHelper
                        .createFloatRightCenterWindow(context, value);
                FloatWindowHelper
                        .createFloatRightTopWindow(context, value);
            } else {
                // FloatWindowHelper.removeSwipWindow(context, -1);
                // FloatWindowHelper.removeSwipWindow(context, -2);
                // FloatWindowHelper.removeSwipWindow(context, -3);
            }
            // 左侧中部
            if (pre.getDialogRadioLeftCenter()) {
                FloatWindowHelper.removeSwipWindow(context, 2);
                FloatWindowHelper.removeSwipWindow(context, 3);
                FloatWindowHelper.createFloatLeftCenterCenterWindow(context, value);
                if (pre.getDialogRadioLeftBottom()) {
                    FloatWindowHelper
                            .createFloatLeftBottomWindow(context, value);
                }
            }
            // 右侧中部
            if (pre.getDialogRadioRightCenter()) {
                FloatWindowHelper.removeSwipWindow(context, -2);
                FloatWindowHelper.removeSwipWindow(context, -3);
                FloatWindowHelper.createFloatRightCenterCenterWindow(context, value);
                if (pre.getDialogRadioRightBottom()) {
                    FloatWindowHelper
                            .createFloatRightBottomWindow(context, value);

                }
            }
        }
    }

    /**
     * must call in UI thread
     * 
     * @param context
     */
    public static void updateFloatWindowBackgroudColor(boolean flag) {
        if (flag) {
            if (mLeftBottomView != null) {
                mLeftBottomView
                        .setBackgroundResource(R.color.quick_gesture_switch_setting_show_color);
            }
            if (mLeftCenterView != null) {
                mLeftCenterView
                        .setBackgroundResource(R.color.quick_gesture_switch_setting_show_color);
            }
            if (mLeftCenterCenterView != null) {
                mLeftCenterCenterView
                        .setBackgroundResource(R.color.quick_gesture_switch_setting_show_color);
            }
            if (mLeftTopView != null) {
                mLeftTopView.setBackgroundResource(R.color.quick_gesture_switch_setting_show_color);
            }
            if (mRightBottomView != null) {
                mRightBottomView
                        .setBackgroundResource(R.color.quick_gesture_switch_setting_show_color);
            }
            if (mRightCenterView != null) {
                mRightCenterView
                        .setBackgroundResource(R.color.quick_gesture_switch_setting_show_color);
            }
            if (mRightCenterCenterView != null) {
                mRightCenterCenterView
                        .setBackgroundResource(R.color.quick_gesture_switch_setting_show_color);
            }
            if (mRightTopView != null) {
                mRightTopView
                        .setBackgroundResource(R.color.quick_gesture_switch_setting_show_color);
            }
        } else if (!flag) {
            if (mLeftBottomView != null) {
                mLeftBottomView
                        .setBackgroundResource(R.color.quick_gesture_switch_setting_hidden_color);
            }
            if (mLeftCenterView != null) {
                mLeftCenterView
                        .setBackgroundResource(R.color.quick_gesture_switch_setting_hidden_color);
            }
            if (mLeftCenterCenterView != null) {
                mLeftCenterCenterView
                        .setBackgroundResource(R.color.quick_gesture_switch_setting_hidden_color);
            }
            if (mLeftTopView != null) {
                mLeftTopView
                        .setBackgroundResource(R.color.quick_gesture_switch_setting_hidden_color);
            }
            if (mRightBottomView != null) {
                mRightBottomView
                        .setBackgroundResource(R.color.quick_gesture_switch_setting_hidden_color);
            }
            if (mRightCenterView != null) {
                mRightCenterView
                        .setBackgroundResource(R.color.quick_gesture_switch_setting_hidden_color);
            }
            if (mRightCenterCenterView != null) {
                mRightCenterCenterView
                        .setBackgroundResource(R.color.quick_gesture_switch_setting_hidden_color);
            }
            if (mRightTopView != null) {
                mRightTopView
                        .setBackgroundResource(R.color.quick_gesture_switch_setting_hidden_color);
            }
        }
    }

    public static void closeQuickGesture(SectorQuickGestureContainer.Orientation orientation) {
        if (orientation == SectorQuickGestureContainer.Orientation.Left) {
            // sLeftPopup.dismiss();
        } else {
            // sRightPopup.dismiss();s
        }
    }

    @SuppressWarnings("deprecation")
    // MIUI系统提示层
    public static void createMiuiTipWindow(final Context mContext) {
        final WindowManager windowManager = getWindowManager(mContext);
        if (mMiuiTipRl == null) {
            mMiuiTipRl = (RelativeLayout) LayoutInflater.from(mContext).inflate(
                    R.layout.activity_miui_open_float_window_tip, null);
            mMiuiTipRl.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (mMiuiTipRl != null) {
                        windowManager.removeView(mMiuiTipRl);
                        mMiuiTipRl = null;
                    }
                }
            });
            if (mMiuiTipParams == null) {
                int width = windowManager.getDefaultDisplay().getWidth();
                int height = windowManager.getDefaultDisplay().getHeight();
                mMiuiTipParams = new LayoutParams();
                mMiuiTipParams.width = width;
                mMiuiTipParams.height = height;
                mMiuiTipParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
                mMiuiTipParams.format = PixelFormat.RGBA_8888;
                mMiuiTipParams.windowAnimations = R.anim.lock_mode_guide_in;
                mMiuiTipParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            }
            windowManager.addView(mMiuiTipRl, mMiuiTipParams);
        }
    }

    private static void onTouchAreaShowQuick(int flag) {
        if (flag == -1) {
            Intent intent;
            intent = new Intent(AppMasterApplication.getInstance(), QuickGesturePopupActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("orientation", 0);
            AppMasterApplication.getInstance().startActivity(intent);
            LockManager.getInstatnce().onTuchGestureFlag = -1;
        } else if (flag == 1) {
            Intent intent;
            intent = new Intent(AppMasterApplication.getInstance(), QuickGesturePopupActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("orientation", 1);
            AppMasterApplication.getInstance().startActivity(intent);
            LockManager.getInstatnce().onTuchGestureFlag = 1;
        }
    }
}
