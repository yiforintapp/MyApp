package com.leo.appmaster.applocker;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.logic.LockHandler;
import com.leo.appmaster.fragment.GestureLockFragment;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.fragment.PasswdLockFragment;
import com.leo.appmaster.ui.CommonTitleBar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

public class LockScreenActivity extends FragmentActivity {

	public static String EXTRA_UNLOCK_FROM = "extra_unlock_from";
	public static String EXTRA_UKLOCK_TYPE = "extra_unlock_type";

	int mFrom;

	private CommonTitleBar mTtileBar;
	private LockFragment mFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lock_setting);

		handleIntent();

		initUI();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.e("xxxx", "onNewIntent");
		super.onNewIntent(intent);
	}

	private void handleIntent() {
		Intent intent = getIntent();
		int type = intent.getIntExtra(EXTRA_UKLOCK_TYPE,
				LockFragment.LOCK_TYPE_PASSWD);
		if (type == LockFragment.LOCK_TYPE_PASSWD) {
			mFragment = new PasswdLockFragment();
		} else {
			mFragment = new GestureLockFragment();
		}
		mFrom = intent.getIntExtra(EXTRA_UNLOCK_FROM, LockFragment.FROM_SELF);

		mFragment.setFrom(mFrom);
		mFragment.setPackage(intent
				.getStringExtra(LockHandler.EXTRA_LOCKED_APP_PKG));
	}

	private void initUI() {
		mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		mTtileBar.setTitle(R.string.app_lock);

		if (mFrom == LockFragment.FROM_SELF) {
			mTtileBar.openBackView();
		}

		FragmentManager fm = getSupportFragmentManager();

		FragmentTransaction tans = fm.beginTransaction();
		tans.replace(R.id.fragment_contain, mFragment);
		tans.commit();
	}

	@Override
	public void onBackPressed() {
		if (mFrom == LockFragment.FROM_SELF) {
			super.onBackPressed();
		}
	}
}
