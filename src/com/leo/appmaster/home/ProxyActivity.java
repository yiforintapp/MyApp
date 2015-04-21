package com.leo.appmaster.home;

import com.leo.appmaster.applocker.service.StatusBarEventService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ProxyActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        int type = intent.getIntExtra(StatusBarEventService.EXTRA_EVENT_TYPE, -1);
        Intent intentService = new Intent(this, StatusBarEventService.class);
        intentService.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, type);
        startService(intentService);
        
        finish();
    }
    
    

}
