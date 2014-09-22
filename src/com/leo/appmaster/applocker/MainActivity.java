package com.leo.appmaster.applocker;


import com.leo.appmaster.R;
import com.leo.appmaster.applocker.service.LockService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		Intent serviceIntent = new Intent(this, LockService.class);
		serviceIntent.putExtra(LockService.EXTRA_STARTUP_FROM, "main activity");
		startService(serviceIntent);
    }
}
