package com.leo.appmaster.sdk;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;

public class BasePreferenceActivity extends PreferenceActivity {
    protected LockManager mLockManager;

    private ActivityLifeCircle mLifeCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);

        mLifeCircle = new ActivityLifeCircle(this);
        mLifeCircle.onCreate();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mLifeCircle.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mLifeCircle.onStop();
    }

    @Override
    protected void onPause() {
        SDKWrapper.onPause(this);
        super.onPause();

        mLifeCircle.onPause();
    }

    @Override
    protected void onResume() {
        SDKWrapper.onResume(this);
        super.onResume();

        mLifeCircle.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mLifeCircle.onDestroy();
    }
}
