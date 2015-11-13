
package com.leo.appmaster.appmanage;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;

import com.leo.appmaster.R;
import com.leo.appmaster.appmanage.view.ManagerFlowFragment;
import com.leo.appmaster.appmanage.view.ManagerFlowListFragment;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.LeoPagerTab;
import com.leo.appmaster.wifiSecurity.WifiSecurityActivity;
import com.leo.appmaster.wifiSecurity.WifiSettingActivity;

public class FlowActivity extends BaseFragmentActivity implements OnClickListener,
        OnPageChangeListener {
    public static final String MESSAGE_MONTH_TRAFFI_SMALL_SETTING = "month_traffic_small_setting";
    private LeoPagerTab mPagerTab;
    private ViewPager mViewPager;
    private CommonToolbar mTitleBar;
    //    private View trffic_setting_iv;
    private ManagerFlowListFragment trifficListFragment;
    private ManagerFlowFragment trifficFragment;

    private ManagerFlowFragmentHoler[] mFragmentHolders = new ManagerFlowFragmentHoler[2];

    // private int mDeskType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flow_trafficlist);
        initUI();
    }

    private void initUI() {
        mTitleBar = (CommonToolbar) findViewById(R.id.traffic_title_bar);
        mTitleBar.setToolbarTitle(R.string.app_flow_elec);
        mTitleBar.setToolbarColorResource(R.color.cb);
        mTitleBar.setOptionClickListener(this);
        mTitleBar.setNavigationClickListener(this);
        mTitleBar.setOptionImageResource(R.drawable.setup_icon);
        mTitleBar.setOptionMenuVisible(true);

//        mTtileBar.setTitle(R.string.app_flow_elec);
//        mTtileBar.openBackView();

//        trffic_setting_iv = findViewById(R.id.trffic_setting_iv);
//        trffic_setting_iv.setVisibility(View.VISIBLE);
//        trffic_setting_iv.setOnClickListener(this);

        mPagerTab = (LeoPagerTab) findViewById(R.id.traffic_app_tab_indicator);
        mPagerTab.setOnPageChangeListener(this);
        mPagerTab.setBackgroundResource(R.color.cb);
        mViewPager = (ViewPager) findViewById(R.id.traffic_app_viewpager);
        initFragment();

        mViewPager.setAdapter(new ManagerFlowAdapter(getSupportFragmentManager()));
        mViewPager.setOffscreenPageLimit(2);
        mPagerTab.setViewPager(mViewPager);
    }

    // @Override
    // protected void onResume() {
    // Intent intent = getIntent();
    // mDeskType = intent.getIntExtra(StatusBarEventService.EXTRA_EVENT_TYPE,
    // -1);
    // super.onResume();
    // }

    private void initFragment() {
        ManagerFlowFragmentHoler holder = new ManagerFlowFragmentHoler();
        holder.title = this.getString(R.string.app_flow);
        trifficFragment = new ManagerFlowFragment();
        holder.fragment = trifficFragment;
        mFragmentHolders[0] = holder;

        holder = new ManagerFlowFragmentHoler();
        holder.title = this.getString(R.string.app_elec);
        trifficListFragment = new ManagerFlowListFragment();
        holder.fragment = trifficListFragment;
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

    class ManagerFlowFragmentHoler {
        String title;
        BaseFragment fragment;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.trffic_setting_iv:
//                Intent intent = new Intent(this, TrafficSetting.class);
//                startActivity(intent);
                break;
            case R.id.ct_option_1_rl:
                Intent intent = new Intent(this, TrafficSetting.class);
                startActivity(intent);
                break;
            case R.id.ct_back_rl:
                onBackPressed();
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
        if (arg0 == 1) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "datapage", "usagelist");
        }
    }

}
