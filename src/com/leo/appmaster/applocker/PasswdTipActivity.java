package com.leo.appmaster.applocker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.utils.LeoLog;

public class PasswdTipActivity extends BaseActivity implements OnClickListener {
	CommonTitleBar mTitleBar;
	EditText mEtTip;
	TextView mTvMakesure;
	private boolean mShouldLockOnRestart = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_passwd_tip);
		initUI();
	}

	private void initUI() {
		mEtTip = (EditText) findViewById(R.id.et_passwd_tip);
		mTvMakesure = (TextView) findViewById(R.id.tv_make_sure);
		mTvMakesure.setOnClickListener(this);
		mTitleBar = (CommonTitleBar) findViewById(R.id.commonTitleBar1);
		mTitleBar.setTitle(R.string.passwd_notify);
		mTitleBar.openBackView();
		String tip = AppMasterPreference.getInstance(this).getPasswdTip();
		if (tip != null) {
			mEtTip.setText(tip);
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if (mShouldLockOnRestart ) {
			showLockPage();
		} else {
			mShouldLockOnRestart  = true;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		LeoLog.d("LockOptionActivity", "onActivityResault: requestCode = "
				+ requestCode + "    resultCode = " + resultCode);
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
//		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//				| Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivityForResult(intent, 1000);
	}

	@Override
	public void onClick(View v) {
		if (v == mTvMakesure) {
			String tip = mEtTip.getText().toString().trim();
			AppMasterPreference ap = AppMasterPreference.getInstance(this);
			String q = ap.getPpQuestion();
			String a = ap.getPpAnwser();
			AppMasterPreference.getInstance(this).savePasswdProtect(q, a, tip);
			Toast.makeText(this, R.string.set_success, 0).show();
			finish();
		}
	}

}
