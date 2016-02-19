
package com.leo.appmaster.home;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.activity.QuickHelperActivity;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.LockSettingActivity;
import com.leo.appmaster.applocker.RecommentAppLockListActivity;
import com.leo.appmaster.applocker.WeiZhuangActivity;
import com.leo.appmaster.applocker.manager.MobvistaEngine;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.appmanage.BackUpActivity;
import com.leo.appmaster.appmanage.FlowActivity;
import com.leo.appmaster.appmanage.HotAppActivity;
import com.leo.appmaster.battery.BatteryMainActivity;
import com.leo.appmaster.callfilter.CallFilterMainActivity;
import com.leo.appmaster.callfilter.CallFilterHelper;
import com.leo.appmaster.callfilter.StrangeCallActivity;
import com.leo.appmaster.imagehide.ImageHideMainActivity;
import com.leo.appmaster.lockertheme.LockerTheme;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.privacycontact.PrivacyContactActivity;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.videohide.VideoHideMainActivity;
import com.leo.appmaster.wifiSecurity.WifiSecurityActivity;
import com.mobvista.sdk.m.core.MobvistaAdWall;

public class DeskProxyActivity extends Activity {
    private static final String TAG = "DeskProxyActivity";
    public static final int IDX_APP_LOCK = 1;
    public static final int IDX_APP_COVER = 2;
    public static final int IDX_PIC_HIDE = 3;
    public static final int IDX_VID_HIDE = 4;
    public static final int IDX_PRIVACY_SMS = 5;
    public static final int IDX_FLOW = 6;
    public static final int IDX_ELEC = 7;
    public static final int IDX_BACKUP = 8;
    public static final int mQuickGues = 9;
    public static final int IDX_LOCK_THEME = 10;
    public static final int IDX_HOT_APP = 11;
    public static final int IDX_AD = 12;
    public static final int IDX_WIFI = 13;
    public static final int IDX_QUICK_HELPER = 14;
    public static final int IDX_FILTER_NOTI = 15;
    public static final int IDX_STRANGER_CALL_NOTI = 16;
    public static final int mMissCallNoti = 17;
    public static final int IDX_CALL_FILTER = 18;
    public static final int IDX_BATTERY_PROTECT = 19;

    private static final int IDX_HOME = 9999;

    private MobvistaAdWall wallAd;

    private boolean mDelayFinish = false;
    private Handler mHandler;
    private String mCbPath;

    private boolean mHasRegistered = false;

    public static final String CALL_FILTER_PUSH = "from"; //是否从骚扰拦截push通知进入key

    private LockManager mLockManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LeoLog.i("proxy", "entered!");
        mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
        Intent intent = getIntent();
        Uri uri = intent.getData();

        String fromWhere = null;
        int type = 0;
        if (uri != null) {
            String schema = uri.getScheme();
            String host = uri.getHost();
            String path = uri.getPath();
            LeoLog.d(TAG, "onCreate, uri: " + uri);
            if (!Constants.DP_SCHEMA.equals(schema) || !Constants.DP_HOST.equals(host) || TextUtils.isEmpty(path)) {
                finish();
                return;
            }

            try {
                path = path.substring(1);
                type = Integer.parseInt(path);
            } catch (Exception e) {
                e.printStackTrace();
                finish();
                return;
            }
        } else {
            type = intent.getIntExtra(StatusBarEventService.EXTRA_EVENT_TYPE,
                    StatusBarEventService.EVENT_EMPTY);
            fromWhere = intent.getStringExtra(Constants.FROM_WHERE);
            mCbPath = intent.getStringExtra("cb_download_path");
        }

        handleAction(type, fromWhere);
    }

    private void handleAction(int type, String fromWhere) {
        if (type == StatusBarEventService.EVENT_EMPTY) {
            mDelayFinish = true;
            mHandler = new Handler();
            String from = getIntent().getStringExtra(CALL_FILTER_PUSH);
            if (from != null && from != "") {
                gotoCallFilerActivity();
            }
        } else {
            if (AppMasterPreference.getInstance(this).getLockType() == AppMasterPreference.LOCK_TYPE_NONE) {
                if (type == IDX_FLOW) {
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ", "dataUsage");
                    goToFlow(type);
                } else if (type == IDX_STRANGER_CALL_NOTI) {
                    goToStrangerCall(type);
                } else if (type == IDX_CALL_FILTER) {
                    goToBlackListTab1(type);
                } else if (type == IDX_FILTER_NOTI) {
                    goToBlackList(type);
                } else if (type == IDX_ELEC) {
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ", "battery");
                    gotoEle(type);
                } else if (type == IDX_BACKUP) {
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ", "backUp");
                    gotoBackUp(type);
                } else if (type == IDX_WIFI) {
                    if (fromWhere != null && fromWhere.equals(Constants.FROM_PUSH)) {
                        SDKWrapper.addEvent(this, SDKWrapper.P1, "push_refresh",
                                "push_wifi_cnts");
                        LeoLog.d("testFromWhere", "Wifi from push");
                    }
                    gotoWifi(type);
                } else if (type == mQuickGues) {
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ", "quickGesture");
                } else if (type == IDX_LOCK_THEME) {
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ", "lockThem");
                    gotoLockThem(type);
                } else if (type == IDX_HOT_APP) {
                    gotoHotApp(type);
                } else if (type == IDX_AD) {
                    gotoAd(type);
                } else if (type == IDX_QUICK_HELPER) {
                    if (fromWhere != null && fromWhere.equals(Constants.FROM_PUSH)) {
                        SDKWrapper.addEvent(this, SDKWrapper.P1, "push_refresh",
                                "push_assistant_cnts");
                        LeoLog.d("testFromWhere", "mQuickHelper from push");
                    }
                    gotoQuickHelper(type);
                } else if (type == IDX_BATTERY_PROTECT) {
                    gotoBatteryClick();
                } else {
                    Intent mIntent = new Intent(this, LockSettingActivity.class);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_NEW_TASK);
                    mIntent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, type);
                    mIntent.putExtra("cb_download_path", mCbPath);
                    startActivity(mIntent);
                }
            } else {
                switch (type) {
                    case IDX_APP_LOCK:
                        SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ", "appLock");
                        goToAppLock(type);
                        break;
                    case IDX_APP_COVER:
                        SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ", "appDisguise");
                        goToAppWeiZhuang(type);
                        break;
                    case IDX_WIFI:
                        if (fromWhere != null && fromWhere.equals(Constants.FROM_PUSH)) {
                            SDKWrapper.addEvent(this, SDKWrapper.P1, "push_refresh",
                                    "push_wifi_cnts");
                            LeoLog.d("testFromWhere", "Wifi from push");
                        }
                        gotoWifi(type);
                        break;
                    case IDX_PIC_HIDE:
                        SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ",
                                "picHide");
                        goToHidePic(type);
                        break;
                    case IDX_VID_HIDE:
                        SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ",
                                "videoHide");
                        goToHideVio(type);
                        break;
                    case IDX_CALL_FILTER:
                        goToBlackListTab1(type);
                        break;
                    case IDX_STRANGER_CALL_NOTI:
                        goToStrangerCall(type);
                        break;
                    case IDX_FILTER_NOTI:
                        goToBlackList(type);
                        break;
                    case IDX_PRIVACY_SMS:
                        SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ",
                                "privaceSms");
                        goToPrivateSms(type);
                        break;
                    case IDX_FLOW:
                        SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ",
                                "dataUsage");
                        goToFlow(type);
                        break;
                    case IDX_ELEC:
                        SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ",
                                "battery");
                        gotoEle(type);
                        break;
                    case IDX_BACKUP:
                        SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ",
                                "backUp");
                        gotoBackUp(type);
                        break;
                    case mQuickGues:
                        SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ",
                                "quickGesture");
                        break;
                    case IDX_LOCK_THEME:
                        SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ",
                                "lockThem");
                        gotoLockThem(type);
                        break;
                    case IDX_HOT_APP:
                        gotoHotApp(type);
                        break;
                    case IDX_AD:
                        gotoAd(type);
                        break;
                    case IDX_QUICK_HELPER:
                        if (fromWhere != null && fromWhere.equals(Constants.FROM_PUSH)) {
                            SDKWrapper.addEvent(this, SDKWrapper.P1, "push_refresh",
                                    "push_assistant_cnts");
                            LeoLog.d("testFromWhere", "mQuickHelper from push");
                        }
                        gotoQuickHelper(type);
                        break;
                    case IDX_BATTERY_PROTECT:
                        gotoBatteryClick();
                        break;
                }
            }
            if (type != IDX_BATTERY_PROTECT) {
                finish();
            }
        }
    }

    private void gotoBatteryClick() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        this.registerReceiver(mPresentReceiver, intentFilter);
        mHasRegistered = true;
    }

    private BroadcastReceiver mPresentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LeoLog.d("proxy", "action=" + intent.getAction());
            finish();
        }
    };

    private void gotoQuickHelper(int type) {
        mLockManager.filterPackage(this.getPackageName(), 1000);
        Intent qhintent = new Intent(this, QuickHelperActivity.class);
        qhintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(qhintent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wallAd != null) {
            wallAd.release();
            wallAd = null;
        }
        if (mHasRegistered) {
            try {
                unregisterReceiver(mPresentReceiver);
            } catch(Exception e) {
            }
            mHasRegistered = false;
        }
    }

    private void gotoAd(int type) {
        if (getIntent().getBooleanExtra("from_quickhelper", false)) {
            SDKWrapper.addEvent(this, SDKWrapper.P1,
                    "assistant", "appjoy_cnts");
        }
        mLockManager.filterPackage(this.getPackageName(), 1000);
        // wallAd = MobvistaEngine.getInstance().createAdWallController(this);
        wallAd = MobvistaEngine.getInstance(this).createAdWallController(this, Constants.UNIT_ID_61);

        if (wallAd != null) {
            wallAd.preloadWall();
            wallAd.clickWall();
        }
    }

    private void gotoHotApp(int type) {
        mLockManager.filterPackage(this.getPackageName(), 1000);
        Intent intent = new Intent(this, HotAppActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void gotoLockThem(int type) {
        mLockManager.filterPackage(this.getPackageName(), 1000);
        Intent intent = new Intent(this, LockerTheme.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void goToStrangerCall(int type) {
        mLockManager.filterPackage(this.getPackageName(), 1000);
        Intent intent = new Intent(this, StrangeCallActivity.class);
        intent.putExtra("fromNotif", true);  //判断从通知进入页面
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        CallFilterHelper manager = CallFilterHelper.getInstance(this);
        manager.setLastClickedCallLogsId(manager.getLastShowedCallLogsBigestId());
        SDKWrapper.addEvent(this, SDKWrapper.P1, "block", "notify_stranger_cnts");
    }

    /**
     * 从骚扰拦截push通知进入骚扰拦截界面
     */
    private void gotoCallFilerActivity() {
        mLockManager.filterPackage(this.getPackageName(), 1000);
        Intent intent = new Intent(this, CallFilterMainActivity.class);
        intent.putExtra(CALL_FILTER_PUSH, "push");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void goToBlackList(int type) {
        mLockManager.filterPackage(this.getPackageName(), 1000);
        Intent intent = new Intent(this, CallFilterMainActivity.class);
        intent.putExtra("needMoveToTab2", true);
        intent.putExtra("needToHomeWhenFinish", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        SDKWrapper.addEvent(this, SDKWrapper.P1, "block", "notify_blacklist_cnts");
    }

    private void goToBlackListTab1(int type) {
        mLockManager.filterPackage(this.getPackageName(), 1000);
        Intent intent = new Intent(this, CallFilterMainActivity.class);
//        intent.putExtra("needMoveToTab2", true);
        intent.putExtra("needToHomeWhenFinish", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
//        Intent intent = new Intent(this, CallFilterMainActivity.class);
////        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
    }

    private void gotoBackUp(int type) {
        if (getIntent().getBooleanExtra("from_quickhelper", false)) {
            SDKWrapper.addEvent(this, SDKWrapper.P1,
                    "assistant", "backup_cnts");
        }
        mLockManager.filterPackage(this.getPackageName(), 1000);
        Intent intent = new Intent(this, BackUpActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void gotoWifi(int type) {
        if (getIntent().getBooleanExtra("from_quickhelper", false)) {
            SDKWrapper.addEvent(this, SDKWrapper.P1,
                    "assistant", "wifi_cnts");
        }
        mLockManager.filterPackage(this.getPackageName(), 1000);
        Intent intent = new Intent(this, WifiSecurityActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, type);
        startActivity(intent);
    }

    private void gotoEle(int type) {
        if (getIntent().getBooleanExtra("from_quickhelper", false)) {
            SDKWrapper.addEvent(this, SDKWrapper.P1,
                    "assistant", "power_cnts");
        } else {
            SDKWrapper.addEvent(this, SDKWrapper.P1,
                    "batterypage", "comsuption_ntf_cnts");
        }
        mLockManager.filterPackage(this.getPackageName(), 1000);
        Intent dlIntent = new Intent(this, BatteryMainActivity.class);
        dlIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        dlIntent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, type);
        startActivity(dlIntent);
    }

    private void goToFlow(int type) {
        if (getIntent().getBooleanExtra("from_quickhelper", false)) {
            SDKWrapper.addEvent(this, SDKWrapper.P1,
                    "assistant", "dataflow_cnts");
        }
        mLockManager.filterPackage(this.getPackageName(), 1000);
        Intent mIntent = new Intent(this, FlowActivity.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mIntent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, type);
        startActivity(mIntent);
    }

    private void goToPrivateSms(int type) {
        Intent intent = new Intent(this,
                PrivacyContactActivity.class);
        intent.putExtra(PrivacyContactUtils.TO_PRIVACY_CONTACT,
                PrivacyContactUtils.TO_PRIVACY_MESSAGE_FLAG);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        LeoLog.d("Track Lock Screen", "apply lockscreen form goToPrivateSms");
        mLockManager.applyLock(LockManager.LOCK_MODE_FULL, this.getPackageName(), false, null);
    }

    private void goToHideVio(int type) {
        Intent intent = new Intent(this, VideoHideMainActivity.class);
        if (mCbPath != null) {
            intent.putExtra("cb_download_path", mCbPath);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        LeoLog.d("Track Lock Screen", "apply lockscreen form goToHideVio");
        mLockManager.applyLock(LockManager.LOCK_MODE_FULL, this.getPackageName(), false, null);
    }

    private void goToHidePic(int type) {
        Intent intent = new Intent(this, ImageHideMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        LeoLog.d("Track Lock Screen", "apply lockscreen form goToHidePic");
        mLockManager.applyLock(LockManager.LOCK_MODE_FULL, this.getPackageName(), false, null);
    }

    private void goToAppWeiZhuang(int type) {
        Intent intent = new Intent(this, WeiZhuangActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        LeoLog.d("Track Lock Screen", "apply lockscreen form goToAppWeiZhuang");
        mLockManager.applyLock(LockManager.LOCK_MODE_FULL, this.getPackageName(), false, null);
    }

    private void goToAppLock(int type) {
        LockMode curMode = mLockManager.getCurLockMode();
        Intent intent;
        if (curMode != null && curMode.defaultFlag == 1 && !curMode.haveEverOpened) {
            intent = new Intent(this, RecommentAppLockListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("target", 0);
            intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, type);
            startActivity(intent);
            curMode.haveEverOpened = true;
            mLockManager.updateMode(curMode);
        } else {
            intent = new Intent(this, AppLockListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, type);
            startActivity(intent);
        }
        LeoLog.d("Track Lock Screen", "apply lockscreen form goToAppLock");
        mLockManager.applyLock(LockManager.LOCK_MODE_FULL, this.getPackageName(), false, null);
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
