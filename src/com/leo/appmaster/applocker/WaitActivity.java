package com.leo.appmaster.applocker;

import com.leo.appmaster.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class WaitActivity extends Activity {
	
	private TextView mTvTime;
	private int mWaitTime = 10;
	private UpdateTask mTask;

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
				while (mWaitTime > 0) {
					mTvTime.post(mTask);
					mWaitTime--;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				WaitActivity.this.finish();
			}
		}).start();
	}

	private void initUI() {
		mTvTime = (TextView) findViewById(R.id.tv_wait_time);
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
