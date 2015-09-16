
package com.leo.appmaster.home;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.admin.DevicePolicyManager;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WifiConfiguration.Visibility;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.LockOptionActivity;
import com.leo.appmaster.applocker.LockSettingActivity;
import com.leo.appmaster.applocker.PasswdProtectActivity;
import com.leo.appmaster.applocker.PasswdTipActivity;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.manager.MobvistaEngine;
import com.leo.appmaster.applocker.model.ProcessDetectorCompat22;
import com.leo.appmaster.applocker.receiver.DeviceReceiver;
import com.leo.appmaster.appmanage.view.HomeAppManagerFragment;
import com.leo.appmaster.appsetting.AboutActivity;
import com.leo.appmaster.appwall.AppWallActivity;
import com.leo.appmaster.db.MsgCenterTable;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.BackupEvent;
import com.leo.appmaster.eventbus.event.MsgCenterEvent;
import com.leo.appmaster.feedback.FeedbackActivity;
import com.leo.appmaster.feedback.FeedbackHelper;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.fragment.HomeLockFragment;
import com.leo.appmaster.fragment.HomePravicyFragment;
import com.leo.appmaster.fragment.Selectable;
import com.leo.appmaster.home.HomeShadeView.OnShaderColorChangedLisetner;
import com.leo.appmaster.msgcenter.MsgCenterActivity;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.quickgestures.ISwipUpdateRequestManager;
import com.leo.appmaster.quickgestures.ui.IswipUpdateTipDialog;
import com.leo.appmaster.quickgestures.ui.QuickGestureActivity;
import com.leo.appmaster.quickgestures.ui.QuickGestureTipDialog;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.DrawerArrowDrawable;
import com.leo.appmaster.ui.IconPagerAdapter;
import com.leo.appmaster.ui.LeoHomePopMenu;
import com.leo.appmaster.ui.LeoPagerTab;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOSelfIconAlarmDialog;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.LanguageUtils;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.RootChecker;
import com.leo.appmaster.utils.Utilities;
import com.mobvista.sdk.m.core.MobvistaAdWall;

@SuppressLint("ResourceAsColor")
public class HomeActivity extends BaseFragmentActivity implements OnClickListener,
        OnItemClickListener,
        OnPageChangeListener, OnShaderColorChangedLisetner {

    private final static String KEY_ROOT_CHECK = "root_check";
    public final static long INTERVEL_CLICK_AD = 24 * 60 * 60 * 1000;
    public static final String ROTATE_FRAGMENT = "rotate_fragment";
    /* 数组中为不显示开启高级保护的渠道 */
    public static final String[] FILTER_CHANNEL = {
            "0001a"
    };

    // 释放系统预加载资源使用
    private static LongSparseArray<Drawable.ConstantState>[] sPreloadedDrawables =
            new LongSparseArray[2];

    private ViewStub mViewStub;
    private MultiModeView mMultiModeView;
    private DrawerLayout mDrawerLayout;
    private ImageView mLeftMenu;
    private ListView mMenuList;
    private HomeTitleBar mTtileBar;
    private LeoPagerTab mPagerTab;
    private ViewPager mViewPager;
    private View mBgStatusbar, mFgStatusbar;
    private HomeShadeView mShadeView;
    private LeoHomePopMenu mLeoPopMenu;
    private LEOAlarmDialog mAlarmDialog;
    private LEOSelfIconAlarmDialog mSelfIconDialog;
    private QuickGestureTipDialog mQuickGestureSettingDialog;
    private QuickGestureTipDialog mQuickGestureTip;
    private float mDrawerOffset;
    private Handler mHandler = new Handler();
    private DrawerArrowDrawable mDrawerArrowDrawable;
    private HomeFragmentHoler[] mFragmentHolders = new HomeFragmentHoler[3];
    // private ImageView app_hot_tip_icon;
    private int type;
    private int REQUEST_IS_FROM_APP_LOCK_LIST = 1;
    private boolean mIsFromAppLockList = false;
    private MobvistaAdWall mWallAd;
    private IswipUpdateTipDialog mIswipDialog;
    private boolean mShowIswipeFromNotfi;
    private static final String TAG = "HomeActivity";
    private static final boolean DBG = true;
    private ImageView mLeftMenuRedTip;
    private AutoStartTipDialog mAutoStartGuideDialog;
    private AnimationDrawable adAnimation;
    private ImageView mAdIcon;
    private boolean isEnterPrivacySuggest = false;
    private MenuAdapter mMenuAdapter;
    private TextView mUnreadCountTv;
    private List<MenuItem> mMenuItems;
    private AdvanceProtectTipDialog mAdvanceProtectDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // lockType , num or guesture
        initUI();
        tryTransStatusbar();
        // installShortcut();

        // TODO
        mobvistaCheck();

        FeedbackHelper.getInstance().tryCommit();
        shortcutAndRoot();
        showQuickGestureContinue();
        recordEnterHomeTimes();
        SDKWrapper.addEvent(this, SDKWrapper.P1, "home", "enter");
        LeoEventBus.getDefaultBus().register(this);
        // AM-2128 偶现图片显示异常，先暂时注释掉
        // releaseSysResources();
        /* 获取是否从iswipe通知进入 */
        checkIswipeNotificationTo();
        /* 白名单引导 */
        autoStartDialogHandler();
    }

    private void checkIswipeNotificationTo() {
        String fromPrivacyFlag = getIntent()
                .getStringExtra(ISwipUpdateRequestManager.ISWIP_NOTIFICATION_TO_PG_HOME);
        if (fromPrivacyFlag != null && ISwipUpdateRequestManager.ISWIP_NOTIFICATION_TO_PG_HOME
                .equals(ISwipUpdateRequestManager.ISWIP_NOTIFICATION_TO_PG_HOME)) {
            ISwipUpdateRequestManager.getInstance(getApplicationContext());
            mShowIswipeFromNotfi = true;
            mPagerTab.setCurrentItem(2);
        }
    }

    /**
     * 释放系统预加载资源，完全无用，占用内存约10M
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void releaseSysResources() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            sPreloadedDrawables[0] = new LongSparseArray<Drawable.ConstantState>();
            sPreloadedDrawables[1] = new LongSparseArray<Drawable.ConstantState>();

            Resources res = getResources();

            try {
                Field field = res.getClass().getDeclaredField("sPreloadedDrawables");
                field.setAccessible(true);
                field.set(res, sPreloadedDrawables);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    // private void tryIsFromLockMore() {
    // // TODO Auto-generated method stub
    // Intent intent = getIntent();
    //
    // mIsFromAppLockList= intent.getBooleanExtra("isFromAppLockList", false);
    // Log.e("lockmore",
    // "isfromlist"+intent.getBooleanExtra("isFromAppLockList", false));
    // }

    // 伪装的引导，当第一次将应用加了所后返回home，弹出提示。
    private void showWeiZhuangTip() {

        // Log.e("isshow", "isfromlist"+mIsFromAppLockList);
        Log.e("isshow", "isneed" + AppMasterPreference.getInstance(this).getIsNeedPretendTips()
                + "");
        Log.e("isshow", "lockedcount" + LockManager.getInstatnce().getLockedAppCount() + "");
        Log.e("isshow", "getpretendtype" + AppMasterPreference.getInstance(this).getPretendLock()
                + "");
        Log.e("isshow", "getisfromAppList"
                + AppMasterPreference.getInstance(this).getIsFromLockList() + "");
        if (AppMasterPreference.getInstance(this).getIsNeedPretendTips()
                && LockManager.getInstatnce().getLockedAppCount() > 0
                && AppMasterPreference.getInstance(this).getPretendLock() == 0 &&
                AppMasterPreference.getInstance(this).getIsFromLockList() == true)
        {
            if (showAdvanceProtectDialog()) {
                if (mSelfIconDialog == null)
                {
                    mSelfIconDialog = new LEOSelfIconAlarmDialog(this);
                    mSelfIconDialog.setIcon(R.drawable.pretend_guide);
                    mSelfIconDialog
                            .setOnClickListener(new LEOSelfIconAlarmDialog.OnDiaogClickListener() {

                                @Override
                                public void onClick(int which) {
                                    if (which == 1)
                                    {
                                        SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1,
                                                "coverguide", "cli_y");

                                        mSelfIconDialog.dismiss();
                                        if (mFragmentHolders[0].fragment != null)
                                        {

                                            HomeLockFragment fragment = (HomeLockFragment) mFragmentHolders[0].fragment;

                                            mPagerTab.setCurrentItem(0);
                                            fragment.playPretendEnterAnim();
                                        }
                                    }
                                    else if (which == 0)
                                    {
                                        SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1,
                                                "coverguide", "cli_n");
                                    }
                                    dismissDialog(mSelfIconDialog);
                                }
                            });
                    //
                }
                mSelfIconDialog.setSureButtonText(getString(R.string.button_disguise_guide_select));
                mSelfIconDialog.setLeftBtnStr(getString(R.string.button_disguise_guide_cancel));
                mSelfIconDialog.setContent(getString(R.string.button_disguise_guide_content));// poha
                                                                                              // to
                                                                                              // du
                mSelfIconDialog.setCanceledOnTouchOutside(false);
                mSelfIconDialog.show();
                AppMasterPreference.getInstance(this).setIsNeedPretendTips(false);
            }
        } else {
            AppMasterPreference.getInstance(this).setIsHomeToLockList(false);
            AppMasterPreference.getInstance(this).setIsFromLockList(false);
            AppMasterPreference.getInstance(this).setIsClockToLockList(false);
        }
    }

    // @Override
    // protected void onActivityResult(int requestCode, int resultCode, Intent
    // data) {
    // // TODO Auto-generated method stub
    // Log.e("poha","resultCode"+resultCode);
    // Log.e("poha","reqCode"+requestCode);
    // if(resultCode==RESULT_OK)
    // {
    // Log.e("poha","in if");
    // // switch (requestCode) {
    // //
    // // case 0:
    // mIsFromAppLockList = data.getBooleanExtra("isFromAppLockList", false);
    //
    //
    //
    //
    //
    //
    // Log.e("poha","data.getBooleanExtra(isFromAppLockList, false);======"
    // +data.getBooleanExtra("isFromAppLockList", false));
    // // break;
    // //
    // // default:
    // // break;
    // // }
    // }

    // }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LeoEventBus.getDefaultBus().unregister(this);
        if (mWallAd != null) {
            mWallAd.release();
            mWallAd = null;
        }
        if (mAdvanceProtectDialog != null) {
            mAdvanceProtectDialog.dismiss();
            mAdvanceProtectDialog = null;
        }
        if (mQuickGestureTip != null) {
            mQuickGestureTip.dismiss();
            mQuickGestureTip = null;
        }
        if (mIswipDialog != null) {
            mIswipDialog.dismiss();
            mIswipDialog = null;
        }
    }

    @Override
    protected void onPause() {
        removeAppFragmentGestureBg();
        super.onPause();
    }

    public void onEventMainThread(BackupEvent event) {
        String msg = event.eventMsg;
        if (HomeAppManagerFragment.FINISH_HOME_ACTIVITY_FALG.equals(msg)) {
            this.finish();
        } else if (HomeAppManagerFragment.ISWIPE_CANCEL_RED_TIP.equals(msg)) {
            if (mPagerTab != null) {
                mFragmentHolders[2].isRedTip = false;
                mPagerTab.notifyDataSetChanged();
            }
        }
    }

    public void onEvent(MsgCenterEvent event) {
        // 设置消息中心未读计数
        setMsgCenterUnread();
        LeoLog.i(TAG, "onEvent, event: " + event);
    }

    private void initUI() {
        mViewStub = (ViewStub) findViewById(R.id.viewstub);
        mPagerTab = (LeoPagerTab) findViewById(R.id.tab_indicator);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        initFragment();
        mViewPager.setAdapter(new HomePagerAdapter(getSupportFragmentManager()));
        mViewPager.setOffscreenPageLimit(2);
        mPagerTab.setViewPager(mViewPager);
        mPagerTab.setOnPageChangeListener(this);
        mPagerTab.setCurrentItem(0);

        mDrawerArrowDrawable = new DrawerArrowDrawable(getResources());
        mDrawerArrowDrawable.setStrokeColor(getResources()
                .getColor(R.color.white));
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                mDrawerOffset = slideOffset;
                mDrawerArrowDrawable.setParameter(mDrawerOffset);
            }
        });
        mMenuList = (ListView) findViewById(R.id.menu_list);
        mMenuItems = getMenuItems();
        mMenuAdapter = new MenuAdapter(this, mMenuItems);
        mMenuList.setAdapter(mMenuAdapter);
        mMenuList.setOnItemClickListener(this);

        mTtileBar = (HomeTitleBar) findViewById(R.id.layout_title_bar);
        mLeftMenu = (ImageView) findViewById(R.id.iv_menu);
        mLeftMenuRedTip = (ImageView) findViewById(R.id.iv_menu_red_tip);
        mLeftMenu.setOnClickListener(this);
        mLeftMenu.setImageDrawable(mDrawerArrowDrawable);
        mTtileBar.setOptionClickListener(this);
        mTtileBar.setHotAppClickListener(this);
        mBgStatusbar = findViewById(R.id.bg_statusbar);
        mFgStatusbar = findViewById(R.id.fg_statusbar);
        mShadeView = (HomeShadeView) findViewById(R.id.shadeview);
        mShadeView.setPosition(0);
        mShadeView.setColorChangedListener(this);

        mAdIcon = (ImageView) findViewById(R.id.iv_ad_icon);
        mAdIcon.setOnClickListener(this);
        // app_hot_tip_icon = (ImageView)
        // mTtileBar.findViewById(R.id.app_hot_tip_icon_);
        // if (AppMasterPreference.getInstance(this).getHomeFragmentRedTip()) {
        // app_hot_tip_icon.setVisibility(View.VISIBLE);
        // } else {
        // app_hot_tip_icon.setVisibility(View.GONE);
        // }

        mUnreadCountTv = (TextView) findViewById(R.id.home_mc_unread_tv);
    }

    // public void setAdIconVisible() {
    // if (mAdIcon != null) {
    // mAdIcon.setVisibility(View.VISIBLE);
    // }
    // }

    public void setEnterPrivacySuggest(boolean value) {
        isEnterPrivacySuggest = value;
    }

    public void setAdIconInVisible() {
        if (mAdIcon != null) {
            mAdIcon.setVisibility(View.INVISIBLE);
        }
    }

    public void showModePages(boolean show/* , int[] center */) {
        if (mMultiModeView == null) {
            mMultiModeView = (MultiModeView) mViewStub.inflate();
        }

        if (show) {
            mMultiModeView.show();
        } else {
            mMultiModeView.hide();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            int oldTab = savedInstanceState.getInt("current_tap");
            mViewPager.setCurrentItem(oldTab);
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public int getShaderColor() {
        return mShadeView.getCurColor();
    }

    private void initFragment() {
        HomeFragmentHoler holder = new HomeFragmentHoler();
        holder.title = this.getString(R.string.lock_tab);
        HomeLockFragment lockFragment = new HomeLockFragment();
        holder.fragment = lockFragment;
        holder.iconId = R.drawable.lock_active_icon;
        mFragmentHolders[0] = holder;

        holder = new HomeFragmentHoler();
        holder.title = this.getString(R.string.pravicy_protect);
        HomePravicyFragment pravicyFragment = new HomePravicyFragment();
        holder.fragment = pravicyFragment;
        holder.iconId = R.drawable.protction_active_icon;
        mFragmentHolders[1] = holder;

        holder = new HomeFragmentHoler();
        holder.title = this.getString(R.string.app_manager);
        HomeAppManagerFragment appManagerFragment = new HomeAppManagerFragment();
        holder.fragment = appManagerFragment;
        holder.iconId = R.drawable.my_apps_active_icon;
        if (AppMasterPreference.getInstance(this).getQuickGestureRedTip()) {
            holder.isRedTip = true;
        }
        mFragmentHolders[2] = holder;

        // remove cached fragments
        FragmentManager fm = getSupportFragmentManager();
        try {
            FragmentTransaction ft = fm.beginTransaction();
            List<Fragment> list = fm.getFragments();
            if (list != null) {
                for (Fragment f : fm.getFragments()) {
                    ft.remove(f);
                }
            }
            ft.commit();
        } catch (Exception e) {

        }

    }

    public int getCurrentPage() {
        if (mViewPager != null) {
            return mViewPager.getCurrentItem();
        } else {
            return 0;
        }
    }

    @Override
    protected void onResume() {

        // Ad from Home
        shouldShowAd();

        /* 分析是否需要升级红点显示 */
        boolean menuRedTipVisibility = (mLeftMenuRedTip.getVisibility() == View.GONE);
        if (mLeftMenuRedTip != null && menuRedTipVisibility) {
            if (SDKWrapper.isUpdateAvailable()) {
                mLeftMenuRedTip.setVisibility(View.VISIBLE);
            }
        }
        /* check if there is force update when showing HomeActivity */
        SDKWrapper.checkForceUpdate();
        type = AppMasterPreference.getInstance(this).getLockType();

        judgeShowGradeTip();

        // tryIsFromLockMore();
        showWeiZhuangTip();
        // compute privacy level here to avoid unknown change, such as file
        // deleted outside of your phone.
        AppMasterPreference amp = AppMasterPreference.getInstance(this);
        if (amp.getFromOther()) {
            PrivacyHelper.getInstance(this).computePrivacyLevel(PrivacyHelper.VARABLE_ALL);
            amp.setFromOther(false);
        }

        if (mViewPager != null) {
            int current = mViewPager.getCurrentItem();
            if (current < mFragmentHolders.length) {
                HomeFragmentHoler hfh = mFragmentHolders[current];
                if (hfh != null && hfh.fragment instanceof Selectable) {
                    ((Selectable) (hfh.fragment)).onSelected(current);
                }
            }
        }
        if (mFragmentHolders != null) {
            if (!AppMasterPreference.getInstance(this).getQuickGestureRedTip()) {
                mFragmentHolders[2].isRedTip = false;
                mPagerTab.notifyDataSetChanged();

            }
        }

        // if (AppMasterPreference.getInstance(this).getHomeFragmentRedTip()) {
        // app_hot_tip_icon.setVisibility(View.VISIBLE);
        // } else {
        // app_hot_tip_icon.setVisibility(View.GONE);
        // }
        /* ISwipe升级对话框提示 */
        showIswipDialog();
        super.onResume();
        SDKWrapper.addEvent(this, SDKWrapper.P1, "tdau", "home");

        ProcessDetectorCompat22.setForegroundScore();
        // 设置消息中心未读计数
        setMsgCenterUnread();
        /* 判断是否打开高级保护，显示“卸载”项 */
        addUninstallPgTOMenueItem();
    }

    private void addUninstallPgTOMenueItem() {
        LeoLog.i("pg_delete_menue", "是否开启了高级保护：" + isAdminActive());
        mMenuAdapter = null;
        mMenuItems = null;
        mMenuItems = getMenuItems();
        mMenuAdapter = new MenuAdapter(this, mMenuItems);
        if (mMenuList.getAdapter() != null) {
            mMenuList.setAdapter(mMenuAdapter);
        }
        mDrawerLayout.postInvalidate();
    }

    public void shouldShowAd() {
        long clickTime = AppMasterPreference.getInstance(this).getAdClickTimeFromHome();
        long nowTime = System.currentTimeMillis();
        if ((nowTime - clickTime > INTERVEL_CLICK_AD) && !isEnterPrivacySuggest) {
            mAdIcon.setVisibility(View.VISIBLE);
            mAdIcon.setBackgroundResource(R.drawable.adanimationfromhome);
            adAnimation = (AnimationDrawable)
                    mAdIcon.getBackground();
            adAnimation.start();
        } else {
            mAdIcon.setVisibility(View.INVISIBLE);
        }
    }

    private void setMsgCenterUnread() {
        ThreadManager.executeOnFileThread(new Runnable() {
            @Override
            public void run() {
                MsgCenterTable table = new MsgCenterTable();
                final int unreadCount = table.getUnreadCount();
                String unreadCountStr = unreadCount + "";
                if (unreadCount > 99) {
                    unreadCountStr = "99+";
                }

                final String finalUnreadCountStr = unreadCountStr;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (unreadCount <= 0) {
                            mUnreadCountTv.setVisibility(View.GONE);
                        } else {
                            mUnreadCountTv.setVisibility(View.VISIBLE);
                            mUnreadCountTv.setText(finalUnreadCountStr);
                        }
                    }
                });
            }
        });
    }

    private void showIswipDialog() {
        /* ISwipe升级对话框提示 */
        if (mShowIswipeFromNotfi) {
            mShowIswipeFromNotfi = false;
            boolean installIswipe = ISwipUpdateRequestManager.getInstance(this).isInstallIsiwpe();
            if (!installIswipe) {
                LeoLog.e(TAG, "直接显示的iswipe对话框,不用赋值");
                showDownLoadISwipDialog(this, null);
            }
        } else {
            showIswipeUpdateTip(this, "homeactivity");
            LeoLog.e(TAG, "需要值显示的iswipe对话框");
        }
    }

    private void saveIswipUpdateDate(int checkUpdate, int frequency, int number, String gpUrl,
            String browserUrl, int downType) {
        LeoLog.e(Constants.RUN_TAG, "初始化测试数据");
        AppMasterPreference preference = AppMasterPreference.getInstance(AppMasterApplication
                .getInstance());
        preference.setIswipUpdateFlag(checkUpdate);
        preference.setIswipUpdateFre(frequency);
        preference.setIswipUpdateNumber(number);
        if (!Utilities.isEmpty(gpUrl)) {
            preference.setIswipUpdateGpUrl(gpUrl);
        }
        if (!Utilities.isEmpty(browserUrl)) {
            preference.setIswipUpdateBrowserUrl(browserUrl);
        }
        preference.setIswipUpdateDownType(downType);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("current_tap", mViewPager.getCurrentItem());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (mQuickGestureSettingDialog != null) {
            AppMasterPreference.getInstance(HomeActivity.this).setQGSettingFirstDialogTip(
                    true);
        }
        if (mDrawerLayout.isDrawerOpen(mMenuList)) {
            mDrawerLayout.closeDrawer(mMenuList);
            return;
        }

        if (mMultiModeView != null && mMultiModeView.getVisibility() == View.VISIBLE) {
            showModePages(false/* , new int[]{1,1} */);
            return;
        }

        // Whether the child consumed the event
        HomeFragmentHoler holder = mFragmentHolders[mViewPager.getCurrentItem()];
        if (holder != null && holder.fragment != null && holder.fragment.onBackPressed()) {
            return;
        }

        finish();

        // ===== AMAM-1336 ========
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_menu:
                if (mDrawerLayout.isDrawerVisible(Gravity.START)) {
                    mDrawerLayout.closeDrawer(Gravity.START);
                } else {
                    mDrawerLayout.openDrawer(Gravity.START);
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "home", "menu");
                }
                break;
            case R.id.iv_option_image:
                SDKWrapper.addEvent(this, SDKWrapper.P1, "home", "password");
                if (mDrawerLayout.isDrawerVisible(Gravity.START)) {
                    mDrawerLayout.closeDrawer(Gravity.START);
                }

                if (mLeoPopMenu == null) {
                    mLeoPopMenu = new LeoHomePopMenu();
                }

                mLeoPopMenu.setAnimation(R.style.RightEnterAnim);
                mLeoPopMenu.setPopItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                        if (position == 0) {
                            SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "home",
                                    "changepwd");
                            Intent intent = new Intent(HomeActivity.this, LockSettingActivity.class);
                            intent.putExtra("reset_passwd", true);
                            startActivity(intent);
                        } else if (position == 1) {
                            /*
                             * SDKWrapper.addEvent(HomeActivity.this,
                             * SDKWrapper.P1, "home", "changepwd");
                             */
                            // Intent intent = new Intent(HomeActivity.this,
                            // LockChangeModeActivity.class);
                            Intent intent = new Intent(HomeActivity.this, LockSettingActivity.class);
                            intent.putExtra("reset_passwd", true);
                            intent.putExtra(ROTATE_FRAGMENT, true);
                            startActivity(intent);
                        } else if (position == 2) {
                            SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "home", "mibao");
                            Intent intent = new Intent(HomeActivity.this,
                                    PasswdProtectActivity.class);
                            startActivity(intent);
                        } else if (position == 3) {
                            SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "home",
                                    "passwdtip");
                            Intent intent = new Intent(HomeActivity.this, PasswdTipActivity.class);
                            startActivity(intent);
                        } else if (position == 4) {
                            SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "home",
                                    "locksetting");
                            Intent intent = new Intent(HomeActivity.this, LockOptionActivity.class);
                            intent.putExtra(LockOptionActivity.TAG_COME_FROM,
                                    LockOptionActivity.FROM_HOME);
                            HomeActivity.this.startActivity(intent);
                        }
                        mLeoPopMenu.dismissSnapshotList();
                    }
                });
                mLeoPopMenu.setPopMenuItems(this, getRightMenuItems(), getRightMenuIcons());
                mLeoPopMenu.showPopMenu(this, mTtileBar.findViewById(R.id.iv_option_image), null,
                        null);
                mLeoPopMenu.setListViewDivider(null);
                break;
            case R.id.bg_show_hotapp:
                // app_hot_tip_icon.setVisibility(View.GONE);
                // AppMasterPreference.getInstance(this).setHomeFragmentRedTip(false);
                // SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "home",
                // "hot");
                // Intent nIntent = new Intent(HomeActivity.this,
                // HotAppActivity.class);
                // try {
                // startActivity(nIntent);
                // } catch (Exception e) {
                // }
                // AppMasterPreference.getInstance(this).setAdClickTimeFromHome(
                // System.currentTimeMillis());
                // Intent mWallIntent = mWallAd.getWallIntent();
                // startActivity(mWallIntent);
                Intent msgCenter = new Intent();
                msgCenter.setClass(this, MsgCenterActivity.class);
                startActivity(msgCenter);
                break;
            case R.id.iv_ad_icon:
                AppMasterPreference.getInstance(this).setAdClickTimeFromHome(
                        System.currentTimeMillis());
                LockManager.getInstatnce().timeFilterSelf();
                Intent mWallIntent = mWallAd.getWallIntent();
                startActivity(mWallIntent);
                break;
            default:
                break;
        }
        int current = mViewPager.getCurrentItem();
        if (current < mFragmentHolders.length) {
            BaseFragment fragment = mFragmentHolders[current].fragment;
            if (fragment instanceof Selectable) {
                ((Selectable) fragment).onScrolling();
            }
        }

    }

    private List<String> getRightMenuItems() {
        List<String> listItems = new ArrayList<String>();
        listItems.add(getString(R.string.reset_passwd));
        if (type == AppMasterPreference.LOCK_TYPE_PASSWD) {
            listItems.add(getString(R.string.change_to_gesture));
        } else {
            listItems.add(getString(R.string.change_to_password));
        }
        listItems.add(getString(R.string.set_protect_or_not));
        listItems.add(getString(R.string.passwd_notify));
        listItems.add(getString(R.string.lock_setting));
        return listItems;
    }

    private List<Integer> getRightMenuIcons() {
        List<Integer> icons = new ArrayList<Integer>();
        icons.add(R.drawable.reset_pasword_icon);
        icons.add(R.drawable.switch_pasword_icon);
        icons.add(R.drawable.question_icon);
        icons.add(R.drawable.pasword_icon);
        icons.add(R.drawable.settings);
        return icons;
    }

    private List<MenuItem> getMenuItems() {
        List<MenuItem> listItems = new ArrayList<MenuItem>();
        Resources resources = AppMasterApplication.getInstance().getResources();
        /* 亲给个好评 */
        listItems.add(new MenuItem(resources.getString(R.string.grade),
                R.drawable.menu_star_icon_menu));
        /* 点个赞 */
        listItems
                .add(new MenuItem(resources.getString(R.string.about_praise),
                        R.drawable.menu_hot_icon));
        /* 加入粉丝团 */
        listItems.add(new MenuItem(resources.getString(R.string.about_group),
                R.drawable.menu_join_icon));
        /* 吐个槽 */
        listItems.add(new MenuItem(resources.getString(R.string.feedback),
                R.drawable.menu_feedbacks_icon));
        /* 检查升级 */
        if (SDKWrapper.isUpdateAvailable()) {
            listItems.add(new MenuItem(resources.getString(R.string.app_setting_has_update),
                    R.drawable.menu_updates_icon));
        } else {
            listItems.add(new MenuItem(resources.getString(R.string.app_setting_update),
                    R.drawable.menu_updates_icon));
        }
        /* 关于 */
        listItems.add(new MenuItem(resources.getString(R.string.app_setting_about),
                R.drawable.menu_about_icon));
        /* 卸载PG */
        boolean isAdmin = isAdminActive();
        if (isAdmin) {
            // listItems.add(object)
            listItems.add(new MenuItem(resources.getString(R.string.menue_item_delete_pg),
                    R.drawable.menu_delete));
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
                if (!installed) {
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
                    ShortcutIconResource iconRes = Intent.ShortcutIconResource
                            .fromContext(HomeActivity.this, R.drawable.ic_launcher);
                    shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
                    shortcut.putExtra("duplicate", false);
                    shortcut.putExtra("from_shortcut", true);
                    sendBroadcast(shortcut);
                    prefernece.edit().putBoolean("shortcut", true).apply();
                }

                boolean isInstall = AppMasterPreference.getInstance(HomeActivity.this)
                        .getAdDeskIcon();
                if (!isInstall) {
                    // Intent newIntent = mWallAd.getWallIntent();
                    Intent appWallShortIntent = new Intent(AppMasterApplication.getInstance(),
                            DeskAdActivity.class);
                    appWallShortIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    Intent appWallShortcut = new Intent(
                            "com.android.launcher.action.INSTALL_SHORTCUT");
                    appWallShortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME,
                            getString(R.string.desk_ad_name));
                    ShortcutIconResource appwallIconRes =
                            Intent.ShortcutIconResource.fromContext(
                                    HomeActivity.this,
                                    R.drawable.ad_desktop_icon);
                    appWallShortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                            appwallIconRes);
                    appWallShortcut.putExtra("duplicate", false);
                    appWallShortcut.putExtra("from_shortcut", true);
                    appWallShortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT,
                            appWallShortIntent);
                    sendBroadcast(appWallShortcut);
                    AppMasterPreference.getInstance(HomeActivity.this).setAdDeskIcon(true);
                }
                // boolean appwallFlag =
                // prefernece.getBoolean("shortcut_appwall", false);
                // if (appwallFlag) {
                // Intent appWallShortIntent = new Intent(HomeActivity.this,
                // AppWallActivity.class);
                // appWallShortIntent.putExtra("from_appwall_shortcut",
                // true);
                // appWallShortIntent.setAction(Intent.ACTION_MAIN);
                // appWallShortIntent.addCategory(Intent.CATEGORY_DEFAULT);
                // appWallShortIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // Intent appWallShortcut = new Intent(
                // "com.android.launcher.action.UNINSTALL_SHORTCUT");
                // appWallShortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME,
                // getString(R.string.appwall_name));
                // appWallShortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT,
                // appWallShortIntent);
                // appWallShortcut.putExtra("duplicate", true);
                // sendBroadcast(appWallShortcut);
                // prefernece.edit().putBoolean("shortcut_appwall", false);
                // } else {
                // Intent appWallShortIntent = new Intent(HomeActivity.this,
                // ProxyActivity.class);
                // appWallShortIntent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE,
                // StatusBarEventService.EVENT_BUSINESS_GAME);
                // Intent appWallShortcut = new Intent(
                // "com.android.launcher.action.INSTALL_SHORTCUT");
                // appWallShortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME,
                // getString(R.string.appwall_name));
                // ShortcutIconResource appwallIconRes =
                // Intent.ShortcutIconResource.fromContext(
                // HomeActivity.this,
                // R.drawable.game);
                // appWallShortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                // appwallIconRes);
                // appWallShortcut.putExtra("duplicate", false);
                // appWallShortcut.putExtra("from_shortcut", true);
                // appWallShortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT,
                // appWallShortIntent);
                // sendBroadcast(appWallShortcut);
                // prefernece.edit().putBoolean("shortcut_appwall",
                // true).commit();
                // }

                if (prefernece.getBoolean(KEY_ROOT_CHECK, true)) {
                    boolean root = RootChecker.isRoot();
                    if (root) {
                        SDKWrapper.addEvent(getApplicationContext(), SDKWrapper.P1,
                                KEY_ROOT_CHECK, "root");
                    }
                    prefernece.edit().putBoolean(KEY_ROOT_CHECK, false).apply();
                }
            }

        });
    }

    private void judgeShowGradeTip() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                ActivityManager mActivityManager = (ActivityManager) HomeActivity.this
                        .getSystemService(Context.ACTIVITY_SERVICE);

                @SuppressWarnings("deprecation")
                RunningTaskInfo topTaskInfo = mActivityManager.getRunningTasks(
                        1).get(0);
                String pkg = HomeActivity.this.getPackageName();
                if (pkg.equals(topTaskInfo.baseActivity.getPackageName())) {
                    long count = AppMasterPreference.getInstance(
                            HomeActivity.this).getUnlockCount();
                    boolean haveTip = AppMasterPreference.getInstance(
                            HomeActivity.this).getGoogleTipShowed();
                    if (count >= 25 && !haveTip) {
                        /* google play 评分提示 */
                        googlePlayScoreTip();
                    }
                    /**
                     * show quick guesture dialog tip
                     */
                    boolean switchQuickGesture = AppMasterPreference.getInstance(HomeActivity.this)
                            .getSwitchOpenQuickGesture();
                    if (!switchQuickGesture) {
                        boolean firstDilaogTip = AppMasterPreference.getInstance(HomeActivity.this)
                                .getFristDialogTip();
                        boolean updateUser = AppMasterPreference.getInstance(HomeActivity.this)
                                .getIsUpdateQuickGestureUser();
                        LeoLog.i(TAG, "是否为升级用户：" + updateUser);
                        if (!updateUser) {
                            boolean isMiui = BuildProperties.isMIUI();
                            boolean isOpenWindow = BuildProperties
                                    .isFloatWindowOpAllowed(HomeActivity.this);
                            // new user,enter home >=2 times
                            if (!firstDilaogTip
                                    && AppMasterPreference.getInstance(HomeActivity.this)
                                            .getEnterHomeTimes() == 2) {
                                if (isMiui && isOpenWindow) {
                                    AppMasterPreference.getInstance(HomeActivity.this)
                                            .setFristDialogTip(true);
                                } else {
                                    /* 系统是否安装iswipe */
                                    boolean isIswipInstall = ISwipUpdateRequestManager.getInstance(
                                            getApplicationContext()).isInstallIsiwpe();
                                    if (!isIswipInstall) {
                                        showFirstOpenQuickGestureTipDialog();
                                    } else {
                                        AppMasterPreference.getInstance(HomeActivity.this)
                                                .setFristDialogTip(true);
                                    }
                                    LeoLog.i(TAG, "新用户提示！");
                                }
                            }
                        } else {
                            // update user
                            if (!firstDilaogTip) {
                                LeoLog.i(TAG, "升级用户提示！");
                                boolean isIswipInstall = ISwipUpdateRequestManager.getInstance(
                                        getApplicationContext()).isInstallIsiwpe();
                                if (!isIswipInstall) {
                                    showFirstOpenQuickGestureTipDialog();
                                } else {
                                    AppMasterPreference.getInstance(HomeActivity.this)
                                            .setFristDialogTip(true);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private void googlePlayScoreTip() {
        Intent intent = new Intent(HomeActivity.this,
                GradeTipActivity.class);
        HomeActivity.this.startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        Intent intent = null;
        if (position == 2) {
            /* 加入粉丝团 */

            /* sdk mark */
            SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "menu",
                    "google+");
            Intent intentBeta = null;
            LockManager.getInstatnce().timeFilterSelf();
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
                        startActivity(intentBeta);
                    }
                }
            } else {
                Uri uri = Uri
                        .parse("https://plus.google.com/u/0/communities/112552044334117834440");
                intentBeta = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intentBeta);
            }
            SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "about", "like");

        } else if (position == 1) {
            /* Facebook */
            /* sdk mark */
            SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "menu",
                    "Facebook");
            Intent intentLikeUs = null;
            LockManager.getInstatnce().timeFilterSelf();
            if (AppUtil.appInstalled(getApplicationContext(),
                    "com.facebook.katana")) {
                intentLikeUs = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri
                        .parse("fb://page/1709302419294051");
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
                startActivity(intentLikeUs);
            }
        } else if (position == 0) {
            /* google play */
            /* sdk mark */
            SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "menu",
                    "googleplay");
            LockManager.getInstatnce().timeFilterSelf();
            if (AppUtil.appInstalled(getApplicationContext(),
                    "com.android.vending")) {
                intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri
                        .parse("market://details?id=com.leo.appmaster&referrer=utm_source=AppMaster");
                intent.setData(uri);
                // ComponentName cn = new ComponentName(
                // "com.android.vending",
                // "com.google.android.finsky.activities.MainActivity");
                // intent.setComponent(cn);
                intent.setPackage("com.android.vending");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intent);
                    // mHandler.postDelayed(new Runnable() {
                    // @Override
                    // public void run() {
                    // String lastActivity =
                    // LockManager.getInstatnce().getLastActivity();
                    // if (lastActivity != null
                    // && lastActivity
                    // .equals("com.google.android.finsky.activities.MainActivity"))
                    // {
                    // // Intent intent2 = new Intent(
                    // // HomeActivity.this,
                    // // GooglePlayGuideActivity.class);
                    // // startActivity(intent2);
                    // }
                    // }
                    // }, 1000);
                } catch (Exception e) {

                }
            } else {
                intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri
                        .parse("https://play.google.com/store/apps/details?id=com.leo.appmaster&referrer=utm_source=AppMaster");
                intent.setData(uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } else if (position == 3) {
            /* sdk mark */
            SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "menu",
                    "feedback");
            intent = new Intent(HomeActivity.this,
                    FeedbackActivity.class);
            startActivity(intent);
        } else if (position == 6) {
            /* 游戏中心 */

            /* sdk mark */
            // SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "menu",
            // "gamecenter");
            // intent = new Intent(HomeActivity.this,
            // AppWallActivity.class);
            // intent.putExtra(Constants.HOME_TO_APP_WALL_FLAG,
            // Constants.HOME_TO_APP_WALL_FLAG_VALUE);
            // startActivity(intent);
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
            SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "menu",
                    "about");
            intent = new Intent(HomeActivity.this,
                    AboutActivity.class);
            startActivity(intent);
        }
    }

    private void tryTransStatusbar() {
        if (VERSION.SDK_INT >= 19) {
            // change status bar
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // change nav bar
            // getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        } else {
            mBgStatusbar.setVisibility(View.GONE);
            mFgStatusbar.setVisibility(View.GONE);
        }
    }

    class HomeFragmentHoler {
        String title;
        int iconId;
        boolean isRedTip;
        BaseFragment fragment;
    }

    class HomePagerAdapter extends FragmentPagerAdapter implements IconPagerAdapter {
        public HomePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentHolders[position].fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentHolders[position].title;
        }

        @Override
        public int getCount() {
            return mFragmentHolders.length;
        }

        @Override
        public int getIconResId(int index) {
            return mFragmentHolders[index].iconId;
        }

        @Override
        public boolean getRedTip(int index) {
            return mFragmentHolders[index].isRedTip;
        }
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
            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.home_menu_item, arg2,
                    false);
            TextView tv = (TextView) layout.findViewById(R.id.menu_item_tv);
            /* some item not HTML styled text, such as "check update" item */
            tv.setText(Html.fromHtml(items.get(arg0).itemName));

            /**
             * 类似于阿拉伯语等从右往左显示的处理
             */
            if (LanguageUtils.isRightToLeftLanguage(null)) {
                // Log.e(Constants.RUN_TAG, "阿拉伯语");
                tv.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources()
                        .getDrawable(items.get(arg0).iconId), null);
            } else {
                tv.setCompoundDrawablesWithIntrinsicBounds(
                        getResources().getDrawable(items.get(arg0).iconId), null, null,
                        null);
            }
            return layout;
        }

    }

    class MenuItem {
        String itemName;
        int iconId;

        public MenuItem(String itemName, int iconId) {
            super();
            this.itemName = itemName;
            this.iconId = iconId;
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        if (arg0 == 1 && mFragmentHolders[arg0].fragment instanceof Selectable) {
            ((Selectable) mFragmentHolders[mViewPager.getCurrentItem()].fragment).onScrolling();
        } else {
            ((Selectable) mFragmentHolders[mViewPager.getCurrentItem()].fragment).onSelected(arg0);
        }
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        mShadeView.setPosition(arg0 + arg1);
    }

    @Override
    public void onPageSelected(int arg0) {
        if (mFragmentHolders[arg0].fragment instanceof Selectable) {
            ((Selectable) mFragmentHolders[arg0].fragment).onSelected(arg0);
        } else {

        }
    }

    @Override
    public void onShaderColorChanged(int color) {
        if (mBgStatusbar.getVisibility() == View.VISIBLE) {
            mBgStatusbar.setBackgroundColor(color);
        }
        if (mFragmentHolders[1] != null && mFragmentHolders[1].fragment != null) {
            mFragmentHolders[1].fragment.onBackgroundChanged(color);
        }

    }

    private void showQuickGestureSettingDialog() {
        if (mQuickGestureSettingDialog == null) {
            mQuickGestureSettingDialog = new QuickGestureTipDialog(this);
        }
        mQuickGestureSettingDialog.setQuickContentIconVisibility(true);
        mQuickGestureSettingDialog.setCanceledOnTouchOutside(false);
        mQuickGestureSettingDialog.setTitle(this.getResources().getString(
                R.string.quick_gesture_dialog_tip_contniue_title));
        mQuickGestureSettingDialog.setContent(this.getResources().getString(
                R.string.quick_gesture_dialog_tip_contniue_cotent));
        mQuickGestureSettingDialog.setLeftBtnStr(this.getResources().getString(
                R.string.quick_gesture_dialog_tip_contniue_right_bt));
        mQuickGestureSettingDialog.setRightBtnStr(this.getResources().getString(
                R.string.quick_gesture_dialog_tip_contniue_left_bt));
        mQuickGestureSettingDialog.setLeftOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                AppMasterPreference.getInstance(HomeActivity.this).setQGSettingFirstDialogTip(
                        true);
                SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "qs_guide ",
                        "continued_n");
                dismissDialog(mQuickGestureSettingDialog);
            }
        });
        mQuickGestureSettingDialog.setRightOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                AppMasterPreference.getInstance(HomeActivity.this).setQGSettingFirstDialogTip(
                        true);
                Intent inten = new Intent(HomeActivity.this, QuickGestureActivity.class);
                startActivity(inten);
                SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "qs_guide ",
                        "continued_y");
                dismissDialog(mQuickGestureSettingDialog);
            }
        });
        mQuickGestureSettingDialog.show();
    }

    private void showFirstOpenQuickGestureTipDialog() {
        if (mQuickGestureTip == null) {
            mQuickGestureTip = new QuickGestureTipDialog(this);
        }
        mQuickGestureTip.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mQuickGestureTip != null) {
                    mQuickGestureTip = null;
                }
            }
        });
        mQuickGestureTip.setLeftOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "qs_guide ",
                        "firsd_n");
                SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "qs_iSwipe", "new_guide_n");
                AppMasterPreference.getInstance(HomeActivity.this).setNewUserUnlockCount(0);
                dismissDialog(mQuickGestureTip);
            }
        });
        mQuickGestureTip.setRightOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "qs_guide ",
                        "firstd_y");
                /* 系统是否安装iswipe */
                boolean isIswipInstall = ISwipUpdateRequestManager.getInstance(
                        getApplicationContext()).isInstallIsiwpe();
                AppMasterPreference.getInstance(HomeActivity.this).setQuickGestureRedTip(false);
                AppMasterPreference.getInstance(HomeActivity.this).setNewUserUnlockCount(0);
                if (isIswipInstall) {
                    startQuickGestureEnterTip();
                } else {
                    ISwipUpdateRequestManager.getInstance(HomeActivity.this).iSwipDownLoadHandler();
                }
                SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "qs_iSwipe", "new_guide_y");
                dismissDialog(mQuickGestureTip);
            }
        });
        mQuickGestureTip.setCanceledOnTouchOutside(false);
        AppMasterPreference.getInstance(HomeActivity.this).setFristDialogTip(true);
        mQuickGestureTip.show();
    }

    private void showQuickGestureContinue() {
        AppMasterPreference pre = AppMasterPreference.getInstance(this);
        boolean isFirstSlidingOpenQuick = pre.getFristSlidingTip();
        if (!isFirstSlidingOpenQuick) {
            boolean setMiuiFist = pre.getQuickGestureMiuiSettingFirstDialogTip();
            boolean isMiui = BuildProperties.isMIUI();
            boolean isOpenWindow = BuildProperties.isFloatWindowOpAllowed(this);
            boolean dialogShow = pre.getQGSettingFirstDialogTip();
            if ((isMiui && setMiuiFist && !dialogShow && isOpenWindow && !isFirstSlidingOpenQuick)
                    || (isMiui && !setMiuiFist && isOpenWindow)) {
                if (pre.getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
                    showQuickGestureSettingDialog();
                }
            }
        }
    }

    public void recordEnterHomeTimes() {
        AppMasterPreference pref = AppMasterPreference.getInstance(this);
        int times = pref.getEnterHomeTimes();
        if (times < 2) {
            pref.setEnterHomeTimes(++times);
        }
    }

    /**
     * show the animation when click try in the dialog
     */
    private void startQuickGestureEnterTip() {
        HomeAppManagerFragment fragment = (HomeAppManagerFragment) mFragmentHolders[2].fragment;
        fragment.isGestureAnimating = true;
        LeoLog.d("shodonghua", "set true");
        mPagerTab.setCurrentItem(2);
        fragment.playQuickGestureEnterAnim();
    }

    /**
     * when leave the home page,remove the gesture tab background of app manager
     * fragment
     */
    private void removeAppFragmentGestureBg() {
        HomeAppManagerFragment fragment = (HomeAppManagerFragment) mFragmentHolders[2].fragment;
        fragment.setGestureTabBgVisibility(View.GONE);
    }

    private void dismissDialog(Dialog dlg) {
        if (dlg != null) {
            // 消失后释放相关图片资源
            dlg.dismiss();
            dlg = null;
        }
    }

    private void mobvistaCheck() {
        // -----------------Mobvista Sdk--------------------

        // init wall controller
        // newAdWallController(Context context,String unitid, String fbid)
        mWallAd = MobvistaEngine.getInstance().createAdWallController(this);
        if (mWallAd != null) {
            // preload the wall data
            mWallAd.preloadWall();
        }

    }

    private void showDownLoadISwipDialog(Context context, String flag) {
        if (mIswipDialog == null) {
            mIswipDialog = new IswipUpdateTipDialog(context);
        }
        mIswipDialog.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mIswipDialog != null) {
                    mIswipDialog = null;
                }
            }
        });
        mIswipDialog.setLeftListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mShowIswipeFromNotfi) {
                    mShowIswipeFromNotfi = false;
                    /* 对来自通知栏的统计 */
                    SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "qs_iSwipe",
                            "old_statusbar_n");
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
        mIswipDialog.setRightListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mShowIswipeFromNotfi) {
                    mShowIswipeFromNotfi = false;
                    /* 对来自通知栏的统计 */
                    SDKWrapper.addEvent(HomeActivity.this, SDKWrapper.P1, "qs_iSwipe",
                            "old_statusbar_y");
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
        mIswipDialog.setFlag(flag);
        mIswipDialog.show();
    }

    /* 不是通知进入主页，显示iswipe更新对话框 */
    private void showIswipeUpdateTip(Context context, String flag) {
        boolean tipFlag = ISwipUpdateRequestManager.getInstance(this).getIswipeUpdateTip();
        if (tipFlag) {
            showDownLoadISwipDialog(this, flag);
        }
    }

    /* 自启动引导对话框 */
    private void autoStartGuideDialog() {
        if (mAutoStartGuideDialog == null) {
            mAutoStartGuideDialog = new AutoStartTipDialog(this);
        }
        mAutoStartGuideDialog.setTitleText(getString(R.string.auto_start_guide_tip_title));
        mAutoStartGuideDialog.setContentText(getString(R.string.auto_start_guide_tip_content));
        mAutoStartGuideDialog
                .setLeftButtonText(getString(R.string.auto_start_guide_tip_left_button));
        mAutoStartGuideDialog
                .setRightButtonText(getString(R.string.auto_start_guide_tip_right_button));
        mAutoStartGuideDialog.setIswipeUpdateDialogBackground(R.drawable.auto_start_dialog_tip_bg);
        mAutoStartGuideDialog.setContentImage(R.drawable.shouquan);
        mAutoStartGuideDialog.setFlag(AutoStartTipDialog.AUTOSTART_TIP_DIALOG);
        mAutoStartGuideDialog.setLeftListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mAutoStartGuideDialog.dismiss();
            }
        });
        mAutoStartGuideDialog.setRightListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mAutoStartGuideDialog.dismiss();
                new AutoStartGuideList().executeGuide();
            }
        });
        mAutoStartGuideDialog.setCanceledOnTouchOutside(false);
        mAutoStartGuideDialog.show();
    }

    /* 首次进入PG自启动引导 */
    private void autoStartDialogHandler() {
        /* 是否需要提示 */
        boolean autoStartGuideFlag = AppMasterPreference.getInstance(this).getPGUnlockUpdateTip();
        if (autoStartGuideFlag) {
            /* 是否存在于白名单 */
            int model = AutoStartGuideList.isAutoWhiteListModel(this);
            if (model != -1) {
                autoStartGuideDialog();
            }
        }
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
            LockManager.getInstatnce().timeFilterSelf();
            startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /* 开启高级保护引导对话框 */
    private void advanceProtectDialogTip() {
        if (mAdvanceProtectDialog == null) {
            mAdvanceProtectDialog = new AdvanceProtectTipDialog(this);
        }
        int content = AutoStartGuideList
                .getAutoWhiteListTipText(AppMasterApplication.getInstance());
        mAdvanceProtectDialog.setContentTextId(content);
        mAdvanceProtectDialog.setLeftListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mAdvanceProtectDialog.dismiss();
            }
        });
        mAdvanceProtectDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mAdvanceProtectDialog != null) {
                    mAdvanceProtectDialog = null;
                }
            }
        });
        mAdvanceProtectDialog.setRightListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mAdvanceProtectDialog.dismiss();
                Intent intent = new Intent(HomeActivity.this, LockOptionActivity.class);
                startActivity(intent);
            }
        });
        mAdvanceProtectDialog.setFlag(AdvanceProtectTipDialog.ADVANCE_PROTECT_TIP_DIALOG);
        mAdvanceProtectDialog.setCanceledOnTouchOutside(false);
        mAdvanceProtectDialog.show();
    }

    /* 是否显示高级保护对话框的处理 */
    private boolean showAdvanceProtectDialog() {
        String channel = getString(R.string.channel_code);
        boolean isFilterChannel = false;
        List<String> channels = Arrays.asList(FILTER_CHANNEL);
        if (channels != null && channels.size() > 0) {
            for (String string : channels) {
                if (string.equals(channel)) {
                    isFilterChannel = true;
                    break;
                }
            }
        }
        boolean isAdvanceProtectTip = AppMasterPreference.getInstance(this)
                .getAdvanceProtectDialogTip();
        /* 是否为新用户 */
        boolean updateUser = AppMasterPreference.getInstance(HomeActivity.this)
                .getIsUpdateQuickGestureUser();
        if (!isFilterChannel/* 是否为不是需要过滤不显示的渠道 */
                && isAdvanceProtectTip /* 是否需要提示 */
                && !updateUser /* 是否为新用户 */
                && !isAdminActive() /* 是否为开启高级保护 */) {
            advanceProtectDialogTip();
        }
        return isFilterChannel;
    }
}
