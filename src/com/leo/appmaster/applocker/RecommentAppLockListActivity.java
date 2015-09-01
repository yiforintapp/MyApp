
package com.leo.appmaster.applocker;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.applocker.model.ProcessDetector;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.engine.AppLoadEngine.AppChangeListener;
import com.leo.appmaster.model.AppInfo;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.LockImageView;
import com.leo.appmaster.ui.PagedGridView;

public class RecommentAppLockListActivity extends BaseActivity implements OnClickListener,
        OnItemClickListener, AppChangeListener {

    public static final String RECOMMEND_FROM_LOCK = "new_app_install_lock";
    public static final String RECOMMEND_FROM_LOCK_MORE = "new_app_install_lock_more";

    private List<AppInfo> mLockList;
    private List<AppInfo> mUnLockList;
    private PagedGridView mAppPager;
    private TextView lockTV;
    private ArrayList<AppInfo> resault;
    private String mPackageName;
    private String mInstallPackageName;
    private static final String FROM_DEFAULT_RECOMMENT_ACTIVITY = "recomment_activity";
    private final static String[] DEFAULT_LOCK_LIST = new String[] {
            "com.whatsapp",
            "com.android.mms",
            "com.sonyericsson.conversations",
            "com.facebook.katana",
            "com.android.gallery3d",
            "com.sec.android.gallery3d",
            "com.sonyericsson.album",
            "com.android.contacts",
            "com.google.android.contacts",
            "com.sonyericsson.android.socialphonebook",
            "com.facebook.orca",
            "com.google.android.youtube",
            "com.android.providers.downloads.ui",
            "com.sec.android.app.myfiles",
            "com.android.email",
            "com.viber.voip",
            "com.google.android.talk",
            "com.mxtech.videoplayer.ad",
            "com.android.calendar",
            "com.google.android.calendar",
            "com.tencent.mm",
            "com.tencent.mm",
            "com.tencent.mobileqq",
            "com.tencent.qq",
            "jp.naver.line.android"
    };

    private String mFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recomment_lock_app_list);
        mLockList = new ArrayList<AppInfo>();
        mUnLockList = new ArrayList<AppInfo>();
        getIntentFrom();
        initUI();
        loadData();
    }

    private void initUI() {
        CommonTitleBar mCommonTitleBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        mCommonTitleBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        mCommonTitleBar.openBackView();
        mCommonTitleBar.setTitle(R.string.app_lock);
        mAppPager = (PagedGridView) findViewById(R.id.recomment_pager_unlock);
        lockTV = (TextView) findViewById(R.id.recomment_lock);
        lockTV.setOnClickListener(this);
        mAppPager.setItemClickListener(this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {

        }
    }

    /*
     * @Override public void onRestoreInstanceState(Bundle savedInstanceState,
     * PersistableBundle persistentState) { try {
     * super.onRestoreInstanceState(savedInstanceState, persistentState); }
     * catch (Exception e) { } }
     */

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnLockList.clear();
        mLockList.clear();
    }

    @SuppressLint("NewApi")
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        animateItem(arg1);
        AppInfo selectApp = (AppInfo) arg1.getTag();
        AppInfo info = null;
        LockManager lm = LockManager.getInstatnce();
        LockMode curMode = lm.getCurLockMode();
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
            // to set view unlocked
            ((LockImageView) arg1.findViewById(R.id.iv_app_icon))
                    .setDefaultRecommendApp(false);
            SDKWrapper.addEvent(this, SDKWrapper.P1, "app", "unlock_"+curMode.modeName+"_"+selectApp.packageName);
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
            // to set view lock
            ((LockImageView) arg1.findViewById(R.id.iv_app_icon))
                    .setDefaultRecommendApp(true);
            SDKWrapper.addEvent(this, SDKWrapper.P1, "app", "lock_"+curMode.modeName+"_"+selectApp.packageName);
        }

        if (mLockList.size() <= 0) {
            lockTV.setEnabled(false);
            // int image = R.drawable.unclick_button;
            // lockTV.setBackgroundDrawable(getResources().getDrawable(image));
            // lockTV.setTextColor(getResources().getColor(R.color.default_lock));
        } else {
            lockTV.setEnabled(true);
            // int image = R.color.default_lock_down;
            // lockTV.setBackgroundDrawable(getResources().getDrawable(image));
            // lockTV.setTextColor(getResources().getColor(R.color.white));
        }
    }

    private void saveLockList() {
        AppMasterPreference pref = AppMasterPreference
                .getInstance(RecommentAppLockListActivity.this);
        List<String> list = new ArrayList<String>();
        for (AppInfo info : mLockList) {
            list.add(info.packageName);
        }
        pref.setRecommentTipList(list);

        // add pkgs to vistor mode
        LockManager lm = LockManager.getInstatnce();
        if (lm.getCurLockMode().defaultFlag == 1) {
            lm.addPkg2Mode(list, lm.getCurLockMode());
        } else {
            List<LockMode> modeList = lm.getLockMode();
            for (LockMode lockMode : modeList) {
                if (lockMode.defaultFlag == 1) {
                    lm.addPkg2Mode(list, lockMode);
                    break;
                }
            }
        }
    }

    private void loadData() {
        ArrayList<AppItemInfo> localAppList = AppLoadEngine.getInstance(this).getAllPkgInfo();
        List<String> defaultLockList = getDefaultLockList();
        if (mInstallPackageName != null && !mInstallPackageName.equals("")) {
            defaultLockList.add(0, mInstallPackageName);
        }
        AppInfo installPackage = null;
        ProcessDetector detector = new ProcessDetector();
        for (AppItemInfo localApp : localAppList) {
            if (localApp.packageName.equals(this.getPackageName())
                    || localApp.packageName.equals(Constants.CP_PACKAGE)
                    || localApp.packageName.equals(Constants.ISWIPE_PACKAGE)
                    || localApp.packageName.equals(Constants.SEARCH_BOX_PACKAGE)
                    || detector.isHomePackage(localApp.packageName))
                continue;
            if (defaultLockList.contains(localApp.packageName)) {
                localApp.isLocked = true;
                if (localApp.packageName.equals(mInstallPackageName)) {
                    installPackage = localApp;
                } else {
                    mLockList.add(localApp);
                }
            } else {
                localApp.isLocked = false;
                mUnLockList.add(localApp);
            }
        }
        Collections.sort(mLockList, new LockedAppComparator(mLockList));
        if (installPackage != null) {
            mLockList.add(0, installPackage);
        }
        Collections.sort(mUnLockList, new DefalutAppComparator());
        resault = new ArrayList<AppInfo>(mLockList);
        resault.addAll(mUnLockList);

        int rowCount = getResources().getInteger(R.integer.recomment_gridview_row_count);
        int colCount = getResources().getInteger(R.integer.recomment_gridview_col_count);

        mAppPager.setDatas(resault, colCount, rowCount - 1);
        mAppPager.setFlag(FROM_DEFAULT_RECOMMENT_ACTIVITY);
    }

    private class LockedAppComparator implements Comparator<AppInfo> {
        List<AppInfo> sortBase;

        public LockedAppComparator(List<AppInfo> sortBase) {
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

    private List<String> getDefaultLockList() {
        List<String> defaultLockList = new ArrayList<String>();
        for (String string : DEFAULT_LOCK_LIST) {
            defaultLockList.add(string);
        }
        return defaultLockList;
    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.recomment_lock:
                saveLockList();
                Intent intent = new Intent(this, SuccessAppLockListActivity.class);
                if (TextUtils.equals(mFrom, RECOMMEND_FROM_LOCK)) {
                    intent.putExtra("target", getIntent().getIntExtra("target", 1));
                } else if (TextUtils.equals(mFrom, RECOMMEND_FROM_LOCK_MORE)) {
                    intent.putExtra("target", getIntent().getIntExtra("target", 2));
                } else {
                    intent.putExtra("target", getIntent().getIntExtra("target", 0));
//                    intent.putExtra("isFromHomeToLockMode", true);
                }
                startActivity(intent);
                this.finish();
                break;
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

    private void animateItem(View view) {
        AnimatorSet animate = new AnimatorSet();
        animate.setDuration(300);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f,
                0.8f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f,
                0.8f, 1f);
        animate.playTogether(scaleX, scaleY);
        animate.start();
    }

    private void getIntentFrom() {
        Intent intent = this.getIntent();
        mFrom = intent.getStringExtra("from");
        mInstallPackageName = intent.getStringExtra("install_lockApp");
    }

    @Override
    public void onBackPressed() {
        RecommentAppLockListActivity.this.finish();
        super.onBackPressed();
    }

}
