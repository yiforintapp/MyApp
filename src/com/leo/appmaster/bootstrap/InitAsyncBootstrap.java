
package com.leo.appmaster.bootstrap;

import android.os.SystemClock;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.PhoneInfo;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.manager.MobvistaEngine;
import com.leo.appmaster.backup.AppBackupRestoreManager;
import com.leo.appmaster.db.BlacklistTab;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.ThirdAppManager;
import com.leo.appmaster.mgr.impl.LostSecurityManagerImpl;
import com.leo.appmaster.privacycontact.PrivacyContactManager;
import com.leo.appmaster.privacycontact.PrivacyTrickUtil;
import com.leo.appmaster.quickgestures.ISwipUpdateRequestManager;
import com.leo.appmaster.schedule.FetchScheduleJob;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.CollectVideoUtils;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.Utilities;

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
        // 加载所有的首选项
        PreferenceTable.getInstance().loadPreference();

        PrivacyTrickUtil.clearOtherApps(mApp);
        AppLoadEngine.getInstance(mApp).preloadAllBaseInfo();
        quickGestureTipInit();
        ((ThirdAppManager) MgrContext.
                getManager(MgrContext.MGR_THIRD_APP)).
                getBackupList(AppBackupRestoreManager.BACKUP_PATH);
//        AppBackupRestoreManager.getInstance(mApp).getBackupList(saveFileName);
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
        saveSimIMEI();
        PrivacyContactManager.getInstance(mApp).getPrivateContacts();

        MgrContext.getManager(MgrContext.MGR_BATTERY);

        BlacklistTab.getInstance().initEncryptList();

        collectVideosData();

        return true;
    }

    //印度用户视频大小统计
    private void collectVideosData() {
        CollectVideoUtils.getAllVideoData();
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

    /*保存sim标识*/
    private void saveSimIMEI() {
        String simNu=PreferenceTable.getInstance().getString(PrefConst.KEY_SIM_IMEI);
        if (Utilities.isEmpty(simNu)) {
            LostSecurityManagerImpl manager = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
            manager.setSimIMEI();
        }
    }
}
