package com.leo.appmaster.applocker;

import android.app.Activity;
import android.os.Bundle;

import com.leo.appmaster.ad.ADEngineWrapper;
import com.leo.appmaster.applocker.manager.MobvistaEngine;
import com.leo.appmaster.applocker.manager.MobvistaEngine.MobvistaListener;
import com.leo.appmaster.utils.LeoLog;

import java.util.List;

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
        MobvistaEngine.getInstance(this).loadMobvista("", ADEngineWrapper.AD_TYPE_NATIVE, this);
        
        finish();
    }

    @Override
    public void onMobvistaFinished(int code, List<com.mobvista.msdk.out.Campaign> campaign, String msg) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onMobvistaClick(com.mobvista.msdk.out.Campaign campaign, String unitID) {
        // TODO Auto-generated method stub
        
    }

}
