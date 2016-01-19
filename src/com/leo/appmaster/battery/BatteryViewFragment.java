
package com.leo.appmaster.battery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Html;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.manager.MobvistaEngine;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.mgr.BatteryManager;
import com.leo.appmaster.mgr.impl.BatteryManagerImpl;
import com.leo.appmaster.quickgestures.ISwipUpdateRequestManager;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.BatteryMenu;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.WaveView;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BatteryViewFragment extends BaseFragment implements View.OnTouchListener, BatteryTestViewLayout.ScrollBottomListener, View.OnClickListener {

    private static final String TAG = "BatteryViewFragment";
    private static final int ANIMATION_TIME = 300;
    private static final int MOVE_UP = 1;
    private static final int MOVE_DOWN = 2;
    private static final int GREEN_ARROW_MOVE = 3;

    private static final int CHARING_TYPE_SPEED = 1;
    private static final int CHARING_TYPE_CONTINUOUS = 2;
    private static final int CHARING_TYPE_TRICKLE = 3;

    private static final int DIMISS_POP = 5;
    private static final int LOAD_DONE_INIT_PLACE = 6;

    private View mTimeContent;
    private View mBatteryIcon;
    private View mRemainTimeContent;
    private BatteryTestViewLayout mSlideView;

    private TextView mTvLevel;
    private TextView mTvBigTime;
    private TextView mTvSmallLeft;
    private TextView mTvSmallRight;
    private TextView mTvLeftTime;
    private TextView mTvTime;
    private SelfScrollView mScrollView;

    private WaveView mBottleWater;
    private View mThreeMoveView;
    private ImageView mGreenArrow;
    private ImageView mIvTrickle;
    private View mTrickleContent;
    private TextView mTvTrickle;
    private ImageView mIvContinuous;
    private View mContinuousContent;
    private TextView mTvContinuous;
    private ImageView mIvSpeed;
    private View mSpeedContent;
    private TextView mTvSpeed;
    private View mHideTextView;
    private TextView mTvHideTime;
    private TextView mTvHideText;

    private View mSettingView;
    private View mArrowMoveContent;
    private ImageView mIvArrowMove;

    public static boolean isExpand = false;
    private BatteryManagerImpl.BatteryState newState;
    private String mChangeType = BatteryManagerImpl.SHOW_TYPE_IN;
    private int mRemainTime;
    private BatteryMenu mLeoPopMenu;
    private View mBossView;
    private boolean isSetInitPlace = false;

    /**
     * Swifty
     */
    private TextView mSwiftyTitle;
    private ImageView mSwiftyImg;
    private TextView mSwiftyContent;
    private RippleView mSwiftyBtnLt;

    /**
     * 预留推广位
     */
    private TextView mExtraTitle;
    private ImageView mExtraImg;
    private TextView mExtraContent;
    private RippleView mExtraBtnLt;

    private ImageLoader mImageLoader;

    public static String[] days = AppMasterApplication.getInstance().getResources()
            .getStringArray(R.array.days_of_week);

    //开始首页动画
    private android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MOVE_UP:
                    if (mBossView.getVisibility() == View.VISIBLE) {
                        mIvArrowMove.setBackgroundResource(R.drawable.bay_arrow_down);
                        mSlideView.setScrollable(true);
                        mShowing = true;
                        showMoveUp();
                        timeContentMoveSmall();
                        batteryIconMoveSmall();
                    }
                    break;
                case MOVE_DOWN:
                    if (mBatteryManager.getIsCharing()) {
                        mIvArrowMove.setBackgroundResource(R.drawable.bay_arrow_up);
                        mSlideView.setScrollable(true);
                        mShowing = true;
                        showMoveDown();
                        timeContentMoveBig();
                        batteryIconMoveBig();
                    }
                    break;
                case GREEN_ARROW_MOVE:
                    arrowMove();
                    break;
                case DIMISS_POP:
                    mLeoPopMenu.dimissPop();
                    break;
                case LOAD_DONE_INIT_PLACE:
                    reLocateMoveContent();
                    break;
            }
        }
    };

    private void reLocateMoveContent() {
        LeoLog.d("testBatteryView", "slideview Y : " + mSlideView.getY());
        if (!isSetInitPlace) {
            mSlideView.setY(mBossView.getHeight() / 2);
            isSetInitPlace = true;
        }
        mBossView.setVisibility(View.VISIBLE);
    }

    private void batteryIconMoveBig() {
        ObjectAnimator animMoveX = ObjectAnimator.ofFloat(mBatteryIcon,
                "x", mBatteryIcon.getLeft() + mBatteryIcon.getWidth() / 5, mBatteryIcon.getLeft());
        ObjectAnimator animMoveY = ObjectAnimator.ofFloat(mBatteryIcon,
                "y", mBatteryIcon.getTop()
                        - mBatteryIcon.getHeight() - DipPixelUtil.dip2px(mActivity, 40), mBatteryIcon.getTop());

        ObjectAnimator animScaleX = ObjectAnimator.ofFloat(mBatteryIcon,
                "scaleX", 0.6f, 1f);
        ObjectAnimator animScaleY = ObjectAnimator.ofFloat(mBatteryIcon,
                "scaleY", 0.6f, 1f);

        AnimatorSet set = new AnimatorSet();
        set.play(animMoveX).with(animMoveY);
        set.play(animMoveY).with(animScaleX);
        set.play(animScaleX).with(animScaleY);
        set.setDuration(ANIMATION_TIME);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mHideTextView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mThreeMoveView.setVisibility(View.VISIBLE);
                mRemainTimeContent.setVisibility(View.VISIBLE);
                setTime(mRemainTime, false);
            }
        });
        set.start();
    }

    private void batteryIconMoveSmall() {
        ObjectAnimator animMoveX = ObjectAnimator.ofFloat(mBatteryIcon,
                "x", mBatteryIcon.getLeft(), mBatteryIcon.getLeft() + mBatteryIcon.getWidth() / 5);
        ObjectAnimator animMoveY = ObjectAnimator.ofFloat(mBatteryIcon,
                "y", mBatteryIcon.getTop(), mBatteryIcon.getTop()
                        - mBatteryIcon.getHeight() - DipPixelUtil.dip2px(mActivity, 40));

        ObjectAnimator animScaleX = ObjectAnimator.ofFloat(mBatteryIcon,
                "scaleX", 1f, 0.6f);
        ObjectAnimator animScaleY = ObjectAnimator.ofFloat(mBatteryIcon,
                "scaleY", 1f, 0.6f);

        AnimatorSet set = new AnimatorSet();
        set.play(animMoveX).with(animMoveY);
        set.play(animMoveY).with(animScaleX);
        set.play(animScaleX).with(animScaleY);
        set.setDuration(ANIMATION_TIME);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mThreeMoveView.setVisibility(View.INVISIBLE);
                mRemainTimeContent.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mHideTextView.setVisibility(View.VISIBLE);
                setTime(mRemainTime, true);
            }
        });
        set.start();
    }

    private void arrowMove() {
        float stayY;
        if (place == CHARING_TYPE_SPEED) {
            stayY = mThreeMoveView.getHeight() - mIvSpeed.getHeight() / 2 - DipPixelUtil.dip2px(mActivity, 2);
        } else if (place == CHARING_TYPE_CONTINUOUS) {
            stayY = mThreeMoveView.getHeight() / 2 - DipPixelUtil.dip2px(mActivity, 2);
        } else {
            stayY = mIvSpeed.getHeight() / 2;
        }
        mGreenArrow.setY(stayY);
    }

    private void timeContentMoveBig() {
        ObjectAnimator animScaleX = ObjectAnimator.ofFloat(mTimeContent,
                "scaleX", 0.8f, 1f);
        ObjectAnimator animScaleY = ObjectAnimator.ofFloat(mTimeContent,
                "scaleY", 0.8f, 1f);
        ObjectAnimator animMoveX = ObjectAnimator.ofFloat(mTimeContent,
                "x", mTimeContent.getLeft() - mTimeContent.getWidth() * 3 / 4, mTimeContent.getLeft());

        AnimatorSet set = new AnimatorSet();
        set.play(animScaleX).with(animScaleY);
        set.play(animScaleY).with(animMoveX);
        set.setDuration(ANIMATION_TIME);
        set.start();
    }

    private void timeContentMoveSmall() {
        ObjectAnimator animScaleX = ObjectAnimator.ofFloat(mTimeContent,
                "scaleX", 1f, 0.8f);
        ObjectAnimator animScaleY = ObjectAnimator.ofFloat(mTimeContent,
                "scaleY", 1f, 0.8f);
        ObjectAnimator animMoveX = ObjectAnimator.ofFloat(mTimeContent,
                "x", mTimeContent.getLeft(), mTimeContent.getLeft() - mTimeContent.getWidth() * 3 / 4);

        AnimatorSet set = new AnimatorSet();
        set.play(animScaleX).with(animScaleY);
        set.play(animScaleY).with(animMoveX);
        set.setDuration(ANIMATION_TIME);
        set.start();
    }


    @Override
    protected int layoutResourceId() {
        return R.layout.activity_battery_view_new;
    }

    @Override
    protected void onInitUI() {
        LeoLog.d("testBatteryView", "INIT UI");
        mImageLoader = ImageLoader.getInstance();

        mTimeContent = findViewById(R.id.time_move_content);
        mBatteryIcon = findViewById(R.id.infos_content);
        mRemainTimeContent = findViewById(R.id.remain_time);

        mTvBigTime = (TextView) findViewById(R.id.time_big);
        mTvSmallLeft = (TextView) findViewById(R.id.time_small);
        mTvSmallRight = (TextView) findViewById(R.id.time_small_right);

        mTvLevel = (TextView) findViewById(R.id.battery_num);
        mTvLeftTime = (TextView) findViewById(R.id.left_time);
        mTvTime = (TextView) findViewById(R.id.right_time);

        mBossView = findViewById(R.id.move_boss);
        mBossView.setOnTouchListener(this);

        mSlideView = (BatteryTestViewLayout) findViewById(R.id.slide_content);

        mScrollView = (SelfScrollView) findViewById(R.id.slide_content_sv);
        mScrollView.setParent(mSlideView);
        mSlideView.setScrollBottomListener(this);

        mBottleWater = (WaveView) findViewById(R.id.bottle_water);
        mThreeMoveView = findViewById(R.id.battery_icon_flag);
        mGreenArrow = (ImageView) findViewById(R.id.little_arrow);
        mTrickleContent = findViewById(R.id.trickle_content);
        mTrickleContent.setOnClickListener(this);
        mIvTrickle = (ImageView) findViewById(R.id.iv_trickle);
        mTvTrickle = (TextView) findViewById(R.id.tv_trickle);
        mContinuousContent = findViewById(R.id.continuous_content);
        mContinuousContent.setOnClickListener(this);
        mIvContinuous = (ImageView) findViewById(R.id.iv_continuous);
        mTvContinuous = (TextView) findViewById(R.id.tv_continuous);
        mSpeedContent = findViewById(R.id.speed_content);
        mSpeedContent.setOnClickListener(this);
        mIvSpeed = (ImageView) findViewById(R.id.iv_speed);
        mTvSpeed = (TextView) findViewById(R.id.tv_speed);
        mHideTextView = findViewById(R.id.hide_tv_content);
        mTvHideTime = (TextView) findViewById(R.id.hide_tv_one);
        mTvHideText = (TextView) findViewById(R.id.hide_tv_two);

        mSettingView = findViewById(R.id.ct_option_2_rl);
        mSettingView.setOnClickListener(this);
        mArrowMoveContent = findViewById(R.id.move_arrow);
        mArrowMoveContent.setOnClickListener(this);
        mIvArrowMove = (ImageView) findViewById(R.id.iv_move_arrow);

        if (newState != null) {
            process(mChangeType, newState, mRemainTime);
        }

        if (mRootView != null) {
            loadAd();
        }

        try {
            if (mRootView != null) {
                initSwiftyLayout(mRootView);
                initExtraLayout(mRootView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateTime();
        new TimeThread(this).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        releaseAd();
    }


    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isExpand = false;
        mShowing = false;
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

    public void notifyUI(String type) {

        setBatteryPercent();
        setBottleWater();
        setTime(mRemainTime, isExpand);

        if (type.equals(BatteryManagerImpl.SHOW_TYPE_OUT)) {
            if (!isExpand) {
                mSlideView.setScrollable(true);
                expandContent(true);
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

        mTvBigTime.setText(hour + ":" + String.format("%02d", minute));
        mTvSmallLeft.setText((month + 1) + "/" + day);

        // 资源应该从周日 - 周六 这样的顺序
        if (day_of_week >= 2) {
            mTvSmallRight.setText(days[day_of_week - 2]);
        } else {
            mTvSmallRight.setText(days[6]);
        }
    }

    static class TimeThread extends Thread {
        WeakReference<BatteryViewFragment> mFragmentRef;

        public TimeThread(BatteryViewFragment fragment) {
            mFragmentRef = new WeakReference<BatteryViewFragment>(fragment);
        }

        @Override
        public void run() {
            while (true) {
                // update time every 10 second
                SystemClock.sleep(10 * 1000);
                final BatteryViewFragment fragment = mFragmentRef.get();
                if (fragment == null) {
                    return;
                }
                ThreadManager.executeOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fragment.updateTime();
                    }
                });
            }
        }
    }

    private void setBottleWater() {
        int level = newState.level;
//        int level = 99;
//        if (level > 95) {
//            level = 93;
//        }
        mBottleWater.setPercent(level);
    }

    private int place = 0;

    private void setBatteryPercent() {
        mTvLevel.setText(newState.level + "%");

        if (newState.level < 70) {
            place = CHARING_TYPE_SPEED;
            mIvTrickle.setBackgroundResource(R.drawable.bay_trickle);
            mIvContinuous.setBackgroundResource(R.drawable.bay_continuous);
            mIvSpeed.setBackgroundResource(R.drawable.bay_speed2);
        } else if (newState.level < 85) {
            place = CHARING_TYPE_CONTINUOUS;
            mIvTrickle.setBackgroundResource(R.drawable.bay_trickle);
            mIvContinuous.setBackgroundResource(R.drawable.bay_continuous2);
            mIvSpeed.setBackgroundResource(R.drawable.bay_speed);
        } else {
            place = CHARING_TYPE_TRICKLE;
            mIvTrickle.setBackgroundResource(R.drawable.bay_trickle2);
            mIvContinuous.setBackgroundResource(R.drawable.bay_continuous);
            mIvSpeed.setBackgroundResource(R.drawable.bay_speed);
        }

        mHandler.sendEmptyMessage(GREEN_ARROW_MOVE);

    }

    private void noTime() {
        mTvLeftTime.setVisibility(View.GONE);
        mTvTime.setVisibility(View.GONE);
    }


    public void setTime(int second, boolean isExpandContent) {
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

//        if (hString.equals("0")) {
//            if (!dString.equals("0")) {
//                String text = mActivity.getString(R.string.screen_protect_time_right_two, dString);
//                mTvLeftTime.setVisibility(View.VISIBLE);
//                mTvTime.setVisibility(View.VISIBLE);
//                mTvTime.setText(Html.fromHtml(text));
//                mBatteryText = text;
//            } else {
//                if (newState.level == 100) {
//                    mTvLeftTime.setText(mActivity.getString(R.string.screen_protect_charing_text_four));
//                    mTvTime.setVisibility(View.GONE);
//                } else {
//                    mTvLeftTime.setText(mActivity.getString(R.string.screen_protect_charing_text_two));
//                    mTvTime.setVisibility(View.GONE);
//                }
//            }
//        } else {
//            String text = mActivity.getString(R.string.screen_protect_time_right, hString, dString);
//            mTvLeftTime.setVisibility(View.VISIBLE);
//            mTvTime.setVisibility(View.VISIBLE);
//            mTvTime.setText(Html.fromHtml(text));
//            mBatteryText = text;
//        }


//        int texta;
//        if (isCharing) {
//            texta = 1;
//        } else {
//            texta = 0;
//        }
//
//        int textb;
//        if (isExpandContent) {
//            textb = 1;
//        } else {
//            textb = 0;
//        }
//        Toast.makeText(mActivity, "level:" + newState.level +
//                ",isCharing:" + texta + ",isExpand:" + textb + ",h:" + hString + ",d:" + dString
//                , Toast.LENGTH_LONG).show();

        if (newState.level >= 100) {
            if (isExpandContent) {
                String text = mActivity.getString(R.string.screen_protect_charing_text_four);
                mTvHideText.setText(text);
                mTvHideTime.setVisibility(View.GONE);
            } else {
                String text;
                if (isCharing) {
                    text = mActivity.getString(R.string.screen_protect_charing_text_three);
                } else {
                    text = mActivity.getString(R.string.screen_protect_charing_text_four);
                }
                mTvLeftTime.setText(text);
                mTvTime.setVisibility(View.GONE);
            }
        } else {
            if (isExpandContent) {
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
                            String text = mActivity.getString(R.string.screen_protect_charing_text_one);
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
            } else {
                if (hString.equals("0")) {
                    if (!dString.equals("0")) {
                        String text = mActivity.getString(R.string.screen_protect_time_right_two, dString);
                        String text2 = mActivity.getString(R.string.screen_protect_time);
                        mTvLeftTime.setVisibility(View.VISIBLE);
                        mTvTime.setVisibility(View.VISIBLE);
                        mTvLeftTime.setText(text2);
                        mTvTime.setText(Html.fromHtml(text));
                    } else {
                        String text = mActivity.getString(R.string.screen_protect_charing_text_one);
                        mTvLeftTime.setText(text);
                        mTvTime.setVisibility(View.GONE);
                    }
                } else {
                    String text = mActivity.getString(R.string.screen_protect_time_right, hString, dString);
                    String text2 = mActivity.getString(R.string.screen_protect_time);
                    mTvLeftTime.setVisibility(View.VISIBLE);
                    mTvTime.setVisibility(View.VISIBLE);
                    mTvLeftTime.setText(text2);
                    mTvTime.setText(Html.fromHtml(text));
                }
            }


        }


    }

    public static boolean mShowing = false;
    private int staryY;

    @Override
    public boolean onTouch(View view, MotionEvent event) {
//        if (view == mBossView) {
//            switch (event.getAction()) {
//                case MotionEvent.ACTION_DOWN:// 手指按下屏幕
//                    LeoLog.d("testBatteryView", "ACTION_DOWN");
//                    staryY = (int) event.getRawY();
//                    break;
//                case MotionEvent.ACTION_MOVE:// 手指在屏幕上移动
//                    int newY = (int) event.getRawY();
//                    int moveY = newY - staryY;
//                    if (!isExpand) {
//                        if (moveY < -100 && !mShowing) {
//                            expandContent(true);
//                        }
//                    } else {
//                        if (moveY > 100 && !mShowing) {
//                            expandContent(false);
//                        }
//                    }
//
//                    break;
//                case MotionEvent.ACTION_UP:// 手指离开屏幕一瞬间
//
//                    break;
//            }
//
//        }
        return true;
    }

    private void showMoveUp() {

        ObjectAnimator animMoveY = ObjectAnimator.ofFloat(mSlideView,
                "y", mSlideView.getTop() + mBossView.getHeight() / 2, mSlideView.getTop());
        animMoveY.setDuration(ANIMATION_TIME);
        animMoveY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isExpand = true;
                mShowing = false;
                mScrollView.setScrollY(0);
                mSlideView.setScrollable(true);
            }
        });
        animMoveY.start();
    }

    private void showMoveDown() {
        ObjectAnimator animMoveY = ObjectAnimator.ofFloat(mSlideView,
                "y", mSlideView.getTop(), mSlideView.getTop() + mBossView.getHeight() / 2);
        animMoveY.setDuration(ANIMATION_TIME);
        animMoveY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isExpand = false;
                mShowing = false;
                mScrollView.setScrollY(0);
                mSlideView.setScrollable(false);
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
        switch (view.getId()) {
            case R.id.ct_option_2_rl:
                mLockManager.filterPackage(mActivity.getPackageName(), 1000);

                Intent dlIntent = new Intent(mActivity, BatterySettingActivity.class);
                dlIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                dlIntent.putExtra(Constants.BATTERY_FROM, Constants.FROM_BATTERY_PROTECT);
                dlIntent.putExtra(BatteryManagerImpl.REMAIN_TIME, mRemainTime);
                Bundle bundle = new Bundle();
                bundle.putSerializable(BatteryManagerImpl.SEND_BUNDLE, newState);
                dlIntent.putExtras(bundle);
                startActivity(dlIntent);
                mActivity.finish();

                break;
            case R.id.speed_content:
                showPop(CHARING_TYPE_SPEED);
                break;
            case R.id.continuous_content:
                showPop(CHARING_TYPE_CONTINUOUS);
                break;
            case R.id.trickle_content:
                showPop(CHARING_TYPE_TRICKLE);
                break;
            case R.id.item_btn_rv:
                mLockManager.filterSelfOneMinites();
                if (view == mSwiftyBtnLt) {
                    boolean installISwipe = ISwipUpdateRequestManager.isInstallIsiwpe(mActivity);

                    if (installISwipe) {
                        Utilities.startISwipIntent(mActivity);
                    } else {
                        PreferenceTable preferenceTable = PreferenceTable.getInstance();
                        Utilities.selectType(preferenceTable, PrefConst.KEY_CHARGE_SWIFTY_TYPE,
                                PrefConst.KEY_CHARGE_SWIFTY_GP_URL, PrefConst.KEY_CHARGE_SWIFTY_URL,
                                Constants.ISWIPE_PACKAGE, mActivity);
                    }
                } else if (view == mExtraBtnLt) {
                    PreferenceTable preferenceTable = PreferenceTable.getInstance();
                    Utilities.selectType(preferenceTable, PrefConst.KEY_CHARGE_EXTRA_TYPE,
                            PrefConst.KEY_CHARGE_EXTRA_GP_URL, PrefConst.KEY_CHARGE_EXTRA_URL,
                            Constants.ISWIPE_PACKAGE, mActivity);
                }
                break;
            case R.id.move_arrow:
                if (isExpand) {
                    expandContent(false);
                } else {
                    expandContent(true);
                }
                break;
        }
    }

    private void showPop(int type) {
        if (!isExpand && !mShowing) {
            initPopMenu();
            mLeoPopMenu.setPopMenuItems(mActivity, getRightMenuItems(type), null);

            View view;
            if (type == CHARING_TYPE_SPEED) {
                view = mSpeedContent;
            } else if (type == CHARING_TYPE_CONTINUOUS) {
                view = mContinuousContent;
            } else {
                view = mTrickleContent;
            }

            mLeoPopMenu.showPopMenu(mActivity, view, null, null);
            mHandler.sendEmptyMessageDelayed(DIMISS_POP, 5000);
        }
    }

    private List<String> getRightMenuItems(int type) {
        List<String> listItems = new ArrayList<String>();
        Context ctx = mActivity;
        String mStr;
        if (type == CHARING_TYPE_SPEED) {
            mStr = ctx.getString(R.string.screen_protect_type_pop_one);
        } else if (type == CHARING_TYPE_CONTINUOUS) {
            mStr = ctx.getString(R.string.screen_protect_type_pop_two);
        } else {
            mStr = ctx.getString(R.string.screen_protect_type_pop_three);
        }
        listItems.add(mStr);
        return listItems;
    }

    public void initPopMenu() {
        if (mLeoPopMenu != null) return;
        mLeoPopMenu = new BatteryMenu();
        mLeoPopMenu.setAnimation(R.style.RightEnterAnim);
        mLeoPopMenu.setListViewDivider(null);
    }

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
                            // TODO 埋点
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
                LeoLog.d(TAG, "load done: " + imageUri);
                // TODO fill advertise view here
                fragment.initAdLayout(fragment.mRootView, mCampaign, loadedImage);
            }
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {

        }
    }

    private static AdPreviewLoaderListener sAdImageListener;

    private void initAdLayout(View rootView, Campaign campaign, Bitmap previewImage) {
        View adView = rootView.findViewById(R.id.ad_content);
        TextView tvTitle = (TextView) adView.findViewById(R.id.item_title);
        tvTitle.setText(campaign.getAppName());
        Button btnCTA = (Button) adView.findViewById(R.id.ad_result_cta);
        btnCTA.setText(campaign.getAdCall());
        ImageView preview = (ImageView) adView.findViewById(R.id.item_ad_preview);
        preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        preview.setImageBitmap(previewImage);
        ImageView iconView = (ImageView) adView.findViewById(R.id.ad_icon);
        ImageLoader.getInstance().displayImage(campaign.getIconUrl(), iconView);
        MobvistaEngine.getInstance(mActivity).registerView(Constants.UNIT_ID_CHARGING, adView);
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "ad_act", "adv_shws_scan");
        adView.setVisibility(View.VISIBLE);
        mSlideView.post(new Runnable() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(LOAD_DONE_INIT_PLACE);
            }
        });
    }
    /* 广告相关 - 结束 */

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

            View include = viewStub.inflate();
            mSwiftyTitle = (TextView) include.findViewById(R.id.item_title);
            mSwiftyImg = (ImageView) include.findViewById(R.id.swifty_img);
            mSwiftyContent = (TextView) include.findViewById(R.id.swifty_content);
            mSwiftyBtnLt = (RippleView) include.findViewById(R.id.item_btn_rv);
            mSwiftyBtnLt.setOnClickListener(this);
            mSwiftyContent.setText(preferenceTable.getString(PrefConst.KEY_CHARGE_SWIFTY_CONTENT));
            String imgUrl = preferenceTable.getString(PrefConst.KEY_CHARGE_SWIFTY_IMG_URL);
            mImageLoader.displayImage(imgUrl, mSwiftyImg, getOptions(R.drawable.swifty_banner));
            boolean isTitleEmpty = TextUtils.isEmpty(
                    preferenceTable.getString(PrefConst.KEY_CHARGE_SWIFTY_TITLE));
            if (!isTitleEmpty) {
                mSwiftyTitle.setText(preferenceTable.getString(
                        PrefConst.KEY_CHARGE_SWIFTY_TITLE));
            }
            mSlideView.post(new Runnable() {
                @Override
                public void run() {
                    mHandler.sendEmptyMessage(LOAD_DONE_INIT_PLACE);
                }
            });
        }
    }

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
            View include = viewStub.inflate();
            mExtraTitle = (TextView) include.findViewById(R.id.item_title);
            mExtraImg = (ImageView) include.findViewById(R.id.swifty_img);
            mExtraContent = (TextView) include.findViewById(R.id.swifty_content);
            mExtraBtnLt = (RippleView) include.findViewById(R.id.item_btn_rv);
            mExtraBtnLt.setOnClickListener(this);
            mExtraContent.setText(preferenceTable.getString(PrefConst.KEY_CHARGE_EXTRA_CONTENT));
            String imgUrl = preferenceTable.getString(PrefConst.KEY_CHARGE_EXTRA_IMG_URL);
            mImageLoader.displayImage(imgUrl, mExtraImg, getOptions(R.drawable.swifty_banner));
            boolean isTitleEmpty = TextUtils.isEmpty(
                    preferenceTable.getString(PrefConst.KEY_CHARGE_EXTRA_TITLE));
            if (!isTitleEmpty) {
                mExtraTitle.setText(preferenceTable.getString(
                        PrefConst.KEY_CHARGE_EXTRA_TITLE));
            }
            mSlideView.post(new Runnable() {
                @Override
                public void run() {
                    mHandler.sendEmptyMessage(LOAD_DONE_INIT_PLACE);
                }
            });
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
}
