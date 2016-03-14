package com.leo.appmaster.callfilter;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;


public class CallFilterToast {
    private static final String TAG = "CallFilterToast";
    private static final int API_LEVEL_19 = 19;
    public static final int BLACK_LIST_TYPE = 0;
    public static final int FILTER_TYPE = 1;
    public static final int TYPE_ANNOY = 1;
    public static final int TYPE_CHEAT = 2;
    public static final int TYPE_AD = 3;
    public static final String TOAST_Y = "record_toast_y";

    public static TextView mTextOne, mTextTwo, mTextThree;
    public static View mClose;
    public static View mContent;
    private static Context mContext;
    private int mToastY = 0;
    private static int currSDK_INT = Build.VERSION.SDK_INT;


    public static CallFilterToast makeText(final Context context, String title, int peopleNum, int toastType, int filterType) {
        final CallFilterToast result = new CallFilterToast(context);
        mContext = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.call_filter_toast, null);


        mTextOne = (TextView) view.findViewById(R.id.tv_number);
        mTextTwo = (TextView) view.findViewById(R.id.tv_desc);
        mTextThree = (TextView) view.findViewById(R.id.tv_desc2);

        mClose = view.findViewById(R.id.filter_close);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                result.hide();

            }
        });
        mContent = view.findViewById(R.id.filter_content);

        mTextOne.setText(title);
        if (toastType == BLACK_LIST_TYPE) {
            String partOne = context.getString(R.string.number_toast_text_black, peopleNum);
            String times1 = context.getString(R.string.number_toast_color,
                    context.getString(R.string.call_filter_black_list_tab));
            mTextTwo.setText(partOne);
            mTextThree.setText(Html.fromHtml(times1));
            mContent.setBackgroundResource(R.drawable.filter_toast_bg);
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
            mContent.setBackgroundResource(R.drawable.filter_toast_bg_m);
        }

        view.setOnTouchListener(new View.OnTouchListener() {
            // 定义手指的初始化位置
            int startY;
            int startViewY;

            @Override
            public boolean onTouch(View view, MotionEvent event) {

                if (view == null) {
                    return false;
                } else {
                    if (currSDK_INT >= API_LEVEL_19 && !view.isAttachedToWindow()) {
                        return false;
                    }
                }


                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:// 手指按下屏幕
                        startY = (int) event.getRawY();
                        startViewY = mParams.y;
                        LeoLog.d("testToastMove", "手指摸到控件 y : " + startY);
                        LeoLog.d("testToastMove", "view place y : " + startViewY);

                        break;
                    case MotionEvent.ACTION_MOVE:// 手指在屏幕上移动

                        int newY = (int) event.getRawY();
                        int dy = newY - startY;

                        LeoLog.d("testToastMove", "startY : " + startY);
                        LeoLog.d("testToastMove", "newY : " + newY);
                        LeoLog.d("testToastMove", "move Y : " + dy);
                        LeoLog.d("testToastMove", "----------------------");

                        startViewY += dy;

                        if (mParams.y < 0) {
                            startViewY = 0;
                        }
                        if (mParams.y > (mWM.getDefaultDisplay().getHeight() - view
                                .getHeight())) {
                            startViewY = (mWM.getDefaultDisplay().getHeight() - view
                                    .getHeight());
                        }

                        mParams.y = startViewY;

                        try {
                            mWM.updateViewLayout(view, mParams);
                        } catch (Exception e) {
                            LeoLog.e(TAG, "updateViewLayout e: " + e.getMessage());
                        }

                        // 重新初始化手指的开始结束位置。
                        startY += dy;

                        break;
                    case MotionEvent.ACTION_UP:// 手指离开屏幕一瞬间
                        // 记录控件距离屏幕左上角的坐标
                        LeoLog.d("testToastMove", "finger up");
                        LeoLog.d("testToastMove", "view place y : " + mParams.y);
                        PreferenceTable.getInstance().putInt(TOAST_Y, mParams.y);
                        break;
                }

                return false;
            }
        });

        result.mNextView = view;

        setViewTouchble();
        return result;
    }

    private static void setViewTouchble() {

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
    private static View mNextView;

    private static WindowManager mWM;
    private static WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();


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

        int marginTop = 120;
        setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, DipPixelUtil.dip2px(mContext, marginTop));
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
        if (currSDK_INT < API_LEVEL_19) {
            params.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_TOAST;
        }

        mWM = (WindowManager) context.getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);

        mToastY = PreferenceTable.getInstance().getInt(TOAST_Y, 0);
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
            if (mToastY != 0) {
                mParams.y = mToastY;
            } else {
                mParams.y = mY;
            }
            mParams.verticalMargin = mVerticalMargin;
            mParams.horizontalMargin = mHorizontalMargin;
            try {
                if (mView.getParent() != null) {
                    mWM.removeView(mView);
                }
                mWM.addView(mView, mParams);
            } catch (Exception e) {
            }
        }
    }

    private static void handleHide() {
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