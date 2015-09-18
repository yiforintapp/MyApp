
package com.leo.appmaster.home;

import com.leo.appmaster.Constants;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.manager.MobvistaEngine;
import com.mobvista.sdk.m.core.MobvistaAdWall;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class DeskAdActivity extends Activity {
    private MobvistaAdWall mWallAd;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWallAd = MobvistaEngine.getInstance().createAdWallController(this, Constants.UNIT_ID_61);
        if (mWallAd != null) {
            mWallAd.preloadWall();
        }
        // LockManager.getInstatnce().timeFilter(this.getPackageName(), 1000);
        Intent mWallIntent = mWallAd.getWallIntent();
        mWallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        LockManager.getInstatnce().timeFilterSelf();
        startActivity(mWallIntent);

        finish();
    }


}
