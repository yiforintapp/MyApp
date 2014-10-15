package com.leo.appmaster.applocker;

import com.leo.appmaster.R;
import com.leo.appmaster.fragment.GestureSettingFragment;
import com.leo.appmaster.fragment.PasswdSettingFragment;
import com.leo.appmaster.ui.CommonTitleBar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class LockSettingActivity extends FragmentActivity implements
		OnClickListener {

	public static final int LOCK_TYPE_PASSWD = 1;
	public static final int LOCK_TYPE_GESTURE = 2;

	private int mLockType = LOCK_TYPE_PASSWD;

	private TextView mTvSwitch;
	private CommonTitleBar mTitleBar;

	FragmentManager mFm;

	Fragment mPasswd, mGesture;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		setContentView(R.layout.activity_lock_setting);
		initUI();
		initFragment();
	}

	private void initFragment() {
		mPasswd = new PasswdSettingFragment();
		mGesture = new GestureSettingFragment();

		mFm = getSupportFragmentManager();

		FragmentTransaction tans = mFm.beginTransaction();

		int type = AppLockerPreference.getInstance(this).getLockType();
		if (type == AppLockerPreference.LOCK_TYPE_GESTURE) {
			tans.replace(R.id.fragment_contain, mGesture);
		} else {
			tans.replace(R.id.fragment_contain, mPasswd);
		}
		mLockType = LOCK_TYPE_PASSWD;
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
		mTitleBar.setOptionBackground(R.drawable.switch_passwd);
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
			mTitleBar.setOptionBackground(R.drawable.switch_gesture);
		} else {
			tans.replace(R.id.fragment_contain, mPasswd);
			mLockType = LOCK_TYPE_PASSWD;
			mTitleBar.setOptionBackground(R.drawable.switch_passwd);
		}
		tans.commit();
	}

}
