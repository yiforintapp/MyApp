
package com.leo.appmaster.quickgestures;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.quickgestures.ui.QuickGesturePopupActivity;
import com.leo.appmaster.quickgestures.view.QuickGesturesAreaView;
import com.leo.appmaster.quickgestures.view.SectorQuickGestureContainer;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.Utilities;
import com.leo.appmater.globalbroadcast.LeoGlobalBroadcast;
import com.leo.appmater.globalbroadcast.ScreenOnOffListener;

/**
 * QuickGestureWindowManager
 * 
 * @author run
 */
@SuppressLint("InflateParams")
public class FloatWindowHelper {
    public static final String QUICK_GESTURE_SETTING_DIALOG_LEFT_RADIO_FINISH_NOTIFICATION = "quick_gesture_setting_dialog_left_radio_finish_notification";
    public static final String QUICK_GESTURE_SETTING_DIALOG_RIGHT_RADIO_FINISH_NOTIFICATION = "quick_gesture_setting_dialog_right_radio_finish_notification";
    public static final String QUICK_GESTURE_SETTING_DIALOG_RADIO_SLIDE_TIME_SETTING_FINISH_NOTIFICATION = "quick_gesture_setting_finish_notification";
    public static final String QUICK_GESTURE_LEFT_SLIDE_AREA = "left_slide_area";
    public static final String QUICK_GESTURE_RIGHT_SLIDE_AREA = "right_slide_area";
    public static final int ONTUCH_LEFT_FLAG = -1;
    public static final int ONTUCH_RIGHT_FLAG = 1;
    public static final String QUICK_GESTURE_MSM_TIP = "quick_gesture_msm_tip";
    public static final String QUICK_GESTURE_ADD_FREE_DISTURB_NOTIFICATION = "quick_gesture_add_free_disturb_notification";
    private static QuickGesturesAreaView mLeftBottomView, mLeftCenterView, mLeftTopView,
            mLeftCenterCenterView;
    private static QuickGesturesAreaView mRightBottomView, mRightCenterView, mRightTopView,
            mRightCenterCenterView;
    private static LayoutParams mLeftBottomParams, mLeftCenterParams, mLeftTopParams,
            mLeftCenterCenterParams,mWhiteFloatParams;
    private static LayoutParams mRightBottomParams, mRightCenterParams, mRightTopParams,
            mRightCenterCenterParams;
    private static WindowManager mWindowManager;
    private static ImageView mWhiteFloatView ;
    private static ScreenOnOffListener mScreenListener;
    

    public static boolean mGestureShowing = false;
    public static boolean mEditQuickAreaFlag = false;
    private static float startX;
    private static float startY;
    private static boolean isMoveIng = false;
    public static boolean isFanShowing = false;
    // left bottom width
    private static float mLeftBottomWidth = 20;
    // left bottom height
    private static float mLeftBottomHeight = 15;
    // left center width
    private static float mLeftCenterWidth = 15;
    // left center height
    private static float mLeftCenterHeight = 30;
    // left center center height
    private static float mLeftCenterCenterHeight = 250;
    // left top width
    private static float mLeftTopWidth = 10;
    // left top height
    private static float mLeftTopHeight = 30;

    // right bottom width
    private static float mRightBottomWidth = 20;
    // right bottom height
    private static float mRightBottomHeight = 15;
    // right center width
    private static float mRightCenterWidth = 15;
    // right center height
    private static float mRightCenterHeight = 30;
    // right center center height
    private static float mRightCenterCenterHeight = 250;
    // right top width
    private static float mRightTopWidth = 10;
    // right top height
    private static float mRightTopHeight = 30;
    // white float width and height
    private static int mWhiteFLoatWidth = 60;
    private static final int LEFT_BOTTOM_FLAG = 1;
    private static final int LEFT_CENTER_FLAG = 2;
    private static final int LEFT_TOP_FLAG = 3;
    private static final int LEFT_CENTER_CENTER_FLAG = 4;
    private static final int RIGHT_BOTTOM_FLAG = -1;
    private static final int RIGHT_CENTER_FLAG = -2;
    private static final int RIGHT_CENTER_CENTER_FLAG = -3;
    private static final int RIGHT_TOP_FLAG = -4;
    public static final String RUN_TAG = "RUN_TAG";
    /**
     * left bottom must call in UI thread
     * 
     * @param context
     */
    public static void createFloatLeftBottomWindow(final Context mContext, int value) {
        Log.i("null", "createFloatLeftBottomWindow" );
        final WindowManager windowManager = getWindowManager(mContext);
        final boolean isShowTip = QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage;
        // final boolean isShowBusinessRedTip =
        // QuickGestureManager.getInstance(mContext)
        // .checkBusinessRedTip();
        if (mLeftBottomView == null) {
            mLeftBottomView = new QuickGesturesAreaView(mContext);
            // // no read contact /message/privacycontact red tip
            // if
            // (QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage
            // && (QuickGestureManager.getInstance(mContext).onTuchGestureFlag
            // == -1 || QuickGestureManager
            // .getInstance(mContext).onTuchGestureFlag == -2)) {
            // mLeftBottomView.setIsShowReadTip(true, 1);
            // }
            // // business red tip
            // if (isShowBusinessRedTip
            // && (QuickGestureManager.getInstance(mContext).onTuchGestureFlag
            // == -1 || QuickGestureManager
            // .getInstance(mContext).onTuchGestureFlag == -2)) {
            // mLeftBottomView.setIsShowReadTip(true, 1);
            // }
            mLeftBottomView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    float moveX = 0;
                    float moveY = 0;
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_OUTSIDE:
                            break;
                        case MotionEvent.ACTION_DOWN:
                            isMoveIng = false;
                            startX = event.getRawX();
                            startY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            moveX = Math.abs(startX - event.getRawX());
                            moveY = Math.abs(startY - event.getRawY());
                            if (((moveX > ViewConfiguration.get(mContext).getScaledTouchSlop()
                                    || moveY > ViewConfiguration.get(mContext).getScaledTouchSlop() || event
                                    .getPressure() > 0.55)
                            && !isMoveIng)) {
                                isMoveIng = true;
                                if (!mEditQuickAreaFlag) {
                                    FloatWindowHelper.mGestureShowing = true;
                                    removeAllFloatWindow(mContext);
                                    onTouchAreaShowQuick(-1);
                                    if (isShowTip) {
                                        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "qs_page",
                                                "notice");
                                    } else {
                                        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "qs_page",
                                                "user");
                                    }
                                }
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            // if (Math.abs(startX - event.getRawX()) < 10
                            // || Math.abs(startY - event.getRawY()) < 10) {
                            if ((moveX < ViewConfiguration.get(mContext).getScaledTouchSlop() && moveY < ViewConfiguration
                                    .get(mContext).getScaledTouchSlop())) {
                                // cancel system no read message tip
                                // if (isShowTip || isShowBusinessRedTip) {
                                // AppMasterPreference.getInstance(mContext).setLastTimeLayout(1);
                                // SDKWrapper.addEvent(mContext, SDKWrapper.P1,
                                // "qs_page",
                                // "notice");
                                // Intent intent = new Intent(mContext,
                                // QuickGesturePopupActivity.class);
                                // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                // intent.putExtra("show_orientation", 0);
                                // try {
                                // mContext.startActivity(intent);
                                // //
                                // QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage
                                // // = false;
                                // } catch (Exception e) {
                                // e.printStackTrace();
                                // }
                                // }
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
            int temp = 0;
            if (QuickGestureManager.getInstance(mContext).screenSpace >= 0) {
                temp = QuickGestureManager.getInstance(mContext).screenSpace;
            }
            int height = windowManager.getDefaultDisplay().getHeight() + temp;
            int flag = Utilities.isScreenType(mContext);
            if (mLeftBottomParams == null) {
                mLeftBottomParams = new LayoutParams();
                mLeftBottomParams.width = (int) ((DipPixelUtil.dip2px(mContext, mLeftBottomWidth) / 2) + (value / 2)) * 2;
                mLeftBottomParams.height = (int) ((DipPixelUtil.dip2px(mContext, mLeftBottomHeight) / 2) + (value)) * 2;
                // mLeftBottomParams.x = (int) (-(width / 2) +
                // (mLeftBottomParams.width / 2));
                mLeftBottomParams.x = (int) ((DipPixelUtil.dip2px(mContext, mLeftCenterWidth) / 2) + (value / 2))
                        * 2 + (-(width / 2) + (mLeftBottomParams.width / 2));
                mLeftBottomParams.y = (int) ((height / 2) - (mLeftBottomParams.height / 2));
                mLeftBottomParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
                mLeftBottomParams.format = PixelFormat.RGBA_8888;
                mLeftBottomParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;

            } else {
                // mLeftBottomParams.x = -(width / 2) + (mLeftBottomParams.width
                // / 2);
                // mLeftBottomParams.y = (height / 2) -
                // (mLeftBottomParams.height / 2);
                mLeftBottomParams.x = (int) ((DipPixelUtil.dip2px(mContext, mLeftCenterWidth) / 2) + (value / 2))
                        * 2 + (-(width / 2) + (mLeftBottomParams.width / 2));
                mLeftBottomParams.y = (int) ((height / 2) - (mLeftBottomParams.height / 2));
            }
            if (!mGestureShowing) {
                windowManager.addView(mLeftBottomView, mLeftBottomParams);
            } else {
                mLeftBottomView = null;
            }
        }
    }

    /**
     * left center must call in UI thread
     * 
     * @param context
     */
    public static void createFloatLeftCenterWindow(final Context mContext, int value) {
        Log.i("null", "createFloatLeftCenterWindow" );
        final WindowManager windowManager = getWindowManager(mContext);
        final boolean isShowTip = QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage;
        final boolean isShowBusinessRedTip = QuickGestureManager.getInstance(mContext)
                .checkBusinessRedTip();
        if (mLeftCenterView == null) {
            mLeftCenterView = new QuickGesturesAreaView(mContext);
            // no read contact /message/privacycontact red tip
            if (QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage
                    && (QuickGestureManager.getInstance(mContext).onTuchGestureFlag == -1 || QuickGestureManager
                            .getInstance(mContext).onTuchGestureFlag == -2)) {
                mLeftCenterView.setIsShowReadTip(true, 1);
            }
            // business red tip
            if (isShowBusinessRedTip
                    && (QuickGestureManager.getInstance(mContext).onTuchGestureFlag == -1 || QuickGestureManager
                            .getInstance(mContext).onTuchGestureFlag == -2)) {
                mLeftCenterView.setIsShowReadTip(true, 1);
            }
            mLeftCenterView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    float moveX = 0;
                    float moveY = 0;
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_OUTSIDE:
                            break;
                        case MotionEvent.ACTION_DOWN:
                            isMoveIng = false;
                            startX = event.getRawX();
                            startY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            // Log.e(TAG,"按压力度："+event.getPressure());
                            moveX = Math.abs(startX - event.getRawX());
                            moveY = Math.abs(startY - event.getRawY());
                            float presssure = event.getPressure();
                            // if (((moveX > 10 || moveY > 10)
                            // && !isMoveIng)) {
                            if (((moveX > ViewConfiguration.get(mContext).getScaledTouchSlop()
                                    || moveY > ViewConfiguration.get(mContext).getScaledTouchSlop() || event
                                    .getPressure() > 0.55)
                            && !isMoveIng)) {
                                isMoveIng = true;
                                if (!mEditQuickAreaFlag) {
                                    FloatWindowHelper.mGestureShowing = true;
                                    removeAllFloatWindow(mContext);
                                    onTouchAreaShowQuick(-1);
                                    if (isShowTip) {
                                        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "qs_page",
                                                "notice");
                                    } else {
                                        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "qs_page",
                                                "user");
                                    }
                                }
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            // if (Math.abs(startX - event.getRawX()) < 10
                            // || Math.abs(startY - event.getRawY()) < 10) {
                            if ((moveX < ViewConfiguration.get(mContext).getScaledTouchSlop() && moveY < ViewConfiguration
                                    .get(mContext).getScaledTouchSlop())) {
                                // cancel system no read message tip
                                if (isShowTip || isShowBusinessRedTip) {
                                    AppMasterPreference.getInstance(mContext).setLastTimeLayout(1);
                                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "qs_page",
                                            "notice");
                                    Intent intent = new Intent(mContext,
                                            QuickGesturePopupActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("show_orientation", 0);
                                    try {
                                        mContext.startActivity(intent);
                                        // QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage
                                        // = false;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                removeSwipWindow(mContext, 2);
                            }
                            break;
                    }
                    return false;
                }
            });
            int width = windowManager.getDefaultDisplay().getWidth();
            int temp = 0;
            if (QuickGestureManager.getInstance(mContext).screenSpace >= 0) {
                temp = QuickGestureManager.getInstance(mContext).screenSpace;
            }
            int height = windowManager.getDefaultDisplay().getHeight() + temp;
            if (mLeftCenterParams == null) {
                mLeftCenterParams = new LayoutParams();
                mLeftCenterParams.width = (int) ((DipPixelUtil.dip2px(mContext, mLeftCenterWidth) / 2) + (value / 2)) * 2;
                mLeftCenterParams.height = (int) ((DipPixelUtil.dip2px(mContext, mLeftCenterHeight) / 2) + (value)) * 2;
                mLeftCenterParams.x = (int) (-(width / 2) + (mLeftCenterParams.width / 2));
                // mLeftCenterParams.y = (int) ((height / 2) -
                // (mLeftCenterParams.height / 2)
                // - mLeftBottomParams.height - DipPixelUtil.dip2px(mContext,
                // 12));
                mLeftCenterParams.y = (int) ((height / 2) - (mLeftCenterParams.height / 2));
                mLeftCenterParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
                mLeftCenterParams.format = PixelFormat.RGBA_8888;
                mLeftCenterParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            } else {
                // mLeftCenterParams.x = (int) (-(width / 2) +
                // (mLeftCenterParams.width / 2));
                // mLeftCenterParams.y = (int) ((height / 2) -
                // (mLeftCenterParams.height / 2)
                // - mLeftBottomParams.height - DipPixelUtil.dip2px(mContext,
                // 12));
                mLeftCenterParams.x = (int) (-(width / 2) + (mLeftCenterParams.width / 2));
                mLeftCenterParams.y = (int) ((height / 2) - (mLeftBottomParams.height / 2));
            }

            if (!mGestureShowing) {
                windowManager.addView(mLeftCenterView, mLeftCenterParams);
            } else {
                mLeftCenterView = null;
            }
        }
    }

    /**
     * left center center must call in UI thread
     * 
     * @param context
     */
    public static void createFloatLeftCenterCenterWindow(final Context mContext, int value) {
        
        final WindowManager windowManager = getWindowManager(mContext);
        final boolean isShowTip = QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage;
        final boolean isShowBusinessRedTip = QuickGestureManager.getInstance(mContext)
                .checkBusinessRedTip();
        if (mLeftCenterCenterView == null) {
            mLeftCenterCenterView = new QuickGesturesAreaView(mContext);
            // no read contact/message/privacycontact red tip
            if (QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage
                    && (QuickGestureManager.getInstance(mContext).onTuchGestureFlag == -1 || QuickGestureManager
                            .getInstance(mContext).onTuchGestureFlag == -2)
                    && mLeftBottomView == null) {
                mLeftCenterCenterView.setIsShowReadTip(true, 3);
            }
            // business red tip
            if (isShowBusinessRedTip
                    && (QuickGestureManager.getInstance(mContext).onTuchGestureFlag == -1 || QuickGestureManager
                            .getInstance(mContext).onTuchGestureFlag == -2)
                    && mLeftBottomView == null) {
                mLeftCenterCenterView.setIsShowReadTip(true, 3);
            }
            mLeftCenterCenterView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    float moveX = 0;
                    float moveY = 0;
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_OUTSIDE:
                            break;
                        case MotionEvent.ACTION_DOWN:
                            isMoveIng = false;
                            startX = event.getRawX();
                            startY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            // Log.e(TAG,"按压力度："+event.getPressure());
                            moveX = Math.abs(startX - event.getRawX());
                            moveY = Math.abs(startY - event.getRawY());
                            float presssure = event.getPressure();
                            // if ((moveX > mLeftCenterCenterParams.width / 8
                            // || moveY > mLeftCenterCenterParams.width / 6)
                            // && !isMoveIng) {
                            // if (((moveX > 10 || moveY > 10)
                            // && !isMoveIng)) {
                            if (((moveX > ViewConfiguration.get(mContext).getScaledTouchSlop()
                                    || moveY > ViewConfiguration.get(mContext).getScaledTouchSlop() || event
                                    .getPressure() > 0.55)
                            && !isMoveIng)) {
                                isMoveIng = true;
                                if (!mEditQuickAreaFlag) {
                                    FloatWindowHelper.mGestureShowing = true;
                                    removeAllFloatWindow(mContext);
                                    onTouchAreaShowQuick(-2);
                                    if (isShowTip) {
                                        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "qs_page",
                                                "notice");
                                    } else {
                                        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "qs_page",
                                                "user");
                                    }
                                }
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            // if (Math.abs(startX - event.getRawX()) < 10
                            // || Math.abs(startY - event.getRawY()) < 10) {
                            if ((moveX < ViewConfiguration.get(mContext).getScaledTouchSlop() && moveY < ViewConfiguration
                                    .get(mContext).getScaledTouchSlop())) {
                                // cancel system no read message tip
                                if ((isShowTip || isShowBusinessRedTip) && mLeftBottomView == null) {
                                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "qs_page",
                                            "notice");
                                    AppMasterPreference.getInstance(mContext).setLastTimeLayout(1);
                                    Intent intent = new Intent(mContext,
                                            QuickGesturePopupActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("show_orientation", 0);
                                    try {
                                        mContext.startActivity(intent);
                                        // QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage
                                        // = false;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                removeSwipWindow(mContext, 4);
                            }
                            break;
                    }
                    return false;
                }
            });
            int width = windowManager.getDefaultDisplay().getWidth();
            int temp = 0;
            if (QuickGestureManager.getInstance(mContext).screenSpace >= 0) {
                temp = QuickGestureManager.getInstance(mContext).screenSpace;
            }
            int height = windowManager.getDefaultDisplay().getHeight() + temp;
            int leftBottom = (int) ((DipPixelUtil.dip2px(mContext, mLeftBottomHeight) / 2) + (value)) * 2;
            int leftCenter = (int) ((DipPixelUtil.dip2px(mContext, mLeftCenterHeight) / 2) + (value)) * 2;
            if (mLeftCenterCenterParams == null) {
                mLeftCenterCenterParams = new LayoutParams();
                mLeftCenterCenterParams.width = (int) ((DipPixelUtil.dip2px(mContext,
                        mLeftCenterWidth) / 2) + (value / 2)) * 2;
                mLeftCenterCenterParams.height = (int) ((DipPixelUtil.dip2px(mContext,
                        mLeftCenterCenterHeight) / 2) + (value)) * 2;
                mLeftCenterCenterParams.x = (int) (-(width / 2) + (mLeftCenterCenterParams.width / 2));
                if (mLeftBottomView != null) {
                    // mLeftCenterCenterParams.y = (int) ((height / 2)
                    // - (mLeftCenterCenterParams.height / 2) -
                    // mLeftBottomParams.height
                    // - mLeftCenterParams.height - DipPixelUtil
                    // .dip2px(mContext, 12));
                    mLeftCenterCenterParams.y = (int) ((height / 2)
                            - (mLeftCenterCenterParams.height / 2) - mLeftCenterParams.height - DipPixelUtil
                            .dip2px(mContext, 12));
                } else {
                    mLeftCenterCenterParams.y = (int) ((height / 2)
                            - (mLeftCenterCenterParams.height / 2)
                            - leftBottom - leftCenter - DipPixelUtil
                            .dip2px(mContext, 12));
                }
                mLeftCenterCenterParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
                mLeftCenterCenterParams.format = PixelFormat.RGBA_8888;
                mLeftCenterCenterParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            } else {
                mLeftCenterCenterParams.x = (int) (-(width / 2) + (mLeftCenterCenterParams.width / 2));
                if (mLeftBottomView != null) {
                    // mLeftCenterCenterParams.y = (int) ((height / 2)
                    // - (mLeftCenterCenterParams.height / 2) -
                    // mLeftBottomParams.height
                    // - mLeftCenterParams.height - DipPixelUtil
                    // .dip2px(mContext, 12));
                    mLeftCenterCenterParams.y = (int) ((height / 2)
                            - (mLeftCenterCenterParams.height / 2) - mLeftCenterParams.height - DipPixelUtil
                            .dip2px(mContext, 12));
                } else {
                    mLeftCenterCenterParams.y = (int) ((height / 2)
                            - (mLeftCenterCenterParams.height / 2) - leftBottom - leftCenter - DipPixelUtil
                            .dip2px(mContext, 12));
                }
            }
            if (!mGestureShowing) {
                windowManager.addView(mLeftCenterCenterView, mLeftCenterCenterParams);
            } else {
                mLeftCenterCenterView = null;
            }
        }
    }

    /**
     * left top must call in UI thread
     * 
     * @param context
     */
    public static void createFloatLeftTopWindow(final Context mContext, int value) {
        Log.i("null", "createFloatLeftTopWindow" );
        final WindowManager windowManager = getWindowManager(mContext);
        final boolean isShowTip = QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage;
        if (mLeftTopView == null) {
            mLeftTopView = new QuickGesturesAreaView(mContext);
            mLeftTopView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    float moveX = 0;
                    float moveY = 0;

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_OUTSIDE:
                            break;
                        case MotionEvent.ACTION_DOWN:
                            isMoveIng = false;
                            startX = event.getRawX();
                            startY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            // Log.e(TAG,"按压力度："+event.getPressure());
                            moveX = Math.abs(startX - event.getRawX());
                            moveY = Math.abs(startY - event.getRawY());
                            float presssure = event.getPressure();
                            // if (((moveX > 10 || moveY > 10)
                            // && !isMoveIng)) {
                            if (((moveX > ViewConfiguration.get(mContext).getScaledTouchSlop()
                                    || moveY > ViewConfiguration.get(mContext).getScaledTouchSlop() || event
                                    .getPressure() > 0.55)
                            && !isMoveIng)) {
                                isMoveIng = true;
                                if (!mEditQuickAreaFlag) {
                                    FloatWindowHelper.mGestureShowing = true;
                                    removeAllFloatWindow(mContext);
                                    onTouchAreaShowQuick(-1);
                                    if (isShowTip) {
                                        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "qs_page",
                                                "notice");
                                    } else {
                                        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "qs_page",
                                                "user");
                                    }
                                }
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            // if (Math.abs(startX - event.getRawX()) < 10
                            // || Math.abs(startY - event.getRawY()) < 10) {
                            if ((moveX < ViewConfiguration.get(mContext).getScaledTouchSlop() && moveY < ViewConfiguration
                                    .get(mContext).getScaledTouchSlop())) {
                                removeSwipWindow(mContext, 3);
                            }
                            break;
                    }
                    return false;
                }
            });
            int width = windowManager.getDefaultDisplay().getWidth();
            int temp = 0;
            if (QuickGestureManager.getInstance(mContext).screenSpace >= 0) {
                temp = QuickGestureManager.getInstance(mContext).screenSpace;
            }
            int height = windowManager.getDefaultDisplay().getHeight() + temp;
            if (mLeftTopParams == null) {
                mLeftTopParams = new LayoutParams();
                mLeftTopParams.width = (int) ((DipPixelUtil.dip2px(mContext, mLeftTopWidth) / 2) + (value / 2)) * 2;
                mLeftTopParams.height = (int) ((DipPixelUtil.dip2px(mContext, mLeftTopHeight) / 2) + (value)) * 2;
                mLeftTopParams.x = (int) (-(width / 2) + (mLeftTopParams.width / 2));
                // mLeftTopParams.y = (int) ((height / 2) -
                // (mLeftTopParams.height / 2)
                // - mLeftBottomParams.height - mLeftCenterParams.height -
                // DipPixelUtil
                // .dip2px(mContext, 12));
                mLeftTopParams.y = (int) ((height / 2) - (mLeftTopParams.height / 2)
                        - mLeftCenterParams.height - DipPixelUtil
                        .dip2px(mContext, 12));

                mLeftTopParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
                mLeftTopParams.format = PixelFormat.RGBA_8888;
                mLeftTopParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            } else {
                mLeftTopParams.x = (int) (-(width / 2) + (mLeftTopParams.width / 2));
                // mLeftTopParams.y = (int) ((height / 2) -
                // (mLeftTopParams.height / 2)
                // - mLeftBottomParams.height - mLeftCenterParams.height -
                // DipPixelUtil
                // .dip2px(mContext, 12));
                mLeftTopParams.y = (int) ((height / 2) - (mLeftTopParams.height / 2)
                        - mLeftCenterParams.height - DipPixelUtil
                        .dip2px(mContext, 12));
            }

            if (!mGestureShowing) {
                windowManager.addView(mLeftTopView, mLeftTopParams);
            } else {
                mLeftTopView = null;
            }
        }
    }

    /**
     * right bottom must call in UI thread
     * 
     * @param context
     */
    public static void createFloatRightBottomWindow(final Context mContext, int value) {
        final WindowManager windowManager = getWindowManager(mContext);
        final boolean isShowTip = QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage;
        // final boolean isShowBusinessRedTip =
        // QuickGestureManager.getInstance(mContext)
        // .checkBusinessRedTip();
        if (mRightBottomView == null) {
            mRightBottomView = new QuickGesturesAreaView(mContext);

            // // no read contact/message/privacycontact red tip
            // if
            // (QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage
            // && (QuickGestureManager.getInstance(mContext).onTuchGestureFlag
            // == 1 || QuickGestureManager
            // .getInstance(mContext).onTuchGestureFlag == 2)) {
            // mRightBottomView.setIsShowReadTip(true, 2);
            // }
            // // business red tip
            // if (isShowBusinessRedTip
            // && (QuickGestureManager.getInstance(mContext).onTuchGestureFlag
            // == 1 || QuickGestureManager
            // .getInstance(mContext).onTuchGestureFlag == 2)) {
            // mRightBottomView.setIsShowReadTip(true, 2);
            // }
            mRightBottomView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    float moveX = 0;
                    float moveY = 0;
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_OUTSIDE:
                            break;
                        case MotionEvent.ACTION_DOWN:
                            isMoveIng = false;
                            startX = event.getRawX();
                            startY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            // Log.e(TAG,"按压力度："+event.getPressure());
                            moveX = Math.abs(startX - event.getRawX());
                            moveY = Math.abs(startY - event.getRawY());
                            float presssure = event.getPressure();
                            // if (((moveX > 10 || moveY > 10)
                            // && !isMoveIng)) {
                            if (((moveX > ViewConfiguration.get(mContext).getScaledTouchSlop()
                                    || moveY > ViewConfiguration.get(mContext).getScaledTouchSlop() || event
                                    .getPressure() > 0.55)
                            && !isMoveIng)) {
                                isMoveIng = true;
                                if (!mEditQuickAreaFlag) {
                                    FloatWindowHelper.mGestureShowing = true;
                                    removeAllFloatWindow(mContext);
                                    onTouchAreaShowQuick(1);
                                    if (isShowTip) {
                                        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "qs_page",
                                                "notice");
                                    } else {
                                        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "qs_page",
                                                "user");
                                    }
                                }
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            // if (Math.abs(startX - event.getRawX()) < 10
                            // || Math.abs(startY - event.getRawY()) < 10) {
                            if ((moveX < ViewConfiguration.get(mContext).getScaledTouchSlop() && moveY < ViewConfiguration
                                    .get(mContext).getScaledTouchSlop())) {
                                // cancel system no read message tip
                                // if (isShowTip || isShowBusinessRedTip) {
                                // SDKWrapper.addEvent(mContext, SDKWrapper.P1,
                                // "qs_page",
                                // "notice");
                                // AppMasterPreference.getInstance(mContext).setLastTimeLayout(1);
                                // Intent intent = new Intent(mContext,
                                // QuickGesturePopupActivity.class);
                                // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                // intent.putExtra("show_orientation", 2);
                                // try {
                                // mContext.startActivity(intent);
                                // //
                                // QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage
                                // // = false;
                                // } catch (Exception e) {
                                // e.printStackTrace();
                                // }
                                // }
                                removeSwipWindow(mContext, -1);
                            }
                            break;
                    }
                    return false;
                }
            });
            int width = windowManager.getDefaultDisplay().getWidth();
            int temp = 0;
            if (QuickGestureManager.getInstance(mContext).screenSpace >= 0) {
                temp = QuickGestureManager.getInstance(mContext).screenSpace;
            }
            int height = windowManager.getDefaultDisplay().getHeight() + temp;
            if (mRightBottomParams == null) {
                mRightBottomParams = new LayoutParams();
                mRightBottomParams.width = (int) ((DipPixelUtil.dip2px(mContext, mRightBottomWidth) / 2) + (value / 2)) * 2;
                mRightBottomParams.height = (int) ((DipPixelUtil.dip2px(mContext,
                        mRightBottomHeight) / 2) + (value)) * 2;
                mRightBottomParams.x = (int) ((width / 2) - (mRightBottomParams.width / 2))
                        - ((DipPixelUtil.dip2px(mContext, mRightCenterWidth) / 2) + (value / 2))
                        * 2;
                mRightBottomParams.y = (int) ((height / 2) - (mRightBottomParams.height / 2));
                mRightBottomParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
                mRightBottomParams.format = PixelFormat.RGBA_8888;
                mRightBottomParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            } else {
                // mRightBottomParams.x = (int) ((width / 2) +
                // (mRightBottomParams.width / 2));
                mRightBottomParams.x = (int) ((width / 2) - (mRightBottomParams.width / 2))
                        - ((DipPixelUtil.dip2px(mContext, mRightCenterWidth) / 2) + (value / 2))
                        * 2;
                mRightBottomParams.y = (int) ((height / 2) - (mRightBottomParams.height / 2));
            }

            if (!mGestureShowing) {
                windowManager.addView(mRightBottomView, mRightBottomParams);
            } else {
                mRightBottomView = null;
            }
        }
    }

    /**
     * right center must call in UI thread
     * 
     * @param context
     */
    public static void createFloatRightCenterWindow(final Context mContext, int value) {
        final WindowManager windowManager = getWindowManager(mContext);
        final boolean isShowTip = QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage;
        final boolean isShowBusinessRedTip = QuickGestureManager.getInstance(mContext)
                .checkBusinessRedTip();
        if (mRightCenterView == null) {
            mRightCenterView = new QuickGesturesAreaView(mContext);
            // no read contact/message/privacycontact red tip
            if (QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage
                    && (QuickGestureManager.getInstance(mContext).onTuchGestureFlag == 1 || QuickGestureManager
                            .getInstance(mContext).onTuchGestureFlag == 2)) {
                mRightCenterView.setIsShowReadTip(true, 2);
            }
            // business red tip
            if (isShowBusinessRedTip
                    && (QuickGestureManager.getInstance(mContext).onTuchGestureFlag == 1 || QuickGestureManager
                            .getInstance(mContext).onTuchGestureFlag == 2)) {
                mRightCenterView.setIsShowReadTip(true, 2);
            }
            mRightCenterView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    float moveX = 0;
                    float moveY = 0;
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_OUTSIDE:
                            break;
                        case MotionEvent.ACTION_DOWN:
                            isMoveIng = false;
                            startX = event.getRawX();
                            startY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            // Log.e(TAG,"按压力度："+event.getPressure());
                            moveX = Math.abs(startX - event.getRawX());
                            moveY = Math.abs(startY - event.getRawY());
                            float presssure = event.getPressure();
                            // if (((moveX > 10 || moveY > 10)
                            // && !isMoveIng)) {
                            if (((moveX > ViewConfiguration.get(mContext).getScaledTouchSlop()
                                    || moveY > ViewConfiguration.get(mContext).getScaledTouchSlop() || event
                                    .getPressure() > 0.55)
                            && !isMoveIng)) {
                                isMoveIng = true;
                                if (!mEditQuickAreaFlag) {
                                    FloatWindowHelper.mGestureShowing = true;
                                    removeAllFloatWindow(mContext);
                                    onTouchAreaShowQuick(1);
                                    if (isShowTip) {
                                        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "qs_page",
                                                "notice");
                                    } else {
                                        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "qs_page",
                                                "user");
                                    }
                                }
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            // if (Math.abs(startX - event.getRawX()) < 10
                            // || Math.abs(startY - event.getRawY()) < 10) {
                            if ((moveX < ViewConfiguration.get(mContext).getScaledTouchSlop() && moveY < ViewConfiguration
                                    .get(mContext).getScaledTouchSlop())) {
                                // cancel system no read message tip
                                if (isShowTip || isShowBusinessRedTip) {
                                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "qs_page",
                                            "notice");
                                    AppMasterPreference.getInstance(mContext).setLastTimeLayout(1);
                                    Intent intent = new Intent(mContext,
                                            QuickGesturePopupActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("show_orientation", 2);
                                    try {
                                        mContext.startActivity(intent);
                                        // QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage
                                        // = false;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                removeSwipWindow(mContext, -2);
                            }
                            break;
                    }
                    return false;
                }
            });
            int width = windowManager.getDefaultDisplay().getWidth();
            int temp = 0;
            if (QuickGestureManager.getInstance(mContext).screenSpace >= 0) {
                temp = QuickGestureManager.getInstance(mContext).screenSpace;
            }
            int height = windowManager.getDefaultDisplay().getHeight() + temp;
            if (mRightCenterParams == null) {
                mRightCenterParams = new LayoutParams();
                mRightCenterParams.width = (int) ((DipPixelUtil.dip2px(mContext, mRightCenterWidth) / 2) + (value / 2)) * 2;
                mRightCenterParams.height = (int) ((DipPixelUtil.dip2px(mContext,
                        mRightCenterHeight) / 2) + (value)) * 2;
                mRightCenterParams.x = (int) ((width / 2) + (mRightCenterParams.width / 2));
                mRightCenterParams.y = (int) ((height / 2) - (mRightCenterParams.height / 2));
                // mRightCenterParams.y = (int) ((height / 2) -
                // (mRightCenterParams.height / 2)
                // - mRightBottomParams.height - DipPixelUtil.dip2px(mContext,
                // 12));
                mRightCenterParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
                mRightCenterParams.format = PixelFormat.RGBA_8888;
                mRightCenterParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            } else {
                // mRightCenterParams.x = (int) ((width / 2) +
                // (mRightCenterParams.width / 2));
                // mRightCenterParams.y = (int) ((height / 2) -
                // (mRightCenterParams.height / 2)
                // - mRightBottomParams.height - DipPixelUtil.dip2px(mContext,
                // 12));
                mRightCenterParams.x = (int) ((width / 2) + (mRightCenterParams.width / 2));
                mRightCenterParams.y = (int) ((height / 2) - (mRightCenterParams.height / 2));
            }

            if (!mGestureShowing) {
                windowManager.addView(mRightCenterView, mRightCenterParams);
            } else {
                mRightCenterView = null;
            }
        }
    }

    /**
     * right center center must call in UI thread
     * 
     * @param context
     */
    public static void createFloatRightCenterCenterWindow(final Context mContext, int value) {
        final WindowManager windowManager = getWindowManager(mContext);
        final boolean isShowTip = QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage;
        final boolean isShowBusinessRedTip = QuickGestureManager.getInstance(mContext)
                .checkBusinessRedTip();
        if (mRightCenterCenterView == null) {
            mRightCenterCenterView = new QuickGesturesAreaView(mContext);
            // no read contact/message/privacycontact red tip
            if (QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage
                    && (QuickGestureManager.getInstance(mContext).onTuchGestureFlag == 1 || QuickGestureManager
                            .getInstance(mContext).onTuchGestureFlag == 2)
                    && mRightBottomView == null) {
                mRightCenterCenterView.setIsShowReadTip(true, 4);
            }
            // business red tip
            if (isShowBusinessRedTip
                    && (QuickGestureManager.getInstance(mContext).onTuchGestureFlag == 1 || QuickGestureManager
                            .getInstance(mContext).onTuchGestureFlag == 2)
                    && mRightBottomView == null) {
                mRightCenterCenterView.setIsShowReadTip(true, 4);
            }

            mRightCenterCenterView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    float moveX = 0;
                    float moveY = 0;
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_OUTSIDE:
                            break;
                        case MotionEvent.ACTION_DOWN:
                            isMoveIng = false;
                            startX = event.getRawX();
                            startY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            // Log.e(TAG,"按压力度："+event.getPressure());
                            moveX = Math.abs(startX - event.getRawX());
                            moveY = Math.abs(startY - event.getRawY());
                            float presssure = event.getPressure();
                            // if (((moveX > 10 || moveY > 10)
                            // && !isMoveIng)) {
                            if (((moveX > ViewConfiguration.get(mContext).getScaledTouchSlop()
                                    || moveY > ViewConfiguration.get(mContext).getScaledTouchSlop() || event
                                    .getPressure() > 0.55)
                            && !isMoveIng)) {
                                isMoveIng = true;
                                if (!mEditQuickAreaFlag) {
                                    FloatWindowHelper.mGestureShowing = true;
                                    removeAllFloatWindow(mContext);
                                    onTouchAreaShowQuick(2);
                                    if (isShowTip) {
                                        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "qs_page",
                                                "notice");
                                    } else {
                                        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "qs_page",
                                                "user");
                                    }
                                }
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            // if (Math.abs(startX - event.getRawX()) < 10
                            // || Math.abs(startY - event.getRawY()) < 10) {
                            if ((moveX < ViewConfiguration.get(mContext).getScaledTouchSlop() && moveY < ViewConfiguration
                                    .get(mContext).getScaledTouchSlop())) {
                                // cancel system no read message tip
                                if ((isShowTip || isShowBusinessRedTip) && mRightBottomView == null) {
                                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "qs_page",
                                            "notice");
                                    AppMasterPreference.getInstance(mContext).setLastTimeLayout(1);
                                    Intent intent = new Intent(mContext,
                                            QuickGesturePopupActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("show_orientation", 2);
                                    try {
                                        mContext.startActivity(intent);
                                        // QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage
                                        // = false;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                removeSwipWindow(mContext, -4);
                            }
                            break;
                    }
                    return false;
                }
            });
            int width = windowManager.getDefaultDisplay().getWidth();
            int temp = 0;
            if (QuickGestureManager.getInstance(mContext).screenSpace >= 0) {
                temp = QuickGestureManager.getInstance(mContext).screenSpace;
            }
            int height = windowManager.getDefaultDisplay().getHeight() + temp;
            int rightBottom = (int) ((DipPixelUtil.dip2px(mContext,
                    mRightCenterHeight) / 2) + (value)) * 2;
            int rightCenter = (int) ((DipPixelUtil.dip2px(mContext,
                    mRightBottomHeight) / 2) + (value)) * 2;
            if (mRightCenterCenterParams == null) {
                mRightCenterCenterParams = new LayoutParams();
                mRightCenterCenterParams.width = (int) ((DipPixelUtil.dip2px(mContext,
                        mRightCenterWidth) / 2) + (value / 2)) * 2;
                mRightCenterCenterParams.height = (int) ((DipPixelUtil.dip2px(mContext,
                        mRightCenterCenterHeight) / 2) + (value)) * 2;
                mRightCenterCenterParams.x = (int) ((width / 2) + (mRightCenterCenterParams.width / 2));
                if (mRightBottomView != null) {
                    // mRightCenterCenterParams.y = (int) ((height / 2)
                    // - (mRightCenterCenterParams.height / 2) -
                    // mRightBottomParams.height
                    // - mRightCenterParams.height - DipPixelUtil
                    // .dip2px(mContext, 12));
                    mRightCenterCenterParams.y = (int) ((height / 2)
                            - (mRightCenterCenterParams.height / 2) - mRightCenterParams.height - DipPixelUtil
                            .dip2px(mContext, 12));
                } else {
                    mRightCenterCenterParams.y = (int) ((height / 2)
                            - (mRightCenterCenterParams.height / 2) - rightBottom
                            - rightCenter - DipPixelUtil
                            .dip2px(mContext, 12));
                }
                mRightCenterCenterParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
                mRightCenterCenterParams.format = PixelFormat.RGBA_8888;
                mRightCenterCenterParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            } else {
                if (mRightBottomView != null) {
                    // mRightCenterCenterParams.y = (int) ((height / 2)
                    // - (mRightCenterCenterParams.height / 2) -
                    // mRightBottomParams.height
                    // - mRightCenterParams.height - DipPixelUtil
                    // .dip2px(mContext, 12));
                    mRightCenterCenterParams.y = (int) ((height / 2)
                            - (mRightCenterCenterParams.height / 2) - mRightCenterParams.height - DipPixelUtil
                            .dip2px(mContext, 12));
                } else {
                    mRightCenterCenterParams.y = (int) ((height / 2)
                            - (mRightCenterCenterParams.height / 2) - rightBottom
                            - rightCenter - DipPixelUtil
                            .dip2px(mContext, 12));
                }

            }

            if (!mGestureShowing) {
                windowManager.addView(mRightCenterCenterView, mRightCenterCenterParams);
            } else {
                mRightCenterCenterView = null;
            }
        }
    }

    /**
     * right top must call in UI thread
     * 
     * @param context
     */
    public static void createFloatRightTopWindow(final Context mContext, int value) {
        final WindowManager windowManager = getWindowManager(mContext);
        final boolean isShowTip = QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage;
        if (mRightTopView == null) {
            mRightTopView = new QuickGesturesAreaView(mContext);
            mRightTopView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    float moveX = 0;
                    float moveY = 0;
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_OUTSIDE:
                            break;
                        case MotionEvent.ACTION_DOWN:
                            isMoveIng = false;
                            startX = event.getRawX();
                            startY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            // Log.e(TAG,"按压力度："+event.getPressure());
                            moveX = Math.abs(startX - event.getRawX());
                            moveY = Math.abs(startY - event.getRawY());
                            float presssure = event.getPressure();
                            // if (((moveX > 10 || moveY > 10)
                            // && !isMoveIng)) {
                            if (((moveX > ViewConfiguration.get(mContext).getScaledTouchSlop()
                                    || moveY > ViewConfiguration.get(mContext).getScaledTouchSlop() || event
                                    .getPressure() > 0.55)
                            && !isMoveIng)) {
                                isMoveIng = true;
                                if (!mEditQuickAreaFlag) {
                                    FloatWindowHelper.mGestureShowing = true;
                                    removeAllFloatWindow(mContext);
                                    onTouchAreaShowQuick(1);
                                    if (isShowTip) {
                                        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "qs_page",
                                                "notice");
                                    } else {
                                        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "qs_page",
                                                "user");
                                    }
                                }
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            // if (Math.abs(startX - event.getRawX()) < 10
                            // || Math.abs(startY - event.getRawY()) < 10) {
                            if ((moveX < ViewConfiguration.get(mContext).getScaledTouchSlop() && moveY < ViewConfiguration
                                    .get(mContext).getScaledTouchSlop())) {
                                removeSwipWindow(mContext, -3);
                            }
                            break;
                    }
                    return false;
                }
            });
            int width = windowManager.getDefaultDisplay().getWidth();
            int temp = 0;
            if (QuickGestureManager.getInstance(mContext).screenSpace >= 0) {
                temp = QuickGestureManager.getInstance(mContext).screenSpace;
            }
            int height = windowManager.getDefaultDisplay().getHeight() + temp;
            if (mRightTopParams == null) {
                mRightTopParams = new LayoutParams();
                mRightTopParams.width = (int) ((DipPixelUtil.dip2px(mContext, mRightTopWidth) / 2) + (value / 2)) * 2;
                mRightTopParams.height = (int) ((DipPixelUtil.dip2px(mContext, mRightTopHeight) / 2) + (value)) * 2;
                mRightTopParams.x = (int) ((width / 2) + (mRightTopParams.width / 2));
                mRightTopParams.y = (int) ((height / 2) - (mRightTopParams.height / 2)
                        - mRightCenterParams.height - DipPixelUtil.dip2px(mContext, 12));
                // mRightTopParams.y = (int) ((height / 2) -
                // (mRightTopParams.height / 2)
                // - mRightBottomParams.height - mRightCenterParams.height -
                // DipPixelUtil
                // .dip2px(mContext, 12));
                mRightTopParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
                mRightTopParams.format = PixelFormat.RGBA_8888;
                mRightTopParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            } else {
                mRightTopParams.x = (int) ((width / 2) + (mRightTopParams.width / 2));
                // mRightTopParams.y = (int) ((height / 2) -
                // (mRightTopParams.height / 2)
                // - mRightBottomParams.height - mRightCenterParams.height -
                // DipPixelUtil
                // .dip2px(mContext, 12));
                mRightTopParams.y = (int) ((height / 2) - (mRightTopParams.height / 2)
                        - mRightCenterParams.height - DipPixelUtil.dip2px(mContext, 12));
            }

            if (!mGestureShowing) {
                windowManager.addView(mRightTopView, mRightTopParams);
            } else {
                mRightTopView = null;
            }
        }
    }

    /**
     * remove float window must call in UI thread
     * 
     * @param context
     */
    public static void removeSwipWindow(Context context, int flag) {
        WindowManager windowManager = getWindowManager(context);
        if (LEFT_BOTTOM_FLAG == flag) {
            // left bottom
            if (mLeftBottomView != null) {
                windowManager.removeView(mLeftBottomView);
                mLeftBottomView = null;
            }
        } else if (LEFT_CENTER_FLAG == flag) {
            // left center
            if (mLeftCenterView != null) {
                windowManager.removeView(mLeftCenterView);
                mLeftCenterView = null;
            }
        } else if (LEFT_TOP_FLAG == flag) {
            // left top
            if (mLeftTopView != null) {
                windowManager.removeView(mLeftTopView);
                mLeftTopView = null;
            }
        } else if (LEFT_CENTER_CENTER_FLAG == flag) {
            // left center center
            if (mLeftCenterCenterView != null) {
                windowManager.removeView(mLeftCenterCenterView);
                mLeftCenterCenterView = null;
            }
        } else if (RIGHT_BOTTOM_FLAG == flag) {
            // right bottom
            if (mRightBottomView != null) {
                windowManager.removeView(mRightBottomView);
                mRightBottomView = null;
            }
        } else if (RIGHT_CENTER_FLAG == flag) {
            // right center
            if (mRightCenterView != null) {
                windowManager.removeView(mRightCenterView);
                mRightCenterView = null;
            }
        } else if (RIGHT_CENTER_CENTER_FLAG == flag) {
            // right top
            if (mRightTopView != null) {
                windowManager.removeView(mRightTopView);
                mRightTopView = null;
            }
        } else if (RIGHT_TOP_FLAG == flag) {
            // right center center
            if (mRightCenterCenterView != null) {
                windowManager.removeView(mRightCenterCenterView);
                mRightCenterCenterView = null;
            }
        }
    }

    // remove all float window
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

    // remove have red tip float window
    public static void removeShowReadTipWindow(Context context) {
        removeSwipWindow(context, 2);
        removeSwipWindow(context, 4);
        removeSwipWindow(context, -2);
        removeSwipWindow(context, -4);
    }

    /**
     * update view LayoutParams must call in UI thread
     * 
     * @param context
     */
    public static void updateView(Context context, int value) {
        WindowManager windowManager = getWindowManager(context);
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        int temp = 0;
        if (QuickGestureManager.getInstance(context).screenSpace >= 0) {
            temp = QuickGestureManager.getInstance(context).screenSpace;
        }
        int height = windowManager.getDefaultDisplay().getHeight() + temp;
        int width = display.getWidth();
        int leftBottom = (int) ((DipPixelUtil.dip2px(context, mLeftBottomHeight) / 2) + (value)) * 2;
        int leftCenter = (int) ((DipPixelUtil.dip2px(context, mLeftCenterHeight) / 2) + (value)) * 2;
        int rightBottom = (int) ((DipPixelUtil.dip2px(context,
                mRightCenterHeight) / 2) + (value)) * 2;
        int rightCenter = (int) ((DipPixelUtil.dip2px(context,
                mRightBottomHeight) / 2) + (value)) * 2;
        // left bottom
        if (mLeftBottomParams != null) {
            mLeftBottomParams.width = (int) ((DipPixelUtil.dip2px(context, mLeftBottomWidth) / 2) + (value / 2)) * 2;
            mLeftBottomParams.height = (int) ((DipPixelUtil.dip2px(context, mLeftBottomHeight) / 2) + (value)) * 2;
            // mLeftBottomParams.x = (int) (-(width / 2) +
            // (mLeftBottomParams.width / 2));
            // mLeftBottomParams.y = (int) ((height / 2) -
            // (mLeftBottomParams.height / 2));
            mLeftBottomParams.x = (int) ((DipPixelUtil.dip2px(context, mLeftCenterWidth) / 2) + (value / 2))
                    * 2 + (-(width / 2) + (mLeftBottomParams.width / 2));
            mLeftBottomParams.y = (int) ((height / 2) - (mLeftBottomParams.height / 2));
        }
        // left center
        if (mLeftCenterParams != null) {
            mLeftCenterParams.width = (int) ((DipPixelUtil.dip2px(context, mLeftCenterWidth) / 2) + (value / 2)) * 2;
            mLeftCenterParams.height = (int) ((DipPixelUtil.dip2px(context, mLeftCenterHeight) / 2) + (value)) * 2;
            // mLeftCenterParams.x = (int) (-(width / 2) +
            // (mLeftCenterParams.width / 2));
            // mLeftCenterParams.y = (int) ((height / 2) -
            // (mLeftCenterParams.height / 2)
            // - mLeftBottomParams.height - DipPixelUtil.dip2px(context, 12));
            mLeftCenterParams.x = (int) (-(width / 2) + (mLeftCenterParams.width / 2));
            mLeftCenterParams.y = (int) ((height / 2) - (mLeftBottomParams.height / 2));
        }
        // left center center
        if (mLeftCenterCenterParams != null) {
            mLeftCenterCenterParams.width = (int) ((DipPixelUtil.dip2px(context, mLeftCenterWidth) / 2) + (value / 2)) * 2;
            mLeftCenterCenterParams.height = (int) ((DipPixelUtil.dip2px(context,
                    mLeftCenterCenterHeight) / 2) + (value)) * 2;
            mLeftCenterCenterParams.x = (int) (-(width / 2) + (mLeftCenterCenterParams.width / 2));
            if (mLeftBottomView != null) {
                // mLeftCenterCenterParams.y = (int) ((height / 2)
                // - (mLeftCenterCenterParams.height / 2) -
                // mLeftBottomParams.height
                // - mLeftCenterParams.height - DipPixelUtil
                // .dip2px(context, 12));
                mLeftCenterCenterParams.y = (int) ((height / 2)
                        - (mLeftCenterCenterParams.height / 2) - mLeftCenterParams.height - DipPixelUtil
                        .dip2px(context, 12));
            } else {
                mLeftCenterCenterParams.y = (int) ((height / 2)
                        - (mLeftCenterCenterParams.height / 2) - leftBottom - leftCenter - DipPixelUtil
                        .dip2px(context, 12));
            }
        }
        // left top
        if (mLeftTopParams != null) {
            mLeftTopParams.width = (int) ((DipPixelUtil.dip2px(context, mLeftTopWidth) / 2) + (value / 2)) * 2;
            mLeftTopParams.height = (int) ((DipPixelUtil.dip2px(context, mLeftTopHeight) / 2) + (value)) * 2;
            mLeftTopParams.x = (int) (-(width / 2) + (mLeftTopParams.width / 2));
            // mLeftTopParams.y = (int) ((height / 2) - (mLeftTopParams.height /
            // 2)
            // - mLeftBottomParams.height - mLeftCenterParams.height -
            // DipPixelUtil.dip2px(
            // context, 12));
            mLeftTopParams.y = (int) ((height / 2) - (mLeftTopParams.height / 2)
                    - mLeftCenterParams.height - DipPixelUtil
                    .dip2px(context, 12));
        }
        // right bottom
        if (mRightBottomParams != null) {
            mRightBottomParams.width = (int) ((DipPixelUtil.dip2px(context, mRightBottomWidth) / 2) + (value / 2)) * 2;
            mRightBottomParams.height = (int) ((DipPixelUtil.dip2px(context, mRightBottomHeight) / 2) + (value)) * 2;
            // mRightBottomParams.x = +(width / 2);
            // mRightBottomParams.y = (height / 2) - value;
            mRightBottomParams.x = (int) ((width / 2) - (mRightBottomParams.width / 2))
                    - ((DipPixelUtil.dip2px(context, mRightCenterWidth) / 2) + (value / 2)) * 2;
            mRightBottomParams.y = (int) ((height / 2) - (mRightBottomParams.height / 2));
        }
        // right center
        if (mRightCenterParams != null) {
            mRightCenterParams.width = (int) ((DipPixelUtil.dip2px(context, mRightCenterWidth) / 2) + (value / 2)) * 2;
            mRightCenterParams.height = (int) ((DipPixelUtil.dip2px(context, mRightCenterHeight) / 2) + (value)) * 2;
            // mRightCenterParams.x = (int) ((width / 2) +
            // (mRightCenterParams.width / 2));
            // mRightCenterParams.y = (int) ((height / 2) -
            // (mRightCenterParams.height / 2)
            // - mRightBottomParams.height - DipPixelUtil.dip2px(context, 12));
            mRightCenterParams.x = (int) ((width / 2) + (mRightCenterParams.width / 2));
            mRightCenterParams.y = (int) ((height / 2) - (mRightCenterParams.height / 2));
        }
        // right top
        if (mRightTopParams != null) {
            mRightTopParams.width = (int) ((DipPixelUtil.dip2px(context, mRightTopWidth) / 2) + (value / 2)) * 2;
            mRightTopParams.height = (int) ((DipPixelUtil.dip2px(context, mRightTopHeight) / 2) + (value)) * 2;
            mRightTopParams.x = (int) ((width / 2) + (mRightTopParams.width / 2));
            // mRightTopParams.y = (int) ((height / 2) - (mRightTopParams.height
            // / 2)
            // - mRightBottomParams.height - mRightCenterParams.height -
            // DipPixelUtil.dip2px(
            // context, 12));
            mRightTopParams.y = (int) ((height / 2) - (mRightTopParams.height / 2)
                    - mRightCenterParams.height - DipPixelUtil.dip2px(context, 12));
        }
        // right center center
        if (mRightCenterCenterParams != null) {
            mRightCenterCenterParams.width = (int) ((DipPixelUtil
                    .dip2px(context, mRightCenterWidth) / 2) + (value / 2)) * 2;
            mRightCenterCenterParams.height = (int) ((DipPixelUtil.dip2px(context,
                    mRightCenterCenterHeight) / 2) + (value)) * 2;
            if (mRightBottomView != null) {
                // mRightCenterCenterParams.y = (int) ((height / 2)
                // - (mRightCenterCenterParams.height / 2) -
                // mRightBottomParams.height
                // - mRightCenterParams.height - DipPixelUtil
                // .dip2px(context, 12));
                mRightCenterCenterParams.y = (int) ((height / 2)
                        - (mRightCenterCenterParams.height / 2) - mRightCenterParams.height - DipPixelUtil
                        .dip2px(context, 12));
            } else {
                mRightCenterCenterParams.y = (int) ((height / 2)
                        - (mRightCenterCenterParams.height / 2) - rightBottom
                        - rightCenter - DipPixelUtil.dip2px(
                        context, 12));
            }

        }
        // update left
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
        // update right
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

    private static void createFloatArea(Context context, int value, boolean leftBottom,
            boolean rightBottom, boolean leftCenter, boolean rightCenter) {
        // Log.e("#########", "++++++++++++++++++++开始创建热取时间：" +
        // System.currentTimeMillis());
        // left bottom
        if (leftBottom) {
            FloatWindowHelper
                    .createFloatLeftBottomWindow(context, value);
            FloatWindowHelper
                    .createFloatLeftCenterWindow(context, value);
            FloatWindowHelper
                    .createFloatLeftTopWindow(context, value);
        }
        // right bottom
        if (rightBottom) {
            FloatWindowHelper
                    .createFloatRightBottomWindow(context, value);
            FloatWindowHelper
                    .createFloatRightCenterWindow(context, value);
            FloatWindowHelper
                    .createFloatRightTopWindow(context, value);
        }
        // left center
        if (leftCenter) {
            FloatWindowHelper.removeSwipWindow(context, 3);
            FloatWindowHelper.createFloatLeftCenterCenterWindow(context, value);
            if (leftBottom) {
                FloatWindowHelper
                        .createFloatLeftBottomWindow(context, value);
                FloatWindowHelper
                        .createFloatLeftCenterWindow(context, value);
            }
        }
        // right center
        if (rightCenter) {
            FloatWindowHelper.removeSwipWindow(context, -3);
            FloatWindowHelper.createFloatRightCenterCenterWindow(context, value);
            if (rightBottom) {
                FloatWindowHelper
                        .createFloatRightBottomWindow(context, value);
                FloatWindowHelper
                        .createFloatRightCenterWindow(context, value);
            }
        }
        // Log.e("#########", "++++++++++++++++++++结束创建热取时间：" +
        // System.currentTimeMillis());
    }

    /**
     * must call in UI thread
     * 
     * @param context
     */
    public static void createFloatWindow(Context context, int value) {
        boolean leftBottom = QuickGestureManager
                .getInstance(AppMasterApplication.getInstance()).isLeftBottom;
        boolean rightBottom = QuickGestureManager.getInstance(AppMasterApplication
                .getInstance()).isRightBottom;
        boolean leftCenter = QuickGestureManager
                .getInstance(AppMasterApplication.getInstance()).isLeftCenter;
        boolean rightCenter = QuickGestureManager.getInstance(AppMasterApplication
                .getInstance()).isRightCenter;
        // left bottom
        if (leftBottom) {
            if (!FloatWindowHelper.isLeftBottomShowing()
                    || !FloatWindowHelper.isLeftCenterShowing()
                    || !FloatWindowHelper.isLeftTopShowing()) {
                /* create float */
                createFloatArea(context, value, leftBottom, rightBottom, leftCenter, rightCenter);
            }
        }
        // right bottom
        if (rightBottom) {
            if (!FloatWindowHelper.isRightBottomShowing()
                    || !FloatWindowHelper.isRightCenterShowing()
                    || !FloatWindowHelper.isRightTopShowing()) {
                /* create float */
                createFloatArea(context, value, leftBottom, rightBottom, leftCenter, rightCenter);
            }
        }
        // left center
        if (leftCenter) {
            if (!FloatWindowHelper.isLeftCenterCenterShowing()) {
                /* create float */
                createFloatArea(context, value, leftBottom, rightBottom, leftCenter, rightCenter);
            }
        }
        // right center
        if (rightCenter) {
            if (!FloatWindowHelper.isRightCenterCenterShowing()) {
                /* create float */
                createFloatArea(context, value, leftBottom, rightBottom, leftCenter, rightCenter);
            }
        }

        // if (!FloatWindowHelper.isLeftBottomShowing()
        // || !FloatWindowHelper.isLeftCenterShowing()
        // || !FloatWindowHelper.isLeftTopShowing()
        // || !FloatWindowHelper.isRightBottomShowing()
        // || !FloatWindowHelper.isRightCenterShowing()
        // || !FloatWindowHelper.isRightTopShowing()
        // || !FloatWindowHelper.isLeftCenterCenterShowing()
        // || !FloatWindowHelper.isRightCenterCenterShowing()) {
        //
        // }
    }

    /**
     * updateFloatWindowBackgroudColor must call in UI thread
     * 
     * @param flag
     */
    public static void updateFloatWindowBackgroudColor(Context context, boolean flag) {
        if (flag) {
            if (mLeftBottomView != null) {
                mLeftBottomView
                        .setBackgroundResource(R.color.quick_gesture_switch_setting_show_color);
            }
            if (mLeftCenterView != null) {
                mLeftCenterView
                        .setBackgroundResource(R.color.quick_gesture_switch_setting_show_color);
                if (QuickGestureManager.getInstance(context).isShowSysNoReadMessage
                        && (QuickGestureManager.getInstance(context).onTuchGestureFlag == -1 || QuickGestureManager
                                .getInstance(context).onTuchGestureFlag == -2)) {
                    mLeftCenterView.setIsShowReadTip(false, 1);
                }
            }
            if (mLeftCenterCenterView != null) {
                mLeftCenterCenterView
                        .setBackgroundResource(R.color.quick_gesture_switch_setting_show_color);
                if (QuickGestureManager.getInstance(context).isShowSysNoReadMessage
                        && (QuickGestureManager.getInstance(context).onTuchGestureFlag == -1 || QuickGestureManager
                                .getInstance(context).onTuchGestureFlag == -2)
                        && mLeftBottomView == null) {
                    mLeftCenterCenterView.setIsShowReadTip(false, 3);
                }
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
                if (QuickGestureManager.getInstance(context).isShowSysNoReadMessage
                        && (QuickGestureManager.getInstance(context).onTuchGestureFlag == 1 || QuickGestureManager
                                .getInstance(context).onTuchGestureFlag == 2)) {
                    mRightCenterView.setIsShowReadTip(false, 2);
                }
            }
            if (mRightCenterCenterView != null) {
                mRightCenterCenterView
                        .setBackgroundResource(R.color.quick_gesture_switch_setting_show_color);
                if (QuickGestureManager.getInstance(context).isShowSysNoReadMessage
                        && (QuickGestureManager.getInstance(context).onTuchGestureFlag == 1 || QuickGestureManager
                                .getInstance(context).onTuchGestureFlag == 2)
                        && mRightBottomView == null) {
                    mRightCenterCenterView.setIsShowReadTip(false, 4);
                }
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
                if (QuickGestureManager.getInstance(context).isShowSysNoReadMessage
                        && (QuickGestureManager.getInstance(context).onTuchGestureFlag == -1 || QuickGestureManager
                                .getInstance(context).onTuchGestureFlag == -2)) {
                    mLeftCenterView.setIsShowReadTip(true, 1);
                }
            }
            if (mLeftCenterCenterView != null) {
                mLeftCenterCenterView
                        .setBackgroundResource(R.color.quick_gesture_switch_setting_hidden_color);
                if (QuickGestureManager.getInstance(context).isShowSysNoReadMessage
                        && (QuickGestureManager.getInstance(context).onTuchGestureFlag == -1 || QuickGestureManager
                                .getInstance(context).onTuchGestureFlag == -2)
                        && mLeftBottomView == null) {
                    mLeftCenterCenterView.setIsShowReadTip(true, 3);
                }
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
                if (QuickGestureManager.getInstance(context).isShowSysNoReadMessage
                        && (QuickGestureManager.getInstance(context).onTuchGestureFlag == 1 || QuickGestureManager
                                .getInstance(context).onTuchGestureFlag == 2)) {
                    mRightCenterView.setIsShowReadTip(true, 2);
                }
            }
            if (mRightCenterCenterView != null) {
                mRightCenterCenterView
                        .setBackgroundResource(R.color.quick_gesture_switch_setting_hidden_color);
                if (QuickGestureManager.getInstance(context).isShowSysNoReadMessage
                        && (QuickGestureManager.getInstance(context).onTuchGestureFlag == 1 || QuickGestureManager
                                .getInstance(context).onTuchGestureFlag == 2)
                        && mRightBottomView == null) {
                    mRightCenterCenterView.setIsShowReadTip(true, 4);
                }
            }
            if (mRightTopView != null) {
                mRightTopView
                        .setBackgroundResource(R.color.quick_gesture_switch_setting_hidden_color);
            }
        }
    }

    public static void setShowSlideArea(Context context, String flag) {
        int value = QuickGestureManager.getInstance(context).mSlidAreaSize;
        boolean leftBottom = QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isLeftBottom;
        boolean rightBottom = QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isRightBottom;
        boolean leftCenter = QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isLeftCenter;
        boolean rightCenter = QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isRightCenter;
        if (FloatWindowHelper.QUICK_GESTURE_LEFT_SLIDE_AREA.equals(flag)) {
            if (!leftBottom) {
                FloatWindowHelper.removeSwipWindow(context, 1);
                FloatWindowHelper.removeSwipWindow(context, 2);
                FloatWindowHelper.removeSwipWindow(context, 3);
                if (!leftCenter) {
                    FloatWindowHelper.removeSwipWindow(context, 4);
                } else {
                    FloatWindowHelper.removeSwipWindow(context, 4);
                    FloatWindowHelper
                            .createFloatLeftCenterCenterWindow(context, value);
                }
            } else {
                if (leftCenter) {
                    FloatWindowHelper.removeSwipWindow(context, 4);
                    FloatWindowHelper.removeSwipWindow(context, 3);
                    FloatWindowHelper
                            .createFloatLeftBottomWindow(context, value);
                    FloatWindowHelper
                            .createFloatLeftCenterWindow(context, value);
                    FloatWindowHelper
                            .createFloatLeftCenterCenterWindow(context, value);
                } else {
                    FloatWindowHelper.removeSwipWindow(context, 4);
                    FloatWindowHelper
                            .createFloatLeftBottomWindow(context, value);
                    FloatWindowHelper
                            .createFloatLeftCenterWindow(context, value);
                    FloatWindowHelper
                            .createFloatLeftTopWindow(context, value);
                }
            }
        }
        if (FloatWindowHelper.QUICK_GESTURE_RIGHT_SLIDE_AREA.equals(flag)) {
            if (!rightBottom) {
                FloatWindowHelper.removeSwipWindow(context, -1);
                FloatWindowHelper.removeSwipWindow(context, -2);
                FloatWindowHelper.removeSwipWindow(context, -3);
                if (!rightCenter) {
                    FloatWindowHelper.removeSwipWindow(context, -4);
                } else {
                    FloatWindowHelper.removeSwipWindow(context, -4);
                    FloatWindowHelper
                            .createFloatRightCenterCenterWindow(context, value);
                }
            } else {
                if (rightCenter) {
                    FloatWindowHelper.removeSwipWindow(context, -4);
                    FloatWindowHelper.removeSwipWindow(context, -3);

                    FloatWindowHelper
                            .createFloatRightBottomWindow(context, value);
                    FloatWindowHelper
                            .createFloatRightCenterWindow(context, value);
                    FloatWindowHelper
                            .createFloatRightCenterCenterWindow(context, value);
                } else {
                    FloatWindowHelper.removeSwipWindow(context, -4);
                    FloatWindowHelper
                            .createFloatRightBottomWindow(context, value);
                    FloatWindowHelper
                            .createFloatRightCenterWindow(context, value);
                    FloatWindowHelper
                            .createFloatRightTopWindow(context, value);
                }
            }
            if (FloatWindowHelper.mEditQuickAreaFlag) {
                FloatWindowHelper
                        .updateFloatWindowBackgroudColor(context, true);
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

    private static void onTouchAreaShowQuick(int flag) {
        if (flag == -1 || flag == -2) {
            // Log.e("#########", "滑动功能处理起始时间：" + System.currentTimeMillis());
            // cancel message tip
            if (QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isShowSysNoReadMessage) {
                AppMasterPreference.getInstance(AppMasterApplication.getInstance())
                        .setLastTimeLayout(1);
                // QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isShowSysNoReadMessage
                // = false;
            }
            Intent intent;
            intent = new Intent(AppMasterApplication.getInstance(), QuickGesturePopupActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("show_orientation", 0);
            // Log.e("#########", "滑动准备启动快捷手势界面时间：" +
            // System.currentTimeMillis());
            AppMasterApplication.getInstance().startActivity(intent);
            if (flag == -1) {
                QuickGestureManager.getInstance(AppMasterApplication.getInstance()).onTuchGestureFlag = -1;
            } else if (flag == -2) {
                QuickGestureManager.getInstance(AppMasterApplication.getInstance()).onTuchGestureFlag = -2;
            }
            // Log.e("#########", "滑动功能处理结束时间：" + System.currentTimeMillis());

        } else if (flag == 1 || flag == 2) {
            // cancel message tip
            if (QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isShowSysNoReadMessage) {
                AppMasterPreference.getInstance(AppMasterApplication.getInstance())
                        .setLastTimeLayout(1);
                // QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isShowSysNoReadMessage
                // = false;
            }
            Intent intent;
            intent = new Intent(AppMasterApplication.getInstance(), QuickGesturePopupActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("show_orientation", 2);
            AppMasterApplication.getInstance().startActivity(intent);
            if (flag == 1) {
                QuickGestureManager.getInstance(AppMasterApplication.getInstance()).onTuchGestureFlag = 1;
            } else if (flag == 2) {
                QuickGestureManager.getInstance(AppMasterApplication.getInstance()).onTuchGestureFlag = 2;
            }
        }
    }
//    public static void closePopuCreateFloatWindow(Context context,int value){
//        if(mLeftBottomView!=null && mLeftBottomParams!=null){
//            mWindowManager.addView(mLeftBottomView,mLeftBottomParams);
//        }
//        if (mLeftCenterView != null && mLeftCenterCenterParams!=null) {
//            mWindowManager.addView(mLeftCenterView, mLeftCenterParams);
//        }
//        if (mLeftTopView != null && mLeftTopParams!=null)  {
//            mWindowManager.addView(mLeftTopView, mLeftTopParams);
//        }
//        if (mLeftCenterCenterView != null && mLeftCenterCenterParams!=null) {
//            mWindowManager.addView(mLeftCenterCenterView, mLeftCenterCenterParams);
//        }
//        // update right
//        if (mRightBottomView != null && mRightBottomParams!=null) {
//            mWindowManager.addView(mRightBottomView, mRightBottomParams);
//        }
//        if (mRightCenterView != null && mRightCenterParams!=null) {
//            mWindowManager.addView(mRightCenterView, mRightCenterParams);
//        }
//        if (mRightTopView != null && mRightTopParams!=null) {
//            mWindowManager.addView(mRightTopView, mRightTopParams);
//        }
//        if (mRightCenterCenterView != null && mRightCenterCenterParams!=null ){
//            mWindowManager.addView(mRightCenterCenterView, mRightCenterCenterParams);
//        }
//    }
    
    
    public static void createWhiteFloatView(Context mContext){
        WindowManager windowManager = getWindowManager(mContext);
        int W = windowManager.getDefaultDisplay().getWidth();
        int H = windowManager.getDefaultDisplay().getHeight();
        int lastSlideOrientation = QuickGestureManager.getInstance(mContext).onTuchGestureFlag;
        
        if(null == mWhiteFloatParams){
            mWhiteFloatParams = new LayoutParams();
            mWhiteFloatParams.width = mWhiteFLoatWidth;
            mWhiteFloatParams.height = mWhiteFLoatWidth;
            mWhiteFloatParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
            mWhiteFloatParams.format = PixelFormat.RGBA_8888;
            mWhiteFloatParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | LayoutParams.FLAG_NOT_FOCUSABLE;
        }
        
        //get the last coordinate,if 0 then appear in last swipe orientation 
        int[] coordinate = AppMasterPreference.getInstance(mContext).getWhiteFloatViewCoordinate();
        if(coordinate[0] == 0){
            if(lastSlideOrientation < 0){
                mWhiteFloatParams.x = -W/2;
                if(lastSlideOrientation == -1){
                    mWhiteFloatParams.y = H-mWhiteFloatParams.height;
                }else if(lastSlideOrientation == -2){
                    mWhiteFloatParams.y = mWhiteFloatParams.height;
                }
            }else{
                mWhiteFloatParams.x = W/2;
                if(lastSlideOrientation == 1){
                    mWhiteFloatParams.y = H-mWhiteFloatParams.height;
                }else if(lastSlideOrientation == 2){
                    mWhiteFloatParams.y = mWhiteFloatParams.height;
                }
            }
        }else {
            mWhiteFloatParams.x = coordinate[0];
            mWhiteFloatParams.y = coordinate[1];
        }
        
        if(null == mWhiteFloatView){
            mWhiteFloatView = new ImageView(mContext);
            mWhiteFloatView.setImageResource(R.drawable.radio_buttons);
            setWhiteFloatOnTouchEvent(mContext);
        }
        try {
            windowManager.addView(mWhiteFloatView, mWhiteFloatParams);
        } catch (Exception e) {
        }
        registerWhiteFlaotOnScreenListener(mContext);
        Log.i("null","创建小白点");
    }
    
    public static void removeWhiteFloatView(Context mContext){
      if(null != mWhiteFloatView){
          WindowManager windowManager = getWindowManager(mContext);
          try{
              windowManager.removeView(mWhiteFloatView);
          }catch(Exception e){
          }
          if(null != mScreenListener){
              LeoGlobalBroadcast.unregisterBroadcastListener(mScreenListener);
          }
          mWhiteFloatView = null;
          Log.i("null","删除小白点");
      }
    }
    
    public static void hideWhiteFloatView(Context mContext){
        if(null != mWhiteFloatView && null != mWhiteFloatParams){
            if(mWhiteFloatView.getVisibility() == View.VISIBLE){
                WindowManager windowManager = getWindowManager(mContext);
                mWhiteFloatView.setVisibility(View.GONE);
                try{
                    windowManager.updateViewLayout(mWhiteFloatView, mWhiteFloatParams);
                }catch(Exception e){
                }
                Log.i("null","hide");
            }
        }
    }
    
    public static void showWhiteFloatView(Context mContext){
        if(null != mWhiteFloatView && null != mWhiteFloatParams){
            if(hasMessageTip(mContext)){
                
            }else{
                if(mWhiteFloatView.getVisibility() != View.VISIBLE){
                    WindowManager windowManager = getWindowManager(mContext);
                    mWhiteFloatView.setVisibility(View.VISIBLE);
                    windowManager.updateViewLayout(mWhiteFloatView, mWhiteFloatParams);
                }
            }
        }else{
            createWhiteFloatView(mContext);
        }
    }
    
    public static void update(Context mContext){
        WindowManager windowManager = getWindowManager(mContext);
        mWhiteFloatView.setVisibility(View.GONE);
        windowManager.updateViewLayout(mWhiteFloatView, mWhiteFloatParams);
    }
    
    private static void setWhiteFloatOnclickListener(final Context mContext){
        if(null == mWhiteFloatView)
            return;
        if(hasMessageTip(mContext)){
            AppMasterPreference.getInstance(mContext).setLastTimeLayout(1);
        }
        int oreatation = mWhiteFloatParams.x<0?0:2;
        showQuickGuestureView(mContext,oreatation);
       Log.i("null", "white float");
    }
    
    private static void setWhiteFloatOnTouchEvent(final Context mContext){
        if(null == mWhiteFloatView)
            return;
        final WindowManager windowManager = getWindowManager(mContext);
        final int halfW = windowManager.getDefaultDisplay().getWidth()/2;
        final int halfH = windowManager.getDefaultDisplay().getHeight()/2;
        final int diff = DipPixelUtil.dip2px(mContext,10);
        
        mWhiteFloatView.setOnTouchListener(new OnTouchListener() {
            float startX,startY,x,y,moveX,moveY;
            int upX,upY;
            long downTime;
            ValueAnimator animator;
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_OUTSIDE:
                        break;
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getRawX();
                        startY = event.getRawY();
                        downTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_MOVE:
                         moveX = Math.abs(startX - event.getRawX());
                         moveY = Math.abs(startY - event.getRawY());
                         if(moveX > 10 && moveY > 10){
                             x = event.getRawX()-halfW;
                             y =  event.getRawY()-halfH-diff;
                             mWhiteFloatParams.x = (int) x;
                             mWhiteFloatParams.y = (int) y;
                             windowManager.updateViewLayout(mWhiteFloatView, mWhiteFloatParams);
                         }
                        break;
                    case MotionEvent.ACTION_UP:
                        upX = event.getRawX() < halfW ? -halfW : halfW;
                        upY = (int) (event.getRawY() - halfH);
                        if (System.currentTimeMillis() - downTime < 200) {
                            setWhiteFloatOnclickListener(mContext);
                        }else {
                            if (x < 0) {
                                animator = ValueAnimator.ofInt((int) x, -halfW);
                            } else {
                                animator = ValueAnimator.ofInt((int) x, halfW);
                            }
                            animator.addUpdateListener(new AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator animation) {
                                    int value = (Integer) animation.getAnimatedValue();
                                    mWhiteFloatParams.x = value;
                                    windowManager.updateViewLayout(mWhiteFloatView, mWhiteFloatParams);
                                }
                            });
                            animator.start();
                        }
                        AppMasterPreference.getInstance(mContext).setWhiteFloatViewCoordinate(upX,upY);
                        break;
                }
                return false;
            }
        });
    }
    
    private static void showQuickGuestureView(Context mContext , int orientation){
        Intent intent = new Intent(mContext,
                QuickGesturePopupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("show_orientation", orientation);
        mContext.startActivity(intent);
    }
    
    private static boolean hasMessageTip(Context mContext){
         boolean isShowTip = QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage;
         boolean isShowBusinessRedTip = QuickGestureManager.getInstance(mContext).checkBusinessRedTip();
         return isShowTip || isShowBusinessRedTip;
    }

    /*
     * 快捷手势界面创建后停止创建热区的任务，解决关闭界面时立即创建热区与任务创建热区之间的时间差，引起的二次创建闪动问题
     */
    public static void stopFloatWindowCreate(Context context) {
        QuickGestureManager.getInstance(context).stopFloatWindow();
        FloatWindowHelper.removeAllFloatWindow(context);
    }

    private static void registerWhiteFlaotOnScreenListener(final Context mContext){
        if(null == mScreenListener){
            mScreenListener = new ScreenOnOffListener() {
                @Override
                public void onScreenChanged(Intent intent) {
                        if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                            hideWhiteFloatView(mContext);
                            AppMasterPreference.getInstance(mContext).setSwitchOpenStrengthenMode(false);
                            Log.i("null","锁屏啦");
                        }else if(!AppUtil.isScreenLocked(mContext) && Intent.ACTION_SCREEN_ON.equals(intent.getAction())){
                            Log.i("null","亮屏啦");
                            AppMasterPreference.getInstance(mContext).setSwitchOpenStrengthenMode(true);
                            showWhiteFloatView(mContext);
                        }else if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
                            Log.i("null","解锁啦");
                            AppMasterPreference.getInstance(mContext).setSwitchOpenStrengthenMode(true);
                            showWhiteFloatView(mContext);
                        }
                    super.onScreenChanged(intent);
                }
            };
        }
        LeoGlobalBroadcast.registerBroadcastListener(mScreenListener);
    }
}
