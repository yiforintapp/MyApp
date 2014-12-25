
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
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
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
    private List<AppInfo> mLockList;
    private List<AppInfo> mUnLockList;
    private PagedGridView mAppPager;
    private TextView lockTV;
    private Object mLock = new Object();
    private ArrayList<AppInfo> resault;
    private String mPackageName;
    private String mInstallPackageName;
    private static final String FROM_DEFAULT_RECOMMENT_ACTIVITY = "recomment_activity";
    private final static String[] DEFAULT_LOCK_LIST = new String[] {
            " com.whatsapp",
            "com.android.mms",
            "com.facebook.katana",
            "com.android.gallery3d",
            "com.sec.android.gallery3d",
            "com.android.contacts",
            "com.facebook.orca",
            "com.google.android.youtube",
            "com.android.providers.downloads.ui",
            "com.sec.android.app.myfiles",
            "com.android.email",
            "com.viber.voip",
            "com.google.android.talk",
            "com.mxtech.videoplayer.ad",
            "com.android.calendar"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recomment_lock_app_list);
        mLockList = new ArrayList<AppInfo>();
        mUnLockList = new ArrayList<AppInfo>();
        initUI();
        getIntentFrom();
        loadData();
    }

    private void initUI() {
        CommonTitleBar mCommonTitleBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        mCommonTitleBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        mCommonTitleBar.setTitle(R.string.app_lock);
        mCommonTitleBar.openBackView();
        mAppPager = (PagedGridView) findViewById(R.id.recomment_pager_unlock);
        lockTV = (TextView) findViewById(R.id.recomment_lock);
        lockTV.setOnClickListener(this);
        mAppPager.setItemClickListener(this);
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
        }
        if (mLockList.size() <= 0) {
            lockTV.setEnabled(false);
            lockTV.setBackground(getResources().getDrawable(R.drawable.unclick_button));
            lockTV.setTextColor(getResources().getColor(R.color.default_lock));
        } else {
            lockTV.setEnabled(true);
            lockTV.setBackground(getResources().getDrawable(R.drawable.default_lock_down));
            lockTV.setTextColor(getResources().getColor(R.color.white));
        }
    }

    private void saveLockList() {
        new Thread(new PushLockedListTask()).start();
    }

    private void loadData() {
        ArrayList<AppItemInfo> localAppList = AppLoadEngine.getInstance(this).getAllPkgInfo();
        List<String> defaultLockList = getDefaultLockList();
        if (mInstallPackageName != null && !mInstallPackageName.equals("")) {
            defaultLockList.add(0, mInstallPackageName);
        }
        for (AppItemInfo localApp : localAppList) {
            if (defaultLockList.contains(localApp.packageName)) {
                localApp.isLocked = true;
                mLockList.add(localApp);
            } else {
                localApp.isLocked = false;
                mUnLockList.add(localApp);
            }
        }
        Collections.sort(mLockList, new LockedAppComparator(mLockList));
        Collections.sort(mUnLockList, new DefalutAppComparator());
        resault = new ArrayList<AppInfo>(mLockList);
        resault.addAll(mUnLockList);

        int rowCount = getResources().getInteger(R.integer.recomment_gridview_row_count);
        mAppPager.setDatas(resault, 4, rowCount);
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
                Intent intent = new Intent(this, LockSettingActivity.class);
                if (mPackageName != null && !mPackageName.equals("")) {
                    intent.putExtra(LockScreenActivity.EXTRA_TO_ACTIVITY,
                            mPackageName);
                } else {
                    intent.putExtra(LockScreenActivity.EXTRA_TO_ACTIVITY,
                            AppLockListActivity.class.getName());
                }
                startActivity(intent);
                this.finish();
                break;
        }

    }

    private class PushLockedListTask implements Runnable {
        @Override
        public void run() {
            synchronized (mLock) {
                AppMasterPreference pref = AppMasterPreference
                        .getInstance(RecommentAppLockListActivity.this);
                List<String> list = new ArrayList<String>();
                for (AppInfo info : RecommentAppLockListActivity.this.mLockList) {
                    list.add(info.packageName);
                }
                pref.setRecommentTipList(list);
            }
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
        mPackageName = intent.getStringExtra(LockScreenActivity.EXTRA_TO_ACTIVITY);
        mInstallPackageName = intent.getStringExtra("install_lockApp");
    }

}
