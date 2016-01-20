
package com.leo.appmaster.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

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
    public static final int mAppLockType = 1;
    public static final int mAppWeiZhuang = 2;
    public static final int mPicHide = 3;
    public static final int mVioHide = 4;
    public static final int mPrivateSms = 5;

    public static final int mFlow = 6;
    public static final int mElec = 7;
    public static final int mBackup = 8;
    public static final int mQuickGues = 9;
    public static final int mLockThem = 10;
    public static final int mHotApp = 11;
    public static final int mAd = 12;

    public static final int mWifi = 13;
    public static final int mQuickHelper = 14;

    public static final int mFilterNoti =15;
    public static final int mStrangerCallNoti =16;
    public static final int mMissCallNoti =17;

    public static final int mCallfilter = 18;

    private MobvistaAdWall wallAd;

    private boolean mDelayFinish = false;
    private Handler mHandler;
    private String mCbPath;

    private LockManager mLockManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LeoLog.i("proxy", "entered!");
        mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
        Intent intent = getIntent();
        int type = intent.getIntExtra(StatusBarEventService.EXTRA_EVENT_TYPE,
                StatusBarEventService.EVENT_EMPTY);
        String fromWhere = intent.getStringExtra(Constants.FROM_WHERE);
        mCbPath = intent.getStringExtra("cb_download_path");
        if (type == StatusBarEventService.EVENT_EMPTY) {
            mDelayFinish = true;
            mHandler = new Handler();
        } else {
            if (AppMasterPreference.getInstance(this).getLockType() == AppMasterPreference.LOCK_TYPE_NONE) {
                if (type == mFlow) {
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ", "dataUsage");
                    goToFlow(type);
                } else if (type == mStrangerCallNoti) {
                    goToStrangerCall(type);
                } else if (type == mCallfilter) {
                    goToBlackListTab1(type);
                } else if (type == mFilterNoti) {
                    goToBlackList(type);
                } else if (type == mElec) {
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ", "battery");
                    gotoEle(type);
                } else if (type == mBackup) {
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ", "backUp");
                    gotoBackUp(type);
                } else if (type == mWifi) {
                    if (fromWhere != null && fromWhere.equals(Constants.FROM_PUSH)) {
                        SDKWrapper.addEvent(this, SDKWrapper.P1, "push_refresh",
                                "push_wifi_cnts");
                        LeoLog.d("testFromWhere", "Wifi from push");
                    }
                    gotoWifi(type);
                } else if (type == mQuickGues) {
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ", "quickGesture");
                } else if (type == mLockThem) {
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ", "lockThem");
                    gotoLockThem(type);
                } else if (type == mHotApp) {
                    gotoHotApp(type);
                } else if (type == mAd) {
                    gotoAd(type);
                } else if (type == mQuickHelper) {
                    if (fromWhere != null && fromWhere.equals(Constants.FROM_PUSH)) {
                        SDKWrapper.addEvent(this, SDKWrapper.P1, "push_refresh",
                                "push_assistant_cnts");
                        LeoLog.d("testFromWhere", "mQuickHelper from push");
                    }
                    gotoQuickHelper(type);
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
                    case mAppLockType:
                        SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ", "appLock");
                        goToAppLock(type);
                        break;
                    case mAppWeiZhuang:
                        SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ", "appDisguise");
                        goToAppWeiZhuang(type);
                        break;
                    case mWifi:
                        if (fromWhere != null && fromWhere.equals(Constants.FROM_PUSH)) {
                            SDKWrapper.addEvent(this, SDKWrapper.P1, "push_refresh",
                                    "push_wifi_cnts");
                            LeoLog.d("testFromWhere", "Wifi from push");
                        }
                        gotoWifi(type);
                        break;
                    case mPicHide:
                        SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ",
                                "picHide");
                        goToHidePic(type);
                        break;
                    case mVioHide:
                        SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ",
                                "videoHide");
                        goToHideVio(type);
                        break;
                    case mCallfilter:
                        goToBlackListTab1(type);
                        break;
                    case mStrangerCallNoti:
//                        SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ",
//                                "videoHide");
                        goToStrangerCall(type);
                        break;
                    case mFilterNoti:
//                        SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ",
//                                "videoHide");
                        goToBlackList(type);
                        break;
                    case mPrivateSms:
                        SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ",
                                "privaceSms");
                        goToPrivateSms(type);
                        break;
                    case mFlow:
                        SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ",
                                "dataUsage");
                        goToFlow(type);
                        break;
                    case mElec:
                        SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ",
                                "battery");
                        gotoEle(type);
                        break;
                    case mBackup:
                        SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ",
                                "backUp");
                        gotoBackUp(type);
                        break;
                    case mQuickGues:
                        SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ",
                                "quickGesture");
                        break;
                    case mLockThem:
                        SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ",
                                "lockThem");
                        gotoLockThem(type);
                        break;
                    case mHotApp:
                        gotoHotApp(type);
                        break;
                    case mAd:
                        gotoAd(type);
                        break;
                    case mQuickHelper:
                        if (fromWhere != null && fromWhere.equals(Constants.FROM_PUSH)) {
                            SDKWrapper.addEvent(this, SDKWrapper.P1, "push_refresh",
                                    "push_assistant_cnts");
                            LeoLog.d("testFromWhere", "mQuickHelper from push");
                        }
                        gotoQuickHelper(type);
                        break;
                }
            }
            finish();
        }
    }

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
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        CallFilterHelper manager = CallFilterHelper.getInstance(this);
        manager.setLastClickedCallLogsId(manager.getLastShowedCallLogsBigestId());
        SDKWrapper.addEvent(this, SDKWrapper.P1, "block", "notify_stranger_cnts");
    }

    private void goToBlackList(int type) {
        mLockManager.filterPackage(this.getPackageName(), 1000);
        Intent intent = new Intent(this, CallFilterMainActivity.class);
        intent.putExtra("needMoveToTab2", true);
        intent.putExtra("needToHomeWhenFinish", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity(intent);
        SDKWrapper.addEvent(this, SDKWrapper.P1, "block", "notify_blacklist_cnts");
    }

    private void goToBlackListTab1(int type) {
        mLockManager.filterPackage(this.getPackageName(), 1000);
        Intent intent = new Intent(this, CallFilterMainActivity.class);
//        intent.putExtra("needMoveToTab2", false);
        intent.putExtra("needToHomeWhenFinish", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
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
