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
import com.zlf.appmaster.ui.CommonToolbar;
import com.zlf.appmaster.ui.PagerSlidingTabStrip;

import java.util.List;

/**
 * Created by Administrator on 2016/11/2.
 */
public class WordLiveActivity extends BaseFragmentActivity {

    private ViewPager mViewPager;
    private HomeTabHolder[] mHomeHolders = new HomeTabHolder[5];
    private PagerSlidingTabStrip mPagerSlidingTab;
    private WordZhiBoFragment mLiveFragment;
    private WordChatFragment mChatFragment;
    private WordCreamFragment mCreamFragment;
    private WordAdviceFragment mAdviceFragment;
    private WordNoticeFragment mNoticeFragment;
    private CommonToolbar mToolBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_word);
        init();
    }

    private void init() {
        initFragment();
        initViewPager();
        mToolBar = (CommonToolbar) findViewById(R.id.word_toolbar);
        mPagerSlidingTab = (PagerSlidingTabStrip) findViewById(R.id.word_tab_tabs);
        mPagerSlidingTab.setBackgroundResource(R.color.white);
        mPagerSlidingTab.setShouldExpand(true);
        mPagerSlidingTab.setIndicatorColor(getResources().getColor(R.color.indicator_select_color));
        mPagerSlidingTab.setTextColor(R.color.black);
        mPagerSlidingTab.setTextSize(30);
        mPagerSlidingTab.setIndicatorHeight(6);
        mPagerSlidingTab.setDividerColor(getResources().getColor(R.color.white));
        mPagerSlidingTab.setViewPager(mViewPager);
    }

    private void initViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.word_viewpager);
        mViewPager.setAdapter(new ZhiBoAdapter(getSupportFragmentManager()));
        mViewPager.setOffscreenPageLimit(1); //预加载2个
        mViewPager.setCurrentItem(0);
    }

    private void initFragment() {
        HomeTabHolder holder = new HomeTabHolder();
        holder.title = this.getResources().getString(R.string.word_zhibo_title);
        mLiveFragment = new WordZhiBoFragment();
        holder.fragment = mLiveFragment;
        mHomeHolders[0] = holder;

        holder = new HomeTabHolder();
        holder.title = this.getResources().getString(R.string.word_chat_title);
        mChatFragment = new WordChatFragment();
        holder.fragment = mChatFragment;
        mHomeHolders[1] = holder;

        holder = new HomeTabHolder();
        holder.title = this.getResources().getString(R.string.word_cream_title);
        mCreamFragment = new WordCreamFragment();
        holder.fragment = mCreamFragment;
        mHomeHolders[2] = holder;

        holder = new HomeTabHolder();
        holder.title = this.getResources().getString(R.string.word_advice_title);
        mAdviceFragment = new WordAdviceFragment();
        holder.fragment = mAdviceFragment;
        mHomeHolders[3] = holder;

        holder = new HomeTabHolder();
        holder.title = this.getResources().getString(R.string.word_notice_title);
        mNoticeFragment = new WordNoticeFragment();
        holder.fragment = mNoticeFragment;
        mHomeHolders[4] = holder;

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
