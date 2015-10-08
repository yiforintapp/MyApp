
package com.leo.appmaster.applocker;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.model.LocationLock;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.applocker.model.ProcessDetector;
import com.leo.appmaster.applocker.model.ProcessDetectorUsageStats;
import com.leo.appmaster.applocker.model.TimeLock;
import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.engine.AppLoadEngine.AppChangeListener;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.LockModeEvent;
import com.leo.appmaster.home.AutoStartGuideList;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.model.AppInfo;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.LeoLockSortPopMenu;
import com.leo.appmaster.ui.LeoPopMenu;
import com.leo.appmaster.ui.LockImageView;
import com.leo.appmaster.ui.PagedGridView;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.ui.dialog.LEOThreeButtonDialog;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.LeoLog;

public class AppLockListActivity extends BaseActivity implements
        AppChangeListener, OnItemClickListener, OnClickListener {

    public LayoutInflater mInflater;
    private TextView mTvModeName;
    private ImageView mIvBack, mIvSortSelected, mGuideHelpTipBt;
    private View mLyoutModeName, mMaskLayer;
    private List<AppInfo> mLockedList;
    private List<AppInfo> mUnlockList;
    private PagedGridView mAppPager;
    private LeoPopMenu mLeoPopMenu;
    private LeoLockSortPopMenu mLeoLockSortPopMenu;
    private AppInfo mLastSelectApp;
    private String[] mSortType;
    private TextView mSecurityGuideBt, mAutoGuideBt, mBackageGroundBt;
    private Button mFinishBt;
    private RelativeLayout mSecurityRL, mAutoRL, mBackgroundRL;
    private TextView mSecurityText, mAutoText, mBackGroudText;
    private View mGuideTip;
    public static final int DEFAULT_SORT = 0;
    public static final int NAME_SORT = 1;
    public static final int INSTALL_TIME_SORT = 2;
    private int mCurSortType = DEFAULT_SORT;
    private static final String FROM_DEFAULT_RECOMMENT_ACTIVITY = "applocklist_activity";
    private static final boolean DBG = false;
    

    private int mType = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_app_list);
        AppLoadEngine.getInstance(this).registerAppChangeListener(this);
        LeoEventBus.getDefaultBus().register(this);
        handleIntent();
        initUI();
        loadData();
    }

    private void handleIntent() {
        Intent intent = getIntent();
        mType = intent.getIntExtra(StatusBarEventService.EXTRA_EVENT_TYPE,
                StatusBarEventService.EVENT_EMPTY);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        AppLoadEngine.getInstance(this).unregisterAppChangeListener(this);
        // 解决内存泄露
        LeoEventBus.getDefaultBus().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mType == StatusBarEventService.EVENT_EMPTY) {
//            if (mMaskLayer != null && mMaskLayer.getVisibility() == View.VISIBLE) {
//                mMaskLayer.setVisibility(View.GONE);
//            } else {

                boolean fromLockMore = getIntent().getBooleanExtra("from_lock_more", false);
                LeoLog.e("lockmore", "fromLockMore==" + fromLockMore);
                boolean isStartFromLockmode = getIntent().getBooleanExtra("enter_from_lockmode",
                        false);
                LeoLog.e("lockmore", "isStartFromLockmode==" + isStartFromLockmode);
                // if(isStartFromLockmode)
                // {
                //
                // // Intent intent = new Intent(this, LockModeActivity.class);
                // // startActivity(intent);
                // this.finish();
                // }
                // else
                if (fromLockMore) {
                    LockManager.getInstatnce().timeFilter(getPackageName(), 1000);
                    Intent intent = new Intent(this, HomeActivity.class);
                    if (AppMasterPreference.getInstance(this).getIsHomeToLockList()
                            || AppMasterPreference.getInstance(this).getIsClockToLockList()) {
                        LeoLog.e("lockmore", "inif is home");
                        AppMasterPreference.getInstance(this).setIsFromLockList(true);
                    }
                    Log.e("lockmore", "settrue");
                    startActivity(intent);

                } else {
                    if (AppMasterPreference.getInstance(this).getIsHomeToLockList()
                            || AppMasterPreference.getInstance(this).getIsClockToLockList()) {
                        LeoLog.e("lockmore", "inif is home");
                        LeoLog.e("lockmore", "settrue");
                        AppMasterPreference.getInstance(this).setIsFromLockList(true);
                    }
                }
                super.onBackPressed();
//            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {

        }
    }

    private void initUI() {

        mSortType = getResources().getStringArray(R.array.sort_type);
        mCurSortType = AppMasterPreference.getInstance(this).getSortType();

        mInflater = LayoutInflater.from(this);
//        if (AppMasterPreference.getInstance(this).isFisrtUseLocker()) {
//            mMaskLayer = findViewById(R.id.mask_layer);
//            mMaskLayer.setOnClickListener(this);
//            mMaskLayer.setVisibility(View.VISIBLE);
//            AppMasterPreference.getInstance(this).setLockerUsed();
//        }
        mIvBack = (ImageView) findViewById(R.id.iv_back);
        mLyoutModeName = findViewById(R.id.mode_select_layout);
        mTvModeName = (TextView) findViewById(R.id.mode_name_tv);
        mIvSortSelected = (ImageView) findViewById(R.id.iv_sort_select);

        mIvBack.setOnClickListener(this);
        mLyoutModeName.setOnClickListener(this);
        mIvSortSelected.setOnClickListener(this);
        LockMode lm = LockManager.getInstatnce().getCurLockMode();
        if (lm != null) {
            mTvModeName.setText(lm.modeName);
        }

        mLockedList = new ArrayList<AppInfo>();
        mUnlockList = new ArrayList<AppInfo>();
        mAppPager = (PagedGridView) findViewById(R.id.pager_unlock);
        mAppPager.setItemClickListener(this);
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
        mGuideHelpTipBt = (ImageView) findViewById(R.id.tip_help);
        /* 系统是否大于Android5.1 */
        boolean androidApiMore = BuildProperties.isMoreAndroid22();
        /* 是否存在于白名单:-1-----不存在百名单 */
        int model = AutoStartGuideList.isAutoWhiteListModel(this);
        if (DBG) {
            model = AutoStartGuideList.HUAWEIP7_PLUS;
        }
        if (model == -1 && !androidApiMore) {
            mGuideHelpTipBt.setVisibility(View.GONE);
        } else {
            mGuideHelpTipBt.setOnClickListener(this);
        }
        mSecurityText = (TextView) findViewById(R.id.security_guide_text);
        mAutoText = (TextView) findViewById(R.id.auto_guide_text);
        mBackGroudText = (TextView) findViewById(R.id.background_guide_text);

        mSecurityGuideBt.setVisibility(needGuide() ? View.VISIBLE : View.GONE);
        /* 锁提示蒙层消失，引导蒙层显示 */
        boolean  isShowLockAutoTip=AppMasterPreference.getInstance(this).getLockAndAutoStartGuide();
        if(!isShowLockAutoTip){
            setGuideTipShow();
        }
        
    }

    private void loadData() {
//        if (AppMasterPreference.getInstance(this).isFisrtUseLocker()) {
//            mMaskLayer.setVisibility(View.VISIBLE);
//            mMaskLayer.setOnClickListener(this);
//        }
        mUnlockList.clear();
        mLockedList.clear();
        ArrayList<AppItemInfo> list = AppLoadEngine.getInstance(this)
                .getAllPkgInfo();
        List<String> lockList = LockManager.getInstatnce().getCurLockList();

        ProcessDetector detector = new ProcessDetector();
        for (AppItemInfo appDetailInfo : list) {
            if (appDetailInfo.packageName.equals(this.getPackageName())
                    || appDetailInfo.packageName.equals(Constants.CP_PACKAGE)
                    || appDetailInfo.packageName.equals(Constants.ISWIPE_PACKAGE)
                    || appDetailInfo.packageName.equals(Constants.SEARCH_BOX_PACKAGE)
                    || detector.isHomePackage(appDetailInfo.packageName))
                continue;
            if (lockList.contains(appDetailInfo.packageName)) {
                appDetailInfo.isLocked = true;
                mLockedList.add(appDetailInfo);

            } else {
                appDetailInfo.isLocked = false;
                mUnlockList.add(appDetailInfo);
            }
        }
        Collections.sort(mLockedList, new LockedAppComparator(lockList));
        if (mCurSortType == DEFAULT_SORT) {
            Collections.sort(mUnlockList, new DefalutAppComparator());
        } else if (mCurSortType == NAME_SORT) {
            Collections.sort(mUnlockList, new NameComparator());
        } else if (mCurSortType == INSTALL_TIME_SORT) {
            Collections.sort(mUnlockList, new InstallTimeComparator());
        }

        ArrayList<AppInfo> resault = new ArrayList<AppInfo>(mLockedList);
        resault.addAll(mUnlockList);

        int rowCount = getResources().getInteger(R.integer.gridview_row_count);
        mAppPager.setDatas(resault, 4, rowCount);
        mAppPager.setFlag(FROM_DEFAULT_RECOMMENT_ACTIVITY);
    }

    private void addLockMode() {
        Intent intent = new Intent(this, LockModeEditActivity.class);
        intent.putExtra("mode_name", getString(R.string.new_mode));
        intent.putExtra("mode_id", -1);
        intent.putExtra("new_mode", true);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        animateItem(view);
        LockManager lm = LockManager.getInstatnce();
        LockMode curMode = lm.getCurLockMode();
        if (curMode == null || curMode.defaultFlag == 0) {
            Toast.makeText(this, R.string.unlock_all_mode_tip, Toast.LENGTH_SHORT).show();
            return;
        }

        if (view == null)
            return;

        mLastSelectApp = (AppInfo) view.getTag();
        if (mLastSelectApp == null)
            return;

        AppInfo info = null;
        if (mLastSelectApp.isLocked) {
            mLastSelectApp.isLocked = false;
            for (AppInfo baseInfo : mLockedList) {
                if (baseInfo.packageName != null
                        && baseInfo.packageName.equals(mLastSelectApp.packageName)) {
                    info = baseInfo;
                    info.isLocked = false;
                    break;
                }
            }

            if (info == null)
                return;

            if (!mUnlockList.contains(info)) {
                mUnlockList.add(info);
            }
            if (mLockedList.contains(info)) {
                mLockedList.remove(info);
            }
            List<String> list = new LinkedList<String>();
            list.add(info.packageName);
            lm.removePkgFromMode(list, lm.getCurLockMode());

            // to set view unlocked
            LockImageView lockImageView = (LockImageView) view.findViewById(R.id.iv_app_icon);
            if (lockImageView != null) {
                lockImageView.setLocked(false);
            }
            SDKWrapper.addEvent(this, SDKWrapper.P1, "app", "unlock_" + curMode.modeName + "_"
                    + mLastSelectApp.packageName);
        } else {
            mLastSelectApp.isLocked = true;
            for (AppInfo baseInfo : mUnlockList) {
                if (baseInfo.packageName != null
                        && baseInfo.packageName.equals(mLastSelectApp.packageName)) {
                    info = baseInfo;
                    info.isLocked = true;
                    break;
                }
            }
            if (info == null)
                return;

            if (mLockedList.contains(info)) {
                mLockedList.remove(info);
            }
            mLockedList.add(0, info);
            if (mUnlockList.contains(info)) {
                mUnlockList.remove(info);
            }

            List<String> list = new LinkedList<String>();
            list.add(info.packageName);
            lm.addPkg2Mode(list, lm.getCurLockMode());

            // to set view lock
            LockImageView lockImageView = (LockImageView) view.findViewById(R.id.iv_app_icon);
            if (lockImageView != null) {
                lockImageView.setLocked(true);
            }
            SDKWrapper.addEvent(this, SDKWrapper.P1, "app", "lock_" + curMode.modeName + "_"
                    + mLastSelectApp.packageName);
        }
        // saveLockList();
    }

    private void animateItem(View view) {

        AnimatorSet as = new AnimatorSet();
        as.setDuration(300);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f,
                0.8f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f,
                0.8f, 1f);
        as.playTogether(scaleX, scaleY);
        as.start();
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.iv_sort_select:
                if (mLeoLockSortPopMenu == null) {
                    mLeoLockSortPopMenu = new LeoLockSortPopMenu();
                }
                mLeoLockSortPopMenu.setAnimation(R.style.RightEnterAnim);
                mLeoLockSortPopMenu.setPopItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                        /*如果提示界面存在，让其消失*/
                        if (mGuideTip.getVisibility() == View.VISIBLE) {
                            mGuideTip.setVisibility(View.GONE);
                            mGuideTip.startAnimation(AnimationUtils
                                    .loadAnimation(AppLockListActivity.this, R.anim.lock_mode_guide_out));
                        }
                        AppMasterPreference.getInstance(AppLockListActivity.this).setLockAndAutoStartGuide(true);
                        if (position == 0) {
                            mCurSortType = DEFAULT_SORT;
                        } else if (position == 1) {
                            mCurSortType = NAME_SORT;
                        } else if (position == 2) {
                            mCurSortType = INSTALL_TIME_SORT;
                        }
                        loadData();
                        AppMasterPreference.getInstance(
                                AppLockListActivity.this).setSortType(
                                        mCurSortType);
                        if (mLeoLockSortPopMenu != null) {
                            mLeoLockSortPopMenu.dismissSnapshotList();
                        }
                    }
                });
                mLeoLockSortPopMenu.setPopMenuItems(this, getSortMenuItems(), mCurSortType);
                mLeoLockSortPopMenu.showPopMenu(this,
                        mIvSortSelected, null, null);
                break;
            case R.id.mode_select_layout:
                if (mLeoPopMenu == null) {
                    mLeoPopMenu = new LeoPopMenu();
                }
                mLeoPopMenu.setAnimation(R.style.CenterEnterAnim);
                mLeoPopMenu.setPopItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                        /*如果提示界面存在，让其消失*/
                        if (mGuideTip.getVisibility() == View.VISIBLE) {
                            mGuideTip.setVisibility(View.GONE);
                            mGuideTip.startAnimation(AnimationUtils
                                    .loadAnimation(AppLockListActivity.this, R.anim.lock_mode_guide_out));
                        }
                        AppMasterPreference.getInstance(AppLockListActivity.this).setLockAndAutoStartGuide(true);
                        List<Integer> list = mLeoPopMenu.getPopMenuItemIds();
                        int selectModeID = list.get(position);
                        if (selectModeID == LockMode.MODE_OTHER) { // add new
                                                                   // mode item
                            addLockMode();
                            SDKWrapper.addEvent(getApplicationContext(), SDKWrapper.P1, "modesadd",
                                    "applock");
                            mLeoPopMenu.dismissSnapshotList();
                        } else {
                            LockManager lm = LockManager.getInstatnce();
                            List<LockMode> lockModes = lm.getLockMode();
                            for (LockMode lockMode : lockModes) {
                                if (selectModeID == lockMode.modeId) {// the
                                                                      // first
                                                                      // time
                                                                      // show
                                                                      // lock
                                                                      // app
                                                                      // list
                                    if (lockMode.defaultFlag == 1
                                            && !lockMode.haveEverOpened) {
                                        lm.setCurrentLockMode(lockMode, true);
                                        checkLockTip();
                                        SDKWrapper.addEvent(getApplicationContext(), SDKWrapper.P1,
                                                "modeschage", "applock");
                                        startRcommendLock();
                                        lockMode.haveEverOpened = true;
                                        lm.updateMode(lockMode);
                                    } else {
                                        lm.setCurrentLockMode(lockMode, true);
                                        checkLockTip();
                                        SDKWrapper.addEvent(getApplicationContext(), SDKWrapper.P1,
                                                "modeschage", "applock");
                                        Toast.makeText(
                                                AppLockListActivity.this,
                                                AppLockListActivity.this.getString(
                                                        R.string.mode_change, lockMode.modeName),
                                                Toast.LENGTH_SHORT).show();
                                    }

                                    break;
                                }
                            }

                            loadData();
                            LockMode lockMode = lm.getCurLockMode();
                            if (lockMode != null) {
                                mTvModeName.setText(lockMode.modeName);
                            }
                            mLeoPopMenu.dismissSnapshotList();
                        }
                    }
                });
                mLeoPopMenu.setPopMenuItems(this, getLockModeMenuMapItems());
                mLeoPopMenu.showPopMenu(this, mIvBack, null, null);
                break;
            case R.id.mask_layer:
//                mMaskLayer.setVisibility(View.INVISIBLE);
//                AppMasterPreference.getInstance(this).setLockerUsed();
                break;
            case R.id.security_guide_button:
                /* Android5.01+ */
                ProcessDetectorUsageStats usageStats = new ProcessDetectorUsageStats();
                if (!usageStats.checkAvailable()) {
                    Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    try {
                        LockManager.getInstatnce().timeFilterSelf();
                        startActivity(intent);
                    } catch (Exception e) {
                    }
                }
                AppMasterPreference.getInstance(this).setLockAndAutoStartGuide(true);
                break;
            case R.id.auto_guide_button:
                int model = AutoStartGuideList.isAutoWhiteListModel(this);
                /* 华为P7类rom */
                if (AutoStartGuideList.HUAWEIP7_PLUS == model) {
                    Intent autoIntent = new Intent();
                    autoIntent.setAction("android.intent.action.MAIN");
                    ComponentName autoCn = new ComponentName("com.huawei.systemmanager",
                            "com.huawei.systemmanager.optimize.bootstart.BootStartActivity");
                    autoIntent.setComponent(autoCn);
                    LockManager.getInstatnce().timeFilterSelf();
                    startActivity(autoIntent);
                } else {
                    new AutoStartGuideList().executeGuide();
                }
                AppMasterPreference.getInstance(this).setLockAndAutoStartGuide(true);
                break;
            case R.id.background_guide_button:
                new AutoStartGuideList().executeGuide();
                break;
            case R.id.finish:
                if (mGuideTip.getVisibility() == View.VISIBLE) {
                    mGuideTip.setVisibility(View.GONE);
                    mGuideTip.startAnimation(AnimationUtils
                            .loadAnimation(AppLockListActivity.this, R.anim.lock_mode_guide_out));
                    Animation animation = AnimationUtils.loadAnimation(AppLockListActivity.this,
                            R.anim.help_tip_show);
                    mGuideHelpTipBt.startAnimation(animation);
                }
                AppMasterPreference.getInstance(this).setLockAndAutoStartGuide(true);
                break;
            case R.id.tip_help:
                if (mGuideTip.getVisibility() == View.GONE) {
                    mGuideTip.setVisibility(View.VISIBLE);
                    Animation animation = AnimationUtils.loadAnimation(AppLockListActivity.this,
                            R.anim.lock_mode_guide_in);
                    mGuideTip.startAnimation(animation);
                    setGuideTipShow();
                } else if (mGuideTip.getVisibility() == View.VISIBLE) {
                    mGuideTip.setVisibility(View.GONE);
                    mGuideTip.startAnimation(AnimationUtils
                            .loadAnimation(AppLockListActivity.this, R.anim.lock_mode_guide_out));
//                    Animation animation = AnimationUtils.loadAnimation(AppLockListActivity.this,
//                            R.anim.help_tip_show);
//                    mGuideHelpTipBt.startAnimation(animation);
                }
                AppMasterPreference.getInstance(this).setLockAndAutoStartGuide(true);
                break;
        }
    }

    private void setGuideTipShow() {
        /* 是否为Android5.1+ */
        boolean moreAndroid22 = BuildProperties.isMoreAndroid22();
        /* 是否存在于白名单，返回值为-1,则不再白名单中 */
        int model = AutoStartGuideList.isAutoWhiteListModel(this);
        /*联想k50android5.1以上不用显示应用锁提示*/
        boolean lenovo = BuildProperties.isLenoveModel();
        if (moreAndroid22 && !lenovo) {
            mGuideTip.setVisibility(View.VISIBLE);
            mSecurityRL.setVisibility(View.VISIBLE);
        } else {
            mSecurityRL.setVisibility(View.GONE);
            mGuideTip.setVisibility(View.GONE);
        }
        if (DBG) {
            model = AutoStartGuideList.HUAWEIP7_PLUS;
        }
        if (model != -1) {
            mGuideTip.setVisibility(View.VISIBLE);
            int content = AutoStartGuideList
                    .getAutoWhiteListTipText(AppMasterApplication.getInstance());
            mAutoText.setText(content);
            mAutoRL.setVisibility(View.VISIBLE);
            mBackgroundRL.setVisibility(View.GONE);

            /* 查询是否为双提示打开系统权限的机型 */
            if (AutoStartGuideList.isDoubleTipOPenPhone(model)) {
                mBackgroundRL.setVisibility(View.VISIBLE);
                mAutoText.setText(R.string.auto_start_tip_text_huawei_plus);
                mBackGroudText.setText(content);
            }
        } else {
            mAutoRL.setVisibility(View.GONE);
            mBackgroundRL.setVisibility(View.GONE);
//            mGuideTip.setVisibility(View.GONE);
        }
    }

    private boolean needGuide() {
        return BuildProperties.isMoreAndroid22()
                || AutoStartGuideList.isAutoWhiteListModel(this) != -1;
    }

    public void onEventMainThread(LockModeEvent event) {
        LockManager lm = LockManager.getInstatnce();
        LockMode lockMode = lm.getCurLockMode();
        loadData();
        if (lockMode != null) {
            mTvModeName.setText(lockMode.modeName);
        }
    }

    private void checkLockTip() {
        int switchCount = AppMasterPreference.getInstance(this).getSwitchModeCount();
        switchCount++;
        AppMasterPreference.getInstance(this).setSwitchModeCount(switchCount);
        LockManager lm = LockManager.getInstatnce();
        List<TimeLock> timeLockList = lm.getTimeLock();
        List<LocationLock> locationLockList = lm.getLocationLock();
        if (switchCount == 6) {
            // TODO show tip
            int timeLockCount = timeLockList.size();
            int locationLockCount = locationLockList.size();

            if (timeLockCount == 0 && locationLockCount == 0) {
                // show three btn dialog
                LEOThreeButtonDialog dialog = new LEOThreeButtonDialog(
                        this);
                dialog.setTitle(R.string.time_location_lock_tip_title);
                String tip = this.getString(R.string.time_location_lock_tip_content);
                dialog.setContent(tip);
                dialog.setLeftBtnStr(this.getString(R.string.cancel));
                dialog.setMiddleBtnStr(this.getString(R.string.lock_mode_time));
                dialog.setRightBtnStr(this.getString(R.string.lock_mode_location));
                dialog.setRightBtnBackground(R.drawable.manager_mode_lock_third_button_selecter);
                dialog.setOnClickListener(new LEOThreeButtonDialog.OnDiaogClickListener() {
                    @Override
                    public void onClick(int which) {
                        Intent intent = null;
                        if (which == 0) {
                            // cancel
                        } else if (which == 1) {
                            // new time lock
                            intent = new Intent(AppLockListActivity.this,
                                    TimeLockEditActivity.class);
                            intent.putExtra("new_time_lock", true);
                            intent.putExtra("from_dialog", true);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            AppLockListActivity.this.startActivity(intent);
                        } else if (which == 2) {
                            // new location lock
                            intent = new Intent(AppLockListActivity.this,
                                    LocationLockEditActivity.class);
                            intent.putExtra("new_location_lock", true);
                            intent.putExtra("from_dialog", true);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            AppLockListActivity.this.startActivity(intent);
                        }
                    }
                });
                // dialog.getWindow().setType(
                // WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                dialog.show();
            } else {
                if (timeLockCount == 0 && locationLockCount != 0) {
                    // show time lock btn dialog
                    LEOAlarmDialog dialog = new LEOAlarmDialog(this);
                    dialog.setTitle(R.string.time_location_lock_tip_title);
                    String tip = this.getString(R.string.time_location_lock_tip_content);
                    dialog.setContent(tip);
                    dialog.setRightBtnStr(this.getString(R.string.lock_mode_time));
                    dialog.setRightBtnBackground(R.drawable.manager_right_contact_button_selecter);
                    dialog.setLeftBtnStr(this.getString(R.string.cancel));
                    dialog.setOnClickListener(new OnDiaogClickListener() {
                        @Override
                        public void onClick(int which) {
                            Intent intent = null;
                            if (which == 0) {
                                // cancel
                            } else if (which == 1) {
                                // new time lock
                                intent = new Intent(AppLockListActivity.this,
                                        TimeLockEditActivity.class);
                                intent.putExtra("new_time_lock", true);
                                intent.putExtra("from_dialog", true);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                AppLockListActivity.this.startActivity(intent);
                            }

                        }
                    });
                    dialog.getWindow().setType(
                            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    dialog.show();

                } else if (timeLockCount != 0 && locationLockCount == 0) {
                    // show lcaotion btn dialog
                    LEOAlarmDialog dialog = new LEOAlarmDialog(this);
                    dialog.setTitle(R.string.time_location_lock_tip_title);
                    String tip = this.getString(R.string.time_location_lock_tip_content);
                    dialog.setContent(tip);
                    dialog.setRightBtnStr(this.getString(R.string.lock_mode_location));
                    dialog.setRightBtnBackground(R.drawable.manager_right_contact_button_selecter);
                    dialog.setLeftBtnStr(this.getString(R.string.cancel));
                    dialog.setOnClickListener(new OnDiaogClickListener() {
                        @Override
                        public void onClick(int which) {
                            if (which == 0) {
                                // cancel
                            } else if (which == 1) {
                                // new time lock
                                Intent intent = new Intent(AppLockListActivity.this,
                                        LocationLockEditActivity.class);
                                intent.putExtra("new_location_lock", true);
                                intent.putExtra("from_dialog", true);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                AppLockListActivity.this.startActivity(intent);
                            }

                        }
                    });
                    // dialog.getWindow().setType(
                    // WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    dialog.show();
                }
            }
        }
    }

    private void startRcommendLock() {
        Intent intent = new Intent(this, RecommentAppLockListActivity.class);
        startActivity(intent);
    }

    private List<String> getSortMenuItems() {
        List<String> listItems = new ArrayList<String>();
        listItems.add(mSortType[DEFAULT_SORT]);
        listItems.add(mSortType[NAME_SORT]);
        listItems.add(mSortType[INSTALL_TIME_SORT]);

        ProcessDetectorUsageStats usageStats = new ProcessDetectorUsageStats();
        if (!usageStats.checkAvailable()) {
            listItems.add("开启权限");
        }
        return listItems;
    }

    /**
     * return a map,the key is modeId,and value is modeName
     * 
     * @return
     */
    private Map<Integer, String> getLockModeMenuMapItems() {
        Map<Integer, String> lockMap = new LinkedHashMap<Integer, String>();
        List<LockMode> lockModes = LockManager.getInstatnce().getLockMode();
        LockMode curMode = LockManager.getInstatnce().getCurLockMode();
        if (curMode != null) {
            for (LockMode lockMode : lockModes) {
                if (lockMode.modeId != curMode.modeId) {
                    lockMap.put(lockMode.modeId, lockMode.modeName);
                    // Log.i("mode", lockMode.modeName + " -->
                    // "+lockMode.modeId);
                }
            }
        }
        lockMap.put(LockMode.MODE_OTHER, getString(R.string.add_new_mode));
        return lockMap;
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    public static class InstallTimeComparator implements
            Comparator<AppInfo> {

        @Override
        public int compare(AppInfo lhs, AppInfo rhs) {
            if (lhs.installTime > rhs.installTime) {
                return -1;
            } else if (lhs.installTime < rhs.installTime) {
                return 1;
            } else {
                return Collator.getInstance().compare(trimString(lhs.label),
                        trimString(rhs.label));
            }
        }

        private String trimString(String s) {
            return s.replaceAll("\u00A0", "").trim();
        }
    }

    public static class DefalutAppComparator implements Comparator<AppInfo> {
        @Override
        public int compare(AppInfo lhs, AppInfo rhs) {
            if (lhs.topPos > -1 && rhs.topPos < 0) {
                return -1;
            } else if (lhs.topPos < 0 && rhs.topPos > -1) {
                return 1;
            } else if (lhs.topPos > -1 && rhs.topPos > -1) {
                return lhs.topPos - rhs.topPos;
            }

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

}
