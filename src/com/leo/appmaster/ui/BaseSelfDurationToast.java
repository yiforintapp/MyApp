package com.leo.appmaster.ui;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.LeoLog;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class BaseSelfDurationToast{
   
    public static BaseSelfDurationToast makeText(Context context, CharSequence text, int duration)
    {
        BaseSelfDurationToast result = new BaseSelfDurationToast(context);
        LinearLayout mLayout=new LinearLayout(context);
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextColor(Color.WHITE);
        tv.setGravity(Gravity.CENTER);
        mLayout.setBackgroundResource(R.drawable.ic_launcher);
        int w=context.getResources().getDisplayMetrics().widthPixels / 2;
        int h=context.getResources().getDisplayMetrics().widthPixels / 10;
        mLayout.addView(tv, w, h);
        result.mNextView = mLayout;
        result.mDuration = duration;
 
        return result;
    }
    
    public static final int LENGTH_SHORT = 2000;
    public static final int LENGTH_LONG = 3500;
    
    private final Handler mHandler = new Handler();  
    private int mDuration=LENGTH_SHORT;
    private int mGravity = Gravity.CENTER;
    private int mX, mY;
    private float mHorizontalMargin;
    private float mVerticalMargin;
    private View mView;
    private View mNextView;
    private boolean mIsShowing = false;
    
    private WindowManager mWM;
    private final WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
    
    
     public BaseSelfDurationToast(Context context) {
            init(context);
        }
     
    /**
     * Set the view to show.
     * @see #getView
     */
    public void setView(View view) {
        mNextView = view;
    }
 
    /**
     * Return the view.
     * @see #setView
     */
    public View getView() {
        return mNextView;
    }
 
    /**
     * Set how long to show the view for.
     * @see #LENGTH_SHORT
     * @see #LENGTH_LONG
     */
    public void setDuration(int duration) {
        mDuration = duration;
    }
 
    /**
     * Return the duration.
     * @see #setDuration
     */
    public int getDuration() {
        return mDuration;
    }
    
    /**
     * Set the margins of the view.
     *
     * @param horizontalMargin The horizontal margin, in percentage of the
     *        container width, between the container's edges and the
     *        notification
     * @param verticalMargin The vertical margin, in percentage of the
     *        container height, between the container's edges and the
     *        notification
     */
    public void setMargin(float horizontalMargin, float verticalMargin) {
        mHorizontalMargin = horizontalMargin;
        mVerticalMargin = verticalMargin;
    }
 
    /**
     * Return the horizontal margin.
     */
    public float getHorizontalMargin() {
        return mHorizontalMargin;
    }
 
    /**
     * Return the vertical margin.
     */
    public float getVerticalMargin() {
        return mVerticalMargin;
    }
 
    /**
     * Set the location at which the notification should appear on the screen.
     * @see android.view.Gravity
     * @see #getGravity
     */
    public void setGravity(int gravity, int xOffset, int yOffset) {
        mGravity = gravity;
        mX = xOffset;
        mY = yOffset;
    }
 
     /**
     * Get the location at which the notification should appear on the screen.
     * @see android.view.Gravity
     * @see #getGravity
     */
    public int getGravity() {
        return mGravity;
    }
    
 
    /**
     * Return the X offset in pixels to apply to the gravity's location.
     */
    public int getXOffset() {
        return mX;
    }
    
    /**
     * Return the Y offset in pixels to apply to the gravity's location.
     */
    public int getYOffset() {
        return mY;
    }
    
    /**
     * schedule handleShow into the right thread
     */
    public void show() {
        mHandler.post(mShow);
        mHandler.removeCallbacks(mHide);
        if(mDuration>0)
        {
            mHandler.postDelayed(mHide, mDuration);
        }
    }
 
    /**
     * schedule handleHide into the right thread
     */
    public void hide() {
        mHandler.post(mHide);
    }
    
    private final Runnable mShow = new Runnable() {
        public void run() {
            handleShow();
        }
    };
 
    private final Runnable mHide = new Runnable() {
        public void run() {
            handleHide();
        }
    };
 
    private void init(Context context) { 
         final WindowManager.LayoutParams params = mParams;
         params.height = WindowManager.LayoutParams.WRAP_CONTENT;
         params.width = WindowManager.LayoutParams.WRAP_CONTENT;
//         params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//         | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
         params.flags =
                 WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
                 WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
         params.format = PixelFormat.TRANSLUCENT;
//         params.windowAnimations = android.R.style.Animation_Toast;
         params.windowAnimations = R.style.toast_getscore;
         params.type = WindowManager.LayoutParams.TYPE_TOAST;
         params.setTitle("Toast");

         mWM = (WindowManager) context.getApplicationContext()
                 .getSystemService(Context.WINDOW_SERVICE);
    }

    public void setMatchParent() {
        mParams.width = WindowManager.LayoutParams.MATCH_PARENT;
    }

    public void setWindowAnimations(int styleId) {
        mParams.windowAnimations = styleId;
    }
    
    private void handleShow() {

        if (mView != mNextView) {
            // remove the old view if necessary
            handleHide();
            mView = mNextView;
//            mWM = WindowManagerImpl.getDefault();
            final int gravity = mGravity;
            mParams.gravity = gravity;
            if ((gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.FILL_HORIZONTAL)
            {
                mParams.horizontalWeight = 1.0f;
            }
            if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.FILL_VERTICAL)
            {
                mParams.verticalWeight = 1.0f;
            }
            mParams.x = mX;
            mParams.y = mY;
            mParams.verticalMargin = mVerticalMargin;
            mParams.horizontalMargin = mHorizontalMargin;
            if (mView.getParent() != null)
            {
                mWM.removeView(mView);
            }
            mWM.addView(mView, mParams);
            mIsShowing = true;
        }
    }

    private void handleHide()
    {
        if (mView != null)
        {
            if (mView.getParent() != null)
            {
                mWM.removeView(mView);
                mIsShowing = false;
            }
            mView = null;
        }
    }

    public boolean isShowing() {
        return mIsShowing;
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            event.startTracking();
//            return true;
//        }
//
//        return false;
//    }
//
//    @Override
//    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
//        return false;
//    }
//
//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
//                && !event.isCanceled()) {
//            return true;
//        }
//        return false;
//    }
//
//    @Override
//    public boolean onKeyMultiple(int keyCode, int count, KeyEvent event) {
//        return false;
//    }
}