package com.leo.appmaster.home;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.PhoneInfo;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.activity.AboutActivity;
import com.leo.appmaster.applocker.LockOptionActivity;
import com.leo.appmaster.applocker.manager.ChangeThemeManager;
import com.leo.appmaster.applocker.model.ProcessDetectorCompat22;
import com.leo.appmaster.applocker.receiver.DeviceReceiver;
import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.db.MsgCenterTable;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.BackupEvent;
import com.leo.appmaster.eventbus.event.MsgCenterEvent;
import com.leo.appmaster.feedback.FeedbackActivity;
import com.leo.appmaster.feedback.FeedbackHelper;
import com.leo.appmaster.fragment.GuideFragment;
import com.leo.appmaster.home.HomeScanningFragment.PhotoList;
import com.leo.appmaster.mgr.IntrudeSecurityManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.privacycontact.ContactBean;
import com.leo.appmaster.quickgestures.ISwipUpdateRequestManager;
import com.leo.appmaster.quickgestures.IswipUpdateTipDialog;
import com.leo.appmaster.schedule.MsgCenterFetchJob;
import com.leo.appmaster.schedule.PhoneSecurityFetchJob;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.DrawerArrowDrawable;
import com.leo.appmaster.ui.HomeUpArrow;
import com.leo.appmaster.ui.MaterialRippleLayout;
import com.leo.appmaster.ui.dialog.LEOAnimationDialog;
import com.leo.appmaster.ui.dialog.LEOMessageDialog;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.LanguageUtils;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.RootChecker;
import com.leo.appmaster.utils.Utilities;
import com.leo.appmaster.videohide.VideoItemBean;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.utils.IoUtils;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends BaseFragmentActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener {
    private static final String TAG = "HomeActivity";

    private DrawerLayout mDrawerLayout;
    private ListView mMenuList;
    private HomeToolbar mToolbar;
    private DrawerArrowDrawable mDrawerArrowDrawable;
    private float mDrawerOffset;
    private List<MenuItem> mMenuItems;
    private MenuAdapter mMenuAdapter;
    //    private ImageView mAdIcon;
    //    private MobvistaAdWall mWallAd;
    private int mMenuTextColorId;
    private Handler mHandler = new Handler();
    private PreferenceTable mPt = PreferenceTable.getInstance();
    public static int mHomeAdSwitchOpen = -1;

    private IswipUpdateTipDialog mIswipDialog;
    private IntrudeSecurityManager mISManger;
    private boolean mShowIswipeFromNotfi;

    public HomeMoreFragment mMoreFragment;
    private HomePrivacyFragment mPrivacyFragment;
    private HomeTabFragment mTabFragment;
    private GuideFragment mGuideFragment;
    private HomeScanningFragment mScanningFragment;
    private Fragment mCurrentFragment;
    private PrivacyHelper mPrivacyHelper;
    private boolean mProcessAlreadyTimeout;
    private int mProcessedScore;
    private String mProcessedMgr;

    private int mHeaderHeight;
    private int mToolbarHeight;
    private CommonToolbar mCommonToolbar;
    private Animation mComingInAnim;
    private Animation mComingOutAnim;

    private List<AppItemInfo> mAppList;
    private PhotoList mPhotoList;
    private List<VideoItemBean> mVideoList;
    private List<ContactBean> mContactList;

    private boolean mShownIgnoreDlg;
    private boolean mShowContact;

    private LEOAnimationDialog mMessageDialog;

    private BroadcastReceiver mLocaleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;

            String action = intent.getAction();
            LeoLog.i(TAG, "locale change, action: " + action);
            if (Intent.ACTION_LOCALE_CHANGED.equals(action)) {
                finish();
            }
        }
    };
    private int mScoreBeforeProcess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        removeFragments();
        if (savedInstanceState != null) {
            // 移除保存的fragment状态
            savedInstanceState.remove("android:support:fragments");
        }
        super.onCreate(savedInstanceState);
        LeoLog.d(TAG, "onCreate... savedInstanceState: " + savedInstanceState);

        setContentView(R.layout.activity_home_main);
        SDKWrapper.addEvent(this, SDKWrapper.P1, "home", "enter");
        mPrivacyHelper = PrivacyHelper.getInstance(this);
        mISManger = (IntrudeSecurityManager) MgrContext.getManager(MgrContext.MGR_INTRUDE_SECURITY);
        initUI();
//        initMobvista();
        requestCamera();

        FeedbackHelper.getInstance().tryCommit();
        shortcutAndRoot();
        recordEnterHomeTimes();

//        autoStartDialogHandler();
        // 进入首页拉取一次消息中心列表
        MsgCenterFetchJob.startImmediately();
        LeoEventBus.getDefaultBus().register(this);

        checkEnterScanIntent(getIntent());

        /*手机防盗开启人数，在用户没有打开手机防盗时没此进入主页拉取一次*/
        PhoneSecurityFetchJob.startImmediately();
        registerLocaleChange();

        openAdvanceProtectDialogHandler();
    }

    private void requestCamera() {
        if ((!mPt.getBoolean(PrefConst.KEY_HAS_REQUEST_CAMERA, false) && (mISManger.getIsIntruderSecurityAvailable()))) {

            ThreadManager.executeOnAsyncThreadDelay(new Runnable() {
                @Override
                public void run() {
                    Camera camera = null;
                    try {
                        LeoLog.i(TAG, "requestCamera!");
                        camera = Camera.open(CameraInfo.CAMERA_FACING_FRONT);
                    } catch (Throwable e) {

                    } finally {
                        if (camera != null) {
                            camera.release();
                        }
                    }
                }
            }, 1000);
        }
        mPt.putBoolean(PrefConst.KEY_HAS_REQUEST_CAMERA, true);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkEnterScanIntent(intent);
    }

    private void checkEnterScanIntent(Intent intent) {
        if (intent == null) return;

        boolean enterScan = intent.getBooleanExtra(Constants.PRIVACY_ENTER_SCAN, false);
        int type = intent.getIntExtra(Constants.PRIVACY_ENTER_SCAN_TYPE, PrivacyHelper.PRIVACY_NONE);
        String fromWhere = intent.getStringExtra(Constants.FROM_WHERE);
        LeoLog.i(TAG, "checkEnterScanIntent, enterScan: " + enterScan + " | type: " + type);
        if (enterScan) {
            String description = null;
            if (type == PrivacyHelper.PRIVACY_APP_LOCK) {
                description = "prilevel_cnts_app";
            } else if (type == PrivacyHelper.PRIVACY_HIDE_PIC) {
                description = "prilevel_cnts_pic";
            } else if (type == PrivacyHelper.PRIVACY_HIDE_VID) {
                description = "prilevel_cnts_vid";
            }
            if (description != null) {
                SDKWrapper.addEvent(this, SDKWrapper.P1, "prilevel", description);
            }
            if (fromWhere != null && fromWhere.equals(Constants.FROM_PUSH)) {
                SDKWrapper.addEvent(this, SDKWrapper.P1, "push_refresh", "push_scan_cnts");
                LeoLog.d("testFromWhere", "HomeActivity from push");
            }
            onShieldClick();
        }
    }

    public void registerLocaleChange() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        registerReceiver(mLocaleReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LeoEventBus.getDefaultBus().unregister(this);
//        if (mWallAd != null) {
//            mWallAd.release();
//            mWallAd = null;
//        }
        if (mIswipDialog != null) {
            mIswipDialog.dismiss();
            mIswipDialog = null;
        }

        // 重置隐私等级减少的分数
        PrivacyHelper.getInstance(this).resetDecScore();
        ImageLoader.getInstance().clearMemoryCache();
        unregisterReceiver(mLocaleReceiver);
    }

    public void onEventMainThread(BackupEvent event) {
        // TODO: 2015/9/30 检查是否必要
//        String msg = event.eventMsg;
//        if (HomeAppManagerFragment.FINISH_HOME_ACTIVITY_FALG.equals(msg)) {
//            this.finish();
//        } else if (HomeAppManagerFragment.ISWIPE_CANCEL_RED_TIP.equals(msg)) {
//            if (mPagerTab != null) {
//                mFragmentHolders[2].isRedTip = false;
//                mPagerTab.notifyDataSetChanged();
//            }
//        }
    }


    public void onEvent(final MsgCenterEvent event) {
        // 设置消息中心未读计数
        if (event.getEventId() != MsgCenterEvent.ID_MSG)
            return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int count = event.count;

                if (count <= 0) {
                    mToolbar.showMsgcenterUnreadCount(false, 0);
                } else {
                    mToolbar.showMsgcenterUnreadCount(true, count);
                }
            }
        });
        LeoLog.i(TAG, "onEvent, event: " + event);
    }

    public void onShieldClick() {
        int securityScore = mPrivacyHelper.getSecurityScore();
        if (securityScore == 100) {
            mShowContact = false;
//            mMoreFragment.setEnable(false);
//            startProcess();
//            Toast.makeText(this, R.string.pri_pro_summary_confirm, Toast.LENGTH_SHORT).show();
//            return;
        } else {
            mShowContact = true;
        }
        if (!mTabFragment.isTabDismiss() && !mTabFragment.isAnimating()) {
            mTabFragment.dismissTab();
            mPrivacyFragment.setShowColorProgress(false);
            mMoreFragment.setEnable(false);

            Animation comingIn = AnimationUtils.loadAnimation(this, R.anim.alpha_coming_in);
            Animation comingOut = AnimationUtils.loadAnimation(this, R.anim.alpha_coming_out);

            mToolbar.startAnimation(comingIn);
            mCommonToolbar.startAnimation(comingOut);
            mToolbar.setVisibility(View.INVISIBLE);
            mCommonToolbar.setVisibility(View.VISIBLE);
            mCommonToolbar.setBackgroundColor(getResources().getColor(R.color.transparent));

            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            SDKWrapper.addEvent(this, SDKWrapper.P1, "home", "home_privacyScan");
        }
    }

    public void onTabAnimationFinish() {
        if (isFinishing()) return;

        if (!mTabFragment.isTabDismiss()) {
            mPrivacyFragment.setShowColorProgress(true);
        } else {
            int securityScore = mPrivacyHelper.getSecurityScore();
            if (securityScore == 100) {
                startProcess();
            } else {
                mScanningFragment = new HomeScanningFragment();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.addToBackStack(null);
                ft.replace(R.id.pri_pro_content, mScanningFragment);
                boolean commited = false;
                try {
                    ft.commit();
                    commited = true;
                } catch (Exception e) {
                }
                if (!commited) {
                    try {
                        ft.commitAllowingStateLoss();
                    } catch (Exception e) {
                    }
                }
                mCurrentFragment = mScanningFragment;
            }
        }
    }

    public void onScanningStart(int duration) {
        mPrivacyFragment.showScanningPercent(duration);
    }

    public void scanningFromPercent(int duration, int from, int to) {
        mPrivacyFragment.showScanningPercent(duration, from, to);
    }

    public int getScanningPercent() {
        return mPrivacyFragment.getScanningPercent();
    }

    public void onScanningFinish(List<AppItemInfo> appList, PhotoList photoItems, List<VideoItemBean> videoItemBeans) {
        mAppList = appList;
        mPhotoList = photoItems;
        mVideoList = videoItemBeans;
    }

    public void onExitScanning() {
        if (mTabFragment.isTabDismiss()) {
            LeoLog.d(TAG, "onExitScanning...");
            reportFragmentExit();
            mTabFragment.showTab(new HomeTabFragment.OnShowTabListener() {
                @Override
                public void onShowTabListener() {
                    mMoreFragment.setEnable(true);
                    /*首页引导*/
                    showHomeGuide();
                }
            });
            getSupportFragmentManager().popBackStack();
            mPrivacyFragment.setInterceptRaiseAnim();
            mPrivacyFragment.reset();

            mToolbar.setVisibility(View.VISIBLE);
            mCommonToolbar.setVisibility(View.INVISIBLE);

            mToolbar.startAnimation(mComingOutAnim);
            mToolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
            mCommonToolbar.startAnimation(mComingInAnim);

            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mMoreFragment.cancelUpArrowAnim();
        }
    }

    public void setContactList(List<ContactBean> contactList) {
        mContactList = contactList;
    }

    // 是否已显示过跳过弹窗
    public boolean shownIgnoreDlg() {
        return mShownIgnoreDlg;
    }

    // 设置已显示过跳过弹窗
    public void setShownIngoreDlg() {
        mShownIgnoreDlg = true;
    }

    public void startProcess() {
        mShownIgnoreDlg = false;
        int count = 1;
        if (mAppList != null && mAppList.size() > 0) {
            count++;
        }

        if (mPhotoList != null && mPhotoList.photoItems.size() > 0) {
            count++;
        }

        if (mVideoList != null && mVideoList.size() > 0) {
            count++;
        }
        mPrivacyFragment.startProcessing(count);
        jumpToNextFragment();
        mScanningFragment = null;
    }

    public int getToolbarColor() {
        return mPrivacyFragment.getToolbarColor();
    }

    public void jumpToNextFragment() {
        mPrivacyFragment.showProcessProgress(PrivacyHelper.PRIVACY_NONE);

        FragmentManager fm = getSupportFragmentManager();
        try {
            fm.popBackStack();
        } catch (Exception e) {
        }
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.anim_down_to_up, 0, 0, R.anim.anim_up_to_down);
        if (mAppList != null && mAppList.size() > 0) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "process", "app_arv");
            PrivacyNewFragment fragment = PrivacyNewAppFragment.newInstance();
            ft.replace(R.id.pri_pro_content, fragment);
            fragment.setData(mAppList);
            mAppList = null;
            mCurrentFragment = fragment;
        } else if (mPhotoList != null && mPhotoList.photoItems.size() > 0) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "process", "pic_arv");
            Fragment fragment = PrivacyNewPicFragment.getFragment(mPhotoList);
            ft.replace(R.id.pri_pro_content, fragment);
            mPhotoList = null;
            mCurrentFragment = fragment;
        } else if (mVideoList != null && mVideoList.size() > 0) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "process", "vid_arv");
            Fragment fragment = PrivacyNewVideoFragment.getFragment(mVideoList);
            ft.replace(R.id.pri_pro_content, fragment);
            mVideoList = null;
            mCurrentFragment = fragment;
        } else {
            mPrivacyFragment.startFinalAnim();
            PrivacyConfirmFragment fragment = PrivacyConfirmFragment.newInstance(mShowContact);
            fragment.setDataList(mContactList);
            ft.replace(R.id.pri_pro_content, fragment);
            mCurrentFragment = fragment;
        }
        ft.addToBackStack(null);
        IoUtils.commitSafely(ft);
    }

    private void initUI() {
        mMoreFragment = (HomeMoreFragment) getSupportFragmentManager().findFragmentById(R.id.home_more_ft);
        mPrivacyFragment = (HomePrivacyFragment) getSupportFragmentManager().findFragmentById(R.id.home_anim_ft);
        mTabFragment = (HomeTabFragment) getSupportFragmentManager().findFragmentById(R.id.home_tab_ft);
        mGuideFragment = (GuideFragment) getSupportFragmentManager().findFragmentById(R.id.home_guide);
        mGuideFragment.setEnable(false, GuideFragment.GUIDE_TYPE.HOME_MORE_GUIDE);

        mHeaderHeight = getResources().getDimensionPixelSize(R.dimen.pri_pro_header);
        mToolbarHeight = getResources().getDimensionPixelSize(R.dimen.toolbar_height);
        mCommonToolbar = (CommonToolbar) findViewById(R.id.home_common_toobar);
        mCommonToolbar.setToolbarTitle(R.string.home_privacy_status);
        mCommonToolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
        mCommonToolbar.setNavigationClickListener(this);

        mComingInAnim = AnimationUtils.loadAnimation(this, R.anim.alpha_coming_in);
        mComingOutAnim = AnimationUtils.loadAnimation(this, R.anim.alpha_coming_out);

        mDrawerArrowDrawable = new DrawerArrowDrawable(getResources());
        mDrawerArrowDrawable.setStrokeColor(getResources()
                .getColor(R.color.white));
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                mDrawerOffset = slideOffset;
                mDrawerArrowDrawable.setParameter(mDrawerOffset);
                if (slideOffset > 0) {
                    int color = mPrivacyFragment.getToolbarColor();
                    int a = Color.alpha(color);
                    int r = Color.red(color);
                    int g = Color.green(color);
                    int b = Color.blue(color);
                    a *= slideOffset;

                    color = Color.argb(a, r, g, b);
                    mToolbar.setBackgroundColor(color);
                    mMoreFragment.setEnable(false);
                    mToolbar.setNavigationLogoResource(R.drawable.ic_toolbar_back);
                } else {
                    mToolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
                    mMoreFragment.setEnable(true);
                    mToolbar.setNavigationLogoResource(R.drawable.ic_toolbar_menu);
                }
            }
        });
        mMenuList = (ListView) findViewById(R.id.menu_list);
        tryChangeToChrismasTheme();
        mMenuItems = getMenuItems();
        mMenuAdapter = new MenuAdapter(this, mMenuItems);
        mMenuList.setAdapter(mMenuAdapter);
        mMenuList.setOnItemClickListener(this);

        mToolbar = (HomeToolbar) findViewById(R.id.home_tool_bar);
        mToolbar.setDrawerLayout(mDrawerLayout);

//        mAdIcon = (ImageView) findViewById(R.id.iv_ad_icon);
//        mAdIcon.setOnClickListener(this);
    }

    private void tryChangeToChrismasTheme() {
        Drawable drawable = ChangeThemeManager.getChrismasThemeDrawbleBySlotId(ChangeThemeManager.BG_HOME_ASIDE_FRAGMENT, this);
        if (drawable != null) {
            mMenuList.setBackgroundDrawable(drawable);
            mMenuList.setDivider(getResources().getDrawable(R.drawable.home_menu_list_divider_chrismas));
            mMenuList.setDividerHeight(1);
            mMenuTextColorId = getResources().getColor(R.color.c1);
        } else {
            mMenuTextColorId = getResources().getColor(R.color.home_menu_text);
        }
    }

    @Override
    public void onBackPressed() {
        if (mMoreFragment.isPanelOpen()) {
            mMoreFragment.closePanel();
            return;
        }
        if (mShowIswipeFromNotfi) {
            getIntent().removeExtra(ISwipUpdateRequestManager.ISWIP_NOTIFICATION_TO_PG_HOME);
        }
        if (mDrawerLayout.isDrawerOpen(mMenuList)) {
            mDrawerLayout.closeDrawer(mMenuList);
            return;
        }

        if (getFragmentManager().getBackStackEntryCount() > 0
                || mTabFragment.isTabDismiss()) {
            try {
                onExitScanning();
            } catch (Exception e) {
                LeoLog.e(TAG, "ex on onExitScanning...", e);
            }
        } else {
            finish();

            // ===== AMAM-1336 ========
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            try {
                startActivity(intent);
            } catch (Exception e) {
            }
        }

    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        try {
            return super.onCreateView(name, context, attrs);
        } catch (Exception e) {
            removeFragments();
        }
        return super.onCreateView(name, context, attrs);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        LeoLog.d(TAG, "onSaveInstanceState...");
        try {
            super.onSaveInstanceState(outState);
        } catch (Exception e) {
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        LeoLog.d(TAG, "onRestoreInstanceState...");
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {
        }
    }

    private void reportFragmentExit() {
        int score = mPrivacyHelper.getSecurityScore();
        if (mCurrentFragment instanceof HomeScanningFragment) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "scan", "cancel");
        } else if (mCurrentFragment instanceof PrivacyNewAppFragment) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "points", "points_app_" + score);
        } else if ((mCurrentFragment instanceof PrivacyNewPicFragment) ||
                (mCurrentFragment instanceof FolderPicFragment)) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "points", "points_pic_" + score);
        } else if ((mCurrentFragment instanceof PrivacyNewVideoFragment ||
                mCurrentFragment instanceof FolderVidFragment)) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "points", "points_vid_" + score);
        } else if (mCurrentFragment instanceof PrivacyConfirmFragment) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "points", "points_fsh_" + score);
        }
    }

    private void removeFragments() {
        long start = SystemClock.elapsedRealtime();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        try {
            List<Fragment> list = fm.getFragments();
            if (list != null) {
                for (Fragment f : list) {
                    ft.remove(f);
                }
            }
            IoUtils.commitSafely(ft);
        } catch (Exception e) {
            LeoLog.w(TAG, "remove fragments ex.", e);
        }
        LeoLog.i(TAG, "removeFragments, cost: " + (SystemClock.elapsedRealtime() - start));
    }

    private void initMobvista() {
        // app wall at home is abandon
//        mWallAd = MobvistaEngine.getInstance(this).createAdWallController(this, Constants.UNIT_ID_64);
//        if (mWallAd != null) {
//            mWallAd.preloadWall();
//        }

//        mHandler.postDelayed((new Runnable() {
//            @Override
//            public void run() {
//                // 默认是开，记得改回默认是关
//                if (mHomeAdSwitchOpen == -1) {
//                    LeoLog.d(TAG, "获取主页广告开关");
//                    mHomeAdSwitchOpen = AppMasterPreference.getInstance(HomeActivity.this).getIsADAtAppLockFragmentOpen();
//                }
//                LeoLog.d(TAG, "开关值是：" + mHomeAdSwitchOpen);
//                if (isTimetoShow()
//                        // && !isEnterPrivacySuggest
//                        && mHomeAdSwitchOpen == 1) {
//                    SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "ad_act", "adv_shws_homeAppWall");
//                    setAdIconVisible();
//                } else {
//                    setAdIconInVisible();
//                }
//            }
//        }), 1000);
    }

//    public boolean isTimetoShow() {
//        long clickTime = AppMasterPreference.getInstance(this).getAdClickTimeFromHome();
//        long nowTime = System.currentTimeMillis();
//        if (nowTime - clickTime > Constants.TIME_ONE_DAY) {
//            LeoLog.d("testHomeAd", "isTimetoShow true");
//            return true;
//        } else {
//            LeoLog.d("testHomeAd", "isTimetoShow false");
//            return false;
//        }
//    }

    public void recordEnterHomeTimes() {
        AppMasterPreference pref = AppMasterPreference.getInstance(this);
        int times = pref.getEnterHomeTimes();
        if (times < 2) {
            pref.setEnterHomeTimes(++times);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //这里只是为了更新标记
        Drawable chrismasThemeDrawbleBySlotId = ChangeThemeManager.getChrismasThemeDrawbleBySlotId(ChangeThemeManager.BG_HOME_TAB1, this);
        LeoLog.d(TAG, "onResume...");
        judgeShowGradeTip();
        /* 获取是否从iswipe通知进入 */
        checkIswipeNotificationTo();
        /* 分析是否需要升级红点显示 */
        if (SDKWrapper.isUpdateAvailable()) {
            mToolbar.showMenuRedTip(true);
        }
        /* check if there is force update when showing HomeActivityOld */
        SDKWrapper.checkForceUpdate();


        // tryIsFromLockMore();

        showIswipDialog();
        SDKWrapper.addEvent(this, SDKWrapper.P1, "tdau", "home");

        ProcessDetectorCompat22.setForegroundScore();
        if (!BuildProperties.isApiLevel14()) {
            // 设置消息中心未读计数
            setMsgCenterUnread();
        }
        /* 判断是否打开高级保护，显示“卸载”项 */
        addUninstallPgTOMenueItem();
        if (!LeoEventBus.getDefaultBus().isRegistered(this)) {
            LeoEventBus.getDefaultBus().register(this);
        }
    }

    private void addUninstallPgTOMenueItem() {
        LeoLog.i(TAG, "是否开启了高级保护：" + isAdminActive());
        mMenuAdapter = null;
        mMenuItems = null;
        mMenuItems = getMenuItems();
        mMenuAdapter = new MenuAdapter(this, mMenuItems);
        if (mMenuList.getAdapter() != null) {
            mMenuList.setAdapter(mMenuAdapter);
        }
        mDrawerLayout.postInvalidate();
    }

    private void showIswipDialog() {
        AppMasterPreference amp = AppMasterPreference.getInstance(this);
        boolean quickGestureFristTip = amp.getFristSlidingTip();
        if (quickGestureFristTip && !ISwipUpdateRequestManager.isInstallIsiwpe(this)) {
            showDownLoadISwipDialog(this, "homeactivity");
        }
        amp.setFristSlidingTip(false);
    }

    private void showDownLoadISwipDialog(Context context, String flag) {
        if (mIswipDialog == null) {
            mIswipDialog = new IswipUpdateTipDialog(context);
        }
        mIswipDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mIswipDialog != null) {
                    mIswipDialog = null;
                }
            }
        });

        mIswipDialog.setLeftListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mShowIswipeFromNotfi) {
                    mShowIswipeFromNotfi = false;
                    /* 对来自通知栏的统计 */
                    SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "qs_iSwipe", "old_statusbar_n");
                } else {
                    /* 非通知栏 */
                    SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "qs_iSwipe", "old_dia_n");
                }
                /* 稍后再说 */
                if (mIswipDialog != null) {
                    mIswipDialog.dismiss();
                }
            }
        });
        mIswipDialog.setRightListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mShowIswipeFromNotfi) {
                    mShowIswipeFromNotfi = false;
                    /* 对来自通知栏的统计 */
                    SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "qs_iSwipe", "old_statusbar_y");
                } else {
                    /* 非通知栏 */
                    SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "qs_iSwipe", "old_dia_y");
                }
                /* 立即下载 */

                if (mIswipDialog != null) {
                    mIswipDialog.dismiss();
                }
                ISwipUpdateRequestManager.getInstance(HomeActivity.this).iSwipDownLoadHandler();
            }
        });
        getIntent().removeExtra(ISwipUpdateRequestManager.ISWIP_NOTIFICATION_TO_PG_HOME);
        mIswipDialog.setFlag(flag);
        mIswipDialog.show();
    }

    private void setMsgCenterUnread() {
        ThreadManager.executeOnFileThread(new Runnable() {
            @Override
            public void run() {
                MsgCenterTable table = new MsgCenterTable();
                final int unreadCount = table.getUnreadCount();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (unreadCount <= 0) {
                            mToolbar.showMsgcenterUnreadCount(false, 0);
                        } else {
                            mToolbar.showMsgcenterUnreadCount(true, unreadCount);
                        }
                    }
                });
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void judgeShowGradeTip() {
        AppMasterPreference pref = AppMasterPreference.getInstance(this);
        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.RunningTaskInfo topTaskInfo = mActivityManager.getRunningTasks(1).get(0);
        String pkg = getPackageName();
        if (pkg.equals(topTaskInfo.baseActivity.getPackageName())) {
            long count = pref.getUnlockCount();
            boolean haveTip = pref.getGoogleTipShowed();
            if (count >= 25 && !haveTip) {
                        /* google play 评分提示 */
                SDKWrapper.addEvent(this, SDKWrapper.P1, "home", "home_dlg_rank");
                Intent intent = new Intent(this, GradeTipActivity.class);
                startActivity(intent);
            }
        }
    }

    private void checkIswipeNotificationTo() {
        String fromPrivacyFlag = getIntent()
                .getStringExtra(ISwipUpdateRequestManager.ISWIP_NOTIFICATION_TO_PG_HOME);
        LeoLog.i(TAG, "来自iswipe：" + fromPrivacyFlag);
        if (ISwipUpdateRequestManager.ISWIP_NOTIFICATION_TO_PG_HOME
                .equals(fromPrivacyFlag)) {
            ISwipUpdateRequestManager.getInstance(getApplicationContext());
            mShowIswipeFromNotfi = true;
        }
    }

//    public void setAdIconVisible() {
//        if (mAdIcon != null) {
//            mAdIcon.setVisibility(View.VISIBLE);
//            if (Build.VERSION.SDK_INT > 15) {
//                mAdIcon.setBackgroundResource(R.drawable.adanimationfromhome);
//                AnimationDrawable adAnimation = (AnimationDrawable) mAdIcon.getBackground();
//                adAnimation.start();
//            }
//        }
//    }
//
//    public void setAdIconInVisible() {
//        if (mAdIcon != null) {
//            mAdIcon.setVisibility(View.INVISIBLE);
//        }
//    }

    private List<MenuItem> getMenuItems() {
        List<MenuItem> listItems = new ArrayList<MenuItem>();
        Resources resources = AppMasterApplication.getInstance().getResources();
        /* 亲给个好评 */
        listItems.add(new MenuItem(resources.getString(R.string.grade),
                R.drawable.menu_star_icon_menu, false));
        /* 点个赞 */
        listItems.add(new MenuItem(resources.getString(R.string.about_praise),
                R.drawable.menu_hot_icon, false));
        /* 加入粉丝团 */
        listItems.add(new MenuItem(resources.getString(R.string.about_group),
                R.drawable.menu_join_icon, false));
        /* 吐个槽 */
        listItems.add(new MenuItem(resources.getString(R.string.feedback),
                R.drawable.menu_feedbacks_icon, false));
        /* 检查升级 */
        if (SDKWrapper.isUpdateAvailable()) {
            listItems.add(new MenuItem(resources.getString(R.string.app_setting_update),
                    R.drawable.menu_updates_icon, true));
        } else {
            listItems.add(new MenuItem(resources.getString(R.string.app_setting_update),
                    R.drawable.menu_updates_icon, false));
        }
        /* 关于 */
        listItems.add(new MenuItem(resources.getString(R.string.app_setting_about),
                R.drawable.menu_about_icon, false));
        /* 卸载PG */
        boolean isAdmin = isAdminActive();
        if (isAdmin) {
            // listItems.add(object)
            listItems.add(new MenuItem(resources.getString(R.string.menue_item_delete_pg),
                    R.drawable.menu_delete, false));
            LeoLog.i("pg_delete_menue", "显示卸载按钮");
        } else {
            LeoLog.i("pg_delete_menue", "不显示卸载按钮");
        }
        return listItems;
    }

    /* 查看是否开启设备管理器成功 */
    private boolean isAdminActive() {
        DevicePolicyManager manager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        ComponentName mAdminName = new ComponentName(this, DeviceReceiver.class);
        if (manager.isAdminActive(mAdminName)) {
            return true;
        } else {
            return false;
        }
    }

    private void shortcutAndRoot() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefernece = PreferenceManager
                        .getDefaultSharedPreferences(HomeActivity.this);
                boolean installed = prefernece.getBoolean("shortcut", false);
                SharedPreferences.Editor editor = prefernece.edit();
                if (!installed) {
                    String channel = getString(R.string.channel_code);
                    if (PhoneInfo.getAndroidVersion() < 21 || !"0001a".equals(channel)) {
                        Intent shortcutIntent = new Intent(HomeActivity.this, SplashActivity.class);
                        shortcutIntent.setAction(Intent.ACTION_MAIN);
                        shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

                        Intent shortcut = new Intent(
                                "com.android.launcher.action.INSTALL_SHORTCUT");
                        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME,
                                getString(R.string.app_name));
                        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
                        Intent.ShortcutIconResource iconRes = Intent.ShortcutIconResource
                                .fromContext(HomeActivity.this, R.drawable.ic_launcher);
                        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
                        shortcut.putExtra("duplicate", false);
                        shortcut.putExtra("from_shortcut", true);
                        sendBroadcast(shortcut);
                    }
                    editor.putBoolean("shortcut", true);
                }

                boolean isInstall = AppMasterPreference.getInstance(HomeActivity.this).getAdDeskIcon();
                if (!isInstall) {
                    Intent appWallShortIntent = new Intent(AppMasterApplication.getInstance(), DeskProxyActivity.class);
                    appWallShortIntent.putExtra("from_quickhelper", true);
                    appWallShortIntent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, DeskProxyActivity.mAd);
                    appWallShortIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    Intent appWallShortcut = new Intent(
                            "com.android.launcher.action.INSTALL_SHORTCUT");
                    appWallShortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME,
                            getString(R.string.desk_ad_name));
                    Intent.ShortcutIconResource appwallIconRes = Intent.ShortcutIconResource.fromContext(
                            HomeActivity.this, R.drawable.ad_desktop_icon);
                    appWallShortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, appwallIconRes);
                    appWallShortcut.putExtra("duplicate", false);
                    appWallShortcut.putExtra("from_shortcut", true);
                    appWallShortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, appWallShortIntent);
                    sendBroadcast(appWallShortcut);
                    AppMasterPreference.getInstance(HomeActivity.this).setAdDeskIcon(true);
                }

                if (prefernece.getBoolean(PrefConst.KEY_ROOT_CHECK, true)) {
                    boolean root = RootChecker.isRoot();
                    if (root) {
                        SDKWrapper.addEvent(getApplicationContext(), SDKWrapper.P1, PrefConst.KEY_ROOT_CHECK, "root");
                    }
                    editor.putBoolean(PrefConst.KEY_ROOT_CHECK, false);
                }
                editor.commit();
            }

        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_ad_icon:
//                AppMasterPreference.getInstance(this).setAdClickTimeFromHome(
//                        System.currentTimeMillis());
//                LockManager.getInstatnce().timeFilterSelf();
                // app wall at home is abandon
//                SDKWrapper.addEvent(this, SDKWrapper.P1, "ad_cli", "adv_cnts_homeAppWall");
//                Intent mWallIntent = mWallAd.getWallIntent();
//                try {
//                    startActivity(mWallIntent);
//                } catch (Exception e) {
//                }
                break;
            case R.id.ct_back_rl:
                onExitScanning();
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(final AdapterView<?> arg0, final View arg1, final int position, final long arg3) {
        if (mDrawerLayout.isDrawerVisible(Gravity.START)) {
            mDrawerLayout.closeDrawer(Gravity.START);
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = null;
                if (position == 2) {
                    /* 加入粉丝团 */

                    /* sdk mark */
                    SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "menu",
                            "google+");
                    Intent intentBeta = null;
                    mLockManager.filterSelfOneMinites();
                    if (AppUtil.appInstalled(getApplicationContext(),
                            "com.google.android.apps.plus")) {
                        intentBeta = new Intent(Intent.ACTION_VIEW);
                        Uri uri = Uri
                                .parse("https://plus.google.com/u/0/communities/112552044334117834440");
                        intentBeta.setData(uri);
                        ComponentName cn = new ComponentName(
                                "com.google.android.apps.plus",
                                "com.google.android.libraries.social.gateway.GatewayActivity");
                        intentBeta.setComponent(cn);
                        intentBeta.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            startActivity(intentBeta);
                        } catch (Exception e) {
                            intentBeta = new Intent(Intent.ACTION_VIEW, uri);
                            ComponentName componentName = new ComponentName(
                                    "com.google.android.apps.plus",
                                    "com.google.android.apps.plus.phone.UrlGatewayActivity");
                            intentBeta.setComponent(componentName);
                            intentBeta.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            try {
                                startActivity(intentBeta);
                            } catch (Exception e1) {
                                intentBeta = new Intent(Intent.ACTION_VIEW, uri);
                                try {
                                    startActivity(intentBeta);
                                } catch (Exception e2) {
                                }
                            }
                        }
                    } else {
                        Uri uri = Uri
                                .parse("https://plus.google.com/u/0/communities/112552044334117834440");
                        intentBeta = new Intent(Intent.ACTION_VIEW, uri);
                        try {
                            startActivity(intentBeta);
                        } catch (Exception e) {
                        }
                    }
                    SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "about", "like");

                } else if (position == 1) {
                    /* Facebook */
                    SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "menu", "Facebook");
                    Intent intentLikeUs = null;
                    mLockManager.filterSelfOneMinites();
                    if (AppUtil.appInstalled(getApplicationContext(), "com.facebook.katana")) {
                        intentLikeUs = new Intent(Intent.ACTION_VIEW);
                        Uri uri = Uri.parse("fb://page/1709302419294051");
                        intentLikeUs.setData(uri);
                        ComponentName cn = new ComponentName("com.facebook.katana",
                                "com.facebook.katana.IntentUriHandler");
                        intentLikeUs.setComponent(cn);
                        intentLikeUs.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            startActivity(intentLikeUs);
                        } catch (Exception e) {
                        }
                    } else {
                        intentLikeUs = new Intent(Intent.ACTION_VIEW);
                        Uri uri = Uri
                                .parse("https://www.facebook.com/pages/App-Master/1709302419294051");
                        intentLikeUs.setData(uri);
                        intentLikeUs.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            startActivity(intentLikeUs);
                        } catch (Exception e) {
                        }
                    }
                    if (intentLikeUs != null) {
                        LeoLog.i(TAG, "facebook, url: " + intentLikeUs.toURI());
                    }
                } else if (position == 0) {
                    /* google play */
                    /* sdk mark */
                    SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "menu",
                            "googleplay");
                    mLockManager.filterSelfOneMinites();
                    Utilities.goFiveStar(HomeActivity.this, false, false);
                } else if (position == 3) {
                    /* sdk mark */
                    SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "menu",
                            "feedback");
                    intent = new Intent(HomeActivity.this,
                            FeedbackActivity.class);
                    startActivity(intent);
                } else if (position == 6) {
                    /* 卸载 */
                    SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "menu", "uninstall");
                    unistallPG();
                } else if (position == 4) {
                    /* 检查更新 */

                    /* sdk mark */
                    SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "menu",
                            "update");
                    SDKWrapper.checkUpdate();
                } else if (position == 5) {
                    /* 关于 */
                    /* sdk mark */
                    SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "menu", "about");
                    intent = new Intent(HomeActivity.this,
                            AboutActivity.class);
                    startActivity(intent);
                }
            }
        }, 200);
    }

    /* 卸载 PG */
    private boolean unistallPG() {
        if (mDrawerLayout.isDrawerVisible(Gravity.START)) {
            mDrawerLayout.closeDrawer(Gravity.START);
        }
        try {
            Uri uri = Uri.fromParts("package", this.getPackageName(), null);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_DELETE);
            intent.setData(uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // 从卸载入口过去，10秒内不对设置加锁
            mLockManager.filterPackage("com.android.settings", 10 * 1000);
            mLockManager.filterSelfOneMinites();
            startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void onListScroll(int scrollHeight) {
        int stickyMaxScrollHeight = mHeaderHeight - mToolbarHeight;
        if (scrollHeight > stickyMaxScrollHeight) {
            mToolbar.setBackgroundColor(mPrivacyFragment.getToolbarColor());
            mCommonToolbar.setBackgroundColor(mPrivacyFragment.getToolbarColor());
        } else {
            mToolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
            mCommonToolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
        }
    }

    public void onProcessClick(Fragment fragment) {
        mScoreBeforeProcess = mPrivacyHelper.getSecurityScore();
        // 分数上涨，标题栏背景异常，非变现，加保护
        mToolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
        mCommonToolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
        mProcessedMgr = null;
        mProcessAlreadyTimeout = false;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.remove(fragment);
        IoUtils.commitSafely(ft);
//        getSupportFragmentManager().beginTransaction().remove(fragment).commit();

        PreferenceTable preferenceTable = PreferenceTable.getInstance();
        if (fragment instanceof PrivacyNewAppFragment) {
            mPrivacyFragment.showProcessProgress(PrivacyHelper.PRIVACY_APP_LOCK);
            boolean appConsumed = preferenceTable.getBoolean(PrefConst.KEY_APP_COMSUMED, false);
            boolean appLockHandler = preferenceTable.getBoolean(PrefConst.KEY_APP_LOCK_HANDLER, false);
            if (!appConsumed) {
                preferenceTable.putBoolean(PrefConst.KEY_APP_COMSUMED, true);
            }
            if (!appLockHandler) {
                preferenceTable.putBoolean(PrefConst.KEY_APP_LOCK_HANDLER, true);
            }
        } else if ((fragment instanceof PrivacyNewPicFragment)
                || (fragment instanceof FolderPicFragment)) {
            mPrivacyFragment.showProcessProgress(PrivacyHelper.PRIVACY_HIDE_PIC);
            boolean picConsumed = preferenceTable.getBoolean(PrefConst.KEY_PIC_COMSUMED, false);
            if (!picConsumed) {
                preferenceTable.putBoolean(PrefConst.KEY_PIC_COMSUMED, true);
                preferenceTable.putBoolean(PrefConst.KEY_PIC_REDDOT_EXIST, true);
                mMoreFragment.updateHideRedTip();
            }
        } else if ((fragment instanceof PrivacyNewVideoFragment)
                || (fragment instanceof FolderVidFragment)) {
            mPrivacyFragment.showProcessProgress(PrivacyHelper.PRIVACY_HIDE_VID);
            boolean vidConsumed = preferenceTable.getBoolean(PrefConst.KEY_VID_COMSUMED, false);
            if (!vidConsumed) {
                preferenceTable.putBoolean(PrefConst.KEY_VID_COMSUMED, true);
                preferenceTable.putBoolean(PrefConst.KEY_VID_REDDOT_EXIST, true);
                mMoreFragment.updateHideRedTip();
            }
        }
        ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mProcessAlreadyTimeout = true;
                if (mProcessedMgr != null) {
                    startProcessFinishAnim(mProcessedScore);
                }
            }
        }, 2000);
    }

    public void onProcessFinish(int increaseScore, String mgr) {
        LeoLog.d(TAG, "onProcessFinish, mgr: " + mgr + " | increaseScore: " + increaseScore);
        int scoreAfterProcess = mPrivacyHelper.getSecurityScore();
        if (scoreAfterProcess != mScoreBeforeProcess) {
            // 处理前后分数不一样了，分数不再继续累加
            increaseScore = scoreAfterProcess - mScoreBeforeProcess;
            increaseScore = increaseScore < 0 ? 0 : increaseScore;
        }
        LeoLog.d(TAG, "onProcessFinish, increaseScore again: " + increaseScore);

        IntrudeSecurityManager ism = (IntrudeSecurityManager) MgrContext.getManager(
                MgrContext.MGR_INTRUDE_SECURITY);
        boolean intruderAdded = PreferenceTable.getInstance().getBoolean(
                PrefConst.KEY_INTRUDER_ADDED, false);
        int intruderScore = 0;
        if (!ism.getIntruderMode() && !ism.getIsIntruderSecurityAvailable() && !intruderAdded) {
            // 1.入侵者未开启   2.入侵者不可用   3.入侵者的分数还未增加
            intruderScore = ism.getMaxScore();
            mPrivacyHelper.increaseScore(MgrContext.MGR_INTRUDE_SECURITY, intruderScore);
            PreferenceTable.getInstance().putBoolean(PrefConst.KEY_INTRUDER_ADDED, true);
        }

        if (increaseScore > 0) {
            mPrivacyHelper.increaseScore(mgr, increaseScore);
        }
        mProcessedMgr = mgr;
        increaseScore += intruderScore;
        mProcessedScore = increaseScore;
        LeoLog.d(TAG, "onProcessFinish, increaseScore again: " + increaseScore +
                " | mProcessTimeout:" + mProcessAlreadyTimeout);
        if (mProcessAlreadyTimeout) {
            startProcessFinishAnim(increaseScore);
        }
    }

    /**
     * 开始处理完之后的动画
     *
     * @param increaseScore
     */
    private void startProcessFinishAnim(int increaseScore) {
        if (mTabFragment.isTabDismiss() && mScanningFragment == null) {
            // 1、tab消失  2、不处于扫描状态
            mPrivacyFragment.startLoadingRiseAnim(increaseScore);
            LeoLog.d(TAG, "startProcessFinishAnim, startLoadingRiseAnim..." + increaseScore);
        } else {
            mPrivacyFragment.startIncreaseSocreAnim(increaseScore);
            LeoLog.d(TAG, "startProcessFinishAnim, startIncreaseSocreAnim..." + increaseScore);
        }
    }

    public boolean isTabDismiss() {
        return mTabFragment.isTabDismiss();
    }

    public void onIgnoreClick(int increaseScore, String mgr) {
        jumpToNextFragment();
        mPrivacyFragment.increaseStepAnim();
    }

    public void resetToolbarColor() {
        mToolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
        mCommonToolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
    }

    class MenuAdapter extends BaseAdapter {

        List<MenuItem> items;
        LayoutInflater inflater;

        public MenuAdapter(Context ctx, List<MenuItem> items) {
            super();
            this.items = items;
            inflater = LayoutInflater.from(ctx);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int arg0) {
            return items.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        /**
         * need not ViewHolder here
         */
        @SuppressLint("ViewHolder")
        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {
//            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.home_menu_item, arg2, false);
            MaterialRippleLayout layout = (MaterialRippleLayout) inflater.inflate(R.layout.home_menu_item, arg2, false);
            TextView tv = (TextView) layout.findViewById(R.id.menu_item_tv);
                tv.setTextColor(mMenuTextColorId);
            ImageView redTip = (ImageView) layout.findViewById(R.id.update_red_tip);
            /* some item not HTML styled text, such as "check update" item */
            tv.setText(Html.fromHtml(items.get(arg0).itemName));
            if (items.get(arg0).isRedTip) {
                redTip.setVisibility(View.VISIBLE);
            } else {
                redTip.setVisibility(View.GONE);
            }
            /**
             * 类似于阿拉伯语等从右往左显示的处理
             */
            if (LanguageUtils.isRightToLeftLanguage(null)) {
                // Log.e(Constants.RUN_TAG, "阿拉伯语");
                tv.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources()
                        .getDrawable(items.get(arg0).iconId), null);
            } else {
                tv.setCompoundDrawablesWithIntrinsicBounds(
                        getResources().getDrawable(items.get(arg0).iconId), null, null, null);
            }

//            if (layout instanceof LinearRippleView) {
//                ((LinearRippleView) layout).setOnRippleCompleteListener(new LinearRippleView.OnRippleCompleteListener() {
//                    @Override
//                    public void onRippleComplete(LinearRippleView rippleView) {
//
//                    }
//                });

//            }
            return layout;
        }

    }

    class MenuItem {
        String itemName;
        int iconId;
        boolean isRedTip;

        public MenuItem(String itemName, int iconId, boolean isRedTip) {
            super();
            this.itemName = itemName;
            this.iconId = iconId;
            this.isRedTip = isRedTip;
        }
    }

    /*高级保护开启首页提示*/
    private void openAdvanceProtectDialogHandler() {
        String key = PrefConst.KEY_OPEN_ADVA_PROTECT;
        boolean isTip = PreferenceTable.getInstance().getBoolean(key, true);

        if (isAdminActive() && isTip) {
            /**
             * Samsung 5.1.1 sys 电池优化权限提示
             */
            boolean samSungTip = AutoStartGuideList.samSungSysTip(getApplicationContext(), PrefConst.KEY_HOME_SAMSUNG_TIP);
            if (!samSungTip) {
                openAdvanceProtectDialogTip();
                SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "home", "home_dlg_uninstall");
            }
        } else {
            /**
             * Samsung 5.1.1 sys 电池优化权限提示
             */
            AutoStartGuideList.samSungSysTip(getApplicationContext(), PrefConst.KEY_HOME_SAMSUNG_TIP);
        }
    }

    private void openAdvanceProtectDialogTip() {
        if (mMessageDialog == null) {
            mMessageDialog = new LEOAnimationDialog(this);
            mMessageDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (mMessageDialog != null) {
                        mMessageDialog = null;
                    }
                    AppMasterPreference.getInstance(HomeActivity.this)
                            .setAdvanceProtectOpenSuccessDialogTip(false);
                    String key = PrefConst.KEY_OPEN_ADVA_PROTECT;
                    PreferenceTable.getInstance().putBoolean(key, false);
                }
            });
        }
        String content = getString(R.string.prot_open_suc_tip_cnt);
        mMessageDialog.setContent(content);
        mMessageDialog.show();
//        if (mMessageDialog == null) {
//            mMessageDialog = new LEOMessageDialog(this);
//            mMessageDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                @Override
//                public void onDismiss(DialogInterface dialog) {
//                    if (mMessageDialog != null) {
//                        mMessageDialog = null;
//                    }
//                    String key = PrefConst.KEY_OPEN_ADVA_PROTECT;
//                    PreferenceTable.getInstance().putBoolean(key, false);
//                }
//            });
//        }
//        String title = getString(R.string.advance_protect_open_success_tip_title);
//        String content = getString(R.string.adv_prot_open_suc_tip_cnt);
//        mMessageDialog.setTitle(title);
//        mMessageDialog.setContent(content);
//        mMessageDialog.show();
    }

    /*首页引导*/
    private void showHomeGuide() {
        PreferenceTable preferenceTable = PreferenceTable.getInstance();
        boolean pulledEver = preferenceTable.getBoolean(PrefConst.KEY_MORE_PULLED, false);
        boolean picReddot = preferenceTable.getBoolean(PrefConst.KEY_PIC_REDDOT_EXIST, false);
        boolean vidReddot = preferenceTable.getBoolean(PrefConst.KEY_VID_REDDOT_EXIST, false);
        boolean homeGuide = preferenceTable.getBoolean(PrefConst.KEY_HOME_GUIDE, false);
        if (!pulledEver && (picReddot || vidReddot) && !homeGuide) {
            if (mMoreFragment != null) {
                mMoreFragment.cancelUpArrowAnim();
            }
            mGuideFragment.setEnable(true, GuideFragment.GUIDE_TYPE.HOME_MORE_GUIDE);
            preferenceTable.putBoolean(PrefConst.KEY_HOME_GUIDE, true);
            GuideFragment.setHomeGuideShowStatus(true);
        }
    }
}
