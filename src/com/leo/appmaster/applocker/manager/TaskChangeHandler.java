
package com.leo.appmaster.applocker.manager;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.text.TextUtils;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.utils.LeoLog;

public class TaskChangeHandler {
    private static final String TAG = "TaskChangeHandler";

    public static final String EXTRA_LOCKED_APP_PKG = "locked_app_pkg";

    public static final String LOCKSCREENNAME = "LockScreenActivity";
    public static final String HOMENAME = "HomeActivity";
    public static final String SPLASHNAME = "SplashActivity";
    public static final String PROXYNAME = "ProxyActivity";
    public static final String DESKPROXYNAME = "DeskProxyActivity";
    public static final String BATTERYVIEW = "BatteryShowViewActivity";
    public static final String GAMEBOX = "GameBoxActivity";
    public static final String WAITNAME = "WaitActivity";
    public static final String WEBVIEW = "WebViewActivity";
    public static final String UPDATE = "UpdateActivity";
    public static final String BLACK_LIST = "AskAddToBlacklistActivity";
    // public static final String AD = "AdMobvistaAct";
    // public static final String GESTURESETTING = "QuickGestureActivity";
    public static final String LAUNCHERBOOST = "HomeBoostActivity";

    //
    public static final String APPWALL = "AdMobvistaAct";
    public static final String DESKAD = "DeskAdActivity";

    private static final String DOWNLAOD_PKG = "com.android.providers.downloads.ui";
    private static final String DOWNLAOD_PKG_21 = "com.android.documentsui";

    private static final String GOOGLE_LAUNCHER_PKG = "com.google.android.launcher";
    private static final String GOOGLE_LAUNCHER_PKG21 = "com.google.android.googlequicksearchbox";

    private static final boolean DBG = true;

    private Context mContext;
    private ActivityManager mAm;
    private String mLastRunningPkg = "";
    private String mLastRuningActivity = "";

    private boolean mIsFirstDetect;

    // 解锁前检测到的真正有效的pkg
    private String mDetectedPkgBeforeScreeOff;
    // 是否忽略锁屏屏保
    private boolean mIgnoreBattery = true;

    public TaskChangeHandler(Context context) {
        mContext = context.getApplicationContext();
        mAm = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        mLastRunningPkg = getRunningPkg();
        mIsFirstDetect = true;
    }

    private String getRunningPkg() {
        String pkg = null;
        List<RunningTaskInfo> list = mAm.getRunningTasks(1);
        if (list != null && !list.isEmpty()) {
            pkg = list.get(0).topActivity.getPackageName();
        }
        return pkg;
    }

    public String getLastRunningPackage() {
        return mLastRunningPkg;
    }

    public String getLastRunningActivity() {
        return mLastRuningActivity;
    }

    public void setPkgBeforeScreenOff(String pkg) {
        LeoLog.d(TAG, "setPkgBeforeScreenOff pkg: " + pkg);
        if (Constants.PKG_WHAT_EVER.equals(pkg) ||
                Constants.PKG_LENOVO_SCREEN.equals(pkg) || mContext.getPackageName().equals(pkg))
            return;

        mDetectedPkgBeforeScreeOff = pkg;
    }

    public void ignoreBatteryPage(boolean ignoreBattery) {
        mIgnoreBattery = ignoreBattery;
    }

    public void handleAppLaunch(String pkg, String activity, String baseActivity) {
        if (pkg == null || activity == null)
            return;
        // For android 5.0, download package changed
        if (pkg.equals(DOWNLAOD_PKG_21)) {
            pkg = DOWNLAOD_PKG;
        }

        if (mIsFirstDetect) {
            LeoLog.d("Track Lock Screen", "is first lock,so we ignor this time");
            mLastRunningPkg = pkg;
            mLastRuningActivity = activity;
            mIsFirstDetect = false;
            return;
        }
        String myPackage = mContext.getPackageName();

        boolean containsLauncher = activity.contains("Launcher");
        AppMasterApplication app = AppMasterApplication.getInstance();
        if (containsLauncher && !pkg.equals(myPackage) && app.isForeground()) {
            // 自身页面校准，避免出现栈之间切换时，偶尔出现桌面在栈顶的情况
            Activity top = app.getTopActivity();
            if (top != null) {
                pkg = myPackage;
                activity = getShortClassName(top.getClass(), myPackage);
            }
        }

        if (DBG) {
            LeoLog.d("handleAppLaunch", pkg + "/" + activity);
        }

        // fix bug AM-2134
        if (TextUtils.equals(myPackage, pkg) && activity != null && containsLauncher) {
            return;
        }

        AppMasterPreference amp = AppMasterPreference.getInstance(mContext);
        boolean unlocked = amp.getUnlocked();
        String checkPkg = amp.getDoubleCheck();
        boolean doubleCheck = checkPkg != null && checkPkg.equals(pkg);
        boolean isCurrentSelf = pkg.equals(myPackage);
        boolean isLastSelf = mLastRunningPkg.equals(myPackage);
        boolean selfUnlock = isCurrentSelf && isLastSelf && !unlocked
                && !LockScreenActivity.sLockFilterFlag;
        boolean packageCheck = !pkg.equals(mLastRunningPkg) || selfUnlock;
        if (((doubleCheck && !unlocked) || packageCheck)
                && !TextUtils.isEmpty(mLastRunningPkg)) {
            if (isCurrentSelf && !isLastSelf) {
                amp.setFromOther(true);
            }
            boolean currentLockScreen = activity.contains(LOCKSCREENNAME);
            if (doubleCheck) {
                if (mLastRunningPkg.isEmpty()
                        || (isCurrentSelf && (activity.contains(DESKPROXYNAME)
                        || (activity.contains(BATTERYVIEW) && mIgnoreBattery)
                        || activity.contains(GAMEBOX)
                        || activity.contains(DESKAD)
                        // || activity.contains(APPWALL)
                        || activity.contains(LAUNCHERBOOST)
                        || activity.contains(SPLASHNAME)
                        || activity.contains(PROXYNAME)
                        || activity.contains(WAITNAME)
                        || activity.contains(UPDATE)
                        || activity.contains(WEBVIEW)
                        || activity.contains(BLACK_LIST)
                        // 如果锁屏前的pkg是联想的屏保，则不过滤掉webviewActivity
                        && !pkg.equals(mDetectedPkgBeforeScreeOff))
                        || (!unlocked && currentLockScreen))
                        || (unlocked && isLastSelf && mLastRuningActivity.contains(LOCKSCREENNAME)))
                {
                    mLastRunningPkg = pkg;
                    mLastRuningActivity = activity;
                    return;
                }
            } else {
                if (mLastRunningPkg.isEmpty()
                        || (isCurrentSelf
                        && (activity.contains(DESKPROXYNAME)
                        || (activity.contains(BATTERYVIEW) && mIgnoreBattery)
                        || activity.contains(GAMEBOX)
                        || activity.contains(DESKAD)
                        // || activity.contains(APPWALL)
                        || activity.contains(LAUNCHERBOOST)
                        || activity.contains(SPLASHNAME)
                        || activity.contains(PROXYNAME)
                        || activity.contains(UPDATE)
                        || activity.contains(WAITNAME)
                        || activity.contains(BLACK_LIST)
                        || (activity.contains(WEBVIEW)
                        // 如果锁屏前的pkg是联想的屏保，则不过滤掉webviewActivity
                        && !pkg.equals(mDetectedPkgBeforeScreeOff)))
                        || currentLockScreen)
                        || (unlocked && isLastSelf && mLastRuningActivity.contains(LOCKSCREENNAME))
                        // 排出iswipe
                        || pkg.equals(Constants.ISWIPE_PACKAGE)) {
                    mLastRunningPkg = pkg;
                    mLastRuningActivity = activity;
                    return;
                }
            }

            // reset this filter flag
            if (LockScreenActivity.sLockFilterFlag) {
                LockScreenActivity.sLockFilterFlag = false;
            }

            // No need to lock activities in lock screen's task
            if (!currentLockScreen
                    && (baseActivity != null && baseActivity.contains(LOCKSCREENNAME))) {
                mLastRunningPkg = pkg;
                mLastRuningActivity = activity;
                return;
            }

            mLastRunningPkg = pkg;
            mLastRuningActivity = activity;

            LockManager lockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
            AppLoadEngine.getInstance(mContext).recordAppLaunchTime(mLastRunningPkg,
                    System.currentTimeMillis());

            List<String> lockList = lockManager.getCurLockList();
            boolean lock = false;
            if (lockList != null) {
                lock = lockList.contains(pkg);
                // Google launcher is special
                if (!lock && pkg.equals(GOOGLE_LAUNCHER_PKG21)) {
                    lock = lockList.contains(GOOGLE_LAUNCHER_PKG);
                }
            }
            if (lock) {
                LeoLog.d("Track Lock Screen",
                        "apply lockscreen form TaskChangeHandler");
                if (lockManager.applyLock(LockManager.LOCK_MODE_FULL, pkg, false, null)) {
                    amp.setUnlocked(false);
                }
            }
        } else {
            mLastRuningActivity = activity;
        }
    }

    private String getShortClassName(Class clazz, String pkgName) {
        String className = clazz.getName();
        if (clazz.getName().startsWith(pkgName)) {
            int PN = pkgName.length();
            int CN = className.length();
            if (CN > PN && className.charAt(PN) == '.') {
                return className.substring(PN, CN);
            }
        }
        return className;
    }
}
