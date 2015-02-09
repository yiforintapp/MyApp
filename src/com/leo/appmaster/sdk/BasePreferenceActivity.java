package com.leo.appmaster.sdk;

import android.preference.PreferenceActivity;

public class BasePreferenceActivity extends PreferenceActivity {

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
