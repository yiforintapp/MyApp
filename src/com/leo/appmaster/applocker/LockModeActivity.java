
package com.leo.appmaster.applocker;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.LeoPagerTab;

public class LockModeActivity extends BaseFragmentActivity implements OnClickListener {
    private LeoPagerTab mPagerTab;
    private EditableViewPager mViewPager;
    public ModeFragmentHoler[] mFragmentHolders;
    private CommonTitleBar mTtileBar;
    private boolean mEditMode;
    private int mEditIndex;
    private Fragment mFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_mode);
        initUI();
    }

    private void initUI() {
        mPagerTab = (LeoPagerTab) findViewById(R.id.tab_indicator);
        mViewPager = (EditableViewPager) findViewById(R.id.viewpager);
        mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        
        initFragment();
        mViewPager.setAdapter(new HomePagerAdapter(getSupportFragmentManager()));
        mViewPager.setOffscreenPageLimit(2);
        mPagerTab.setViewPager(mViewPager);
        mPagerTab.setOnPageChangeListener(new ModePageChangeListiner());
        
        mTtileBar.setTitle(R.string.lock_mode);
        mTtileBar.setBackViewListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // mTtileBar.setOptionImageVisibility(View.VISIBLE);
        // mTtileBar.setOptionImage(R.drawable.mode_add_button);
         mTtileBar.setOptionListener(this);
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
        if (mEditMode) {
            mEditMode = false;
            mPagerTab.setVisibility(View.VISIBLE);
            mViewPager.setScrollable(true);
            mPagerTab.setCurrentItem(mEditIndex);
            // mTtileBar.setOptionImage(R.drawable.mode_add_button);
            mTtileBar.setOptionImageVisibility(View.INVISIBLE);
            Fragment f = mFragmentHolders.clone()[mEditIndex].fragment;
            if (f instanceof Editable) {
                ((Editable) f).onFinishEditMode();
            }
        } else {
            super.onBackPressed();
        }
    }

    private void initFragment() {
        mFragmentHolders = new ModeFragmentHoler[3];

        ModeFragmentHoler holder = new ModeFragmentHoler();
        holder.title = this.getString(R.string.lock_mode_all);
        LockModeFragment lockFragment = new LockModeFragment();
        holder.fragment = lockFragment;
        mFragmentHolders[0] = holder;

        holder = new ModeFragmentHoler();
        holder.title = this.getString(R.string.lock_mode_time);
        TimeLockFragment timeLockFragment = new TimeLockFragment();
        holder.fragment = timeLockFragment;
        mFragmentHolders[1] = holder;

        holder = new ModeFragmentHoler();
        holder.title = this.getString(R.string.lock_mode_location);
        LocationLockFragment locationFragment = new LocationLockFragment();
        holder.fragment = locationFragment;
        mFragmentHolders[2] = holder;
        
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

    class HomePagerAdapter extends FragmentPagerAdapter {
        public HomePagerAdapter(FragmentManager fm) {
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

    class ModeFragmentHoler {
        String title;
        BaseFragment fragment;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_option_image) {
            if (mEditMode) {
                Fragment f = mFragmentHolders.clone()[mEditIndex].fragment;
                if (f instanceof Editable) {
                    ((Editable) f).onChangeItem();
                }
            }
        }
    }

    public void onEditMode(int i) {
        mEditMode = true;
        mEditIndex = i;
        mPagerTab.setVisibility(View.GONE);
        mTtileBar.setOptionImage(R.drawable.delete);
        mViewPager.setScrollable(false);
        mTtileBar.setOptionImageVisibility(View.VISIBLE);
    }
    
    public CommonTitleBar getActivityCommonTitleBar(){
        if(null != mTtileBar){
            return this.mTtileBar;
        }
        return null;
    }
    
    
    class ModePageChangeListiner implements OnPageChangeListener{

        @Override
        public void onPageScrollStateChanged(int arg0) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onPageSelected(int arg0) {
            mTtileBar.setOptionImageVisibility(View.INVISIBLE);

            if (arg0 == 1) {
                if (AppMasterPreference.getInstance(LockModeActivity.this).getTimeLockModeGuideClicked()) {
                    mFragment = mFragmentHolders.clone()[arg0].fragment;
                    //if guide page don't open then show help tip
                    if(!((TimeLockFragment) mFragment).getGuideOpenState()){
                        mTtileBar.setOptionImageVisibility(View.VISIBLE);
                        mTtileBar.setOptionImage(R.drawable.selector_help_icon);
                        mTtileBar.setOptionListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ((TimeLockFragment) mFragment).lockGuide();
                            }
                        });
                    }
                }
            } else if (arg0 == 2) {
                if (AppMasterPreference.getInstance(LockModeActivity.this) .getLocationLockModeGuideClicked()) {
                    mFragment = mFragmentHolders.clone()[arg0].fragment;
                  //if guide page don't open then show help tip
                    if (!((LocationLockFragment) mFragment).getGuideOpenState()) {
                        mTtileBar.setOptionImageVisibility(View.VISIBLE);
                        mTtileBar.setOptionImage(R.drawable.selector_help_icon);
                        mTtileBar.setOptionListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ((LocationLockFragment) mFragment).lockGuide();
                            }
                        });
                    }
                }
            }
        }
    }
    
}
