package com.leo.appmaster.applocker;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.lockswitch.BlueToothLockSwitch;
import com.leo.appmaster.applocker.lockswitch.SwitchGroup;
import com.leo.appmaster.applocker.lockswitch.WifiLockSwitch;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.applocker.model.ProcessDetectorUsageStats;
import com.leo.appmaster.applocker.service.TaskDetectService;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.engine.AppLoadEngine.AppChangeListener;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.GradeEvent;
import com.leo.appmaster.eventbus.event.LockModeEvent;
import com.leo.appmaster.eventbus.event.NewThemeEvent;
import com.leo.appmaster.home.AutoStartGuideList;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.lockertheme.LockerTheme;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.model.AppInfo;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by qili on 15-10-9.
 */
public class AppLockListActivity extends BaseActivity implements
        AppChangeListener, OnClickListener, OnItemClickListener {
    public final static int INIT_UI_DONE = 111;
    public final static int LOAD_DATA_DONE = 112;
    public final static int DEFAULT_SORT = 0;
    public final static String FROM_DEFAULT_RECOMMENT_ACTIVITY = "applocklist_activity";
    private static final int IN_LOCK_GUIDE_COUNT = 3;

    private View mHeadView;
    private RippleView mLockModeView, mWeiZhuangView, mLockThemeView;
    private View mBarView;
    //    private View mClickView;
    private ImageView mRedDot, mGuideHelpTipBt;
    private View mGuideHelpTipBtClick;
    private View mGuideHelpTip;
    private TextView mSecurityGuideBt, mAutoGuideBt, mBackageGroundBt;
    private RippleView mFinishBtClick;
    private RelativeLayout mSecurityRL, mAutoRL, mBackgroundRL;
    private TextView mAutoText, mBackGroudText;
    private View mGuideTip;

    @Nullable
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

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
    private ImageView mAutoImage;
    private boolean isFromConfrim = false;
    private WifiLockSwitch wifiSwitch;
    private BlueToothLockSwitch blueToothSwitch;

    private int mWifiAndBluetoothLockCount;
    private int mLockListCount;
    private int mFromSuccessListCount; // 首次进入页面加锁应用个数

    private List<AppItemInfo> mAppList;
    private int mPosition = 1; // 用来定位

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
            mLockAdapter.setMode(mLockManager.getCurLockMode(), false);
            mLockAdapter.setData(mResaultList, true);
            if (mAppList != null && mAppList.size() > 0) {
                List<AppInfo> switchs = mLockAdapter.getSwitchs();
                if(switchs != null && switchs.size() > 0) {
                    mPosition = + (switchs.size() + 1);
                }
                ThreadManager.getUiThreadHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        mLockList.setSelection(mPosition);
                    }
                });
            }
        }

        mProgressBar.setVisibility(View.GONE);
        if (mGuideTip.getVisibility() == View.GONE) {
            mLockList.setVisibility(View.VISIBLE);
        }
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

        wifiSwitch = new WifiLockSwitch();
        blueToothSwitch = new BlueToothLockSwitch();

        AppLoadEngine.getInstance(this).registerAppChangeListener(this);
        LeoEventBus.getDefaultBus().register(this);
        handleIntent();
        initUI();
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

            isFromConfrim = intent.getBooleanExtra(Constants.FROM_CONFIRM_FRAGMENT, false);
            mFromSuccessListCount = intent.getIntExtra("first_lock_size", 0);
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
        mLockModeView.setOnClickListener(this);

        mWeiZhuangView = (RippleView) mHeadView.findViewById(R.id.weizhuang_type);
        mWeiZhuangView.setOnClickListener(this);

        mLockThemeView = (RippleView) mHeadView.findViewById(R.id.lock_theme_type);
        mLockThemeView.setOnClickListener(this);

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
        if(wifiSwitch.isLockNow(mLockManager.getCurLockMode())) {
            mWifiAndBluetoothLockCount ++;
        }
        if(blueToothSwitch.isLockNow(mLockManager.getCurLockMode())) {
            mWifiAndBluetoothLockCount ++;
        }

        mGuideTip = findViewById(R.id.guide_tip_layout);
        mSecurityGuideBt = (TextView) findViewById(R.id.security_guide_button);
        mSecurityGuideBt.setOnClickListener(this);
        mAutoGuideBt = (TextView) findViewById(R.id.auto_guide_button);
        mAutoGuideBt.setOnClickListener(this);
        mBackageGroundBt = (TextView) findViewById(R.id.background_guide_button);
        mBackageGroundBt.setOnClickListener(this);
        mSecurityRL = (RelativeLayout) findViewById(R.id.security_guide);
        mAutoRL = (RelativeLayout) findViewById(R.id.auto_guide);
        mAutoImage = (ImageView) findViewById(R.id.auto_icon);
        mBackgroundRL = (RelativeLayout) findViewById(R.id.background_guide);

        mFinishBtClick = (RippleView) findViewById(R.id.rv_finish);
        mFinishBtClick.setOnClickListener(this);


        mGuideHelpTip = findViewById(R.id.tip_help0);
        mGuideHelpTipBt = (ImageView) findViewById(R.id.tip_help);
        mGuideHelpTipBtClick = findViewById(R.id.tip_help_click);
        mGuideHelpTipBtClick.setOnClickListener(this);

        mAutoText = (TextView) findViewById(R.id.auto_guide_text);
        mBackGroudText = (TextView) findViewById(R.id.background_guide_text);
        updateHelpState();
        inLockListGuideTip();
    }

    /**
     * 进入应用锁列表，引导提示
     */
    private void inLockListGuideTip() {
        if (!isFromConfrim) {
            if (!isGuideEnough()) {
                int guideCount = LeoPreference.getInstance().getInt(PrefConst.KEY_IN_LOCK_GUIDE, 0);
                guideCount = guideCount + 1;
                LeoPreference.getInstance().putInt(PrefConst.KEY_IN_LOCK_GUIDE, guideCount);
            }
            boolean isGuideEnough = isGuideEnough();
            if (!isGuideEnough) {
                if (mWhiteMode != -1 || needAppGuide()) {
                    openHelp(true, false);
                }
            }
        }
    }

    private void updateHelpState() {
        if (DBG) {
            mWhiteMode = AutoStartGuideList.HUAWEIP7_PLUS;
        }
        boolean needAppGuide = needAppGuide();
        if (mWhiteMode != -1 || needAppGuide) {
            mGuideHelpTip.setVisibility(View.VISIBLE);
            mGuideHelpTipBt.setVisibility(View.VISIBLE);
            if (needAppGuide && mGuideTip.getVisibility() == View.VISIBLE) {
                mSecurityRL.setVisibility(View.VISIBLE);
            } else {
                mSecurityRL.setVisibility(View.GONE);
            }
        } else {
            mGuideHelpTip.setVisibility(View.GONE);
            mGuideHelpTipBt.setVisibility(View.GONE);
            mGuideTip.setVisibility(View.GONE);
            mLockList.setVisibility(View.VISIBLE);
        }
        if (DBG) {
            mSecurityRL.setVisibility(View.VISIBLE);
        }
    }

    private synchronized void loadData() {
        long start = SystemClock.elapsedRealtime();

        mUnlockRecommendList.clear();
        mUnlockNormalList.clear();
        mUnlockList.clear();
        mLockedList.clear();
        if (mAppList != null && mAppList.size() > 0) {
            mAppList.clear();
        }

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

        try {
            Collections.sort(mLockedList, new LockedAppComparator(lockList));
            Collections.sort(mUnlockRecommendList, new DefalutAppComparator());
            Collections.sort(mUnlockNormalList, new DefalutAppComparator());
        } catch (Exception e) {
        }
        mLockListCount =mLockedList.size();
        mResaultList = new ArrayList<AppInfo>();
        ArrayList<AppInfo> resaultUnlock = new ArrayList<AppInfo>(mUnlockRecommendList);
        resaultUnlock.addAll(mUnlockNormalList);
        mUnlockList = resaultUnlock;

        LockManager lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
        mAppList = lm.getNewAppList();
        boolean processed = PreferenceTable.getInstance().getBoolean(PrefConst.KEY_SCANNED_APP, false);
        if (!processed) {
            mAppList.clear();
        }
//        AppItemInfo appItemInfo = new AppItemInfo();
//        appItemInfo.packageName = "com.android.settings";
//        appItemInfo.label = "设置";
//        appItemInfo.topPos = 10000;
//        AppItemInfo appItemInfo1 = new AppItemInfo();
//        appItemInfo1.packageName = "com.android.dialer";
//        mAppList.add(appItemInfo);
//        mAppList.add(appItemInfo1);
        if (mAppList != null && mAppList.size() > 0) {
            for (int i = 0; i < mAppList.size(); i++) {
                Iterator<AppInfo> iterator = mUnlockRecommendList.iterator();
                while (iterator.hasNext()) {
                    AppInfo info = iterator.next();
                    if (mAppList.get(i).packageName.equals(info.packageName)) {
                        iterator.remove();
                        break;
                    }
                }

            }
            AppInfo labelInfoDownload = new AppInfo();
            labelInfoDownload.label = Constants.LABLE_LIST;
            labelInfoDownload.titleName = Constants.RECENT_DOWNLOAD_LIST;
            mResaultList.add(labelInfoDownload);
            mResaultList.addAll(mAppList);
        }
//        if (mUnlockRecommendList != null && mUnlockRecommendList.size() > 0) {
//            AppInfo labelInfoRecommend = new AppInfo();
//            labelInfoRecommend.label = Constants.LABLE_LIST;
//            labelInfoRecommend.titleName = Constants.RECOMMEND_LOCK_LIST;
//            mResaultList.add(labelInfoRecommend);
//            mResaultList.addAll(mUnlockRecommendList);
//        }
        if ((mUnlockRecommendList != null && mUnlockRecommendList.size() > 0)
                || (mLockedList != null && mLockedList.size() > 0)
                || (mUnlockNormalList != null && mUnlockNormalList.size() > 0)) {
            AppInfo labelInfoOthers = new AppInfo();
            labelInfoOthers.label = Constants.LABLE_LIST;
            labelInfoOthers.titleName = Constants.OTHERS_LOCK_LIST;
            mResaultList.add(labelInfoOthers);
            mResaultList.addAll(mLockedList);
            mResaultList.addAll(mUnlockRecommendList);
            mResaultList.addAll(mUnlockNormalList);
        }

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

        LeoLog.d("testPosition", "position : " + i + ";;" + mLockAdapter.getSwitchs().size());

        //head return
        if (i == 0) return;
        //label return
        List<AppInfo> switchs = mLockAdapter.getSwitchs();
        if (switchs != null && switchs.size() > 0) {
            if (i == 1) {
                return;
            }
        }
        if (mAppList != null && mAppList.size() > 0) {
            if (i == 1 + switchs.size()) {
                return;
            }
        }
        if (mUnlockRecommendList.size() > 0) {
            if (mAppList.size() > 0) {
                if (i == 2 + switchs.size() + mAppList.size()) {
                    return;
                }
            } else {
                if (i == 1 + switchs.size()) {
                    return;
                }
            }
        }
//        if (mUnlockNormalList.size() > 0 || mLockedList.size() > 0) {
//            if (mAppList.size() > 0 && mUnlockRecommendList.size() > 0) {
//                if (i == 3 + switchs.size() + mAppList.size() + mUnlockRecommendList.size()) {
//                    return;
//                }
//            } else if (mAppList.size() == 0 && mUnlockRecommendList.size() == 0) {
//                if (i == 1 + switchs.size()) {
//                    return;
//                }
//            } else if (mAppList.size() > 0 && mUnlockRecommendList.size() == 0) {
//                if (i == 2 + switchs.size() + mAppList.size()) {
//                    return;
//                }
//            } else if (mAppList.size() == 0 && mUnlockRecommendList.size() > 0) {
//                if (i == 2 + switchs.size() + mUnlockRecommendList.size()) {
//                    return;
//                }
//            }
//        }

        ListLockItem lockImageView = (ListLockItem) view.findViewById(R.id.content_item_all);
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

        //switch
        if (mLastSelectApp.packageName.equals(SwitchGroup.WIFI_SWITCH) ||
                mLastSelectApp.packageName.equals(SwitchGroup.BLUE_TOOTH_SWITCH)) {
            switchClick(mLastSelectApp, lockImageView, curMode);
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
            AutoStartGuideList.saveSamSungAppLock();
        }
    }

    private void switchClick(AppInfo appInfo, ListLockItem lockImageView, LockMode curMode) {

        if (appInfo.isLocked) {
            appInfo.isLocked = false;
            if (lockImageView != null) {
                lockImageView.setLockView(false);
                lockImageView.setDescEx(appInfo, false);
            }
            String toast = this.getString(R.string.unlock_app_action, appInfo.label);
            showTextToast(toast);
            SDKWrapper.addEvent(this, SDKWrapper.P1, "app", "unlock_" + curMode.modeName + "_"
                    + appInfo.packageName);

            if (appInfo.packageName.equals(SwitchGroup.WIFI_SWITCH)) {
                wifiSwitch.switchOff(curMode);
            } else {
                blueToothSwitch.switchOff(curMode);
            }

        } else {
            appInfo.isLocked = true;
            if (lockImageView != null) {
                lockImageView.setLockView(true);
                lockImageView.setDescEx(appInfo, true);
            }
            String toast = this.getString(R.string.lock_app_action, appInfo.label);
            showTextToast(toast);
            SDKWrapper.addEvent(this, SDKWrapper.P1, "app", "lock_" + curMode.modeName + "_"
                    + appInfo.packageName);

            if (appInfo.packageName.equals(SwitchGroup.WIFI_SWITCH)) {
                wifiSwitch.switchOn(curMode);
            } else {
                blueToothSwitch.switchOn(curMode);
            }
        }


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
                    String filterTarget = getfilterTarget(intent);
                    try {
                        startActivity(intent);
                        mLockManager.filterSelfOneMinites();
                        if (!TextUtils.isEmpty(filterTarget)) {
                            mLockManager.filterPackage(filterTarget, Constants.TIME_FILTER_TARGET);
                        }
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
                    String filterTarget = getfilterTarget(autoIntent);
                    try {
                        startActivity(autoIntent);
                        mLockManager.filterSelfOneMinites();
                        if (!TextUtils.isEmpty(filterTarget)) {
                            mLockManager.filterPackage(filterTarget, Constants.TIME_FILTER_TARGET);
                        }
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
            case R.id.rv_finish:
                if (mGuideTip.getVisibility() == View.VISIBLE) {
                    openHelp(false, true);
                    Animation animation = AnimationUtils.loadAnimation(AppLockListActivity.this,
                            R.anim.help_tip_show);
                    mGuideHelpTipBt.startAnimation(animation);
                }
                AppMasterPreference.getInstance(this).setLockAndAutoStartGuide(true);
                SDKWrapper.addEvent(this, SDKWrapper.P1, "gd_wcnts", "gd_wcnts_finish");

                if (AutoStartGuideList.SAMSUMG_SYS == AutoStartGuideList.isAutoWhiteListModel(this)) {
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "gd_wcnts", "gd_samsung_cover_fn");
                }
                break;
            case R.id.tip_help_click:
                if (mGuideTip.getVisibility() == View.GONE) {
                    openHelp(true, true);
                } else if (mGuideTip.getVisibility() == View.VISIBLE) {
                    openHelp(false, true);
                }
                AppMasterPreference.getInstance(this).setLockAndAutoStartGuide(true);
                break;
            case R.id.ct_back_rl:
                onBackPressed();
                break;
            case R.id.lock_setting:
                SDKWrapper.addEvent(this, SDKWrapper.P1, "home", "locksetting");
                SDKWrapper.addEvent(this, SDKWrapper.P1, "app_func", "locksetting");
                enterLockSetting();
                break;
            case R.id.lock_mode_type:
                SDKWrapper.addEvent(this, SDKWrapper.P1, "home", "modes");
                SDKWrapper.addEvent(this, SDKWrapper.P1, "app_func", "modes");
                enterLockMode();
                break;
            case R.id.weizhuang_type:
                SDKWrapper.addEvent(this, SDKWrapper.P1, "home", "appcover");
                SDKWrapper.addEvent(this, SDKWrapper.P1, "app_func", "appcover");
                enterWeiZhuang();
                break;
            case R.id.lock_theme_type:
                SDKWrapper.addEvent(this, SDKWrapper.P1, "home", "theme");
                SDKWrapper.addEvent(this, SDKWrapper.P1, "app_func", "theme");
                SDKWrapper.addEvent(this, SDKWrapper.P1, "theme_enter", "home");
                enterLockTheme();
                break;
        }
    }

    private String getfilterTarget(Intent intent) {
        if (intent == null) {
            return null;
        }

        List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(intent, 0);
        String filterTarget = null;
        if (resolveInfos != null && resolveInfos.size() == 1) {
            for (ResolveInfo resolveInfo : resolveInfos) {
                String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
                if (!TextUtils.isEmpty(pkgName)) {
                    filterTarget = pkgName;
                }
            }
        }

        return filterTarget;
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
            if (AutoStartGuideList.SAMSUMG_SYS == AutoStartGuideList.isAutoWhiteListModel(this)) {
                mAutoImage.setImageResource(R.drawable.backstage_protection);

                SDKWrapper.addEvent(this, SDKWrapper.P1, "gd_wcnts", "gd_samsung_cover");

            } else {
                mAutoImage.setImageResource(R.drawable.power_star);
            }
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
//        Toast.makeText(AppLockListActivity.this, Build.VERSION.SDK_INT+"__"+TaskDetectService.sDetectSpecial+"__"+BuildProperties.isLenoveModel(), Toast.LENGTH_SHORT).show();
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
        LeoLog.e("HomeActivity", mLockedList.size() + "");
        int count = 0;
        if (wifiSwitch.isLockNow(mLockManager.getCurLockMode())) {
            count ++;
        }
        if (blueToothSwitch.isLockNow(mLockManager.getCurLockMode())) {
            count ++;
        }
        if (((mFromSuccessListCount != 0 && mLockedList.size() >= mFromSuccessListCount)
                || (mFromSuccessListCount == 0 && mLockedList.size() > mLockListCount)
                ||  count > mWifiAndBluetoothLockCount)
                && !isFromConfrim) {
            LeoEventBus.getDefaultBus().postSticky(new GradeEvent(GradeEvent.FROM_APP, true));
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

        checkNewTheme();
        super.onResume();
        LeoLog.i("TsCost", "AppLockListActivity-onResume: " + (SystemClock.elapsedRealtime() - start));
        updateHelpState();
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

    private boolean isGuideEnough() {
        int guideCount = LeoPreference.getInstance().getInt(PrefConst.KEY_IN_LOCK_GUIDE, 0);
        /*进入应用锁，引导强制提示3*/
        return guideCount > IN_LOCK_GUIDE_COUNT;
    }
}
