
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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.quickgestures.view.QuickGesturesAreaView;

/**
 * QuickGestureWindowManager
 * 
 * @author run
 */
public class FloatWindowHelper {
    public static final String QUICK_GESTURE_SETTING_DIALOG_RADIO_FINISH_NOTIFICATION = "quick_gesture_setting_dialog_radio_finish_notification";
    public static final String QUICK_GESTURE_SETTING_DIALOG_RADIO_SLIDE_TIME_SETTING_FINISH_NOTIFICATION = "quick_gesture_setting_dialog_radio_slide_time_setting_finish_notification";
    private static QuickGesturesAreaView mLeftBottomView, mLeftCenterView, mLeftTopView,
            mLeftCenterCenterView;
    private static QuickGesturesAreaView mRightBottomView, mRightCenterView, mRightTopView,
            mRightCenterCenterView;
    private static LayoutParams mLeftBottomParams, mLeftCenterParams, mLeftTopParams,
            mLeftCenterCenterParams;
    private static LayoutParams mRightBottomParams, mRightCenterParams, mRightTopParams,
            mRightCenterCenterParams;
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
    // 左侧中部高度
    private static float mLeftCenterCenterHeight = 400;
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
    private static float mRightCenterCenterHeight = 400;
    // 右上宽度
    private static float mRightTopWidth = 50;
    // 右上高度
    private static float mRightTopHeight = 300;

    // 左下
    /**
     * must call in UI thread
     * 
     * @param context
     */
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
                            if ((moveX > mLeftBottomParams.width / 4 || moveY > mLeftBottomParams.height / 4)
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
                int height = windowManager.getDefaultDisplay().getWidth();
                int width = windowManager.getDefaultDisplay().getHeight();
                mLeftBottomParams = new LayoutParams();
                mLeftBottomParams.width = (int) mLeftBottomWidth;
                mLeftBottomParams.height = (int) mLeftBottomHeight;
                mLeftBottomParams.x = -(width / 2);
                mLeftBottomParams.y = (height / 2);
                mLeftBottomParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
                mLeftBottomParams.format = PixelFormat.RGBA_8888;
                mLeftBottomParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
                
//                mLeftBottomParams.flags =   WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
//                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//                | WindowManager.LayoutParams.FLAG_BLUR_BEHIND; 
                
            }
            windowManager.addView(mLeftBottomView, mLeftBottomParams);
        }
    }

    // 左中
    /**
     * must call in UI thread
     * 
     * @param context
     */
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
                int height = windowManager.getDefaultDisplay().getWidth();
                int width = windowManager.getDefaultDisplay().getHeight();
                mLeftCenterParams = new LayoutParams();
                mLeftCenterParams.width = (int) mLeftCenterWidth;
                mLeftCenterParams.height = (int) mLeftCenterHeight;
                mLeftCenterParams.type = 2002;
                mLeftCenterParams.x = (int) -(width / 2);
                mLeftCenterParams.y = (int) ((height / 2)
                        - ((mLeftCenterHeight / 2) + mLeftBottomHeight) - 10);
                mLeftCenterParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
                mLeftCenterParams.format = PixelFormat.RGBA_8888;
                mLeftCenterParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            }
            windowManager.addView(mLeftCenterView, mLeftCenterParams);
        }
    }

    // ×××××××××××××××××××××××××××××××××××左侧中部×××××××××××××××××××××××
    /**
     * must call in UI thread
     * 
     * @param context
     */
    public static void createFloatLeftCenterCenterWindow(final Context mContext) {
        final WindowManager windowManager = getWindowManager(mContext);
        if (mLeftCenterCenterView == null) {
            mLeftCenterCenterView = new QuickGesturesAreaView(mContext);
            mLeftCenterCenterView.setOnTouchListener(new OnTouchListener() {
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
                            if ((moveX > mLeftCenterCenterParams.width / 4 || moveY > mLeftCenterCenterParams.height / 4)
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

            if (mLeftCenterCenterParams == null) {
                int height = windowManager.getDefaultDisplay().getWidth();
                int width = windowManager.getDefaultDisplay().getHeight();
                mLeftCenterCenterParams = new LayoutParams();
                mLeftCenterCenterParams.width = (int) mLeftCenterWidth;
                mLeftCenterCenterParams.height = (int) mLeftCenterCenterHeight;
                mLeftCenterCenterParams.type = 2002;
                mLeftCenterCenterParams.x = (int) -(width / 2);
                mLeftCenterCenterParams.y = (int) ((height / 2)
                        - ((mLeftCenterHeight / 2) + mLeftBottomHeight) - 10);
                mLeftCenterCenterParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
                mLeftCenterCenterParams.format = PixelFormat.RGBA_8888;
                mLeftCenterCenterParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            }
            windowManager.addView(mLeftCenterCenterView, mLeftCenterCenterParams);
        }
    }

    // 左上××××××××××××××××××××××××××××××××××××××××××××××××
    /**
     * must call in UI thread
     * 
     * @param context
     */
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
                int height = windowManager.getDefaultDisplay().getWidth();
                int width = windowManager.getDefaultDisplay().getHeight();
                mLeftTopParams = new LayoutParams();
                mLeftTopParams.width = (int) mLeftTopWidth;
                mLeftTopParams.height = (int) mLeftTopHeight;
                mLeftTopParams.x = -(width / 2);
                mLeftTopParams.y = (int) ((height / 2) - ((mLeftTopHeight / 2) + mLeftBottomHeight
                        + mLeftCenterHeight - 10));
                mLeftTopParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
                mLeftTopParams.format = PixelFormat.RGBA_8888;
                mLeftTopParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            }

            windowManager.addView(mLeftTopView, mLeftTopParams);
        }
    }

    // 右下
    /**
     * must call in UI thread
     * 
     * @param context
     */
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
                int height = windowManager.getDefaultDisplay().getWidth();
                int width = windowManager.getDefaultDisplay().getHeight();
                mRightBottomParams = new LayoutParams();
                mRightBottomParams.width = (int) mRightBottomWidth;
                mRightBottomParams.height = (int) mRightBottomHeight;
                mRightBottomParams.x = (width / 2);
                mRightBottomParams.y = (height / 2);
                mRightBottomParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
                mRightBottomParams.format = PixelFormat.RGBA_8888;
                mRightBottomParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            }
            windowManager.addView(mRightBottomView, mRightBottomParams);
        }
    }

    // 右中
    /**
     * must call in UI thread
     * 
     * @param context
     */
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
                int height = windowManager.getDefaultDisplay().getWidth();
                int width = windowManager.getDefaultDisplay().getHeight();
                mRightCenterParams = new LayoutParams();
                mRightCenterParams.width = (int) mRightCenterWidth;
                mRightCenterParams.height = (int) mRightCenterHeight;
                mRightCenterParams.x = (int) (width / 2);
                mRightCenterParams.y = (int) ((height / 2)
                        - ((mLeftCenterHeight / 2) + mLeftBottomHeight) - 10);
                mRightCenterParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
                mRightCenterParams.format = PixelFormat.RGBA_8888;
                mRightCenterParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            }
            windowManager.addView(mRightCenterView, mRightCenterParams);
        }
    }

    // ×××××××××××××××××××××××××右侧中部××××××××××××××××××××××××××××××××××
    /**
     * must call in UI thread
     * 
     * @param context
     */
    public static void createFloatRightCenterCenterWindow(final Context mContext) {
        final WindowManager windowManager = getWindowManager(mContext);
        if (mRightCenterCenterView == null) {
            mRightCenterCenterView = new QuickGesturesAreaView(mContext);
            mRightCenterCenterView.setOnTouchListener(new OnTouchListener() {
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
                            if ((moveX > mRightCenterCenterParams.width / 4 || moveY > mRightCenterCenterParams.height / 4)
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
            if (mRightCenterCenterParams == null) {
                int height = windowManager.getDefaultDisplay().getWidth();
                int width = windowManager.getDefaultDisplay().getHeight();
                mRightCenterCenterParams = new LayoutParams();
                mRightCenterCenterParams.width = (int) mRightCenterWidth;
                mRightCenterCenterParams.height = (int) mRightCenterCenterHeight;
                mRightCenterCenterParams.x = (int) (width / 2);
                mRightCenterCenterParams.y = (int) ((height / 2)
                        - ((mLeftCenterHeight / 2) + mLeftBottomHeight) - 10);
                mRightCenterCenterParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
                mRightCenterCenterParams.format = PixelFormat.RGBA_8888;
                mRightCenterCenterParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            }
            windowManager.addView(mRightCenterCenterView, mRightCenterCenterParams);
        }
    }

    // 右上×××××××××××××××××××××××××××××××××××××××××××××××××××
    /**
     * must call in UI thread
     * 
     * @param context
     */
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
                int height = windowManager.getDefaultDisplay().getWidth();
                int width = windowManager.getDefaultDisplay().getHeight();
                mRightTopParams = new LayoutParams();
                mRightTopParams.width = (int) mRightTopWidth;
                mRightTopParams.height = (int) mRightTopHeight;
                mRightTopParams.x = (width / 2);
                mRightTopParams.y = (int) ((height / 2) - ((mLeftTopHeight / 2) + mLeftBottomHeight
                        + mLeftCenterHeight - 10));
                mRightTopParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
                mRightTopParams.format = PixelFormat.RGBA_8888;
                mRightTopParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
            }
            windowManager.addView(mRightTopView, mRightTopParams);
        }
    }

    /**
     * 移除悬浮窗口 must call in UI thread
     * 
     * @param context
     */
    public static void removeSwipWindow(Context context, int flag) {
        WindowManager windowManager = getWindowManager(context);
        if (flag == 1) {
            // 左下
            if (mLeftBottomView != null) {
                windowManager.removeView(mLeftBottomView);
                mLeftBottomView = null;
            } else {
                Log.e("##########", "左下为空");
            }
        } else if (flag == 2) {
            // 左中
            if (mLeftCenterView != null) {
                windowManager.removeView(mLeftCenterView);
                mLeftCenterView = null;
            } else {
                Log.e("##########", "左中为空");
            }
        } else if (flag == 3) {
            // 左上
            if (mLeftTopView != null) {
                windowManager.removeView(mLeftTopView);
                mLeftTopView = null;
            } else {
                Log.e("##########", "左上为空");
            }
        } else if (flag == 4) {

        } else if (flag == -1) {
            // 右下
            if (mRightBottomView != null) {
                windowManager.removeView(mRightBottomView);
                mRightBottomView = null;
            } else {
                Log.e("##########", "右下为空");
            }
        } else if (flag == -2) {
            // 右中
            if (mRightCenterView != null) {
                windowManager.removeView(mRightCenterView);
                mRightCenterView = null;
            } else {
                Log.e("##########", "右中为空");
            }
        } else if (flag == -3) {
            // 右上
            if (mRightTopView != null) {
                windowManager.removeView(mRightTopView);
                mRightTopView = null;
            } else {
                Log.e("##########", "右上为空");
            }
        } else if (flag == -4) {

        }
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

    /**
     * must call in UI thread
     * 
     * @param context
     */
    public static void createFloatWindow(Context context) {
        if (!FloatWindowHelper.isLeftBottomShowing()
                || !FloatWindowHelper.isLeftCenterShowing()
                || !FloatWindowHelper.isLeftTopShowing()
                || !FloatWindowHelper.isRightBottomShowing()
                || !FloatWindowHelper.isRightCenterShowing()
                || !FloatWindowHelper.isRightTopShowing()) {
            AppMasterPreference pre = AppMasterPreference
                    .getInstance(context);
            // 透明悬浮窗
            // 左侧底部
            if (pre.getDialogRadioLeftBottom()) {
                FloatWindowHelper
                        .createFloatLeftBottomWindow(context);
                FloatWindowHelper
                        .createFloatLeftCenterWindow(context);
                FloatWindowHelper
                        .createFloatLeftTopWindow(context);
            } else {
                FloatWindowHelper.removeSwipWindow(context, 1);
                FloatWindowHelper.removeSwipWindow(context, 2);
                FloatWindowHelper.removeSwipWindow(context, 3);
            }
            // 右侧底部
            if (pre.getDialogRadioRightBottom()) {
                FloatWindowHelper
                        .createFloatRightBottomWindow(context);
                FloatWindowHelper
                        .createFloatRightCenterWindow(context);
                FloatWindowHelper
                        .createFloatRightTopWindow(context);
            } else {
                FloatWindowHelper.removeSwipWindow(context, -1);
                FloatWindowHelper.removeSwipWindow(context, -2);
                FloatWindowHelper.removeSwipWindow(context, -3);
            }
            // 左侧中部
            if (pre.getDialogRadioLeftCenter()) {
                FloatWindowHelper.removeSwipWindow(context, 2);
                FloatWindowHelper.removeSwipWindow(context, 3);
                FloatWindowHelper.createFloatLeftCenterCenterWindow(context);
            } else {

            }
            // 右侧中部
            if (pre.getDialogRadioRightCenter()) {
                FloatWindowHelper.removeSwipWindow(context, -2);
                FloatWindowHelper.removeSwipWindow(context, -3);
                FloatWindowHelper.createFloatRightCenterCenterWindow(context);
            } else {

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
