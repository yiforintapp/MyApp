
package com.leo.appmaster.callfilter;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.home.DeskProxyActivity;
import com.leo.appmaster.imagehide.ImageHideMainActivity;
import com.leo.appmaster.mgr.CallFilterManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.LeoPagerTab;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.QuickHelperUtils;
import com.leo.appmaster.utils.Utilities;

import java.util.List;

public class CallFilterMainActivity extends BaseFragmentActivity implements OnClickListener,
        OnPageChangeListener {

    public static final int BLACK_TAB = 0;
    public static final int FILTER_TAB = 1;

    private LeoPagerTab mPagerTab;
    private ViewPager mViewPager;
    private CommonToolbar mTitleBar;
    private BlackListFragment mBlackListFragment;
    private CallFilterFragment mCallFilterFragment;
    private boolean mNeedToHomeWhenFinish = false;
    private String mIsFromPushNotif = "";
    private CallFilterFragmentHoler[] mFragmentHolders = new CallFilterFragmentHoler[2];
    private LEOAlarmDialog mShareDialog;
    private PreferenceTable mPt;
    private LEOAlarmDialog mDialogAskCreateShotcut;
    private static final int ACCUMULATIVE_TOTAL_TO_ASK_CREATE_SHOTCUT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_filter_main);
        CallFilterHelper.getInstance(this).setIsFilterTab(false);
        CallFilterHelper.getInstance(this).setCurrFilterTab(BLACK_TAB);
        initUI();
        mNeedToHomeWhenFinish = getIntent().getBooleanExtra("needToHomeWhenFinish", false);
        mIsFromPushNotif = getIntent().getStringExtra("from");
        SDKWrapper.addEvent(this, SDKWrapper.P1, "block", "block_cnts");
        mPt = PreferenceTable.getInstance();
    }
    
    @Override
    public void onBackPressed() {
        if (!mPt.getBoolean(PrefConst.KEY_HAS_ASK_CREATE_SHOTCUT_CALLFILTER, false) && mPt.getInt(PrefConst.KEY_ACCUMULATIVE_TOTAL_ENTER_CALLFILTER, 0) >= ACCUMULATIVE_TOTAL_TO_ASK_CREATE_SHOTCUT) {
            mPt.putBoolean(PrefConst.KEY_HAS_ASK_CREATE_SHOTCUT_CALLFILTER, true);
            if (mDialogAskCreateShotcut == null) {
                mDialogAskCreateShotcut = new LEOAlarmDialog(this);
            }
            mDialogAskCreateShotcut.setTitleVisiable(false);
            mDialogAskCreateShotcut.setDialogIcon(R.drawable.qh_call_filter);
            mDialogAskCreateShotcut.setContent(getString(R.string.ask_create_shortcut_content_callfilter));
            mDialogAskCreateShotcut.setLeftBtnStr(getString(R.string.cancel));
            mDialogAskCreateShotcut.setRightBtnStr(getString(R.string.ask_create_shortcut_button_right));
            mDialogAskCreateShotcut.setRightBtnListener(new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SDKWrapper.addEvent(CallFilterMainActivity.this, SDKWrapper.P1, "assistant", "shortcut_block");
                    Intent intent = new Intent();
                    intent = new Intent(AppMasterApplication.getInstance(), DeskProxyActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, DeskProxyActivity.IDX_CALL_FILTER);
                    intent.putExtra("from_quickhelper", true);
                    QuickHelperUtils.createQuickHelper(getString(R.string.quick_helper_callfilter), R.drawable.qh_call_filter, intent, CallFilterMainActivity.this);
                    Toast.makeText(CallFilterMainActivity.this, getString(R.string.quick_help_add_toast), Toast.LENGTH_SHORT).show();
                    mDialogAskCreateShotcut.dismiss();
                }
            });
            mDialogAskCreateShotcut.setLeftBtnListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SDKWrapper.addEvent(CallFilterMainActivity.this, SDKWrapper.P1, "assistant", "shortcut_block_no");
                    mDialogAskCreateShotcut.dismiss();
                }
            });
            mDialogAskCreateShotcut.show();
        } else {
            super.onBackPressed();
        }
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
        showShareDialog();
    }

    @Override
    public void finish() {
        if (mNeedToHomeWhenFinish || !TextUtils.isEmpty(mIsFromPushNotif)) {
            mNeedToHomeWhenFinish = false;
            mIsFromPushNotif = "";
            Intent intent = new Intent(Intent.ACTION_MAIN);
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
        mIsFromPushNotif = getIntent().getStringExtra("from");
        LeoLog.i("CallFilterMainActivity", "new intent ! mNeedToHomeWhenFinish = " + mNeedToHomeWhenFinish);
        if (intent.getBooleanExtra("needMoveToTab2", false)) {
            mViewPager.setCurrentItem(1);
        }
        showShareDialog();
    }

    private void showShareDialog() {
        Intent i = getIntent();
        String from = i.getStringExtra("from");
        if (from != null && from != "") {
            return;
        }
        PreferenceTable preferenceTable = PreferenceTable.getInstance();
        int currentTimes = preferenceTable.getInt(PrefConst.ENTER_CALL_FILTER_TIMES, 1);
        int limitTimes = preferenceTable.getInt(PrefConst.KEY_CALL_FILTER_SHARE_TIMES, 10);
        if (currentTimes < limitTimes) {  // 小于限制次数
            preferenceTable.putInt(PrefConst.ENTER_CALL_FILTER_TIMES, currentTimes + 1);
            return;
        }
        if (preferenceTable.getBoolean(PrefConst.CALL_FILTER_SHOW, false)) {
            return;
        }
        if (mShareDialog == null) {
            mShareDialog = new LEOAlarmDialog(CallFilterMainActivity.this);
            mShareDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (mShareDialog != null) {
                        mShareDialog = null;
                    }
                }
            });
        }
        String content = getString(R.string.callfilter_share_dialog_content);
        String shareButton = getString(R.string.share_dialog_btn_query);
        String cancelButton = getString(R.string.share_dialog_query_btn_cancel);
        mShareDialog.setContent(content);
        mShareDialog.setLeftBtnStr(cancelButton);
        mShareDialog.setRightBtnStr(shareButton);
        mShareDialog.setLeftBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SDKWrapper.addEvent(CallFilterMainActivity.this, SDKWrapper.P1, "block", "block_noShare");
                if (mShareDialog != null && mShareDialog.isShowing()) {
                    mShareDialog.dismiss();
                    mShareDialog = null;
                }
            }
        });
        mShareDialog.setRightBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (mShareDialog != null && mShareDialog.isShowing()) {
                    mShareDialog.dismiss();
                    mShareDialog = null;
                }
                shareApps();
            }
        });
        mShareDialog.show();
        preferenceTable.putBoolean(PrefConst.CALL_FILTER_SHOW, true);
    }


    /** 分享应用 */
    private void shareApps() {
        SDKWrapper.addEvent(CallFilterMainActivity.this, SDKWrapper.P1, "block", "block_share");
        mLockManager.filterSelfOneMinites();
        PreferenceTable sharePreferenceTable = PreferenceTable.getInstance();
        boolean isContentEmpty = TextUtils.isEmpty(
                sharePreferenceTable.getString(PrefConst.KEY_CALL_FILTER_SHARE_CONTENT));
        boolean isUrlEmpty = TextUtils.isEmpty(
                sharePreferenceTable.getString(PrefConst.KEY_CALL_FILTER_SHARE_URL));

        StringBuilder shareBuilder = new StringBuilder();
        if (!isContentEmpty && !isUrlEmpty) {
            shareBuilder.append(sharePreferenceTable.getString(PrefConst.KEY_CALL_FILTER_SHARE_CONTENT))
                        .append(" ")
                        .append(sharePreferenceTable.getString(PrefConst.KEY_CALL_FILTER_SHARE_URL));
        } else {
            shareBuilder.append(getResources().getString(R.string.callfilter_share_content))
                        .append(" ")
                        .append(Constants.DEFAULT_SHARE_URL);
        }
        Utilities.toShareApp(shareBuilder.toString(), getTitle().toString(), CallFilterMainActivity.this);
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
        CallFilterHelper.getInstance(this).setIsFilterTab(false);
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

    protected CallFilterManager mCallManger =
            (CallFilterManager) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ct_back_rl:
                onBackPressed();
                break;
            case R.id.ct_option_1_rl:
                SDKWrapper.addEvent(this, SDKWrapper.P1, "block", "settings_cnts");
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
            CallFilterHelper.getInstance(this).setIsFilterTab(true);
            CallFilterHelper.getInstance(this).setCurrFilterTab(FILTER_TAB);
        } else if (arg0 == BLACK_TAB) {
            CallFilterHelper.getInstance(this).setIsFilterTab(false);
            CallFilterHelper.getInstance(this).setCurrFilterTab(BLACK_TAB);
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
