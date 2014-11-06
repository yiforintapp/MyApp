
package com.leo.appmaster;

/**
 * Author: stonelam@leoers.com
 * Brief: Base activity to be tracked by application so that we can finish them when completely exit is required
 * */

import com.baidu.mobstat.StatService;
import com.flurry.android.FlurryAgent;
import com.leoers.leoanalytics.LeoStat;

import android.app.Activity;
import android.os.Bundle;

public class BaseActivity extends Activity {

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
        LeoStat.checkForceUpdate();
    }

    @Override
    protected void onPause() {
        StatService.onPause(this);
        super.onPause();
    }

    @Override
    protected void onStart() {
        FlurryAgent.onStartSession(this, "QCKRJN2WQNJN9QBKS5DD");
        super.onStart();
    }

    @Override
    protected void onStop() {
        FlurryAgent.onEndSession(this);
        super.onStop();
    }

}
