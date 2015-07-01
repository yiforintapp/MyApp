
package com.leo.appmaster.applocker.manager;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.text.TextUtils;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.quickgestures.FloatWindowHelper;
import com.leo.appmaster.quickgestures.QuickGestureManager;
import com.leo.appmaster.utils.LeoLog;

public class TaskChangeHandler {

    public static final String EXTRA_LOCKED_APP_PKG = "locked_app_pkg";

    public static final String LOCKSCREENNAME = "LockScreenActivity";
    public static final String HOMENAME = "HomeActivity";
    public static final String SPLASHNAME = "SplashActivity";
    public static final String PROXYNAME = "ProxyActivity";
    public static final String WAITNAME = "WaitActivity";
    public static final String GESTURE = "QuickGesturePopupActivity";
    // public static final String GESTURESETTING = "QuickGestureActivity";

    private static final String DOWNLAOD_PKG = "com.android.providers.downloads.ui";
    private static final String DOWNLAOD_PKG_21 = "com.android.documentsui";

    private static final String GOOGLE_LAUNCHER_PKG = "com.google.android.launcher";
    private static final String GOOGLE_LAUNCHER_PKG21 = "com.google.android.googlequicksearchbox";

    private Context mContext;
    private ActivityManager mAm;
    private String mLastRunningPkg = "";
    private String mLastRuningActivity = "";

    private boolean mIsFirstDetect;

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

    public void handleAppLaunch(String pkg, String activity) {
        if (pkg == null || activity == null)
            return;
        if (mIsFirstDetect) {
            LeoLog.d("Track Lock Screen", "is first lock,so we ignor this time");
            mLastRunningPkg = pkg;
            mLastRuningActivity = activity;
            mIsFirstDetect = false;
            return;
        }

        // for gesture check
        if (activity.contains(GESTURE)) {
            /* 解决：关闭GESTURE这个Acitivty后启动创建热区任务时闪动创建问题 */
            // FloatWindowHelper.mGestureShowing = true;
        } else {
            FloatWindowHelper.mGestureShowing = false;
        }

        String myPackage = mContext.getPackageName();
        AppMasterPreference amp = AppMasterPreference.getInstance(mContext);
        boolean unlocked = amp.getUnlocked();
        String checkPkg = amp.getDoubleCheck();
        boolean doubleCheck = checkPkg != null && checkPkg.equals(pkg);
        if (((doubleCheck && !unlocked) || !pkg.equals(mLastRunningPkg))
                && !TextUtils.isEmpty(mLastRunningPkg)) {
            boolean isCurrentSelf = pkg.equals(myPackage);
            boolean isLastSelf = mLastRunningPkg.equals(myPackage);
            if (isCurrentSelf && !isLastSelf) {
                amp.setFromOther(true);
            }
            if (doubleCheck) {
                if (mLastRunningPkg.isEmpty()
                        || (isCurrentSelf
                                && (activity
                                        .contains(SPLASHNAME) || activity
                                        .contains(GESTURE)/*
                                                           * ||
                                                           * activity.contains
                                                           * (GESTURESETTING)
                                                           */|| activity.contains(PROXYNAME) || activity
                                            .contains(WAITNAME))
                                || (!unlocked && activity.contains(LOCKSCREENNAME)))
                        || (unlocked && isLastSelf && mLastRuningActivity
                                .contains(LOCKSCREENNAME))) {
                    mLastRunningPkg = pkg;
                    mLastRuningActivity = activity;
                    return;
                }
            } else {
                if (mLastRunningPkg.isEmpty()
                        || (isCurrentSelf
                                && (activity
                                        .contains(SPLASHNAME) || activity
                                        .contains(GESTURE)/*
                                                           * ||
                                                           * activity.contains
                                                           * (GESTURESETTING)
                                                           */|| activity.contains(PROXYNAME) || activity
                                            .contains(WAITNAME)) || (activity
                                    .contains(LOCKSCREENNAME)))
                        || (unlocked && isLastSelf && mLastRuningActivity
                                .contains(LOCKSCREENNAME))) {
                    mLastRunningPkg = pkg;
                    mLastRuningActivity = activity;
                    return;
                }
            }
            mLastRunningPkg = pkg;
            mLastRuningActivity = activity;

            // remocde app launch recoder
            LockManager.getInstatnce().recordAppLaunch(mLastRunningPkg);
            AppLoadEngine.getInstance(mContext).recordAppLaunchTime(mLastRunningPkg,
                    System.currentTimeMillis());

            // For android 5.0, download package changed
            if (pkg.equals(DOWNLAOD_PKG_21)) {
                pkg = DOWNLAOD_PKG;
            }
            List<String> lockList = LockManager.getInstatnce().getCurLockList();
            boolean lock = false;
            if (lockList != null) {
                lock = lockList.contains(pkg);
                // Google launcher is special
                if (!lock && pkg.equals(GOOGLE_LAUNCHER_PKG21)) {
                    lock = lockList.contains(GOOGLE_LAUNCHER_PKG);
                }
            }
            if (lock) {
                LeoLog.d("Track Lock Screen", "apply lockscreen form TaskChangeHandler");
                if (LockManager.getInstatnce().applyLock(LockManager.LOCK_MODE_FULL, pkg, false,
                        null)) {
                    amp.setUnlocked(false);
                }
            }
        } else {
            mLastRuningActivity = activity;
        }
    }
}
