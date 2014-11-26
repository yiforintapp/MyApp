package com.leo.appmaster.sdk;

/**
 * Author: stonelam@leoers.com
 * Brief: Base activity to be tracked by application so that we can finish them when completely exit is required
 * */

import com.baidu.mobstat.StatService;
import com.flurry.android.FlurryAgent;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.applocker.ILock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class BaseActivity extends Activity implements ILock {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		onActivityCreate();
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
        if (SDKWrapper.isMTJActivated()) {
            StatService.onResume(this);
        }
    }

    @Override
    protected void onPause() {
        if (SDKWrapper.isMTJActivated()) {
            StatService.onPause(this);
        }
        super.onPause();
    }
	@Override
	protected void onStart() {
        // TODO: switch this when release
        FlurryAgent.onStartSession(this, "N5ZHBYQH7FT5XBY52H7M"); // debug Key
        // FlurryAgent.onStartSession(this, "QCKRJN2WQNJN9QBKS5DD"); // release key
		super.onStart();
	}

	@Override
	protected void onStop() {
		FlurryAgent.onEndSession(this);
		super.onStop();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		onActivityResault(requestCode, resultCode);
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onRestart() {
		onActivityRestart();
		super.onRestart();
	}

	@Override
	public void onActivityCreate() {

	}

	@Override
	public void onActivityRestart() {

	}

	@Override
	public void onActivityResault(int requestCode, int resultCode) {

	}

}
