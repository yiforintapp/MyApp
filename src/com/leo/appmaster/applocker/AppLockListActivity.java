
package com.leo.appmaster.applocker;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.engine.AppLoadEngine.AppChangeListener;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.model.AppInfo;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.quickgestures.QuickGestureManager.AppLauncherRecorder;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.LeoLockSortPopMenu;
import com.leo.appmaster.ui.LeoPopMenu;
import com.leo.appmaster.ui.LockImageView;
import com.leo.appmaster.ui.PagedGridView;
import com.leo.appmaster.utils.LeoLog;

public class AppLockListActivity extends BaseActivity implements
        AppChangeListener, OnItemClickListener, OnClickListener {

    public LayoutInflater mInflater;
    private TextView mTvModeName;
    private ImageView mIvBack, mIvSortSelected;
    private View mLyoutModeName, mMaskLayer;
    private List<AppInfo> mLockedList;
    private List<AppInfo> mUnlockList;
    private PagedGridView mAppPager;
    private LeoPopMenu mLeoPopMenu;
    private LeoLockSortPopMenu mLeoLockSortPopMenu;

    private AppInfo mLastSelectApp;
    private String[] mSortType;

    public static final int DEFAULT_SORT = 0;
    public static final int NAME_SORT = 1;
    public static final int INSTALL_TIME_SORT = 2;
    private int mCurSortType = DEFAULT_SORT;
    private static final String FROM_DEFAULT_RECOMMENT_ACTIVITY = "applocklist_activity";

    private int mType = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_app_list);
        AppLoadEngine.getInstance(this).registerAppChangeListener(this);
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
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mType == StatusBarEventService.EVENT_EMPTY) {
            if (mMaskLayer != null && mMaskLayer.getVisibility() == View.VISIBLE) {
                mMaskLayer.setVisibility(View.GONE);
            } else {

                boolean fromLockMore = getIntent().getBooleanExtra("from_lock_more", false);
                Log.e("lockmore", "fromLockMore==" + fromLockMore);
                if (fromLockMore)
                {
                    LockManager.getInstatnce().timeFilter(getPackageName(), 1000);
                    Intent intent = new Intent(this, HomeActivity.class);
                    if (AppMasterPreference.getInstance(this).getIsHomeToLockList())
                    {
                        Log.e("lockmore", "inif is home");
                        AppMasterPreference.getInstance(this).setIsFromLockList(true);
                    }
                    Log.e("lockmore", "settrue");
                    startActivity(intent);

                }
                else
                {
                    if (AppMasterPreference.getInstance(this).getIsHomeToLockList())
                    {
                        Log.e("lockmore", "inif is home");
                        Log.e("lockmore", "settrue");
                        AppMasterPreference.getInstance(this).setIsFromLockList(true);
                    }
                }
                super.onBackPressed();
            }
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
        if (AppMasterPreference.getInstance(this).isFisrtUseLocker()) {
            mMaskLayer = findViewById(R.id.mask_layer);
            mMaskLayer.setOnClickListener(this);
            mMaskLayer.setVisibility(View.VISIBLE);
            AppMasterPreference.getInstance(this).setLockerUsed();
        }

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
    }

    private void loadData() {
        if (AppMasterPreference.getInstance(this).isFisrtUseLocker()) {
            mMaskLayer.setVisibility(View.VISIBLE);
            mMaskLayer.setOnClickListener(this);
        }
        mUnlockList.clear();
        mLockedList.clear();
        ArrayList<AppItemInfo> list = AppLoadEngine.getInstance(this)
                .getAllPkgInfo();
        List<String> lockList = LockManager.getInstatnce().getCurLockList();
        for (AppItemInfo appDetailInfo : list) {
            if (appDetailInfo.packageName.equals(this.getPackageName()))
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

        mLastSelectApp = (AppInfo) view.getTag();
        AppInfo info = null;
        if (mLastSelectApp.isLocked) {
            mLastSelectApp.isLocked = false;
            for (AppInfo baseInfo : mLockedList) {
                if (baseInfo.packageName.equals(mLastSelectApp.packageName)) {
                    info = baseInfo;
                    info.isLocked = false;
                    break;
                }
            }
            mUnlockList.add(info);

            mLockedList.remove(info);
            List<String> list = new LinkedList<String>();
            list.add(info.packageName);
            lm.removePkgFromMode(list, lm.getCurLockMode());

            // to set view unlocked
            ((LockImageView) view.findViewById(R.id.iv_app_icon))
                    .setLocked(false);
            SDKWrapper.addEvent(this, SDKWrapper.P1, "app", "unlock_" + curMode.modeName + "_"
                    + mLastSelectApp.packageName);
        } else {
            mLastSelectApp.isLocked = true;
            for (AppInfo baseInfo : mUnlockList) {
                if (baseInfo.packageName.equals(mLastSelectApp.packageName)) {
                    info = baseInfo;
                    info.isLocked = true;
                    break;
                }
            }
            mLockedList.add(0, info);
            mUnlockList.remove(info);

            List<String> list = new LinkedList<String>();
            list.add(info.packageName);
            lm.addPkg2Mode(list, lm.getCurLockMode());

            // to set view lock
            ((LockImageView) view.findViewById(R.id.iv_app_icon))
                    .setLocked(true);

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

                        List<String> list = mLeoPopMenu.getPopMenuItems();
                        String selectMode = list.get(position);
                        if (TextUtils.equals(selectMode, getString(R.string.add_new_mode))) {
                            addLockMode();
                            SDKWrapper.addEvent(getApplicationContext(), SDKWrapper.P1, "modesadd",
                                    "applock");
                            mLeoPopMenu.dismissSnapshotList();
                        } else {
                            LockManager lm = LockManager.getInstatnce();
                            List<LockMode> lockModes = lm.getLockMode();
                            for (LockMode lockMode : lockModes) {
                                if (TextUtils.equals(selectMode, lockMode.modeName)) {
                                    if (lockMode.defaultFlag == 1
                                            && !lockMode.haveEverOpened) {
                                        lm.setCurrentLockMode(lockMode, true);
                                        SDKWrapper.addEvent(getApplicationContext(), SDKWrapper.P1,
                                                "modeschage", "applock");
                                        startRcommendLock();
                                        lockMode.haveEverOpened = true;
                                        lm.updateMode(lockMode);
                                    } else {
                                        lm.setCurrentLockMode(lockMode, true);
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
                mLeoPopMenu.setPopMenuItems(this, getLockModeMenuItems());
                mLeoPopMenu.showPopMenu(this, mIvBack, null, null);
                break;
            case R.id.mask_layer:
                mMaskLayer.setVisibility(View.INVISIBLE);
                AppMasterPreference.getInstance(this).setLockerUsed();
                break;
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
        return listItems;
    }

    private List<String> getLockModeMenuItems() {
        List<String> listItems = new ArrayList<String>();
        List<LockMode> lockModes = LockManager.getInstatnce().getLockMode();
        LockMode curMode = LockManager.getInstatnce().getCurLockMode();
        if (curMode != null) {
            for (LockMode lockMode : lockModes) {
                if (!TextUtils.equals(lockMode.modeName, curMode.modeName)) {
                    listItems.add(lockMode.modeName);
                    Log.i("null", lockMode.modeName);
                }
            }
        }

        listItems.add(getString(R.string.add_new_mode));
        return listItems;
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
