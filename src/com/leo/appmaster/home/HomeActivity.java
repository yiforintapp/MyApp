package com.leo.appmaster.home;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.service.LockService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class HomeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		startLockService();
	}

	private void startLockService() {
		Intent serviceIntent = new Intent(this, LockService.class);
		serviceIntent.putExtra(LockService.EXTRA_STARTUP_FROM, "main activity");
		startService(serviceIntent);
	}
}
