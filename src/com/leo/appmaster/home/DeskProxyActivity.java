
package com.leo.appmaster.home;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.LockSettingActivity;
import com.leo.appmaster.applocker.RecommentAppLockListActivity;
import com.leo.appmaster.applocker.WeiZhuangActivity;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.manager.MobvistaEngine;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.appmanage.BackUpActivity;
import com.leo.appmaster.appmanage.EleActivity;
import com.leo.appmaster.appmanage.FlowActivity;
import com.leo.appmaster.appmanage.HotAppActivity;
import com.leo.appmaster.imagehide.ImageHideMainActivity;
import com.leo.appmaster.lockertheme.LockerTheme;
import com.leo.appmaster.privacycontact.PrivacyContactActivity;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.quickgestures.ui.QuickGestureActivity;
import com.leo.appmaster.quickgestures.ui.QuickGestureMiuiTip;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.videohide.VideoHideMainActivity;
import com.mobvista.sdk.m.core.MobvistaAd;
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

    private MobvistaAdWall wallAd;

    private boolean mDelayFinish = false;
    private Handler mHandler;
    private String mCbPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        int type = intent.getIntExtra(StatusBarEventService.EXTRA_EVENT_TYPE,
                StatusBarEventService.EVENT_EMPTY);
        mCbPath = intent.getStringExtra("cb_download_path");
        if (type == StatusBarEventService.EVENT_EMPTY) {
            mDelayFinish = true;
            mHandler = new Handler();
        } else {
            if (AppMasterPreference.getInstance(this).getLockType() == AppMasterPreference.LOCK_TYPE_NONE) {
                if (type == mFlow) {
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ", "dataUsage");
                    goToFlow(type);
                } else if (type == mElec) {
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ", "battery");
                    gotoEle(type);
                } else if (type == mBackup) {
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ", "backUp");
                    gotoBackUp(type);
                } else if (type == mQuickGues) {
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ", "quickGesture");
                    gotoQuickGues(type);
                } else if (type == mLockThem) {
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ", "lockThem");
                    gotoLockThem(type);
                } else if (type == mHotApp) {
                    gotoHotApp(type);
                } else if (type == mAd) {
                    gotoAd(type);
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
                        gotoQuickGues(type);
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
                }
            }
            finish();
        }
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
        LockManager.getInstatnce().timeFilter(this.getPackageName(), 1000);
        wallAd = MobvistaEngine.getInstance().createAdWallController(this);
        if (wallAd != null) {
            wallAd.preloadWall();
            wallAd.clickWall();
        }
    }

    private void gotoHotApp(int type) {
        LockManager.getInstatnce().timeFilter(this.getPackageName(), 1000);
        Intent intent = new Intent(this, HotAppActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void gotoLockThem(int type) {
        LockManager.getInstatnce().timeFilter(this.getPackageName(), 1000);
        Intent intent = new Intent(this, LockerTheme.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void gotoQuickGues(int type) {
        LockManager.getInstatnce().timeFilter(this.getPackageName(), 1000);
        boolean checkHuaWei = BuildProperties.isHuaWeiTipPhone(this);
        boolean checkFloatWindow = BuildProperties.isFloatWindowOpAllowed(this);
        boolean checkMiui = BuildProperties.isMIUI();
        boolean isOppoOs = BuildProperties.isYiJia();
        boolean isOpenWindow =
                BuildProperties.isFloatWindowOpAllowed(this);

        if (checkMiui && !isOpenWindow) {
            // MIUI
            Intent intentv6 = new
                    Intent("miui.intent.action.APP_PERM_EDITOR");
            intentv6.setClassName("com.miui.securitycenter",
                    "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
            intentv6.putExtra("extra_pkgname", this.getPackageName());
            intentv6.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            try {
                LockManager.getInstatnce().addFilterLockPackage("com.miui.securitycenter",
                        false);
                LockManager.getInstatnce().filterAllOneTime(2000);
                startActivity(intentv6);
            } catch (Exception e) {
                LockManager.getInstatnce().addFilterLockPackage("com.android.settings",
                        false);
                LockManager.getInstatnce().filterAllOneTime(1000);
                Intent intentv5 = new Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri
                        .fromParts("package", this.getPackageName(), null);
                intentv5.setData(uri);
                intentv5.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                try {
                    startActivity(intentv5);
                } catch (Exception e1) {
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "qs_open_error", "reason_"
                            + BuildProperties.getPoneModel());
                }
            }
            LockManager.getInstatnce().addFilterLockPackage("com.leo.appmaster", false);
            LockManager.getInstatnce().filterAllOneTime(1000);
            Intent quickIntent = new Intent(this, QuickGestureMiuiTip.class);
            quickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(quickIntent);
        } else if (checkHuaWei && !checkFloatWindow) {
            BuildProperties.isToHuaWeiSystemManager(this);
            LockManager.getInstatnce().addFilterLockPackage("com.leo.appmaster", false);
            Intent quickIntent = new Intent(this, QuickGestureMiuiTip.class);
            quickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            quickIntent.putExtra("sys_name", "huawei");
            try {
                startActivity(quickIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (isOppoOs && !isOpenWindow) {
            boolean backFlag = BuildProperties.startOppoManageIntent(this);
            LockManager.getInstatnce().addFilterLockPackage("com.leo.appmaster", false);
            Intent quickIntent = new Intent(this, QuickGestureMiuiTip.class);
            quickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            quickIntent.putExtra("sys_name", "huawei");
            try {
                startActivity(quickIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Intent quickIntent = new Intent(this, QuickGestureActivity.class);
            quickIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            this.startActivity(quickIntent);
        }
    }

    private void gotoBackUp(int type) {
        LockManager.getInstatnce().timeFilter(this.getPackageName(), 1000);
        Intent intent = new Intent(this, BackUpActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void gotoEle(int type) {
        LockManager.getInstatnce().timeFilter(this.getPackageName(), 1000);
        Intent dlIntent = new Intent(this, EleActivity.class);
        dlIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        dlIntent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, type);
        startActivity(dlIntent);
    }

    private void goToFlow(int type) {
        LockManager.getInstatnce().timeFilter(this.getPackageName(), 1000);
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
        LockManager.getInstatnce().applyLock(LockManager.LOCK_MODE_FULL,
                this.getPackageName(), false, null);
    }

    private void goToHideVio(int type) {
        Intent intent = new Intent(this, VideoHideMainActivity.class);
        if (mCbPath != null) {
            intent.putExtra("cb_download_path", mCbPath);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        LeoLog.d("Track Lock Screen", "apply lockscreen form goToHideVio");
        LockManager.getInstatnce().applyLock(LockManager.LOCK_MODE_FULL,
                this.getPackageName(), false, null);
    }

    private void goToHidePic(int type) {
        Intent intent = new Intent(this, ImageHideMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        LeoLog.d("Track Lock Screen", "apply lockscreen form goToHidePic");
        LockManager.getInstatnce().applyLock(LockManager.LOCK_MODE_FULL,
                this.getPackageName(), false, null);
    }

    private void goToAppWeiZhuang(int type) {
        Intent intent = new Intent(this, WeiZhuangActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        LeoLog.d("Track Lock Screen", "apply lockscreen form goToAppWeiZhuang");
        LockManager.getInstatnce().applyLock(LockManager.LOCK_MODE_FULL,
                this.getPackageName(), false, null);
    }

    private void goToAppLock(int type) {
        LockManager lm = LockManager.getInstatnce();
        LockMode curMode = lm.getCurLockMode();
        Intent intent;
        if (curMode != null && curMode.defaultFlag == 1 && !curMode.haveEverOpened) {
            intent = new Intent(this, RecommentAppLockListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("target", 0);
            intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, type);
            startActivity(intent);
            curMode.haveEverOpened = true;
            lm.updateMode(curMode);
        } else {
            intent = new Intent(this, AppLockListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, type);
            startActivity(intent);
        }
        LeoLog.d("Track Lock Screen", "apply lockscreen form goToAppLock");
        LockManager.getInstatnce().applyLock(LockManager.LOCK_MODE_FULL,
                this.getPackageName(), false, null);
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
