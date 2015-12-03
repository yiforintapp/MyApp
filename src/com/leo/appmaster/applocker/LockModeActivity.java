
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
import com.leo.appmaster.sdk.SDKWrapper;
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


        mTtileBar.setNewStyleText(R.string.lock_mode);
        mTtileBar.setNewStyleBackViewListrener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mTtileBar.setNewStyle();
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
            mTtileBar.setOptionImageVisibility(View.INVISIBLE);
            mPagerTab.setOnPageChangeListener(new ModePageChangeListiner());
            Fragment f = mFragmentHolders.clone()[mEditIndex].fragment;
            if (f instanceof Editable) {
                ((Editable) f).onFinishEditMode();
            }
            // show help tip
            if (mEditIndex != 0) {
                showTitleBarOption(mEditIndex);
            }
        } else {
            finish();
        }
    }

    private void initFragment() {
        mFragmentHolders = new ModeFragmentHoler[3];

        ModeFragmentHoler holder = new ModeFragmentHoler();
        holder.title = getString(R.string.lock_mode_all);
        LockModeFragment lockFragment = new LockModeFragment();
        holder.fragment = lockFragment;
        mFragmentHolders[0] = holder;

        holder = new ModeFragmentHoler();
        holder.title = getString(R.string.lock_mode_time);
        TimeLockFragment timeLockFragment = new TimeLockFragment();
        holder.fragment = timeLockFragment;
        mFragmentHolders[1] = holder;

        holder = new ModeFragmentHoler();
        holder.title = getString(R.string.lock_mode_location);
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
        if (v.getId() == R.id.tv_option_image_content) {
            if (mEditMode) {
                Fragment f = mFragmentHolders[mEditIndex].fragment;
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
        mTtileBar.setOptionImage(R.drawable.un_delete);
        mViewPager.setScrollable(false);
        mTtileBar.setOptionImageVisibility(View.VISIBLE);
        mTtileBar.setOptionListener(null);
        mTtileBar.setOptionImageBackground(0);
        mPagerTab.setOnPageChangeListener(null);
    }

    public void onSelectItemChanged(int count) {
        if (count == 0) {
            mTtileBar.setOptionImage(R.drawable.un_delete);
            mTtileBar.setOptionListener(null);
            mTtileBar.setOptionImageBackground(0);
        } else {
            mTtileBar.setOptionImage(R.drawable.delete);
            mTtileBar.setOptionListener(this);
            mTtileBar.setOptionImageBackground(R.drawable.title_bar_option_selector);
        }
    }

    public CommonTitleBar getActivityCommonTitleBar() {
        if (null != mTtileBar) {
            return this.mTtileBar;
        }
        return null;
    }

    class ModePageChangeListiner implements OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onPageSelected(int position) {
            mTtileBar.setOptionImageVisibility(View.INVISIBLE);
            showTitleBarOption(position);
        }
    }

    private void showTitleBarOption(int currentPageItem) {
        OnClickListener listener = null;
        if (currentPageItem == 0) {
            return;
        }
        if (currentPageItem == 1) {
            mFragment = mFragmentHolders.clone()[currentPageItem].fragment;
            listener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppMasterPreference.getInstance(LockModeActivity.this)
                            .setTimeLockModeGuideClicked(true);
                    ((TimeLockFragment) mFragment).lockGuide();
                    /* SDK Event Mark */
                    SDKWrapper.addEvent(LockModeActivity.this, SDKWrapper.P1, "help", "time");
                }
            };
        } else if (currentPageItem == 2) {
            mFragment = mFragmentHolders.clone()[currentPageItem].fragment;
            listener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppMasterPreference.getInstance(LockModeActivity.this)
                            .setLocationLockModeGuideClicked(true);
                    ((LocationLockFragment) mFragment).lockGuide();
                    /* SDK Event Mark */
                    SDKWrapper.addEvent(LockModeActivity.this, SDKWrapper.P1, "help", "local");
                }
            };
        }

        mTtileBar.setOptionImageVisibility(View.VISIBLE);
        mTtileBar.setOptionImage(R.drawable.help_icon_n);
        if (null != listener) {
            mTtileBar.setOptionListener(listener);
        }
    }

    public void disableOptionImage() {
        mTtileBar.setOptionImage(R.drawable.un_delete);
        mTtileBar.setOptionListener(null);
        mTtileBar.setOptionImageBackground(0);
    }

}
