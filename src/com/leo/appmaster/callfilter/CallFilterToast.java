package com.leo.appmaster.callfilter;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.animation.AnimationListenerAdapter;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.WifiSecurityManager;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.wifiSecurity.WifiSecurityActivity;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorListenerAdapter;
import com.leo.tools.animator.AnimatorSet;
import com.leo.tools.animator.ObjectAnimator;


public class CallFilterToast {
    private static final int API_LEVEL_19 = 19;
    public static final int BLACK_LIST_TYPE = 0;
    public static final int FILTER_TYPE = 1;
    public static final int TYPE_ANNOY = 1;
    public static final int TYPE_AD = 2;
    public static final int TYPE_CHEAT = 3;

    public static TextView mTextOne, mTextTwo, mTextThree;
    private static Context mContext;

    private static Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {

            }
        }
    };

    public static CallFilterToast makeText(final Context context, String title, int peopleNum, int toastType, int filterType) {
        CallFilterToast result = new CallFilterToast(context);
        mContext = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.call_filter_toast, null);

        mTextOne = (TextView) view.findViewById(R.id.tv_number);
        mTextTwo = (TextView) view.findViewById(R.id.tv_desc);
        mTextThree = (TextView) view.findViewById(R.id.tv_desc2);

        mTextOne.setText(title);
        if (toastType == BLACK_LIST_TYPE) {
            String partOne = context.getString(R.string.number_toast_text_black, peopleNum);
            String times1 = context.getString(R.string.number_toast_color,
                    context.getString(R.string.call_filter_black_list_tab));
            mTextTwo.setText(partOne);
            mTextThree.setText(Html.fromHtml(times1));
        } else {
            String partOne = context.getString(R.string.number_toast_text_mark, peopleNum);
            String times2;
            if (filterType == TYPE_ANNOY) {
                times2 = context.getString(R.string.number_toast_color,
                        context.getString(R.string.filter_number_type_saorao));
            } else if (filterType == TYPE_AD) {
                times2 = context.getString(R.string.number_toast_color,
                        context.getString(R.string.filter_number_type_ad));
            } else {
                times2 = context.getString(R.string.number_toast_color,
                        context.getString(R.string.filter_number_type_zhapian));
            }
            mTextTwo.setText(partOne);
            mTextThree.setText(Html.fromHtml(times2));
        }

        result.mNextView = view;
        return result;
    }

    public static final int LENGTH_SHORT = 2000;
    public static final int LENGTH_LONG = 3500;

    private final Handler mHandler = new Handler();
    private int mDuration = LENGTH_LONG;
    private int mGravity = Gravity.CENTER;
    private int mX, mY;
    private float mHorizontalMargin;
    private float mVerticalMargin;
    private static View mView;
    private View mNextView;

    private static WindowManager mWM;
    private final WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();


    public CallFilterToast(Context context) {
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
        setGravity(Gravity.CENTER, 0, 0);
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
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;

        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

        params.format = PixelFormat.TRANSLUCENT;
        params.windowAnimations = android.R.style.Animation_Toast;
        int currSDK_INT = Build.VERSION.SDK_INT;
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
            handleHide();
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
            if (mView.getParent() != null) {
                mWM.removeView(mView);
            }
            mWM.addView(mView, mParams);
        }
    }

    private static void handleHide() {
        if (mView != null) {
            if (mView.getParent() != null) {
                mWM.removeView(mView);
            }
            mView = null;
        }
    }
}