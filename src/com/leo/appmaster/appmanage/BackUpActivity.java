
package com.leo.appmaster.appmanage;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.appmanage.view.BackUpFragment;
import com.leo.appmaster.appmanage.view.RestoreFragment;
import com.leo.appmaster.backup.AppBackupRestoreManager;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.LeoPagerTab;
import com.leo.appmaster.utils.LeoLog;

public class BackUpActivity extends BaseFragmentActivity implements OnClickListener,
        OnItemClickListener {
    private LeoPagerTab mPagerTab;
    private ViewPager mViewPager;
    private CommonToolbar mTtileBar;
    private AppManagerFragmentHoler[] mFragmentHolders = new AppManagerFragmentHoler[2];
    private RestoreFragment restoreFragment;
    private BackUpFragment backupFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (AppMasterPreference.getInstance(this).getIsNeedCutBackupUninstallAndPrivacyContact()) {
//            finish();
//        }
        setContentView(R.layout.activity_delete_backup_restore);
        handleIntent();
        initUI();
    }

    private void handleIntent() {
        Intent intent = getIntent();
        String isFromNotification = (String) intent.getExtra("from");
        LeoLog.d("testBackupNoti", "isFromNotification  : " + isFromNotification);
        if (isFromNotification != null && isFromNotification.equals("notification")) {
            PreferenceTable.getInstance().putString(Constants.NEW_APP_NUM, "");
            SDKWrapper.addEvent(this, SDKWrapper.P1, "backup", "backup_cnts_notify");
        }
    }

    private void initUI() {
        mTtileBar = (CommonToolbar) findViewById(R.id.backup_title_bar);
        mTtileBar.setToolbarTitle(R.string.app_backup_back);
        mTtileBar.setToolbarColorResource(R.color.cb);
        mTtileBar.setOptionMenuVisible(false);

        mPagerTab = (LeoPagerTab) findViewById(R.id.backup_app_tab_indicator);
        mViewPager = (ViewPager) findViewById(R.id.backup_app_viewpager);
        initFragment();

        mViewPager.setAdapter(new AppManagerPagerAdapter(getSupportFragmentManager()));
        mViewPager.setOffscreenPageLimit(2);
        mPagerTab.setViewPager(mViewPager);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {

        }

    }


    private void initFragment() {

        AppManagerFragmentHoler holder = new AppManagerFragmentHoler();

        holder.title = this.getString(R.string.app_backup_v2);
        backupFragment = new BackUpFragment();
        backupFragment.setContent(holder.title);
        holder.fragment = backupFragment;
        mFragmentHolders[0] = holder;

        holder = new AppManagerFragmentHoler();
        holder.title = this.getString(R.string.app_restore_v2);
        restoreFragment = new RestoreFragment();
        restoreFragment.setContent(holder.title);
        holder.fragment = restoreFragment;
        mFragmentHolders[1] = holder;

        // AM-614, remove cached fragments
        FragmentManager fm = getSupportFragmentManager();
        try {
            FragmentTransaction ft = fm.beginTransaction();
            List<Fragment> list = fm.getFragments();
            if (list != null) {
                for (Fragment f : fm.getFragments()) {
                    ft.remove(f);
                }
            }
            ft.commit();
        } catch (Exception e) {

        }

    }

    class AppManagerPagerAdapter extends FragmentPagerAdapter {
        public AppManagerPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentHolders[position].fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentHolders[position].title;
        }

        @Override
        public int getCount() {
            return mFragmentHolders.length;
        }
    }

    class AppManagerFragmentHoler {
        String title;
        BaseFragment fragment;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    public void onClick(View v) {
    }

    public AppBackupRestoreManager getBackupManager() {
        if (restoreFragment != null) {
            return restoreFragment.getBackupManager();
        }
        return null;
    }

    public void tryDeleteApp(AppItemInfo app) {
        if (restoreFragment != null) {
            restoreFragment.tryDeleteApp(app);
        }
    }

    public void refreshRestoreFragment() {
        if (restoreFragment != null) {
            restoreFragment.updateDataFromAdapter();
        }
    }

    public void refreshBackupFragment() {
        if (backupFragment != null) {
            backupFragment.updateDataFromApdater();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
