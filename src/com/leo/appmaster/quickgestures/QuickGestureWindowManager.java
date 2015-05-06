
package com.leo.appmaster.quickgestures;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;

/**
 * QuickGestureWindowManager
 * 
 * @author run
 */
public class QuickGestureWindowManager {
    public static final String QUICK_GESTURE_SETTING_DIALOG_RADIO_FINISH_NOTIFICATION = "quick_gesture_setting_dialog_radio_finish_notification";
    public static final String QUICK_GESTURE_SETTING_DIALOG_RADIO_SLIDE_TIME_SETTING_FINISH_NOTIFICATION = "quick_gesture_setting_dialog_radio_slide_time_setting_finish_notification";
    private static QuickGesturesAreaView mLeftBottomView, mLeftCenterView, mLeftTopView;
    private static QuickGesturesAreaView mRightBottomView, mRightCenterView, mRightTopView;
    private static LayoutParams mLeftBottomParams, mLeftCenterParams, mLeftTopParams;
    private static LayoutParams mRightBottomParams, mRightCenterParams, mRightTopParams;
    private static WindowManager mWindowManager;
    private static float startX;
    private static float startY;
    // private static WindowManager windowManager;
    private static boolean isMoveIng = false;
    private static View mContent;
    public static boolean isFanShowing = false;
    // 左下宽度
    private static float mLeftBottomWidth = 200;
    // 左下高度
    private static float mLeftBottomHeight = 100;
    // 左中宽度
    private static float mLeftCenterWidth = 100;
    // 左中高度
    private static float mLeftCenterHeight = 200;
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
    // 右上宽度
    private static float mRightTopWidth = 50;
    // 右上高度
    private static float mRightTopHeight = 300;

    // 左下
    public static void createFloatLeftBottomWindow(final Context mContext) {
        final WindowManager windowManager = getWindowManager(mContext);
        if (mLeftBottomView == null) {
            mLeftBottomView = new QuickGesturesAreaView(mContext);
            mLeftBottomView.setOnTouchListener(new OnTouchListener() {
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
                            if ((moveX > mLeftBottomParams.width / 2 || moveY > mLeftBottomParams.height / 4)
                                    && !isMoveIng) {
                                isMoveIng = true;
                                removeSwipWindow(mContext, 1);
                                Toast.makeText(mContext, "碰到我的区域了！", Toast.LENGTH_SHORT).show();
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

            if (mLeftBottomParams == null) {
                WindowManager manager = (WindowManager) mContext
                        .getSystemService(Context.WINDOW_SERVICE);
                Display display = manager.getDefaultDisplay();
                int height = display.getHeight();
                int width = display.getWidth();
                mLeftBottomParams = new LayoutParams();
                mLeftBottomParams.width = (int) mLeftBottomWidth;
                mLeftBottomParams.height = (int) mLeftBottomHeight;
                // mLeftBottomParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
                mLeftBottomParams.x = -(width / 2);
                mLeftBottomParams.y = (height / 2);
                mLeftBottomParams.type = LayoutParams.FIRST_SYSTEM_WINDOW + 3;
                mLeftBottomParams.format = PixelFormat.RGBA_8888;
                mLeftBottomParams.flags =
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
            }
            windowManager.addView(mLeftBottomView, mLeftBottomParams);
        }
    }

    // 左中
    public static void createFloatLeftCenterWindow(final Context mContext) {
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
                            startX = event.getRawX();
                            startY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float moveX = Math.abs(startX - event.getRawX());
                            float moveY = Math.abs(startY - event.getRawY());
                            if ((moveX > mLeftCenterParams.width / 4 || moveY > mLeftCenterParams.height / 4)
                                    && !isMoveIng) {
                                isMoveIng = true;
                                removeSwipWindow(mContext, 2);
                                Toast.makeText(mContext, "碰到我的区域了！", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            if (Math.abs(startX - event.getRawX()) < 5
                                    || Math.abs(startY - event.getRawY()) < 5) {
                                removeSwipWindow(mContext, 2);
                            }
                            break;
                    }
                    return false;
                }
            });

            if (mLeftCenterParams == null) {
                WindowManager manager = (WindowManager) mContext
                        .getSystemService(Context.WINDOW_SERVICE);
                Display display = manager.getDefaultDisplay();
                float height = display.getHeight();
                float width = display.getWidth();
                mLeftCenterParams = new LayoutParams();
                mLeftCenterParams.width = (int) mLeftCenterWidth;
                mLeftCenterParams.height = (int) mLeftCenterHeight;
                mLeftCenterParams.type = 2002;
                mLeftCenterParams.x = (int) -(width / 2);
                mLeftCenterParams.y = (int) ((height / 2)
                        - ((mLeftCenterHeight / 2) + mLeftBottomHeight) - 10);
                mLeftCenterParams.type = LayoutParams.FIRST_SYSTEM_WINDOW + 3;
                mLeftCenterParams.format = PixelFormat.RGBA_8888;
                mLeftCenterParams.flags =
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
            }
            windowManager.addView(mLeftCenterView, mLeftCenterParams);
        }
    }

    // 左上
    public static void createFloatLeftTopWindow(final Context mContext) {
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
                            startX = event.getRawX();
                            startY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float moveX = Math.abs(startX - event.getRawX());
                            float moveY = Math.abs(startY - event.getRawY());
                            if ((moveX > mLeftTopParams.width / 4 || moveY > mLeftTopParams.height / 4)
                                    && !isMoveIng) {
                                isMoveIng = true;
                                removeSwipWindow(mContext, 3);
                                Toast.makeText(mContext, "碰到我的区域了！", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            if (Math.abs(startX - event.getRawX()) < 5
                                    || Math.abs(startY - event.getRawY()) < 5) {
                                removeSwipWindow(mContext, 3);
                            }
                            break;
                    }
                    return false;
                }
            });

            if (mLeftTopParams == null) {
                WindowManager manager = (WindowManager) mContext
                        .getSystemService(Context.WINDOW_SERVICE);
                Display display = manager.getDefaultDisplay();
                int height = display.getHeight();
                int width = display.getWidth();
                mLeftTopParams = new LayoutParams();
                mLeftTopParams.width = (int) mLeftTopWidth;
                mLeftTopParams.height = (int) mLeftTopHeight;
                mLeftTopParams.x = -(width / 2);
                mLeftTopParams.y = (int) ((height / 2) - ((mLeftTopHeight / 2) + mLeftBottomHeight
                        + mLeftCenterHeight - 10));
                mLeftTopParams.type = LayoutParams.FIRST_SYSTEM_WINDOW + 3;
                mLeftTopParams.format = PixelFormat.RGBA_8888;
                mLeftTopParams.flags =
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
            }

            windowManager.addView(mLeftTopView, mLeftTopParams);
        }
    }

    // 右下
    public static void createFloatRightBottomWindow(final Context mContext) {
        final WindowManager windowManager = getWindowManager(mContext);
        if (mRightBottomView == null) {
            mRightBottomView = new QuickGesturesAreaView(mContext);
            mRightBottomView.setOnTouchListener(new OnTouchListener() {
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
                            if ((moveX > mRightBottomParams.width / 4 || moveY > mRightBottomParams.height / 4)
                                    && !isMoveIng) {
                                isMoveIng = true;
                                removeSwipWindow(mContext, -1);
                                Toast.makeText(mContext, "碰到我的区域了！", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            if (Math.abs(startX - event.getRawX()) < 5
                                    || Math.abs(startY - event.getRawY()) < 5) {
                                removeSwipWindow(mContext, -1);
                            }
                            break;
                    }
                    return false;
                }
            });
            if (mRightBottomParams == null) {
                WindowManager manager = (WindowManager) mContext
                        .getSystemService(Context.WINDOW_SERVICE);
                Display display = manager.getDefaultDisplay();
                int height = display.getHeight();
                int width = display.getWidth();
                mRightBottomParams = new LayoutParams();
                mRightBottomParams.width = (int) mRightBottomWidth;
                mRightBottomParams.height = (int) mRightBottomHeight;
                mRightBottomParams.x = (width / 2);
                mRightBottomParams.y = (height / 2);
                mRightBottomParams.type = LayoutParams.FIRST_SYSTEM_WINDOW + 3;
                mRightBottomParams.format = PixelFormat.RGBA_8888;
                mRightBottomParams.flags =
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
            }
            windowManager.addView(mRightBottomView, mRightBottomParams);
        }
    }

    // 右中
    public static void createFloatRightCenterWindow(final Context mContext) {
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
                            startX = event.getRawX();
                            startY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float moveX = Math.abs(startX - event.getRawX());
                            float moveY = Math.abs(startY - event.getRawY());
                            if ((moveX > mRightCenterParams.width / 4 || moveY > mRightCenterParams.height / 4)
                                    && !isMoveIng) {
                                isMoveIng = true;
                                removeSwipWindow(mContext, -2);
                                Toast.makeText(mContext, "碰到我的区域了！", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            if (Math.abs(startX - event.getRawX()) < 5
                                    || Math.abs(startY - event.getRawY()) < 5) {
                                removeSwipWindow(mContext, -2);
                            }
                            break;
                    }
                    return false;
                }
            });
            if (mRightCenterParams == null) {
                WindowManager manager = (WindowManager) mContext
                        .getSystemService(Context.WINDOW_SERVICE);
                Display display = manager.getDefaultDisplay();
                int height = display.getHeight();
                int width = display.getWidth();
                mRightCenterParams = new LayoutParams();
                mRightCenterParams.width = (int) mRightCenterWidth;
                mRightCenterParams.height = (int) mRightCenterHeight;
                mRightCenterParams.x = (int) (width / 2);
                mRightCenterParams.y = (int) ((height / 2)
                        - ((mLeftCenterHeight / 2) + mLeftBottomHeight) - 10);
                mRightCenterParams.type = LayoutParams.FIRST_SYSTEM_WINDOW + 3;
                mRightCenterParams.format = PixelFormat.RGBA_8888;
                mRightCenterParams.flags =
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
            }
            windowManager.addView(mRightCenterView, mRightCenterParams);
        }
    }

    // 右上
    public static void createFloatRightTopWindow(final Context mContext) {
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
                            startX = event.getRawX();
                            startY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float moveX = Math.abs(startX - event.getRawX());
                            float moveY = Math.abs(startY - event.getRawY());
                            if ((moveX > mRightTopParams.width / 4 || moveY > mRightTopParams.height / 4)
                                    && !isMoveIng) {
                                isMoveIng = true;
                                removeSwipWindow(mContext, -3);
                                Toast.makeText(mContext, "碰到我的区域了！", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isMoveIng = false;
                            if (Math.abs(startX - event.getRawX()) < 5
                                    || Math.abs(startY - event.getRawY()) < 5) {
                                removeSwipWindow(mContext, -3);
                            }
                            break;
                    }
                    return false;
                }
            });
            if (mRightTopParams == null) {
                WindowManager manager = (WindowManager) mContext
                        .getSystemService(Context.WINDOW_SERVICE);
                Display display = manager.getDefaultDisplay();
                int height = display.getHeight();
                int width = display.getWidth();
                mRightTopParams = new LayoutParams();
                mRightTopParams.width = (int) mRightTopWidth;
                mRightTopParams.height = (int) mRightTopHeight;
                mRightTopParams.x = (width / 2);
                mRightTopParams.y = (int) ((height / 2) - ((mLeftTopHeight / 2) + mLeftBottomHeight
                        + mLeftCenterHeight - 10));
                mRightTopParams.type = LayoutParams.FIRST_SYSTEM_WINDOW + 3;
                mRightTopParams.format = PixelFormat.RGBA_8888;
                mRightTopParams.flags =
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
            }
            windowManager.addView(mRightTopView, mRightTopParams);
        }
    }

    // 移除悬浮窗口
    public static void removeSwipWindow(Context context, int flag) {
        WindowManager windowManager = getWindowManager(context);
        if (flag == 1) {
            // 左下
            if (mLeftBottomView != null) {
                windowManager.removeView(mLeftBottomView);
                mLeftBottomView = null;
            }
        } else if (flag == 2) {
            // 左中
            if (mLeftCenterView != null) {
                windowManager.removeView(mLeftCenterView);
                mLeftCenterView = null;
            }
        } else if (flag == 3) {
            // 左上
            if (mLeftTopView != null) {
                windowManager.removeView(mLeftTopView);
                mLeftTopView = null;
            }
        } else if (flag == -1) {
            // 右下
            if (mRightBottomView != null) {
                windowManager.removeView(mRightBottomView);
                mRightBottomView = null;
            }
        } else if (flag == -2) {
            // 右中
            if (mRightCenterView != null) {
                windowManager.removeView(mRightCenterView);
                mRightCenterView = null;
            }
        } else if (flag == -3) {
            // 右上
            if (mRightTopView != null) {
                windowManager.removeView(mRightTopView);
                mRightTopView = null;
            }
        }
    }

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

    public static boolean isRightBottomShowing() {
        return mRightBottomView != null;
    }

    public static boolean isRightCenterShowing() {
        return mRightCenterView != null;
    }

    public static boolean isRightTopShowing() {
        return mRightTopView != null;
    }

    public static boolean isFanShowing() {
        return isFanShowing;
    }

    private static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

    public static void createFloatWindow(Handler handler, final Context context) {
        if (!QuickGestureWindowManager.isLeftBottomShowing()
                || !QuickGestureWindowManager.isLeftCenterShowing()
                || !QuickGestureWindowManager.isLeftTopShowing()
                || !QuickGestureWindowManager.isRightBottomShowing()
                || !QuickGestureWindowManager.isRightCenterShowing()
                || !QuickGestureWindowManager.isRightTopShowing()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    AppMasterPreference pre = AppMasterPreference
                            .getInstance(context);
                    // 透明悬浮窗
                    // 左
                    if (pre.getDialogRadioLeftBottom()) {
                        QuickGestureWindowManager
                                .createFloatLeftBottomWindow(context);
                        QuickGestureWindowManager
                                .createFloatLeftCenterWindow(context);
                        QuickGestureWindowManager
                                .createFloatLeftTopWindow(context);
                    } else {
                        QuickGestureWindowManager.removeSwipWindow(context, 1);
                        QuickGestureWindowManager.removeSwipWindow(context, 2);
                        QuickGestureWindowManager.removeSwipWindow(context, 3);
                    }
                    // 右
                    if (pre.getDialogRadioRightBottom()) {
                        QuickGestureWindowManager
                                .createFloatRightBottomWindow(context);
                        QuickGestureWindowManager
                                .createFloatRightCenterWindow(context);
                        QuickGestureWindowManager
                                .createFloatRightTopWindow(context);
                    } else {
                        QuickGestureWindowManager.removeSwipWindow(context, -1);
                        QuickGestureWindowManager.removeSwipWindow(context, -2);
                        QuickGestureWindowManager.removeSwipWindow(context, -3);
                    }
                }
            });
        }
    }

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
            if (mRightTopView != null) {
                mRightTopView
                        .setBackgroundResource(R.color.quick_gesture_switch_setting_hidden_color);
            }
        }
    }

}
