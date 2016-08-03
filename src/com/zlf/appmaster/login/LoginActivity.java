package com.zlf.appmaster.login;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.zlf.appmaster.R;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.home.BaseFragmentActivity;
import com.zlf.appmaster.ui.PagerSlidingTabStrip;

import java.util.List;

/**
 * Created by Administrator on 2016/7/19.
 */
public class LoginActivity  extends BaseFragmentActivity {
    private ViewPager mViewPager;
    private LoginHolder[] mLoginHolders = new LoginHolder[2];
    private UserLoginFragment mUserLoginFragment;
    private PhoneLoginFragment mPhoneLoginFragment;
    private PagerSlidingTabStrip mPagerSlidingTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
        setListener();
    }

    private void init() {
        mViewPager = (ViewPager) findViewById(R.id.login_viewpager);
        initFragment();
        mViewPager.setAdapter(new LoginAdapter(getSupportFragmentManager()));
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setCurrentItem(0);
        mPagerSlidingTab = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        mPagerSlidingTab.setBackgroundResource(R.color.tab_background);
        mPagerSlidingTab.setShouldExpand(true);
        mPagerSlidingTab.setIndicatorColor(getResources().getColor(R.color.main_icon_red));
        mPagerSlidingTab.setDividerColor(0xffffffff);
        mPagerSlidingTab.setViewPager(mViewPager);
    }

    private void initFragment() {
        LoginHolder holder = new LoginHolder();
        holder.title = "账号登录";
        mUserLoginFragment = new UserLoginFragment();
        holder.fragment = mUserLoginFragment;
        mLoginHolders[0] = holder;

        holder = new LoginHolder();
        holder.title = "手机登录";
        mPhoneLoginFragment = new PhoneLoginFragment();
        holder.fragment = mPhoneLoginFragment;
        mLoginHolders[1] = holder;

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

    private void setListener() {

    }

    class LoginAdapter extends FragmentPagerAdapter {
        public LoginAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mLoginHolders[position].fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mLoginHolders[position].title;
        }

        @Override
        public int getCount() {
            return mLoginHolders.length;
        }
    }

    class LoginHolder {
        String title;
        BaseFragment fragment;
    }

}
