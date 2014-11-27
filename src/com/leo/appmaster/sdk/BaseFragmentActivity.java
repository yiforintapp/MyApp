
package com.leo.appmaster.sdk;

/**
 * Author: stonelam@leoers.com
 * Brief: all FragmentActivity should extends this class for SDK event track
 * */

import com.baidu.mobstat.StatService;

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

}
