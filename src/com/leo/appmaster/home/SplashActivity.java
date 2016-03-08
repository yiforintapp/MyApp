
package com.leo.appmaster.home;

import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.EdgeEffectCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.LockSettingActivity;
import com.leo.appmaster.bootstrap.SplashBootstrap;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.AppUnlockEvent;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.sdk.push.ui.WebViewActivity;
import com.leo.appmaster.ui.CirclePageIndicator;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.FileOperationUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NinePatchChunk;
import com.leo.appmaster.utils.Utilities;

public class SplashActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = "SplashActivity";
    /* 是否走测试模式：true--为测试模式，false--为正常模式 */
    private static final boolean DBG = false;

    public static final int MSG_LAUNCH_HOME_ACTIVITY = 1000;
    public static final String SPLASH_TO_WEBVIEW = "splash_to_webview";

    private Handler mEventHandler;
    /* Guide page stuff begin */
    private ViewPager mNewFuncViewPager;
    /* pages */
    private ArrayList<View> mNewFuncPageViews;
    private GuideItemView mNewPageBackgroundView;
    /* footer indicators */
    private CirclePageIndicator mIndicator;
    private ViewGroup mNewGuideMain;
    /* color for each page */
    private int[] mPageColors = new int[7];
    private EdgeEffectCompat leftEdge;
    private EdgeEffectCompat rightEdge;
    private RelativeLayout mSplashRL;
    private ImageView mSplashIcon;
    private ImageView mSplashName;
    private boolean mIsEmptyForSplashUrl;
    private ImageView mSkipToPgButton;
    private boolean mShowSplashFlag;
    private TextView mSkipText;
    private RelativeLayout mSplaFacRt;

    /* 是否显示更多引导 */
    private boolean mIsShowGuide;
    /*是否从闪屏跳出到facebook，标志*/
    private boolean mIsToFacebk;

    /* Guide page stuff end */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LeoLog.d("SplashActivity", "onCreate");
        setContentView(R.layout.activity_splash_guide);
        initSplash();
        mEventHandler = new EventHandler();
        LeoEventBus.getDefaultBus().register(this, 2);
        AppMasterApplication.sIsSplashActioned = false;
    }

    @SuppressLint("NewApi")
    private void initSplash() {
        AppMasterPreference pre = AppMasterPreference.getInstance(this);
        mSplashRL = (RelativeLayout) findViewById(R.id.splashRL);
        mSplashIcon = (ImageView) findViewById(R.id.image_view_splash_center);
        mSplashName = (ImageView) findViewById(R.id.iv_back);
        mSkipToPgButton = (ImageView) findViewById(R.id.skip_to_pg_bt);
        mIsEmptyForSplashUrl = checkSplashUrlIsEmpty();
        mSplaFacRt = (RelativeLayout) findViewById(R.id.spl_fc_RT);
        long startShowSplashTime = pre.getSplashStartShowTime();
        long endShowSplashTime = pre.getSplashEndShowTime();
        long currentTime = System.currentTimeMillis();
        if (DBG) {
            SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            LeoLog.i(TAG, "当前系统时间：" + dateFormate.format(new Date(currentTime)));
            LeoLog.i(TAG, "闪屏开始时间：" + dateFormate.format(new Date(startShowSplashTime)));
            LeoLog.i(TAG, "闪屏结束时间：" + dateFormate.format(new Date(endShowSplashTime)));
            LeoLog.i(TAG, " 闪屏跳转模式：" + AppMasterPreference.getInstance(getApplicationContext())
                    .getSplashSkipMode());
        }
        /**
         * 可能存在的几种情况：<br>
         * 1.只有开始时间<br>
         * 2.只有结束时间<br>
         * 3.没有配置时间<br>
         * 4.开始.结束时间都有<br>
         */
        if (startShowSplashTime > 0 || endShowSplashTime > 0) {
            /* 只有开始时间 */
            if (endShowSplashTime <= 0 && startShowSplashTime > 0) {
                if (currentTime >= startShowSplashTime) {
                    showSplash();
                }
            }
            /* 只有结束时间 */
            if (startShowSplashTime <= 0 && endShowSplashTime > 0) {
                if (currentTime < endShowSplashTime) {
                    showSplash();
                }
            }
            /* 开始，结束时间都存在 */
            if (startShowSplashTime > 0 && endShowSplashTime > 0) {
                if (currentTime >= startShowSplashTime && currentTime < endShowSplashTime) {
                    showSplash();
                }
            }
            if (!mShowSplashFlag) {
                if (!DBG) {
                    showDefaultSplash();
                } else {
                    showSplash();
                }
            }
        } else {
            if (!DBG) {
                showDefaultSplash();
            } else {
                showSplash();
            }
        }
        PrivacyHelper.getInstance(this).setDirty(true);
    }

    private void showDefaultSplash() {
        /* 没有开始，没有结束时间，默认 */
        if (DBG) {
            LeoLog.i(TAG, "splash_end&start_time：No time!");
            LeoLog.i(TAG, "使用默认闪屏!");
        }

        if (mSplashIcon.getVisibility() == View.INVISIBLE) {
            mSplashIcon.setVisibility(View.VISIBLE);
        }
        if (mSplashName.getVisibility() == View.INVISIBLE) {
            mSplashName.setVisibility(View.VISIBLE);
        }
        if (mSkipToPgButton.getVisibility() == View.VISIBLE) {
            mSkipToPgButton.setVisibility(View.GONE);
        }
        if (mSplaFacRt.getVisibility() == View.VISIBLE) {
            mSplaFacRt.setVisibility(View.GONE);
        }
    }

    /* 如果url存在则设置点击跳转 */
    private void showSkipUrlButton() {
        if (DBG) {
            LeoLog.e(TAG, "链接是否为空：" + mIsEmptyForSplashUrl);
        }
        if (!mIsEmptyForSplashUrl) {
            mSplashRL.setOnClickListener(new SkipUrlOnClickListener());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.spl_fc_RT:
                String splashDir = FileOperationUtil.getSplashPath();
                if (Utilities.isEmpty(splashDir)) {
                    return;
                }
                StringBuilder sb = new StringBuilder(splashDir);
//                sb.append(Constants.SPL_SHARE_QR_NAME);
                sb.append(Constants.SPLASH_NAME);
                /*facebook分享闪屏*/
                mLockManager.filterPackage(Constants.PKG_FACEBOOK, 1000);

                Intent shareIntent = AppUtil.shareImageToApp(sb.toString());
                shareIntent.setPackage(Constants.FACEBOOK_PKG_NAME);
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    if (mEventHandler != null) {
                        mEventHandler.removeMessages(MSG_LAUNCH_HOME_ACTIVITY);
                    }
                    startActivity(shareIntent);
                    mIsToFacebk = true;
                    mSplaFacRt.setOnClickListener(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                SDKWrapper.addEvent(this, SDKWrapper.P1, "screen_cli", "share_cnts");
                break;
            default:
                break;
        }

    }

    /* 闪屏链接跳转按钮单击监听 */
    private class SkipUrlOnClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            int viewId = v.getId();
            switch (viewId) {
                case R.id.splashRL:
                    SDKWrapper.addEvent(SplashActivity.this, SDKWrapper.P1,
                            "screen_cli", "go");
                    skipModeHandle();
                    break;
                case R.id.skip_to_pg_bt:
                    SDKWrapper.addEvent(SplashActivity.this, SDKWrapper.P1,
                            "screen_cli", "skip");
                    startHome();
                    if (mEventHandler != null) {
                        mEventHandler.removeMessages(MSG_LAUNCH_HOME_ACTIVITY);
                    }
                    break;
                default:
                    break;
            }
        }

    }

    private void showSplash() {
        String path = FileOperationUtil.getSplashPath();
        Bitmap splash = null;
        if (path != null && !"".equals(path)) {
            BitmapFactory.Options option = new BitmapFactory.Options();
            option.inJustDecodeBounds = true;
            option.inScaled = true;
            BitmapFactory.decodeFile(path + Constants.SPLASH_NAME, option);
            int[] pix = AppUtil.getScreenPix(this);
            int width = pix[0];
            int height = pix[1];
            if (AppUtil.calculateInSampleSize(option, width, height) >= 2) {
                option.inSampleSize = AppUtil.calculateInSampleSize(option, width, height);
            } else {
                option.inDensity = 480;
                option.inTargetDensity = getResources().getDisplayMetrics().densityDpi;
                // scale for hdpi, mdpi and ldpi
                if (option.inTargetDensity < 125) {
                    option.inTargetDensity = option.inTargetDensity - 40;
                } else if (option.inTargetDensity < 165) {
                    option.inTargetDensity = option.inTargetDensity - 40;
                } else if (option.inTargetDensity < 245) {
                    option.inTargetDensity = option.inTargetDensity - 40;
                }
            }
            option.inJustDecodeBounds = false;
            try {
                splash = BitmapFactory.decodeFile(path + Constants.SPLASH_NAME, option);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        if (splash != null) {
            byte[] chunk = splash.getNinePatchChunk();
            if (chunk != null && NinePatch.isNinePatchChunk(chunk)) {
                if (DBG) {
                    LeoLog.i(TAG, "使用后台配置闪屏");
                }
                mSplashIcon.setVisibility(View.INVISIBLE);
                mSplashName.setVisibility(View.INVISIBLE);
                mSplashRL.setBackgroundDrawable(new NinePatchDrawable(getResources(),
                        splash, chunk, NinePatchChunk.deserialize(chunk).mPaddings, null));
                mShowSplashFlag = true;
                mSkipToPgButton.setVisibility(View.VISIBLE);
                mSkipToPgButton.setOnClickListener(new SkipUrlOnClickListener());
                showSkipUrlButton();
                showFacbkShareButton();
            }
        }
    }

    /*活动闪屏facebook分享处理*/
    private void showFacbkShareButton() {
        boolean isInslFac = AppUtil.isInstallPkgName(this, Constants.FACEBOOK_PKG_NAME);
        String path = FileOperationUtil.getSplashPath();
        StringBuilder sb = new StringBuilder(path);
//        sb.append(Constants.SPL_SHARE_QR_NAME);
        sb.append(Constants.SPLASH_NAME);
        File file = new File(sb.toString());
        boolean isExistShreFile = file.exists();
        if (isInslFac && isExistShreFile) {
            mSplaFacRt.setVisibility(View.VISIBLE);
            mSplaFacRt.setOnClickListener(this);
            LeoLog.i(TAG, "install facebook");
            SDKWrapper.addEvent(this, SDKWrapper.P1, "screen_cli", "share_shw");
        } else {
            LeoLog.i(TAG, "no install facebook");
        }
    }

    /* 反初始化闪屏跳过按钮和Url跳转 */
    private void cancelSplashSkipbtAndUrlbt() {
        mSkipToPgButton.setVisibility(View.INVISIBLE);
        mSkipToPgButton.setOnClickListener(null);
        mSplashRL.setOnClickListener(null);
    }


    public void finishForSkip(boolean finish) {
        if (finish) {
            finish();
        }

        if (mEventHandler != null) {
            mEventHandler.removeMessages(MSG_LAUNCH_HOME_ACTIVITY);
        }
    }

    @Override
    protected void onDestroy() {
        LeoLog.d("SplashActivity", "onDestroy");
        LeoEventBus.getDefaultBus().unregister(this);
        super.onDestroy();
        mEventHandler.removeMessages(MSG_LAUNCH_HOME_ACTIVITY);
        mEventHandler = null;
        mIsToFacebk = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEventHandler.removeMessages(MSG_LAUNCH_HOME_ACTIVITY);
        splashDelayShow();
        mLockManager.clearFilterList();
        long currentTs = SystemClock.elapsedRealtime();
        if (AppMasterApplication.sCheckTs) {
            LeoLog.i("TsCost", "App onCreate ~ Splash onResume: " +
                    (currentTs - AppMasterApplication.sAppOnCrate));
            AppMasterApplication.sCheckTs = false;
        }

//        ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Debug.stopMethodTracing();
//            }
//        }, 1000);
    }

    private void splashDelayShow() {
        if (mIsToFacebk) {
            startHome();
            return;
        }
        if (mShowSplashFlag) {
            mEventHandler.sendEmptyMessageDelayed(MSG_LAUNCH_HOME_ACTIVITY,
                    SplashBootstrap.mSplashDelayTime);
            if (DBG) {
                LeoLog.i(TAG, "配置闪屏时间:" + SplashBootstrap.mSplashDelayTime);
            }
        } else {
            mEventHandler.sendEmptyMessageDelayed(MSG_LAUNCH_HOME_ACTIVITY,
                    Constants.SPLASH_DELAY_TIME);
            if (DBG) {
                LeoLog.i(TAG, "闪屏默认时间:" + Constants.SPLASH_DELAY_TIME);
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void onEvent(AppUnlockEvent event) {
        LeoLog.d(TAG, "onEvent, result: " + event.mUnlockResult);
        if (event.mUnlockResult == AppUnlockEvent.RESULT_UNLOCK_SUCCESSFULLY) {
            if (getPackageName() != null && getPackageName().equals(event.mUnlockedPkg)) {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                AppMasterApplication.getInstance().startActivity(intent);
                AppMasterApplication.sIsSplashActioned = true;
            }
            this.finish();
        } else if (event.mUnlockResult == AppUnlockEvent.RESULT_UNLOCK_CANCELED) {
            this.finish();
        } else if (event.mUnlockResult == AppUnlockEvent.RESULT_UNLOCK_OUTCOUNT) {
        }
    }

    private class EventHandler extends Handler {

        public EventHandler() {
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LAUNCH_HOME_ACTIVITY:
                    if (AppMasterPreference.getInstance(SplashActivity.this).getGuidePageFirstUse()) {
                        boolean guidNotShown = mNewGuideMain == null
                                || mNewGuideMain.getVisibility() != View.VISIBLE;
                        if (guidNotShown) {
                            cancelSplashSkipbtAndUrlbt();
                            if (!mIsShowGuide) {
                                try {
                                    long start = SystemClock.elapsedRealtime();
                                    LeoLog.i("TsCost", "showNewFuncGuide: start");
                                    showNewFuncGuide();
                                    LeoLog.i("TsCost", "showNewFuncGuide: " + (SystemClock.elapsedRealtime() - start));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                /*如果走引导，去除分享按钮*/
                                if (mSplaFacRt.getVisibility() == View.VISIBLE) {
                                    mSplaFacRt.setVisibility(View.GONE);
                                }
                            }
                        }
                    } else {
                        LeoLog.d(TAG, "teststart, handleMessage--else...");
                        startHome();
                    }
                    SDKWrapper.addEvent(SplashActivity.this, SDKWrapper.P1, "screen_cli", "none");
                    break;

                default:
                    break;
            }
        }
    }

    private void startHome() {
        AppMasterPreference amp = AppMasterPreference.getInstance(this);
        if (amp.getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
            if (mLockManager.inRelockTime(getPackageName())) {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                LeoLog.d("Track Lock Screen", "apply lockscreen form SplashActivity");
                amp.setLastFilterSelfTime(0);
                mLockManager.applyLock(LockManager.LOCK_MODE_FULL, getPackageName(), true, null);
                amp.setDoubleCheck(null);
            }
        } else {
            Intent intent = new Intent(this, LockSettingActivity.class);
            intent.putExtra("from_splash", true);
            startActivity(intent);
            finish();
        }
        if (mShowSplashFlag) {
            mShowSplashFlag = false;
        }
    }

    class GuidePageAdapter extends PagerAdapter {
        List<View> pageViews;

        public GuidePageAdapter(List<View> pageViews) {
            this.pageViews = pageViews;
        }

        @Override
        public int getCount() {
            return pageViews.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(pageViews.get(arg1));
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(pageViews.get(arg1));
            return pageViews.get(arg1);
        }
    }

    private int caculateNewColor(int originColor, int targetColor, float position) {
        int originRGB[] = {
                Color.red(originColor), Color.green(originColor), Color.blue(originColor)
        };
        int targetRGB[] = {
                Color.red(targetColor), Color.green(targetColor), Color.blue(targetColor)
        };
        int newRGB[] = new int[3];
        for (int i = 0; i < 3; i++) {
            newRGB[i] = (int) (originRGB[i] + (targetRGB[i] - originRGB[i]) * position);
        }
        return Color.rgb(newRGB[0], newRGB[1], newRGB[2]);
    }

    private void initViewPagerEdges(ViewPager viewPager) {
        try {
            Field leftEdgeField = viewPager.getClass().getDeclaredField("mLeftEdge");
            Field rightEdgeField = viewPager.getClass().getDeclaredField("mRightEdge");
            if (leftEdgeField != null && rightEdgeField != null) {
                leftEdgeField.setAccessible(true);
                rightEdgeField.setAccessible(true);
                leftEdge = (EdgeEffectCompat) leftEdgeField.get(viewPager);
                rightEdge = (EdgeEffectCompat) rightEdgeField.get(viewPager);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* add for Guide Screen end */

    /* 删除目录下的图片 */
    public static boolean deleteImage() {
        boolean flag = false;
        File file = new File(FileOperationUtil.getSplashPath()
                + Constants.SPLASH_NAME);
        if (file.exists()) {
            flag = file.delete();
        }
        return flag;
    }

    /*
     * set click listener of skip btn in guide page
     */
    private void setSkipClickListener() {
        if (mSkipText == null) {
            mSkipText = (TextView) findViewById(R.id.skip_tv);
        }
        mSkipText.setVisibility(View.VISIBLE);
        mSkipText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                enterHome();
            }
        });
    }

    private void unsetSkipClickListener() {
        if (mSkipText != null) {
            mSkipText.setVisibility(View.GONE);
            mSkipText.setOnClickListener(null);
        }
    }

    private void enterHome() {
        AppMasterPreference.getInstance(SplashActivity.this).setGuidePageFirstUse(false);
        mEventHandler.removeMessages(MSG_LAUNCH_HOME_ACTIVITY);
        startHome();
        String currentVersionName = SplashActivity.this
                .getString(R.string.version_name);
        AppMasterPreference.getInstance(SplashActivity.this).setAppVersionName(
                currentVersionName);
    }

    private void showNewFuncGuide() {
        mNewFuncPageViews = new ArrayList<View>();
        LayoutInflater inflater = getLayoutInflater();
        TextView tvTitle, tvContent;
        Button enterAppButton;
        ImageView bigImage = null;
        mPageColors[3] = getResources().getColor(R.color.new_guide_page2_background_color);
        mPageColors[4] = getResources().getColor(R.color.new_guide_page2_background_color);
        mPageColors[5] = getResources().getColor(R.color.new_guide_page4_background_color);
        /*骚扰拦截引导*/
        mPageColors[6] = getResources().getColor(R.color.new_guide_page1_background_color);

        mNewPageBackgroundView = (GuideItemView) findViewById(R.id.new_func_guide_bg_view);
        mNewPageBackgroundView.initBackgroundColor(mPageColors[3]);
        /* 显示跳过按钮 */
        setSkipClickListener();
        /* page1 */
        ViewGroup page1 = (ViewGroup) inflater.inflate(R.layout.guide_page_layout, null);
        bigImage = (ImageView) page1.findViewById(R.id.guide_image);
        bigImage.setImageDrawable(getResources().getDrawable(R.drawable.gd_leo_pic));
        tvTitle = (TextView) page1.findViewById(R.id.guide_tv_title);
        tvTitle.setText(getResources().getString(R.string.gde_renm_title));
        tvContent = (TextView) page1.findViewById(R.id.guide_tv_content);
        tvContent.setText(getResources().getString(R.string.gde_renm_content));
        mNewFuncPageViews.add(page1);

        /* page2 */
        ViewGroup page2 = (ViewGroup) inflater.inflate(R.layout.guide_page_layout, null);
        bigImage = (ImageView) page2.findViewById(R.id.guide_image);
        bigImage.setImageDrawable(getResources().getDrawable(R.drawable.new_page_2));
        tvTitle = (TextView) page2.findViewById(R.id.guide_tv_title);
        tvTitle.setText(getResources().getString(R.string.privacy_scan));
        tvContent = (TextView) page2.findViewById(R.id.guide_tv_content);
        tvContent.setText(getResources().getString(R.string.one_key_confirm_privacy));
        mNewFuncPageViews.add(page2);
        /* page3 */
        ViewGroup page3 = (ViewGroup) inflater.inflate(R.layout.guide_page_layout, null);
        bigImage = (ImageView) page3.findViewById(R.id.guide_image);
        bigImage.setImageDrawable(getResources().getDrawable(R.drawable.new_page_4));
        tvTitle = (TextView) page3.findViewById(R.id.guide_tv_title);
        tvTitle.setText(getResources().getString(R.string.secur_feedbk_type));
        tvContent = (TextView) page3.findViewById(R.id.guide_tv_content);
        tvContent.setText(getResources().getString(R.string.phone_security_tips));
        mNewFuncPageViews.add(page3);
        /*骚扰拦截引导：page4*/
        ViewGroup page4 = (ViewGroup) inflater.inflate(R.layout.guide_page_layout, null);
        bigImage = (ImageView) page4.findViewById(R.id.guide_image);
        bigImage.setImageDrawable(getResources().getDrawable(R.drawable.callfilter_page));
        tvTitle = (TextView) page4.findViewById(R.id.guide_tv_title);
        tvTitle.setText(getResources().getString(R.string.call_filter_name));
        tvContent = (TextView) page4.findViewById(R.id.guide_tv_content);
        tvContent.setText(getResources().getString(R.string.gud_page_call_fil));
        mNewFuncPageViews.add(page4);

        mNewGuideMain = (ViewGroup) findViewById(R.id.layout_new_func_guide);
        mNewFuncViewPager = (ViewPager) mNewGuideMain.findViewById(R.id.new_func_guide_viewpager);
        initViewPagerEdges(mNewFuncViewPager);

        mNewGuideMain.setVisibility(View.VISIBLE);
        AlphaAnimation aa = new AlphaAnimation(0.0f, 1.0f);
        aa.setDuration(1000);
        mNewGuideMain.startAnimation(aa);

        mNewFuncViewPager.setAdapter(new GuidePageAdapter(mNewFuncPageViews));
        mIndicator = (CirclePageIndicator) mNewGuideMain.findViewById(R.id.new_splash_indicator);
        mIndicator.setViewPager(mNewFuncViewPager);
        mIndicator.setOnPageChangeListener(new GuidePageChangeListener(mNewFuncPageViews, 3));

        enterAppButton = (Button) page4.findViewById(R.id.button_guide);
        enterAppButton.setVisibility(View.VISIBLE);
        enterAppButton.setTextColor(getResources().getColor(
                R.color.new_guide_page1_background_color));
        enterAppButton.setBackgroundResource(R.drawable.new_letgo_bg_selecter);
        enterAppButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LeoLog.d(TAG, "teststart, enterAppButton--click...");
                enterHome();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mIsShowGuide) {
            mIsShowGuide = false;
        }
        super.onBackPressed();
    }

    class GuidePageChangeListener implements OnPageChangeListener {

        List<View> pageViews;
        int startColorIndex = 0;
        GuideItemView backGroundView;

        GuidePageChangeListener(List<View> pageViews) {
            this.pageViews = pageViews;
            initBackGroundView();
        }

        GuidePageChangeListener(List<View> pageViews, int startColorIndex) {
            this.pageViews = pageViews;
            this.startColorIndex = startColorIndex;
            initBackGroundView();
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            if (arg1 > 0.0f && arg0 + 1 < pageViews.size()) {
                int newColor = caculateNewColor(
                        mPageColors[arg0 + startColorIndex],
                        mPageColors[arg0 + 1 + startColorIndex], arg1);
                backGroundView.setCurrentColor(newColor);
            }
            /* disable edges scrolling effect */
            if (leftEdge != null && rightEdge != null) {
                leftEdge.finish();
                rightEdge.finish();
                leftEdge.setSize(0, 0);
                rightEdge.setSize(0, 0);
            }
            if (startColorIndex != 0) {
                if (mNewFuncViewPager != null) {
                    mNewFuncViewPager.invalidate();
                }
            }
        }

        @Override
        public void onPageSelected(int arg0) {
            if (pageViews.size() == (arg0 + 1)) {
                unsetSkipClickListener();
            } else {
                setSkipClickListener();
            }
        }

        private void initBackGroundView() {
            if (startColorIndex != 0) {
                backGroundView = mNewPageBackgroundView;
            }
        }
    }

    /* 闪屏链接是否存在 */
    private boolean checkSplashUrlIsEmpty() {
        return SplashBootstrap.mIsEmptyForSplashUrl;
    }

    /* 闪屏链接跳转按钮,点击跳转处理 */
    private void skipModeHandle() {
        AppMasterPreference pref = AppMasterPreference.getInstance(this);
        String skipMode = pref.getSplashSkipMode();
        if (!mIsEmptyForSplashUrl) {
            String url = AppMasterPreference.getInstance(this).getSplashSkipUrl();
            if (!Utilities.isEmpty(skipMode)) {
                if (Constants.SPLASH_SKIP_PG_WEBVIEW.equals(skipMode)) {
                    /* 跳转到pg内webview */
                    Log.i(TAG, "进入WebView");
                    startIntentForWebViewActivity(url);
                    finishForSkip(true);
                } else if (Constants.SPLASH_SKIP_PG_CLIENT.equals(skipMode)) {
                    /* 跳转到指定客户端 */
                    String clientIntent = AppMasterPreference.getInstance(this)
                            .getSplashSkipToClient();
                    if (clientIntent != null) {
                        try {
                            /* 存在客户端 */

                            mLockManager.filterPackage(Constants.PKG_FACEBOOK, 1000);

                            Intent intent = Intent.parseUri(clientIntent, 0);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            Log.i(TAG, "存在客户端并进入");
                        } catch (Exception e) {
                            /* 不存在指定客户端 */
                            Log.i(TAG, "不存在客户端进入进入到WebView");
                            startIntentForWebViewActivity(url);
                        } finally {
                            finishForSkip(true);
                        }
                    } else {
                        /* 不存在指定客户端 */
                        Log.i(TAG, "去客户端但是链接为空进入到WebView");
                        startIntentForWebViewActivity(url);
                        finishForSkip(true);
                    }
                }
            }
        }
    }

    private void startIntentForWebViewActivity(String url) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.WEB_URL, url);
        intent.putExtra(SPLASH_TO_WEBVIEW, SPLASH_TO_WEBVIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        Log.i(TAG, "跳转到PG的WebView中，URL=" + url);
    }
}
