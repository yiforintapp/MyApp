package com.leo.appmaster.home;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

public class ProxyActivity extends Activity {
    
    private boolean mDelayFinish = false;
    private Handler mHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDelayFinish && mHandler != null) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 1000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDelayFinish = false;
        mHandler = null;
    }
    
    

}
