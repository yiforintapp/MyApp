package com.leo.appmaster.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.sdk.push.ui.WebViewActivity;

public class ProxyActivity extends Activity {
    
    private boolean mDelayFinish = false;
    private Handler mHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        int type = intent.getIntExtra(StatusBarEventService.EXTRA_EVENT_TYPE, StatusBarEventService.EVENT_EMPTY);
        if (type == StatusBarEventService.EVENT_EMPTY) {
            mDelayFinish = true;
            mHandler = new Handler();
        } else {
            Intent intentService = new Intent(this, StatusBarEventService.class);
            intentService.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, type);
         /*   if(type == StatusBarEventService.EVENT_APP_WEBVIEW_NOTIFICATION){
                String url = intent.getStringExtra(WebViewActivity.WEB_URL);
                intentService.putExtra(WebViewActivity.WEB_URL, url);
            }*/
            startService(intentService);
            finish();
        }
        
    }

    @Override
    protected void onResume() {
        super.onResume();
        SDKWrapper.onResume(this);
        if (mDelayFinish && mHandler != null) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "fdau", "view");
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
        SDKWrapper.onPause(this);
        mDelayFinish = false;
        mHandler = null;
    }
    
    

}
