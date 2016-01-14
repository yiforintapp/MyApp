package com.leo.appmaster.battery;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorListenerAdapter;
import com.leo.tools.animator.ObjectAnimator;


public class BatterProtectView {
    private static final String TAG = "CallFilterToast";
    private static final int API_LEVEL_19 = 19;

    private static Context mContext;
    private static int currSDK_INT = Build.VERSION.SDK_INT;
    private static BatteryMainViewLayout view;

    private boolean mIsCharing;
    private int mBatteryLevel;
    private String mTime;

    private static Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {

            }
        }
    };


    public static BatterProtectView makeText(final Context context) {
        final BatterProtectView result = new BatterProtectView(context);
        mContext = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = (BatteryMainViewLayout)
                inflater.inflate(R.layout.activity_battery_view, null);
        result.mNextView = view;
        return result;
    }

    public void setBatteryStatus(boolean isCharing) {
        mIsCharing = isCharing;
    }

    public void setBatteryLevel(int level) {
        mBatteryLevel = level;
    }

    public void setBatteryTime(String text) {
        mTime = text;
    }


    public static final int LENGTH_SHORT = 2000;
    public static final int LENGTH_LONG = 20000;

    private final Handler mHandler = new Handler();
    private int mDuration = LENGTH_LONG;
    private int mGravity = Gravity.CENTER;
    private int mX, mY;
    private float mHorizontalMargin;
    private float mVerticalMargin;
    private static View mView;
    private static View mNextView;

    private static WindowManager mWM;
    private static WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();

    public BatterProtectView(Context context) {
        init(context);
    }

    /**
     * Set the view to show.
     *
     * @see #getView
     */
    public void setView(View view) {
        mNextView = view;
    }

    /**
     * Return the view.
     *
     * @see #setView
     */
    public View getView() {
        return mNextView;
    }

    /**
     * Set how long to show the view for.
     *
     * @see #LENGTH_SHORT
     * @see #LENGTH_LONG
     */
    public void setDuration(int duration) {
        mDuration = duration;
    }

    /**
     * Return the duration.
     *
     * @see #setDuration
     */
    public int getDuration() {
        return mDuration;
    }

    /**
     * Set the margins of the view.
     *
     * @param horizontalMargin The horizontal margin, in percentage of the
     *                         container width, between the container's edges and the
     *                         notification
     * @param verticalMargin   The vertical margin, in percentage of the
     *                         container height, between the container's edges and the
     *                         notification
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
     *
     * @see Gravity
     * @see #getGravity
     */
    public void setGravity(int gravity, int xOffset, int yOffset) {
        mGravity = gravity;
        mX = xOffset;
        mY = yOffset;
    }

    /**
     * Get the location at which the notification should appear on the screen.
     *
     * @see Gravity
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

        setGravity(Gravity.NO_GRAVITY, 0, 0);
        mHandler.post(mShow);
//        if (mDuration > 0) {
//            mHandler.postDelayed(mHide, mDuration);
//        }

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
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;

        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

        params.format = PixelFormat.TRANSLUCENT;
        params.windowAnimations = android.R.style.Animation_Toast;

        if (currSDK_INT < API_LEVEL_19) {
            params.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_TOAST;
        }

        mWM = (WindowManager) context.getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
    }


    private void handleShow() {

        if (mView != mNextView) {

            // remove the old view if necessary
//            handleHide();
            mView = mNextView;
//            mWM = WindowManagerImpl.getDefault();
            final int gravity = mGravity;
            mParams.gravity = gravity;
            if ((gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.FILL_HORIZONTAL) {
                mParams.horizontalWeight = 1.0f;
            }
            if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.FILL_VERTICAL) {
                mParams.verticalWeight = 1.0f;
            }
            mParams.x = mX;
            mParams.y = mY;
            mParams.verticalMargin = mVerticalMargin;
            mParams.horizontalMargin = mHorizontalMargin;

            try {
                if (mView.getParent() != null) {
                    mWM.removeView(mView);
                }
                view.notifyUI(mIsCharing, mBatteryLevel, mTime);
                mWM.addView(mView, mParams);
            } catch (Exception e) {

            }
        }
    }

    public static void handleHide() {
        if (mView != null) {
            try {
                if (mView.getParent() != null) {
                    mWM.removeView(mView);
                }
            } catch (Exception e) {
            }
            mView = null;
        }
    }
}