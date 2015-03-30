
package com.leo.appmaster.home;

import java.util.List;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.LockSettingActivity;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.manager.LockManager.OnUnlockedListener;
import com.leo.appmaster.fragment.ApplicaionAppFragment;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.fragment.GameAppFragment2;
import com.leo.appmaster.lockertheme.LockerTheme;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.LeoPagerTab;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class HotAppActivity extends FragmentActivity {
    private static final String MOVE_TO_NEW_APP = "move_to_new_app";
    private static final String MOVE_TO_GAME_FRAGMENT = "move_to_game_gragment";
    private LeoPagerTab mPagerTab;
    private ViewPager mViewPager;
    private ImageView iv_red_tip;
    private CommonTitleBar mTtileBar;
    private HotAppFragmentHoler[] mFragmentHolders = new HotAppFragmentHoler[2];
    private GameAppFragment2 gameFragment;
    private ApplicaionAppFragment appFragment;
    private AppMasterPreference sp_hot_app;
    
    boolean mFromStatusbar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_hotapp);
        mFromStatusbar = getIntent().getBooleanExtra("move_to_new_app", false);
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
        if(sp_hot_app.getHotAppActivityRedTip()){
            iv_red_tip.setVisibility(View.VISIBLE);
        }
        mViewPager = (ViewPager) findViewById(R.id.hotapp_app_viewpager);
        initFragment();

        mViewPager.setAdapter(new HotAppAdapter(getSupportFragmentManager()));
        mViewPager.setOffscreenPageLimit(2);
        mPagerTab.setViewPager(mViewPager);
    }

    public void dimissRedTip(){
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
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        try {
            super.onRestoreInstanceState(savedInstanceState, persistentState);
        } catch (Exception e) {
            
        }
    }
    
    @Override
    public void onBackPressed() {
        Intent intent = null;
        if (mFromStatusbar) {
            if (AppMasterPreference.getInstance(this).getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
                LockManager.getInstatnce().applyLock(LockManager.LOCK_MODE_FULL,
                        getPackageName(), true, new OnUnlockedListener() {
                            @Override
                            public void onUnlocked() {
                                LockManager.getInstatnce().timeFilter(getPackageName(), 500);
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
        super.onResume();

        if (mFromStatusbar) {
            // 指定去新品速递
            mViewPager.setCurrentItem(0);
        } /*else if (MOVE_TO_GAME_FRAGMENT.equals(intent.getAction())) {
            // 没指定就默认游戏中心
            mViewPager.setCurrentItem(1);
        }*/

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
}
