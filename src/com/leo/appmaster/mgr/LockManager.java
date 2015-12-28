package com.leo.appmaster.mgr;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.applocker.model.LocationLock;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.applocker.model.TimeLock;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.utils.LeoLog;

/**
 * 应用锁
 * Created by Jasper on 2015/9/28.
 */
public abstract class LockManager extends Manager {
    // 每新增一个应用扣除的分数

    public static int MAX_SCORE = 40;
    static {
        IntrudeSecurityManager ism = (IntrudeSecurityManager) MgrContext.getManager(MgrContext.MGR_INTRUDE_SECURITY);
        if (ism.getIsIntruderSecurityAvailable()) {
            MAX_SCORE = 40;
        } else {
            MAX_SCORE = 40 + ism.getMaxScore();
        }
    }

    public static final int SPA = 4;

    public static final int LOCK_MODE_FULL = 1;
    public static final int LOCK_MODE_PURE = 2;

    public static final String EXTRA_LOCKED_APP_PKG = "locked_app_pkg";

    public static final int NETWORK_NULL = -1;
    public static final int NETWORK_MOBILE = 0;
    public static final int NETWORK_WIFI = 1;

    public static interface OnUnlockedListener {
        /**
         * called when unlock successfully
         */
        public void onUnlocked();

        /**
         * called when unlock canceled
         */
        public void onUnlockCanceled();

        /**
         * called when unlock canceled
         */
        public void onUnlockOutcount();
    }

    @Override
    public String description() {
        return MgrContext.MGR_APPLOCKER;
    }

    public abstract void init();
    /**
     * 添加到对应加锁模式
     * @param pkgs
     * @param mode
     */
    public abstract void addPkg2Mode(List<String> pkgs, LockMode mode);

    /**
     * 从加锁模式中移除
     * @param pkgs
     * @param mode
     * @param notifyChange 是否通知变更
     */
    public abstract void removePkgFromMode(List<String> pkgs, LockMode mode, boolean notifyChange);

    /**
     * 更新LockMode
     * @param mode
     */
    public abstract void updateMode(LockMode mode);

    /**
     * 添加LockMode
     * @param lockMode
     */
    public abstract void addLockMode(LockMode lockMode);

    /**
     * 移除LockMode
     * @param lockMode
     */
    public abstract void removeLockMode(LockMode lockMode);

    /**
     * 添加时间锁
     * @param lock
     */
    public abstract void addTimeLock(TimeLock lock);

    /**
     * 移除时间锁
     * @param timeLock
     */
    public abstract void removeTimeLock(TimeLock timeLock);

    public abstract void openTimeLock(TimeLock timeLock, boolean open);

    /**
     * 更新时间锁
     * @param lock
     */
    public abstract void updateTimeLock(TimeLock lock);

    /**
     * 添加位置锁
     * @param lock
     */
    public abstract void addLocationLock(LocationLock lock);

    /**
     * 移除位置锁
     * @param lock
     */
    public abstract void removeLocationLock(LocationLock lock);
    public abstract void openLocationLock(LocationLock lock, boolean open);
    public abstract void updateLocationLock(LocationLock lock);

    public abstract LockMode getCurLockMode();
    public abstract String getCurLockName();
    public abstract List<String> getCurLockList();

    public abstract boolean isPackageLocked(String packageName);

    public abstract void setCurrentLockMode(LockMode mode, boolean fromUser);

    public abstract List<LockMode> getLockMode();
    public abstract List<TimeLock> getTimeLock();
    public abstract List<LocationLock> getLocationLock();

    public abstract void filterPackage(String pkg, boolean persistent);
    public abstract void filterPackage(String packageName, long time);
    public abstract void filterAll(long outtime);
    public abstract void filterSelfOneMinites();
    public abstract void clearFilterList();

    public abstract void startLockService();
    public abstract void stopLockService();

    public abstract boolean applyLock(int lockMode, String lockedPkg, boolean restart, OnUnlockedListener listener);

    public abstract boolean inRelockTime(String pkg);
    public abstract int getLockedAppCount();
    public abstract int getAllAppCount();

    public abstract String getLastActivity();
    public abstract String getLastPackage();

    public abstract void recordOutcountTask(String pkg);
    public abstract int getOutcountTime(String pkg);

    public abstract void setPauseScreenonLock(boolean value);

    public abstract boolean inFilterList(String pkg);

    public abstract List<AppItemInfo> getNewAppList();
    public abstract int getSecurityScore(List<AppItemInfo> list);

    /**
     * 加锁应用，处理隐私等级时调用
     * @param pkgList
     * @return 新增得分
     */
    public abstract int lockAddedApp(List<String> pkgList);

    /**
     * 根据新增应用个数，获取处理后的得分
     * @param appNum
     * @return
     */
    public abstract int getIncreaseScore(int appNum);

    private static final String ACTION_FIRST_USE_LOCK_MODE = "com.leo.appmaster.ACTION_FIRST_USE_LOCK_MODE";
    private static final String ACTION_LOCK_MODE_CHANGE = "com.leo.appmaster.ACTION_LOCK_MODE_CHANGE";
    private static final String SEND_RECEIVER_TO_SWIPE_PERMISSION = "com.leo.appmaster.RECEIVER_TO_ISWIPE";

    public static void sendFirstUseLockModeToISwipe() {
        Intent intent = new Intent(ACTION_FIRST_USE_LOCK_MODE);

        Context ctx = AppMasterApplication.getInstance();
        AppMasterPreference pref = AppMasterPreference.getInstance(ctx);
        if (pref.getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
            LockManager manager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);

            List<LockMode> list = manager.getLockMode();
            ArrayList<LockMode> arrayList = new ArrayList<LockMode>();
            arrayList.addAll(list);
            intent.putParcelableArrayListExtra("lock_mode_list", arrayList);
        }
        try {
            ctx.sendBroadcast(intent, SEND_RECEIVER_TO_SWIPE_PERMISSION);
            LeoLog.e("LockManager", "send first use lock mode .");
        } catch (Exception e) {
            LeoLog.e("LockManager", "send first use lock mode failed.", e);
        }
    }

    public static void sendLockModeChangeToISwipe() {
        Intent intent = new Intent(ACTION_LOCK_MODE_CHANGE);

        Context ctx = AppMasterApplication.getInstance();
        AppMasterPreference pref = AppMasterPreference.getInstance(ctx);
        if (pref.getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
            LockManager manager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);

            List<LockMode> list = manager.getLockMode();
            ArrayList<LockMode> arrayList = new ArrayList<LockMode>();
            arrayList.addAll(list);
            intent.putParcelableArrayListExtra("lock_mode_list", arrayList);
        }
        try {
            ctx.sendBroadcast(intent, SEND_RECEIVER_TO_SWIPE_PERMISSION);
            LeoLog.e("LockManager", "send lock mode changed .");
        } catch (Exception e) {
            LeoLog.e("LockManager", "send lock mode changed failed.", e);
        }
    }
}
