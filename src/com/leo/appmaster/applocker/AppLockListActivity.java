package com.leo.appmaster.applocker;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.applocker.model.ProcessDetectorUsageStats;
import com.leo.appmaster.applocker.service.TaskDetectService;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.engine.AppLoadEngine.AppChangeListener;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.LockModeEvent;
import com.leo.appmaster.eventbus.event.NewThemeEvent;
import com.leo.appmaster.home.AutoStartGuideList;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.lockertheme.LockerTheme;
import com.leo.appmaster.model.AppInfo;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.MaterialRippleLayout;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.LeoLog;

/**
 * Created by qili on 15-10-9.
 */
public class AppLockListActivity extends BaseActivity implements
        AppChangeListener, OnClickListener, OnItemClickListener, RippleView.OnRippleCompleteListener {
    public final static int INIT_UI_DONE = 111;
    public final static int LOAD_DATA_DONE = 112;
    public final static int DEFAULT_SORT = 0;
    public final static String FROM_DEFAULT_RECOMMENT_ACTIVITY = "applocklist_activity";
    private View mHeadView;
    private RippleView mLockModeView, mWeiZhuangView, mLockThemeView;
    private View mBarView;
    //    private View mClickView;
    private ImageView mRedDot, mGuideHelpTipBt;
    private TextView mSecurityGuideBt, mAutoGuideBt, mBackageGroundBt;
    private Button mFinishBt;
    private RelativeLayout mSecurityRL, mAutoRL, mBackgroundRL;
    private TextView mSecurityText, mAutoText, mBackGroudText;
    private View mGuideTip;
    private ListView mLockList;
    private CommonToolbar mTtileBar;
    private ListAppLockAdapter mLockAdapter;
    private Toast toast = null;

    private List<AppInfo> mLockedList;
    private List<AppInfo> mUnlockList;
    private List<AppInfo> mUnlockRecommendList;
    private List<AppInfo> mUnlockNormalList;
    private List<String> mDefaultLockList;

    private static final boolean DBG = false;
    private static String LOCK_AUTO_START_GUIDE_PUSH = "lock_auto_start_guide_push";
    private int mWhiteMode = -1;
    private boolean mIsLenovo;
    private ProgressBar mProgressBar;
    private ArrayList<AppInfo> mResaultList;
    private int goCnotR = 0;

    private android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case INIT_UI_DONE:
                    asyncLoad();
                    break;
                case LOAD_DATA_DONE:
                    loadDone();
                    break;
            }
        }
    };

    private void loadDone() {
        //Flag is Recomment list
        mLockAdapter.setFlag(FROM_DEFAULT_RECOMMENT_ACTIVITY);
        if (mResaultList != null) {
            mLockAdapter.setData(mResaultList);
        }

        mProgressBar.setVisibility(View.GONE);
        mLockList.setVisibility(View.VISIBLE);
        updateHelpState();
    }

    private void asyncLoad() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long start = SystemClock.elapsedRealtime();
        setContentView(R.layout.activity_list_lockapp);

        AppLoadEngine.getInstance(this).registerAppChangeListener(this);
        LeoEventBus.getDefaultBus().register(this);
        handleIntent();
        initUI();
        goCnotR = 1;
        mHandler.sendEmptyMessage(INIT_UI_DONE);


        LeoLog.i("TsCost", "AppLockListActivity-onCreate: " + (SystemClock.elapsedRealtime() - start));
    }


    private void handleIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            boolean isShowGuide = intent.getBooleanExtra(LOCK_AUTO_START_GUIDE_PUSH, false);
            if (isShowGuide) {
                /*应用锁，白名单引导，push掉起显示*/
                AppMasterPreference.getInstance(this).setLockAndAutoStartGuide(false);
            }
        }
    }


    private void initUI() {
        /* 是否存在于白名单:-1-----不存在百名单 */
        mWhiteMode = AutoStartGuideList.isAutoWhiteListModel(this);
        mIsLenovo = BuildProperties.isLenoveModel();

        mTtileBar = (CommonToolbar) findViewById(R.id.listlock_title_bar);
        mTtileBar.setToolbarTitle(R.string.app_lock);
        mTtileBar.setToolbarColorResource(R.color.cb);
        mTtileBar.setOptionMenuVisible(false);
        mTtileBar.setNavigationClickListener(this);

        mBarView = findViewById(R.id.lock_setting);
        mBarView.setVisibility(View.VISIBLE);
        mBarView.setOnClickListener(this);

        LayoutInflater layoutInflater = LayoutInflater.from(this);

        mHeadView = layoutInflater.inflate(R.layout.list_lockapp_headview, null);

        mLockModeView = (RippleView) mHeadView.findViewById(R.id.lock_mode_type);
        mLockModeView.setOnRippleCompleteListener(this);

        mWeiZhuangView = (RippleView) mHeadView.findViewById(R.id.weizhuang_type);
        mWeiZhuangView.setOnRippleCompleteListener(this);

        mLockThemeView = (RippleView) mHeadView.findViewById(R.id.lock_theme_type);
        mLockThemeView.setOnRippleCompleteListener(this);
        mRedDot = (ImageView) mHeadView.findViewById(R.id.theme_red_dot);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading_lockapp);

        mLockList = (ListView) findViewById(R.id.lock_app_list);
        mLockList.setOnItemClickListener(this);
        mLockList.addHeaderView(mHeadView);
        mLockAdapter = new ListAppLockAdapter(this);
        mLockList.setAdapter(mLockAdapter);

        mLockedList = new ArrayList<AppInfo>();
        mUnlockList = new ArrayList<AppInfo>();
        mUnlockRecommendList = new ArrayList<AppInfo>();
        mUnlockNormalList = new ArrayList<AppInfo>();

        mGuideTip = findViewById(R.id.guide_tip_layout);
        mSecurityGuideBt = (TextView) findViewById(R.id.security_guide_button);
        mSecurityGuideBt.setOnClickListener(this);
        mAutoGuideBt = (TextView) findViewById(R.id.auto_guide_button);
        mAutoGuideBt.setOnClickListener(this);
        mBackageGroundBt = (TextView) findViewById(R.id.background_guide_button);
        mBackageGroundBt.setOnClickListener(this);
        mSecurityRL = (RelativeLayout) findViewById(R.id.security_guide);
        mAutoRL = (RelativeLayout) findViewById(R.id.auto_guide);
        mBackgroundRL = (RelativeLayout) findViewById(R.id.background_guide);
        mFinishBt = (Button) findViewById(R.id.finish);
        mFinishBt.setOnClickListener(this);

//        mClickView = findViewById(R.id.lock_tip_help);
//        mClickView.setOnClickListener(this);
        mGuideHelpTipBt = (ImageView) findViewById(R.id.tip_help);
        mGuideHelpTipBt.setOnClickListener(this);

        mSecurityText = (TextView) findViewById(R.id.security_guide_text);
        mAutoText = (TextView) findViewById(R.id.auto_guide_text);
        mBackGroudText = (TextView) findViewById(R.id.background_guide_text);

        AppMasterPreference amp = AppMasterPreference.getInstance(this);
        boolean isShowLockAutoTip = amp.getLockAndAutoStartGuide();
        if (!isShowLockAutoTip) {
            if (mWhiteMode != -1 || needAppGuide()) {
                openHelp(true, false);
            }
            amp.setLockAndAutoStartGuide(true);
        }
    }

    private void updateHelpState() {
        if (DBG) {
            mWhiteMode = AutoStartGuideList.HUAWEIP7_PLUS;
        }
        boolean needAppGuide = needAppGuide();
        if (mWhiteMode != -1 || needAppGuide) {
            mGuideHelpTipBt.setVisibility(View.VISIBLE);
            if (needAppGuide && mGuideTip.getVisibility() == View.VISIBLE) {
                mSecurityRL.setVisibility(View.VISIBLE);
            } else {
                mSecurityRL.setVisibility(View.GONE);
            }
        } else {
            mGuideHelpTipBt.setVisibility(View.GONE);
            mGuideTip.setVisibility(View.GONE);
            mLockList.setVisibility(View.VISIBLE);
        }
    }

    private void loadData() {
        long start = SystemClock.elapsedRealtime();

        mUnlockRecommendList.clear();
        mUnlockNormalList.clear();
        mUnlockList.clear();
        mLockedList.clear();

        ArrayList<AppItemInfo> list = AppLoadEngine.getInstance(this)
                .getAllPkgInfo();
        List<String> lockList = mLockManager.getCurLockList();
        mDefaultLockList = AppLoadEngine.getInstance(this).getRecommendLockList();

        long part1 = SystemClock.elapsedRealtime();
        LeoLog.i("TsCost", "loadData part1: " + (part1 - start));

        for (AppItemInfo appDetailInfo : list) {
            if (mLockManager.inFilterList(appDetailInfo.packageName)) {
                continue;
            }
            if (lockList.contains(appDetailInfo.packageName)) {
                appDetailInfo.topPos = fixPosEqules(appDetailInfo);
                appDetailInfo.isLocked = true;
                mLockedList.add(appDetailInfo);
            } else {
                appDetailInfo.isLocked = false;
                appDetailInfo.topPos = fixPosEqules(appDetailInfo);

                if (appDetailInfo.topPos > -1) {
                    mUnlockRecommendList.add(appDetailInfo);
                } else {
                    mUnlockNormalList.add(appDetailInfo);
                }
            }

            if (mDefaultLockList.contains(appDetailInfo.packageName)) {
                appDetailInfo.isRecomment = true;
            } else {
                appDetailInfo.isRecomment = false;
            }
        }

        long part2 = SystemClock.elapsedRealtime();
        LeoLog.i("TsCost", "loadData part2: " + (part2 - part1));

        Collections.sort(mLockedList, new LockedAppComparator(lockList));
        Collections.sort(mUnlockRecommendList, new DefalutAppComparator());
        Collections.sort(mUnlockNormalList, new DefalutAppComparator());

        ArrayList<AppInfo> resaultUnlock = new ArrayList<AppInfo>(mUnlockRecommendList);
        resaultUnlock.addAll(mUnlockNormalList);
        mUnlockList = resaultUnlock;

        //the final list
        mResaultList = new ArrayList<AppInfo>(mLockedList);
        mResaultList.addAll(mUnlockList);


        long part3 = SystemClock.elapsedRealtime();
        LeoLog.i("TsCost", "loadData part3: " + (part3 - part2));

        if (mHandler != null) {
            mHandler.sendEmptyMessage(LOAD_DATA_DONE);
        }
    }

    private int fixPosEqules(AppInfo info) {
        int topPosGet = info.topPos;
        String pckName = info.packageName;

        if (topPosGet != -1) {
            String[] strings = AppLoadEngine.sLocalLockArray;
            int k = 0;
            boolean isHavePckName = false;
            for (int i = 0; i < strings.length; i++) {
                String string = strings[i];
                if (string.equals(pckName)) {
                    k = i;
                    isHavePckName = true;
                    break;
                }
            }

            if (isHavePckName) {
                String[] nums = AppLoadEngine.sLocalLockNumArray;
                int num = Integer.parseInt(nums[k]);
                if (num > topPosGet) {
                    return num;
                } else {
                    return topPosGet;
                }
            } else {
                if (topPosGet <= 0) {
                    return 1000;
                } else {
                    return topPosGet;
                }
            }
        } else {
            return -1;
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {

        }
    }

    private void showTextToast(String msg) {
        if (toast == null) {
            toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        long a = System.currentTimeMillis();

        if (i == 0) return;
        MaterialRippleLayout headView = (MaterialRippleLayout) view;
        ListLockItem lockImageView = (ListLockItem) headView.findViewById(R.id.content_item_all);
        LockMode curMode = mLockManager.getCurLockMode();
        if (curMode == null || curMode.defaultFlag == 0) {
            Toast.makeText(this, R.string.unlock_all_mode_tip, Toast.LENGTH_SHORT).show();
            return;
        }

        if (view == null) {
            return;
        }

        AppInfo mLastSelectApp = lockImageView.getInfo();
        if (mLastSelectApp == null) {
            return;
        }

        long b = System.currentTimeMillis();
        LeoLog.d("testWhoNull", "part a : " + (b - a));

        AppInfo info = null;
        if (mLastSelectApp.isLocked) {
            LeoLog.d("testWhoNull", "mLastSelectApp.isLocked");
            mLastSelectApp.isLocked = false;
            for (AppInfo baseInfo : mLockedList) {
                if (baseInfo.packageName != null
                        && baseInfo.packageName.equals(mLastSelectApp.packageName)) {
                    info = baseInfo;
                    info.isLocked = false;
                    break;
                }
            }

            if (info == null) {
                return;
            }

            if (!mUnlockList.contains(info)) {
                mUnlockList.add(info);
            }
            if (mLockedList.contains(info)) {
                mLockedList.remove(info);
            }

            long c = System.currentTimeMillis();
            LeoLog.d("testWhoNull", "part b : " + (c - b));

            List<String> list = new LinkedList<String>();
            list.add(info.packageName);
            mLockManager.removePkgFromMode(list, mLockManager.getCurLockMode(), true);

            long d = System.currentTimeMillis();
            LeoLog.d("testWhoNull", "part c : " + (d - c));

            // to set view unlocked
            if (lockImageView != null) {
                lockImageView.setLockView(false);
                lockImageView.setDescEx(info, false);
            }
            String toast = this.getString(R.string.unlock_app_action, info.label);
            showTextToast(toast);
            SDKWrapper.addEvent(this, SDKWrapper.P1, "app", "unlock_" + curMode.modeName + "_"
                    + mLastSelectApp.packageName);

            long e = System.currentTimeMillis();
            LeoLog.d("testWhoNull", "part c : " + (e - d));
        } else {
            LeoLog.d("testWhoNull", "!!!mLastSelectApp.isLocked");
            mLastSelectApp.isLocked = true;
            for (AppInfo baseInfo : mUnlockList) {
                if (baseInfo.packageName != null
                        && baseInfo.packageName.equals(mLastSelectApp.packageName)) {
                    info = baseInfo;
                    info.isLocked = true;
                    break;
                }
            }
            if (info == null) return;

            if (mLockedList.contains(info)) {
                mLockedList.remove(info);
            }
            mLockedList.add(0, info);
            if (mUnlockList.contains(info)) {
                mUnlockList.remove(info);
            }

            long c = System.currentTimeMillis();
            LeoLog.d("testWhoNull", "part b : " + (c - b));

            List<String> list = new LinkedList<String>();
            list.add(info.packageName);
            mLockManager.addPkg2Mode(list, mLockManager.getCurLockMode());

            long d = System.currentTimeMillis();
            LeoLog.d("testWhoNull", "part c : " + (d - c));

            // to set view lock
            if (lockImageView != null) {
                lockImageView.setLockView(true);
                lockImageView.setDescEx(info, true);
            }
            String toast = this.getString(R.string.lock_app_action, info.label);
            showTextToast(toast);
            SDKWrapper.addEvent(this, SDKWrapper.P1, "app", "lock_" + curMode.modeName + "_"
                    + mLastSelectApp.packageName);
            long e = System.currentTimeMillis();
            LeoLog.d("testWhoNull", "part c : " + (e - d));
        }
    }

    @Override
    public void onRippleComplete(RippleView rippleView) {
        if (mLockModeView == rippleView) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "home", "modes");
            SDKWrapper.addEvent(this, SDKWrapper.P1, "app_func", "modes");
            enterLockMode();
        } else if (mWeiZhuangView == rippleView) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "home", "appcover");
            SDKWrapper.addEvent(this, SDKWrapper.P1, "app_func", "appcover");
            enterWeiZhuang();
        } else if (mLockThemeView == rippleView) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "home", "theme");
            SDKWrapper.addEvent(this, SDKWrapper.P1, "app_func", "theme");
            SDKWrapper.addEvent(this, SDKWrapper.P1, "theme_enter", "home");
            enterLockTheme();
        }
//        else if (mBarView == rippleView) {
//            SDKWrapper.addEvent(this, SDKWrapper.P1, "home", "locksetting");
//            SDKWrapper.addEvent(this, SDKWrapper.P1, "app_func", "locksetting");
//            enterLockSetting();
//        }
    }

    private class LockedAppComparator implements Comparator<AppInfo> {
        List<String> sortBase;

        public LockedAppComparator(List<String> sortBase) {
            super();
            this.sortBase = sortBase;
        }

        @Override
        public int compare(AppInfo lhs, AppInfo rhs) {
            if (sortBase.indexOf(lhs.packageName) > sortBase
                    .indexOf(rhs.packageName)) {
                return 1;
            } else {
                return -1;
            }
        }
    }


    public static class DefalutAppComparator implements Comparator<AppInfo> {
        @Override
        public int compare(AppInfo lhs, AppInfo rhs) {


//            if (lhs.topPos != -1 || rhs.topPos != -1) {
//                Integer a = lhs.topPos;
//                Integer b = rhs.topPos;
//                return b.compareTo(a);
//            }
//
//            if (lhs.systemApp && !rhs.systemApp) {
//                return -1;
//            } else if (!lhs.systemApp && rhs.systemApp) {
//                return 1;
//            }
//
//            return Collator.getInstance().compare(trimString(lhs.label),
//                    trimString(rhs.label));

            if (lhs.topPos > -1 && rhs.topPos < 0) {
                return 1;
            } else if (lhs.topPos < 0 && rhs.topPos > -1) {
                return -1;
            } else if (lhs.topPos > -1 && rhs.topPos > -1) {
//                return lhs.topPos - rhs.topPos;
                return rhs.topPos - lhs.topPos;
            }

//            if (lhs.topPos > -1 && rhs.topPos < 0) {
//                return -1;
//            } else if (lhs.topPos < 0 && rhs.topPos > -1) {
//                return 1;
//            } else if (lhs.topPos > -1 && rhs.topPos > -1) {
//                return lhs.topPos - rhs.topPos;
//            }

            if (lhs.systemApp && !rhs.systemApp) {
                return -1;
            } else if (!lhs.systemApp && rhs.systemApp) {
                return 1;
            }

            return Collator.getInstance().compare(trimString(lhs.label),
                    trimString(rhs.label));
        }

        private String trimString(String s) {
            return s.replaceAll("\u00A0", "").trim();
        }
    }

    public static class NameComparator implements Comparator<AppInfo> {

        @Override
        public int compare(AppInfo lhs, AppInfo rhs) {
            return Collator.getInstance().compare(trimString(lhs.label),
                    trimString(rhs.label));
        }

        private String trimString(String s) {
            return s.replaceAll("\u00A0", "").trim();
        }

    }

    @Override
    public void onAppChanged(ArrayList<AppItemInfo> changes, int type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.security_guide_button:
                /* Android5.01+ */
                ProcessDetectorUsageStats usageStats = new ProcessDetectorUsageStats();
                if (!usageStats.checkAvailable()) {
                    Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    try {
                        startActivity(intent);
                        mLockManager.filterSelfOneMinites();
                    } catch (Exception e) {
                    }
                }
                AppMasterPreference.getInstance(this).setLockAndAutoStartGuide(true);
                SDKWrapper.addEvent(this, SDKWrapper.P1, "gd_wcnts", "gd_wcnts_use");
                break;
            case R.id.auto_guide_button:
                /* 华为P7类rom */
                if (AutoStartGuideList.HUAWEIP7_PLUS == mWhiteMode) {
                    Intent autoIntent = new Intent();
                    autoIntent.setAction("android.intent.action.MAIN");
                    ComponentName autoCn = new ComponentName("com.huawei.systemmanager",
                            "com.huawei.systemmanager.optimize.bootstart.BootStartActivity");
                    autoIntent.setComponent(autoCn);
                    try {
                        startActivity(autoIntent);
                        mLockManager.filterSelfOneMinites();
                    } catch (Exception e) {
                    }
                } else {
                    new AutoStartGuideList().executeGuide();
                }
                SDKWrapper.addEvent(this, SDKWrapper.P1, "gd_wcnts", "gd_wcnts_fn");
                AppMasterPreference.getInstance(this).setLockAndAutoStartGuide(true);
                break;
            case R.id.background_guide_button:
                new AutoStartGuideList().executeGuide();
                SDKWrapper.addEvent(this, SDKWrapper.P1, "gd_wcnts", "gd_wcnts_back");
                break;
            case R.id.finish:
                if (mGuideTip.getVisibility() == View.VISIBLE) {
                    openHelp(false, true);
                    Animation animation = AnimationUtils.loadAnimation(AppLockListActivity.this,
                            R.anim.help_tip_show);
                    mGuideHelpTipBt.startAnimation(animation);
                }
                AppMasterPreference.getInstance(this).setLockAndAutoStartGuide(true);
                SDKWrapper.addEvent(this, SDKWrapper.P1, "gd_wcnts", "gd_wcnts_finish");
                break;
            case R.id.tip_help:
                if (mGuideTip.getVisibility() == View.GONE) {
                    openHelp(true, true);
                } else if (mGuideTip.getVisibility() == View.VISIBLE) {
                    openHelp(false, true);
                }
                AppMasterPreference.getInstance(this).setLockAndAutoStartGuide(true);
                break;
//            case R.id.lock_tip_help:
//                if (mGuideTip.getVisibility() == View.GONE) {
//                    openHelp(true, true);
//                } else if (mGuideTip.getVisibility() == View.VISIBLE) {
//                    openHelp(false, true);
//                }
//                AppMasterPreference.getInstance(this).setLockAndAutoStartGuide(true);
//                break;
            case R.id.ct_back_rl:
                onBackPressed();
                break;
            case R.id.lock_setting:
                SDKWrapper.addEvent(this, SDKWrapper.P1, "home", "locksetting");
                SDKWrapper.addEvent(this, SDKWrapper.P1, "app_func", "locksetting");
                enterLockSetting();
                break;
        }
    }

    private void openHelp(boolean open, boolean anim) {
        if (open) {
            mGuideTip.setVisibility(View.VISIBLE);
            if (anim) {
                Animation animation = AnimationUtils.loadAnimation(AppLockListActivity.this,
                        R.anim.lock_mode_guide_in);
                mGuideTip.startAnimation(animation);
            }
            setGuideTipShow();
            mLockList.setVisibility(View.INVISIBLE);
            SDKWrapper.addEvent(this, SDKWrapper.P1, "gd_wcnts", "gd_display_cnts");
        } else {
            mGuideTip.setVisibility(View.GONE);
            if (anim) {
                mGuideTip.startAnimation(AnimationUtils
                        .loadAnimation(AppLockListActivity.this, R.anim.lock_mode_guide_out));
            }
            mLockList.setVisibility(View.VISIBLE);
        }
    }

    private void setGuideTipShow() {
        if (needAppGuide()) {
            mGuideTip.setVisibility(View.VISIBLE);
            mSecurityRL.setVisibility(View.VISIBLE);
            SDKWrapper.addEvent(this, SDKWrapper.P1, "gd_wcnts", "gd_display_use");
        } else {
            mSecurityRL.setVisibility(View.GONE);
            mGuideTip.setVisibility(View.GONE);
        }
        if (DBG) {
            mWhiteMode = AutoStartGuideList.HUAWEIP7_PLUS;
        }
        if (mWhiteMode != -1) {
            mGuideTip.setVisibility(View.VISIBLE);
            int content = AutoStartGuideList
                    .getAutoWhiteListTipText(AppMasterApplication.getInstance());
            mAutoText.setText(content);
            mAutoRL.setVisibility(View.VISIBLE);
            mBackgroundRL.setVisibility(View.GONE);
            SDKWrapper.addEvent(this, SDKWrapper.P1, "gd_wcnts", "gd_display_fn");
            /* 查询是否为双提示打开系统权限的机型 */
            if (AutoStartGuideList.isDoubleTipOPenPhone(mWhiteMode)) {
                mBackgroundRL.setVisibility(View.VISIBLE);
                mAutoText.setText(R.string.auto_start_tip_text_huawei_plus);
                mBackGroudText.setText(content);
                SDKWrapper.addEvent(this, SDKWrapper.P1, "gd_wcnts", "gd_display_back");
            }
        } else {
            mAutoRL.setVisibility(View.GONE);
            mBackgroundRL.setVisibility(View.GONE);
        }
    }

    private boolean needAppGuide() {
        if (TaskDetectService.sDetectSpecial && !mIsLenovo) {
            ProcessDetectorUsageStats usageStats = new ProcessDetectorUsageStats();
            return !usageStats.checkAvailable();
        }
        return false;
    }

    private void enterLockTheme() {
        Intent intent = new Intent(this, LockerTheme.class);
        if (mRedDot.getVisibility() == View.VISIBLE) {
            mRedDot.setVisibility(View.GONE);
            intent.putExtra("isRedDot", true);
        }
        this.startActivity(intent);
    }

    private void enterWeiZhuang() {
        Intent intent = new Intent(this, WeiZhuangActivity.class);
        this.startActivity(intent);
    }

    private void enterLockSetting() {
        Intent intent = new Intent(this, LockOptionActivity.class);
        intent.putExtra(LockOptionActivity.TAG_COME_FROM,
                LockOptionActivity.FROM_HOME);
        this.startActivity(intent);
    }

    private void enterLockMode() {
        Intent intent = new Intent(this, LockModeActivity.class);
        intent.putExtra("isFromHomeToLockMode", true);
        this.startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        boolean fromLockMore = getIntent().getBooleanExtra("from_lock_more", false);
        LeoLog.e("lockmore", "fromLockMore==" + fromLockMore);
        boolean isStartFromLockmode = getIntent().getBooleanExtra("enter_from_lockmode", false);
        LeoLog.e("lockmore", "isStartFromLockmode==" + isStartFromLockmode);
        if (fromLockMore) {
            mLockManager.filterPackage(getPackageName(), 1000);
            Intent intent = new Intent(this, HomeActivity.class);
            Log.e("lockmore", "settrue");
            startActivity(intent);
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if (toast != null) {
            toast.cancel();
        }
        AppLoadEngine.getInstance(this).unregisterAppChangeListener(this);
        // 解决内存泄露
        LeoEventBus.getDefaultBus().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        long start = SystemClock.elapsedRealtime();
        if (goCnotR == 0) {
            loadData();
        }
        checkNewTheme();
        goCnotR = 0;
        super.onResume();
        LeoLog.i("TsCost", "AppLockListActivity-onResume: " + (SystemClock.elapsedRealtime() - start));
    }

    public void onEventMainThread(NewThemeEvent event) {
        //little red point
        mRedDot.setVisibility(View.VISIBLE);
    }

    public void onEventMainThread(LockModeEvent event) {
        asyncLoad();
    }

    private void checkNewTheme() {
        String locSerial = AppMasterPreference.getInstance(this)
                .getLocalThemeSerialNumber();
        String onlineSerial = AppMasterPreference.getInstance(this)
                .getOnlineThemeSerialNumber();
        if (!locSerial.equals(onlineSerial)) {
            mRedDot.setVisibility(View.VISIBLE);
        } else {
            mRedDot.setVisibility(View.GONE);
        }
    }
}
