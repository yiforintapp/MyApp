
package com.leo.appmaster.appmanage;

import java.util.List;

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

import com.leo.appmaster.R;
import com.leo.appmaster.appmanage.view.BackUpFragment;
import com.leo.appmaster.appmanage.view.RestoreFragment;
import com.leo.appmaster.backup.AppBackupRestoreManager;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.LeoPagerTab;

public class BackUpActivity extends BaseFragmentActivity implements OnClickListener,
        OnItemClickListener {
    private LeoPagerTab mPagerTab;
    private ViewPager mViewPager;
    private CommonTitleBar mTtileBar;
    private AppManagerFragmentHoler[] mFragmentHolders = new AppManagerFragmentHoler[2];
    private RestoreFragment restoreFragment;
    private BackUpFragment backupFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_backup_restore);
        initUI();
    }

    private void initUI() {
        mTtileBar = (CommonTitleBar) findViewById(R.id.backup_title_bar);
        mTtileBar.setTitle(R.string.app_backup_back);
        mTtileBar.openBackView();

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
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        try {
            super.onRestoreInstanceState(savedInstanceState, persistentState);
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
