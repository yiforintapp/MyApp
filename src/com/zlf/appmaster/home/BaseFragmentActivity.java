
package com.zlf.appmaster.home;

/**
 * Author: stonelam@leoers.com
 * Brief: all FragmentActivity should extends this class for SDK event track
 */

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;

public class BaseFragmentActivity extends FragmentActivity {
//    protected UpdateManager mWifiManager;
//    protected BatteryManager mBatteryManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
        } catch (Throwable error) {
            error.printStackTrace();
        }
//        mWifiManager = (UpdateManager) MgrContext.getManager(MgrContext.MGR_WIFI_SECURITY);
//        mBatteryManager = (BatteryManager) MgrContext.getManager(MgrContext.MGR_BATTERY);

    }

    @Override
    protected void onStart() {
        try {
            super.onStart();
        } catch (Throwable e) {
        }


    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();
        } catch (Throwable e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onResume() {
        try {
            super.onResume();
        } catch (Throwable e) {
        }

    }

    @Override
    protected void onPause() {
        try {
            super.onPause();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Throwable e) {
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        try {
            super.onSaveInstanceState(outState);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostResume() {
        try {
            super.onPostResume();
        } catch (Throwable e) {
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return false;
    }
}
