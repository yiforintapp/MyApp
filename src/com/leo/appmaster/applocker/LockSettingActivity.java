package com.leo.appmaster.applocker;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.fragment.GestureSettingFragment;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.fragment.PasswdSettingFragment;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.ui.CommonTitleBar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class LockSettingActivity extends BaseFragmentActivity implements
		OnClickListener {

	public static final String RESET_PASSWD_FLAG = "reset_passwd";

	public static final int LOCK_TYPE_PASSWD = 1;
	public static final int LOCK_TYPE_GESTURE = 2;
	private int mLockType = LOCK_TYPE_PASSWD;
	private TextView mTvSwitch;
	private CommonTitleBar mTitleBar;
	private  FragmentManager mFm;
	private PasswdSettingFragment mPasswd;
	private GestureSettingFragment mGesture;

	private boolean mResetFlag;

	private String mFromActivity;

	private boolean mShouldLockOnRestart = true;

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
		if (mShouldLockOnRestart) {
			if (AppMasterPreference.getInstance(this).getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
				showLockPage();
			}

		} else {
			mShouldLockOnRestart = true;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		mShouldLockOnRestart = false;
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void showLockPage() {
		Intent intent = new Intent(this, LockScreenActivity.class);
		int lockType = AppMasterPreference.getInstance(this).getLockType();
		if (lockType == AppMasterPreference.LOCK_TYPE_PASSWD) {
			intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
					LockFragment.LOCK_TYPE_PASSWD);
		} else {
			intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
					LockFragment.LOCK_TYPE_GESTURE);
		}
		intent.putExtra(LockScreenActivity.EXTRA_UNLOCK_FROM,
				LockFragment.FROM_SELF);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		startActivityForResult(intent, 1000);
	}

	private void handleIntent() {
		Intent intent = getIntent();
		mResetFlag = intent.getBooleanExtra(RESET_PASSWD_FLAG, false);
		mFromActivity = intent
				.getStringExtra(LockScreenActivity.EXTRA_TO_ACTIVITY);
	}

	private void initFragment() {
		mPasswd = new PasswdSettingFragment();
		mGesture = new GestureSettingFragment();
		mPasswd.setActivityName(mFromActivity);
		mGesture.setActivityName(mFromActivity);

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
