
package com.leo.appmaster.sdk;

/**
 * Author: stonelam@leoers.com
 * Brief: all FragmentActivity should extends this class for SDK event track
 * */

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;

public class BaseFragmentActivity extends FragmentActivity {
    protected LockManager mLockManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppMasterApplication.getInstance().addActivity(this);
        try {
            super.onCreate(savedInstanceState);
        } catch (Exception e) {
            
        }
        mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
    }
    
    @Override
    protected void onStart() {
        try {
            super.onStart();
        } catch (Exception e) {            
        } catch (Error error) {            
        }
        AppMasterApplication.getInstance().resumeActivity(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        AppMasterApplication.getInstance().pauseActivity(this);
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
    
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {
        }
    }

}
