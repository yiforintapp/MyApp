
package com.leo.appmaster.sdk;

/**
 * Author: stonelam@leoers.com
 * Brief: all FragmentActivity should extends this class for SDK event track
 * */

import com.baidu.mobstat.StatService;
import com.flurry.android.FlurryAgent;

import android.support.v4.app.FragmentActivity;

public class BaseFragmentActivity extends FragmentActivity {

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

    @Override
    protected void onStart() {
        // TODO: switch this when release
        FlurryAgent.onStartSession(this, "N5ZHBYQH7FT5XBY52H7M"); // debug Key
        // FlurryAgent.onStartSession(this, "QCKRJN2WQNJN9QBKS5DD"); // release
        // key
        super.onStart();
    }

    @Override
    protected void onStop() {
        FlurryAgent.onEndSession(this);
        super.onStop();
    }

}
