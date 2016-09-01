package com.zlf.appmaster.home;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.zlf.appmaster.R;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.hometab.StockIndexFragment;
import com.zlf.appmaster.hometab.StockJinGuiFragment;
import com.zlf.appmaster.hometab.StockPlateFragment;
import com.zlf.appmaster.hometab.StockQiLuFragment;
import com.zlf.appmaster.hometab.StockRiseInfoFragment;
import com.zlf.appmaster.hometab.StockTopicFragment;
import com.zlf.appmaster.ui.PagerSlidingTabStrip;
import com.zlf.appmaster.userTab.StockFavoriteFragment;

import java.util.List;

/**
 * Created by Administrator on 2016/7/18.
 */
public class HomeTabFragment extends BaseFragment {

    private ViewPager mViewPager;
    private HomeTabHolder[] mHomeHolders = new HomeTabHolder[2];
//    private HomeTabChildFragment mUserSelectFragment;
    private StockJinGuiFragment mStockJinGuiFragment;
    private StockQiLuFragment mStockQiLuFragment;
    private StockTopicFragment mStockTopicFragment;
    private StockIndexFragment mStockIndexFragment;
    private StockRiseInfoFragment mStockRiseInfoFragment;
    private StockPlateFragment mStockPlateFragment;
    private PagerSlidingTabStrip mPagerSlidingTab;

    private StockFavoriteFragment mStockFavoriteFragment;


    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_home;
    }

    @Override
    protected void onInitUI() {
        mViewPager = (ViewPager) findViewById(R.id.home_tab_viewpager);
        initFragment();
        mViewPager.setAdapter(new LoginAdapter(getChildFragmentManager()));
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setCurrentItem(0);
        mPagerSlidingTab = (PagerSlidingTabStrip) findViewById(R.id.home_tab_tabs);
        mPagerSlidingTab.setBackgroundResource(R.color.ctc);
        mPagerSlidingTab.setShouldExpand(true);
        mPagerSlidingTab.setIndicatorColor(getResources().getColor(R.color.indicator_select_color));
        mPagerSlidingTab.setDividerColor(getResources().getColor(R.color.ctc));
        mPagerSlidingTab.setViewPager(mViewPager);
    }

    private void initFragment() {
//        HomeTabHolder holder = new HomeTabHolder();
//        holder.title = "自选";
//        mStockFavoriteFragment = new StockFavoriteFragment();
//        holder.fragment = mStockFavoriteFragment;
//        mHomeHolders[0] = holder;
        HomeTabHolder holder = new HomeTabHolder();
        holder.title = "津贵所";
        mStockJinGuiFragment = new StockJinGuiFragment();
        holder.fragment = mStockJinGuiFragment;
        mHomeHolders[0] = holder;

        holder = new HomeTabHolder();
        holder.title = "齐鲁商品";
        mStockQiLuFragment = new StockQiLuFragment();
        holder.fragment = mStockQiLuFragment;
        mHomeHolders[1] = holder;

       /* holder = new HomeTabHolder();
        holder.title = "指数";
        mStockIndexFragment = new StockIndexFragment();
        holder.fragment = mStockIndexFragment;
        mHomeHolders[2] = holder;

        holder = new HomeTabHolder();
        holder.title = "涨跌";
        mStockRiseInfoFragment = new StockRiseInfoFragment();
        holder.fragment = mStockRiseInfoFragment;
        mHomeHolders[3] = holder;

        holder = new HomeTabHolder();
        holder.title = "行业";
        mStockPlateFragment = new StockPlateFragment();
        holder.fragment = mStockPlateFragment;
        mHomeHolders[4] = holder;

        holder = new HomeTabHolder();
        holder.title = "题材";
        mStockTopicFragment = new StockTopicFragment();
        holder.fragment = mStockTopicFragment;
        mHomeHolders[5] = holder;*/


        FragmentManager fm = getChildFragmentManager();
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
