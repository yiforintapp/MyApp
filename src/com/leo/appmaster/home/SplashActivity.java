
package com.leo.appmaster.home;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.drawable.NinePatchDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.LockSettingActivity;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.appwall.AppWallActivity;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.AppUnlockEvent;
import com.leo.appmaster.http.HttpRequestAgent;
import com.leo.appmaster.http.HttpRequestAgent.RequestListener;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.push.ui.WebViewActivity;
import com.leo.appmaster.ui.CirclePageIndicator;
import com.leo.appmaster.utils.FileOperationUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NetWorkUtil;
import com.leo.appmaster.utils.NinePatchChunk;
import com.leo.appmaster.utils.Utilities;

public class SplashActivity extends BaseActivity {

    public static final int MSG_LAUNCH_HOME_ACTIVITY = 1000;
    private Handler mEventHandler;

    /* Guide page stuff begin */
    private ViewPager mViewPager, mNewFuncViewPager;
    /* pages */
    private ArrayList<View> mPageViews, mNewFuncPageViews;
    private GuideItemView mPageBackgroundView, mNewPageBackgroundView;
    /* footer indicators */
    private CirclePageIndicator mIndicator;
    private ViewGroup mMain, mNewGuideMain;
    /* color for each page */
    private int[] mPageColors = new int[7];
    private EdgeEffectCompat leftEdge;
    private EdgeEffectCompat rightEdge;
    private RelativeLayout mSplashRL;
    private ImageView mSplashIcon;
    private ImageView mSplashName;
    private boolean mIsEmptyForSplashUrl;
    private ImageView mSkipUrlButton, mSkipToPgButton;
    private boolean mShowSplashFlag;

    /* Guide page stuff end */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LeoLog.d("SplashActivity", "onCreate");
        setContentView(R.layout.activity_splash_guide);
        // mSplashRL = (RelativeLayout) findViewById(R.id.splashRL);
        // mSplashIcon = (ImageView)
        // findViewById(R.id.image_view_splash_center);
        // mSplashName = (ImageView) findViewById(R.id.iv_back);
        // showSplash();
        initSplash();
        mEventHandler = new EventHandler();
        startInitTask();
        LeoEventBus.getDefaultBus().register(this, 2);
    }

    @SuppressLint("NewApi")
    private void initSplash() {
        AppMasterPreference pre = AppMasterPreference.getInstance(this);
        mSplashRL = (RelativeLayout) findViewById(R.id.splashRL);
        mSplashIcon = (ImageView) findViewById(R.id.image_view_splash_center);
        mSplashName = (ImageView) findViewById(R.id.iv_back);
        mSkipUrlButton = (ImageView) findViewById(R.id.url_skip_bt);
        mSkipToPgButton = (ImageView) findViewById(R.id.skip_to_pg_bt);
        mSkipUrlButton.setOnClickListener(new SkipUrlOnClickListener());
        mSkipToPgButton.setOnClickListener(new SkipUrlOnClickListener());
        mIsEmptyForSplashUrl = checkSplashUrlIsEmpty();
        showSkipUrlButton();
        showSkipEnterHomeButton();
        long startShowSplashTime = pre.getSplashStartShowTime();
        long endShowSplashTime = pre.getSplashEndShowTime();
        long currentTime = System.currentTimeMillis();
        /**
         * 可能存在的几种情况：
         * 
         * @1.只有开始时间
         * @2.只有结束时间
         * @3.没有配置时间
         * @4.开始.结束时间都有
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
                showDefaultSplash();
            }
        } else {
            showDefaultSplash();
        }
        PrivacyHelper.getInstance(this).setDirty(true);
    }

    private void showDefaultSplash() {
        /* 没有开始，没有结束时间，默认 */
        Log.d(Constants.RUN_TAG, "splash_end&start_time：No time!");
        clearSpSplashFlagDate();
        if (mSplashIcon.getVisibility() == View.INVISIBLE) {
            mSplashIcon.setVisibility(View.VISIBLE);
        }
        if (mSplashName.getVisibility() == View.INVISIBLE) {
            mSplashName.setVisibility(View.VISIBLE);
        }
        if (mSkipUrlButton.getVisibility() == View.VISIBLE) {
            mSkipUrlButton.setVisibility(View.GONE);
        }
        if (mSkipToPgButton.getVisibility() == View.VISIBLE) {
            mSkipToPgButton.setVisibility(View.GONE);
        }
    }

    /* 对后台配置的过期闪屏数据初始化 */
    private void clearSpSplashFlagDate() {
        if (AppMasterApplication.mSplashFlag) {
            AppMasterPreference.getInstance(getApplicationContext()).setSplashUriFlag(
                    Constants.SPLASH_FLAG);
            AppMasterApplication.mSplashFlag = false;
        }
        if (AppMasterApplication.mSplashDelayTime != Constants.SPLASH_DELAY_TIME) {
            AppMasterPreference.getInstance(this).setSplashDelayTime(Constants.SPLASH_DELAY_TIME);
            AppMasterApplication.mSplashDelayTime = Constants.SPLASH_DELAY_TIME;
        }
        if (!AppMasterApplication.mIsEmptyForSplashUrl) {
            AppMasterPreference.getInstance(this).setSplashSkipUrl(null);
            AppMasterApplication.mIsEmptyForSplashUrl = true;
        }
        deleteImage();
    }

    private void showSkipEnterHomeButton() {
        if (AppMasterApplication.mSplashFlag) {
            mSkipToPgButton.setVisibility(View.VISIBLE);
        }
    }

    /* 如果url存在则显示按钮 */
    private void showSkipUrlButton() {
        if (!mIsEmptyForSplashUrl) {
            mSkipUrlButton.setVisibility(View.VISIBLE);
        }
    }

    /* 闪屏链接跳转按钮单击监听 */
    private class SkipUrlOnClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            int viewId = v.getId();
            switch (viewId) {
                case R.id.url_skip_bt:
                    // Log.e(Constants.RUN_TAG, "立即设置");
                    skipModeHandle();
                    break;
                case R.id.skip_to_pg_bt:
                    // Log.e(Constants.RUN_TAG, "跳过");
                    startHome();
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
            option.inScaled = true;
            splash = BitmapFactory.decodeFile(path + Constants.SPLASH_NAME, option);
        }
        if (splash != null) {
            byte[] chunk = splash.getNinePatchChunk();
            if (chunk != null && NinePatch.isNinePatchChunk(chunk)) {
                mSplashIcon.setVisibility(View.INVISIBLE);
                mSplashName.setVisibility(View.INVISIBLE);
                mSplashRL.setBackgroundDrawable(new NinePatchDrawable(getResources(),
                        splash, chunk, NinePatchChunk.deserialize(chunk).mPaddings, null));
                mShowSplashFlag = true;
            }
        }
    }

    @Override
    protected void onDestroy() {
        LeoLog.d("SplashActivity", "onDestroy");
        LeoEventBus.getDefaultBus().unregister(this);
        super.onDestroy();
        mEventHandler.removeMessages(MSG_LAUNCH_HOME_ACTIVITY);
        mEventHandler = null;
    }

    @Override
    protected void onResume() {
        mEventHandler.removeMessages(MSG_LAUNCH_HOME_ACTIVITY);
        mEventHandler.sendEmptyMessageDelayed(MSG_LAUNCH_HOME_ACTIVITY,
                AppMasterApplication.mSplashDelayTime);
        LockManager lm = LockManager.getInstatnce();
        lm.clearFilterList();
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void onEvent(AppUnlockEvent event) {
        if (event.mUnlockResult == AppUnlockEvent.RESULT_UNLOCK_SUCCESSFULLY) {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            AppMasterApplication.getInstance().startActivity(intent);
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
                            showNewFuncGuide();
                        }
                    } else {
                        // AppMasterPreference pre = AppMasterPreference
                        // .getInstance(SplashActivity.this);
                        // // 存储的版本号
                        // String versionName = pre.getAppVersionName();
                        // // 获取当前的版本号
                        // String currentVersion =
                        // getString(R.string.version_name);
                        // if (!versionName.equals(currentVersion)) {
                        // boolean guidNotShown = mMain == null
                        // || mMain.getVisibility() != View.VISIBLE;
                        // if (guidNotShown) {
                        // showGuide();
                        // }
                        // } else {
                        // startHome();
                        // }
                        startHome();
                    }
                    break;

                default:
                    break;
            }
        }
    }

    private void startHome() {
        AppMasterPreference amp = AppMasterPreference.getInstance(this);
        if (amp.getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
            if (LockManager.getInstatnce().inRelockTime(getPackageName())) {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                LeoLog.d("Track Lock Screen", "apply lockscreen form SplashActivity");
                LockManager.getInstatnce().applyLock(LockManager.LOCK_MODE_FULL,
                        getPackageName(), true, null);
                amp.setDoubleCheck(null);
            }
        } else {
            Intent intent = new Intent(this, LockSettingActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void startInitTask() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                // get recommend app lock list
                final AppMasterPreference pref = AppMasterPreference
                        .getInstance(getApplicationContext());
                long lastPull = pref.getLastLocklistPullTime();
                long interval = pref.getPullInterval();
                if (interval < (System.currentTimeMillis() - lastPull)
                        && NetWorkUtil.isNetworkAvailable(SplashActivity.this)) {
                    AppLockListener listener = new AppLockListener(SplashActivity.this);
                    HttpRequestAgent.getInstance(getApplicationContext()).getAppLockList(listener,
                            listener);
                }
            }
        }).start();
    }

    /* add for Guide Screen begin */
    private void showGuide() {
        mPageColors[0] = getResources().getColor(R.color.guide_page1_background_color);
        mPageColors[1] = getResources().getColor(R.color.guide_page2_background_color);
        mPageColors[2] = getResources().getColor(R.color.guide_page3_background_color);
        mPageColors[3] = getResources().getColor(R.color.guide_page4_background_color);

        Log.i("tag", mPageColors[0] + "  " + mPageColors[3]);

        LayoutInflater inflater = getLayoutInflater();
        mPageViews = new ArrayList<View>();
        mPageBackgroundView = (GuideItemView) findViewById(R.id.guide_bg_view);
        mPageBackgroundView.initBackgroundColor(mPageColors[0]);
        ImageView bigImage = null;
        TextView tvTitle = null;
        TextView tvContent = null;
        /* page1 */
        ViewGroup page1 = (ViewGroup) inflater.inflate(R.layout.guide_page_layout, null);
        bigImage = (ImageView) page1.findViewById(R.id.guide_image);
        bigImage.setImageDrawable(getResources().getDrawable(R.drawable.page_1));
        tvTitle = (TextView) page1.findViewById(R.id.guide_tv_title);
        tvTitle.setText(getResources().getString(R.string.guide_page1_title));
        tvContent = (TextView) page1.findViewById(R.id.guide_tv_content);
        tvContent.setText(getResources().getString(R.string.guide_page1_content));
        setSkipClickListener(page1);
        mPageViews.add(page1);
        /* page2 */
        ViewGroup page2 = (ViewGroup) inflater.inflate(R.layout.guide_page_layout, null);
        bigImage = (ImageView) page2.findViewById(R.id.guide_image);
        bigImage.setImageDrawable(getResources().getDrawable(R.drawable.page_2));
        tvTitle = (TextView) page2.findViewById(R.id.guide_tv_title);
        tvTitle.setText(getResources().getString(R.string.guide_page2_title));
        tvContent = (TextView) page2.findViewById(R.id.guide_tv_content);
        tvContent.setText(getResources().getString(R.string.guide_page2_content));
        setSkipClickListener(page2);
        mPageViews.add(page2);
        /* page3 */
        ViewGroup page3 = (ViewGroup) inflater.inflate(R.layout.guide_page_layout, null);
        bigImage = (ImageView) page3.findViewById(R.id.guide_image);
        bigImage.setImageDrawable(getResources().getDrawable(R.drawable.page_3));
        tvTitle = (TextView) page3.findViewById(R.id.guide_tv_title);
        tvTitle.setText(getResources().getString(R.string.guide_page3_title));
        tvContent = (TextView) page3.findViewById(R.id.guide_tv_content);
        tvContent.setText(getResources().getString(R.string.guide_page3_content));
        setSkipClickListener(page3);
        mPageViews.add(page3);
        /* page4 */
        ViewGroup page4 = (ViewGroup) inflater.inflate(R.layout.guide_page_layout, null);
        bigImage = (ImageView) page4.findViewById(R.id.guide_image);
        bigImage.setImageDrawable(getResources().getDrawable(R.drawable.page_4));
        tvTitle = (TextView) page4.findViewById(R.id.guide_tv_title);
        tvTitle.setText(getResources().getString(R.string.guide_page4_title));
        tvContent = (TextView) page4.findViewById(R.id.guide_tv_content);
        tvContent.setText(getResources().getString(R.string.guide_page4_content));
        mPageViews.add(page4);

        mMain = (ViewGroup) findViewById(R.id.layout_guide);
        mViewPager = (ViewPager) mMain.findViewById(R.id.guide_viewpager);
        initViewPagerEdges(mViewPager);

        mMain.setVisibility(View.VISIBLE);
        /*
         * AlphaAnimation aa = new AlphaAnimation(0.0f, 1.0f);
         * aa.setDuration(500); mMain.startAnimation(aa);
         */

        mViewPager.setAdapter(new GuidePageAdapter(mPageViews));
        mIndicator = (CirclePageIndicator) findViewById(R.id.splash_indicator);
        mIndicator.setViewPager(mViewPager);
        mIndicator.setOnPageChangeListener(new GuidePageChangeListener(mPageViews));

        Button button = (Button) mPageViews.get(mPageViews.size() - 1).findViewById(
                R.id.button_guide);
        button.setVisibility(View.VISIBLE);
        button.setBackgroundResource(R.drawable.letgo_bg);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                enterHome();
            }
        });
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
    private void setSkipClickListener(ViewGroup page) {
        final TextView skipText;
        skipText = (TextView) page.findViewById(R.id.skip_tv);
        skipText.setVisibility(View.VISIBLE);
        /*
         * skipText.setOnTouchListener(new OnTouchListener() {
         * @Override public boolean onTouch(View v, MotionEvent event) { int
         * action = event.getAction(); switch (action) { case
         * MotionEvent.ACTION_DOWN: skipText.setAlpha(0.6f); break; default:
         * break; } return false; } });
         */

        skipText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                enterHome();
            }
        });
    }

    private void enterHome() {
        AppMasterPreference.getInstance(SplashActivity.this).setGuidePageFirstUse(false);
        startHome();
        String currentVersionName = SplashActivity.this
                .getString(R.string.version_name);
        AppMasterPreference.getInstance(SplashActivity.this).setAppVersionName(
                currentVersionName);
    }

    private void showNewFuncGuide() {
        mNewFuncPageViews = new ArrayList<View>();
        LayoutInflater inflater = getLayoutInflater();
        TextView tvTitle, tvContent, tvMoreFunc;
        Button enterAppButton;
        ImageView bigImage = null;
        mPageColors[4] = getResources().getColor(R.color.new_guide_page1_background_color);
        mPageColors[5] = getResources().getColor(R.color.new_guide_page2_background_color);
        mPageColors[6] = getResources().getColor(R.color.new_guide_page3_background_color);

        mNewPageBackgroundView = (GuideItemView) findViewById(R.id.new_func_guide_bg_view);
        mNewPageBackgroundView.initBackgroundColor(mPageColors[4]);
        /* page1 */
        ViewGroup page1 = (ViewGroup) inflater.inflate(R.layout.guide_page_layout, null);
        bigImage = (ImageView) page1.findViewById(R.id.guide_image);
        bigImage.setImageDrawable(getResources().getDrawable(R.drawable.new_page_1));
        tvTitle = (TextView) page1.findViewById(R.id.guide_tv_title);
        tvTitle.setText(getResources().getString(R.string.new_guide_page1_title));
        tvContent = (TextView) page1.findViewById(R.id.guide_tv_content);
        tvContent.setText(getResources().getString(R.string.new_guide_page1_content));
        setSkipClickListener(page1);
        mNewFuncPageViews.add(page1);

        /* page2 */
        ViewGroup page2 = (ViewGroup) inflater.inflate(R.layout.guide_page_layout, null);
        bigImage = (ImageView) page2.findViewById(R.id.guide_image);
        bigImage.setImageDrawable(getResources().getDrawable(R.drawable.new_page_2));
        tvTitle = (TextView) page2.findViewById(R.id.guide_tv_title);
        tvTitle.setText(getResources().getString(R.string.new_guide_page2_title));
        tvContent = (TextView) page2.findViewById(R.id.guide_tv_content);
        tvContent.setText(getResources().getString(R.string.new_guide_page2_content));
        setSkipClickListener(page2);
        mNewFuncPageViews.add(page2);

        /* page3 */
        ViewGroup page3 = (ViewGroup) inflater.inflate(R.layout.guide_page_layout, null);
        bigImage = (ImageView) page3.findViewById(R.id.guide_image);
        bigImage.setImageDrawable(getResources().getDrawable(R.drawable.new_page_3));
        tvTitle = (TextView) page3.findViewById(R.id.guide_tv_title);
        tvTitle.setText(getResources().getString(R.string.new_guide_page3_title));
        tvContent = (TextView) page3.findViewById(R.id.guide_tv_content);
        tvContent.setText(getResources().getString(R.string.new_guide_page3_content));
        mNewFuncPageViews.add(page3);

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
        mIndicator.setOnPageChangeListener(new GuidePageChangeListener(mNewFuncPageViews, 4));

        tvMoreFunc = (TextView) page3.findViewById(R.id.more_func);
        tvMoreFunc.setVisibility(View.VISIBLE);
        tvMoreFunc.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mNewGuideMain.setVisibility(View.INVISIBLE);
                showGuide();
            }
        });

        enterAppButton = (Button) page3.findViewById(R.id.button_guide);
        enterAppButton.setVisibility(View.VISIBLE);
        enterAppButton.setTextColor(getResources().getColor(
                R.color.new_guide_page3_background_color));
        enterAppButton.setBackgroundResource(R.drawable.new_letgo_bg_selecter);
        enterAppButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                enterHome();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mMain != null && mMain.getVisibility() == View.VISIBLE) {
            mMain.setVisibility(View.INVISIBLE);
            mNewGuideMain.setVisibility(View.VISIBLE);
            mNewFuncViewPager.setCurrentItem(mNewFuncPageViews.size() - 1);
            initViewPagerEdges(mNewFuncViewPager);
        } else {
            super.onBackPressed();
        }
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
            if (startColorIndex == 0) {
                if (mViewPager != null) {
                    mViewPager.invalidate();
                }
            } else {
                if (mNewFuncViewPager != null) {
                    mNewFuncViewPager.invalidate();
                }
            }
        }

        @Override
        public void onPageSelected(int arg0) {
        }

        private void initBackGroundView() {
            if (startColorIndex == 0) {
                backGroundView = mPageBackgroundView;
            } else {
                backGroundView = mNewPageBackgroundView;
            }
        }
    }

    private static class AppLockListener extends RequestListener<SplashActivity> {

        public AppLockListener(SplashActivity outerContext) {
            super(outerContext);
        }

        @Override
        public void onResponse(JSONObject response, boolean noMidify) {
            Context ctx = AppMasterApplication.getInstance();
            AppMasterPreference pref = AppMasterPreference.getInstance(ctx);

            JSONArray list;
            ArrayList<String> lockList = new ArrayList<String>();
            long next_pull;
            JSONObject data;
            try {
                data = response.getJSONObject("data");
                list = data.getJSONArray("list");
                for (int i = 0; i < list.length(); i++) {
                    lockList.add(list.getString(i));
                }
                next_pull = data.getLong("next_pull");
                LeoLog.d("next_pull = " + next_pull + " lockList = ", lockList.toString());

                pref.setPullInterval(next_pull * 24 * 60 * 60 * 1000);
                pref.setLastLocklistPullTime(System.currentTimeMillis());
                Intent intent = new Intent(AppLoadEngine.ACTION_RECOMMEND_LIST_CHANGE);
                intent.putStringArrayListExtra(Intent.EXTRA_PACKAGES, lockList);
                ctx.sendBroadcast(intent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            LeoLog.d("Pull Lock list", error.getMessage());
        }
    }

    /* 闪屏链接是否存在 */
    private boolean checkSplashUrlIsEmpty() {
        return AppMasterApplication.getInstance().mIsEmptyForSplashUrl;
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
                    Log.e(Constants.RUN_TAG, "进入WebView");
                    startIntentForWebViewActivity(url);
                } else if (Constants.SPLASH_SKIP_PG_CLIENT.equals(skipMode)) {
                    /* 跳转到指定客户端 */
                    String packageName = pref.getSplashSkipToClient();
                    boolean existClient = checkExistClient(packageName);
                    if (existClient) {
                        /* 存在客户端 */
                        Log.e(Constants.RUN_TAG, "存在客户端并进入:" + packageName);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        Uri uri = Uri.parse(url);
                        intent.setData(uri);
                        intent.setPackage(packageName);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        /* 不存在指定客户端 */
                        Log.e(Constants.RUN_TAG, "不存在客户端进入进入到WebView");
                        startIntentForWebViewActivity(url);
                    }
                }
            }
        }
        finish();
    }

    /* 检查要跳转的客户端是否存在 */
    private boolean checkExistClient(String packageName) {
        if (!Utilities.isEmpty(packageName)) {
            List<AppItemInfo> pkgInfos = AppLoadEngine.getInstance(this).getAllPkgInfo();
            List<String> packageNames = new ArrayList<String>();
            for (AppItemInfo info : pkgInfos) {
                packageNames.add(info.packageName);
            }
            for (String string : packageNames) {
                if (packageName.equals(string)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void startIntentForWebViewActivity(String url) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.WEB_URL, url);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
