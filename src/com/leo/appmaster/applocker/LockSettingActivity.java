
package com.leo.appmaster.applocker;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.fragment.GestureSettingFragment;
import com.leo.appmaster.fragment.PasswdSettingFragment;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.ui.CommonTitleBar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;

public class LockSettingActivity extends BaseFragmentActivity implements
        OnClickListener {

    public static final String RESET_PASSWD_FLAG = "reset_passwd";

    public static final int LOCK_TYPE_PASSWD = 1;
    public static final int LOCK_TYPE_GESTURE = 2;
    private int mLockType = LOCK_TYPE_PASSWD;
    private CommonTitleBar mTitleBar;
    private FragmentManager mFm;
    private PasswdSettingFragment mPasswd;
    private GestureSettingFragment mGesture;

    private boolean mResetFlag;

    public boolean mToLockList;
    public boolean mJustFinish;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_lock_setting);
        handleIntent();
        initUI();
        initFragment();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleIntent() {
        Intent intent = getIntent();
        mResetFlag = intent.getBooleanExtra(RESET_PASSWD_FLAG, false);
        mToLockList = intent.getBooleanExtra("to_lock_list", false);
        mJustFinish = intent.getBooleanExtra("just_finish", false);
    }

    private void initFragment() {
        mPasswd = new PasswdSettingFragment();
        mGesture = new GestureSettingFragment();

        if (mResetFlag) {
            mTitleBar.setTitle(R.string.reset_passwd);
        }

        mFm = getSupportFragmentManager();
        FragmentTransaction tans = mFm.beginTransaction();
        int type = AppMasterPreference.getInstance(this).getLockType();
        if (type == AppMasterPreference.LOCK_TYPE_GESTURE) {
            mLockType = LOCK_TYPE_GESTURE;
            tans.replace(R.id.fragment_contain, mGesture);
            mTitleBar.setOptionText(getString(R.string.switch_passwd));
        } else {
            mLockType = LOCK_TYPE_PASSWD;
            tans.replace(R.id.fragment_contain, mPasswd);
            mTitleBar.setOptionText(getString(R.string.switch_gesture));
        }
        tans.commit();

    }

    private void initUI() {
        mTitleBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        mTitleBar.openBackView();
        mTitleBar.setOptionListener(this);
        mTitleBar.setOptionListener(this);
        mTitleBar.setOptionTextVisibility(View.VISIBLE);
        mTitleBar.setOptionText("");
        mTitleBar.setTitle(R.string.passwd_setting);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_option_text:
                switchLockType();
                break;

            default:
                break;
        }
    }

    private void switchLockType() {
        FragmentTransaction tans = mFm.beginTransaction();
        if (mLockType == LOCK_TYPE_PASSWD) {
            tans.replace(R.id.fragment_contain, mGesture);
            mLockType = LOCK_TYPE_GESTURE;
            mTitleBar.setOptionText(getString(R.string.switch_passwd));
        } else {
            tans.replace(R.id.fragment_contain, mPasswd);
            mLockType = LOCK_TYPE_PASSWD;
            mTitleBar.setOptionText(getString(R.string.switch_gesture));
        }
        tans.commit();
    }

    public boolean isResetPasswd() {
        return mResetFlag;
    }
}
