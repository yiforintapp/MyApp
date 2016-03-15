package com.leo.appmaster.battery;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Contacts;
import android.text.Html;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.ad.ADEngineWrapper;
import com.leo.appmaster.ad.WrappedCampaign;
import com.leo.appmaster.applocker.manager.MobvistaEngine;
import com.leo.appmaster.db.PrefTableHelper;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.mgr.BatteryManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.privacycontact.CircleImageViewTwo;
import com.leo.appmaster.schedule.ScreenRecommentJob;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.AdWrapperLayout;
import com.leo.appmaster.ui.ResizableImageView;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class BatteryViewFragment extends BaseFragment implements View.OnTouchListener, BatteryTestViewLayout.ScrollBottomListener, View.OnClickListener {

    private static final String TAG = "BatteryViewFragment";
    private final int ANIMATION_TIME = 300;
    private final int MOVE_UP = 1;
    private final int MOVE_DOWN = 2;
    private final int LOAD_DONE_INIT_PLACE = 6;
    private final int RECOMMAND_TYPE_ONE = 1;
    private final int RECOMMAND_TYPE_TWO = 2;
    private final int RECOMMAND_TYPE_THREE = 3;

    private final int AD_TYPE_MSG = 1;
    private final int SWTIFY_TYPE_MSG = 2;
    private final int EXTRA_TYPE_MSG = 3;

    private static final int AD_LOAD_TIME = 3000;
    private final int DELAY_SHOW_AD = 2000;

    private final String GOOGLE = "Google";
    private final String AMAZON = "Amazon";
    private final String YAHOO = "Yahoo";
    private final String FACEBOOK = "Facebook";
    private final String TWITTER = "Twitter";
    private final String YOUTUBE = "YouTube";
    private final String HULU = "hulu";
    private final String TED = "TED";
    private final String VIMEO = "Vimeo";

    private final String AIQIYI = "com.qiyi.video";
    private final String BAIDUSHIPIN = "com.baidu.video";
    private final String SOUHU = "com.sohu.sohuvideo";
    private final String TENGXUNSHIPIN = "com.tencent.qqlive";
    private final String YOUKU = "com.youku.phone";


    public static boolean mShowing = false;
    public static boolean isExpand = false;
    public static boolean mIsExtraLayout = false;
    public static boolean mIsAdLayout = false;

    private View mRemainTimeContent;
    private View mRemainContent;
    private BatteryTestViewLayout mSlideView;

    private TextView mTvLevel;
    private TextView mTvBigTime;
    private TextView mTvSmallLeft;
    private TextView mTvSmallRight;
    //    private TextView mTvTime;
    private SelfScrollView mScrollView;
    private WaveView mBottleWater;
    private ImageView mIvLight;
    private TextView mTvHideTime;
    private TextView mTvHideText;
    private View mSettingView;
    private View mArrowMoveContent;
    private ImageView mIvArrowMove;
    private ImageView mIvCancel;
    private View mShowOne;
    private View mShowTwo;
    private View mShowThree;
    private View mRecommandView;
    private View mRecommandContentView;
    private GradientMaskView mMaskView;

    private TextView mPhoneHour;
    private TextView mPhoneHourText;
    private TextView mPhoneMin;
    private TextView mPhoneMinText;
    private TextView mNetHour;
    private TextView mNetHourText;
    private TextView mNetMin;
    private TextView mNetMinText;
    private TextView mPlayHour;
    private TextView mPlayHourText;
    private TextView mPlayMin;
    private TextView mPlayMinText;

    private View mRecommandNumOne;
    private View mRecommandNumTwo;
    private View mRecommandNumThree;
    private View mRecommandNumFour;

    private CircleImageViewTwo mIvShowOne;
    private CircleImageViewTwo mIvShowTwo;
    private CircleImageViewTwo mIvShowThree;
    private CircleImageViewTwo mIvShowFour;

    private TextView mRecommandTvOne;
    private TextView mRecommandTvTwo;
    private TextView mRecommandTvThree;
    private TextView mRecommandTvFour;

    private View mBatteryIconView;
    private View mTimeContentAll;
    private View mTimeContentView;

    private View mBackGroundView;

    public static int mScreenHeight;

    private long mInitTime;
    private int mCurrentClickType = -1;

    private BatteryManager.BatteryState newState;
    private String mChangeType = BatteryManager.SHOW_TYPE_IN;
    private int mRemainTime;
    private int[] mRemainTimeArr;
    private View mBossView;
    private boolean isSetInitPlace = false;

    /*  */
    private View mAdView = null;
    private AdWrapperLayout mAdWrapper = null;
    private Runnable mClickRunnable = null;

    /* 用于更新时间 */
    private Timer mUpdateTimer;
    private UpdateTimeTask mUpdateTask;
    private int nowSetType = 0;
    private boolean isShowMask = false;
    private boolean isClickable = true;

    private boolean smallScreen = false;
    private boolean midScreen = false;

    private View mFolderIndicator;

    /**
     * 第一个推广位
     */
    private TextView mSwiftyTitle;
    private ImageView mSwiftyImg;
    private TextView mSwiftyContent;
    private RelativeLayout mSwiftyLayout;

    /**
     * 预留推广位
     */
    private TextView mExtraTitle;
    private ImageView mExtraImg;
    private TextView mExtraContent;
    private RelativeLayout mExtraLayout;

    private ImageLoader mImageLoader;

    //    private boolean mShowBoost = true;
    private int mAdSource = ADEngineWrapper.SOURCE_MOB; // 默认值

    private List<PackageInfo> mPackages;

    public static String[] days = AppMasterApplication.getInstance().getResources()
            .getStringArray(R.array.days_of_week);

    //开始首页动画
    private android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MOVE_UP:
                    if (mBossView != null && mBossView.getVisibility() == View.VISIBLE &&
                            mArrowMoveContent.getVisibility() == View.VISIBLE) {
                        SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "batterypage", "screen_up");
                        mIvArrowMove.setBackgroundResource(R.drawable.bay_arrow_down);
                        mShowing = true;
                        showMoveUp();
                        theRestPartHide();
                    }
                    break;
                case MOVE_DOWN:
                    SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "batterypage", "screen_down");
                    mIvArrowMove.setBackgroundResource(R.drawable.bay_arrow_up);
                    mShowing = true;
                    showMoveDown();
                    theRestPartShow();
                    break;
                case LOAD_DONE_INIT_PLACE:
                    int type = (Integer) msg.obj;

                    //是否满足忽略按钮
                    long lastIgnore = PreferenceTable.getInstance().getLong(Constants.AD_CLICK_IGNORE, 0);
                    long now = System.currentTimeMillis();
                    long internal = PrefTableHelper.getIgnoreTs() * 60 * 60 * 1000;//hours change to mmin
                    LeoLog.d("locationP", "now - lastIgnore : " + (now - lastIgnore) + " . internal : " + internal);
                    if (now - lastIgnore > internal) {
                        reLocateMoveContent(type);
                    }
                    break;
            }
        }
    };

    private void theRestPartShow() {
        ObjectAnimator anim20 = ObjectAnimator.ofFloat(mRemainTimeContent,
                "scaleX", 0f, 1.0f);
        ObjectAnimator anim21 = ObjectAnimator.ofFloat(mRemainTimeContent,
                "scaleY", 0f, 1.0f);
        ObjectAnimator anim22 = ObjectAnimator.ofFloat(mRemainTimeContent,
                "alpha", 0f, 1.0f);

        ObjectAnimator anim23 = ObjectAnimator.ofFloat(mRemainContent,
                "scaleX", 0f, 1.0f);
        ObjectAnimator anim24 = ObjectAnimator.ofFloat(mRemainContent,
                "scaleY", 0f, 1.0f);
        ObjectAnimator anim25 = ObjectAnimator.ofFloat(mRemainContent,
                "alpha", 0f, 1.0f);


        AnimatorSet set = new AnimatorSet();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mRemainTimeContent.setVisibility(View.VISIBLE);
                mRemainContent.setVisibility(View.VISIBLE);
            }
        });
        set.setDuration(ANIMATION_TIME);
        set.play(anim20).with(anim21);
        set.play(anim21).with(anim22);
        set.play(anim22).with(anim23);
        set.play(anim23).with(anim24);
        set.play(anim24).with(anim25);
        set.start();
    }

    private void theRestPartHide() {
        ObjectAnimator anim20 = ObjectAnimator.ofFloat(mRemainTimeContent,
                "scaleX", 1.0f, 0f);
        ObjectAnimator anim21 = ObjectAnimator.ofFloat(mRemainTimeContent,
                "scaleY", 1.0f, 0f);
        ObjectAnimator anim22 = ObjectAnimator.ofFloat(mRemainTimeContent,
                "alpha", 1.0f, 0f);

        ObjectAnimator anim23 = ObjectAnimator.ofFloat(mRemainContent,
                "scaleX", 1.0f, 0f);
        ObjectAnimator anim24 = ObjectAnimator.ofFloat(mRemainContent,
                "scaleY", 1.0f, 0f);
        ObjectAnimator anim25 = ObjectAnimator.ofFloat(mRemainContent,
                "alpha", 1.0f, 0f);


        AnimatorSet set = new AnimatorSet();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mRemainTimeContent.setVisibility(View.INVISIBLE);
                mRemainContent.setVisibility(View.INVISIBLE);
            }
        });
        set.setDuration(ANIMATION_TIME);
        set.play(anim20).with(anim21);
        set.play(anim21).with(anim22);
        set.play(anim22).with(anim23);
        set.play(anim23).with(anim24);
        set.play(anim24).with(anim25);
        set.start();
    }

    private int mMoveDisdance;
    private BatteryBoostController mBoostView;

    private void reLocateMoveContent(int type) {
        if (mSlideView != null) {
            LeoLog.d("locationP", "slideview Y : " + mSlideView.getY());
        }
        int contentHeight = 0;

        if (mBossView != null) {
            contentHeight = mBossView.getHeight();
            LeoLog.d("locationP", "mBossView.getHeight() : " + mBossView.getHeight());
        }

        setYplace(contentHeight, type);
    }

    private int arrowHeight;

    private void setYplace(int contentHeight, final int type) {

        if (!isSetInitPlace) {
            mMoveDisdance = contentHeight * 9 / 16;

            arrowHeight = mArrowMoveContent.getHeight();
            if (type == AD_TYPE_MSG) {
                if (mAdWrapper != null) {
                    nowSetType = type;
                    mMoveDisdance = contentHeight - mAdWrapper.getHeight() - arrowHeight - 20;
                    LeoLog.d("locationP", "mAdWrapper.getHeight() : " + mAdWrapper.getHeight());
                }
            } else if (type == SWTIFY_TYPE_MSG) {
                if (mSwiftyView != null) {
                    nowSetType = type;
                    mMoveDisdance = contentHeight - mSwiftyView.getHeight() - arrowHeight - 20;
                    LeoLog.d("locationP", "mSwiftyView.getHeight() : " + mSwiftyView.getHeight());
                }
            } else if (type == EXTRA_TYPE_MSG) {
                if (mExtraView != null && mExtraView.getHeight() > 0) {
                    nowSetType = type;
                    mMoveDisdance = contentHeight - mExtraView.getHeight() - arrowHeight - 20;
                    LeoLog.d("locationP", "mExtraView.getHeight() : " + mExtraView.getHeight());
                }
            }

            int biggestDistance = 0;
            if (mBossView != null) {
                biggestDistance = mBossView.getHeight() / 3;
            }
            if (mMoveDisdance < biggestDistance) {
                mMoveDisdance = biggestDistance;
                LeoLog.d("locationP", "so high , reset");
            }

            if (type != AD_TYPE_MSG) {
                mMoveDisdance = mMoveDisdance + arrowHeight;
                if (smallScreen) {
                    mMoveDisdance = mMoveDisdance + arrowHeight * 2;
                } else if (midScreen) {
                    mMoveDisdance = mMoveDisdance + arrowHeight;
                }
            } else {
                if (smallScreen || midScreen) {
                    mMoveDisdance = mMoveDisdance + arrowHeight * 2;
                }
            }


            if (mSlideView == null) {
                return;
            }

            LeoLog.d("locationP", "mMoveDisdance : " + mMoveDisdance);
            ObjectAnimator animMoveY = ObjectAnimator.ofFloat(mSlideView,
                    "y", contentHeight, mSlideView.getTop() + mMoveDisdance);
            animMoveY.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    boolean isExpandContentShow = mRecommandView.getVisibility() == View.VISIBLE;
                    if (isExpandContentShow) {
                        mCurrentClickType = 0;
                        shrinkRecommandContent(false);
                    }
                    if (mBossView != null) {
                        mBossView.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                    if (type != AD_TYPE_MSG || smallScreen || midScreen) {
                        mArrowMoveContent.setVisibility(View.VISIBLE);
                        if (mMaskView != null) {
                            mMaskView.showMask();
                        }
                        isShowMask = true;
                    }

                }
            });
            animMoveY.setDuration(300);
            animMoveY.start();
            isSetInitPlace = true;
            mArrowMoveContent.setVisibility(View.INVISIBLE);
            if (mMaskView != null) {
                mMaskView.hideMask();
            }
        } else {
            if (nowSetType != type) {
                mArrowMoveContent.setVisibility(View.VISIBLE);
                if (mMaskView != null) {
                    mMaskView.showMask();
                }
                isShowMask = true;
            }
        }
    }


    @Override
    protected int layoutResourceId() {
        return R.layout.activity_battery_view_newter;
    }

    @Override
    protected void onInitUI() {
        LeoLog.d(TAG, "INIT UI");
        mImageLoader = ImageLoader.getInstance();

        mBackGroundView = findViewById(R.id.background_pic);
        mBackGroundView.setOnClickListener(this);

        mRemainTimeContent = findViewById(R.id.remain_time);
        mRemainContent = findViewById(R.id.use_time_content);

        mTvBigTime = (TextView) findViewById(R.id.time_big);
        mTvSmallLeft = (TextView) findViewById(R.id.time_small);
        mTvSmallRight = (TextView) findViewById(R.id.time_small_right);

        mTvLevel = (TextView) findViewById(R.id.battery_num);


        mBottleWater = (WaveView) findViewById(R.id.bottle_water);
        mIvLight = (ImageView) findViewById(R.id.iv_battery_light);
//        mBottleWater.setPostInvalidateDelayMs(40);
        mBottleWater.setWaveColor(0xff0ad931);
        mBottleWater.setWave2Color(0xff0ab522);
        mBottleWater.setFactorA(DipPixelUtil.dip2px(mActivity, 2.5f));
        mBottleWater.setSpeed1(DipPixelUtil.dip2px(mActivity, 2f));
        mBottleWater.setSpeed2(DipPixelUtil.dip2px(mActivity, 1.5f));

        mTvHideTime = (TextView) findViewById(R.id.hide_tv_one);
        mTvHideText = (TextView) findViewById(R.id.hide_tv_two);

        mSettingView = findViewById(R.id.ct_option_2_rl);
        mSettingView.setOnClickListener(this);

        mIvCancel = (ImageView) findViewById(R.id.slider);
        mIvCancel.setOnClickListener(this);

        mRecommandNumOne = findViewById(R.id.show_one);
        mRecommandNumTwo = findViewById(R.id.show_two);
        mRecommandNumThree = findViewById(R.id.show_three);
        mRecommandNumFour = findViewById(R.id.show_four);

        mIvShowOne = (CircleImageViewTwo) findViewById(R.id.iv_show_one);
        mIvShowTwo = (CircleImageViewTwo) findViewById(R.id.iv_show_two);
        mIvShowThree = (CircleImageViewTwo) findViewById(R.id.iv_show_three);
        mIvShowFour = (CircleImageViewTwo) findViewById(R.id.iv_show_four);

        mRecommandTvOne = (TextView) findViewById(R.id.tv_show_one);
        mRecommandTvTwo = (TextView) findViewById(R.id.tv_show_two);
        mRecommandTvThree = (TextView) findViewById(R.id.tv_show_three);
        mRecommandTvFour = (TextView) findViewById(R.id.tv_show_four);

        mPhoneHour = (TextView) findViewById(R.id.tv_one_time_one);
        mPhoneHourText = (TextView) findViewById(R.id.tv_one_time_two);
        mPhoneMin = (TextView) findViewById(R.id.tv_one_time_three);
        mPhoneMinText = (TextView) findViewById(R.id.tv_one_time_four);

        mNetHour = (TextView) findViewById(R.id.tv_two_time_one);
        mNetHourText = (TextView) findViewById(R.id.tv_two_time_two);
        mNetMin = (TextView) findViewById(R.id.tv_two_time_three);
        mNetMinText = (TextView) findViewById(R.id.tv_two_time_four);

        mPlayHour = (TextView) findViewById(R.id.tv_three_time_one);
        mPlayHourText = (TextView) findViewById(R.id.tv_three_time_two);
        mPlayMin = (TextView) findViewById(R.id.tv_three_time_three);
        mPlayMinText = (TextView) findViewById(R.id.tv_three_time_four);

        mBatteryIconView = findViewById(R.id.infos_content);
        mTimeContentAll = findViewById(R.id.time_content);
        mTimeContentView = findViewById(R.id.time_move_content);

        mPackages = mActivity.getPackageManager().getInstalledPackages(0);

        mShowOne = findViewById(R.id.remain_one);
        mShowOne.setTag(true);
        mShowOne.setOnClickListener(this);
        makeSmall(mShowOne);
        mShowTwo = findViewById(R.id.remain_two);
        mShowTwo.setTag(true);
        mShowTwo.setOnClickListener(this);
        makeSmall(mShowTwo);
        mShowThree = findViewById(R.id.remain_three);
        mShowThree.setTag(true);
        mShowThree.setOnClickListener(this);
        makeSmall(mShowThree);

        WindowManager windowManager = mActivity.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        mScreenHeight = display.getHeight();
        LeoLog.d("testBatteryView", "screenHeight:" + mScreenHeight);
        if (mScreenHeight <= 320) {
            smallScreen = true;
        } else if (mScreenHeight <= 480) {
            midScreen = true;
        }

        mInitTime = System.currentTimeMillis();

        mRecommandView = findViewById(R.id.three_show_content);
        mRecommandContentView = findViewById(R.id.show_small_content);
        mFolderIndicator = findViewById(R.id.folder_indicator);
        LeoLog.e("currentX", "currentX:" + mFolderIndicator.getX());


        if (newState != null) {
            process(mChangeType, newState, mRemainTime, mRemainTimeArr);
        }

//        if (mShowBoost) {
        initBoostLayout();
//        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mActivity.registerReceiver(mPresentReceiver, intentFilter);
    }

    private void initBoostLayout() {
        BatteryManager btrManager = (BatteryManager) MgrContext.getManager(MgrContext.MGR_BATTERY);
        boolean isBatteryPowSavOpen = btrManager.getBatteryPowSavStatus();
        if (PrefTableHelper.shouldBatteryBoost() && isBatteryPowSavOpen) {
            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "batterypage", "screen_save");
            ViewStub viewStub = (ViewStub) findViewById(R.id.boost_stub);
            mBoostView = (BatteryBoostController) viewStub.inflate();

            mRemainTimeContent.setVisibility(View.INVISIBLE);
            mRemainContent.setVisibility(View.INVISIBLE);

            timeTurnBig();

            mBoostView.setBoostFinishListener(new BatteryBoostController.OnBoostFinishListener() {
                @Override
                public void onBoostFinish() {
                    PreferenceTable.getInstance().putLong(PrefConst.KEY_LAST_BOOST_TS, System.currentTimeMillis());
                    checkingData(true);
                    showViewAfterBoost(true);
                    timeTurnSmall();
                }
            });
        } else {
            checkingData(false);
            showViewAfterBoost(false);
        }
    }

    private void timeTurnSmall() {
        ObjectAnimator anim20 = ObjectAnimator.ofFloat(mTimeContentAll,
                "scaleX", 1.4f, 1.0f);
        ObjectAnimator anim21 = ObjectAnimator.ofFloat(mTimeContentAll,
                "scaleY", 1.4f, 1.0f);

        AnimatorSet set = new AnimatorSet();
        set.setDuration(100);
        set.play(anim20).with(anim21);
        set.start();
    }

    private void timeTurnBig() {
        ObjectAnimator anim20 = ObjectAnimator.ofFloat(mTimeContentAll,
                "scaleX", 1.0f, 1.4f);
        ObjectAnimator anim21 = ObjectAnimator.ofFloat(mTimeContentAll,
                "scaleY", 1.0f, 1.4f);

        AnimatorSet set = new AnimatorSet();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mTimeContentView.setVisibility(View.VISIBLE);
            }
        });
        set.setDuration(50);
        set.play(anim20).with(anim21);
        set.start();
    }

    private void checkingData(boolean checking) {
        if (checking) {
            mTimeContentView.post(new Runnable() {
                @Override
                public void run() {

                    ObjectAnimator animMoveY = ObjectAnimator.ofFloat(mTimeContentView,
                            "x", mTimeContentView.getLeft(), mTimeContentView.getLeft() -
                                    mTimeContentView.getWidth() / 2 - DipPixelUtil.dip2px(mActivity, 15));
                    animMoveY.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            bayIconTurnBig();
                        }
                    });

                    animMoveY.setDuration(500);
                    animMoveY.start();
                }
            });
        } else {
            mTimeContentView.post(new Runnable() {
                @Override
                public void run() {
                    ObjectAnimator animMoveY = ObjectAnimator.ofFloat(mTimeContentView,
                            "x", mTimeContentView.getLeft(), mTimeContentView.getLeft() -
                                    mTimeContentView.getWidth() / 2 - DipPixelUtil.dip2px(mActivity, 15));

                    ObjectAnimator animMoveY2 = ObjectAnimator.ofFloat(mBatteryIconView,
                            "x", mBatteryIconView.getLeft(), mBatteryIconView.getLeft() +
                                    mBatteryIconView.getWidth() / 2 + DipPixelUtil.dip2px(mActivity, 5));


                    AnimatorSet set = new AnimatorSet();
                    set.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mTimeContentView.setVisibility(View.VISIBLE);
                            mBatteryIconView.setVisibility(View.VISIBLE);
                        }
                    });
                    set.setDuration(20);
                    set.play(animMoveY).with(animMoveY2);
                    set.start();
                }
            });
        }
    }

    private void bayIconTurnBig() {
        ObjectAnimator animMoveY = ObjectAnimator.ofFloat(mBatteryIconView,
                "x", mBatteryIconView.getLeft(), mBatteryIconView.getLeft() +
                        mBatteryIconView.getWidth() / 2 + DipPixelUtil.dip2px(mActivity, 5));
        animMoveY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mBatteryIconView.setVisibility(View.VISIBLE);
            }
        });

        ObjectAnimator anim20 = ObjectAnimator.ofFloat(mBatteryIconView,
                "scaleX", 0f, 1.0f);
        ObjectAnimator anim21 = ObjectAnimator.ofFloat(mBatteryIconView,
                "scaleY", 0f, 1.0f);
        ObjectAnimator anim22 = ObjectAnimator.ofFloat(mBatteryIconView,
                "alpha", 0f, 1f);

        AnimatorSet set = new AnimatorSet();
        set.setDuration(500);
        set.play(animMoveY).before(anim20);
        set.play(anim20).with(anim21);
        set.play(anim21).with(anim22);
        set.start();
    }

    private void showViewAfterBoost(boolean afterAnimation) {
        mRemainTimeContent.setVisibility(View.VISIBLE);
        mRemainContent.setVisibility(View.VISIBLE);

        ViewStub viewStub = (ViewStub) findViewById(R.id.bay_advertise_stub);
        mBossView = viewStub.inflate();
        mBossView.setOnTouchListener(this);

        mSlideView = (BatteryTestViewLayout) findViewById(R.id.slide_content);

        mScrollView = (SelfScrollView) findViewById(R.id.slide_content_sv);
        mScrollView.setParent(mSlideView);
        mSlideView.setScrollBottomListener(this);

        mArrowMoveContent = findViewById(R.id.move_arrow);
        mArrowMoveContent.setOnTouchListener(this);
        mIvArrowMove = (ImageView) findViewById(R.id.iv_move_arrow);

        mMaskView = (GradientMaskView) findViewById(R.id.mask_view);

        if (afterAnimation) {
            startRemindTimeAppearAnim();
        } else {
            expandRecommandContent(RECOMMAND_TYPE_TWO, true);
        }
        ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    // 将加速动画view从父布局里移除，降低屏幕渲染压力
                    ViewGroup viewGroup = (ViewGroup) mBoostView.getParent();
                    viewGroup.removeView(mBoostView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 1000);
    }

    private void initThreeContent() {
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
    }

    private void startRemindTimeAppearAnim() {
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mRemainContent, "alpha", 0f, 255f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mRemainContent, "scaleX", 0.8f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mRemainContent, "scaleY", 0.8f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(500);
        animatorSet.playTogether(alpha, scaleX, scaleY);
        animatorSet.start();

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                expandRecommandContent(RECOMMAND_TYPE_TWO, true);
            }
        });
    }

    private void expandRecommandContent(final int recommandTypeThree, final boolean firInLoad) {
        if (mActivity == null) return;
        turnDark(recommandTypeThree);
        isClickable = false;
        if (mMaskView != null) {
            mMaskView.hideMask();
        }

        mRecommandView.setVisibility(View.VISIBLE);

        if (mBossView != null) {
            boolean isSlideContentShow = mBossView.getVisibility() == View.VISIBLE;
            if (isSlideContentShow) {
                adDimissAnima();
            }
        }


        mRecommandView.clearAnimation();
        mRecommandContentView.setVisibility(View.INVISIBLE);
        Animation expand = AnimationUtils.
                loadAnimation(mActivity, R.anim.file_floder_expand_anim);
        expand.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                fillShowContentData(recommandTypeThree);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mRecommandContentView.setVisibility(View.VISIBLE);
                if (firInLoad) {
                    initThreeContent();
                }
                isClickable = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mRecommandView.setAnimation(expand);
        setTheIndicatorX(recommandTypeThree);

    }

    /** 设置指示器位置 */
    private void setTheIndicatorX(int type) {
        float positionX = 0;
        float margeX = DipPixelUtil.dip2px(mActivity, 8);
        if (RECOMMAND_TYPE_ONE == type) {
            positionX = mShowOne.getLeft() + mShowOne.getWidth() / 2 - margeX;
        } else if (RECOMMAND_TYPE_TWO == type) {
            positionX = mShowTwo.getLeft() + mShowTwo.getWidth() / 2 - margeX;
        } else if (RECOMMAND_TYPE_THREE == type) {
            positionX = mShowThree.getLeft() + mShowThree.getWidth() / 2 - margeX;
        }
        if (positionX > 0) {  // 首次展开时positionX小于0,不需要手动设置指示器位置
            mFolderIndicator.setX(positionX);
        }
    }

    /** 指示器动画 */
    private void  showIndicatorAnim(int type) {
        ObjectAnimator moveAnim;
        float endX = DipPixelUtil.dip2px(mActivity, 8);
        float currentX = mFolderIndicator.getX();
        if (RECOMMAND_TYPE_ONE == type) {
            endX = mShowOne.getLeft() + mShowOne.getWidth() / 2 - endX;
        } else if (RECOMMAND_TYPE_TWO == type) {
            endX = mShowTwo.getLeft() + mShowTwo.getWidth() / 2 - endX;
        } else if (RECOMMAND_TYPE_THREE == type) {
            endX = mShowThree.getLeft() + mShowThree.getWidth() / 2 - endX;
        }
        moveAnim =ObjectAnimator.ofFloat(mFolderIndicator, "x", currentX, endX);
        final float  finalX = endX;
        moveAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mFolderIndicator.setX(finalX);
            }
        });
        moveAnim.setDuration(300);
        moveAnim.start();
    }

    private void adDimissAnima() {
        if (mSlideView == null) {
            return;
        }
        ObjectAnimator animMoveY = ObjectAnimator.ofFloat(mSlideView,
                "y", mSlideView.getTop() + mMoveDisdance, mSlideView.getTop() + mSlideView.getHeight());
        animMoveY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mSlideView.setVisibility(View.INVISIBLE);
            }
        });
        animMoveY.setDuration(300);
        animMoveY.start();
    }

    private void adShowAnima() {
        if (mSlideView == null) {
            return;
        }
        mSlideView.setVisibility(View.VISIBLE);
        ObjectAnimator animMoveY = ObjectAnimator.ofFloat(mSlideView,
                "y", mSlideView.getTop() + mSlideView.getHeight(), mSlideView.getTop() + mMoveDisdance);
        animMoveY.setDuration(300);
        animMoveY.start();
    }


    private void fillShowContentData(int recommandTypeThree) {
        if (mActivity == null) return;
        if (recommandTypeThree == RECOMMAND_TYPE_ONE) {
            List<BatteryAppItem> phoneList = ScreenRecommentJob.getBatteryCallList();
            LeoLog.d("testGetList", "3G通话 size is : " + phoneList.size());
            for (int i = 0; i < phoneList.size(); i++) {
                LeoLog.d("testGetList", "名字: " + phoneList.get(i).name);
                LeoLog.d("testGetList", "包名: " + phoneList.get(i).pkg);
                LeoLog.d("testGetList", "Url: " + phoneList.get(i).actionUrl);
                LeoLog.d("testGetList", "iconUrl: " + phoneList.get(i).iconUrl);
            }
            LeoLog.d("testGetList", "-------------分割线---------------");
            //fill the local ,  size of : 3
            mIvShowOne.setSelfImageDrawable(getResources().getDrawable(R.drawable.icon_time_contacts), false);
            mRecommandTvOne.setText(getString(R.string.battery_protect_show_num_contact));
            mRecommandNumOne.setVisibility(View.VISIBLE);
            mRecommandNumOne.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Contacts.People.CONTENT_URI);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        startActivity(intent);
                        mActivity.finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            mIvShowTwo.setSelfImageDrawable(getResources().getDrawable(R.drawable.icon_time_phone), false);
            mRecommandTvTwo.setText(getString(R.string.battery_protect_show_num_call));
            mRecommandNumTwo.setVisibility(View.VISIBLE);
            mRecommandNumTwo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        startActivity(intent);
                        mActivity.finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            mIvShowThree.setSelfImageDrawable(getResources().getDrawable(R.drawable.icon_time_message), false);
            mRecommandTvThree.setText(getString(R.string.battery_protect_show_num_msm));
            mRecommandNumThree.setVisibility(View.VISIBLE);
            mRecommandNumThree.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    try {
                        Intent intentM = new Intent(Intent.ACTION_VIEW);
                        intentM.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ComponentName cnM = new ComponentName("com.android.mms", "com.android.mms.ui.ConversationList");
                        intentM.setComponent(cnM);
                        startActivity(intentM);
                        mActivity.finish();
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        //努比亚短信列表
                        Intent intentN = new Intent(Intent.ACTION_VIEW);
                        intentN.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ComponentName cnN = new ComponentName("com.android.contacts", "com.android.contacts.MmsConversationActivity");
                        intentN.setComponent(cnN);
                        startActivity(intentN);
                        mActivity.finish();
                        return;
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    //三星
                    try {
                        Intent intentS = new Intent(Intent.ACTION_VIEW);
                        intentS.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ComponentName cnS = new ComponentName("com.android.mms", "com.android.mms.ui.ConversationComposer");
                        intentS.setComponent(cnS);
                        startActivity(intentS);
                        mActivity.finish();
                        return;
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    //qiku
                    try {
                        Intent intentQiku = new Intent(Intent.ACTION_VIEW);
                        intentQiku.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ComponentName cnQiku = new ComponentName("com.android.mms", "com.yulong.android.mms.ui.MmsConversationListActivity");
                        intentQiku.setComponent(cnQiku);
                        startActivity(intentQiku);
                        mActivity.finish();
                        return;
                    } catch (Exception e3) {
                        e3.printStackTrace();
                    }
                    //huawei p8
                    try {
                        Intent intentHwP8 = new Intent(Intent.ACTION_MAIN);
                        intentHwP8.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ComponentName cnHwP8 = new ComponentName("com.android.contacts", "com.android.mms.ui.ConversationList");
                        intentHwP8.setComponent(cnHwP8);
                        startActivity(intentHwP8);
                        mActivity.finish();
                        return;
                    } catch (Exception e4) {
                        e4.printStackTrace();
                    }
                    //sony
                    try {
                        Intent intentSony = new Intent(Intent.ACTION_MAIN);
                        intentSony.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ComponentName cnSony = new ComponentName("com.sonyericsson.conversations", "com.sonyericsson.conversations.ui.ConversationListActivity");
                        intentSony.setComponent(cnSony);
                        startActivity(intentSony);
                        mActivity.finish();
                        return;
                    } catch (Exception e5) {
                        e5.printStackTrace();
                    }
                    try {
                        // ASUS
                        Intent intentAsus = new Intent(Intent.ACTION_MAIN);
                        intentAsus.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ComponentName cnAsus = new ComponentName("com.asus.message", "com.android.mms.ui.ConversationList");
                        intentAsus.setComponent(cnAsus);
                        startActivity(intentAsus);
                        mActivity.finish();
                        return;
                    } catch (Exception e6) {
                        e6.printStackTrace();
                    }
                    try {
                        //lenovo
                        Intent intentLenovo = new Intent(Intent.ACTION_MAIN);
                        intentLenovo.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ComponentName cnLenovo = new ComponentName("com.lenovo.ideafriend", "com.lenovo.ideafriend.alias.MmsActivity");
                        intentLenovo.setComponent(cnLenovo);
                        startActivity(intentLenovo);
                        mActivity.finish();
                        return;
                    } catch (Exception e7) {
                        e7.printStackTrace();
                    }
                    //Nexus 6
                    try {
                        Intent intentNexus6 = new Intent(Intent.ACTION_MAIN);
                        intentNexus6.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ComponentName cnNexus6 = new ComponentName("com.google.android.apps.messaging", "com.google.android.apps.messaging.ui.ConversationListActivity");
                        intentNexus6.setComponent(cnNexus6);
                        startActivity(intentNexus6);
                        mActivity.finish();
                        return;
                    } catch (Exception e8) {
                        e8.printStackTrace();
                    }

                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.setType("vnd.android-dir/mms-sms");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        startActivity(intent);
                        mActivity.finish();
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    mActivity.finish();
                }
            });

            //4
            String name = null;
            Drawable map = null;
            if (phoneList != null && phoneList.size() > 0) {
                BatteryAppItem info = phoneList.get(0);
                final BatteryAppItem infoCopy = info;
                for (int i = 0; i < mPackages.size(); i++) {
                    PackageInfo packageInfo = mPackages.get(i);
                    if (packageInfo.applicationInfo.packageName.equals(info.pkg)) {
                        name = packageInfo.applicationInfo.loadLabel(mActivity.getPackageManager()).toString();
                        map = packageInfo.applicationInfo.loadIcon(mActivity.getPackageManager());
                        break;
                    }
                }
                if (name != null && map != null) {
                    mRecommandNumFour.setVisibility(View.VISIBLE);
                    mIvShowFour.setSelfImageDrawable(map, true);
                    mRecommandTvFour.setText(name);
                    mRecommandNumFour.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
                            resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                            resolveIntent.setPackage(infoCopy.pkg);
                            List<ResolveInfo> resolveinfoList = mActivity.getPackageManager()
                                    .queryIntentActivities(resolveIntent, 0);
                            ResolveInfo resolveinfo = resolveinfoList.iterator().next();
                            if (resolveinfo != null) {
                                SDKWrapper.addEvent(mActivity, SDKWrapper.P1,
                                        "batterypage",
                                        "call_" + infoCopy.pkg);
                                String className = resolveinfo.activityInfo.name;
                                Intent intent = new Intent(Intent.ACTION_MAIN);
                                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                ComponentName cn = new ComponentName(infoCopy.pkg, className);
                                intent.setComponent(cn);
                                try {
                                    startActivity(intent);
                                    mActivity.finish();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    });
                } else {
                    mRecommandNumFour.setVisibility(View.INVISIBLE);
                }
            } else {
                mRecommandNumFour.setVisibility(View.INVISIBLE);
            }

        } else if (recommandTypeThree == RECOMMAND_TYPE_TWO) {
            twoContentFill();
        } else {
            threeContentFill();
        }
    }

    private void twoContentFill() {
        List<BatteryAppItem> netList = ScreenRecommentJob.getBatteryNetList();
        LeoLog.d("testGetList", "上网 size is : " + netList.size());
        mIvShowOne.setSelfImageDrawable(getResources().getDrawable(R.drawable.icon_time_browser), false);
        mRecommandTvOne.setText(getString(R.string.battery_protect_show_num_browser));
        mRecommandNumOne.setVisibility(View.VISIBLE);
        mRecommandNumOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickRunnable = new Runnable() {
                    @Override
                    public void run() {
                        String browserPack = getBrowserApp(mActivity);
                        if (!Utilities.isEmpty(browserPack)) {
                            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
                            resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                            resolveIntent.setPackage(browserPack);
                            List<ResolveInfo> resolveinfoList = mActivity.getPackageManager()
                                    .queryIntentActivities(resolveIntent, 0);
                            ResolveInfo resolveinfo = resolveinfoList.iterator().next();
                            if (resolveinfo != null) {
                                String className = resolveinfo.activityInfo.name;
                                Intent intent = new Intent(Intent.ACTION_MAIN);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                                ComponentName cn = new ComponentName(browserPack, className);
                                intent.setComponent(cn);
                                try {
                                    startActivity(intent);
                                    mActivity.finish();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                };
                handleRunnable();
            }
        });

        //2
        if (netList != null && netList.size() > 0) {
            mRecommandNumTwo.setVisibility(View.VISIBLE);
            BatteryAppItem typeTwo = netList.get(0);
            gotoBrowser(typeTwo, mIvShowTwo, mRecommandTvTwo, mRecommandNumTwo, RECOMMAND_TYPE_TWO);
        } else {
            mRecommandNumTwo.setVisibility(View.INVISIBLE);
        }

        //3
        if (netList != null && netList.size() > 1) {
            mRecommandNumThree.setVisibility(View.VISIBLE);
            BatteryAppItem typeThree = netList.get(1);
            gotoBrowser(typeThree, mIvShowThree, mRecommandTvThree, mRecommandNumThree, RECOMMAND_TYPE_TWO);
        } else {
            mRecommandNumThree.setVisibility(View.INVISIBLE);
        }

        //4
        if (netList != null && netList.size() > 2) {
            mRecommandNumFour.setVisibility(View.VISIBLE);
            BatteryAppItem typeFour = netList.get(2);
            gotoBrowser(typeFour, mIvShowFour, mRecommandTvFour, mRecommandNumFour, RECOMMAND_TYPE_TWO);
        } else {
            mRecommandNumFour.setVisibility(View.INVISIBLE);
        }
    }

    private void threeContentFill() {
        List<BatteryAppItem> playList = ScreenRecommentJob.getBatteryVideoList();
        LeoLog.d("testGetList", "玩应用 size is : " + playList.size());

        List<BatteryAppItem> fitList = new ArrayList<BatteryAppItem>();
        for (int i = 0; i < playList.size(); i++) {
            boolean isRightinfo = getRightInfo(playList, i);
            if (isRightinfo) {
                fitList.add(playList.get(i));
            }
        }

        //one
        if (fitList != null && fitList.size() > 0) {
            mRecommandNumOne.setVisibility(View.VISIBLE);
            BatteryAppItem infoOne = fitList.get(0);
            threeIconFill(infoOne, mIvShowOne, mRecommandTvOne, mRecommandNumOne);
        } else {
            mRecommandNumOne.setVisibility(View.INVISIBLE);
        }

        //two
        if (fitList != null && fitList.size() > 1) {
            mRecommandNumTwo.setVisibility(View.VISIBLE);
            BatteryAppItem infoTwo = fitList.get(1);
            threeIconFill(infoTwo, mIvShowTwo, mRecommandTvTwo, mRecommandNumTwo);
        } else {
            mRecommandNumTwo.setVisibility(View.INVISIBLE);
        }

        //three
        if (fitList != null && fitList.size() > 2) {
            mRecommandNumThree.setVisibility(View.VISIBLE);
            BatteryAppItem infoThree = fitList.get(2);
            threeIconFill(infoThree, mIvShowThree, mRecommandTvThree, mRecommandNumThree);
        } else {
            mRecommandNumThree.setVisibility(View.INVISIBLE);
        }

        //four
        if (fitList != null && fitList.size() > 3) {
            mRecommandNumFour.setVisibility(View.VISIBLE);
            BatteryAppItem infoFour = fitList.get(3);
            threeIconFill(infoFour, mIvShowFour, mRecommandTvFour, mRecommandNumFour);
        } else {
            mRecommandNumFour.setVisibility(View.INVISIBLE);
        }

    }

    private void threeIconFill(BatteryAppItem info, CircleImageViewTwo mIvShow, TextView mRecommandTv, View mRecommandNum) {
        boolean isPackExist = isExist(info.pkg);
        if (isPackExist) {
            gotoApp(info, mIvShow, mRecommandTv, mRecommandNum);
        } else {
            gotoBrowser(info, mIvShow, mRecommandTv, mRecommandNum, RECOMMAND_TYPE_THREE);
        }
    }

    private void startBrowser(String url) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (!url.contains("://")) {
            url = "http://" + url;
        }

        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        try {
            startActivity(intent);
            mActivity.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void gotoBrowser(final BatteryAppItem infoOne, CircleImageViewTwo icon, TextView title, View contentView, final int type) {

        final String urlFour = infoOne.actionUrl;
        if (!Utilities.isEmpty(infoOne.iconUrl)) {
            mImageLoader.displayImage(infoOne.iconUrl, icon, getOptions(R.drawable.default_user_avatar));
        } else {
            icon.setSelfImageDrawable(getRightIcon(infoOne), false);
        }
        title.setText(infoOne.name);
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickRunnable = new Runnable() {
                    @Override
                    public void run() {

                        if (type == RECOMMAND_TYPE_TWO) {
                            SDKWrapper.addEvent(mActivity, SDKWrapper.P1,
                                    "batterypage",
                                    "inter_" + infoOne.name);
                        } else if (type == RECOMMAND_TYPE_THREE) {
                            SDKWrapper.addEvent(mActivity, SDKWrapper.P1,
                                    "batterypage",
                                    "app_" + infoOne.name);
                        }

                        startBrowser(urlFour);
                    }
                };
                handleRunnable();
            }
        });
    }

    private void gotoApp(final BatteryAppItem infoOne, CircleImageViewTwo icon, TextView title, View contentView) {
        if (mActivity == null) return;
        String name = null;
        Drawable map = null;

        for (int i = 0; i < mPackages.size(); i++) {
            PackageInfo packageInfo = mPackages.get(i);
            if (packageInfo.applicationInfo.packageName.equals(infoOne.pkg)) {
                name = packageInfo.applicationInfo.loadLabel(mActivity.getPackageManager()).toString();
                map = packageInfo.applicationInfo.loadIcon(mActivity.getPackageManager());
                break;
            }
        }

        LeoLog.d("testGetList", "infoOne.pkg is : " + infoOne.pkg);
        LeoLog.d("testGetList", "---------------");
        for (int i = 0; i < mPackages.size(); i++) {
            PackageInfo packageInfo = mPackages.get(i);
            if (packageInfo.applicationInfo.packageName.equals(infoOne.pkg)) {
                LeoLog.d("testGetList", "pkg is : " + packageInfo.applicationInfo.packageName);
            }
        }

        if (name != null && map != null) {
            Drawable fitMap = getRightAppIcon(infoOne.pkg);
            if (fitMap == null) {
                icon.setSelfImageDrawable(map, true);
            } else {
                icon.setSelfImageDrawable(fitMap, false);
            }

            title.setText(name);
            contentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    SDKWrapper.addEvent(mActivity, SDKWrapper.P1,
                            "batterypage",
                            "app_" + infoOne.pkg);

                    Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
                    resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    resolveIntent.setPackage(infoOne.pkg);
                    List<ResolveInfo> resolveinfoList = mActivity.getPackageManager()
                            .queryIntentActivities(resolveIntent, 0);
                    ResolveInfo resolveinfo = resolveinfoList.iterator().next();
                    if (resolveinfo != null) {
                        String className = resolveinfo.activityInfo.name;
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ComponentName cn = new ComponentName(infoOne.pkg, className);
                        intent.setComponent(cn);
                        try {
                            startActivity(intent);
                            mActivity.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    private Drawable getRightAppIcon(String pkg) {
        Context ctx = AppMasterApplication.getInstance();
        Drawable fitMap;
        if (pkg.equals(AIQIYI)) {
            fitMap = ctx.getResources().getDrawable(R.drawable.icon_time_aiqiyi);
        } else if (pkg.equals(BAIDUSHIPIN)) {
            fitMap = ctx.getResources().getDrawable(R.drawable.icon_time_baidu);
        } else if (pkg.equals(SOUHU)) {
            fitMap = ctx.getResources().getDrawable(R.drawable.icon_time_souhu);
        } else if (pkg.equals(TENGXUNSHIPIN)) {
            fitMap = ctx.getResources().getDrawable(R.drawable.icon_time_tengxun);
        } else if (pkg.equals(YOUKU)) {
            fitMap = ctx.getResources().getDrawable(R.drawable.icon_time_youku);
        } else {
            fitMap = null;
        }
        return fitMap;
    }

    private boolean isExist(String pkg) {
        if (!Utilities.isEmpty(pkg) && checkPackage(pkg)) {
            return true;
        } else {
            return false;
        }
    }


    private boolean getRightInfo(List<BatteryAppItem> playList, int i) {
        BatteryAppItem infoOne = playList.get(i);
        String packNameOne = infoOne.pkg;
        String urlOne = infoOne.actionUrl;
        if (!Utilities.isEmpty(packNameOne) && checkPackage(packNameOne)) {
            return true;
        } else if (!Utilities.isEmpty(urlOne)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkPackage(String packageName) {
        if (packageName == null || "".equals(packageName))
            return false;
        try {
            PackageManager mPackageManager = mActivity.getPackageManager();
            mPackageManager.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    private Drawable getRightIcon(BatteryAppItem info) {
        Drawable map;
        Context ctx = AppMasterApplication.getInstance();
        if (info.name.equals(GOOGLE)) {
            map = ctx.getResources().getDrawable(R.drawable.icon_time_google);
        } else if (info.name.equals(AMAZON)) {
            map = ctx.getResources().getDrawable(R.drawable.icon_time_amazon);
        } else if (info.name.equals(YAHOO)) {
            map = ctx.getResources().getDrawable(R.drawable.icon_time_yahoo);
        } else if (info.name.equals(FACEBOOK)) {
            map = ctx.getResources().getDrawable(R.drawable.icon_time_facebook);
        } else if (info.name.equals(YOUTUBE)) {
            map = ctx.getResources().getDrawable(R.drawable.icon_time_youtube);
        } else if (info.name.equals(TWITTER)) {
            map = ctx.getResources().getDrawable(R.drawable.icon_time_twitter);
        } else if (info.name.equals(HULU)) {
            map = ctx.getResources().getDrawable(R.drawable.icon_time_hulu);
        } else if (info.name.equals(TED)) {
            map = ctx.getResources().getDrawable(R.drawable.icon_time_ted);
        } else if (info.name.equals(VIMEO)) {
            map = ctx.getResources().getDrawable(R.drawable.icon_time_vimeo);
        } else {
            map = ctx.getResources().getDrawable(R.drawable.default_user_avatar);
        }
        return map;
    }

    private String getBrowserApp(Context context) {
        String default_browser = "android.intent.category.DEFAULT";

        String browsable = "android.intent.category.BROWSABLE";
        String view = "android.intent.action.VIEW";

        Intent intent = new Intent(view);
        intent.addCategory(default_browser);
        intent.addCategory(browsable);
        Uri uri = Uri.parse("http://");
        intent.setDataAndType(uri, null);

        String pack = null;
        try {
            // 找出手机当前安装的所有浏览器程序
            List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentActivities(
                    intent, PackageManager.GET_INTENT_FILTERS);

            LeoLog.i("getBrowserInfo", "能跳转的浏览器个数： " + resolveInfoList.size());
            for (int i = 0; i < resolveInfoList.size(); i++) {
                LeoLog.i("getBrowserInfo", resolveInfoList.get(i).activityInfo.packageName);
            }

            if (resolveInfoList.size() > 0) {
                pack = resolveInfoList.get(0).activityInfo.packageName;
            }

        } catch (Exception e) {
            return null;
        }
        return pack;
    }

    private void shrinkRecommandContent(final boolean isShowAdAnima) {
        if (mActivity == null) return;
        turnLight();
        isClickable = false;

        if (isShowMask && mMaskView != null) {
            mMaskView.showMask();
        }
        mRecommandView.clearAnimation();
        mRecommandContentView.setVisibility(View.INVISIBLE);
        Animation shrink = AnimationUtils.
                loadAnimation(mActivity, R.anim.file_floder_shrink_anim);

        shrink.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (mBossView != null) {
                    boolean isSlideContentShow = mBossView.getVisibility() == View.VISIBLE;
                    if (isSlideContentShow && isShowAdAnima) {
                        adShowAnima();
                    }
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mRecommandView.setVisibility(View.INVISIBLE);
                isClickable = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mRecommandView.setAnimation(shrink);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        releaseAd();
        if (mActivity != null) {
            mActivity.unregisterReceiver(mPresentReceiver);
        }
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
        mIsAdLayout = false;
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

    public void initCreate(String type, BatteryManager.BatteryState state, int remainTime, int[] remainTimeArr) {
        mChangeType = type;
        newState = state;
        mRemainTime = remainTime;
        mRemainTimeArr = remainTimeArr;
        LeoLog.d("testBatteryEvent", "initCreate : " + mRemainTimeArr[0] + " ; " + mRemainTimeArr[1] + " ; " + mRemainTimeArr[2]);
    }

    public void process(String type, BatteryManager.BatteryState state, int remainTime, int[] remainTimeArr) {
        mChangeType = type;
        newState = state;
        mRemainTime = remainTime;
        mRemainTimeArr = remainTimeArr;
        LeoLog.d("testBatteryEvent", "process : " + mRemainTimeArr[0] + " ; " + mRemainTimeArr[1] + " ; " + mRemainTimeArr[2]);
        notifyUI(mChangeType);
    }

    private BroadcastReceiver mPresentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LeoLog.d("stone_test_browser", "action=" + action);
            if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                if (mClickRunnable != null && mActivity != null) {
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
        if (mActivity == null) return;
        setBatteryPercent();
        setBottleWater();
        setTime(mRemainTime, isExpand);
        setThreeRemainTime(mRemainTimeArr);

        if (BatteryManager.SHOW_TYPE_OUT.equals(type)) {
            if (!isExpand) {


                boolean isShowContentShow;
                if (mRecommandView == null) {
                    isShowContentShow = true;
                } else {
                    isShowContentShow = mRecommandView.getVisibility() == View.VISIBLE;
                }

                if (!isShowContentShow) {
                    expandRecommandContent(RECOMMAND_TYPE_TWO, false);
                    mCurrentClickType = RECOMMAND_TYPE_TWO;
                }
            } else {
                expandContent(false);

                boolean isShowContentShow;
                if (mRecommandView == null) {
                    isShowContentShow = true;
                } else {
                    isShowContentShow = mRecommandView.getVisibility() == View.VISIBLE;
                }

                if (!isShowContentShow) {
                    expandRecommandContent(RECOMMAND_TYPE_TWO, false);
                    mCurrentClickType = RECOMMAND_TYPE_TWO;
                }
            }
        }

        if (newState != null && mIvArrowMove != null && mBottleWater != null) {
            if (newState.plugged == 0) {
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

    private void setThreeRemainTime(int[] timeArr) {
        if (timeArr == null || timeArr.length < 3) {
            return;
        }
        int phoneTime = timeArr[0];
        int netTime = timeArr[1];
        int playTime = timeArr[2];

        List<String> phoneTimeArr = getTimes(phoneTime);
        String phoneHourStr = phoneTimeArr.get(0);
        String phoneMinStr = phoneTimeArr.get(1);
        if (phoneHourStr.equals("0")) {
            mPhoneHour.setVisibility(View.GONE);
            mPhoneHourText.setVisibility(View.GONE);
            mPhoneMin.setVisibility(View.VISIBLE);
            mPhoneMinText.setVisibility(View.VISIBLE);

            if (phoneMinStr.equals("0")) {
                mPhoneMin.setText("1");//if min == 0 , then show 1min
            } else {
                mPhoneMin.setText(phoneMinStr);
            }
        } else {
            if (phoneMinStr.equals("0")) {
                mPhoneHour.setVisibility(View.VISIBLE);
                mPhoneHourText.setVisibility(View.VISIBLE);
                mPhoneMin.setVisibility(View.GONE);
                mPhoneMinText.setVisibility(View.GONE);

                mPhoneHour.setText(phoneHourStr);
            } else {
                mPhoneHour.setVisibility(View.VISIBLE);
                mPhoneHourText.setVisibility(View.VISIBLE);
                mPhoneMin.setVisibility(View.VISIBLE);
                mPhoneMinText.setVisibility(View.VISIBLE);

                mPhoneHour.setText(phoneHourStr);
                mPhoneMin.setText(phoneMinStr);
            }
        }

        List<String> netTimeArr = getTimes(netTime);
        String netHourStr = netTimeArr.get(0);
        String netMinStr = netTimeArr.get(1);
        if (netHourStr.equals("0")) {
            mNetHour.setVisibility(View.GONE);
            mNetHourText.setVisibility(View.GONE);
            mNetMin.setVisibility(View.VISIBLE);
            mNetMinText.setVisibility(View.VISIBLE);

            if (netMinStr.equals("0")) {
                mNetMin.setText("1");//if min == 0 , then show 1min
            } else {
                mNetMin.setText(netMinStr);
            }

        } else {
            if (netMinStr.equals("0")) {
                mNetHour.setVisibility(View.VISIBLE);
                mNetHourText.setVisibility(View.VISIBLE);
                mNetMin.setVisibility(View.GONE);
                mNetMinText.setVisibility(View.GONE);
                mNetHour.setText(netHourStr);
            } else {
                mNetHour.setVisibility(View.VISIBLE);
                mNetHourText.setVisibility(View.VISIBLE);
                mNetMin.setVisibility(View.VISIBLE);
                mNetMinText.setVisibility(View.VISIBLE);
                mNetHour.setText(netHourStr);
                mNetMin.setText(netMinStr);
            }
        }

        List<String> playTimeArr = getTimes(playTime);
        String playHourStr = playTimeArr.get(0);
        String playMinStr = playTimeArr.get(1);
        if (playHourStr.equals("0")) {
            mPlayHour.setVisibility(View.GONE);
            mPlayHourText.setVisibility(View.GONE);
            mPlayMin.setVisibility(View.VISIBLE);
            mPlayMinText.setVisibility(View.VISIBLE);

            if (playMinStr.equals("0")) {
                mPlayMin.setText("1");//if min == 0 , then show 1min
            } else {
                mPlayMin.setText(playMinStr);
            }
        } else {
            if (playMinStr.equals("0")) {
                mPlayHour.setVisibility(View.VISIBLE);
                mPlayHourText.setVisibility(View.VISIBLE);
                mPlayMin.setVisibility(View.GONE);
                mPlayMinText.setVisibility(View.GONE);
                mPlayHour.setText(playHourStr);
            } else {
                mPlayHour.setVisibility(View.VISIBLE);
                mPlayHourText.setVisibility(View.VISIBLE);
                mPlayMin.setVisibility(View.VISIBLE);
                mPlayMinText.setVisibility(View.VISIBLE);
                mPlayHour.setText(playHourStr);
                mPlayMin.setText(playMinStr);
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
            if (level >= 30) {
                mIvLight.setVisibility(View.VISIBLE);
            } else {
                mIvLight.setVisibility(View.INVISIBLE);
            }
        }
    }


    private void setBatteryPercent() {
        if (mTvLevel != null && newState != null) {
            mTvLevel.setText(newState.level + "%");

            if (newState.level < 70) {
                mBottleWater.setPostInvalidateDelayMs(66);
            } else if (newState.level < 85) {
                mBottleWater.setPostInvalidateDelayMs(73);
            } else {
                mBottleWater.setPostInvalidateDelayMs(80);
            }
        }
    }

    private List<String> getTimes(int second) {
        List<String> strings = new ArrayList<String>();
        int h = 0;
        int d = 0;
        int temp = second % 3600;
        if (second > 3600) {
            h = second / 3600;
            if (temp != 0) {
                if (temp > 60) {
                    d = temp / 60;
                }
            }
        } else {
            d = second / 60;
        }

        String hString, dString;
        hString = String.valueOf(h);
        dString = String.valueOf(d);

        strings.add(hString);
        strings.add(dString);
        return strings;
    }

    public void setTime(int second, boolean isExpandContent) {
        if (mActivity == null || newState == null) {
            return;
        }

        List<String> timeStr = getTimes(second);
        String hString, dString;
        hString = timeStr.get(0);
        dString = timeStr.get(1);
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
                        String text2 = mActivity.getString(R.string.battery_saver_remain_charge_time);
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
                    String text2 = mActivity.getString(R.string.battery_saver_remain_charge_time);
                    mTvHideText.setVisibility(View.VISIBLE);
                    mTvHideTime.setVisibility(View.VISIBLE);
                    mTvHideText.setText(text2);
                    mTvHideTime.setText(Html.fromHtml(text));
                }
            } else {
//                String text2 = mActivity.getString(R.string.battery_saver_remain_char_power);
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
        if (mActivity == null || mSlideView == null) {
            return;
        }
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
                mScrollView.setScrollEnabled(true);
                if (mMaskView != null) {
                    mMaskView.setY(mMaskView.getTop() - DipPixelUtil.dip2px(mActivity, 8));
                }
            }
        });
        animMoveY.start();

    }

    private void showMoveDown() {
        if (mActivity == null || mSlideView == null) {
            return;
        }
        ObjectAnimator animMoveY = ObjectAnimator.ofFloat(mSlideView,
                "y", mSlideView.getTop(), mSlideView.getTop() + mMoveDisdance);
        animMoveY.setDuration(ANIMATION_TIME);
        animMoveY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (mMaskView != null) {
                    mMaskView.setY(mMaskView.getTop() + DipPixelUtil.dip2px(mActivity, 8));
                }
                mScrollView.setScrollEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isExpand = false;
                mShowing = false;
                mScrollView.setScrollY(0);
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
                dlIntent.putExtra(BatteryManager.ARR_REMAIN_TIME, mRemainTimeArr);
                Bundle bundle = new Bundle();
                bundle.putSerializable(BatteryManager.SEND_BUNDLE, newState);
                dlIntent.putExtras(bundle);
                mActivity.startActivity(dlIntent);
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "batterypage", "screen_setting");
                mActivity.finish();
                break;
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
            case R.id.slider:
                BatteryShowViewActivity activity = (BatteryShowViewActivity) mActivity;
                boolean isClickCancel = PreferenceTable.getInstance().getBoolean(Constants.CANCEL_BAY_FIRST_TIME, false);
                if (!isClickCancel && newState.plugged != 0) {
                    activity.onFinishActivity(true, newState.level);
                    PreferenceTable.getInstance().putBoolean(Constants.CANCEL_BAY_FIRST_TIME, true);
                } else {
                    activity.onFinishActivity(false, 0);
                }

                break;
            case R.id.remain_one:
                if (isClickable) {
                    SDKWrapper.addEvent(mActivity, SDKWrapper.P1,
                            "batterypage",
                            "screen_call");
                    showRecommandContent(RECOMMAND_TYPE_ONE);
                }
                break;
            case R.id.remain_two:
                if (isClickable) {
                    SDKWrapper.addEvent(mActivity, SDKWrapper.P1,
                            "batterypage",
                            "screen_inter");
                    showRecommandContent(RECOMMAND_TYPE_TWO);
                }
                break;
            case R.id.remain_three:
                if (isClickable) {
                    SDKWrapper.addEvent(mActivity, SDKWrapper.P1,
                            "batterypage",
                            "screen_app");
                    showRecommandContent(RECOMMAND_TYPE_THREE);
                }
                break;
            case R.id.background_pic:
                if (mRecommandView.getVisibility() == View.VISIBLE && isClickable) {
                    mCurrentClickType = 0;
                    shrinkRecommandContent(true);
                }
                break;
        }
    }

    private void turnLight() {
        if (getActivity() == null || isDetached() || isRemoving()) {  // 防止崩溃
            return;
        }

        boolean isOneNormal = (Boolean) mShowOne.getTag();
        boolean isTwoNormal = (Boolean) mShowTwo.getTag();
        boolean isThreeNormal = (Boolean) mShowThree.getTag();

        if (!isOneNormal) {
            turnNormal(mShowOne);
        }
        if (!isTwoNormal) {
            turnNormal(mShowTwo);
        }
        if (!isThreeNormal) {
            turnNormal(mShowThree);
        }

    }


    private void turnDark(int recommandType) {
        boolean isOneNormal = (Boolean) mShowOne.getTag();
        boolean isTwoNormal = (Boolean) mShowTwo.getTag();
        boolean isThreeNormal = (Boolean) mShowThree.getTag();

        if (recommandType == RECOMMAND_TYPE_ONE) {
            if (isOneNormal) {
                turnBig(mShowOne);
            }
            if (!isTwoNormal) {
                turnNormal(mShowTwo);
            }
            if (!isThreeNormal) {
                turnNormal(mShowThree);
            }
        } else if (recommandType == RECOMMAND_TYPE_TWO) {
            if (!isOneNormal) {
                turnNormal(mShowOne);
            }
            if (isTwoNormal) {
                turnBig(mShowTwo);
            }
            if (!isThreeNormal) {
                turnNormal(mShowThree);
            }
        } else {
            if (!isOneNormal) {
                turnNormal(mShowOne);
            }
            if (!isTwoNormal) {
                turnNormal(mShowTwo);
            }
            if (isThreeNormal) {
                turnBig(mShowThree);
            }
        }

    }

    private void turnBig(View View) {
        View.setTag(false);
        ObjectAnimator anim20 = ObjectAnimator.ofFloat(View,
                "scaleX", 0.9f, 1.1f);
        ObjectAnimator anim21 = ObjectAnimator.ofFloat(View,
                "scaleY", 0.9f, 1.1f);

        AnimatorSet set = new AnimatorSet();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }
        });
        set.setDuration(200);
        set.play(anim20).with(anim21);
        set.start();
    }

    private void makeSmall(View View) {
        ObjectAnimator anim20 = ObjectAnimator.ofFloat(View,
                "scaleX", 1.0f, 0.9f);
        ObjectAnimator anim21 = ObjectAnimator.ofFloat(View,
                "scaleY", 1.0f, 0.9f);

        AnimatorSet set = new AnimatorSet();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }
        });
        set.setDuration(10);
        set.play(anim20).with(anim21);
        set.start();
    }

    private void turnNormal(View View) {
        View.setTag(true);
        ObjectAnimator anim20 = ObjectAnimator.ofFloat(View,
                "scaleX", 1.1f, 0.9f);
        ObjectAnimator anim21 = ObjectAnimator.ofFloat(View,
                "scaleY", 1.1f, 0.9f);

        AnimatorSet set = new AnimatorSet();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }
        });
        set.setDuration(200);
        set.play(anim20).with(anim21);
        set.start();
    }

    private void showRecommandContent(int recommandTypeThree) {
        if (mCurrentClickType == recommandTypeThree) {
            mCurrentClickType = 0;
            shrinkRecommandContent(true);
        } else {
            if (mCurrentClickType == 0) {
                expandRecommandContent(recommandTypeThree, false);
            } else if (mCurrentClickType == -1) {
                if (recommandTypeThree == RECOMMAND_TYPE_TWO) {
                    mCurrentClickType = 0;
                    shrinkRecommandContent(true);
                    return;
                } else {
                    turnDark(recommandTypeThree);
                    fillShowContentData(recommandTypeThree);
                    showIndicatorAnim(recommandTypeThree);
                }
            } else {
                turnDark(recommandTypeThree);
                fillShowContentData(recommandTypeThree);
                showIndicatorAnim(recommandTypeThree);
            }
            mCurrentClickType = recommandTypeThree;
        }
    }


    private void handleRunnable() {
        if (mClickRunnable == null || mActivity == null) {
            return;
        }

        if (AppUtil.isScreenLocked(mActivity)) {
            // 默认浏览器是chrome而且系统锁住了，让user_present receiver处理
//            if (samsungLolipopDevice()) {
            mLockManager.filterPackage(mActivity.getPackageName(), 2000);
            LeoLog.d(TAG, "Samsung 5.x device, launch specific activity");
            Intent intent = new Intent(mActivity, EmptyForJumpActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mActivity.startActivity(intent);
//            } else {
//                Intent intent = new Intent(mActivity, DeskProxyActivity.class);
//                intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, DeskProxyActivity.IDX_BATTERY_PROTECT);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                mActivity.startActivity(intent);
//            }

        } else {
            // 没有锁或者默认浏览器不是chrome，直接跑
            mClickRunnable.run();
            mClickRunnable = null;
        }
    }

    /* 广告相关 - 开始 */
    private boolean mShouldLoadAd = false;

    private void loadAd() {
        LeoLog.d(TAG, "loadAd called");
        AppMasterPreference amp = AppMasterPreference.getInstance(AppMasterApplication.getInstance());
        mShouldLoadAd = amp.getADOnScreenSaver() == 1;
        if (mShouldLoadAd) {
            mAdSource = amp.getChargingAdConfig();
            ADEngineWrapper.getInstance(AppMasterApplication.getInstance()).loadAd(mAdSource, Constants.UNIT_ID_CHARGING, new ADEngineWrapper.WrappedAdListener() {
                @Override
                public void onWrappedAdLoadFinished(int code, List<WrappedCampaign> campaign, String msg) {
                    if (code == MobvistaEngine.ERR_OK) {
                        LeoLog.d(TAG, "Ad data ready ad title: " + campaign.get(0).getAppName());
                        sAdImageListener = new AdPreviewLoaderListener(BatteryViewFragment.this, campaign.get(0));
                        ImageLoader.getInstance().loadImage(campaign.get(0).getImageUrl(), sAdImageListener);
                    }
                }

                @Override
                public void onWrappedAdClick(WrappedCampaign campaign, String unitID) {
                    SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "ad_cli", "adv_cnts_screen");
                    LeoLog.d(TAG, "Ad clicked");
                }
            });
            /*MobvistaEngine.getInstance(mActivity).loadMobvista(Constants.UNIT_ID_CHARGING,
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
                        public void onMobvistaClick(Campaign campaign, String unitID) {
                            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "ad_cli", "adv_cnts_screen");
                            LeoLog.d(TAG, "Ad clicked");
                        }
                    });*/
        }
    }

    private void releaseAd() {
        if (mShouldLoadAd) {
            LeoLog.d(TAG, "release ad");
            ADEngineWrapper.getInstance(mActivity).releaseAd(mAdSource, Constants.UNIT_ID_CHARGING, mAdView);
        }
    }

    public static class AdPreviewLoaderListener implements ImageLoadingListener {
        WeakReference<BatteryViewFragment> mFragment;
        WrappedCampaign mCampaign;

        public AdPreviewLoaderListener(BatteryViewFragment fragment, final WrappedCampaign campaign) {
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

    private void initAdLayout(View rootView, WrappedCampaign campaign, Bitmap previewImage) {
        if (previewImage == null || previewImage.isRecycled()) {
            return;
        }

        View adView = rootView.findViewById(R.id.ad_content);
        mAdWrapper = (AdWrapperLayout) rootView.findViewById(R.id.ad_wrapper);
        mAdWrapper.setNeedIntercept(true);

        final Button ignoreBtn = (Button) rootView.findViewById(R.id.ignore_button);
        boolean isShowIgnoreBtn = PrefTableHelper.showIgnoreBtn();
        if (isShowIgnoreBtn) {
            ignoreBtn.setVisibility(View.VISIBLE);
            ignoreBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LeoLog.d("stone_test_ignore", "ignore!");
                    showPopUp(ignoreBtn);
                }
            });
        } else {
            ignoreBtn.setVisibility(View.INVISIBLE);
        }

        TextView tvTitle = (TextView) adView.findViewById(R.id.item_title);
        tvTitle.setText(campaign.getAppName());
        TextView tvDesc = (TextView) adView.findViewById(R.id.item_description);
        tvDesc.setText(campaign.getDescription());
        Button btnCTA = (Button) adView.findViewById(R.id.ad_result_cta);
        btnCTA.setText(campaign.getAdCall());
        ResizableImageView preview = (ResizableImageView) adView.findViewById(R.id.item_ad_preview);
        preview.setImageBitmap(previewImage);
        ImageView iconView = (ImageView) adView.findViewById(R.id.ad_icon);
        ImageLoader.getInstance().displayImage(campaign.getIconUrl(), iconView);
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "ad_act", "adv_shws_screen");

        View saverAdView = rootView.findViewById(R.id.screen_saver_id);
        saverAdView.setVisibility(View.VISIBLE);

        adView.setVisibility(View.VISIBLE);

        int delayTime;
        if (System.currentTimeMillis() - mInitTime > AD_LOAD_TIME) {
            delayTime = 300;
        } else {
            delayTime = DELAY_SHOW_AD;
        }

        LeoLog.d("testDelayTime", "System.currentTimeMillis() - mInitTime : " + (System.currentTimeMillis() - mInitTime));
        LeoLog.d("testDelayTime", "delayTime : " + delayTime);

        mIsAdLayout = true;

        mAdWrapper.postDelayed(new Runnable() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                msg.what = LOAD_DONE_INIT_PLACE;
                msg.obj = AD_TYPE_MSG;
                mHandler.sendMessage(msg);
            }
        }, delayTime);

        mAdView = adView;

        // make the count correct
        //MobvistaEngine.getInstance(mActivity).registerView(Constants.UNIT_ID_CHARGING, mAdView);
        ADEngineWrapper.getInstance(mActivity).registerView(mAdSource, mAdView, Constants.UNIT_ID_CHARGING);
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

            int delayTime;
            if (System.currentTimeMillis() - mInitTime > AD_LOAD_TIME) {
                delayTime = 300;
            } else {
                delayTime = DELAY_SHOW_AD;
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
            }, delayTime);
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

            int delayTime;
            if (System.currentTimeMillis() - mInitTime > AD_LOAD_TIME) {
                delayTime = 300;
            } else {
                delayTime = DELAY_SHOW_AD;
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
            }, delayTime);
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

    private PopupWindow popupWindow;

    private void showPopUp(View v) {
        if (mActivity == null) return;
        View contentView = LayoutInflater.from(mActivity).inflate(
                R.layout.popmenu_battery_list_item, null);

        popupWindow = new PopupWindow(contentView, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);//TODO 原本是用height，为啥？
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isExpand) {
                    theRestPartShow();
                }
                if (mBossView != null) {
                    mBossView.setVisibility(View.INVISIBLE);
                }
                popupWindow.dismiss();

                showRecommandContent(RECOMMAND_TYPE_TWO);
                PreferenceTable.getInstance().putLong(Constants.AD_CLICK_IGNORE, System.currentTimeMillis());
            }
        });


        int[] location = new int[2];
        v.getLocationOnScreen(location);
        int x = location[0] - v.getWidth() * 2;
        int y = location[1] + v.getHeight();
        popupWindow.showAtLocation(v, Gravity.NO_GRAVITY, x, y);

        //        TextView text = (TextView) contentView.findViewById(R.id.menu_text);
//        float textWidth = getTextViewLength(text, str);
//        LeoLog.d("testPop", "width : " + textWidth);
//
//        text.setText(str);
//        int size = (int) getResources().getDimension(R.dimen.popu_txt_size);
//        text.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
//        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(AppMasterApplication.sScreenWidth, View.MeasureSpec.AT_MOST);
//        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//        text.measure(widthMeasureSpec, heightMeasureSpec);


//        int height = DipPixelUtil.dip2px(mActivity, 50);
//        if (textWidth > CHANGE_LINE_INT) {
//            x = location[0];
//            height = DipPixelUtil.dip2px(mActivity, 70);
//        } else if (textWidth > MID_WIDTH) {
//            x = location[0] - 80;
//        } else {
//            x = location[0] - 100;
//        }

    }

//    public float getTextViewLength(TextView textView, String text) {
//        TextPaint paint = textView.getPaint();
//        // 得到使用该paint写上text的时候,像素为多少
//        float textLength = paint.measureText(text);
//        return textLength;
//    }

}
