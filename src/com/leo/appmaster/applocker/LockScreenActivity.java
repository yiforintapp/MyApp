package com.leo.appmaster.applocker;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.logic.LockHandler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class LockScreenActivity extends Activity {

	private String mCurLockedApp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lockscreen);
		handleIntent();
	}

	private void handleIntent() {
		mCurLockedApp = getIntent().getStringExtra(
				LockHandler.EXTRA_LOCKED_APP_PKG);
	}

	@Override
	public void onBackPressed() {
		if(mCurLockedApp != null) {
			sendUnlockAppNotification();
		}
		super.onBackPressed();
	}

	private void sendUnlockAppNotification() {
		Intent intent = new Intent(LockHandler.ACTION_APP_UNLOCKED);
		intent.putExtra(LockHandler.EXTRA_LOCKED_APP_PKG, mCurLockedApp);
	}
}
