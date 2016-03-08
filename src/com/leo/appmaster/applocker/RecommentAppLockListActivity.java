package com.leo.appmaster.applocker;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.applocker.model.ProcessDetector;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.home.AutoStartGuideList;
import com.leo.appmaster.model.AppInfo;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.MaterialRippleLayout;
import com.leo.appmaster.ui.RippleView;

/**
 * Created by qili on 15-10-11.
 */
public class RecommentAppLockListActivity extends BaseActivity implements
        AppLoadEngine.AppChangeListener, OnItemClickListener, OnClickListener {
    public final static String FROM_DEFAULT_RECOMMENT_ACTIVITY = "recomment_activity";
    public final static String RECOMMEND_FROM_LOCK = "new_app_install_lock";
    public final static String RECOMMEND_FROM_LOCK_MORE = "new_app_install_lock_more";
    public final static String RECOMMEND_FROM_VISITED_MODE = "first_in_from_visit_mode";
    public final static int INIT_UI_DONE = 10;
    public final static int LOAD_DATA_DONE = 11;

    private ListView mLockListView;
    private CommonToolbar mTtileBar;
    private RippleView lockTV;
    private ListAppLockAdapter mLockAdapter;
    private List<AppInfo> mLockList;
    private List<AppInfo> mUnLockList;
    private ArrayList<AppInfo> mResaultList;
    private String mFrom;
    private String mInstallPackageName;
    private ProgressBar mProgressBar;

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
        mProgressBar.setVisibility(View.GONE);
        mLockListView.setVisibility(View.VISIBLE);
        if (mLockAdapter != null) {
            mLockAdapter.setFlag(FROM_DEFAULT_RECOMMENT_ACTIVITY);
            if (mResaultList != null) {
                mLockAdapter.setData(mResaultList);
            }
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
        setContentView(R.layout.activity_list_recomment_lockapp);
        getIntentFrom();
        initUI();
        mHandler.sendEmptyMessage(INIT_UI_DONE);
//        loadData();
    }

    private void initUI() {
        mTtileBar = (CommonToolbar) findViewById(R.id.recomment_title_bar);
        mTtileBar.setToolbarTitle(R.string.app_lock);
        mTtileBar.setToolbarColorResource(R.color.cb);
        mTtileBar.setOptionMenuVisible(false);

        lockTV = (RippleView) findViewById(R.id.recomment_lock);
        lockTV.setOnClickListener(this);

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View headView = layoutInflater.inflate(R.layout.recom_ac_head, null);

        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading_recomment);
        mLockListView = (ListView) findViewById(R.id.recomment_app_list);
        mLockListView.setOnItemClickListener(this);
        mLockListView.addHeaderView(headView);
        mLockAdapter = new ListAppLockAdapter(this);
        mLockListView.setAdapter(mLockAdapter);

        mLockList = new ArrayList<AppInfo>();
        mUnLockList = new ArrayList<AppInfo>();
    }

    private void loadData() {
        ArrayList<AppItemInfo> localAppList = AppLoadEngine.getInstance(this).getAllPkgInfo();
        List<String> defaultLockList = AppLoadEngine.getInstance(this).getRecommendLockList();
        if (mInstallPackageName != null && !mInstallPackageName.equals("")) {
            try {
                defaultLockList.add(0, mInstallPackageName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        AppInfo installPackage = null;
        ProcessDetector detector = new ProcessDetector();

        //过滤掉在首页已经加锁的应用
        List<String> lockList = mLockManager.getCurLockList();

        for (AppItemInfo localApp : localAppList) {
            if (localApp.packageName.equals(this.getPackageName())
                    || localApp.packageName.equals(Constants.CP_PACKAGE)
                    || localApp.packageName.equals(Constants.ISWIPE_PACKAGE)
                    || localApp.packageName.equals(Constants.PL_PKG_NAME)
                    || localApp.packageName.equals(Constants.SEARCH_BOX_PACKAGE)
                    || detector.isHomePackage(localApp.packageName)
                    || lockList.contains(localApp.packageName))
                continue;
//            if (defaultLockList.contains(localApp.packageName)) {
            if (localApp.topPos != -1) {
                localApp.topPos = fixPosEqules(localApp);
                localApp.isLocked = true;
                if (localApp.packageName.equals(mInstallPackageName)) {
                    installPackage = localApp;
                } else {
                    try {
                        mLockList.add(localApp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                localApp.isLocked = false;
                try {
                    mUnLockList.add(localApp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (mUnLockList.size() == 0) {
            Intent intent = new Intent(this, AppLockListActivity.class);
            startActivity(intent);
            this.finish();
            return;
        }

        Collections.sort(mLockList, new DefalutAppComparator());
        if (installPackage != null) {
            try {
                mLockList.add(0, installPackage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Collections.sort(mUnLockList, new DefalutAppComparator());
        mResaultList = new ArrayList<AppInfo>(mLockList);
        mResaultList.addAll(mUnLockList);

        mHandler.sendEmptyMessage(LOAD_DATA_DONE);
    }

    private int fixPosEqules(AppInfo info) {
        int topPosGet = info.topPos;
        String pckName = info.packageName;

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
    protected void onDestroy() {
        super.onDestroy();
        mLockAdapter = null;
        mUnLockList.clear();
        mLockList.clear();
    }


    public static class DefalutAppComparator implements Comparator<AppInfo> {
        @Override
        public int compare(AppInfo lhs, AppInfo rhs) {


            if (lhs.topPos != -1 || rhs.topPos != -1) {
                Integer a = lhs.topPos;
                Integer b = rhs.topPos;
                return b.compareTo(a);
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

    private void getIntentFrom() {
        Intent intent = this.getIntent();
        mFrom = intent.getStringExtra("from");
        mInstallPackageName = intent.getStringExtra("install_lockApp");
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
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (!(view instanceof MaterialRippleLayout)) return;
        MaterialRippleLayout headView = (MaterialRippleLayout) view;
        ListLockItem lockImageView = (ListLockItem) headView.findViewById(R.id.content_item_all);
        AppInfo selectApp = lockImageView.getInfo();
//        AppInfo selectApp = (AppInfo) view.getTag(R.id.lock_app_tag_first);

        AppInfo info = null;
        LockMode curMode = mLockManager.getCurLockMode();
        if (selectApp.isLocked) {
            selectApp.isLocked = false;
            for (AppInfo lockAppInfo : mLockList) {
                if (selectApp.packageName.equals(lockAppInfo.packageName)) {
                    info = lockAppInfo;
                    lockAppInfo.isLocked = false;
                    break;
                }
            }
            mUnLockList.add(0, info);
            mLockList.remove(info);
            lockImageView.setDefaultRecommendApp(false);
            SDKWrapper.addEvent(this, SDKWrapper.P1, "app", "unlock_" + curMode.modeName + "_" + selectApp.packageName);

        } else {
            for (AppInfo unLockAppInfo : mUnLockList) {
                selectApp.isLocked = true;
                if (selectApp.packageName.equals(unLockAppInfo.packageName)) {
                    info = unLockAppInfo;
                    unLockAppInfo.isLocked = true;
                    break;
                }
            }
            mUnLockList.remove(info);
            mLockList.add(info);
            lockImageView.setDefaultRecommendApp(true);
            SDKWrapper.addEvent(this, SDKWrapper.P1, "app", "lock_" + curMode.modeName + "_" + selectApp.packageName);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.recomment_lock:
                if (mLockList == null || mLockList.isEmpty()) {
                    Intent intent = new Intent(this, AppLockListActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    saveLockList();

                    Intent intent = new Intent(this, SuccessAppLockListActivity.class);
                    if (TextUtils.equals(mFrom, RECOMMEND_FROM_LOCK)) {
                        intent.putExtra("target", getIntent().getIntExtra("target", 1));
                    } else if (TextUtils.equals(mFrom, RECOMMEND_FROM_LOCK_MORE)) {
                        intent.putExtra("target", getIntent().getIntExtra("target", 2));
                    } else if (TextUtils.equals(mFrom, RECOMMEND_FROM_VISITED_MODE)) {
                        intent.putExtra("target", getIntent().getIntExtra("target", 9));
                    } else {
                        intent.putExtra("target", getIntent().getIntExtra("target", 0));
                    }

                    startActivity(intent);
                    finish();
                }
                break;
        }
    }

    private void saveLockList() {
        AppMasterPreference pref = AppMasterPreference
                .getInstance(this);
        List<String> list = new ArrayList<String>();
        for (AppInfo info : mLockList) {
            if(info != null) {
                list.add(0, info.packageName);
            }
        }
        pref.setRecommentTipList(list);

        // add pkgs to vistor mode
        if (mLockManager.getCurLockMode().defaultFlag == 1) {
            mLockManager.addPkg2Mode(list, mLockManager.getCurLockMode());
            AutoStartGuideList.saveSamSungAppLock();
        } else {
            List<LockMode> modeList = mLockManager.getLockMode();
            for (LockMode lockMode : modeList) {
                if (lockMode.defaultFlag == 1) {
                    mLockManager.addPkg2Mode(list, lockMode);
                    break;
                }
            }
        }
        if (mLockList.size() > 0) {
            AutoStartGuideList.saveSamSungAppLock();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {

        }
    }

    @Override
    public void onBackPressed() {
        RecommentAppLockListActivity.this.finish();
        super.onBackPressed();
    }
}
