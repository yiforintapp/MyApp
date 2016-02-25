
package com.leo.appmaster.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.text.Html;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.manager.MobvistaEngine;
import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.home.DeskProxyActivity;
import com.leo.appmaster.mgr.BatteryManager;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.AdWrapperLayout;
import com.leo.appmaster.ui.ResizableImageView;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.WaveView;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.Utilities;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FailReason;
import com.leo.imageloader.core.ImageLoadingListener;
import com.leo.imageloader.core.ImageScaleType;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorListenerAdapter;
import com.leo.tools.animator.AnimatorSet;
import com.leo.tools.animator.ObjectAnimator;
import com.mobvista.sdk.m.core.entity.Campaign;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class BatteryViewFragment extends BaseFragment implements View.OnTouchListener, BatteryTestViewLayout.ScrollBottomListener, View.OnClickListener {

    private static final String TAG = "BatteryViewFragment";
    private static final int ANIMATION_TIME = 300;
    private static final int MOVE_UP = 1;
    private static final int MOVE_DOWN = 2;
    private static final int LOAD_DONE_INIT_PLACE = 6;
    private static final int RECOMMAND_TYPE_ONE = 1;
    private static final int RECOMMAND_TYPE_TWO = 2;
    private static final int RECOMMAND_TYPE_THREE = 3;

    private static final int AD_TYPE_MSG = 1;
    private static final int SWTIFY_TYPE_MSG = 2;
    private static final int EXTRA_TYPE_MSG = 3;


    public static boolean mShowing = false;
    public static boolean isExpand = false;
    public static boolean mIsExtraLayout = false;

    private View mTimeContent;
    private View mBatteryIcon;
    private View mRemainTimeContent;
    private View mRemainContent;
    private BatteryTestViewLayout mSlideView;

    private TextView mTvLevel;
    private TextView mTvBigTime;
    private TextView mTvSmallLeft;
    private TextView mTvSmallRight;
    private TextView mTvLeftTime;
    //    private TextView mTvTime;
    private SelfScrollView mScrollView;
    private WaveView mBottleWater;
    private TextView mTvHideTime;
    private TextView mTvHideText;
    private View mSettingView;
    private View mArrowMoveContent;
    private ImageView mIvArrowMove;
    private ImageView mIvCancel;
    private View mShowOne;
    private View mShowTwo;
    private View mShowThree;
    private TextView mTvShowOne;
    private TextView mTvShowTwo;
    private TextView mTvShowThree;
    private View mRecommandView;
    private int mRecommandViewHeight;

    private int mCurrentClickType = 0;

    private BatteryManager.BatteryState newState;
    private String mChangeType = BatteryManager.SHOW_TYPE_IN;
    private int mRemainTime;
    private View mBossView;
    private boolean isSetInitPlace = false;

    /*  */
    private View mAdView = null;
    private AdWrapperLayout mAdWrapper = null;
    private Runnable mClickRunnable = null;

    /* 用于更新时间 */
    private Timer mUpdateTimer;
    private UpdateTimeTask mUpdateTask;

    /**
     * 第一个推广位
     */
    private TextView mSwiftyTitle;
    private ImageView mSwiftyImg;
    private TextView mSwiftyContent;
    private RippleView mSwiftyBtnLt;
    private RelativeLayout mSwiftyLayout;

    /**
     * 预留推广位
     */
    private TextView mExtraTitle;
    private ImageView mExtraImg;
    private TextView mExtraContent;
    private RelativeLayout mExtraLayout;

    private ImageLoader mImageLoader;

    public static String[] days = AppMasterApplication.getInstance().getResources()
            .getStringArray(R.array.days_of_week);

    //开始首页动画
    private android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MOVE_UP:
                    if (mBossView.getVisibility() == View.VISIBLE) {
                        SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "batterypage", "screen_up");
                        if (newState.plugged == 0) {
                            mIvArrowMove.setVisibility(View.INVISIBLE);
                        } else {
                            mIvArrowMove.setBackgroundResource(R.drawable.bay_arrow_down);
                        }
//                        mSlideView.setScrollable(true);
                        mShowing = true;
                        showMoveUp();
                        mRemainTimeContent.setVisibility(View.INVISIBLE);
                        mRemainContent.setVisibility(View.INVISIBLE);
//                        timeContentMoveSmall();
//                        batteryIconMoveSmall();
                    }
                    break;
                case MOVE_DOWN:
                    if (newState.plugged != 0) {
                        SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "batterypage", "screen_down");
                        mIvArrowMove.setBackgroundResource(R.drawable.bay_arrow_up);
//                        mSlideView.setScrollable(true);
                        mShowing = true;
                        showMoveDown();
                        mRemainTimeContent.setVisibility(View.VISIBLE);
                        mRemainContent.setVisibility(View.VISIBLE);
//                        timeContentMoveBig();
//                        batteryIconMoveBig();
                    }
                    break;
                case LOAD_DONE_INIT_PLACE:
                    int type = (Integer) msg.obj;
                    reLocateMoveContent(type);
                    break;
            }
        }
    };

    private int mMoveDisdance;

    private void reLocateMoveContent(int type) {
        LeoLog.d("locationP", "slideview Y : " + mSlideView.getY());

        final int contentHeight = mBossView.getHeight();
        final int arrowHeight = mArrowMoveContent.getHeight();


        mMoveDisdance = contentHeight * 9 / 16;
        LeoLog.d("locationP", "mBossView.getHeight() : " + mBossView.getHeight());

        if (type == AD_TYPE_MSG) {
            if (mAdWrapper != null) {
                mMoveDisdance = contentHeight - mAdWrapper.getHeight() - arrowHeight;
                LeoLog.d("locationP", "mAdWrapper.getHeight() : " + mAdWrapper.getHeight());
                setYplace();
            }
        } else if (type == SWTIFY_TYPE_MSG) {
            if (mSwiftyView != null) {
                mMoveDisdance = contentHeight - mSwiftyView.getHeight() - arrowHeight;
                LeoLog.d("locationP", "mSwiftyView.getHeight() : " + mSwiftyView.getHeight());
                setYplace();
            }
        } else if (type == EXTRA_TYPE_MSG) {
            if (mExtraView != null && mExtraView.getHeight() > 0) {
                mMoveDisdance = contentHeight - mExtraView.getHeight() - arrowHeight;
                LeoLog.d("locationP", "mExtraView.getHeight() : " + mExtraView.getHeight());
                setYplace();
            }
        }
    }

    private void setYplace() {
        if (!isSetInitPlace) {
            boolean isExpandContentShow = mRecommandView.getVisibility() == 0;
            if (isExpandContentShow) {
                mMoveDisdance = mMoveDisdance + mRecommandViewHeight + 20;
            }
            mSlideView.setY(mMoveDisdance);
            LeoLog.d("locationP", "mMoveDisdance : " + mMoveDisdance);
            isSetInitPlace = true;
        }
        mBossView.setVisibility(View.VISIBLE);
    }

//    private void batteryIconMoveBig() {
//        ObjectAnimator animMoveX = ObjectAnimator.ofFloat(mBatteryIcon,
//                "x", mBatteryIcon.getLeft() + mBatteryIcon.getWidth() / 5, mBatteryIcon.getLeft());
//        ObjectAnimator animMoveY = ObjectAnimator.ofFloat(mBatteryIcon,
//                "y", mBatteryIcon.getTop()
//                        - mBatteryIcon.getHeight() - DipPixelUtil.dip2px(mActivity, 29), mBatteryIcon.getTop());
//
//        ObjectAnimator animScaleX = ObjectAnimator.ofFloat(mBatteryIcon,
//                "scaleX", 0.6f, 1f);
//        ObjectAnimator animScaleY = ObjectAnimator.ofFloat(mBatteryIcon,
//                "scaleY", 0.6f, 1f);
//
//        AnimatorSet set = new AnimatorSet();
//        set.play(animMoveX).with(animMoveY);
//        set.play(animMoveY).with(animScaleX);
//        set.play(animScaleX).with(animScaleY);
//        set.setDuration(ANIMATION_TIME);
//        set.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//                super.onAnimationStart(animation);
////                mHideTextView.setVisibility(View.INVISIBLE);
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                super.onAnimationEnd(animation);
////                mThreeMoveView.setVisibility(View.VISIBLE);
//                mRemainTimeContent.setVisibility(View.VISIBLE);
//                setTime(mRemainTime, false);
//            }
//        });
//        set.start();
//    }

//    private void batteryIconMoveSmall() {
//        ObjectAnimator animMoveX = ObjectAnimator.ofFloat(mBatteryIcon,
//                "x", mBatteryIcon.getLeft(), mBatteryIcon.getLeft() + mBatteryIcon.getWidth() / 5);
//        ObjectAnimator animMoveY = ObjectAnimator.ofFloat(mBatteryIcon,
//                "y", mBatteryIcon.getTop(), mBatteryIcon.getTop()
//                        - mBatteryIcon.getHeight() - DipPixelUtil.dip2px(mActivity, 29));
//
//        ObjectAnimator animScaleX = ObjectAnimator.ofFloat(mBatteryIcon,
//                "scaleX", 1f, 0.6f);
//        ObjectAnimator animScaleY = ObjectAnimator.ofFloat(mBatteryIcon,
//                "scaleY", 1f, 0.6f);
//
//        AnimatorSet set = new AnimatorSet();
//        set.play(animMoveX).with(animMoveY);
//        set.play(animMoveY).with(animScaleX);
//        set.play(animScaleX).with(animScaleY);
//        set.setDuration(ANIMATION_TIME);
//        set.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//                super.onAnimationStart(animation);
////                mThreeMoveView.setVisibility(View.INVISIBLE);
//                mRemainTimeContent.setVisibility(View.INVISIBLE);
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                super.onAnimationEnd(animation);
////                mHideTextView.setVisibility(View.VISIBLE);
//                setTime(mRemainTime, true);
//            }
//        });
//        set.start();
//    }

//    private void arrowMove() {
//        if (place == CHARING_TYPE_SPEED) {
//            mGreenArrow.setBackgroundResource(R.drawable.bay_kedu1);
//        } else if (place == CHARING_TYPE_CONTINUOUS) {
//            mGreenArrow.setBackgroundResource(R.drawable.bay_kedu2);
//        } else {
//            mGreenArrow.setBackgroundResource(R.drawable.bay_kedu3);
//        }
//    }

//    private void timeContentMoveBig() {
//        ObjectAnimator animScaleX = ObjectAnimator.ofFloat(mTimeContent,
//                "scaleX", 0.8f, 1f);
//        ObjectAnimator animScaleY = ObjectAnimator.ofFloat(mTimeContent,
//                "scaleY", 0.8f, 1f);
//        ObjectAnimator animMoveX = ObjectAnimator.ofFloat(mTimeContent,
//                "x", mTimeContent.getLeft() - mTimeContent.getWidth() * 4 / 8, mTimeContent.getLeft());
//
//        AnimatorSet set = new AnimatorSet();
//        set.play(animScaleX).with(animScaleY);
//        set.play(animScaleY).with(animMoveX);
//        set.setDuration(ANIMATION_TIME);
//        set.start();
//    }

//    private void timeContentMoveSmall() {
//        ObjectAnimator animScaleX = ObjectAnimator.ofFloat(mTimeContent,
//                "scaleX", 1f, 0.8f);
//        ObjectAnimator animScaleY = ObjectAnimator.ofFloat(mTimeContent,
//                "scaleY", 1f, 0.8f);
//        ObjectAnimator animMoveX = ObjectAnimator.ofFloat(mTimeContent,
//                "x", mTimeContent.getLeft(), mTimeContent.getLeft() - mTimeContent.getWidth() * 4 / 8);
//
//        AnimatorSet set = new AnimatorSet();
//        set.play(animScaleX).with(animScaleY);
//        set.play(animScaleY).with(animMoveX);
//        set.setDuration(ANIMATION_TIME);
//        set.start();
//    }


    @Override
    protected int layoutResourceId() {
        return R.layout.activity_battery_view_newter;
    }

    @Override
    protected void onInitUI() {
        LeoLog.d(TAG, "INIT UI");
        mImageLoader = ImageLoader.getInstance();

//        mTimeContent = findViewById(R.id.time_move_content);
//        mBatteryIcon = findViewById(R.id.infos_content);
        mRemainTimeContent = findViewById(R.id.remain_time);
        mRemainContent = findViewById(R.id.use_time_content);

        mTvBigTime = (TextView) findViewById(R.id.time_big);
        mTvSmallLeft = (TextView) findViewById(R.id.time_small);
        mTvSmallRight = (TextView) findViewById(R.id.time_small_right);

        mTvLevel = (TextView) findViewById(R.id.battery_num);
        mTvLeftTime = (TextView) findViewById(R.id.left_time);

        mBossView = findViewById(R.id.move_boss);
        mBossView.setOnTouchListener(this);

        mSlideView = (BatteryTestViewLayout) findViewById(R.id.slide_content);

        mScrollView = (SelfScrollView) findViewById(R.id.slide_content_sv);
        mScrollView.setParent(mSlideView);
        mSlideView.setScrollBottomListener(this);

        mBottleWater = (WaveView) findViewById(R.id.bottle_water);
//        mBottleWater.setPostInvalidateDelayMs(40);
        mBottleWater.setWaveColor(0xff0ad931);
        mBottleWater.setWave2Color(0xff0ab522);
        mBottleWater.setFactorA(DipPixelUtil.dip2px(mActivity, 3));

        mTvHideTime = (TextView) findViewById(R.id.hide_tv_one);
        mTvHideText = (TextView) findViewById(R.id.hide_tv_two);

        mSettingView = findViewById(R.id.ct_option_2_rl);
        mSettingView.setOnClickListener(this);
        mArrowMoveContent = findViewById(R.id.move_arrow);
        mArrowMoveContent.setOnTouchListener(this);
        mIvArrowMove = (ImageView) findViewById(R.id.iv_move_arrow);

        mIvCancel = (ImageView) findViewById(R.id.iv_cancle);
        mIvCancel.setOnClickListener(this);

        mShowOne = findViewById(R.id.remain_one);
        mShowOne.setOnClickListener(this);
        mShowTwo = findViewById(R.id.remain_two);
        mShowTwo.setOnClickListener(this);
        mShowThree = findViewById(R.id.remain_three);
        mShowThree.setOnClickListener(this);

        mRecommandView = findViewById(R.id.three_show_content);
        mRecommandView.post(new Runnable() {
            @Override
            public void run() {
                mRecommandViewHeight = mRecommandView.getHeight();
            }
        });

        if (newState != null) {
            process(mChangeType, newState, mRemainTime);
        }

        if (mRootView != null) {
            try {
                loadAd();
            } catch (Exception e) {
                LeoLog.e(TAG, "[loadAd Data]Catch exception happen inside Mobvista: ");
                if (e != null) {
                    LeoLog.e(TAG, e.getLocalizedMessage());
                }
            }
        }

        try {
            if (mRootView != null) {
                initSwiftyLayout(mRootView);
                initExtraLayout(mRootView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mActivity.registerReceiver(mPresentReceiver, intentFilter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        releaseAd();
        mActivity.unregisterReceiver(mPresentReceiver);
    }

    @Override
    public void onStart() {
        super.onStart();
        scheduleUpdateTimer();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer = null;
        }
        if (mUpdateTask != null) {
            mUpdateTask.cancel();
            mUpdateTask = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isExpand = false;
        mShowing = false;
        mIsExtraLayout = false;
    }

    static class UpdateTimeTask extends TimerTask {
        WeakReference<BatteryViewFragment> mFragmentRef;

        public UpdateTimeTask(BatteryViewFragment fragment) {
            mFragmentRef = new WeakReference<BatteryViewFragment>(fragment);
        }

        @Override
        public void run() {
            final BatteryViewFragment fragment = mFragmentRef.get();
            if (fragment != null) {
                ThreadManager.executeOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fragment.updateTime();
                    }
                });
            }
        }
    }

    private void scheduleUpdateTimer() {
        if (mUpdateTimer == null) {
            mUpdateTimer = new Timer();
        }
        if (mUpdateTask == null) {
            mUpdateTask = new UpdateTimeTask(this);
        }
        mUpdateTimer.schedule(mUpdateTask, 0, 10 * 1000);
    }

    public void initCreate(String type, BatteryManager.BatteryState state, int remainTime) {
        mChangeType = type;
        newState = state;
        mRemainTime = remainTime;
    }

    public void process(String type, BatteryManager.BatteryState state, int remainTime) {
        mChangeType = type;
        newState = state;
        mRemainTime = remainTime;
        notifyUI(mChangeType);
    }

    private BroadcastReceiver mPresentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LeoLog.d("stone_test_browser", "action=" + action);
            if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                if (mClickRunnable != null) {
                    mActivity.finish();
                }
            } else if (action.equals(Intent.ACTION_USER_PRESENT)) {
                if (mClickRunnable != null) {
                    mClickRunnable.run();
                    mClickRunnable = null;
                }
            }
        }
    };

    public void notifyUI(String type) {

        setBatteryPercent();
        setBottleWater();
        setTime(mRemainTime, isExpand);

        if (BatteryManager.SHOW_TYPE_OUT.equals(type)) {
            if (!isExpand) {
//                mSlideView.setScrollable(true);
                expandContent(true);
            }
        }

        if (newState != null && mIvArrowMove != null && mBottleWater != null) {
            if (newState.plugged == 0) {
                mIvArrowMove.setVisibility(View.INVISIBLE);
                mBottleWater.setIsNeedWave(false);
            } else {
                mBottleWater.setIsNeedWave(true);
                if (isExpand) {
                    mIvArrowMove.setVisibility(View.VISIBLE);
                    mIvArrowMove.setBackgroundResource(R.drawable.bay_arrow_down);
                } else {
                    mIvArrowMove.setVisibility(View.VISIBLE);
                    mIvArrowMove.setBackgroundResource(R.drawable.bay_arrow_up);
                }
            }
        }

    }

    private void updateTime() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int day_of_week = c.get(Calendar.DAY_OF_WEEK);
        LeoLog.d(TAG, year + ":" + month + ":" + day + ":" + hour + ":" + minute + ":" + day_of_week);

        mTvBigTime.setText(hour + ":" + String.format(Locale.ENGLISH, "%02d", minute));
        mTvSmallLeft.setText((month + 1) + "/" + day);

        // 资源应该从周日 - 周六 这样的顺序
        if (day_of_week >= 2 && day_of_week - 2 < days.length) {
            mTvSmallRight.setText(days[day_of_week - 2]);
        } else {
            mTvSmallRight.setText(days[6]);
        }

        LeoLog.d(TAG, "updateTime@" + hour + ":" + minute);
    }

    private void setBottleWater() {
        if (newState != null && mBottleWater != null) {
            int level = newState.level;
            mBottleWater.setPercent(level);
        }
    }


    private void setBatteryPercent() {
        if (mTvLevel != null && newState != null) {
            mTvLevel.setText(newState.level + "%");

            if (newState.level < 70) {
                mBottleWater.setPostInvalidateDelayMs(50);
            } else if (newState.level < 85) {
                mBottleWater.setPostInvalidateDelayMs(70);
            } else {
                mBottleWater.setPostInvalidateDelayMs(80);
            }
        }
    }

    public void setTime(int second, boolean isExpandContent) {
        if (mActivity == null) {
            return;
        }
        int h = 0;
        int d = 0;
        int s = 0;
        int temp = second % 3600;
        if (second > 3600) {
            h = second / 3600;
            if (temp != 0) {
                if (temp > 60) {
                    d = temp / 60;
                    if (temp % 60 != 0) {
                        s = temp % 60;
                    }
                } else {
                    s = temp;
                }
            }
        } else {
            d = second / 60;
            if (second % 60 != 0) {
                s = second % 60;
            }
        }

        String hString, dString;
        hString = String.valueOf(h);
        dString = String.valueOf(d);
        LeoLog.d("testBatteryView", "hString : " + hString + "dString : " + dString);
        boolean isCharing = newState.plugged != 0 ? true : false;

        if (newState.level >= 100) {
            String text = mActivity.getString(R.string.screen_protect_charing_text_four);
            mTvHideText.setText(text);
            mTvHideTime.setVisibility(View.GONE);
        } else {
            if (isCharing) {
                if (hString.equals("0")) {
                    if (!dString.equals("0")) {
                        String text = mActivity.getString(R.string.screen_protect_time_right_two, dString);
                        String text2 = mActivity.getString(R.string.screen_protect_time);
                        mTvHideText.setVisibility(View.VISIBLE);
                        mTvHideTime.setVisibility(View.VISIBLE);
                        mTvHideText.setText(text2);
                        mTvHideTime.setText(Html.fromHtml(text));
                    } else {
                        String text = mActivity.getString(R.string.screen_protect_charing_text_two);
                        mTvHideText.setText(text);
                        mTvHideTime.setVisibility(View.GONE);
                    }
                } else {
                    String text = mActivity.getString(R.string.screen_protect_time_right, hString, dString);
                    String text2 = mActivity.getString(R.string.screen_protect_time);
                    mTvHideText.setVisibility(View.VISIBLE);
                    mTvHideTime.setVisibility(View.VISIBLE);
                    mTvHideText.setText(text2);
                    mTvHideTime.setText(Html.fromHtml(text));
                }
            } else {
                String text2 = mActivity.getString(R.string.screen_protect_charing_text_one);
                mTvHideText.setText(text2);
                mTvHideTime.setVisibility(View.GONE);
            }

        }


    }

    private int staryY;
    private boolean isMissionDone = false;

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == mArrowMoveContent) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    staryY = (int) event.getRawY();

                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!isMissionDone) {
                        int newY = (int) event.getRawY();
                        int moveY = newY - staryY;

                        if (isExpand) {
                            if (!mShowing && moveY > 0) {
                                isMissionDone = true;
                                expandContent(false);
                            }
                        } else {
                            if (!mShowing && moveY < 0) {
                                isMissionDone = true;
                                expandContent(true);
                            }
                        }

                    }

                    break;
                case MotionEvent.ACTION_UP:
                    isMissionDone = false;
                    int upY = (int) event.getRawY();
                    if (staryY == upY) {
                        if (isExpand) {
                            if (!mShowing) {
                                expandContent(false);
                            }
                        } else {
                            if (!mShowing) {
                                expandContent(true);
                            }
                        }
                    }
                    break;
            }
        } else if (view == mBossView) {
            return false;   //TODO 取消遮盖popupwindow的点击，如果后续有影响，请取消
        }
        return true;
    }

    private void showMoveUp() {

//        ObjectAnimator animMoveY = ObjectAnimator.ofFloat(mSlideView,
//                "y", mSlideView.getTop() + mBossView.getHeight() * 9 / 16, mSlideView.getTop());
        ObjectAnimator animMoveY = ObjectAnimator.ofFloat(mSlideView,
                "y", mSlideView.getTop() + mMoveDisdance, mSlideView.getTop());
        animMoveY.setDuration(ANIMATION_TIME);
        animMoveY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isExpand = true;
                mShowing = false;
                mScrollView.setScrollY(0);
//                mSlideView.setScrollable(false);
            }
        });
        animMoveY.start();
    }

    private void showMoveDown() {
//        ObjectAnimator animMoveY = ObjectAnimator.ofFloat(mSlideView,
//                "y", mSlideView.getTop(), mSlideView.getTop() + mBossView.getHeight() * 9 / 16);
        ObjectAnimator animMoveY = ObjectAnimator.ofFloat(mSlideView,
                "y", mSlideView.getTop(), mSlideView.getTop() + mMoveDisdance);
        animMoveY.setDuration(ANIMATION_TIME);
        animMoveY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isExpand = false;
                mShowing = false;
                mScrollView.setScrollY(0);
//                mSlideView.setScrollable(false);
            }
        });
        animMoveY.start();
    }

    private void expandContent(boolean expand) {
        if (expand) {
            mHandler.sendEmptyMessage(MOVE_UP);
        } else {
            mHandler.sendEmptyMessage(MOVE_DOWN);
        }

    }

    @Override
    public void scrollBottom() {
        expandContent(false);
    }

    @Override
    public void scrollTop() {
        expandContent(true);
    }

    @Override
    public void onClick(View view) {
        if (mActivity == null) {
            return;
        }
        switch (view.getId()) {
            case R.id.ct_option_2_rl:
                mLockManager.filterPackage(mActivity.getPackageName(), 5000);
                mBatteryManager.markSettingClick();

                Intent dlIntent = new Intent(mActivity, BatterySettingActivity.class);
                dlIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                dlIntent.putExtra(Constants.BATTERY_FROM, Constants.FROM_BATTERY_PROTECT);
                dlIntent.putExtra(BatteryManager.REMAIN_TIME, mRemainTime);
                Bundle bundle = new Bundle();
                bundle.putSerializable(BatteryManager.SEND_BUNDLE, newState);
                dlIntent.putExtras(bundle);
                mActivity.startActivity(dlIntent);
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "batterypage", "screen_setting");
                mActivity.finish();
                break;
//            case R.id.speed_content:
//                showPop(CHARING_TYPE_SPEED);
//                break;
//            case R.id.continuous_content:
//                showPop(CHARING_TYPE_CONTINUOUS);
//                break;
//            case R.id.trickle_content:
//                showPop(CHARING_TYPE_TRICKLE);
//                break;
            case R.id.ad_wrapper:
                mClickRunnable = new Runnable() {
                    @Override
                    public void run() {
                        LeoLog.d("stone_test_browser", "fire touch event!");
                        MotionEvent eventDown = MotionEvent.obtain(SystemClock.elapsedRealtime(),
                                SystemClock.elapsedRealtime(), MotionEvent.ACTION_DOWN,
                                10.0f, 10.0f, 0);
                        mAdView.dispatchTouchEvent(eventDown);
                        MotionEvent eventUp = MotionEvent.obtain(SystemClock.elapsedRealtime(),
                                SystemClock.elapsedRealtime(), MotionEvent.ACTION_UP,
                                10.0f, 10.0f, 0);
                        mAdView.dispatchTouchEvent(eventUp);
                    }
                };
                if (mAdWrapper != null) {
                    mAdWrapper.setNeedIntercept(false);
                }
                handleRunnable();
                break;
            case R.id.parent_layout:
                if (view == mSwiftyLayout) {
                    mClickRunnable = new Runnable() {
                        @Override
                        public void run() {
                            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "batterypage", "screen_promote1");
                            PreferenceTable preferenceTable = PreferenceTable.getInstance();
                            Utilities.selectType(preferenceTable, PrefConst.KEY_CHARGE_SWIFTY_TYPE,
                                    PrefConst.KEY_CHARGE_SWIFTY_GP_URL, PrefConst.KEY_CHARGE_SWIFTY_URL,
                                    "", mActivity);
                        }
                    };
                    handleRunnable();
                } else if (view == mExtraLayout) {
                    mClickRunnable = new Runnable() {
                        @Override
                        public void run() {
                            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "batterypage", "screen_promote2");
                            PreferenceTable preferenceTable = PreferenceTable.getInstance();
                            Utilities.selectType(preferenceTable, PrefConst.KEY_CHARGE_EXTRA_TYPE,
                                    PrefConst.KEY_CHARGE_EXTRA_GP_URL, PrefConst.KEY_CHARGE_EXTRA_URL,
                                    "", mActivity);
                        }
                    };
                    handleRunnable();
                }
                break;
            case R.id.iv_cancle:
                BatteryShowViewActivity activity = (BatteryShowViewActivity) mActivity;
                activity.onFinishActivity();
                break;
            case R.id.remain_one:
                showRecommandContent(RECOMMAND_TYPE_ONE);
                break;
            case R.id.remain_two:
                showRecommandContent(RECOMMAND_TYPE_TWO);
                break;
            case R.id.remain_three:
                showRecommandContent(RECOMMAND_TYPE_THREE);
                break;
        }
    }

    private void showRecommandContent(int recommandTypeThree) {
        if (mCurrentClickType == recommandTypeThree) {
            mCurrentClickType = 0;
            mRecommandView.setVisibility(View.INVISIBLE);
            makeSlideContentUp();
        } else {
            mRecommandView.setVisibility(View.VISIBLE);
            if (mCurrentClickType == 0) {
                makeSlideContentDown();
            } else {
                //TODO 展示内容替换
            }
            mCurrentClickType = recommandTypeThree;
        }
    }

    private void makeSlideContentUp() {
        boolean isSlideContentShow = mBossView.getVisibility() == 0;
        if (isSlideContentShow) {
            mMoveDisdance = mMoveDisdance - mRecommandViewHeight - 20;
            mSlideView.setY(mMoveDisdance);
        }
    }

    private void makeSlideContentDown() {
        boolean isSlideContentShow = mBossView.getVisibility() == 0;
        if (isSlideContentShow) {
            mMoveDisdance = mMoveDisdance + mRecommandViewHeight + 20;
            mSlideView.setY(mMoveDisdance);
        }
    }

    /* AM-3889: 三星5.x的手机从系统锁之外跳转有问题，需要特殊处理 */
    private boolean samsungLolipopDevice() {
        LeoLog.d(TAG, "MANUFACTURER: " + Build.MANUFACTURER);
        LeoLog.d(TAG, "SDK_INT: " + Build.VERSION.SDK_INT);
        return (Build.MANUFACTURER.equalsIgnoreCase("samsung")
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && Build.VERSION.SDK_INT < 23 /*Marshmallow*/);
    }

    private void handleRunnable() {
        if (mClickRunnable == null) {
            return;
        }

        if (AppUtil.isScreenLocked(mActivity)) {
            // 默认浏览器是chrome而且系统锁住了，让user_present receiver处理
            if (samsungLolipopDevice()) {
                mLockManager.filterPackage(mActivity.getPackageName(), 2000);
                LeoLog.d(TAG, "Samsung 5.x device, launch specific activity");
                Intent intent = new Intent(mActivity, EmptyForJumpActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mActivity.startActivity(intent);
            } else {
                Intent intent = new Intent(mActivity, DeskProxyActivity.class);
                intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, DeskProxyActivity.IDX_BATTERY_PROTECT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mActivity.startActivity(intent);
            }

        } else {
            // 没有锁或者默认浏览器不是chrome，直接跑
            mClickRunnable.run();
            mClickRunnable = null;
        }
    }

//    private long showTime;
//
//    private void showPop(int type) {
//        if (!isExpand && !mShowing) {
//            String str = getRightMenuItems(type);
//            View view;
//            if (type == CHARING_TYPE_SPEED) {
////                view = mSpeedContent;
//                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "batterypage", "screen_quick");
//            } else if (type == CHARING_TYPE_CONTINUOUS) {
////                view = mContinuousContent;
//                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "batterypage", "screen_constant");
//            } else {
////                view = mTrickleContent;
//                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "batterypage", "screen_trickle");
//            }
//
////            showPopUp(view, str);
//            showTime = System.currentTimeMillis();
//            mHandler.sendEmptyMessageDelayed(DIMISS_POP, 5000);
//        }
//    }

//    private String getRightMenuItems(int type) {
//        Context ctx = mActivity;
//        String mStr;
//        if (type == CHARING_TYPE_SPEED) {
//            mStr = ctx.getString(R.string.screen_protect_type_pop_one);
//        } else if (type == CHARING_TYPE_CONTINUOUS) {
//            mStr = ctx.getString(R.string.screen_protect_type_pop_two);
//        } else {
//            mStr = ctx.getString(R.string.screen_protect_type_pop_three);
//        }
//        return mStr;
//    }


    /* 广告相关 - 开始 */
    private boolean mShouldLoadAd = false;

    private void loadAd() {
        LeoLog.d(TAG, "loadAd called");
        mShouldLoadAd = AppMasterPreference.getInstance(mActivity).getADOnScreenSaver() == 1;
        if (mShouldLoadAd) {
            MobvistaEngine.getInstance(mActivity).loadMobvista(Constants.UNIT_ID_CHARGING,
                    new MobvistaEngine.MobvistaListener() {
                        @Override
                        public void onMobvistaFinished(int code, Campaign campaign, String msg) {
                            if (code == MobvistaEngine.ERR_OK) {
                                LeoLog.d(TAG, "Ad data ready");
                                sAdImageListener = new AdPreviewLoaderListener(BatteryViewFragment.this, campaign);
                                ImageLoader.getInstance().loadImage(campaign.getImageUrl(), sAdImageListener);
                            }
                        }

                        @Override
                        public void onMobvistaClick(Campaign campaign) {
                            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "ad_cli", "adv_cnts_screen");
                            LeoLog.d(TAG, "Ad clicked");
                        }
                    });
        }
    }

    private void releaseAd() {
        if (mShouldLoadAd) {
            LeoLog.d(TAG, "release ad");
            MobvistaEngine.getInstance(mActivity).release(Constants.UNIT_ID_CHARGING);
        }
    }

    public static class AdPreviewLoaderListener implements ImageLoadingListener {
        WeakReference<BatteryViewFragment> mFragment;
        Campaign mCampaign;

        public AdPreviewLoaderListener(BatteryViewFragment fragment, final Campaign campaign) {
            mFragment = new WeakReference<BatteryViewFragment>(fragment);
            mCampaign = campaign;
        }

        @Override
        public void onLoadingStarted(String imageUri, View view) {

        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            LeoLog.e(TAG, "failed to load AD preview!");
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            LeoLog.d(TAG, "Ad preview image ready");
            BatteryViewFragment fragment = mFragment.get();
            if (loadedImage != null && fragment != null) {
                try {
                    LeoLog.d(TAG, "load done: " + imageUri);
                    fragment.initAdLayout(fragment.mRootView, mCampaign, loadedImage);
                } catch (Exception e) {
                    LeoLog.e(TAG, "[Impression]Catch exception happen inside Mobvista: ");
                    if (e != null) {
                        LeoLog.e(TAG, e.getLocalizedMessage());
                    }
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {

        }
    }

    private static AdPreviewLoaderListener sAdImageListener;

    private void initAdLayout(View rootView, Campaign campaign, Bitmap previewImage) {
        View adView = rootView.findViewById(R.id.ad_content);
        mAdWrapper = (AdWrapperLayout) rootView.findViewById(R.id.ad_wrapper);
        mAdWrapper.setNeedIntercept(true);

        TextView tvTitle = (TextView) adView.findViewById(R.id.item_title);
        tvTitle.setText(campaign.getAppName());
        TextView tvDesc = (TextView) adView.findViewById(R.id.item_description);
        tvDesc.setText(campaign.getAppDesc());
        Button btnCTA = (Button) adView.findViewById(R.id.ad_result_cta);
        btnCTA.setText(campaign.getAdCall());
        ResizableImageView preview = (ResizableImageView) adView.findViewById(R.id.item_ad_preview);
        preview.setImageBitmap(previewImage);
        ImageView iconView = (ImageView) adView.findViewById(R.id.ad_icon);
        ImageLoader.getInstance().displayImage(campaign.getIconUrl(), iconView);
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "ad_act", "adv_shws_screen");

//        ImageView ignoreBtn = (ImageView) adView.findViewById(R.id.iv_ignore);
//        ignoreBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(mActivity, "!!!!!!!", Toast.LENGTH_SHORT).show();
//            }
//        });


        adView.setVisibility(View.VISIBLE);
        mAdWrapper.postDelayed(new Runnable() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                msg.what = LOAD_DONE_INIT_PLACE;
                msg.obj = AD_TYPE_MSG;
                mHandler.sendMessage(msg);
            }
        }, 300);
        mAdView = adView;

        // make the count correct
        MobvistaEngine.getInstance(mActivity).registerView(Constants.UNIT_ID_CHARGING, mAdView);

        mAdWrapper.setOnClickListener(this);
    }
    /* 广告相关 - 结束 */

    private View mSwiftyView;

    private void initSwiftyLayout(View view) {
        ViewStub viewStub = (ViewStub) view.findViewById(R.id.content_type_1);
        if (viewStub == null) {
            return;
        }

        PreferenceTable preferenceTable = PreferenceTable.getInstance();

        boolean isContentEmpty = TextUtils.isEmpty(
                preferenceTable.getString(PrefConst.KEY_CHARGE_SWIFTY_CONTENT));

        boolean isImgUrlEmpty = TextUtils.isEmpty(
                preferenceTable.getString(PrefConst.KEY_CHARGE_SWIFTY_IMG_URL));

        boolean isTypeEmpty = TextUtils.isEmpty(
                preferenceTable.getString(PrefConst.KEY_CHARGE_SWIFTY_TYPE));

        boolean isGpUrlEmpty = TextUtils.isEmpty(
                preferenceTable.getString(PrefConst.KEY_CHARGE_SWIFTY_GP_URL));

        boolean isBrowserUrlEmpty = TextUtils.isEmpty(
                preferenceTable.getString(PrefConst.KEY_CHARGE_SWIFTY_URL));

        boolean isUrlEmpty = isGpUrlEmpty && isBrowserUrlEmpty; //判断两个地址是否都为空

        if (!isContentEmpty && !isImgUrlEmpty && !isTypeEmpty && !isUrlEmpty) {
//        if (true) {
            mSwiftyView = viewStub.inflate();

            mSwiftyImg = (ImageView) mSwiftyView.findViewById(R.id.card_img);
            mSwiftyContent = (TextView) mSwiftyView.findViewById(R.id.card_content);
            mSwiftyLayout = (RelativeLayout) mSwiftyView.findViewById(R.id.parent_layout);
            mSwiftyLayout.setOnClickListener(this);
            mSwiftyContent.setText(preferenceTable.getString(PrefConst.KEY_CHARGE_SWIFTY_CONTENT));
            String imgUrl = preferenceTable.getString(PrefConst.KEY_CHARGE_SWIFTY_IMG_URL);
            mImageLoader.displayImage(imgUrl, mSwiftyImg, getOptions(R.drawable.online_theme_loading));
            mSwiftyTitle = (TextView) mSwiftyView.findViewById(R.id.card_title);
            boolean isTitleEmpty = TextUtils.isEmpty(
                    preferenceTable.getString(PrefConst.KEY_CHARGE_SWIFTY_TITLE));
            if (!isTitleEmpty) {
                mSwiftyTitle.setText(preferenceTable.getString(
                        PrefConst.KEY_CHARGE_SWIFTY_TITLE));
            }
            mIsExtraLayout = true;
            mSwiftyView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Message msg = Message.obtain();
                    msg.what = LOAD_DONE_INIT_PLACE;
                    msg.obj = SWTIFY_TYPE_MSG;
                    mHandler.sendMessage(msg);
                }
            }, 300);
        }
    }

    private View mExtraView;

    private void initExtraLayout(View view) {
        ViewStub viewStub = (ViewStub) view.findViewById(R.id.content_type_2);
        if (viewStub == null) {
            return;
        }

        PreferenceTable preferenceTable = PreferenceTable.getInstance();

        boolean isContentEmpty = TextUtils.isEmpty(
                preferenceTable.getString(PrefConst.KEY_CHARGE_EXTRA_CONTENT));

        boolean isImgUrlEmpty = TextUtils.isEmpty(
                preferenceTable.getString(PrefConst.KEY_CHARGE_EXTRA_IMG_URL));

        boolean isTypeEmpty = TextUtils.isEmpty(
                preferenceTable.getString(PrefConst.KEY_CHARGE_EXTRA_TYPE));

        boolean isGpUrlEmpty = TextUtils.isEmpty(
                preferenceTable.getString(PrefConst.KEY_CHARGE_EXTRA_GP_URL));

        boolean isBrowserUrlEmpty = TextUtils.isEmpty(
                preferenceTable.getString(PrefConst.KEY_CHARGE_EXTRA_URL));

        boolean isUrlEmpty = isGpUrlEmpty && isBrowserUrlEmpty; //判断两个地址是否都为空

        if (!isContentEmpty && !isImgUrlEmpty && !isTypeEmpty && !isUrlEmpty) {
//        if (true) {
            mExtraView = viewStub.inflate();
            mExtraTitle = (TextView) mExtraView.findViewById(R.id.card_title);
            mExtraImg = (ImageView) mExtraView.findViewById(R.id.card_img);
            mExtraContent = (TextView) mExtraView.findViewById(R.id.card_content);
            mExtraLayout = (RelativeLayout) mExtraView.findViewById(R.id.parent_layout);
            mExtraLayout.setOnClickListener(this);
            mExtraContent.setText(preferenceTable.getString(PrefConst.KEY_CHARGE_EXTRA_CONTENT));
            String imgUrl = preferenceTable.getString(PrefConst.KEY_CHARGE_EXTRA_IMG_URL);
            mImageLoader.displayImage(imgUrl, mExtraImg, getOptions(R.drawable.online_theme_loading));
            boolean isTitleEmpty = TextUtils.isEmpty(
                    preferenceTable.getString(PrefConst.KEY_CHARGE_EXTRA_TITLE));
            if (!isTitleEmpty) {
                mExtraTitle.setText(preferenceTable.getString(
                        PrefConst.KEY_CHARGE_EXTRA_TITLE));
            }
            mIsExtraLayout = true;
            mExtraView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Message msg = Message.obtain();
                    msg.what = LOAD_DONE_INIT_PLACE;
                    msg.obj = EXTRA_TYPE_MSG;
                    mHandler.sendMessage(msg);
                }
            }, 300);
        }

    }

    public DisplayImageOptions getOptions(int drawble) {  //需要提供默认图
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(drawble)
                .showImageForEmptyUri(drawble)
                .showImageOnFail(drawble)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .build();

        return options;
    }

//    private PopupWindow popupWindow;
//    private void showPopUp(View v, String str) {
//        View contentView = LayoutInflater.from(mActivity).inflate(
//                R.layout.popmenu_battery_list_item, null);
//        TextView text = (TextView) contentView.findViewById(R.id.menu_text);
//        float textWidth = getTextViewLength(text, str);
//        LeoLog.d("testPop", "width : " + textWidth);
//        text.setText(str);
//        int size = (int) getResources().getDimension(R.dimen.popu_txt_size);
//        text.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
//        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(AppMasterApplication.sScreenWidth, View.MeasureSpec.AT_MOST);
//        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//        text.measure(widthMeasureSpec, heightMeasureSpec);
//
//        int[] location = new int[2];
//        v.getLocationOnScreen(location);
//        int x;
//        int height = DipPixelUtil.dip2px(mActivity, 50);
//        if (textWidth > CHANGE_LINE_INT) {
//            x = location[0];
//            height = DipPixelUtil.dip2px(mActivity, 70);
//        } else if (textWidth > MID_WIDTH) {
//            x = location[0] - 80;
//        } else {
//            x = location[0] - 100;
//        }
//
//        popupWindow = new PopupWindow(contentView, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);//TODO 原本是用height，为啥？
//        popupWindow.setFocusable(true);
//        popupWindow.setOutsideTouchable(true);
//        popupWindow.setBackgroundDrawable(new BitmapDrawable());
//        LeoLog.d("testPop", "text_height : " + text.getMeasuredHeight());
//        int y = location[1] - text.getMeasuredHeight() - mSpeedContent.getHeight();
//        popupWindow.showAtLocation(v, Gravity.NO_GRAVITY, x, y);
//    }

//    public float getTextViewLength(TextView textView, String text) {
//        TextPaint paint = textView.getPaint();
//        // 得到使用该paint写上text的时候,像素为多少
//        float textLength = paint.measureText(text);
//        return textLength;
//    }

}
