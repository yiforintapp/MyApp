package com.leo.appmaster.applocker;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.model.AppInfo;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.RippleView1;

/**
 * Created by qili on 15-10-11.
 */
public class SuccessAppLockListActivity extends BaseActivity implements OnClickListener {
    private static final String FROM_DEFAULT_RECOMMENT_ACTIVITY = "recomment_activity";
    private List<AppInfo> mLockList;
    private ArrayList<AppInfo> resault;
    private ListView mLockListView;
    private RippleView1 lockTV;
    private List<LockMode> mModeList;
    private ListSuccessAdapter mListAdapter;
    public CommonTitleBar mTitleBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_lockapp_success);
        initUI();
        loadData();
    }

    private void initUI() {
        mTitleBar = (CommonTitleBar)
                findViewById(R.id.lock_success_title_bar);
        mTitleBar.setBackArrowVisibility(View.GONE);

        lockTV = (RippleView1) findViewById(R.id.success_recomment_lock);
        lockTV.setOnClickListener(this);
        mLockListView = (ListView) findViewById(R.id.recomment_lock_list);
        mListAdapter = new ListSuccessAdapter(this);
        mLockListView.setAdapter(mListAdapter);

        mLockList = new ArrayList<AppInfo>();
        mModeList = new ArrayList<LockMode>();
    }

    private void loadData() {

        mModeList = mLockManager.getLockMode();
        ArrayList<AppItemInfo> localAppList = AppLoadEngine.getInstance(this).getAllPkgInfo();
        List<String> lockList = AppMasterPreference.getInstance(this).getRecommentTipList();
        for (AppItemInfo app : localAppList) {
            if (lockList.contains(app.packageName)) {
                app.isLocked = true;
                mLockList.add(app);
                /* SDK */
                SDKWrapper.addEvent(this, SDKWrapper.P1, "first_lock", app.packageName);
            }
        }

//        Collections.sort(mLockList, new LockedAppComparator(lockList));
        Collections.sort(mLockList, new DefalutAppComparator());
        resault = new ArrayList<AppInfo>(mLockList);
        mListAdapter.setData(resault);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.success_recomment_lock:
                int target = getIntent().getIntExtra("target", 0);
                Intent intent;
                if (target == 0) {
                    intent = new Intent(SuccessAppLockListActivity.this,
                            AppLockListActivity.class);
                    intent.putExtra("from_lock_more", true);
//                    intent.putExtra("enter_from_lockmode", true);
                    this.startActivity(intent);
                } else if (target == 1) {
                    intent = new Intent(this,
                            LockSettingActivity.class);
                    intent.putExtra("just_finish", true);
                    this.startActivity(intent);
                } else if (target == 2) {
                    if (AppMasterPreference.getInstance(getApplicationContext()).getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
                        intent = new Intent(SuccessAppLockListActivity.this,
                                AppLockListActivity.class);
                        intent.putExtra("from_lock_more", true);
                        this.startActivity(intent);
                    } else {
                        intent = new Intent(this,
                                LockSettingActivity.class);
                        intent.putExtra("to_lock_list", true);
                        this.startActivity(intent);
                    }
                } else if (target == 9) {
                    LockMode visitMode = null;
                    if (mModeList != null) {
                        for (LockMode mode : mModeList) {
                            if (mode.defaultFlag == 1) {
                                visitMode = mode;
                                break;
                            }
                        }
                        editLockMode(visitMode, false);
                    }

                }
                SuccessAppLockListActivity.this.finish();
                break;
        }
    }

    private void editLockMode(LockMode lockMode, boolean addNewMode) {
        if (lockMode != null) {
            Intent intent = new Intent(this, LockModeEditActivity.class);
            if (addNewMode) {
                intent.putExtra("mode_name", this.getString(R.string.new_mode));
                intent.putExtra("mode_id", -1);
            } else {
                intent.putExtra("mode_name", lockMode.modeName);
                intent.putExtra("mode_id", lockMode.modeId);
            }
            intent.putExtra("new_mode", addNewMode);
            startActivity(intent);
        }
    }

    @SuppressLint("Override")
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {

        }
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
        mLockList.clear();
    }

    @Override
    public void onBackPressed() {
        int target = getIntent().getIntExtra("target", 0);
        Intent intent;
        if (target == 0) {
            intent = new Intent(this, AppLockListActivity.class);
            try {
                this.startActivity(intent);
            } catch (Exception e) {
            }
        }
        super.onBackPressed();
    }
}
