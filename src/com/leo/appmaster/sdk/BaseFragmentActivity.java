
package com.leo.appmaster.sdk;

/**
 * Author: stonelam@leoers.com
 * Brief: all FragmentActivity should extends this class for SDK event track
 */

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.leo.appmaster.mgr.BatteryManager;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.WifiSecurityManager;

public class BaseFragmentActivity extends FragmentActivity {
    protected LockManager mLockManager;
    private ActivityLifeCircle mLifeCircle;
//    protected WifiSecurityManager mWifiManager;
//    protected BatteryManager mBatteryManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
        } catch (Exception e) {
        } catch (Error error) {
        }
        mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
//        mWifiManager = (WifiSecurityManager) MgrContext.getManager(MgrContext.MGR_WIFI_SECURITY);
//        mBatteryManager = (BatteryManager) MgrContext.getManager(MgrContext.MGR_BATTERY);

        mLifeCircle = new ActivityLifeCircle(this);
        mLifeCircle.onCreate();
    }

    @Override
    protected void onStart() {
        try {
            super.onStart();
        } catch (Exception e) {
        } catch (Error error) {
        }

        mLifeCircle.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mLifeCircle.onStop();
    }

    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mLifeCircle.onDestroy();
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();
        } catch (Exception e) {
        }
        SDKWrapper.onResume(this);

        mLifeCircle.onResume();
    }

    @Override
    protected void onPause() {
        SDKWrapper.onPause(this);
        super.onPause();

        mLifeCircle.onPause();
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {
        }
    }
    
    @Override
    protected void onPostResume() {
        try {
            super.onPostResume();
        } catch (Exception e) {
        }
    }

}
