
package com.leo.appmaster.applocker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.sdk.SDKWrapper;

public class VisitorModeProxyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        boolean mQuickLockMode = intent.getBooleanExtra("quick_lock_mode", false);
        String mQuickModeName = intent.getStringExtra("lock_mode_name");
        int mQuiclModeId = intent.getIntExtra("lock_mode_id", -1);
        Intent shortcutIntent = new Intent(this, LockScreenActivity.class);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        shortcutIntent.putExtra("quick_lock_mode", mQuickLockMode);
        shortcutIntent.putExtra("lock_mode_id", mQuiclModeId);
        shortcutIntent.putExtra("lock_mode_name", mQuickModeName);
        startActivity(shortcutIntent);
        finish();
    }
}
