package com.leo.appmaster.sdk;

import com.baidu.mobstat.StatService;

import android.preference.PreferenceActivity;

public class BasePreferenceActivity extends PreferenceActivity {

    @Override
    protected void onPause() {
        StatService.onPause(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        StatService.onResume(this);
        super.onResume();
    }

}
