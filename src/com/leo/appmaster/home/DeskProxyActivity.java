
package com.leo.appmaster.home;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.LockSettingActivity;
import com.leo.appmaster.applocker.RecommentAppLockListActivity;
import com.leo.appmaster.applocker.WeiZhuangActivity;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.appmanage.BackUpActivity;
import com.leo.appmaster.appmanage.EleActivity;
import com.leo.appmaster.appmanage.FlowActivity;
import com.leo.appmaster.imagehide.ImageHideMainActivity;
import com.leo.appmaster.lockertheme.LockerTheme;
import com.leo.appmaster.privacycontact.PrivacyContactActivity;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.quickgestures.ui.QuickGestureActivity;
import com.leo.appmaster.quickgestures.ui.QuickGestureMiuiTip;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.videohide.VideoHideMainActivity;

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
    private boolean mDelayFinish = false;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        int type = intent.getIntExtra(StatusBarEventService.EXTRA_EVENT_TYPE,
                StatusBarEventService.EVENT_EMPTY);
        if (type == StatusBarEventService.EVENT_EMPTY) {
            mDelayFinish = true;
            mHandler = new Handler();
        } else {
            if (AppMasterPreference.getInstance(this).getLockType() == AppMasterPreference.LOCK_TYPE_NONE) {
                Intent mIntent = new Intent(this, LockSettingActivity.class);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                mIntent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, type);
                startActivity(mIntent);
            } else {
                if (type == mAppLockType) {
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ", "appLock");
                    goToAppLock(type);
                } else if (type == mAppWeiZhuang) {
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ", "appDisguise");
                    goToAppWeiZhuang(type);
                } else if (type == mPicHide) {
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ", "picHide");
                    goToHidePic(type);
                } else if (type == mVioHide) {
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ", "videoHide");
                    goToHideVio(type);
                } else if (type == mPrivateSms) {
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ", "privaceSms");
                    goToPrivateSms(type);
                } else if (type == mFlow) {
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ", "dataUsage");
                    LockManager.getInstatnce().timeFilter(this.getPackageName(), 1000);
                    goToFlow(type);
                } else if (type == mElec) {
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ", "battery");
                    LockManager.getInstatnce().timeFilter(this.getPackageName(), 1000);
                    gotoEle(type);
                } else if (type == mBackup) {
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ", "backUp");
                    LockManager.getInstatnce().timeFilter(this.getPackageName(), 1000);
                    gotoBackUp(type);
                } else if (type == mQuickGues) {
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ", "quickGesture");
                    LockManager.getInstatnce().timeFilter(this.getPackageName(), 1000);
                    gotoQuickGues(type);
                } else if (type == mLockThem) {
                    SDKWrapper.addEvent(this, SDKWrapper.P1, "launcher_in ", "lockThem");
                    LockManager.getInstatnce().timeFilter(this.getPackageName(), 1000);
                    gotoLockThem(type);
                }

            }
            finish();
        }
    }

    private void gotoLockThem(int type) {
        Intent intent = new Intent(this, LockerTheme.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void gotoQuickGues(int type) {
        boolean checkHuaWei = BuildProperties.isHuaWeiTipPhone(this);
        boolean checkFloatWindow = BuildProperties.isFloatWindowOpAllowed(this);
        boolean checkMiui = BuildProperties.isMIUI();
        boolean isOppoOs = BuildProperties.isOppoOs();
        boolean isOpenWindow =
                BuildProperties.isFloatWindowOpAllowed(this);

        if (checkMiui && !isOpenWindow) {
            // MIUI
            Intent intentv6 = new
                    Intent("miui.intent.action.APP_PERM_EDITOR");
            intentv6.setClassName("com.miui.securitycenter",
                    "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
            intentv6.putExtra("extra_pkgname", this.getPackageName());
            intentv6.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
                intentv5.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
            quickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
            quickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            quickIntent.putExtra("sys_name", "huawei");
            try {
                startActivity(quickIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Intent quickIntent = new Intent(this, QuickGestureActivity.class);
            quickIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(quickIntent);
        }
    }

    private void gotoBackUp(int type) {
        Intent intent = new Intent(this, BackUpActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void gotoEle(int type) {
        Intent dlIntent = new Intent(this, EleActivity.class);
        dlIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dlIntent);
    }

    private void goToFlow(int type) {
        Intent mIntent = new Intent(this, FlowActivity.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mIntent);
    }

    private void goToPrivateSms(int type) {
        Intent intent = new Intent(this,
                PrivacyContactActivity.class);
        intent.putExtra(PrivacyContactUtils.TO_PRIVACY_CONTACT,
                PrivacyContactUtils.TO_PRIVACY_MESSAGE_FLAG);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void goToHideVio(int type) {
        Intent intent = new Intent(this, VideoHideMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void goToHidePic(int type) {
        Intent intent = new Intent(this, ImageHideMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void goToAppWeiZhuang(int type) {
        Intent intent = new Intent(this, WeiZhuangActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void goToAppLock(int type) {
        LockManager lm = LockManager.getInstatnce();
        LockMode curMode = lm.getCurLockMode();
        Intent intent;
        if (curMode != null && curMode.defaultFlag == 1 && !curMode.haveEverOpened) {
            intent = new Intent(this, RecommentAppLockListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("target", 0);
            intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, type);
            startActivity(intent);
            curMode.haveEverOpened = true;
            lm.updateMode(curMode);
        } else {
            intent = new Intent(this, AppLockListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, type);
            startActivity(intent);
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
