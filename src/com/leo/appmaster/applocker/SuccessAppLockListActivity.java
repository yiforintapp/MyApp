
package com.leo.appmaster.applocker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.leo.appmaster.AppMasterConfig;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.model.AppInfo;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.PagedGridView;
import com.leo.appmaster.utils.LeoLog;

public class SuccessAppLockListActivity extends BaseActivity implements OnClickListener {
    private List<AppInfo> mLockList;
    private PagedGridView mAppPager;
    private TextView lockTV;
    private ArrayList<AppInfo> resault;
    public static final int REQUEST_CODE_LOCK = 1000;
    public static final int REQUEST_CODE_OPTION = 1001;
    private static final String FROM_DEFAULT_RECOMMENT_ACTIVITY = "recomment_activity";

    // private static final String CURRENT_ACTIVITY = "current_activity_name";
    // private static final String CURRENT_ACTIVITY_NAME =
    // "SuccessAppLockListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_app_success_list);
        mLockList = new ArrayList<AppInfo>();
        initUI();
        loadData();

    }

    private void initUI() {
        CommonTitleBar mCommonTitleBar = (CommonTitleBar)
                findViewById(R.id.layout_title_bar);
        // mCommonTitleBar = (CommonTitleBar)
        // findViewById(R.id.layout_title_bar);
        // mCommonTitleBar.setTitle(R.string.app_lock);
        mCommonTitleBar.setBackArrowVisibility(View.GONE);
        mAppPager = (PagedGridView) findViewById(R.id.recomment_pager_unlock);
        lockTV = (TextView) findViewById(R.id.success_recomment_lock);
        lockTV.setOnClickListener(this);
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
    
    
    @SuppressLint("Override")
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {

        }
    }


	@SuppressLint("Override")
	@Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        try {
            super.onRestoreInstanceState(savedInstanceState, persistentState);
        } catch (Exception e) {

        }
    }

    private void loadData() {
        ArrayList<AppItemInfo> localAppList = AppLoadEngine.getInstance(this).getAllPkgInfo();
        List<String> lockList = AppMasterPreference.getInstance(this).getRecommentTipList();
        for (AppItemInfo app : localAppList) {
            if (lockList.contains(app.packageName)) {
                app.isLocked = false;
                mLockList.add(app);
                /* SDK */
                SDKWrapper.addEvent(this, SDKWrapper.P1, "first_lock", app.packageName);
            }
        }
        Collections.sort(mLockList, new LockedAppComparator(lockList));
        resault = new ArrayList<AppInfo>(mLockList);
        int rowCount = getResources().getInteger(R.integer.success_recomment_gridview_row_count);
        int colCount = getResources().getInteger(R.integer.success_recomment_gridview_col_count);
        mAppPager.setDatas(resault, colCount, rowCount);
        mAppPager.setFlag(FROM_DEFAULT_RECOMMENT_ACTIVITY);
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
    public void onClick(View arg0) {
        switch (arg0.getId()) {
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
                    if (AppMasterConfig.LOGGABLE) {
                        LeoLog.f(SuccessAppLockListActivity.class.getSimpleName(), "onclick t = 1", Constants.LOCK_LOG);
                    }
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
                        if (AppMasterConfig.LOGGABLE) {
                            LeoLog.f(SuccessAppLockListActivity.class.getSimpleName(), "onclick t = 2", Constants.LOCK_LOG);
                        }
                        intent = new Intent(this,
                                LockSettingActivity.class);
                        intent.putExtra("to_lock_list", true);
                        this.startActivity(intent);
                    }
                }

                SuccessAppLockListActivity.this.finish();
                break;
        }

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
