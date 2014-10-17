package com.leo.appmaster.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.leo.appmaster.R;

public class SplashActivity extends Activity {

	public static final int MSG_LAUNCH_HOME_ACTIVITY = 1000;

	private Handler mEventHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		mEventHandler = new EventHandler(this);
		mEventHandler.sendEmptyMessageDelayed(MSG_LAUNCH_HOME_ACTIVITY, 1000);
	}

	private static class EventHandler extends Handler {
		SplashActivity sa;

		public EventHandler(SplashActivity sa) {
			super();
			this.sa = sa;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_LAUNCH_HOME_ACTIVITY:
				Intent intent = new Intent(sa, HomeActivity.class);
				sa.startActivity(intent);
				sa.finish();
				break;

			default:
				break;
			}
		}
	}
}
