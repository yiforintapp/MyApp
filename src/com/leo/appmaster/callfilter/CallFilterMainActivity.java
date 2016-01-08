
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
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.CommonEvent;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.mgr.CallFilterContextManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.CallFilterContextManagerImpl;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.LeoPagerTab;
import com.leo.appmaster.utils.LeoLog;

import java.util.ArrayList;
import java.util.List;

public class CallFilterMainActivity extends BaseFragmentActivity implements OnClickListener,
        OnPageChangeListener {

    private static final int BLACK_TAB = 0;
    private static final int FILTER_TAB = 1;

    private LeoPagerTab mPagerTab;
    private ViewPager mViewPager;
    private CommonToolbar mTitleBar;

    private BlackListFragment mBlackListFragment;
    private CallFilterFragment mCallFilterFragment;
    private boolean mNeedToHomeWhenFinish = false;
    private CallFilterFragmentHoler[] mFragmentHolders = new CallFilterFragmentHoler[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_filter_main);
        initUI();
        mNeedToHomeWhenFinish = getIntent().getBooleanExtra("needToHomeWhenFinish", false);
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
        LeoLog.i("tess", "needMoveToTab2 = " + getIntent().getBooleanExtra("needMoveToTab2", false));
        if (getIntent().getBooleanExtra("needMoveToTab2", false)) {
            mViewPager.setCurrentItem(1);
        }
    }

    @Override
    public void finish() {
        if (mNeedToHomeWhenFinish) {
            mNeedToHomeWhenFinish = false;
            Intent intent=new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            super.finish();
        } else {
            super.finish();
        }
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        mNeedToHomeWhenFinish = intent.getBooleanExtra("needToHomeWhenFinish", false);
        LeoLog.i("CallFilterMainActivity", "new intent ! mNeedToHomeWhenFinish = " + mNeedToHomeWhenFinish);
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

    @Override
    protected void onDestroy() {
        CallFilterManager.getInstance(this).setIsFilterTab(false);
        super.onDestroy();
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

    protected CallFilterContextManager mCallManger =
            (CallFilterContextManager) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);

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
        if (arg0 == FILTER_TAB) {
            CallFilterManager.getInstance(this).setIsFilterTab(true);
        } else if (arg0 == BLACK_TAB) {
            CallFilterManager.getInstance(this).setIsFilterTab(false);
        }
    }

    public void blackListShowEmpty() {
        if (mBlackListFragment != null) {
            mBlackListFragment.showEmpty();
        }
    }

    public void blackListReload() {
        if (mBlackListFragment != null) {
            mBlackListFragment.loadData();
        }
    }

    public void callFilterShowEmpty() {
        if (mCallFilterFragment != null) {
            mCallFilterFragment.showEmpty();
        }
    }

    public void moveToFilterFragment() {
        mViewPager.setCurrentItem(1);
    }

}
