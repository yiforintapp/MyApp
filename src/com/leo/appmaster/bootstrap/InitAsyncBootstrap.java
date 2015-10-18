
package com.leo.appmaster.bootstrap;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.PhoneInfo;
import com.leo.appmaster.backup.AppBackupRestoreManager;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.privacycontact.PrivacyContactManager;
import com.leo.appmaster.privacycontact.PrivacyTrickUtil;
import com.leo.appmaster.schedule.FetchScheduleJob;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.AppUtil;

/**
 * 异步初始化，旧版本的startInitTask
 * 
 * @author Jasper
 */
public class InitAsyncBootstrap extends Bootstrap {
    private static final String TAG = "InitAsyncBootstrap";

    InitAsyncBootstrap() {
        super();
    }

    @Override
    protected boolean doStrap() {
        PrivacyTrickUtil.clearOtherApps(mApp);
        AppLoadEngine.getInstance(mApp).preloadAllBaseInfo();
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
}
