
package com.leo.appmaster.callfilter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;

import com.leo.appmaster.R;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.LeoPagerTab;

import java.util.List;

public class CallFilterMainActivity extends BaseFragmentActivity implements OnClickListener,
        OnPageChangeListener {
    private LeoPagerTab mPagerTab;
    private ViewPager mViewPager;
    private CommonToolbar mTitleBar;

    private BlackListFragment mBlackListFragment;
    private CallFilterFragment mCallFilterFragment;

    private CallFilterFragmentHoler[] mFragmentHolders = new CallFilterFragmentHoler[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_filter_main);
        initUI();
    }

    private void initUI() {
        mTitleBar = (CommonToolbar) findViewById(R.id.call_filter_toolbar);
        mTitleBar.setToolbarTitle(R.string.call_filter_name);
        mTitleBar.setToolbarColorResource(R.color.cb);
        mTitleBar.setOptionClickListener(this);
        mTitleBar.setNavigationClickListener(this);
        mTitleBar.setOptionImageResource(R.drawable.setup_icon);
        mTitleBar.setOptionMenuVisible(true);


        mPagerTab = (LeoPagerTab) findViewById(R.id.call_filter_tab_indicator);
        mPagerTab.setOnPageChangeListener(this);
        mPagerTab.setBackgroundResource(R.color.cb);
        mViewPager = (ViewPager) findViewById(R.id.call_filter_viewpager);
        initFragment();

        mViewPager.setAdapter(new ManagerFlowAdapter(getSupportFragmentManager()));
        mViewPager.setOffscreenPageLimit(2);
        mPagerTab.setViewPager(mViewPager);
    }


    private void initFragment() {
        CallFilterFragmentHoler holder = new CallFilterFragmentHoler();
        holder.title = this.getString(R.string.call_filter_black_list_tab);
        mBlackListFragment = new BlackListFragment();
        holder.fragment = mBlackListFragment;
        mFragmentHolders[0] = holder;

        holder = new CallFilterFragmentHoler();
        holder.title = this.getString(R.string.call_filter_list_tab);
        mCallFilterFragment = new CallFilterFragment();
        holder.fragment = mCallFilterFragment;
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

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {
        }
    }


    class ManagerFlowAdapter extends FragmentPagerAdapter {
        public ManagerFlowAdapter(FragmentManager fm) {
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

    class CallFilterFragmentHoler {
        String title;
        BaseFragment fragment;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ct_back_rl:
                onBackPressed();
                break;
            case R.id.ct_option_1_rl:
                Intent intent = new Intent(this, CallFilterSettingActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    @Override
    public void onPageSelected(int arg0) {

    }

    public void blackListShowEmpty() {
        if (mBlackListFragment != null) {
            mBlackListFragment.showEmpty();
        }
    }

}
