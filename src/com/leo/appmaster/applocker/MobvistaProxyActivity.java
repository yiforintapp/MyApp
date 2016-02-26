package com.leo.appmaster.applocker;

import android.app.Activity;
import android.os.Bundle;

import com.leo.appmaster.applocker.manager.MobvistaEngine;
import com.leo.appmaster.applocker.manager.MobvistaEngine.MobvistaListener;
import com.leo.appmaster.utils.LeoLog;
import com.mobvista.sdk.m.core.entity.Campaign;

/**
 * Mobvista常驻广告Activity，会被mobvista长期持有，减少内存占用
 * @author Jasper
 *
 */
public class MobvistaProxyActivity extends Activity implements MobvistaListener {

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        
        LeoLog.i("MobvistaProxyActivity", "proxy oncrete.");
        MobvistaEngine.getInstance(this).loadMobvista("", this);
        
        finish();
    }

    @Override
    public void onMobvistaFinished(int code, Campaign campaign, String msg) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onMobvistaClick(Campaign campaign, String unitID) {
        // TODO Auto-generated method stub
        
    }

}
