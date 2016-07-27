
package com.zlf.appmaster.bootstrap;

import com.zlf.appmaster.AppMasterPreference;
import com.zlf.appmaster.Constants;
import com.zlf.appmaster.PhoneInfo;
import com.zlf.appmaster.ThreadManager;
import com.zlf.appmaster.utils.AppUtil;
import com.zlf.imageloader.DisplayImageOptions;
import com.zlf.imageloader.ImageLoader;
import com.zlf.imageloader.ImageLoaderConfiguration;

/**
 * 异步初始化，旧版本的startInitTask
 *
 * @author Jasper
 */
public class InitAsyncBootstrap extends com.zlf.appmaster.bootstrap.Bootstrap {
    private static final String TAG = "InitAsyncBootstrap";

    InitAsyncBootstrap() {
        super();
    }

    @Override
    protected boolean doStrap() {


        initImageLoader();
//        PrivacyTrickUtil.clearOtherApps(mApp);
        quickGestureTipInit();
//        AppBackupRestoreManager.getInstance(mApp).getBackupList(saveFileName);
//        PrivacyContactManager.getInstance(mApp).getPrivateContacts();
        // GP check
        boolean isAppInstalled;

        try {
            isAppInstalled = AppUtil.appInstalled(mApp, "com.leo.appmaster");
        } catch (Exception e) {
            isAppInstalled = false;
        }

        if (!isAppInstalled) {

        }

        saveSimIMEI();
//        PrivacyContactManager.getInstance(mApp).getPrivateContacts();

//        MgrContext.getManager(MgrContext.MGR_BATTERY);



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
//        String simNu = LeoPreference.getInstance().getString(PrefConst.KEY_SIM_IMEI);
//        if (simNu == null) {
//            LostSecurityManagerImpl manager = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
//            manager.setSimIMEI();
//        }
    }
}
