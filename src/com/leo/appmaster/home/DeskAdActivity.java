
package com.leo.appmaster.home;

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
        mHandler = new Handler();
        mWallAd = MobvistaEngine.getInstance().createAdWallController(this);
        if (mWallAd != null) {
            // preload the wall data
            mWallAd.preloadWall();
        }

        Intent mWallIntent = mWallAd.getWallIntent();
        startActivity(mWallIntent);
        LockManager.getInstatnce().timeFilterSelf();
        // Intent intent = new Intent(this,HomeActivity.class);
        // startActivity(intent);
        // finish();
    }

    @Override
    protected void onResume() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 2000);
        super.onResume();
    }

}
