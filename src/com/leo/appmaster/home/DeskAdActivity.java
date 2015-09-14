
package com.leo.appmaster.home;

import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.manager.MobvistaEngine;
import com.mobvista.sdk.m.core.MobvistaAdWall;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class DeskAdActivity extends Activity {
    private MobvistaAdWall mWallAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWallAd = MobvistaEngine.getInstance().createAdWallController(this);
        if (mWallAd != null) {
            // preload the wall data
            mWallAd.preloadWall();
        }

        LockManager.getInstatnce().timeFilterSelf();
        Intent mWallIntent = mWallAd.getWallIntent();
        startActivity(mWallIntent);
        // Intent intent = new Intent(this,HomeActivity.class);
        // startActivity(intent);
        // finish();
    }
}
