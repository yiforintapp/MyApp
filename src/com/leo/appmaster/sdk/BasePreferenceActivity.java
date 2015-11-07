package com.leo.appmaster.sdk;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;

public class BasePreferenceActivity extends PreferenceActivity {
    protected LockManager mLockManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
    }

    @Override
    protected void onStart() {
        super.onStart();
        AppMasterApplication.getInstance().resumeActivity(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        AppMasterApplication.getInstance().pauseActivity(this);
    }

    @Override
    protected void onPause() {
        SDKWrapper.onPause(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        SDKWrapper.onResume(this);
        super.onResume();
    }

}
