package com.leo.appmaster.applocker;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.logic.LockHandler;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.TimeView;

public class WaitActivity extends BaseActivity {

	private TextView mTvTime;
	private int mWaitTime = 11;
	private UpdateTask mTask;
	private TimeView mTimeView;

	private String mPackage;
	private boolean returned;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wait);
		handleIntent();
		initUI();
		startWaitTime();
		mTask = new UpdateTask();
	}

	private void handleIntent() {
		Intent intent = getIntent();
		mPackage = intent.getStringExtra(LockHandler.EXTRA_LOCKED_APP_PKG);
	}

	
	@Override
	protected void onStop() {
		returned = true;
		super.onStop();
	}

	private void startWaitTime() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					if (returned) {
						return;
					}
					mTvTime.post(mTask);
					mWaitTime--;
					if (mWaitTime == 0) {
						break;
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				returnBack();
//				finish();
			}
		}).start();
		ValueAnimator va = ValueAnimator.ofFloat(0f, 360f);
		va.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator v) {
				float d = (Float) v.getAnimatedValue();
				mTimeView.updateDegree(d);
			}
		});
		va.setDuration(10000);
		va.setInterpolator(new LinearInterpolator());
		va.start();
	}

	private void returnBack() {
		if (returned) {
			return;
		}
		returned = true;
		Intent intent = new Intent(this, LockScreenActivity.class);
		int lockType = AppMasterPreference.getInstance(WaitActivity.this)
				.getLockType();
		if (lockType == AppMasterPreference.LOCK_TYPE_PASSWD) {
			intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
					LockFragment.LOCK_TYPE_PASSWD);
		} else {
			intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
					LockFragment.LOCK_TYPE_GESTURE);
		}
		if (mPackage == null || mPackage.equals("")) {
			intent.putExtra(LockScreenActivity.EXTRA_UNLOCK_FROM,
					LockFragment.FROM_SELF);
		} else {
			intent.putExtra(LockHandler.EXTRA_LOCKED_APP_PKG, mPackage);
			intent.putExtra(LockScreenActivity.EXTRA_UNLOCK_FROM,
					LockFragment.FROM_OTHER);
		}
		startActivity(intent);
		finish();
	}

	private void initUI() {
		mTvTime = (TextView) findViewById(R.id.tv_wait_time);
		mTimeView = (TimeView) findViewById(R.id.time_view);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
//		returnBack();
	}

	private class UpdateTask implements Runnable {
		@Override
		public void run() {
			mTvTime.setText("00:" + mWaitTime);
		}
	}
}
