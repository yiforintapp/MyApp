
package com.leo.appmaster.engine;

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
import android.os.SystemClock;
import android.view.WindowManager;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.RecommentAppLockListActivity;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.applocker.model.ProcessDetector;
import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.appmanage.BackUpActivity;
import com.leo.appmaster.appmanage.BusinessAppInstallTracker;
import com.leo.appmaster.backup.AppBackupRestoreManager;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.db.LockRecommentTable;
import com.leo.appmaster.home.AutoStartGuideList;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.ThirdAppManager;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.model.extra.CacheInfo;
import com.leo.appmaster.phoneSecurity.PhoneSecurityManager;
import com.leo.appmaster.schedule.LockRecommentFetchJob;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.sdk.push.PushNotification;
import com.leo.appmaster.ui.dialog.LEOThreeButtonDialog;
import com.leo.appmaster.ui.dialog.LEOThreeButtonDialog.OnDiaogClickListener;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.Utilities;
import com.leo.appmater.globalbroadcast.LeoGlobalBroadcast;
import com.leo.appmater.globalbroadcast.PackageChangedListener;

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

public class AppLoadEngine extends BroadcastReceiver {
    private static final String TAG = "AppLoadEngine";
    private static final int NEWAPP_BACKUP_NUM = 10;

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
         * @param type    we have 5 change types currently, {@link #TYPE_ADD},
         *                {@link #TYPE_REMOVE}, {@link #TYPE_UPDATE}
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

    public final static String SAVE_LOCK_LIST = "save_lock_list";
    public final static String SAVE_LOCK_LIST_NUM = "save_lock_list_num";
    public static String[] sLocalLockArray;
    //            = new String[]{
//            "com.whatsapp", "com.android.mms",
//            "com.sonyericsson.conversations", "com.facebook.katana",
//            "com.android.gallery3d", "com.sec.android.gallery3d",
//            "com.sonyericsson.album", "com.android.contacts",
//            "com.google.android.contacts", "com.sonyericsson.android.socialphonebook",
//            "com.facebook.orca", "com.google.android.youtube",
//            "com.android.providers.downloads.ui", "com.sec.android.app.myfiles",
//            "com.android.email", "com.viber.voip",
//            "com.google.android.talk", "com.mxtech.videoplayer.ad",
//            "com.android.calendar", "com.google.android.calendar",
//            "com.tencent.mm", "com.tencent.mobileqq",
//            "com.tencent.qq", "jp.naver.line.android",
//            "com.twitter.android", "com.htc.soundrecorder",
//            "com.appstar.callrecorder", "com.samsung.everglades.video",
//            "com.android.dialer", "com.google.android.videos",
//            "com.google.plus", "com.sec.android.app.videoplayer",
//            "com.android.soundrecorder", "com.mediatek.videoplayer",
//            "com.android.vending", "com.android.settings",
//            "com.mediatek.filemanager"
//    };
    public static String[] sLocalLockNumArray;
//    = new String[]{
//            "24000000", "14000000",
//            "1200000", "14000000",
//            "15000000", "10000000",
//            "1500000", "10000000",
//            "1800000", "1200000",
//            "12000000", "7900000",
//            "6700000", "6300000",
//            "6500000", "1600000",
//            "5500000", "9800000",
//            "5200000", "1500000",
//            "1200000", "1200000",
//            "1200000", "2500000",
//            "1800000", "1500000",
//            "1500000", "4200000",
//            "5100000", "3800000",
//            "1500000", "1500000",
//            "5300000", "7400000",
//            "13000000", "16000000",
//            "10000000"
//    };

    public final static String[] sLocalRecommendedAppAndNum = new String[]{
            "com.whatsapp", "17500000",
            "com.android.gallery3d", "10900000",
            "com.android.settings", "10700000",
            "com.android.mms", "9600000",
            "com.facebook.katana", "9200000",
            "com.android.vending", "8400000",
            "com.facebook.orca", "8200000",
            "com.mxtech.videoplayer.ad", "7400000",
            "com.sec.android.gallery3d", "7000000",
            "com.android.contacts", "6900000",
            "com.mediatek.filemanager", "6800000",
            "com.google.android.gm", "5500000",
            "com.google.android.youtube", "5100000",
            "com.mediatek.videoplayer", "4700000",
            "com.android.browser", "4600000",
            "com.android.chrome", "4400000",
            "com.android.providers.downloads.ui", "4200000",
            "com.sec.android.app.myfiles", "4200000",
            "com.android.email", "4100000",
            "com.android.music", "4000000",
            "com.google.android.gms", "3800000",
            "com.google.android.talk", "3500000",
            "com.android.dialer", "3500000",
            "com.UCMobile.intl", "3400000",
            "com.google.android.apps.maps", "3400000",
            "com.lenovo.anyshare.gps", "3400000",
            "com.android.calendar", "3300000",
            "com.android.soundrecorder", "3300000",
            "com.google.android.music", "3200000",
            "com.google.android.apps.plus", "3200000",
            "com.bbm", "2800000",
            "com.android.calculator2", "2800000",
            "com.instagram.android", "2800000",
            "com.google.android.googlequicksearchbox", "2600000",
            "com.google.android.apps.docs", "2600000",
            "com.bsb.hike", "2200000",
            "com.uc.browser.en", "2300000",
            "com.imo.android.imoim", "2300000"
    };

    public static final String PG_PACKAGENAME = "com.android.vending";
    public static final long CLICKINTERVAl = 1000 * 60 * 60 * 2;
    private List<String> mRecommendLocklist;
    private List<String> mRecommendLockNumlist;
    private LockManager mLockManager;

    private AppLoadEngine(Context context) {
        mContext = context.getApplicationContext();
        mPm = mContext.getPackageManager();
        mLatch = new CountDownLatch(1);
        mAppDetails = new ConcurrentHashMap<String, AppItemInfo>();
        mListeners = new ArrayList<AppChangeListener>(1);

        divideRecommendedAppAndNum();
        List<String> list = AppMasterPreference.getInstance(mContext)
                .getRecommendList();
        if (list != null) {
            mRecommendLocklist = list;
        } else {
            mRecommendLocklist = Arrays.asList(sLocalLockArray);
        }
        List<String> listNum = AppMasterPreference.getInstance(mContext)
                .getRecommendNumList();
        if (listNum != null && listNum.size() > 0) {
            mRecommendLockNumlist = listNum;
        } else {
            mRecommendLockNumlist = Arrays.asList(sLocalLockNumArray);
        }


        mTracker = new BusinessAppInstallTracker();

        LeoGlobalBroadcast.registerBroadcastListener(mPackageChangedListener);
        mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
    }

    private void divideRecommendedAppAndNum() {
        int halflenth = sLocalRecommendedAppAndNum.length / 2;
        sLocalLockArray = new String[halflenth];
        sLocalLockNumArray = new String[halflenth];
        for (int i = 0; i < halflenth; i++) {
            sLocalLockArray[i] = sLocalRecommendedAppAndNum[i * 2];
            sLocalLockNumArray[i] = sLocalRecommendedAppAndNum[i * 2 + 1];
        }
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

    public List<String> getRecommendLockNumList() {
        return mRecommendLockNumlist;
    }

    private void updateRecommendLockList(List<String> list, List<String> listnum) {
        long start = SystemClock.elapsedRealtime();
        mRecommendLocklist = list;
        mRecommendLockNumlist = listnum;
        Collection<AppItemInfo> collection = mAppDetails.values();
        for (AppItemInfo appDetailInfo : collection) {
            int position = mRecommendLocklist.indexOf(appDetailInfo.packageName);
            int locknum = -1;
            if (position >= 0 && position < listnum.size()) {
                locknum = Integer.parseInt(listnum.get(position));
            }
            appDetailInfo.topPos = locknum;
        }

        LockRecommentTable table = new LockRecommentTable();
        table.insertLockRecommentList(list, listnum);
        LeoLog.d(TAG, "updateRecommendLockList, insert cost: " + (SystemClock.elapsedRealtime() - start));

    }

    private String makeListToString(List<String> list) {
        String combined = "";
        for (int i = 0; i < list.size(); i++) {
            String string = list.get(i);
            if (i == list.size() - 1) {
                combined = combined + string;
            } else {
                combined = combined + string + ";";
            }
        }
        return combined;
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
        if (pkg == null)
            return null;
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

        try {
            Collections.sort(dataList, new FolwComparator());
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    public ArrayList<AppItemInfo> getDeleteableApps(List<String> dropList) {
        loadAllPkgInfo();
        ArrayList<AppItemInfo> dataList = new ArrayList<AppItemInfo>();
        for (AppItemInfo app : mAppDetails.values()) {
            String startwithPackageName = "s:" + app.packageName;
            String containPackageName = "a:" + app.packageName;
            if (dropList != null) {
                if (!isStartWith(startwithPackageName, dropList) && !app.systemApp
                        && !dropList.contains(containPackageName)) {
                    dataList.add(app);
                }
            } else {
                if (!app.systemApp) {
                    dataList.add(app);
                }
            }
//            if (!app.packageName.startsWith("com.leo.theme") && !app.systemApp
//                    && !app.packageName.equals(AppMasterApplication.getInstance().getPackageName())) {
//                dataList.add(app);
//            }
        }

        Collections.sort(dataList, new ApkSizeComparator());
        return dataList;
    }

    private boolean isStartWith(String startwithPackageName, List<String> dropList) {
        for (int i = 0; i < dropList.size(); i++) {
            String name = dropList.get(i);
            if (startwithPackageName.startsWith(name)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
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
                AppMasterPreference pre = AppMasterPreference.getInstance(mContext);
                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                List<ResolveInfo> apps = mPm.queryIntentActivities(mainIntent, 0);

                // check first load, and save hide theme
                checkFirstLoad();
                List<String> themeList = new ArrayList<String>(pre.getHideThemeList());
                for (final ResolveInfo resolveInfo : apps) {
                    final ApplicationInfo applicationInfo = resolveInfo.activityInfo.applicationInfo;
                    final String packageName = applicationInfo.packageName;
                    // dont filter our app here
                    // if (packageName.equals(mContext.getPackageName()))
                    // continue;
                    final AppItemInfo appInfo = new AppItemInfo();
                    appInfo.type = BaseInfo.ITEM_TYPE_NORMAL_APP;
                    appInfo.packageName = packageName;
                    ThreadManager.executeOnSubThread(new Runnable() {
                        @Override
                        public void run() {
                            loadAppInfoOfPackage(packageName, resolveInfo.activityInfo.name, applicationInfo, appInfo);
                        }
                    });
                    try {
                        appInfo.installTime = mPm.getPackageInfo(packageName, 0).firstInstallTime;
                    } catch (NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    if (!isThemeApk(packageName)) {
                        mAppDetails.put(packageName, appInfo);
                        ThreadManager.executeOnFileThread(new Runnable() {
                            @Override
                            public void run() {
                                loadAppDetailInfo(packageName);
                            }
                        });
                    } else {
                        if (!themeList.contains(packageName)) {
                            themeList.add(packageName);
                        }
                    }
                }

                 /*
                  *	bug AM-3864
                  *	隐藏主题图标后上面的查询安装应用方式不能查询到已安装的隐藏主题
                  *	只是用来查询安装的主题
                  */
                if (mPm != null) {
                    List<ApplicationInfo> themeApp = mPm.getInstalledApplications(0);
                    if (themeApp != null && themeApp.size() > 0) {
                        for (ApplicationInfo info : themeApp) {
                            if (themeList != null) {
                                String packageName = info.packageName;
                                if (!themeList.contains(packageName) && isThemeApk(packageName)) {
                                    LeoLog.d(TAG, "loadLocalTheme --- packageName=" + packageName);
                                    themeList.add(packageName);
                                }
                            }
                        }
                    }
                }

                pre.setHideThemeList(themeList);
                LeoLog.i("setHideThemeList_time", "setHideThemeList_time:" + SystemClock.elapsedRealtime());
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
                if (packageName.isEmpty())
                    continue;
                // AppItemInfo appInfo = new AppItemInfo();
                Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setPackage(packageName);
                List<ResolveInfo> apps = mPm.queryIntentActivities(intent, 0);
                ResolveInfo info = apps.size() > 0 ? apps.get(0) : null;
                /*
                 * loadAppInfoOfPackage(packageName, info != null ?
                 * info.activityInfo.name : "", applicationInfo, appInfo); try {
                 * appInfo.installTime = mPm.getPackageInfo(packageName,
                 * 0).firstInstallTime; } catch (NameNotFoundException e) {
                 * e.printStackTrace(); }
                 */
                if (null != info) {
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

    private void loadAppInfoOfPackage(final String packageName,
                                      String activityName, ApplicationInfo applicationInfo, final AppItemInfo appInfo) {
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
        appInfo.systemApp = AppUtil.isSystemApp(applicationInfo);
        appInfo.inSdcard = AppUtil.isInstalledInSDcard(applicationInfo);
        try {
            appInfo.installTime = mPm.getPackageInfo(packageName, 0).lastUpdateTime;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        appInfo.uid = applicationInfo.uid;
        appInfo.sourceDir = applicationInfo.sourceDir;

        int position = mRecommendLocklist.indexOf(packageName);
        int locknum = -1;
        if (position >= 0 && position < mRecommendLockNumlist.size()) {
            locknum = Integer.parseInt(mRecommendLockNumlist.get(position));
        }
        appInfo.topPos = locknum;
//        appInfo.topPos = mRecommendLocklist.indexOf(packageName);

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
                    "getPackageSizeInfo", new Class[]{
                            String.class,
                            IPackageStatsObserver.class
                    });
            method.invoke(mPm, new Object[]{
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
                    // Toast.makeText(mContext, "监控卸载了应用:"+packageName,
                    // Toast.LENGTH_SHORT).show();
                }
                // else, we are replacing the package, so a PACKAGE_ADDED will
                // be sent
                // later, we will update the package at this time
            } else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {


                AppMasterPreference pref = AppMasterPreference.getInstance(mContext);
                boolean isShowBackup = pref.getIsNeedCutBackupUninstallAndPrivacyContact();
                if (!isShowBackup) {
                    showNewAddAppNoti(packageName);
                }


                LeoLog.d("AppLoadEngineAdd", "ACTION_PACKAGE_ADDED");
                LeoLog.d("AppLoadEngineAdd", "packName is : " + packageName);
                // ad wall 打点
                doStatistics(packageName);

                if (!replacing) {
                    op = AppChangeListener.TYPE_ADD;
                    LockMode lm = mLockManager.getCurLockMode();
                    if (lm != null && lm.defaultFlag != 0) {
                        showLockTip(packageName);
                    }
                    /*启动锁定手机指令后，监控系统新安装应用加锁*/
                    PhoneSecurityManager.getInstance(mContext).executeLockInstruInstallListener(packageName);
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
                        new String[]{
                                packageName
                        }));
            }
        }
    }

    private void showNewAddAppNoti(String packageName) {
        LeoLog.d("testBackupNoti", packageName + " come to it");
        String newApp = LeoPreference.getInstance().getString(Constants.NEW_APP_NUM);
        if (newApp == null) {
            LeoLog.d("testBackupNoti", "saveApp == null");
            LeoPreference.getInstance().putString(Constants.NEW_APP_NUM, packageName);
        } else {
            LeoLog.d("testBackupNoti", "saveApp : " + newApp);
            String[] newAppList = newApp.split(";");
            List<String> newlist = checkIsBackup(newAppList);
            newlist.add(packageName);
            LeoLog.d("testBackupNoti", "newSaveList : " + newlist.toString());
            LeoLog.d("testBackupNoti", "newSaveListSize : " + newlist.size());
            if (newlist.size() >= 0 && newlist.size() < NEWAPP_BACKUP_NUM) {
                saveInDb(newlist);
            } else {
                if (!Utilities.isActivityOnTop(mContext, BackUpActivity.class.getName())) {
                    PushNotification pushNotification = new PushNotification(mContext);
                    Intent intent = new Intent(mContext, StatusBarEventService.class);
                    intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE,
                            StatusBarEventService.EVENT_TEN_NEW_APP_BACKUP);
                    String title = mContext.getString(R.string.ten_new_app_toast);
                    String string = mContext.getString(R.string.ten_new_app_toast_content);
                    pushNotification.showNotification(intent, title, string,
                            R.drawable.ic_launcher_notification, pushNotification.NOTI_BACKUP);


                }
            }
        }

    }

    private void saveInDb(List<String> newlist) {
        String result = "";
        for (int i = 0; i < newlist.size(); i++) {
            String name = newlist.get(i);
            if (i == 0) {
                result = name;
            } else {
                result = result + ";" + name;
            }
        }
        LeoLog.d("testBackupNoti", "save result  : " + result);
        LeoPreference.getInstance().putString(Constants.NEW_APP_NUM, result);
    }

    private List<String> checkIsBackup(String[] text) {
        LeoLog.d("testBackupNoti", "checkIsBackup");
        List<String> newlist = StringArrToList(text);
        LeoLog.d("testBackupNoti", "newlist1:" + newlist.toString());
        ArrayList<AppItemInfo> apps = ((ThirdAppManager) MgrContext.
                getManager(MgrContext.MGR_THIRD_APP)).getRestoreList(AppBackupRestoreManager.BACKUP_PATH);
        for (AppItemInfo app : apps) {
            if (newlist.contains(app.packageName)) {
                newlist = removeContain(newlist, app.packageName);
            }
        }
        LeoLog.d("testBackupNoti", "newlist2:" + newlist.toString());
        //check is have ""
        newlist = removeContain(newlist, "");
        LeoLog.d("testBackupNoti", "newlist3:" + newlist.toString());
        //check app is uninstall ?
        newlist = checkIsUninstalled(newlist);
        LeoLog.d("testBackupNoti", "newlist4:" + newlist.toString());
        return newlist;
    }

    private List<String> checkIsUninstalled(List<String> newlist) {
        //check all apps,if don't exit , just remove it
        List<String> list = new ArrayList<String>();
        List<PackageInfo> packages = mContext.getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packages.size(); i++) {
            PackageInfo packageInfo = packages.get(i);
            String pckName = packageInfo.packageName;
            if (newlist.contains(pckName)) {
                list.add(pckName);
            }
        }
        return list;
    }

    private List<String> StringArrToList(String[] text) {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < text.length; i++) {
            String abc = text[i];
            list.add(abc);
        }
        return list;
    }

    private List<String> removeContain(List<String> newlist, String packageName) {
        int k = 0;
        boolean haveEmptyString = false;
        for (int i = 0; i < newlist.size(); i++) {
            String name = newlist.get(i);
            if (name.equals(packageName)) {
                k = i;
                haveEmptyString = true;
                break;
            }
        }

        if (haveEmptyString) newlist.remove(k);

        return newlist;
    }

    /**
     * adwall 广告安装打点
     *
     * @param packageName
     */
    private void doStatistics(String packageName) {
        long mNowTime = System.currentTimeMillis();
        // 小礼品盒
        long mGiftBoxClickTime = AppMasterPreference.getInstance(mContext).getAdClickTime();
        if (mNowTime - mGiftBoxClickTime < CLICKINTERVAl) {
            SDKWrapper.addEvent(mContext, SDKWrapper.P1,
                    "app_act", "adunlocktop_" + packageName);
        }
        // 外星人
        long mEtClickTime = AppMasterPreference.getInstance(mContext).getAdEtClickTime();
        if (mNowTime - mEtClickTime < CLICKINTERVAl) {
            SDKWrapper.addEvent(mContext, SDKWrapper.P1,
                    "app_act", "adunlockdraw_" + packageName);
        }
        // 底部banner
        long mBannerClickTime = AppMasterPreference.getInstance(mContext).getAdBannerClickTime();
        if (mNowTime - mBannerClickTime < CLICKINTERVAl) {
            SDKWrapper.addEvent(mContext, SDKWrapper.P1,
                    "app_act", "adunlockbanner" + packageName);
        }
        // 半屏banner
        long mHalfScreenBanner = AppMasterPreference.getInstance(mContext)
                .getHalfScreenBannerClickTime();
        if (mNowTime - mHalfScreenBanner < CLICKINTERVAl) {
            SDKWrapper.addEvent(mContext, SDKWrapper.P1,
                    "app_act", "adunlockbannerpop" + packageName);
        }
        // PG首页右上角礼品盒
        long mGiftBoxFromHome = AppMasterPreference.getInstance(mContext).getAdClickTimeFromHome();
        if (mNowTime - mGiftBoxFromHome < CLICKINTERVAl) {
            SDKWrapper.addEvent(mContext, SDKWrapper.P1,
                    "app_act", "home" + packageName);
        }
        //潜艇广告
        long submarineAdTime = AppMasterPreference.getInstance(mContext).getAdSubmarineClickTime();
        if (mNowTime - submarineAdTime < CLICKINTERVAl) {
            SDKWrapper.addEvent(mContext, SDKWrapper.P1,
                    "app_act", "adunlocksubmarine_$" + packageName);
        }
        //超人广告
        long supermanAdTime = AppMasterPreference.getInstance(mContext).getAdSupermanBannerClickTime();
        if (mNowTime - supermanAdTime < CLICKINTERVAl) {
            SDKWrapper.addEvent(mContext, SDKWrapper.P1,
                    "app_act", "adunlocksuperman_$" + packageName);
        }
    }

    /**
     * Call from the handler for ACTION_PACKAGE_ADDED, ACTION_PACKAGE_REMOVED
     * and ACTION_PACKAGE_CHANGED.
     */
    public void onReceive(Context context, final Intent intent) {
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
         */
        if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action)) {
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
                    AppChangeListener.TYPE_LOCAL_CHANGE, new String[]{}));
        } else if (ACTION_RECOMMEND_LIST_CHANGE.equals(action)) {
            ThreadManager.executeOnFileThread(new Runnable() {
                @Override
                public void run() {
                    updateRecommendLockList(intent
                                    .getStringArrayListExtra(Intent.EXTRA_PACKAGES),
                            intent.getStringArrayListExtra(LockRecommentFetchJob.EXTRA_NUM));
                }
            });
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
                    LinkedList<String> list = new LinkedList<String>();
                    list.add(packageName);
                    mLockManager.removePkgFromMode(list, mLockManager.getCurLockMode(), false);
                }

            }
        });
    }

    private void showLockTip(final String packageName) {
        ProcessDetector detector = new ProcessDetector();
        AppMasterPreference amp = AppMasterPreference.getInstance(mContext);
        if (amp.getLockType() == AppMasterPreference.LOCK_TYPE_NONE  //Not init, no tip
                || packageName.startsWith("com.leo.theme")
                || packageName.equals(Constants.CP_PACKAGE)
                || packageName.equals(Constants.ISWIPE_PACKAGE)
                || packageName.equals(Constants.PL_PKG_NAME)
                || packageName.equals(Constants.SEARCH_BOX_PACKAGE)
                || detector.isHomePackage(packageName)) {
            return;
        }

        if (/*AppMasterPreference.getInstance(mContext).isNewAppLockTip()*/false) {  // 3.6暂时关闭提示
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

                                    LockMode curMode = mLockManager.getCurLockMode();
                                    curMode.haveEverOpened = true;
                                    mLockManager.updateMode(curMode);

                                    intent = new Intent(mContext,
                                            RecommentAppLockListActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("install_lockApp",
                                            packageName);
                                    intent.putExtra("from",
                                            RecommentAppLockListActivity.RECOMMEND_FROM_LOCK);
                                    mContext.startActivity(intent);
                                    mLockManager.filterPackage(packageName, false);
                                } else {
                                    LinkedList<String> list = new LinkedList<String>();
                                    list.add(packageName);
                                    mLockManager.addPkg2Mode(list, mLockManager.getCurLockMode());
                                    AutoStartGuideList.saveSamSungAppLock();
                                }

                                /**
                                 * Samsung 5.1.1 sys 电池优化权限提示
                                 */
                                AutoStartGuideList.samSungSysTip(mContext, PrefConst.KEY_LOCK_SAMSUNG_TIP);
                            } else if (which == 2) {
                                if (pre.getLockType() == AppMasterPreference.LOCK_TYPE_NONE) {

                                    LockMode curMode = mLockManager.getCurLockMode();
                                    curMode.haveEverOpened = true;
                                    mLockManager.updateMode(curMode);

                                    intent = new Intent(mContext,
                                            RecommentAppLockListActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("from",
                                            RecommentAppLockListActivity.RECOMMEND_FROM_LOCK_MORE);
                                    intent.putExtra("install_lockApp",
                                            packageName);
                                    mContext.startActivity(intent);
                                    mLockManager.filterPackage(packageName, false);
                                } else {

                                    LeoLog.d("Track Lock Screen",
                                            "apply lockscreen form showLockTip");
                                    mLockManager.applyLock(
                                            LockManager.LOCK_MODE_FULL, mContext.getPackageName(),
                                            false,
                                            new LockManager.OnUnlockedListener() {
                                                @Override
                                                public void onUnlocked() {
                                                    Intent intent2;
                                                    LockMode lm = mLockManager.getCurLockMode();
                                                    if (lm != null && lm.defaultFlag == 1
                                                            && !lm.haveEverOpened) {
                                                        mLockManager.filterPackage(mContext.getPackageName(), 500);
                                                        intent2 = new Intent(mContext, RecommentAppLockListActivity.class);
                                                        intent2.putExtra("install_lockApp", packageName);
                                                        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        intent2.putExtra("from", RecommentAppLockListActivity.RECOMMEND_FROM_LOCK_MORE);
                                                        mContext.startActivity(intent2);
                                                        mLockManager.filterPackage(packageName, false);
                                                        lm.haveEverOpened = true;
                                                        mLockManager.updateMode(lm);
                                                    } else {
                                                        LinkedList<String> list = new LinkedList<String>();
                                                        list.add(packageName);
                                                        mLockManager.addPkg2Mode(list, lm);
                                                        mLockManager.filterPackage(mContext.getPackageName(), 500);
                                                        intent2 = new Intent(mContext, AppLockListActivity.class);
                                                        intent2.putExtra("from_lock_more", true);
                                                        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        mContext.startActivity(intent2);
                                                    }

                                                }

                                                public void onUnlockCanceled() {
                                                }

                                                @Override
                                                public void onUnlockOutcount() {

                                                }

                                                ;

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
                    AppBackupRestoreManager.getInstance(mContext).resetList();
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
            Integer a = (int) lhs.cacheInfo.total;
            Integer b = (int) rhs.cacheInfo.total;
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

    /* 查询客户端是否安装指定App */
    public boolean isInstallApp(String packageName) {
        ArrayList<AppItemInfo> appInfos = AppLoadEngine.getInstance(mContext).getAllPkgInfo();
        for (AppItemInfo appItemInfo : appInfos) {
            if (packageName.equals(appItemInfo.packageName)) {
                return true;
            }
        }
        return false;
    }

    public void uninstallApp(String packageName) {
        LockManager manager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
        manager.filterSelfOneMinites();
        AppUtil.uninstallApp(mContext, packageName);
    }
}
