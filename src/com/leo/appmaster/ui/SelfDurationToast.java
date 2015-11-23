package com.leo.appmaster.ui;

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


public class SelfDurationToast {
    public static final int SHOWNAME = 1;
    private static final int API_LEVEL_19 = 19;
    public static ImageView realLoading;
    public static TextView mTextOne, mTextTwo;
    public static String wifiName;
    private static Context mContext;
    private static View contentView, loadingView;
    private static ImageView mArrow;
    private static View mIconView;

    private static android.os.Handler handler = new android.os.Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SHOWNAME:
                    WifiSecurityManager wsm = (WifiSecurityManager)
                            MgrContext.getManager(MgrContext.MGR_WIFI_SECURITY);
                    boolean isWifiOpen = wsm.isWifiOpen();
                    //connect wifi?
                    boolean isSelectWifi = wsm.getIsWifi();
                    if (isWifiOpen && isSelectWifi) {
                        showAnimation();
                        int wifiSate = (Integer) msg.obj;
                        if (wifiSate == 2) {
                            String a1 = mContext.getString(R.string.change_wifi_toast_title, wifiName);
                            String b1 = mContext.getString(R.string.change_wifi_toast_unsafe);
                            mTextOne.setText(a1);
                            mTextTwo.setText(b1);
                            mTextTwo.setTextColor(mContext.getResources().getColor(R.color.wifi_loading_icon_unsafe));
                            mArrow.setImageResource(R.drawable.wifi_toast_redarrow);
                        } else {
                            String a2 = mContext.getString(R.string.change_wifi_toast_title, wifiName);
                            String b2 = mContext.getString(R.string.change_wifi_toast_safe);
                            mTextOne.setText(a2);
                            mTextTwo.setText(b2);
                            mTextTwo.setTextColor(mContext.getResources().getColor(R.color.wifi_loading_icon_safe));
                            mArrow.setImageResource(R.drawable.wifi_toast_bluearrow);
                        }
                    } else {
                        loadingView.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    };

    private static void showAnimation() {
        circleMissingAnimation();
        mIconView.setVisibility(View.VISIBLE);
        contentView.setVisibility(View.VISIBLE);

        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim_left_to_right);
        animation.setAnimationListener(new AnimationListenerAdapter() {
            @Override
            public void onAnimationEnd(Animation animation) {
                showResult();
            }
        });
        contentView.startAnimation(animation);

    }

    private static void circleMissingAnimation() {
        realLoading.clearAnimation();
        loadingView.setVisibility(View.GONE);
    }

    private static void showResult() {
        ObjectAnimator animX = ObjectAnimator.ofFloat(mIconView,
                "scaleX", 1f, 0.9f);
        ObjectAnimator animY = ObjectAnimator.ofFloat(mIconView,
                "scaleY", 1f, 0.9f);
        ObjectAnimator animAlpha1 = ObjectAnimator.ofFloat(mArrow,
                "alpha", 0f, 1f);
        ObjectAnimator animAlpha2 = ObjectAnimator.ofFloat(mTextOne,
                "alpha", 0f, 1f);
        ObjectAnimator animAlpha3 = ObjectAnimator.ofFloat(mTextTwo,
                "alpha", 0f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mIconView.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
                mArrow.setVisibility(View.VISIBLE);
                mTextOne.setVisibility(View.VISIBLE);
                mTextTwo.setVisibility(View.VISIBLE);
            }
        });
        set.setDuration(250);
        set.play(animAlpha1).with(animAlpha2);
        set.play(animAlpha2).with(animX);
        set.play(animX).with(animY);
        set.play(animY).with(animAlpha3);
        set.start();
    }

    public static SelfDurationToast makeText(final Context context, String text, int duration, final int wifiState) {
        SelfDurationToast result = new SelfDurationToast(context);

        SDKWrapper.addEvent(context,
                SDKWrapper.P1, "wifi_scan", "wifi_cnts_toast");
        mContext = context;
        wifiName = text;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.wifi_change_toast, null);
        view.setBackgroundResource(R.color.transparent);

        mArrow = (ImageView) view.findViewById(R.id.iv_arrow);
        realLoading = (ImageView) view.findViewById(R.id.loding_iv);
        realLoading.setImageResource(R.drawable.real_loading);
        Animation loadingAnimation = AnimationUtils.
                loadAnimation(context, R.anim.loading_animation);
        realLoading.setAnimation(loadingAnimation);

        mIconView = view.findViewById(R.id.wifi_result_icon);
        final int sendWifiState = wifiState;
        contentView = view.findViewById(R.id.wifi_result_content);
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LockManager mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
                mLockManager.filterPackage(mContext.getPackageName(), 1000);
                Intent wifiIntent = new Intent(mContext, WifiSecurityActivity.class);
                wifiIntent.putExtra("from", "toast");
                wifiIntent.putExtra("wifistate", sendWifiState);
                wifiIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                try {
                    mContext.startActivity(wifiIntent);
                    handleHide();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        loadingView = view.findViewById(R.id.loading_content);
        mTextOne = (TextView) view.findViewById(R.id.tv_clean_rocket);
        mTextTwo = (TextView) view.findViewById(R.id.tv_clean_rocket_two);
        result.mNextView = view;
        result.mDuration = duration;

        readyShow(wifiState);

        return result;
    }

    private static void readyShow(final int wifiState) {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = SHOWNAME;
                msg.obj = wifiState;
                handler.sendMessageDelayed(msg, 2500);
            }
        });
    }

    public static final int LENGTH_SHORT = 2000;
    public static final int LENGTH_LONG = 3500;

    private final Handler mHandler = new Handler();
    private int mDuration = LENGTH_SHORT;
    private int mGravity = Gravity.CENTER;
    private int mX, mY;
    private float mHorizontalMargin;
    private float mVerticalMargin;
    private static View mView;
    private View mNextView;

    private static WindowManager mWM;
    private final WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();


    public SelfDurationToast(Context context) {
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
     *
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
        setGravity(Gravity.LEFT | Gravity.TOP, DipPixelUtil.dip2px(mContext, 10), DipPixelUtil.dip2px(mContext, 40));
        mHandler.post(mShow);
        if (mDuration > 0) {
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