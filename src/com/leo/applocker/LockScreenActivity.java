package com.leo.applocker;

import android.app.Activity;
import android.os.Bundle;

public class LockScreenActivity extends Activity {

	public static final String EXTRA_LOCK_PKG = "extra_lock_pkg";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lockscreen);
	}

}
