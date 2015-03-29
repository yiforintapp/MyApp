
package com.leo.appmaster.applocker.service;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.appmanage.AppListActivity;
import com.leo.appmaster.home.HotAppActivity;
import com.leo.appmaster.lockertheme.LockerTheme;
import com.leo.appmaster.utils.LeoLog;

import android.app.IntentService;
import android.content.Intent;
import android.os.Parcelable;

/**
 * this service only use for statusbar notify event
 * 
 * @author zhangwenyang
 */
public class StatusBarEventService extends IntentService {
    public static final String TAG = "StatusBarEventService";
    public static final String EXTRA_EVENT_TYPE = "extra_event_type";

    public static final int EVENT_NEW_THEME = 0;
    public static final int EVENT_BUSINESS_APP = 1;

    public StatusBarEventService() {
        super("");
    }

    public StatusBarEventService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LeoLog.d(TAG, "onHandleIntent");
        Intent targetIntent = null;
        int eventType = intent.getIntExtra(EXTRA_EVENT_TYPE, -1);
        if (eventType == EVENT_NEW_THEME) {
            LockManager.getInstatnce().timeFilter(this.getPackageName(), 1000);
            targetIntent = new Intent(this, LockerTheme.class);
            targetIntent.putExtra("from", "new_theme_tip");
            targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else if (eventType == EVENT_BUSINESS_APP) {

            LockManager.getInstatnce().timeFilter(this.getPackageName(), 1000);
            targetIntent = new Intent(this, HotAppActivity.class);
//            targetIntent.putExtra("from_statubar", true);
//            intent.setAction("move_to_new_app");
            targetIntent.putExtra("move_to_new_app", true);
            targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else {
            return;
        }

        startActivity(targetIntent);
    }

}
