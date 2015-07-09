
package com.leo.appmaster.quickgestures;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;
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
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.quickgestures.ui.QuickGesturePopupActivity;
import com.leo.appmaster.quickgestures.view.QuickGesturesAreaView;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;
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
    public static final int showFirstWhite = 1;
    public static final int moveThen = 2;
    public static final int ONTUCH_LEFT_FLAG = -1;
    public static final int ONTUCH_RIGHT_FLAG = 1;
    public static final String QUICK_GESTURE_MSM_TIP = "quick_gesture_msm_tip";
    public static final String QUICK_GESTURE_ADD_FREE_DISTURB_NOTIFICATION = "quick_gesture_add_free_disturb_notification";
    private static QuickGesturesAreaView mLeftBottomView, mLeftCenterView, mLeftTopView,
            mLeftCenterCenterView;
    private static QuickGesturesAreaView mRightBottomView, mRightCenterView, mRightTopView,
            mRightCenterCenterView;
    private static LayoutParams mLeftBottomParams, mLeftCenterParams, mLeftTopParams,
            mLeftCenterCenterParams, mWhiteFloatParams;
    private static LayoutParams mRightBottomParams, mRightCenterParams, mRightTopParams,
            mRightCenterCenterParams;
    private static WindowManager mWindowManager;
    private static ImageView mWhiteFloatView;
    private static ScreenOnOffListener mScreenListener;

    public static volatile boolean mGestureShowing = false;
    public static boolean mEditQuickAreaFlag = false;
    private static float startX;
    private static float startY;
    private static boolean isMoveIng = false;
    public static boolean isFanShowing = false;
    // left bottom width
    private static float mLeftBottomWidth = 40;
    // left bottom height
    private static float mLeftBottomHeight = 20;
    // left center width
    private static float mLeftCenterWidth = 30;
    // left center height
    private static float mLeftCenterHeight = 60;
    // left center center height
    private static float mLeftCenterCenterHeight = 250;
    // left top width
    private static float mLeftTopWidth = 15;
    // left top height
    private static float mLeftTopHeight = 50;

    // right bottom width
    private static float mRightBottomWidth = 40;
    // right bottom height
    private static float mRightBottomHeight = 20;
    // right center width
    private static float mRightCenterWidth = 30;
    // right center height
    private static float mRightCenterHeight = 60;
    // right center center height
    private static float mRightCenterCenterHeight = 250;
    // right top width
    private static float mRightTopWidth = 15;
    // right top height
    private static float mRightTopHeight = 50;
    // white float width and height
    private static int mWhiteFLoatWidth, mWhiteFloatHeight;
    private static long mLastClickTime;

    private static final int LEFT_BOTTOM_FLAG = 1;
    private static final int LEFT_CENTER_FLAG = 2;
    private static final int LEFT_TOP_FLAG = 3;
    private static final int LEFT_CENTER_CENTER_FLAG = 4;
    private static final int RIGHT_BOTTOM_FLAG = -1;
    private static final int RIGHT_CENTER_FLAG = -2;
    private static final int RIGHT_CENTER_CENTER_FLAG = -3;
    private static final int RIGHT_TOP_FLAG = -4;
    public static final String RUN_TAG = "RUN_TAG";
    private static AnimationDrawable animationLightDrawable;
    private static AnimationDrawable animationDarkDrawable;
    private static int otherStep = 0;
    private static boolean beComingDark = false;
    private static boolean isControling = false;
    private static CountDownTimer nowCount;

    /**
     * left bottom must call in UI thread
     * 
     * @param context
     */
    public static void createFloatLeftBottomWindow(final Context mContext, int value) {
        final WindowManager windowManager = getWindowManager(mContext);
        final boolean isShowTip = QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage;
        if (mLeftBottomView == null) {
            mLeftBottomView = new QuickGesturesAreaView(mContext);
            // // no read contact /message/privacycontact red tip
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
                            moveX = startX - event.getRawX();
                            moveY = startY - event.getRawY();
                            float pressure = event.getPressure();
                            if (-moveX > 0 && moveY > 0) {
                                if (((-moveX > ViewConfiguration.get(mContext).getScaledTouchSlop() * 1.5
                                || moveY > ViewConfiguration.get(mContext)
                                        .getScaledTouchSlop() * 1.5 /*
                                                                     * ||
                                                                     * pressure
                                                                     * > 0.8
                                                                     */)
                                && !isMoveIng)) {
                                    isMoveIng = true;
                                    if (!mEditQuickAreaFlag) {
                                        FloatWindowHelper.mGestureShowing = true;
                                        /*
                                         * 快捷手势界面创建后停止创建热区的任务，
                                         * 解决因为快捷界面首次启动慢在此过程中热区又创建的问题
                                         */
                                        FloatWindowHelper.stopFloatWindowCreate(mContext);
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
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            if ((moveX < ViewConfiguration.get(mContext).getScaledTouchSlop() * 1.5 && moveY < ViewConfiguration
                                    .get(mContext).getScaledTouchSlop() * 1.5)) {
                                // cancel system no read message tip
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
                mLeftBottomParams.x = (int) ((DipPixelUtil.dip2px(mContext, mLeftCenterWidth) / 2) + (value / 2))
                        * 2 + (-(width / 2) + (mLeftBottomParams.width / 2));
                mLeftBottomParams.y = (int) ((height / 2) - (mLeftBottomParams.height / 2));
                mLeftBottomParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
                mLeftBottomParams.format = PixelFormat.RGBA_8888;
                mLeftBottomParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            } else {
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
        final WindowManager windowManager = getWindowManager(mContext);
        final boolean isShowTip = QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage;
        final boolean isShowBusinessRedTip = QuickGestureManager.getInstance(mContext)
                .checkBusinessRedTip();
        final boolean isOpenStrengthenMode = AppMasterPreference.getInstance(mContext)
                .getSwitchOpenStrengthenMode();
        if (mLeftCenterView == null) {
            mLeftCenterView = new QuickGesturesAreaView(mContext);
            // no read contact /message/privacycontact red tip
            if (QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage
                    && (QuickGestureManager.getInstance(mContext).onTuchGestureFlag == -1 || QuickGestureManager
                            .getInstance(mContext).onTuchGestureFlag == -2)
                    && !isOpenStrengthenMode) {
                mLeftCenterView.setIsShowReadTip(true, 1);
            }
            // business red tip
            if (isShowBusinessRedTip
                    && (QuickGestureManager.getInstance(mContext).onTuchGestureFlag == -1 || QuickGestureManager
                            .getInstance(mContext).onTuchGestureFlag == -2)
                    && !isOpenStrengthenMode) {
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
                            moveX = startX - event.getRawX();
                            moveY = startY - event.getRawY();
                            float presssure = event.getPressure();
                            if (-moveX > 0 && moveY > 0) {
                                if (((-moveX > ViewConfiguration.get(mContext).getScaledTouchSlop() * 1.5
                                || moveY > ViewConfiguration.get(mContext)
                                        .getScaledTouchSlop() * 1.5 /*
                                                                     * ||
                                                                     * presssure
                                                                     * > 0.8
                                                                     */)
                                && !isMoveIng)) {
                                    isMoveIng = true;
                                    if (!mEditQuickAreaFlag) {
                                        FloatWindowHelper.mGestureShowing = true;
                                        /*
                                         * 快捷手势界面创建后停止创建热区的任务，
                                         * 解决因为快捷界面首次启动慢在此过程中热区又创建的问题
                                         */
                                        FloatWindowHelper.stopFloatWindowCreate(mContext);
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
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            if ((moveX < ViewConfiguration.get(mContext).getScaledTouchSlop() * 1.5 && moveY < ViewConfiguration
                                    .get(mContext).getScaledTouchSlop() * 1.5)) {
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
                mLeftCenterParams.y = (int) ((height / 2) - (mLeftCenterParams.height / 2));
                mLeftCenterParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
                mLeftCenterParams.format = PixelFormat.RGBA_8888;
                mLeftCenterParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            } else {
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
        final boolean isOpenStrengthenMode = AppMasterPreference.getInstance(mContext)
                .getSwitchOpenStrengthenMode();
        if (mLeftCenterCenterView == null) {
            mLeftCenterCenterView = new QuickGesturesAreaView(mContext);
            // no read contact/message/privacycontact red tip
            if (QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage
                    && (QuickGestureManager.getInstance(mContext).onTuchGestureFlag == -1 || QuickGestureManager
                            .getInstance(mContext).onTuchGestureFlag == -2)
                    && mLeftBottomView == null && !isOpenStrengthenMode) {
                mLeftCenterCenterView.setIsShowReadTip(true, 3);
            }
            // business red tip
            if (isShowBusinessRedTip
                    && (QuickGestureManager.getInstance(mContext).onTuchGestureFlag == -1 || QuickGestureManager
                            .getInstance(mContext).onTuchGestureFlag == -2)
                    && mLeftBottomView == null && !isOpenStrengthenMode) {
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
                            moveX = startX - event.getRawX();
                            moveY = startY - event.getRawY();
                            float presssure = event.getPressure();
                            if (-moveX > 0 && moveY > 0) {
                                if (((-moveX > ViewConfiguration.get(mContext).getScaledTouchSlop() * 1.5
                                || moveY > ViewConfiguration.get(mContext)
                                        .getScaledTouchSlop() * 1.5 /*
                                                                     * ||
                                                                     * presssure
                                                                     * > 0.8
                                                                     */)
                                && !isMoveIng)) {
                                    isMoveIng = true;
                                    if (!mEditQuickAreaFlag) {
                                        FloatWindowHelper.mGestureShowing = true;
                                        /*
                                         * 快捷手势界面创建后停止创建热区的任务，
                                         * 解决因为快捷界面首次启动慢在此过程中热区又创建的问题
                                         */
                                        FloatWindowHelper.stopFloatWindowCreate(mContext);
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
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            if ((moveX < ViewConfiguration.get(mContext).getScaledTouchSlop() * 1.5 && moveY < ViewConfiguration
                                    .get(mContext).getScaledTouchSlop() * 1.5)) {
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
                    mLeftCenterCenterParams.y = (int) ((height / 2)
                            - (mLeftCenterCenterParams.height / 2) - mLeftCenterParams.height - DipPixelUtil
                            .dip2px(mContext, 12));
                } else {
                    mLeftCenterCenterParams.y = (int) ((height / 2)
                            - (mLeftCenterCenterParams.height / 2)
                            - leftBottom - leftCenter - DipPixelUtil
                            .dip2px(mContext, 12));
                }
                mLeftCenterCenterParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
                mLeftCenterCenterParams.format = PixelFormat.RGBA_8888;
                mLeftCenterCenterParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            } else {
                mLeftCenterCenterParams.x = (int) (-(width / 2) + (mLeftCenterCenterParams.width / 2));
                if (mLeftBottomView != null) {
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
                            moveX = startX - event.getRawX();
                            moveY = startY - event.getRawY();
                            float presssure = event.getPressure();
                            if (-moveX > 0 && moveY > 0) {
                                if (((-moveX > ViewConfiguration.get(mContext).getScaledTouchSlop() * 1.5
                                || moveY > ViewConfiguration.get(mContext)
                                        .getScaledTouchSlop() * 1.5 /*
                                                                     * ||
                                                                     * presssure
                                                                     * > 0.8
                                                                     */)
                                && !isMoveIng)) {
                                    isMoveIng = true;
                                    if (!mEditQuickAreaFlag) {
                                        FloatWindowHelper.mGestureShowing = true;
                                        /*
                                         * 快捷手势界面创建后停止创建热区的任务，
                                         * 解决因为快捷界面首次启动慢在此过程中热区又创建的问题
                                         */
                                        FloatWindowHelper.stopFloatWindowCreate(mContext);
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
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            if ((moveX < ViewConfiguration.get(mContext).getScaledTouchSlop() * 1.5 && moveY < ViewConfiguration
                                    .get(mContext).getScaledTouchSlop() * 1.5)) {
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
                mLeftTopParams.y = (int) ((height / 2) - (mLeftTopParams.height / 2)
                        - mLeftCenterParams.height - DipPixelUtil
                        .dip2px(mContext, 12));

                mLeftTopParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
                mLeftTopParams.format = PixelFormat.RGBA_8888;
                mLeftTopParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            } else {
                mLeftTopParams.x = (int) (-(width / 2) + (mLeftTopParams.width / 2));
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
        if (mRightBottomView == null) {
            mRightBottomView = new QuickGesturesAreaView(mContext);
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
                            moveX = startX - event.getRawX();
                            moveY = startY - event.getRawY();
                            float presssure = event.getPressure();
                            if (moveX > 0 && moveY > 0) {
                                if (((moveX > ViewConfiguration.get(mContext).getScaledTouchSlop() * 1.5
                                || moveY > ViewConfiguration.get(mContext)
                                        .getScaledTouchSlop() * 1.5 /*
                                                                     * ||
                                                                     * presssure
                                                                     * > 0.8
                                                                     */)
                                && !isMoveIng)) {
                                    isMoveIng = true;
                                    if (!mEditQuickAreaFlag) {
                                        FloatWindowHelper.mGestureShowing = true;
                                        /*
                                         * 快捷手势界面创建后停止创建热区的任务，
                                         * 解决因为快捷界面首次启动慢在此过程中热区又创建的问题
                                         */
                                        FloatWindowHelper.stopFloatWindowCreate(mContext);
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
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            if ((moveX < ViewConfiguration.get(mContext).getScaledTouchSlop() * 1.5 && moveY < ViewConfiguration
                                    .get(mContext).getScaledTouchSlop() * 1.5)) {
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
                mRightBottomParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
                mRightBottomParams.format = PixelFormat.RGBA_8888;
                mRightBottomParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            } else {
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
        final boolean isOpenStrengthenMode = AppMasterPreference.getInstance(mContext)
                .getSwitchOpenStrengthenMode();
        if (mRightCenterView == null) {
            mRightCenterView = new QuickGesturesAreaView(mContext);
            // no read contact/message/privacycontact red tip
            if (QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage
                    && (QuickGestureManager.getInstance(mContext).onTuchGestureFlag == 1 || QuickGestureManager
                            .getInstance(mContext).onTuchGestureFlag == 2) && !isOpenStrengthenMode) {
                mRightCenterView.setIsShowReadTip(true, 2);
            }
            // business red tip
            if (isShowBusinessRedTip
                    && (QuickGestureManager.getInstance(mContext).onTuchGestureFlag == 1 || QuickGestureManager
                            .getInstance(mContext).onTuchGestureFlag == 2) && !isOpenStrengthenMode) {
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
                            moveX = startX - event.getRawX();
                            moveY = startY - event.getRawY();
                            float presssure = event.getPressure();
                            if (moveX > 0 && moveY > 0) {
                                if (((moveX > ViewConfiguration.get(mContext).getScaledTouchSlop() * 1.5
                                || moveY > ViewConfiguration.get(mContext)
                                        .getScaledTouchSlop() * 1.5 /*
                                                                     * ||
                                                                     * presssure
                                                                     * > 0.8
                                                                     */)
                                && !isMoveIng)) {
                                    isMoveIng = true;
                                    if (!mEditQuickAreaFlag) {
                                        FloatWindowHelper.mGestureShowing = true;
                                        /*
                                         * 快捷手势界面创建后停止创建热区的任务，
                                         * 解决因为快捷界面首次启动慢在此过程中热区又创建的问题
                                         */
                                        FloatWindowHelper.stopFloatWindowCreate(mContext);
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
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            if ((moveX < ViewConfiguration.get(mContext).getScaledTouchSlop() * 1.5 && moveY < ViewConfiguration
                                    .get(mContext).getScaledTouchSlop() * 1.5)) {
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
                mRightCenterParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
                mRightCenterParams.format = PixelFormat.RGBA_8888;
                mRightCenterParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            } else {
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
        final boolean isOpenStrengthenMode = AppMasterPreference.getInstance(mContext)
                .getSwitchOpenStrengthenMode();
        if (mRightCenterCenterView == null) {
            mRightCenterCenterView = new QuickGesturesAreaView(mContext);
            // no read contact/message/privacycontact red tip
            if (QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage
                    && (QuickGestureManager.getInstance(mContext).onTuchGestureFlag == 1 || QuickGestureManager
                            .getInstance(mContext).onTuchGestureFlag == 2)
                    && mRightBottomView == null && !isOpenStrengthenMode) {
                mRightCenterCenterView.setIsShowReadTip(true, 4);
            }
            // business red tip
            if (isShowBusinessRedTip
                    && (QuickGestureManager.getInstance(mContext).onTuchGestureFlag == 1 || QuickGestureManager
                            .getInstance(mContext).onTuchGestureFlag == 2)
                    && mRightBottomView == null && !isOpenStrengthenMode) {
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
                            moveX = startX - event.getRawX();
                            moveY = startY - event.getRawY();
                            float presssure = event.getPressure();
                            if (moveX > 0 && moveY > 0) {
                                if (((moveX > ViewConfiguration.get(mContext).getScaledTouchSlop() * 1.5
                                || moveY > ViewConfiguration.get(mContext)
                                        .getScaledTouchSlop() * 1.5 /*
                                                                     * ||
                                                                     * presssure
                                                                     * > 0.8
                                                                     */)
                                && !isMoveIng)) {
                                    isMoveIng = true;
                                    if (!mEditQuickAreaFlag) {
                                        FloatWindowHelper.mGestureShowing = true;
                                        /*
                                         * 快捷手势界面创建后停止创建热区的任务，
                                         * 解决因为快捷界面首次启动慢在此过程中热区又创建的问题
                                         */
                                        FloatWindowHelper.stopFloatWindowCreate(mContext);
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
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            if ((moveX < ViewConfiguration.get(mContext).getScaledTouchSlop() * 1.5 && moveY < ViewConfiguration
                                    .get(mContext).getScaledTouchSlop() * 1.5)) {
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
                    mRightCenterCenterParams.y = (int) ((height / 2)
                            - (mRightCenterCenterParams.height / 2) - mRightCenterParams.height - DipPixelUtil
                            .dip2px(mContext, 12));
                } else {
                    mRightCenterCenterParams.y = (int) ((height / 2)
                            - (mRightCenterCenterParams.height / 2) - rightBottom
                            - rightCenter - DipPixelUtil
                            .dip2px(mContext, 12));
                }
                mRightCenterCenterParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
                mRightCenterCenterParams.format = PixelFormat.RGBA_8888;
                mRightCenterCenterParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            } else {
                if (mRightBottomView != null) {
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
                            moveX = startX - event.getRawX();
                            moveY = startY - event.getRawY();
                            float presssure = event.getPressure();
                            if (moveX > 0 && moveY > 0) {
                                if (((moveX > ViewConfiguration.get(mContext).getScaledTouchSlop() * 1.5
                                || moveY > ViewConfiguration.get(mContext)
                                        .getScaledTouchSlop() * 1.5 /*
                                                                     * ||
                                                                     * presssure
                                                                     * > 0.8
                                                                     */) && !isMoveIng)) {
                                    isMoveIng = true;
                                    if (!mEditQuickAreaFlag) {
                                        FloatWindowHelper.mGestureShowing = true;
                                        /*
                                         * 快捷手势界面创建后停止创建热区的任务，
                                         * 解决因为快捷界面首次启动慢在此过程中热区又创建的问题
                                         */
                                        FloatWindowHelper.stopFloatWindowCreate(mContext);
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
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            if ((moveX < ViewConfiguration.get(mContext).getScaledTouchSlop() * 1.5 && moveY < ViewConfiguration
                                    .get(mContext).getScaledTouchSlop() * 1.5)) {
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
                mRightTopParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
                mRightTopParams.format = PixelFormat.RGBA_8888;
                mRightTopParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            } else {
                mRightTopParams.x = (int) ((width / 2) + (mRightTopParams.width / 2));
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
        // Log.e(FloatWindowHelper.RUN_TAG, "删除红点");
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
            mLeftBottomParams.x = (int) ((DipPixelUtil.dip2px(context, mLeftCenterWidth) / 2) + (value / 2))
                    * 2 + (-(width / 2) + (mLeftBottomParams.width / 2));
            mLeftBottomParams.y = (int) ((height / 2) - (mLeftBottomParams.height / 2));
        }
        // left center
        if (mLeftCenterParams != null) {
            mLeftCenterParams.width = (int) ((DipPixelUtil.dip2px(context, mLeftCenterWidth) / 2) + (value / 2)) * 2;
            mLeftCenterParams.height = (int) ((DipPixelUtil.dip2px(context, mLeftCenterHeight) / 2) + (value)) * 2;
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
            mLeftTopParams.y = (int) ((height / 2) - (mLeftTopParams.height / 2)
                    - mLeftCenterParams.height - DipPixelUtil
                    .dip2px(context, 12));
        }
        // right bottom
        if (mRightBottomParams != null) {
            mRightBottomParams.width = (int) ((DipPixelUtil.dip2px(context, mRightBottomWidth) / 2) + (value / 2)) * 2;
            mRightBottomParams.height = (int) ((DipPixelUtil.dip2px(context, mRightBottomHeight) / 2) + (value)) * 2;
            mRightBottomParams.x = (int) ((width / 2) - (mRightBottomParams.width / 2))
                    - ((DipPixelUtil.dip2px(context, mRightCenterWidth) / 2) + (value / 2)) * 2;
            mRightBottomParams.y = (int) ((height / 2) - (mRightBottomParams.height / 2));
        }
        // right center
        if (mRightCenterParams != null) {
            mRightCenterParams.width = (int) ((DipPixelUtil.dip2px(context, mRightCenterWidth) / 2) + (value / 2)) * 2;
            mRightCenterParams.height = (int) ((DipPixelUtil.dip2px(context, mRightCenterHeight) / 2) + (value)) * 2;
            mRightCenterParams.x = (int) ((width / 2) + (mRightCenterParams.width / 2));
            mRightCenterParams.y = (int) ((height / 2) - (mRightCenterParams.height / 2));
        }
        // right top
        if (mRightTopParams != null) {
            mRightTopParams.width = (int) ((DipPixelUtil.dip2px(context, mRightTopWidth) / 2) + (value / 2)) * 2;
            mRightTopParams.height = (int) ((DipPixelUtil.dip2px(context, mRightTopHeight) / 2) + (value)) * 2;
            mRightTopParams.x = (int) ((width / 2) + (mRightTopParams.width / 2));
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

    private static void onTouchAreaShowQuick(int flag) {
        if (flag == -1 || flag == -2) {
            if (QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isShowSysNoReadMessage) {
                AppMasterPreference.getInstance(AppMasterApplication.getInstance())
                        .setLastTimeLayout(1);
            }
            Intent intent;
            intent = new Intent(AppMasterApplication.getInstance(), QuickGesturePopupActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("show_orientation", 0);
            if (TextUtils
                    .equals(AppMasterApplication.getInstance().getPackageName(), LockManager
                            .getInstatnce().getLastPackage())) {
                intent.putExtra("from_self_app", true);
            }
            AppMasterApplication.getInstance().startActivity(intent);
            if (flag == -1) {
                QuickGestureManager.getInstance(AppMasterApplication.getInstance()).onTuchGestureFlag = -1;
            } else if (flag == -2) {
                QuickGestureManager.getInstance(AppMasterApplication.getInstance()).onTuchGestureFlag = -2;
            }

        } else if (flag == 1 || flag == 2) {
            // cancel message tip
            if (QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isShowSysNoReadMessage) {
                AppMasterPreference.getInstance(AppMasterApplication.getInstance())
                        .setLastTimeLayout(1);
            }
            Intent intent;
            intent = new Intent(AppMasterApplication.getInstance(), QuickGesturePopupActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("show_orientation", 2);
            if (TextUtils
                    .equals(AppMasterApplication.getInstance().getPackageName(), LockManager
                            .getInstatnce().getLastPackage())) {
                intent.putExtra("from_self_app", true);
            }
            AppMasterApplication.getInstance().startActivity(intent);
            if (flag == 1) {
                QuickGestureManager.getInstance(AppMasterApplication.getInstance()).onTuchGestureFlag = 1;
            } else if (flag == 2) {
                QuickGestureManager.getInstance(AppMasterApplication.getInstance()).onTuchGestureFlag = 2;
            }
        }
    }

    public static void createWhiteFloatView(Context mContext) {
        if (null == mWhiteFloatView) {
            WindowManager windowManager = getWindowManager(mContext);
            AppMasterPreference pref = AppMasterPreference.getInstance(mContext);
            int halfW = windowManager.getDefaultDisplay().getWidth() / 2;
            int H = windowManager.getDefaultDisplay().getHeight() / 2;
            int lastSlideOrientation = QuickGestureManager.getInstance(mContext).onTuchGestureFlag;

            createWhiteFloatParams(mContext);
            // get the last coordinate,if 0 then appear in last swipe
            // orientation
            int[] coordinate = AppMasterPreference.getInstance(mContext)
                    .getWhiteFloatViewCoordinate();
            if (coordinate[0] == 0) {
                // if is the upgrade user and first time create white float,then
                // show int the left center
                if (pref.getUseStrengthenModeTimes() == 0 && pref.getIsUpdateQuickGestureUser()) {
                    lastSlideOrientation = -2;
                }
                if (lastSlideOrientation < 0) {
                    mWhiteFloatParams.x = -halfW;
                    if (lastSlideOrientation == -1) {
                        mWhiteFloatParams.y = H - mWhiteFloatParams.height / 2;
                    } else if (lastSlideOrientation == -2) {
                        mWhiteFloatParams.y = mWhiteFloatParams.height;
                    }
                } else {
                    mWhiteFloatParams.x = halfW;
                    if (lastSlideOrientation == 1) {
                        mWhiteFloatParams.y = H - mWhiteFloatParams.height / 2;
                    } else if (lastSlideOrientation == 2) {
                        mWhiteFloatParams.y = mWhiteFloatParams.height;
                    }
                }
                pref.setWhiteFloatViewCoordinate(mWhiteFloatParams.x, mWhiteFloatParams.y);
            } else {
                mWhiteFloatParams.x = coordinate[0];
                mWhiteFloatParams.y = coordinate[1];
            }
            Log.i("######", "mWhiteFloatParams.y = " + mWhiteFloatParams.y);

            mWhiteFloatView = new ImageView(mContext);
            // mWhiteFloatView.setBackgroundResource(R.drawable.gesture_white_point);

            goToChangeLight();

            setWhiteFloatOnTouchEvent(mContext);
            registerWhiteFlaotOnScreenListener(mContext);
            try {
                windowManager.addView(mWhiteFloatView, mWhiteFloatParams);
                whiteFloatAppearAnim(mContext);
            } catch (Exception e) {
                windowManager.updateViewLayout(mWhiteFloatView, mWhiteFloatParams);
            }
            Log.i("null", "createWhiteFloatView");
        }
    }

    private static void goToChangeDark() {
        if (mWhiteFloatView != null && !isControling) {
            beComingDark = true;
            mWhiteFloatView.setBackgroundResource(R.drawable.whitedotanimation2);
            animationDarkDrawable = (AnimationDrawable)
                    mWhiteFloatView.getBackground();
            animationDarkDrawable.start();

            int duration = 0;
            for (int i = 0; i < animationDarkDrawable.getNumberOfFrames(); i++) {
                duration += animationDarkDrawable.getDuration(i);
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    beComingDark = false;
                }
            }, duration);
        }
    }

    private static void goToChangeLight() {
        if (mWhiteFloatView != null) {

            // 取消上一次的持续效果
            if (nowCount != null) {
                nowCount.cancel();
                nowCount = null;
            }

            mWhiteFloatView.setBackgroundResource(R.drawable.whitedotanimation1);
            animationLightDrawable = (AnimationDrawable)
                    mWhiteFloatView.getBackground();
            animationLightDrawable.start();

            // 已经变亮，开始计时变暗
            int duration = 0;
            for (int i = 0; i < animationLightDrawable.getNumberOfFrames(); i++) {
                duration += animationLightDrawable.getDuration(i);
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {

                    nowCount = new CountDownTimer(3000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            LeoLog.d("testAnimation", "millisUntilFinished : "
                                    + millisUntilFinished);
                        }

                        @Override
                        public void onFinish() {
                            LeoLog.d("testAnimation", "Finish!");
                            goToChangeDark();
                        }
                    };
                    nowCount.start();
                }
            }, duration);
        }

    }

    private static void createWhiteFloatParams(Context mContext) {
        if (null == mWhiteFloatParams) {
            if (mWhiteFLoatWidth <= 0) {
                mWhiteFLoatWidth = mContext.getResources().getDimensionPixelSize(
                        R.dimen.quick_white_float_width);
            }
            if (mWhiteFloatHeight <= 0) {
                mWhiteFloatHeight = mContext.getResources().getDimensionPixelSize(
                        R.dimen.quick_white_float_width);
            }
            mWhiteFloatParams = new LayoutParams();
            mWhiteFloatParams.width = mWhiteFLoatWidth;
            mWhiteFloatParams.height = mWhiteFloatHeight;
            mWhiteFloatParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
            mWhiteFloatParams.format = PixelFormat.RGBA_8888;
            mWhiteFloatParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | LayoutParams.FLAG_NOT_FOCUSABLE;
        }
    }

    public static void removeWhiteFloatView(Context mContext) {
        if (null != mWhiteFloatView) {
            WindowManager windowManager = getWindowManager(mContext);
            try {
                windowManager.removeView(mWhiteFloatView);
            } catch (Exception e) {
            }
            if (null != mScreenListener) {
                LeoGlobalBroadcast.unregisterBroadcastListener(mScreenListener);
            }
            mWhiteFloatView = null;
        }
    }

    public static void hideWhiteFloatView(Context mContext) {
        if (null != mWhiteFloatView && null != mWhiteFloatParams) {
            if (mWhiteFloatView.getVisibility() == View.VISIBLE) {
                WindowManager windowManager = getWindowManager(mContext);
                mWhiteFloatView.setVisibility(View.GONE);
                try {
                    windowManager.updateViewLayout(mWhiteFloatView, mWhiteFloatParams);
                    Log.i("null", "hideWhiteFloatView");
                } catch (Exception e) {
                }
            }
        }
    }

    public static void showWhiteFloatView(Context mContext) {
        if (null != mWhiteFloatView && null != mWhiteFloatParams) {
            WindowManager windowManager = getWindowManager(mContext);
            if (hasMessageTip(mContext)) {
                mWhiteFloatView.setVisibility(View.VISIBLE);
                mWhiteFloatView.setImageResource(R.drawable.gesture_red_point);
                windowManager.updateViewLayout(mWhiteFloatView, mWhiteFloatParams);
            } else {
                mWhiteFloatView.setVisibility(View.VISIBLE);
                mWhiteFloatView.setImageResource(0);
                windowManager.updateViewLayout(mWhiteFloatView, mWhiteFloatParams);
            }
        } else {
            createWhiteFloatView(mContext);
        }
    }

    private static void onWhiteFloatClick(Context mContext) {
        if (null == mWhiteFloatView)
            return;
        if (System.currentTimeMillis() - mLastClickTime < 1000) {
            return;
        }
        AppMasterPreference pref = AppMasterPreference.getInstance(mContext);
        if (hasMessageTip(mContext)) {
            // if has new tip,then to last use layout
            pref.setLastTimeLayout(1);
            mWhiteFloatView.setImageResource(0);
        }
        pref.addUseStrengthenModeTimes();
        pref.setNeedShowWhiteDotSlideTip(false);
        checkDismissWhiteDotLuminescence(mContext);
        int oreatation = mWhiteFloatParams.x < 0 ? 0 : 2;
        showQuickGuestureView(mContext, oreatation);
    }

    private static void setWhiteFloatOnTouchEvent(final Context mContext) {
        if (null == mWhiteFloatView)
            return;
        final WindowManager windowManager = getWindowManager(mContext);
        final int halfW = windowManager.getDefaultDisplay().getWidth() / 2;
        final int halfH = windowManager.getDefaultDisplay().getHeight() / 2;
        final int diff = DipPixelUtil.dip2px(mContext, 10);

        mWhiteFloatView.setOnTouchListener(new OnTouchListener() {
            float startX, startY, x, y, moveX, moveY;
            int upX, upY;
            long downTime;
            boolean ifMove;
            ValueAnimator animator;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_OUTSIDE:
                        break;
                    case MotionEvent.ACTION_DOWN:
                        isControling = true;
                        // 取消上一次的持续效果
                        if (nowCount != null) {
                            nowCount.cancel();
                            nowCount = null;
                        }
                        mWhiteFloatView.setBackgroundResource(R.drawable.gesture_white_point);

                        startX = event.getRawX();
                        startY = event.getRawY();
                        downTime = System.currentTimeMillis();
                        Log.i("tag", "startX =" + startX + "startY = " + startY);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        LeoLog.d("", "");
                        moveX = Math.abs(startX - event.getRawX());
                        moveY = Math.abs(startY - event.getRawY());
                        if (moveX > 10 || moveY > 10) {
                            x = event.getRawX() - halfW;
                            y = event.getRawY() - halfH - diff;
                            mWhiteFloatParams.x = (int) x;
                            mWhiteFloatParams.y = (int) y;
                            windowManager.updateViewLayout(mWhiteFloatView, mWhiteFloatParams);
                            ifMove = true;
                        }
                        Log.i("tag", "x = " + x + " y = " + y);
                        Log.i("tag", "event.getRawX() = " + event.getRawX());
                        Log.i("tag", "moveX = " + moveX);
                        break;
                    case MotionEvent.ACTION_UP:
                        isControling = false;
                        goToChangeLight();

                        upX = event.getRawX() < halfW ? -halfW : halfW;
                        upY = (int) (event.getRawY() - halfH);
                        Log.i("tag", System.currentTimeMillis() - downTime + " ");
                        if (System.currentTimeMillis() - downTime < 150) {
                            onWhiteFloatClick(mContext);
                        } else if (ifMove) {
                            if (x < 0) {
                                animator = ValueAnimator.ofInt((int) x, -halfW);
                            } else {
                                animator = ValueAnimator.ofInt((int) x, halfW);
                            }
                            Log.i("tag", "up then  x = " + x);
                            animator.addUpdateListener(new AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator animation) {
                                    int value = (Integer) animation.getAnimatedValue();
                                    mWhiteFloatParams.x = value;
                                    windowManager.updateViewLayout(mWhiteFloatView,
                                            mWhiteFloatParams);
                                }
                            });
                            animator.start();
                        }
                        if (moveY > 10) {
                            AppMasterPreference.getInstance(mContext).setWhiteFloatViewCoordinate(
                                    upX, upY);
                        }
                        break;
                }
                return true;
            }
        });
    }

    private static void showQuickGuestureView(Context mContext, int orientation) {
        Intent intent = new Intent(mContext,
                QuickGesturePopupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("show_orientation", orientation);
        intent.putExtra("from_white_dot", true);
        if (TextUtils
                .equals(mContext.getPackageName(), LockManager.getInstatnce().getLastPackage())) {
            intent.putExtra("from_self_app", true);
        }
        mContext.startActivity(intent);
    }

    private static boolean hasMessageTip(Context mContext) {
        boolean isShowTip = QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage;
        boolean isShowBusinessRedTip = QuickGestureManager.getInstance(mContext)
                .checkBusinessRedTip();
        return isShowTip || isShowBusinessRedTip;
    }

    /*
     * 快捷手势界面创建后停止创建热区的任务，解决关闭界面时立即创建热区与任务创建热区之间的时间差，引起的二次创建闪动问题
     */
    public static void stopFloatWindowCreate(Context context) {
        QuickGestureManager.getInstance(context).stopFloatWindow();
        FloatWindowHelper.removeAllFloatWindow(context);
    }

    private static void registerWhiteFlaotOnScreenListener(final Context mContext) {
        if (null == mScreenListener) {
            mScreenListener = new ScreenOnOffListener() {
                @Override
                public void onScreenChanged(Intent intent) {
                    if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                        hideWhiteFloatView(mContext);
                        AppMasterPreference.getInstance(mContext)
                                .setSwitchOpenStrengthenMode(false);
                    } else if (!AppUtil.isScreenLocked(mContext)
                            && Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                        AppMasterPreference.getInstance(mContext).setSwitchOpenStrengthenMode(true);
                        showWhiteFloatView(mContext);
                    } else if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
                        AppMasterPreference.getInstance(mContext).setSwitchOpenStrengthenMode(true);
                        showWhiteFloatView(mContext);
                    }
                    super.onScreenChanged(intent);
                }
            };
        }
        LeoGlobalBroadcast.registerBroadcastListener(mScreenListener);
    }

    public static void checkShowWhiteDotLuminescence(Context context) {
        if (mWhiteFloatView != null && mWhiteFloatView.getVisibility() == View.VISIBLE) {

            AnimationDrawable ad = (AnimationDrawable) context.getResources().getDrawable(
                    R.drawable.white_dot_luminescence);
            mWhiteFloatView.setBackgroundDrawable(ad);
            ad.start();
        }
    }

    public static void checkDismissWhiteDotLuminescence(final Context context) {
        if (mWhiteFloatView != null) {
            Drawable bd = mWhiteFloatView.getBackground();
            if (bd instanceof AnimationDrawable) {
                AnimationDrawable ad = (AnimationDrawable) bd;
                ad.stop();
                mWhiteFloatView.setBackgroundResource(R.drawable.gesture_white_point);
                AppMasterPreference amp = AppMasterPreference
                        .getInstance(context);
                amp.setNeedShowWhiteDotSlideTip(false);
            }
        }
    }

    // 去除热区红点，未读，运营icon和红点
    public static void cancelAllRedTip(Context context) {
        Log.e(FloatWindowHelper.RUN_TAG, "是否显示红点："
                + QuickGestureManager.getInstance(context).isShowSysNoReadMessage);
        // 隐私通话
        if (QuickGestureManager.getInstance(context).isShowPrivacyCallLog) {
            QuickGestureManager.getInstance(context).isShowSysNoReadMessage = false;
            QuickGestureManager.getInstance(context).isShowPrivacyCallLog = false;
            AppMasterPreference.getInstance(context).setQuickGestureCallLogTip(
                    false);
        }
        // 隐私短信
        if (QuickGestureManager.getInstance(context).isShowPrivacyMsm) {
            QuickGestureManager.getInstance(context).isShowSysNoReadMessage = false;
            QuickGestureManager.getInstance(context).isShowPrivacyMsm = false;
            AppMasterPreference.getInstance(context).setQuickGestureMsmTip(false);
        }
        // 短信，通话记录
        if (QuickGestureManager.getInstance(context).mCallLogs != null) {
            QuickGestureManager.getInstance(context).mCallLogs.clear();
            if (QuickGestureManager.getInstance(context).isShowSysNoReadMessage) {
                QuickGestureManager.getInstance(context).isShowSysNoReadMessage = false;
            }
        }
        if (QuickGestureManager.getInstance(context).mMessages != null) {
            QuickGestureManager.getInstance(context).mMessages.clear();
            if (QuickGestureManager.getInstance(context).isShowSysNoReadMessage) {
                QuickGestureManager.getInstance(context).isShowSysNoReadMessage = false;
            }
        }
        // 运营
        if (!AppMasterPreference.getInstance(context).getLastBusinessRedTipShow()) {
            AppMasterPreference.getInstance(context).setLastBusinessRedTipShow(true);
            if (QuickGestureManager.getInstance(context).isShowSysNoReadMessage) {
                QuickGestureManager.getInstance(context).isShowSysNoReadMessage = false;
            }
        }
    }

    public static void initSlidingArea(AppMasterPreference pre) {
        // left bottom
        QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isLeftBottom = pre
                .getDialogRadioLeftBottom();
        // right bottom
        QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isRightBottom = pre
                .getDialogRadioRightBottom();
        // left center
        QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isLeftCenter = pre
                .getDialogRadioLeftCenter();
        // right center
        QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isRightCenter = pre
                .getDialogRadioRightCenter();
    }
    
    private static void whiteFloatAppearAnim(Context mContext){
        final WindowManager windowManager = getWindowManager(mContext);
        ValueAnimator alphAnimator = ValueAnimator.ofFloat(0f,1.0f).setDuration(600);
        alphAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mWhiteFloatParams.alpha = (Float) animation.getAnimatedValue();
                Log.i("value","mWhiteFloatParams.alpha = " +mWhiteFloatParams.alpha);
                windowManager.updateViewLayout(mWhiteFloatView, mWhiteFloatParams);
            }
        });
        alphAnimator.start();
        mLastClickTime = System.currentTimeMillis();
        Log.i("null", "whiteFloatAppearAnim 变亮");
    }
    
    /**
     * change the alpha of white float
     * @param startAlpha
     * @param endAlpha
     */
    private static void whiteFloatAlphaAnim(float startAlpha,float endAlpha){
        if(null != mWhiteFloatView){
            ObjectAnimator alphAnimator = ObjectAnimator.ofFloat(mWhiteFloatView, "alpha", startAlpha,endAlpha).setDuration(600);
            alphAnimator.start();
        }
    }
}
