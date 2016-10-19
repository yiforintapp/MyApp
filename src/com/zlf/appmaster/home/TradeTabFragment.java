package com.zlf.appmaster.home;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.zlf.appmaster.R;
import com.zlf.appmaster.ThreadManager;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.tradetab.StockCOMEXFragment;
import com.zlf.appmaster.tradetab.StockJinGuiFragment;
import com.zlf.appmaster.tradetab.StockLMEFragment;
import com.zlf.appmaster.tradetab.StockNYMEXFragment;
import com.zlf.appmaster.tradetab.StockQiLuFragment;
import com.zlf.appmaster.tradetab.StockSHFFragment;
import com.zlf.appmaster.tradetab.StockWaiHuiFragment;
import com.zlf.appmaster.tradetab.StockZhiGoldFragment;
import com.zlf.appmaster.ui.PagerSlidingTabStrip;
import com.zlf.appmaster.ui.SelectPopupWindow;
import com.zlf.appmaster.userTab.StockFavoriteFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/18.
 */
public class TradeTabFragment extends BaseFragment implements View.OnClickListener {

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

    private RelativeLayout mMoreTabLayout;
    private ImageView mMoreTabIv;

    private LoginAdapter mAdapter;
    private ArrayList<String> mTabs = new ArrayList<String>(3);
    private SelectPopupWindow mPoPupWindow;


    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_trade;
    }

    @Override
    protected void onInitUI() {
        mViewPager = (ViewPager) findViewById(R.id.home_tab_viewpager);
        initFragment();
        mMoreTabLayout = (RelativeLayout) findViewById(R.id.trade_more_tab);
        mMoreTabLayout.setOnClickListener(this);
        mMoreTabIv = (ImageView) findViewById(R.id.user_more_tab_iv);
        mPagerSlidingTab = (PagerSlidingTabStrip) findViewById(R.id.home_tab_tabs);
        mPagerSlidingTab.setBackgroundResource(R.color.ctc);
        mPagerSlidingTab.setShouldExpand(true);
        mPagerSlidingTab.setIndicatorColor(getResources().getColor(R.color.indicator_select_color));
        mPagerSlidingTab.setDividerColor(getResources().getColor(R.color.ctc));
        mPagerSlidingTab.setViewPager(mViewPager);
    }

    public void initFragment() {
        if (mTabs != null && mTabs.size() != 0) {
            mTabs.clear();
        }
        HomeTabHolder holder = new HomeTabHolder();
        holder.title = mActivity.getResources().getString(R.string.long_river_company);
        mTabs.add(holder.title);
        mStockJinGuiFragment = new StockJinGuiFragment();
        holder.fragment = mStockJinGuiFragment;
        mHomeHolders[0] = holder;

        holder = new HomeTabHolder();
        holder.title = mActivity.getResources().getString(R.string.global_gold);
        mTabs.add(holder.title);
        mStockQiLuFragment = new StockQiLuFragment();
        holder.fragment = mStockQiLuFragment;
        mHomeHolders[1] = holder;

        holder = new HomeTabHolder();
        holder.title = mActivity.getResources().getString(R.string.hangqing_waihui);
        mTabs.add(holder.title);
        mStockWaiHuiFragment = new StockWaiHuiFragment();
        holder.fragment = mStockWaiHuiFragment;
        mHomeHolders[2] = holder;

        holder = new HomeTabHolder();
        holder.title = mActivity.getResources().getString(R.string.hangqing_zhigold);
        mTabs.add(holder.title);
        mStockZhiGoldFragment = new StockZhiGoldFragment();
        holder.fragment = mStockZhiGoldFragment;
        mHomeHolders[3] = holder;

        holder = new HomeTabHolder();
        holder.title = mActivity.getResources().getString(R.string.hangqing_shf);
        mTabs.add(holder.title);
        mStockSHFFragment = new StockSHFFragment();
        holder.fragment = mStockSHFFragment;
        mHomeHolders[4] = holder;

        holder = new HomeTabHolder();
        holder.title = mActivity.getResources().getString(R.string.hangqing_lme);
        mTabs.add(holder.title);
        mStockLMEFragment = new StockLMEFragment();
        holder.fragment = mStockLMEFragment;
        mHomeHolders[5] = holder;

        holder = new HomeTabHolder();
        holder.title = mActivity.getResources().getString(R.string.hangqing_nymex);
        mTabs.add(holder.title);
        mStockNYMEXFragment = new StockNYMEXFragment();
        holder.fragment = mStockNYMEXFragment;
        mHomeHolders[6] = holder;


        holder = new HomeTabHolder();
        holder.title = mActivity.getResources().getString(R.string.hangqing_comex);
        mTabs.add(holder.title);
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

        mAdapter = new LoginAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setCurrentItem(0);

        ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mActivity != null) {
                    ((HomeMainActivity) mActivity).stopRefreshAnim();
                }
            }
        }, 1000);

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.trade_more_tab:
                if (mPoPupWindow == null || (mPoPupWindow != null && !mPoPupWindow.isShowing())) {
                    mPoPupWindow = new SelectPopupWindow(mActivity, mTabs, mViewPager.getCurrentItem());
                    mPoPupWindow.setOnItemClickListener(new SelectPopupWindow.OnItemClickListener() {
                        @Override
                        public void itemClick(int position) {
                            if (mViewPager != null) {
                                mViewPager.setCurrentItem(position);
                            }
                        }
                    });
                    mPoPupWindow.showPopupWindow(mMoreTabLayout);
                }
                break;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mActivity != null) {
            ((HomeMainActivity) mActivity).stopRefreshAnim();
        }
        if (mPoPupWindow != null && mPoPupWindow.isShowing()) {
            mPoPupWindow.dismiss();
            mPoPupWindow = null;
        }
    }
}
