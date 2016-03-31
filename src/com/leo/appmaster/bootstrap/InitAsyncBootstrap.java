
package com.leo.appmaster.bootstrap;

import com.airsig.airsigengmulti.ASEngine;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.PhoneInfo;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.airsig.airsigsdk.ASGui;
import com.leo.appmaster.airsig.airsigsdk.ASSetting;
import com.leo.appmaster.backup.AppBackupRestoreManager;
import com.leo.appmaster.db.BlacklistTab;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.ThirdAppManager;
import com.leo.appmaster.mgr.impl.LostSecurityManagerImpl;
import com.leo.appmaster.privacycontact.PrivacyContactManager;
import com.leo.appmaster.privacycontact.PrivacyTrickUtil;
import com.leo.appmaster.schedule.FetchScheduleJob;
import com.leo.appmaster.schedule.ScreenRecommentJob;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.Utilities;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.ImageLoaderConfiguration;

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
        initImageLoader();
        PrivacyTrickUtil.clearOtherApps(mApp);
        AppLoadEngine.getInstance(mApp).preloadAllBaseInfo();
        quickGestureTipInit();
        ((ThirdAppManager) MgrContext.getManager(MgrContext.MGR_THIRD_APP)).getBackupList(AppBackupRestoreManager.BACKUP_PATH);
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

        ScreenRecommentJob.initialize();
        FetchScheduleJob.startFetchJobs();
        saveSimIMEI();
        PrivacyContactManager.getInstance(mApp).getPrivateContacts();

        MgrContext.getManager(MgrContext.MGR_BATTERY);

        BlacklistTab.getInstance().initEncryptList();

        //airSig
        initAirSig();
        return true;
    }

    private void initImageLoader() {
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheOnDisk(true).build();
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(mApp);
        builder.taskExecutor(ThreadManager.getNetworkExecutor());
        builder.taskExecutorForCachedImages(ThreadManager.getAsyncExecutor());
        builder.threadPoolSize(Constants.MAX_THREAD_POOL_SIZE);
        builder.threadPriority(Thread.NORM_PRIORITY);
        builder.memoryCacheSizePercentage(8);
        builder.defaultDisplayImageOptions(options);
//        if (AppMasterConfig.LOGGABLE) {
//            builder.writeDebugLogs();
//        }
        builder.diskCacheSize(Constants.MAX_DISK_CACHE_SIZE); // 100 Mb
        builder.denyCacheImageMultipleSizesInMemory();
        ImageLoader.getInstance().init(builder.build());
    }

    private void initAirSig() {
        ASSetting setting = new ASSetting();
        setting.engineParameters = ASEngine.ASEngineParameters.Unlock;


        ASGui.getSharedInstance(AppMasterApplication.getInstance(), null, setting, null); // Database is in /data/data/...
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
        String simNu= LeoPreference.getInstance().getString(PrefConst.KEY_SIM_IMEI);
        if (Utilities.isEmpty(simNu)) {
            LostSecurityManagerImpl manager = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
            manager.setSimIMEI();
        }
    }
}
