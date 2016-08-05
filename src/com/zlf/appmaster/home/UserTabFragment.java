package com.zlf.appmaster.home;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.zlf.appmaster.R;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.stocknews.NewsFlashFragment;
import com.zlf.appmaster.ui.PagerSlidingTabStrip;
import com.zlf.appmaster.ui.stock.StockBaseFragment;

import java.util.List;

/**
 * Created by Administrator on 2016/7/18.
 */
public class UserTabFragment  extends BaseFragment {

    private ViewPager mViewPager;
    private UserTabHolder[] mUserHolders = new UserTabHolder[1];
    private NewsFlashFragment mNewsFlashFragment;
    private PagerSlidingTabStrip mPagerSlidingTab;

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_user;
    }

    @Override
    protected void onInitUI() {
        mViewPager = (ViewPager) findViewById(R.id.user_tab_viewpager);
        initFragment();
        mViewPager.setAdapter(new LoginAdapter(getChildFragmentManager()));
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setCurrentItem(0);
        mPagerSlidingTab = (PagerSlidingTabStrip) findViewById(R.id.user_tab_tabs);
//        DisplayMetrics dm = getResources().getDisplayMetrics();
//        // 设置Tab是自动填充满屏幕的
//        mPagerSlidingTab.setShouldExpand(true);
//        // 设置Tab的分割线是透明的
//        mPagerSlidingTab.setDividerColor(Color.TRANSPARENT);
//        // 设置Tab底部线的高度
//        mPagerSlidingTab.setUnderlineHeight((int) TypedValue.applyDimension(
//                TypedValue.COMPLEX_UNIT_DIP, 1, dm));
//        // 设置Tab Indicator的高度
//        mPagerSlidingTab.setIndicatorHeight((int) TypedValue.applyDimension(
//                TypedValue.COMPLEX_UNIT_DIP, 4, dm));
//        // 设置Tab标题文字的大小
//        mPagerSlidingTab.setTextSize((int) TypedValue.applyDimension(
//                TypedValue.COMPLEX_UNIT_SP, 16, dm));
//        // 设置Tab Indicator的颜色
//        mPagerSlidingTab.setIndicatorColor(Color.parseColor("#D83A3E"));
//        // 设置选中Tab文字的颜色 (这是我自定义的一个方法)
//        mPagerSlidingTab.setSelectedTextColor(Color.parseColor("#D83A3E"));
//        // 取消点击Tab时的背景色
//        mPagerSlidingTab.setTabBackground(0);
        mPagerSlidingTab.setBackgroundResource(R.color.tab_background);
        mPagerSlidingTab.setShouldExpand(true);
        mPagerSlidingTab.setIndicatorColor(getResources().getColor(R.color.main_icon_red));
        mPagerSlidingTab.setDividerColor(0xffffffff);
        mPagerSlidingTab.setViewPager(mViewPager);
    }

    private void initFragment() {
        UserTabHolder holder = new UserTabHolder();
        holder.title = "国鑫评论";
        mNewsFlashFragment = new NewsFlashFragment();
        holder.fragment = mNewsFlashFragment;
        mUserHolders[0] = holder;


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
            return mUserHolders[position].fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mUserHolders[position].title;
        }

        @Override
        public int getCount() {
            return mUserHolders.length;
        }
    }

    class UserTabHolder {
        String title;
        StockBaseFragment fragment;
    }
}
