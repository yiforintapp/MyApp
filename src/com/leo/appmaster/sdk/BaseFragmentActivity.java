package com.leo.appmaster.sdk;

/**
 * Author: stonelam@leoers.com
 * Brief: all FragmentActivity should extends this class for SDK event track
 * */

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.leo.appmaster.AppMasterApplication;

public class BaseFragmentActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		AppMasterApplication.getInstance().addActivity(this);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		AppMasterApplication.getInstance().removeActivity(this);
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		SDKWrapper.onResume(this);
	}

	@Override
	protected void onPause() {
	    SDKWrapper.onPause(this);
		super.onPause();
	}

}
