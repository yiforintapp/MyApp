
package com.leo.appmaster.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.leo.appmaster.Constants;
import com.leo.appmaster.applocker.manager.MobvistaEngine;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.sdk.SDKWrapper;
import com.mobvista.sdk.m.core.MobvistaAdWall;

public class DeskAdActivity extends Activity {
    private MobvistaAdWall mWallAd;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWallAd = MobvistaEngine.getInstance(this).createAdWallController(this, Constants.UNIT_ID_61);
        if (mWallAd != null) {
            mWallAd.preloadWall();
        }
        // LockManager.getInstatnce().timeFilter(this.getPackageName(), 1000);
        // LockManager.getInstatnce().timeFilterSelf();
        SDKWrapper.addEvent(this, SDKWrapper.P1, "ad_cli", "adv_cnts_desktop");
        LockManager lockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
        lockManager.filterPackage(getPackageName(), 5 * 1000);
        Intent mWallIntent = mWallAd.getWallIntent();
        mWallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        try {
            startActivity(mWallIntent);
        } catch (Exception e) {           
        }
        finish();
    }

}
