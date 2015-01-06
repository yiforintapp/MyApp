
package com.leo.appmaster.applocker;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.EdgeEffectCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.lockertheme.LockerTheme;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.LeoPictureViewPager;
import com.leo.appmaster.ui.LeoPictureViewPager.OnPageChangeListener;

public class LockHelpSettingTip extends Activity {
    private CommonTitleBar mTitle;
    private LeoPictureViewPager mViewPager;
    private List<LockHelpItemPager> mHelpPager;
    private LockHelpPagerAdapter mAdapter;
    private EdgeEffectCompat leftEdge;
    private EdgeEffectCompat rightEdge;
    private int mCurrentFlag;
    private List<String> mHelpSettingPager;
    public static final int CURRENT_FLAG_CODE = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_help_setting_tip);
        mHelpPager = new ArrayList<LockHelpItemPager>();
        initUI();
        mAdapter = new LockHelpPagerAdapter(this);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setPageMargin(getResources().getDimensionPixelSize(
                R.dimen.help_setting_page_margin));
        mViewPager.setOffscreenPageLimit(3);
        setScroll();
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (leftEdge != null && rightEdge != null) {
                    leftEdge.finish();
                    rightEdge.finish();
                    leftEdge.setSize(0, 0);
                    rightEdge.setSize(0, 0);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void onResume() {
        getHelpPager();
        mAdapter.notifyDataSetChanged();
        if (mCurrentFlag != CURRENT_FLAG_CODE) {
            if (!mHelpPager.get(0).getTitle().equals(mHelpSettingPager.get(0))) {
                mViewPager.setCurrentItem(mCurrentFlag - 1);
            } else {
                mViewPager.setCurrentItem(mCurrentFlag);
            }
            mCurrentFlag = CURRENT_FLAG_CODE;
        }
        super.onResume();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        mHelpPager.clear();
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            mCurrentFlag = data.getExtras().getInt("help_setting_current");
        }
    }

    private void initUI() {
        mTitle = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        mTitle.setTitle(R.string.help_setting_tip_title);
        mTitle.openBackView();
        mViewPager = (LeoPictureViewPager) findViewById(R.id.help_setting);
    }

    private void getHelpPager() {
        String[] lockHelpSettingTitle = getResources().getStringArray(
                R.array.lock_help_setting_title);
        mHelpSettingPager = Arrays.asList(lockHelpSettingTitle);
        int mType = AppMasterPreference.getInstance(this).getLockType();
        boolean flag = AppMasterPreference.getInstance(this).getIsHelpSettingChangeSucess();
        for (String string : mHelpSettingPager) {
            String content = null;
            String button = null;

            if (string.equals(mHelpSettingPager.get(0)) && !flag) {
                if (mType == AppMasterPreference.LOCK_TYPE_GESTURE) {
                    content = getString(R.string.lock_help_password_setting_content_password);
                } else if (mType == AppMasterPreference.LOCK_TYPE_PASSWD) {
                    content = getString(R.string.lock_help_password_setting_content_gesture);
                }
                button = getString(R.string.lock_help_password_setting_button);
                LockHelpItemPager pager = new LockHelpItemPager(string, content, button);
                mHelpPager.add(pager);
            } else if (string.equals(mHelpSettingPager.get(1))) {
                content = getString(R.string.lock_help_lock_setting_content);
                button = getString(R.string.lock_help_lock_setting_button);
                LockHelpItemPager pager = new LockHelpItemPager(string, content, button);
                mHelpPager.add(pager);
            } else if (string.equals(mHelpSettingPager.get(2))) {
                content = getString(R.string.lock_help_lock_theme_content);
                button = getString(R.string.lock_help_lock_theme_setting_button);
                LockHelpItemPager pager = new LockHelpItemPager(string, content, button);
                mHelpPager.add(pager);
            }

        }

    }

    @SuppressWarnings("unused")
    private class LockHelpPagerAdapter extends PagerAdapter {
        private Context context;

        public LockHelpPagerAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {

            return mHelpPager.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {

            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final LockHelpItemPager lockHelpPager = mHelpPager.get(position);
            String title = lockHelpPager.getTitle();
            String content = lockHelpPager.getContent();
            String button = lockHelpPager.getButton();
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View view = layoutInflater.inflate(R.layout.activity_lock_help_item_viewpager, null);
            TextView titleTV = (TextView) view.findViewById(R.id.lock_help_title);
            TextView contentTV = (TextView) view.findViewById(R.id.lock_help_content);
            TextView buttonTV = (TextView) view.findViewById(R.id.lock_help_bt);
            titleTV.setText(title);
            contentTV.setText(content);
            buttonTV.setText(button);
            container.addView(view);
            buttonTV.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    String buttonText = lockHelpPager.getButton();
                    if (buttonText.equals(getString(R.string.lock_help_password_setting_button))) {
                        enterLockPage();
                    } else if (buttonText.equals(getString(R.string.lock_help_lock_setting_button))) {
                        Intent intent = new Intent(LockHelpSettingTip.this,
                                LockOptionActivity.class);
                        intent.putExtra("help_setting_current", 1);
                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        try {
                            startActivityForResult(intent, 1);
                        } catch (Exception e) {
                        }
                    } else if (buttonText
                            .equals(getString(R.string.lock_help_lock_theme_setting_button))) {
                        Intent intent = new Intent(LockHelpSettingTip.this, LockerTheme.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra("to_online_theme", true);
                        intent.putExtra("help_setting_current", 2);
                        try {
                            startActivityForResult(intent, 1);
                        } catch (Exception e) {
                        }
                    }
                }
            });
            return view;
        }

    }

    private void setScroll() {
        try {

            Field leftEdgeField = mViewPager.getClass().getDeclaredField("mLeftEdge");
            Field rightEdgeField = mViewPager.getClass().getDeclaredField("mRightEdge");
            if (leftEdgeField != null && rightEdgeField != null) {
                leftEdgeField.setAccessible(true);
                rightEdgeField.setAccessible(true);
                leftEdge = (EdgeEffectCompat) leftEdgeField.get(mViewPager);
                rightEdge = (EdgeEffectCompat) rightEdgeField.get(mViewPager);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enterLockPage() {
        Intent intent = null;
        int lockType = AppMasterPreference.getInstance(this).getLockType();
        intent = new Intent(this, LockScreenActivity.class);
        intent.putExtra(LockScreenActivity.EXTRA_UNLOCK_FROM,
                LockFragment.FROM_SELF_HOME);
        intent.putExtra(LockScreenActivity.EXTRA_TO_ACTIVITY,
                AppLockListActivity.class.getName());
        if (lockType == AppMasterPreference.LOCK_TYPE_PASSWD) {
            intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
                    LockFragment.LOCK_TYPE_PASSWD);
        } else {
            intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
                    LockFragment.LOCK_TYPE_GESTURE);
        }
        intent.putExtra(LockScreenActivity.EXTRA_TO_ACTIVITY, LockSettingActivity.class.getName());
        startActivity(intent);

    }

}
