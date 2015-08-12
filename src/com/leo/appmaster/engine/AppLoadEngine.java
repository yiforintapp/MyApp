
package com.leo.appmaster.engine;

import java.io.File;
import java.lang.reflect.Method;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
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
import android.graphics.drawable.Drawable;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.RemoteException;
import android.view.WindowManager;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.RecommentAppLockListActivity;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.manager.LockManager.OnUnlockedListener;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.appmanage.BusinessAppInstallTracker;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.model.extra.CacheInfo;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.dialog.LEOThreeButtonDialog;
import com.leo.appmaster.ui.dialog.LEOThreeButtonDialog.OnDiaogClickListener;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmater.globalbroadcast.LeoGlobalBroadcast;
import com.leo.appmater.globalbroadcast.PackageChangedListener;

public class AppLoadEngine extends BroadcastReceiver {
    public interface ThemeChanageListener {
        public void loadTheme();
    }

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
         * Applications available, for those applications installed in external
         * storage.
         */
        public final static int TYPE_AVAILABLE = 3;
        /**
         * Applications unavailable, for those applications installed in
         * external storage.
         */
        public final static int TYPE_UNAVAILABLE = 4;
        /**
         * Applications unavailable, for those applications installed in
         * external storage.
         */
        public final static int TYPE_LOCAL_CHANGE = 5;

        /**
         * Called when applications changed, see
         * {@link #registerAppChangeListener(AppChangeListener)}
         * 
         * @param changes a list of changed applications
         * @param type we have 5 change types currently, {@link #TYPE_ADD},
         *            {@link #TYPE_REMOVE}, {@link #TYPE_UPDATE}
         */
        public void onAppChanged(final ArrayList<AppItemInfo> changes,
                final int type);
    }

    public static final String ACTION_RECOMMEND_LIST_CHANGE = "com.leo.appmaster.RECOMMEND_LIST_CHANGE";

    private static AppLoadEngine mInstance;
    private Context mContext;
    private PackageManager mPm;
    private CountDownLatch mLatch;
    private boolean mAppsLoaded = false;
    private ThemeChanageListener mThemeListener;
    private BusinessAppInstallTracker mTracker;

    private ArrayList<AppChangeListener> mListeners;
    private final Object mLock = new Object();
    private static final HandlerThread sWorkerThread = new HandlerThread(
            "apps-data-manager");
    static {
        sWorkerThread.start();
    }
    private static final Handler sWorker = new Handler(
            sWorkerThread.getLooper());

    /*
     * do not change this data structure, because it is thread-safety
     */
    private ConcurrentHashMap<String, AppItemInfo> mAppDetails;

    private final static String[] sLocalLockArray = new String[] {
            "com.whatsapp", "com.android.gallery3d", "com.android.mms",
            "com.tencent.mm", "com.android.contacts", "com.facebook.katana",
            "com.mxtech.videoplayer.ad", "com.facebook.orca",
            "com.mediatek.filemanager", "com.sec.android.gallery3d",
            "com.android.settings", "com.android.email",
            "com.android.providers.downloads.ui",
            "com.sec.android.app.myfiles", "com.android.vending",
            "com.google.android.youtube", "com.mediatek.videoplayer",
            "com.android.calendar", "com.google.android.talk",
            "com.viber.voip", "com.android.soundrecorder",
            "com.sec.android.app.videoplayer", "com.tencent.mobileqq",
            "jp.naver.line.android", "com.tencent.qq", "com.google.plus",
            "com.tencent.mm", "com.google.android.videos",
            "com.android.dialer", "com.samsung.everglades.video",
            "com.appstar.callrecorder", "com.sec.android.app.voicerecorder",
            "com.htc.soundrecorder", "com.twitter.android"
    };

    private List<String> mRecommendLocklist;
    
    private AppLoadEngine(Context context) {
        mContext = context.getApplicationContext();
        mPm = mContext.getPackageManager();
        mLatch = new CountDownLatch(1);
        mAppDetails = new ConcurrentHashMap<String, AppItemInfo>();
        mListeners = new ArrayList<AppChangeListener>(1);

        List<String> list = AppMasterPreference.getInstance(mContext)
                .getRecommendList();
        if (list.get(0).equals("")) {
            mRecommendLocklist = Arrays.asList(sLocalLockArray);
        } else {
            mRecommendLocklist = list;
        }

        mTracker = new BusinessAppInstallTracker();

        LeoGlobalBroadcast.registerBroadcastListener(mPackageChangedListener);
    }

    private PackageChangedListener mPackageChangedListener = new PackageChangedListener() {
        @Override
        public void onPackageChanged(Intent intent) {
            onPackageEvent(intent);
            super.onPackageChanged(intent);
        }
    };

    public void setThemeChanageListener(ThemeChanageListener themeListener) {
        this.mThemeListener = themeListener;
    }

    public List<String> getRecommendLockList() {
        return mRecommendLocklist;
    }

    public void updateRecommendLockList(List<String> list) {
        mRecommendLocklist = list;
        Collection<AppItemInfo> collection = mAppDetails.values();
        for (AppItemInfo appDetailInfo : collection) {
            appDetailInfo.topPos = mRecommendLocklist
                    .indexOf(appDetailInfo.packageName);
        }
        AppMasterPreference.getInstance(mContext).setRecommendList(list);
    }

    public static synchronized AppLoadEngine getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new AppLoadEngine(context);
        }
        return mInstance;
    }

    public void registerAppChangeListener(AppChangeListener aListener) {
        if (mListeners.contains(aListener))
            return;
        synchronized (mLock) {
            mListeners.add(aListener);
        }
    }

    public void unregisterAppChangeListener(AppChangeListener aListener) {
        synchronized (mLock) {
            mListeners.remove(aListener);
        }
    }

    public void clearAllListeners() {
        synchronized (mLock) {
            mListeners.clear();
        }
    }

    private void notifyChange(ArrayList<AppItemInfo> changed, int type) {
        synchronized (mLock) {
            for (AppChangeListener listener : mListeners) {
                listener.onAppChanged(changed, type);
            }
        }
    }

    public void preloadAllBaseInfo() {
        sWorker.post(new Runnable() {
            @Override
            public void run() {
                loadAllPkgInfo();
            }
        });
    }

    public String getAppName(String pkg) {
        if (mAppDetails != null) {
            AppItemInfo info = mAppDetails.get(pkg);
            if (info != null) {
                return info.label;
            }
        }
        return "";
    }

    public Drawable getAppIcon(String pkg) {
        if(pkg == null) return null;
        if (mAppDetails != null) {
            AppItemInfo info = mAppDetails.get(pkg);
            if (info != null) {
                return info.icon;
            }
        }
        return null;
    }

    public ArrayList<AppItemInfo> getAllPkgInfo() {
        loadAllPkgInfo();
        ArrayList<AppItemInfo> dataList = new ArrayList<AppItemInfo>();
        for (AppItemInfo app : mAppDetails.values()) {
            if (!app.packageName.startsWith("com.leo.theme")) {
                dataList.add(app);
            }
        }

        Collections.sort(dataList, new FolwComparator());

        return dataList;
    }

    public int getAppCounts() {
        loadAllPkgInfo();
        ArrayList<AppItemInfo> dataList = new ArrayList<AppItemInfo>();
        for (AppItemInfo app : mAppDetails.values()) {
            if (!app.packageName.startsWith("com.leo.theme")) {
                dataList.add(app);
            }
        }
        return dataList.size();
    }

    public AppItemInfo getAppInfo(String pkg) {
        return mAppDetails.get(pkg);
    }

    public ArrayList<AppItemInfo> getDeleteableApps() {
        loadAllPkgInfo();
        ArrayList<AppItemInfo> dataList = new ArrayList<AppItemInfo>();
        for (AppItemInfo app : mAppDetails.values()) {
            if (!app.packageName.startsWith("com.leo.theme") && !app.systemApp
                    && !app.packageName.equals(AppMasterApplication.getInstance().getPackageName())) {
                dataList.add(app);
            }
        }
        Collections.sort(dataList, new ApkSizeComparator());
        return dataList;
    }
    
    
    public ArrayList<AppItemInfo> getLaunchTimeSortedApps() {
        loadAllPkgInfo();
        ArrayList<AppItemInfo> dataList = new ArrayList<AppItemInfo>();
        for (AppItemInfo app : mAppDetails.values()) {
            if (app.lastLaunchTime > 0) {
                dataList.add(app);
            }
        }
        Collections.sort(dataList, new LaunchTimeComparator());
        return dataList;
    }
    

    public AppItemInfo loadAppDetailInfo(String pkgName) {
        AppItemInfo info = mAppDetails.get(pkgName);
        if (info == null)
            return null;
        if (!info.detailLoaded) {
            mLatch = new CountDownLatch(1);
            loadTrafficInfo(pkgName);
            loadPermissionInfo(pkgName);
            loadCacheInfo(pkgName);
            try {
                mLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            info.detailLoaded = true;
        }
        return info;
    }

    private void loadAllPkgInfo() {
        synchronized (mLock) {
            if (!mAppsLoaded) {
                AppMasterPreference pre = AppMasterPreference
                        .getInstance(mContext);
                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                List<ResolveInfo> apps = mPm.queryIntentActivities(mainIntent,
                        0);

                // check first load, and save hide theme
                checkFirstLoad();
                List<String> themeList = new ArrayList<String>(
                        pre.getHideThemeList());
                for (ResolveInfo resolveInfo : apps) {
                    ApplicationInfo applicationInfo = resolveInfo.activityInfo.applicationInfo;
                    String packageName = applicationInfo.packageName;
                    // dont filter our app here
                    // if (packageName.equals(mContext.getPackageName()))
                    // continue;
                    AppItemInfo appInfo = new AppItemInfo();
                    appInfo.type = BaseInfo.ITEM_TYPE_NORMAL_APP;
                    loadAppInfoOfPackage(packageName, resolveInfo.activityInfo.name,
                            applicationInfo, appInfo);
                    try {
                        appInfo.installTime = mPm
                                .getPackageInfo(packageName, 0).firstInstallTime;
                    } catch (NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    if (!isThemeApk(packageName)) {
                        mAppDetails.put(packageName, appInfo);
                        loadAppDetailInfo(packageName);
                    } else {
                        if (!themeList.contains(packageName)) {
                            themeList.add(packageName);
                        }
                    }
                }
                pre.setHideThemeList(themeList);
                if (mThemeListener != null) {
                    mThemeListener.loadTheme();
                }
                mAppsLoaded = true;
            }
        }
    }

    private boolean checkFirstLoad() {
        AppMasterPreference pre = AppMasterPreference.getInstance(mContext);
        boolean isFirstLoadApp = !AppMasterPreference.getInstance(mContext)
                .haveEverAppLoaded();
        List<String> themeList = new ArrayList<String>(pre.getHideThemeList());
        if (isFirstLoadApp) {
            List<ApplicationInfo> all = mPm
                    .getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
            for (ApplicationInfo applicationInfo : all) {
                String packageName = applicationInfo.packageName;
                if(packageName.isEmpty())
                    continue;
//                AppItemInfo appInfo = new AppItemInfo();
                Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setPackage(packageName);
                List<ResolveInfo> apps = mPm.queryIntentActivities(intent, 0);
                ResolveInfo info = apps.size() > 0 ? apps.get(0) : null;
                /*loadAppInfoOfPackage(packageName, info != null ? info.activityInfo.name : "",
                        applicationInfo, appInfo);
                try {
                    appInfo.installTime = mPm.getPackageInfo(packageName, 0).firstInstallTime;
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }*/
                if(null != info){
                    if (isThemeApk(packageName)) {
                        if (!themeList.contains(packageName)) {
                            themeList.add(packageName);
                        }
                    }
                }
            }
            pre.setHaveEverAppLoaded(true);
            pre.setHideThemeList(themeList);
        }
        return isFirstLoadApp;
    }

    private void loadAppInfoOfPackage(String packageName,
            String activityName, ApplicationInfo applicationInfo, AppItemInfo appInfo) {
        // first fill base info
        try {
            PackageInfo pInfo = mPm.getPackageInfo(packageName, 0);
            appInfo.versionCode = pInfo.versionCode;
            appInfo.versionName = pInfo.versionName;
        } catch (NameNotFoundException e) {
        }
        appInfo.packageName = packageName;
        appInfo.activityName = activityName;
        appInfo.label = applicationInfo.loadLabel(mPm).toString().trim();
        appInfo.icon = AppUtil.loadAppIconDensity(packageName);
//        try {
//            appInfo.icon = applicationInfo.loadIcon(mPm);
//        } catch (Exception e) {
//            appInfo.icon = mPm.getDefaultActivityIcon();
//        }
        appInfo.systemApp = AppUtil.isSystemApp(applicationInfo);
        appInfo.inSdcard = AppUtil.isInstalledInSDcard(applicationInfo);
        try {
            appInfo.installTime = mPm.getPackageInfo(packageName, 0).lastUpdateTime;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        appInfo.uid = applicationInfo.uid;
        appInfo.sourceDir = applicationInfo.sourceDir;
        appInfo.topPos = mRecommendLocklist.indexOf(packageName);
    }

    // private void loadPowerComsuInfo() {
    // BatteryInfoProvider provider = new BatteryInfoProvider(mContext);
    // List<BatteryComsuption> list = provider.getBatteryStats();
    // for (BatteryComsuption batterySipper : list) {
    // String packageName = batterySipper.getDefaultPackageName();
    // if (packageName != null && mAppDetails.containsKey(packageName)) {
    // mAppDetails.get(packageName).powerComsuPercent = batterySipper
    // .getPercentOfTotal();
    // }
    // }
    // }

    private void loadCacheInfo(String pkgName) {
        AppItemInfo info = mAppDetails.get(pkgName);
        getCacheInfo(pkgName, info.cacheInfo);
    }

    private void loadPermissionInfo(String pkgName) {
        PackageInfo packangeInfo;
        AppItemInfo info = mAppDetails.get(pkgName);
        try {
            packangeInfo = mPm.getPackageInfo(pkgName,
                    PackageManager.GET_PERMISSIONS);
            // info.getPermissionInfo().setPermissions(packangeInfo.permissions);

            info.permissionInfo.mPermissionList = packangeInfo.requestedPermissions;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void loadTrafficInfo(String pkgName) {
        AppItemInfo info = mAppDetails.get(pkgName);
        if (info != null) {
            long received = TrafficStats.getUidRxBytes(info.uid);
            if (received < 0)
                received = 0;
            long transmitted = TrafficStats.getUidTxBytes(info.uid);
            if (transmitted < 0)
                transmitted = 0;
            info.trafficInfo.mTransmittedData = transmitted;
            info.trafficInfo.mReceivedData = received;
            info.trafficInfo.total = transmitted + received;
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

                            cacheInfo.cacheSize = cacheSize;
                            cacheInfo.codeSize = codeSize;
                            cacheInfo.dataSize = dataSize;
                            cacheInfo.total = cacheSize + codeSize + dataSize;
                            mLatch.countDown();
                        }
                    }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BusinessAppInstallTracker getBusinessTracker() {
        return mTracker;
    }

    public void onDestroyed() {
        LeoGlobalBroadcast.unregisterBroadcastListener(mPackageChangedListener);
    }

    private void onPackageEvent(Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_PACKAGE_REMOVED.equals(action)
                || Intent.ACTION_PACKAGE_ADDED.equals(action)
                || Intent.ACTION_PACKAGE_CHANGED.equals(action)) {

            final String packageName = intent.getData().getSchemeSpecificPart();
            final boolean replacing = intent.getBooleanExtra(
                    Intent.EXTRA_REPLACING, false);
            int op = AppChangeListener.TYPE_NONE;

            if (packageName == null || packageName.length() == 0) {
                // they sent us a bad intent
                return;
            }
            if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
                op = AppChangeListener.TYPE_UPDATE;
            } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                if (!replacing) {
                    op = AppChangeListener.TYPE_REMOVE;
                    checkUnlockWhenRemove(packageName);
                    checkThemeWhenRemove(packageName);
                }
                // else, we are replacing the package, so a PACKAGE_ADDED will
                // be sent
                // later, we will update the package at this time
            } else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                if (!replacing) {
                    op = AppChangeListener.TYPE_ADD;
                    LockMode lm = LockManager.getInstatnce().getCurLockMode();
                    if (lm != null && lm.defaultFlag != 0) {
                        showLockTip(packageName);
                    }

                    mTracker.onAppInstalled(packageName);
                    if (isThemeApk(packageName)) {

                        AppMasterPreference pre = AppMasterPreference
                                .getInstance(mContext);
                        List<String> list = pre.getHideThemeList();
                        List<String> themeList = null;
                        if (list == null) {
                            themeList = new ArrayList<String>();
                        } else {
                            themeList = new ArrayList<String>(list);
                        }

                        if (!themeList.contains(packageName)) {
                            themeList.add(0, packageName);
                        }
                        pre.setHideThemeList(themeList);
                        return;
                    }
                } else {
                    op = AppChangeListener.TYPE_UPDATE;
                }
            }

            if (op != AppChangeListener.TYPE_NONE) {
                enqueuePackageUpdated(new PackageUpdatedTask(op,
                        new String[] {
                                packageName
                        }));
            }
        }
    }

    /**
     * Call from the handler for ACTION_PACKAGE_ADDED, ACTION_PACKAGE_REMOVED
     * and ACTION_PACKAGE_CHANGED.
     */
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        /*
         * if (Intent.ACTION_PACKAGE_REMOVED.equals(action) ||
         * Intent.ACTION_PACKAGE_ADDED.equals(action) ||
         * Intent.ACTION_PACKAGE_CHANGED.equals(action)) { final String
         * packageName = intent.getData().getSchemeSpecificPart(); final boolean
         * replacing = intent.getBooleanExtra( Intent.EXTRA_REPLACING, false);
         * int op = AppChangeListener.TYPE_NONE; if (packageName == null ||
         * packageName.length() == 0) { // they sent us a bad intent return; }
         * if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) { op =
         * AppChangeListener.TYPE_UPDATE; } else if
         * (Intent.ACTION_PACKAGE_REMOVED.equals(action)) { if (!replacing) { op
         * = AppChangeListener.TYPE_REMOVE; checkUnlockWhenRemove(packageName);
         * checkThemeWhenRemove(packageName); } // else, we are replacing the
         * package, so a PACKAGE_ADDED will // be sent // later, we will update
         * the package at this time } else if
         * (Intent.ACTION_PACKAGE_ADDED.equals(action)) { if (!replacing) { op =
         * AppChangeListener.TYPE_ADD; showLockTip(packageName);
         * mTracker.onAppInstalled(packageName); if (isThemeApk(packageName)) {
         * AppMasterPreference pre = AppMasterPreference .getInstance(mContext);
         * List<String> themeList = new ArrayList<String>(
         * pre.getHideThemeList()); if (!themeList.contains(packageName)) {
         * themeList.add(0, packageName); } pre.setHideThemeList(themeList);
         * return; } } else { op = AppChangeListener.TYPE_UPDATE; } } if (op !=
         * AppChangeListener.TYPE_NONE) { enqueuePackageUpdated(new
         * PackageUpdatedTask(op, new String[] { packageName })); } } else
         */if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action)) {
            // First, schedule to add these apps back in.
            String[] packages = intent
                    .getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
            enqueuePackageUpdated(new PackageUpdatedTask(
                    AppChangeListener.TYPE_AVAILABLE, packages));
        } else if (Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE
                .equals(action)) {
            String[] packages = intent
                    .getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
            enqueuePackageUpdated(new PackageUpdatedTask(
                    AppChangeListener.TYPE_UNAVAILABLE, packages));
        } else if (Intent.ACTION_LOCALE_CHANGED.equals(action)) {
            enqueuePackageUpdated(new PackageUpdatedTask(
                    AppChangeListener.TYPE_LOCAL_CHANGE, new String[] {}));
        } else if (ACTION_RECOMMEND_LIST_CHANGE.equals(action)) {
            updateRecommendLockList(intent
                    .getStringArrayListExtra(Intent.EXTRA_PACKAGES));
        }
    }

    private void checkThemeWhenRemove(final String packageName) {
        sWorker.post(new Runnable() {
            @Override
            public void run() {

                if (packageName.equals(AppMasterApplication.usedThemePackage)) {
                    AppMasterApplication
                            .setSharedPreferencesValue(Constants.DEFAULT_THEME);
                }

                AppMasterPreference pre = AppMasterPreference
                        .getInstance(mContext);
                List<String> themeList = new ArrayList<String>(pre
                        .getHideThemeList());
                if (themeList.contains(packageName)) {
                    LeoLog.d("checkThemeWhenRemove", "packageName = "
                            + packageName);
                    themeList.remove(packageName);
                    pre.setHideThemeList(themeList);
                }
            }
        });
    }

    private void checkUnlockWhenRemove(final String packageName) {
        sWorker.post(new Runnable() {
            @Override
            public void run() {

                if (AppMasterPreference.getInstance(mContext).getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
                    LockManager lm = LockManager.getInstatnce();
                    LinkedList<String> list = new LinkedList<String>();
                    list.add(packageName);
                    lm.removePkgFromMode(list, lm.getCurLockMode());
                }

            }
        });
    }

    private void showLockTip(final String packageName) {

        if (packageName.startsWith("com.leo.theme") || packageName.equals(Constants.CP_PACKAGE)) {
            return;
        }

        if (AppMasterPreference.getInstance(mContext).isNewAppLockTip()) {
            sWorker.postDelayed(new Runnable() {
                @Override
                public void run() {
                    LEOThreeButtonDialog dialog = new LEOThreeButtonDialog(
                            mContext);
                    dialog.setTitle(R.string.app_name);
                    String tip = mContext.getString(
                            R.string.new_install_lock_remind,
                            AppUtil.getAppLabel(packageName, mContext));
                    dialog.setContent(tip);
                    dialog.setMiddleBtnStr(mContext.getResources().getString(R.string.lock));
                    dialog.setRightBtnStr(mContext.getResources().getString(R.string.lock_more));
                    dialog.setOnClickListener(new OnDiaogClickListener() {
                        @Override
                        public void onClick(int which) {
                            AppMasterPreference pre = AppMasterPreference
                                    .getInstance(mContext);
                            Intent intent = null;

                            if (which == 0) {
                            } else if (which == 1) {
                                SDKWrapper.addEvent(mContext, SDKWrapper.P1,
                                        "lock_enter", "lock");
                                if (pre.getLockType() == AppMasterPreference.LOCK_TYPE_NONE) {
                                    intent = new Intent(mContext,
                                            RecommentAppLockListActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("install_lockApp",
                                            packageName);
                                    intent.putExtra("from",
                                            RecommentAppLockListActivity.RECOMMEND_FROM_LOCK);
                                    mContext.startActivity(intent);
                                    LockManager.getInstatnce().addFilterLockPackage(packageName,
                                            false);
                                } else {
                                    LockManager lm = LockManager.getInstatnce();
                                    LinkedList<String> list = new LinkedList<String>();
                                    list.add(packageName);
                                    lm.addPkg2Mode(list, lm.getCurLockMode());
                                }

                            } else if (which == 2) {
                                if (pre.getLockType() == AppMasterPreference.LOCK_TYPE_NONE) {
                                    intent = new Intent(mContext,
                                            RecommentAppLockListActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("from",
                                            RecommentAppLockListActivity.RECOMMEND_FROM_LOCK_MORE);
                                    intent.putExtra("install_lockApp",
                                            packageName);
                                    mContext.startActivity(intent);
                                    LockManager.getInstatnce().addFilterLockPackage(packageName,
                                            false);
                                } else {

                                    LeoLog.d("Track Lock Screen",
                                            "apply lockscreen form showLockTip");
                                    LockManager.getInstatnce().applyLock(
                                            LockManager.LOCK_MODE_FULL, mContext.getPackageName(),
                                            false,
                                            new OnUnlockedListener() {
                                                @Override
                                                public void onUnlocked() {
                                                    Intent intent2;
                                                    LockMode lm = LockManager.getInstatnce()
                                                            .getCurLockMode();
                                                    if (lm != null && lm.defaultFlag == 1
                                                            && !lm.haveEverOpened) {
                                                        LockManager.getInstatnce().timeFilter(
                                                                mContext.getPackageName(),
                                                                500);
                                                        intent2 = new Intent(mContext,
                                                                RecommentAppLockListActivity.class);
                                                        intent2.putExtra("install_lockApp",
                                                                packageName);
                                                        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        intent2.putExtra(
                                                                "from",
                                                                RecommentAppLockListActivity.RECOMMEND_FROM_LOCK_MORE);
                                                        mContext.startActivity(intent2);
                                                        LockManager.getInstatnce()
                                                                .addFilterLockPackage(packageName,
                                                                        false);
                                                        lm.haveEverOpened = true;
                                                        LockManager.getInstatnce().updateMode(lm);
                                                    } else {
                                                        LinkedList<String> list = new LinkedList<String>();
                                                        list.add(packageName);
                                                        LockManager.getInstatnce().addPkg2Mode(
                                                                list, lm);
                                                        LockManager.getInstatnce().timeFilter(
                                                                mContext.getPackageName(),
                                                                500);
                                                        intent2 = new Intent(mContext,
                                                                AppLockListActivity.class);
                                                        intent2.putExtra("from_lock_more", true);
                                                        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        mContext.startActivity(intent2);
                                                    }

                                                }

                                                public void onUnlockCanceled() {
                                                }

                                                @Override
                                                public void onUnlockOutcount() {

                                                };

                                            });
                                }
                                SDKWrapper.addEvent(mContext, SDKWrapper.P1,
                                        "lock_enter", "sug_lock_more");
                            }
                        }
                    });
                    dialog.getWindow().setType(
                            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    dialog.show();
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "tdau", "dialogue");
                }
            }, 5000);
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
            final ArrayList<AppItemInfo> changedFinal = new ArrayList<AppItemInfo>(
                    1);
            final String[] packages = mPackages;
            final int N = packages.length;
            switch (mOp) {
                case AppChangeListener.TYPE_ADD:
                case AppChangeListener.TYPE_AVAILABLE:
                    for (int i = 0; i < N; i++) {
                        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                        mainIntent.setPackage(packages[i]);
                        List<ResolveInfo> apps = mPm.queryIntentActivities(
                                mainIntent, 0);
                        if (apps.size() > 0) {
                            ApplicationInfo applicationInfo = apps.get(0).activityInfo.applicationInfo;
                            AppItemInfo appInfo = new AppItemInfo();
                            loadAppInfoOfPackage(packages[i], apps.get(0).activityInfo.name,
                                    applicationInfo,
                                    appInfo);
                            mAppDetails.put(packages[i], appInfo);
                            changedFinal.add(appInfo);
                            loadAppDetailInfo(appInfo.packageName);
                        }
                    }
                    break;
                case AppChangeListener.TYPE_UPDATE:
                    for (int i = 0; i < N; i++) {
                        AppItemInfo appInfo = mAppDetails.get(packages[i]);
                        if (appInfo != null) {
                            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                            mainIntent.setPackage(packages[i]);
                            List<ResolveInfo> apps = mPm.queryIntentActivities(
                                    mainIntent, 0);
                            if (apps.size() > 0) {
                                ApplicationInfo applicationInfo = apps.get(0).activityInfo.applicationInfo;
                                loadAppInfoOfPackage(packages[i], apps.get(0).activityInfo.name,
                                        applicationInfo,
                                        appInfo);
                                changedFinal.add(appInfo);
                            }
                        }
                    }
                    break;
                case AppChangeListener.TYPE_REMOVE:
                case AppChangeListener.TYPE_UNAVAILABLE:
                    for (int i = 0; i < N; i++) {
                        AppItemInfo appInfo = mAppDetails.remove(packages[i]);
                        changedFinal.add(appInfo);
                    }
                    break;
                case AppChangeListener.TYPE_LOCAL_CHANGE:
                    mAppDetails.clear();
                    mAppsLoaded = false;
                    loadAllPkgInfo();
                    AppMasterApplication.getInstance().getBuckupManager()
                            .resetList();
                    break;

            }

            if (!changedFinal.isEmpty()) {
                notifyChange(changedFinal, mOp);
            }
        }
    }

    public boolean isThemeApk(final String pkg) {
        if (pkg.startsWith("com.leo.theme")) {
            return true; // add app list
        } else {
            return false;
        }

    }

    public static class FolwComparator implements Comparator<AppItemInfo> {

        @Override
        public int compare(AppItemInfo lhs, AppItemInfo rhs) {
            if (lhs.topPos > -1 && rhs.topPos < 0) {
                return -1;
            } else if (lhs.topPos < 0 && rhs.topPos > -1) {
                return 1;
            } else if (lhs.topPos > -1 && rhs.topPos > -1) {
                return lhs.topPos - rhs.topPos;
            }
            return Collator.getInstance().compare(trimString(lhs.label),
                    trimString(rhs.label));
        }

        private String trimString(String s) {
            return s.replaceAll("\u00A0", "").trim();
        }

    }

    public static long getApkSize(AppItemInfo app) {
        File file = new File(app.sourceDir);
        if (file.isFile() && file.exists()) {
            long size = file.length();
            return size;
        }
        return 0;
    }

    public String getActivityName(String pkg) {
        AppItemInfo appInfo = mAppDetails.get(pkg);
        if (appInfo != null) {
            return appInfo.activityName;
        } else {
            return "";
        }
    }

    public void recordAppLaunchTime(String lastRunningPkg, long time) {
        AppItemInfo info = mAppDetails.get(lastRunningPkg);
        if (info != null) {
            info.lastLaunchTime = time;
        }
    }

    public static class ApkSizeComparator implements Comparator<AppItemInfo> {

        @Override
        public int compare(AppItemInfo lhs, AppItemInfo rhs) {
            Integer a = (int) getApkSize(lhs);
            Integer b = (int) getApkSize(rhs);
            return b.compareTo(a);
        }
    }

    public static class LaunchTimeComparator implements Comparator<AppItemInfo> {

        @Override
        public int compare(AppItemInfo lhs, AppItemInfo rhs) {
            Long a = lhs.lastLaunchTime;
            Long b = rhs.lastLaunchTime;
            return a.compareTo(b);
        }
    }

}
