package com.leo.appmaster.applocker.service;

import android.app.IntentService;
import android.content.Intent;

import com.leo.appmaster.Constants;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.appmanage.BackUpActivity;
import com.leo.appmaster.appmanage.FlowActivity;
import com.leo.appmaster.appmanage.HotAppActivity;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.imagehide.NewHideImageActivity;
import com.leo.appmaster.lockertheme.LockerTheme;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.videohide.NewHideVidActivity;

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
    /* iswipe更新通知事件标志 */
    public static final int EVENT_ISWIPE_UPDATE_NOTIFICATION = 5;
    // 隐私状态监控
    public static final int EVENT_PRIVACY_STATUS = 6;
    public static final int EVENT_TEN_NEW_APP_BACKUP = 7;
    public static final int EVENT_TEN_OVER_DAY_TRAFFIC = 8;

    public static final int EVENT_PRIVACY_APP = 9;
    public static final int EVENT_PRIVACY_IMAGE = 10;
    public static final int EVENT_PRIVACY_VIDEO = 11;

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
        LockManager manager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
        Intent targetIntent = null;
        int eventType = intent.getIntExtra(EXTRA_EVENT_TYPE, -1);
//        LeoLog.d("testBackupNoti", "eventType is : " + eventType);
        if (eventType == EVENT_NEW_THEME) {
            manager.filterPackage(this.getPackageName(), 1000);
            targetIntent = new Intent(this, LockerTheme.class);
            targetIntent.putExtra("from", "new_theme_tip");
            targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else if (eventType == EVENT_BUSINESS_APP) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "hots", "statusbar_app");
            manager.filterPackage(this.getPackageName(), 1000);
            targetIntent = new Intent(this, HotAppActivity.class);
            targetIntent.putExtra(HotAppActivity.FROME_STATUSBAR, true);
            targetIntent.putExtra(HotAppActivity.SHOW_PAGE, HotAppActivity.PAGE_APP);
            targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        } else if (eventType == EVENT_BUSINESS_GAME) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "hots", "statusbar_game");
            manager.filterPackage(this.getPackageName(), 1000);
            targetIntent = new Intent(this, HotAppActivity.class);
            targetIntent.putExtra(HotAppActivity.FROME_STATUSBAR, true);
            targetIntent.putExtra(HotAppActivity.SHOW_PAGE, HotAppActivity.PAGE_GAME);
            targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else if (eventType == EVENT_BUSINESS_QUICK_GUESTURE) {
//            boolean installIswipe = ISwipUpdateRequestManager.isInstallIsiwpe(this);
//            if (installIswipe) {
//                if (DBG) {
//                    LeoLog.i(TAG, "存在Iswipe请点击Iswipe图标");
//                }
//                /* 进入iswipe主页 */
//                targetIntent = new Intent();
//                targetIntent.setAction(Intent.ACTION_MAIN);
//                ComponentName cn = new
//                        ComponentName(Constants.ISWIPE_PACKAGE,
//                        "com.leo.iswipe.activity.SplashActivity");
//                targetIntent.setComponent(cn);
//                targetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            }
        } else if (eventType == EVENT_QUICK_GESTURE_PERMISSION_NOTIFICATION) {

        } else if (eventType == EVENT_ISWIPE_UPDATE_NOTIFICATION) {
//            LeoLog.i(TAG, "来自iswipe升级通知，启动主页！");
//            targetIntent = new Intent(this, HomeActivity.class);
//            targetIntent.putExtra(ISwipUpdateRequestManager.ISWIP_NOTIFICATION_TO_PG_HOME,
//                    ISwipUpdateRequestManager.ISWIP_NOTIFICATION_TO_PG_HOME);
//            targetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
//                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else if (eventType == EVENT_PRIVACY_STATUS) {
            targetIntent = new Intent(this, HomeActivity.class);
            targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            targetIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            targetIntent.putExtra(Constants.PRIVACY_ENTER_SCAN, true);
            int type = intent.getIntExtra(Constants.PRIVACY_ENTER_SCAN_TYPE, PrivacyHelper.PRIVACY_NONE);
            targetIntent.putExtra(Constants.PRIVACY_ENTER_SCAN_TYPE, type);
        } else if (eventType == EVENT_TEN_NEW_APP_BACKUP) {
            manager.filterPackage(this.getPackageName(), 1000);
            targetIntent = new Intent(this, BackUpActivity.class);
            targetIntent.putExtra("from", "notification");
            targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else if (eventType == EVENT_TEN_OVER_DAY_TRAFFIC) {
            manager.filterPackage(this.getPackageName(), 1000);
            targetIntent = new Intent(this, FlowActivity.class);
            targetIntent.putExtra("from", "notification");
            targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else if (eventType == EVENT_PRIVACY_APP) {
            targetIntent = new Intent(this, AppLockListActivity.class);
            targetIntent.putExtra(Constants.FROM_APP_SCAN_RESULT, true);
            targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            SDKWrapper.addEvent(this, SDKWrapper.P1, "prilevel", "prilevel_cnts_app");
        } else if (eventType == EVENT_PRIVACY_IMAGE) {
            targetIntent = new Intent(this, NewHideImageActivity.class);
            targetIntent.putExtra("pic_from_notify", true);
            targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            SDKWrapper.addEvent(this, SDKWrapper.P1, "prilevel", "prilevel_cnts_pic");
        } else if (eventType == EVENT_PRIVACY_VIDEO) {
            targetIntent = new Intent(this, NewHideVidActivity.class);
            targetIntent.putExtra("vid_from_notify", true);
            targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            SDKWrapper.addEvent(this, SDKWrapper.P1, "prilevel", "prilevel_cnts_vid");
        } else {
            return;
        }
        try {
            startActivity(targetIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
