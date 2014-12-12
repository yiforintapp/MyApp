package com.leo.appmaster.sdk;

/**
 * Author: stonelam@leoers.com
 * Brief: all FragmentActivity should extends this class for SDK event track
 * */

import com.baidu.mobstat.StatService;
import com.leo.appmaster.AppMasterApplication;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

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
		StatService.onResume(this);
	}

	@Override
	protected void onPause() {
		StatService.onPause(this);
		super.onPause();
	}

}
