package com.leo.appmaster.applocker;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.TimeView;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

public class WaitActivity extends Activity {

	private TextView mTvTime;
	private int mWaitTime = 11;
	private UpdateTask mTask;

	private TimeView mTimeView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wait);
		initUI();
		startWaitTime();
		mTask = new UpdateTask();
	}

	private void startWaitTime() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
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
				WaitActivity.this.finish();
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

	private void initUI() {
		mTvTime = (TextView) findViewById(R.id.tv_wait_time);
		mTimeView = (TimeView) findViewById(R.id.time_view);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	private class UpdateTask implements Runnable {
		@Override
		public void run() {
			mTvTime.setText("00:" + mWaitTime);
		}
	}
}
