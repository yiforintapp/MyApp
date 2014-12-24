
package com.leo.appmaster.applocker;

import java.util.ArrayList;
import java.util.List;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.engine.AppLoadEngine.AppChangeListener;
import com.leo.appmaster.model.AppInfo;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.LockImageView;
import com.leo.appmaster.ui.PagedGridView;

public class SuccessAppLockListActivity extends BaseActivity implements OnClickListener {
    private List<AppInfo> mLockList;
    private PagedGridView mAppPager;
    private Button lockTV;
    private Object mLock = new Object();
    private ArrayList<AppInfo> resault;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recomment_lock_app_list);
        mLockList = new ArrayList<AppInfo>();
        initUI();
        loadData();

    }

    private void initUI() {
        CommonTitleBar mCommonTitleBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        mCommonTitleBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        mCommonTitleBar.setTitle(R.string.app_lock);
        mCommonTitleBar.openBackView();
        mAppPager = (PagedGridView) findViewById(R.id.recomment_pager_unlock);
        lockTV = (Button) findViewById(R.id.recomment_lock);
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

    private void loadData() {
        ArrayList<AppItemInfo> localAppList = AppLoadEngine.getInstance(this).getAllPkgInfo();
        List<String> lockList = AppMasterPreference.getInstance(this).getLockedAppList();
    for (AppItemInfo app : localAppList) {
        if(lockList.contains(app.packageName)){
            mLockList.add(app);
        }
    }
        resault = new ArrayList<AppInfo>(mLockList);
        int rowCount = getResources().getInteger(R.integer.recomment_gridview_row_count);
        mAppPager.setDatas(resault, 4, rowCount);
        mAppPager.setFlag(true);
    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.recomment_lock:
                Intent intent = new Intent(SuccessAppLockListActivity.this,
                        AppLockListActivity.class);
                try {
                    this.startActivity(intent);
                } catch (Exception e) {
                }

                SuccessAppLockListActivity.this.finish();
                break;
        }

    }
}
