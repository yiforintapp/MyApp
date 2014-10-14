
package com.leo.appmaster.engine;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageStats;
import android.content.pm.ResolveInfo;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.RemoteException;

import com.leo.appmaster.model.AppDetailInfo;
import com.leo.appmaster.model.CacheInfo;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.TextFormater;

public class AppLoadEngine extends BroadcastReceiver {
    private static final String TAG = "app engine";

    /**
     * Resister this listener to receive application changed events
     */
    public interface AppChangeListener {

        final static int TYPE_NONE = -1;
        /**
         * Applications added
         */
        public final static int TYPE_ADD = 0;
        /**
         * Applications uninstalled
         */
        public final static int TYPE_REMOVE = 1;
        /**
         * Application updated
         */
        public final static int TYPE_UPDATE = 2;        
        /**
         * Applications available, for those applications installed in external storage.
         */
        public final static int TYPE_AVAILABLE = 3;
        /**
         * Applications unavailable, for those applications installed in external storage.
         */
        public final static int TYPE_UNAVAILABLE = 4;

        /**
         * Called when applications changed, see
         * {@link #registerAppChangeListener(AppChangeListener)}
         * 
         * @param changes a list of changed applications
         * @param type we have 5 change types currently, {@link #TYPE_ADD},
         *            {@link #TYPE_REMOVE}, {@link #TYPE_UPDATE}
         */
        public void onAppChanged(final ArrayList<AppDetailInfo> changes, final int type);
    }

    private static AppLoadEngine mInstance;
    private Context mContext;
    private PackageManager mPm;
    private CountDownLatch mLatch;
    private boolean mInit;

    private boolean mAppsLoaded = false;

    private ArrayList<AppChangeListener> mListeners;

    private static final Object mLock = new Object();
    private static final HandlerThread sWorkerThread = new HandlerThread("apps-data-manager");
    static {
        sWorkerThread.start();
    }
    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());

    /*
     * do not change this data structure, because it is thread-safety
     */

    private ConcurrentHashMap<String, AppDetailInfo> mAppDetails;

    private AppLoadEngine(Context context) {
        mContext = context.getApplicationContext();
        mPm = mContext.getPackageManager();
        mLatch = new CountDownLatch(1);
        mAppDetails = new ConcurrentHashMap<String, AppDetailInfo>();
        mListeners = new ArrayList<AppChangeListener>(1);
    }

    public static synchronized AppLoadEngine getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new AppLoadEngine(context);
        }
        return mInstance;
    }

    public void registerAppChangeListener(AppChangeListener aListener) {
        if (mListeners.contains(aListener))  return;
        mListeners.add(aListener);
    }

    public void unregisterAppChangeListener(AppChangeListener aListener) {
        mListeners.remove(aListener);
    }

    public void clearAllListeners() {
        mListeners.clear();
    }

    private void notifyChange(ArrayList<AppDetailInfo> changed, int type) {
        for (AppChangeListener listener : mListeners) {
            listener.onAppChanged(changed, type);
        }
    }

    public void preloadAllBaseInfo() {
        sWorker.post(new Runnable() {
            @Override
            public void run() {
                loadAllPkgInfo();
                loadPowerComsuInfo();
            }
        });
    }

    public ArrayList<AppDetailInfo> getAllPkgInfo() {
        loadAllPkgInfo();
        ArrayList<AppDetailInfo> dataList = new ArrayList<AppDetailInfo>();
        for (AppDetailInfo app : mAppDetails.values()) {
            dataList.add(app);
        }
        return dataList;
    }

    public AppDetailInfo loadAppDetailInfo(String pkgName) {
        mLatch = new CountDownLatch(1);
        loadTrafficInfo(pkgName);
        loadPermissionInfo(pkgName);
        loadCacheInfo(pkgName);
        try {
            mLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return mAppDetails.get(pkgName);
    }

    private void loadAllPkgInfo() {
        synchronized (mLock) {
            if (!mAppsLoaded) {
                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                List<ResolveInfo> apps = mPm.queryIntentActivities(mainIntent, 0);
                for (ResolveInfo resolveInfo : apps) {
                    ApplicationInfo applicationInfo = resolveInfo.activityInfo.applicationInfo;
                    String packageName = applicationInfo.packageName;
                    AppDetailInfo appInfo = new AppDetailInfo();
                    loadAppInfoOfPackage(packageName, applicationInfo, appInfo);
                    mAppDetails.put(packageName, appInfo);
                }
                mAppsLoaded = true;
            }
        }
    }

    private void loadAppInfoOfPackage(String packageName, ApplicationInfo applicationInfo, AppDetailInfo appInfo) {
        // first fill base info
        try {
            PackageInfo pInfo = mPm.getPackageInfo(packageName, 0);
            appInfo.setVersionCode(pInfo.versionCode);
            appInfo.setVersionName(pInfo.versionName);
        } catch (NameNotFoundException e) {
        }
        appInfo.setPkg(packageName);
        appInfo.setAppLabel(applicationInfo.loadLabel(mPm).toString());
        appInfo.setAppIcon(applicationInfo.loadIcon(mPm));
        appInfo.setSystemApp(AppUtil.isSystemApp(applicationInfo));
        appInfo.setInSdcard(AppUtil.isInstalledInSDcard(applicationInfo));
        appInfo.setUid(applicationInfo.uid);
        appInfo.setSourceDir(applicationInfo.sourceDir);
    }

    private void loadPowerComsuInfo() {
        BatteryInfoProvider provider = new BatteryInfoProvider(mContext);
        List<BatteryComsuption> list = provider.getBatteryStats();
        for (BatteryComsuption batterySipper : list) {
            String packageName = batterySipper.getDefaultPackageName();
            if (packageName != null && mAppDetails.containsKey(packageName)) {
                mAppDetails.get(packageName).setPowerComsuPercent(
                        batterySipper.getPercentOfTotal());
            }
        }
    }

    private void loadCacheInfo(String pkgName) {
        AppDetailInfo info = mAppDetails.get(pkgName);
        getCacheInfo(pkgName, info.getCacheInfo());
    }

    private void loadPermissionInfo(String pkgName) {
        PackageInfo packangeInfo;
        AppDetailInfo info = mAppDetails.get(pkgName);
        try {
            packangeInfo = mPm.getPackageInfo(pkgName,
                    PackageManager.GET_PERMISSIONS);
//            info.getPermissionInfo().setPermissions(packangeInfo.permissions);
            
            info.getPermissionInfo().setPermissionList(packangeInfo.requestedPermissions);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void loadTrafficInfo(String pkgName) {
        AppDetailInfo info = mAppDetails.get(pkgName);
        if (info != null) {
            long received = TrafficStats.getUidRxBytes(info.getUid());
            if (received < 0) received = 0;
            long transmitted = TrafficStats.getUidTxBytes(info.getUid());
            if (transmitted < 0) transmitted = 0;
            info.getTrafficInfo().setTransmittedData(transmitted);
            info.getTrafficInfo().setReceivedData(received);
        }
        
    }

    private void getCacheInfo(String pkg, final CacheInfo cacheInfo) {
        try {
            Method method = PackageManager.class.getMethod(
                    "getPackageSizeInfo", new Class[] {
                            String.class,
                            IPackageStatsObserver.class
                    });
            method.invoke(mPm, new Object[] {
                    pkg,
                    new IPackageStatsObserver.Stub() {
                        @Override
                        public void onGetStatsCompleted(PackageStats pStats,
                                boolean succeeded) throws RemoteException {
                            long cacheSize = pStats.cacheSize;
                            long codeSize = pStats.codeSize;
                            long dataSize = pStats.dataSize;

                            cacheInfo.setCacheSize(TextFormater
                                    .dataSizeFormat(cacheSize));
                            cacheInfo.setCodeSize(TextFormater
                                    .dataSizeFormat(codeSize));
                            cacheInfo.setDataSize(TextFormater
                                    .dataSizeFormat(dataSize));

                            mLatch.countDown();
                        }
                    }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


         /**
         * Call from the handler for ACTION_PACKAGE_ADDED, ACTION_PACKAGE_REMOVED and
         * ACTION_PACKAGE_CHANGED.
         */
        public void onReceive(Context context, Intent intent) {        
            final String action = intent.getAction();
            
            if (Intent.ACTION_PACKAGE_REMOVED.equals(action)
                    || Intent.ACTION_PACKAGE_ADDED.equals(action)
                    || Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
                
                final String packageName = intent.getData().getSchemeSpecificPart();
                final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
                int op = AppChangeListener.TYPE_NONE;
    
              if (packageName == null || packageName.length() == 0) {
                    // they sent us a bad intent
                   return;
                }
              if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
                    op = AppChangeListener.TYPE_UPDATE;
              }else  if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                    if (!replacing) {
                        op = AppChangeListener.TYPE_REMOVE;
                    }
                    // else, we are replacing the package, so a PACKAGE_ADDED will be sent
                    // later, we will update the package at this time
                } else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                    if (!replacing) {
                        op = AppChangeListener.TYPE_ADD;
                    } else {
                        op =  AppChangeListener.TYPE_UPDATE;
                   }
               }  
    
                if (op != AppChangeListener.TYPE_NONE) {
                    enqueuePackageUpdated(new PackageUpdatedTask(op, new String[] { packageName }));
                }
           }  else if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action)) {
               // First, schedule to add these apps back in.
               String[] packages = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
               enqueuePackageUpdated(new PackageUpdatedTask(AppChangeListener.TYPE_AVAILABLE, packages));
           } else if (Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(action)) {
               String[] packages = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
               enqueuePackageUpdated(new PackageUpdatedTask(AppChangeListener.TYPE_UNAVAILABLE, packages));
           }
        }
        
        void enqueuePackageUpdated(PackageUpdatedTask task) {
            sWorker.post(task);
        }
    
        private class PackageUpdatedTask implements Runnable {
           int mOp;
            String[] mPackages;
    
            public PackageUpdatedTask(int op, String[] packages) {
                mOp = op;
                mPackages = packages;
           }
    
            public void run() {
                final ArrayList<AppDetailInfo> changedFinal = new ArrayList<AppDetailInfo>(1);
              final String[] packages = mPackages;
                final int N = packages.length;
                switch (mOp) {
               case AppChangeListener.TYPE_ADD:
               case AppChangeListener.TYPE_AVAILABLE:
                    for (int i = 0; i < N; i++) {
                        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                        mainIntent.setPackage(packages[i]);
                        List<ResolveInfo> apps = mPm.queryIntentActivities(mainIntent, 0);
                        if(apps.size() > 0) {
                            ApplicationInfo applicationInfo = apps.get(0).activityInfo.applicationInfo;
                            AppDetailInfo appInfo =  new AppDetailInfo();
                            loadAppInfoOfPackage(packages[i], applicationInfo, appInfo);
                            mAppDetails.put(packages[i], appInfo);
                           changedFinal.add(appInfo);
                        }
                    }
                    break;
                case AppChangeListener.TYPE_UPDATE:
                    for (int i = 0; i < N; i++) {
                        AppDetailInfo appInfo = mAppDetails.get(packages[i]);
                        if(appInfo != null) {
                            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                            mainIntent.setPackage(packages[i]);
                            List<ResolveInfo> apps = mPm.queryIntentActivities(mainIntent, 0);
                            if(apps.size() > 0) {
                                ApplicationInfo applicationInfo = apps.get(0).activityInfo.applicationInfo;
                                loadAppInfoOfPackage(packages[i], applicationInfo, appInfo);
                                changedFinal.add(appInfo);
                            }
                        }
                    }
                    break;
                case AppChangeListener.TYPE_REMOVE:
                case AppChangeListener.TYPE_UNAVAILABLE:
                    for (int i = 0; i < N; i++) {
                        AppDetailInfo appInfo = mAppDetails.remove(packages[i]);
                        changedFinal.add(appInfo);
                    }
                    break;
                }
    
                if (!changedFinal.isEmpty()) {
                    notifyChange(changedFinal, mOp);
                }
            }
        }

}
