
package com.leo.appmaster.appmanage;

import java.util.List;

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
import android.widget.ImageView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.LockSettingActivity;
import com.leo.appmaster.appmanage.view.ApplicaionAppFragment;
import com.leo.appmaster.appmanage.view.GameAppFragment2;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.LeoPagerTab;
import com.leo.appmaster.utils.LeoLog;

public class HotAppActivity extends BaseFragmentActivity implements OnPageChangeListener  {
    public static final String FROME_STATUSBAR = "from_statusbar";
    public static final String SHOW_PAGE = "show_page";

    public static final int PAGE_APP = 0;
    public static final int PAGE_GAME = 1;

    // private static final String MOVE_TO_GAME_FRAGMENT =
    // "move_to_game_gragment";
    private LeoPagerTab mPagerTab;
    private ViewPager mViewPager;
    private ImageView iv_red_tip;
    private CommonTitleBar mTtileBar;
    private HotAppFragmentHoler[] mFragmentHolders = new HotAppFragmentHoler[2];
    private GameAppFragment2 gameFragment;
    private ApplicaionAppFragment appFragment;
    private AppMasterPreference sp_hot_app;

    private boolean mFromStatusbar;
    private int mPage = PAGE_APP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_hotapp);
        Intent intent = getIntent();
        if (intent != null) {
            mFromStatusbar = intent.getBooleanExtra(FROME_STATUSBAR, false);
            mPage = intent.getIntExtra(SHOW_PAGE, PAGE_APP);
        }
        initUI();
    }

    private void initUI() {
        SDKWrapper.addEvent(this, SDKWrapper.P1, "hots", "home");
        sp_hot_app = AppMasterPreference.getInstance(this);
        mTtileBar = (CommonTitleBar) findViewById(R.id.hotapp_title_bar);
        mTtileBar.setTitle(R.string.app_hot_app);
        mTtileBar.openBackView();
        mTtileBar.setBackViewListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mPagerTab = (LeoPagerTab) findViewById(R.id.hotapp_app_tab_indicator);
        iv_red_tip = (ImageView) findViewById(R.id.iv_red_tip);
        if (sp_hot_app.getHotAppActivityRedTip()) {
            iv_red_tip.setVisibility(View.VISIBLE);
        }
        mPagerTab.setOnPageChangeListener(this);
        mViewPager = (ViewPager) findViewById(R.id.hotapp_app_viewpager);
        initFragment();

        mViewPager.setAdapter(new HotAppAdapter(getSupportFragmentManager()));
        mViewPager.setOffscreenPageLimit(2);
        mPagerTab.setViewPager(mViewPager);
    }

    public void dimissRedTip() {
        iv_red_tip.setVisibility(View.GONE);
        sp_hot_app.setHotAppActivityRedTip(false);
        sp_hot_app.setHomeFragmentRedTip(false);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {

        }

    }

    @Override
    public void onBackPressed() {
        Intent intent = null;
        if (mFromStatusbar) {
            if (AppMasterPreference.getInstance(this).getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
                LeoLog.d("Track Lock Screen", "apply lockscreen form HotAppActivity onBackPressed");
                final LockManager manager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
                manager.applyLock(LockManager.LOCK_MODE_FULL,
                        getPackageName(), true, new LockManager.OnUnlockedListener() {
                            @Override
                            public void onUnlocked() {
                                manager.filterPackage(getPackageName(), 500);
                                Intent intent = new Intent(HotAppActivity.this, HomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                AppMasterApplication.getInstance().startActivity(intent);
                                HotAppActivity.this.finish();
                            }

                            @Override
                            public void onUnlockOutcount() {

                            }

                            @Override
                            public void onUnlockCanceled() {
                            }
                        });
            } else {
                intent = new Intent(this, LockSettingActivity.class);
                startActivity(intent);
                finish();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        if (mViewPager != null) {
//            LeoLog.d("testHot", "onResume , mPage is : " + mPage);
            mViewPager.setCurrentItem(mPage);
        }
        super.onResume();
        SDKWrapper.addEvent(this, SDKWrapper.P1, "tdau", "hots");
    }
    
    @Override
    protected void onStop() {
        //离开时记录现在在which page
        mPage = mViewPager.getCurrentItem();
//        LeoLog.d("testHot", "onStop , mPage is : " + mPage);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
//        LeoLog.d("testHot", "onDestroy");
        super.onDestroy();
    }

    private void initFragment() {
        HotAppFragmentHoler holder = new HotAppFragmentHoler();

        holder.title = this.getString(R.string.app_application);
        appFragment = new ApplicaionAppFragment();
        holder.fragment = appFragment;
        mFragmentHolders[0] = holder;

        holder = new HotAppFragmentHoler();
        holder.title = this.getString(R.string.app_game);
        gameFragment = new GameAppFragment2();
        holder.fragment = gameFragment;
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

    class HotAppAdapter extends FragmentPagerAdapter {
        public HotAppAdapter(FragmentManager fm) {
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

    class HotAppFragmentHoler {
        String title;
        BaseFragment fragment;
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        
    }

    @Override
    public void onPageSelected(int arg0) {
//        LeoLog.d("testHot", "onPageSelected , mPage is : " + arg0);
        mPage = arg0;
    }

}
