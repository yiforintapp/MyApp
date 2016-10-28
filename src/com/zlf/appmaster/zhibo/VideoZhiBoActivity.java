
package com.zlf.appmaster.zhibo;

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


public class VideoZhiBoActivity extends BaseFragmentActivity {

    private ViewPager mViewPager;
    private HomeTabHolder[] mHomeHolders = new HomeTabHolder[2];
    private PagerSlidingTabStrip mPagerSlidingTab;
    private ZhiBoChatFragment chatFragment;
    private ZhiBoDataFragment dataFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_zhibo);

        initViews();
    }

    private void initViews() {
        mViewPager = (ViewPager) findViewById(R.id.vedio_viewpager);
        initFragment();
        initViewPager();
        mPagerSlidingTab = (PagerSlidingTabStrip) findViewById(R.id.zhibo_tab_tabs);
        mPagerSlidingTab.setBackgroundResource(R.color.ctc);
        mPagerSlidingTab.setShouldExpand(true);
        mPagerSlidingTab.setIndicatorColor(getResources().getColor(R.color.indicator_select_color));
        mPagerSlidingTab.setDividerColor(getResources().getColor(R.color.ctc));
        mPagerSlidingTab.setViewPager(mViewPager);
    }

    private void initViewPager() {
        mViewPager.setAdapter(new ZhiBoAdapter(getSupportFragmentManager()));
        mViewPager.setOffscreenPageLimit(1); //预加载2个
        mViewPager.setCurrentItem(0);
    }

    private void initFragment() {
        HomeTabHolder holder = new HomeTabHolder();
        holder.title = this.getResources().getString(R.string.zhibo_chat);
        chatFragment = new ZhiBoChatFragment();
        holder.fragment = chatFragment;
        mHomeHolders[0] = holder;

        holder = new HomeTabHolder();
        holder.title = this.getResources().getString(R.string.zhibo_data);
        dataFragment = new ZhiBoDataFragment();
        holder.fragment = dataFragment;
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


    class ZhiBoAdapter extends FragmentPagerAdapter {
        public ZhiBoAdapter(FragmentManager fm) {
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
