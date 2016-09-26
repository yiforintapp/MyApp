package com.zlf.appmaster.hometab;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.zlf.appmaster.R;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.home.BaseFragmentActivity;
import com.zlf.appmaster.ui.CommonToolbar;
import com.zlf.appmaster.ui.PagerSlidingTabStrip;

import java.util.List;

/**
 * Created by Administrator on 2016/9/26.
 */
public class StockPlaceActivity extends BaseFragmentActivity {

    private CommonToolbar mToolbar;
    private ViewPager mViewPager;
    private HomeTabHolder[] mHomeHolders = new HomeTabHolder[2];
    private StockCjFragment mStockCjFragment;
    private StockXhFragment mStockXhFragment;
    private PagerSlidingTabStrip mPagerSlidingTab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_stock);
        init();
    }


    private void init() {
        mToolbar = (CommonToolbar) findViewById(R.id.stock_toolbar);
        mToolbar.setToolbarTitle(getResources().getString(R.string.deal_info));
        mViewPager = (ViewPager) findViewById(R.id.stock_tab_viewpager);
        initFragment();
        mViewPager.setAdapter(new LoginAdapter(getSupportFragmentManager()));
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setCurrentItem(0);
        mPagerSlidingTab = (PagerSlidingTabStrip) findViewById(R.id.stock_tab_tabs);
        mPagerSlidingTab.setBackgroundResource(R.color.ctc);
        mPagerSlidingTab.setShouldExpand(true);
        mPagerSlidingTab.setIndicatorColor(getResources().getColor(R.color.indicator_select_color));
        mPagerSlidingTab.setDividerColor(getResources().getColor(R.color.ctc));
        mPagerSlidingTab.setViewPager(mViewPager);
    }

    private void initFragment() {

        HomeTabHolder holder = new HomeTabHolder();
        holder.title = getResources().getString(R.string.long_river_stock);
        mStockCjFragment = new StockCjFragment();
        holder.fragment = mStockCjFragment;
        mHomeHolders[0] = holder;

        holder = new HomeTabHolder();
        holder.title = getResources().getString(R.string.xin_hua_stock);
        mStockXhFragment = new StockXhFragment();
        holder.fragment = mStockXhFragment;
        mHomeHolders[1] = holder;


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

    class LoginAdapter extends FragmentPagerAdapter {
        public LoginAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mHomeHolders[position].fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mHomeHolders[position].title;
        }

        @Override
        public int getCount() {
            return mHomeHolders.length;
        }
    }

    class HomeTabHolder {
        String title;
        BaseFragment fragment;
    }
}
