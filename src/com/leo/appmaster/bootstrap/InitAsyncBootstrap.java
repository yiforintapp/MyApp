
package com.leo.appmaster.bootstrap;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.PhoneInfo;
import com.leo.appmaster.backup.AppBackupRestoreManager;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.privacycontact.PrivacyContactManager;
import com.leo.appmaster.privacycontact.PrivacyTrickUtil;
import com.leo.appmaster.quickgestures.FloatWindowHelper;
import com.leo.appmaster.quickgestures.ISwipUpdateRequestManager;
import com.leo.appmaster.quickgestures.QuickGestureManager;
import com.leo.appmaster.schedule.FetchScheduleJob;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.LeoLog;

/**
 * 异步初始化，旧版本的startInitTask
 * 
 * @author Jasper
 */
public class InitAsyncBootstrap extends Bootstrap {
    private static final String TAG = "InitBootstrap";

    InitAsyncBootstrap() {
        super();
    }

    @Override
    protected boolean doStrap() {
        checkClosePgQuickGesture();
        PrivacyTrickUtil.clearOtherApps(mApp);
        AppLoadEngine.getInstance(mApp).preloadAllBaseInfo();
        // 初始化快捷手势数据
        if (AppMasterPreference.getInstance(mApp).getSwitchOpenQuickGesture()) {
            QuickGestureManager.getInstance(mApp).init();
        }
        /* checkUpdateFinish(); */
        quickGestureTipInit();
        AppBackupRestoreManager.getInstance(mApp).getBackupList();
        PrivacyContactManager.getInstance(mApp).getPrivateContacts();
        // GP check
        boolean isAppInstalled;

        try {
            isAppInstalled = AppUtil.appInstalled(mApp, Constants.GP_PACKAGE);
        } catch (Exception e) {
            isAppInstalled = false;
        }

        if (!isAppInstalled) {
            SDKWrapper.addEvent(mApp, SDKWrapper.P1, "gp_check", "nogp");
        }

        FetchScheduleJob.startFetchJobs();
        return true;
    }

    @Override
    public String getClassTag() {
        return TAG;
    }

    private void quickGestureTipInit() {
        AppMasterPreference pref = AppMasterPreference.getInstance(mApp);
        if (!pref.getLastVersion().equals(PhoneInfo.getVersionCode(mApp))) {
            pref.setNewUserUnlockCount(0);
        }
    }

    /* 检查关闭Pg自身小白点 */
    private void checkClosePgQuickGesture() {
        AppMasterPreference am = AppMasterPreference.getInstance(mApp);
        /* 是否打开了PG内快捷手势 */
        boolean isOpenIswipe = AppMasterPreference.getInstance(mApp)
                .getSwitchOpenQuickGesture();
        /* 是否安装了iswipe */
        boolean isIntsallIswipe = ISwipUpdateRequestManager.getInstance(mApp).isInstallIsiwpe();
        /* 是否使用过pg内快捷手势 */
        boolean isUseIswipe = ISwipUpdateRequestManager.getInstance(mApp).isUseIswipUser();
        if (isUseIswipe && isOpenIswipe && isIntsallIswipe) {
            LeoLog.i(TAG, "关闭PG快捷手势");
            QuickGestureManager.getInstance(mApp)
                    .stopFloatWindow();
            FloatWindowHelper.removeAllFloatWindow(mApp);
            if (AppMasterPreference.getInstance(mApp).getSwitchOpenStrengthenMode()) {
                FloatWindowHelper.removeWhiteFloatView(mApp);
                AppMasterPreference.getInstance(mApp).setWhiteFloatViewCoordinate(0, 0);
            }
            AppMasterPreference.getInstance(mApp).setSwitchOpenQuickGesture(false);
        }
    }
}
