package com.leo.appmaster.applocker;

import com.leo.appmaster.R;
import com.leo.appmaster.fragment.GestureSettingFragment;
import com.leo.appmaster.fragment.PasswdSettingFragment;
import com.leo.appmaster.ui.CommonTitleBar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class LockSettingActivity extends FragmentActivity implements
		OnClickListener {

	public static final String RESET_PASSWD_FLAG = "reset_passwd";

	public static final int LOCK_TYPE_PASSWD = 1;
	public static final int LOCK_TYPE_GESTURE = 2;
	private int mLockType = LOCK_TYPE_PASSWD;
	private TextView mTvSwitch;
	private CommonTitleBar mTitleBar;
	FragmentManager mFm;
	PasswdSettingFragment mPasswd;
	GestureSettingFragment mGesture;

	private boolean mResetFlag;

	private String mFromActivity;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_lock_setting);
		handleIntent();
		initUI();
		initFragment();
	}

	private void handleIntent() {
		Intent intent = getIntent();
		mResetFlag = intent.getBooleanExtra(RESET_PASSWD_FLAG, false);
		mFromActivity = intent
				.getStringExtra(LockScreenActivity.EXTRA_FROM_ACTIVITY);
	}

	private void initFragment() {
		mPasswd = new PasswdSettingFragment();
		mGesture = new GestureSettingFragment();
		mPasswd.setActivityName(mFromActivity);
		mGesture.setActivityName(mFromActivity);
		mFm = getSupportFragmentManager();
		FragmentTransaction tans = mFm.beginTransaction();
		int type = AppLockerPreference.getInstance(this).getLockType();
		if (type == AppLockerPreference.LOCK_TYPE_GESTURE) {
			mLockType = LOCK_TYPE_GESTURE;
			tans.replace(R.id.fragment_contain, mGesture);
			mTitleBar.setOptionText(getString(R.string.switch_passwd));
			if (mResetFlag) {
				mTitleBar.setTitle(R.string.reset_gesture);
			}
		} else {
			mLockType = LOCK_TYPE_PASSWD;
			tans.replace(R.id.fragment_contain, mPasswd);
			mTitleBar.setOptionText(getString(R.string.switch_gesture));
			if (mResetFlag) {
				mTitleBar.setTitle(R.string.reset_passwd);
			}
		}
		tans.commit();

	}

	private void initUI() {
		mTitleBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		mTitleBar.openBackView();
		mTitleBar.setOptionListener(this);

		mTvSwitch = (TextView) findViewById(R.id.tv_option_text);
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
			if (mResetFlag) {
				mTitleBar.setTitle(R.string.reset_gesture);
			}
			mTitleBar.setOptionText(getString(R.string.switch_passwd));
		} else {
			tans.replace(R.id.fragment_contain, mPasswd);
			mLockType = LOCK_TYPE_PASSWD;
			if (mResetFlag) {
				mTitleBar.setTitle(R.string.reset_passwd);
			}
			mTitleBar.setOptionText(getString(R.string.switch_gesture));
		}
		tans.commit();
	}

	public boolean isResetPasswd() {
		return mResetFlag;
	}

}
