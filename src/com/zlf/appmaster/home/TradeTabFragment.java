package com.zlf.appmaster.home;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.zlf.appmaster.R;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.tradetab.StockCOMEXFragment;
import com.zlf.appmaster.tradetab.StockIndexFragment;
import com.zlf.appmaster.tradetab.StockJinGuiFragment;
import com.zlf.appmaster.tradetab.StockLMEFragment;
import com.zlf.appmaster.tradetab.StockNYMEXFragment;
import com.zlf.appmaster.tradetab.StockPlateFragment;
import com.zlf.appmaster.tradetab.StockQiLuFragment;
import com.zlf.appmaster.tradetab.StockRiseInfoFragment;
import com.zlf.appmaster.tradetab.StockSHFFragment;
import com.zlf.appmaster.tradetab.StockTopicFragment;
import com.zlf.appmaster.tradetab.StockWaiHuiFragment;
import com.zlf.appmaster.tradetab.StockZhiGoldFragment;
import com.zlf.appmaster.ui.PagerSlidingTabStrip;
import com.zlf.appmaster.userTab.StockFavoriteFragment;

import java.util.List;

/**
 * Created by Administrator on 2016/7/18.
 */
public class TradeTabFragment extends BaseFragment {

    private ViewPager mViewPager;
    private HomeTabHolder[] mHomeHolders = new HomeTabHolder[8];
    private StockJinGuiFragment mStockJinGuiFragment;
    private StockQiLuFragment mStockQiLuFragment;
    private StockLMEFragment mStockLMEFragment;
    private StockWaiHuiFragment mStockWaiHuiFragment;
    private StockZhiGoldFragment mStockZhiGoldFragment;
    private StockSHFFragment mStockSHFFragment;
    private StockNYMEXFragment mStockNYMEXFragment;
    private StockCOMEXFragment mStockCOMEXFragment;


    private PagerSlidingTabStrip mPagerSlidingTab;

    private StockFavoriteFragment mStockFavoriteFragment;


    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_trade;
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
        holder.title = mActivity.getResources().getString(R.string.long_river_company);
        mStockJinGuiFragment = new StockJinGuiFragment();
        holder.fragment = mStockJinGuiFragment;
        mHomeHolders[0] = holder;

        holder = new HomeTabHolder();
        holder.title = mActivity.getResources().getString(R.string.global_gold);
        mStockQiLuFragment = new StockQiLuFragment();
        holder.fragment = mStockQiLuFragment;
        mHomeHolders[1] = holder;

        holder = new HomeTabHolder();
        holder.title = mActivity.getResources().getString(R.string.hangqing_waihui);
        mStockWaiHuiFragment = new StockWaiHuiFragment();
        holder.fragment = mStockWaiHuiFragment;
        mHomeHolders[2] = holder;

        holder = new HomeTabHolder();
        holder.title = mActivity.getResources().getString(R.string.hangqing_zhigold);
        mStockZhiGoldFragment = new StockZhiGoldFragment();
        holder.fragment = mStockZhiGoldFragment;
        mHomeHolders[3] = holder;

        holder = new HomeTabHolder();
        holder.title = mActivity.getResources().getString(R.string.hangqing_shf);
        mStockSHFFragment = new StockSHFFragment();
        holder.fragment = mStockSHFFragment;
        mHomeHolders[4] = holder;

        holder = new HomeTabHolder();
        holder.title = mActivity.getResources().getString(R.string.hangqing_lme);
        mStockLMEFragment = new StockLMEFragment();
        holder.fragment = mStockLMEFragment;
        mHomeHolders[5] = holder;

        holder = new HomeTabHolder();
        holder.title = mActivity.getResources().getString(R.string.hangqing_nymex);
        mStockNYMEXFragment = new StockNYMEXFragment();
        holder.fragment = mStockNYMEXFragment;
        mHomeHolders[6] = holder;


        holder = new HomeTabHolder();
        holder.title = mActivity.getResources().getString(R.string.hangqing_comex);
        mStockCOMEXFragment = new StockCOMEXFragment();
        holder.fragment = mStockCOMEXFragment;
        mHomeHolders[7] = holder;


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
