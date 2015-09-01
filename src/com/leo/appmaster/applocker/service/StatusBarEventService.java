
package com.leo.appmaster.applocker.service;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;

import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.appmanage.HotAppActivity;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.lockertheme.LockerTheme;
import com.leo.appmaster.quickgestures.ISwipUpdateRequestManager;
import com.leo.appmaster.quickgestures.ui.QuickGestureActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.LeoLog;

/**
 * this service only use for statusbar notify event
 * 
 * @author zhangwenyang
 */
public class StatusBarEventService extends IntentService {
    public static final String TAG = "StatusBarEventService";
    public static final String EXTRA_EVENT_TYPE = "extra_event_type";
    public static final boolean DBG = false;
    public static final int EVENT_EMPTY = -1;
    public static final int EVENT_NEW_THEME = 0;
    public static final int EVENT_BUSINESS_APP = 1;
    public static final int EVENT_BUSINESS_GAME = 2;
    public static final int EVENT_BUSINESS_QUICK_GUESTURE = 3;
    public static final int EVENT_QUICK_GESTURE_PERMISSION_NOTIFICATION = 4;

    public StatusBarEventService() {
        super("");
    }

    public StatusBarEventService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LeoLog.d(TAG, "onHandleIntent");
        if (intent == null) {
            return;
        }
        Intent targetIntent = null;
        int eventType = intent.getIntExtra(EXTRA_EVENT_TYPE, -1);
        if (eventType == EVENT_NEW_THEME) {
            LockManager.getInstatnce().timeFilter(this.getPackageName(), 1000);
            targetIntent = new Intent(this, LockerTheme.class);
            targetIntent.putExtra("from", "new_theme_tip");
            targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else if (eventType == EVENT_BUSINESS_APP) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "hots", "statusbar_app");
            LockManager.getInstatnce().timeFilter(this.getPackageName(), 1000);
            targetIntent = new Intent(this, HotAppActivity.class);
            targetIntent.putExtra(HotAppActivity.FROME_STATUSBAR, true);
            targetIntent.putExtra(HotAppActivity.SHOW_PAGE, HotAppActivity.PAGE_APP);
            targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        } else if (eventType == EVENT_BUSINESS_GAME) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "hots", "statusbar_game");
            LockManager.getInstatnce().timeFilter(this.getPackageName(), 1000);
            targetIntent = new Intent(this, HotAppActivity.class);
            targetIntent.putExtra(HotAppActivity.FROME_STATUSBAR, true);
            targetIntent.putExtra(HotAppActivity.SHOW_PAGE, HotAppActivity.PAGE_GAME);
            targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else if (eventType == EVENT_BUSINESS_QUICK_GUESTURE) {
            boolean installIswipe = ISwipUpdateRequestManager.getInstance(this).isInstallIsiwpe();
            if (!installIswipe) {
                LockManager.getInstatnce().timeFilter(this.getPackageName(), 1000);
                targetIntent = new Intent(this, QuickGestureActivity.class);
                targetIntent.putExtra(QuickGestureActivity.FROME_STATUSBAR, true);
                targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            } else {
                if (DBG) {
                    LeoLog.i(TAG, "存在Iswipe请点击Iswipe图标");
                }
                /* 进入iswipe主页 */
                targetIntent = new Intent();
                targetIntent.setAction(Intent.ACTION_MAIN);
                ComponentName cn = new
                        ComponentName(AppLoadEngine.ISWIPE_PACKAGENAME,
                                "com.leo.iswipe.activity.SplashActivity");
                targetIntent.setComponent(cn);
                targetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
        } else if (eventType == EVENT_QUICK_GESTURE_PERMISSION_NOTIFICATION) {
            LockManager.getInstatnce().timeFilter(this.getPackageName(), 1000);
            targetIntent = new Intent(this, QuickGestureActivity.class);
            targetIntent.putExtra(QuickGestureActivity.FROME_STATUSBAR, true);
            targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else {
            return;
        }

        try {
            startActivity(targetIntent);
        } catch (Exception e) {
        }
    }
}
