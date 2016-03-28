package com.leo.appmaster.mgr.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.PhoneInfo;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.applocker.WaitActivity;
import com.leo.appmaster.applocker.lockswitch.SwitchGroup;
import com.leo.appmaster.applocker.manager.ILockPolicy;
import com.leo.appmaster.applocker.manager.LockModeDao;
import com.leo.appmaster.applocker.manager.TimeoutRelockPolicy;
import com.leo.appmaster.applocker.model.LocationLock;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.applocker.model.ProcessDetector;
import com.leo.appmaster.applocker.model.TimeLock;
import com.leo.appmaster.applocker.service.TaskDetectService;
import com.leo.appmaster.battery.BatteryShowViewActivity;
import com.leo.appmaster.bootstrap.CheckNewBootstrap;
import com.leo.appmaster.db.InstalledAppTable;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.AppLockChangeEvent;
import com.leo.appmaster.eventbus.event.AppUnlockEvent;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.eventbus.event.LocationLockEvent;
import com.leo.appmaster.eventbus.event.LockModeEvent;
import com.leo.appmaster.eventbus.event.TimeLockEvent;
import com.leo.appmaster.home.ProxyActivity;
import com.leo.appmaster.intruderprotection.IntruderCatchedActivity;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NetWorkUtil;
import com.leo.appmater.globalbroadcast.LeoGlobalBroadcast;
import com.leo.appmater.globalbroadcast.NetworkStateListener;
import com.leo.appmater.globalbroadcast.ScreenOnOffListener;

public class LockManagerImpl extends LockManager {
    private static final String TAG = "LockManager";

    public static final String SAMSUNG = "samsung";
    public static final String SAMSUNG_SETTINGS = "com.android.settings";

    private static final int NO_CACHE = -9999;

    public int mCurNetType = NETWORK_MOBILE;
    public String mCurWifi = "";

    public int mLastNetType = NETWORK_MOBILE;
    public String mLastWifi = "";

    private List<LockMode> mLockModeList;
    private List<TimeLock> mTimeLockList;
    private List<LocationLock> mLocationLockList;
    private LockMode mCurrentMode;
    private boolean mLockModeLoaded;

    private HashMap<TimeLock, List<ScheduledFuture<?>>> mTLMap;

    private ExecutorService mTaskExecutor = Executors.newSingleThreadExecutor();
    /*
     * for time lock, and other time task
     */
    private ScheduledExecutorService mScheduler = ThreadManager.getAsyncExecutor();

    private Future<Boolean> mLoadDefaultDataFuture;

    private HashMap<String, Boolean> mFilterPgks;
    private boolean mPauseScreenonLock;

    private boolean mFilterAll;
    private TimerTask mFillterAllTask;

    private ILockPolicy mLockPolicy;
    /*
     * just call one time for every listener
     */
    private OnUnlockedListener mExtranalUnlockListener;
    /*
     * record ortcount unlock
     */
    private HashMap<String, Integer> mOutcountPkgMap;
    private HashMap<Runnable, ScheduledFuture<?>> mOutcountTaskMap;

    private Handler mHandler;

    /*
     * listener mimute change
     */
    private TimeChangeReceive mTimeChangeReceiver;
    private ScreenOnOffListener mScreenListener;

    // 新增应用列表
    private List<AppItemInfo> mNewList;

    private int mCachedScore = NO_CACHE;

    public LockManagerImpl() {
        mContext = AppMasterApplication.getInstance();
        mLockPolicy = new TimeoutRelockPolicy(mContext);
        mFilterPgks = new HashMap<String, Boolean>();
        mOutcountPkgMap = new HashMap<String, Integer>();
        mOutcountTaskMap = new HashMap<Runnable, ScheduledFuture<?>>();
        mLockModeList = new ArrayList<LockMode>();
        mTimeLockList = new ArrayList<TimeLock>();
        mLocationLockList = new ArrayList<LocationLock>();
        mTLMap = new HashMap<TimeLock, List<ScheduledFuture<?>>>();
        mHandler = new Handler(Looper.getMainLooper());
        mTimeChangeReceiver = new TimeChangeReceive();
        mFilterPgks.put("WaitActivity", true);

        mNewList = new ArrayList<AppItemInfo>();
    }

    public void init() {
        LeoLog.d(TAG, "init");
        startLockService();
        mScreenListener = new ScreenOnOffListener() {
            @Override
            public void onScreenChanged(Intent intent) {
                handleScreenChange(intent);
                super.onScreenChanged(intent);
            }
        };
        LeoGlobalBroadcast.registerBroadcastListener(mScreenListener);
        LeoEventBus.getDefaultBus().register(this);

        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        mContext.registerReceiver(mTimeChangeReceiver, filter);

        // to load lock mode, time and location lock
        long start = SystemClock.elapsedRealtime();
        tryLoadLockMode();
        long end = SystemClock.elapsedRealtime();
        LeoLog.i(TAG, "cost, tryLoadLockMode: " + (end - start));
        // init location lock
        mTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                initLocationLock();
            }
        });
        // init Time lock
        mTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                initTimeLock();
            }
        });

        AppMasterPreference pref = AppMasterPreference.getInstance(mContext);
        if (pref.getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
            sendFirstUseLockModeToISwipe();
        }

        final Handler handler = new Handler(Looper.myLooper());
        start = SystemClock.elapsedRealtime();
        mContext.getContentResolver().registerContentObserver(Constants.LOCK_MODE_URI, true,
                new ContentObserver(handler) {
                    @Override
                    public void onChange(boolean selfChange) {
                        sendLockModeChangeToISwipe();
                    }
                });
        end = SystemClock.elapsedRealtime();
        LeoLog.i(TAG, "cost, registerContentObserver: " + (end - start));

        AppLoadEngine.getInstance(mContext).registerAppChangeListener(new AppLoadEngine.AppChangeListener() {
            @Override
            public void onAppChanged(ArrayList<AppItemInfo> changes, int type) {
                if (type == AppLoadEngine.AppChangeListener.TYPE_REMOVE ||
                        type == AppLoadEngine.AppChangeListener.TYPE_UNAVAILABLE) {
                    InstalledAppTable.getInstance().removePackageList(changes);
                    List<String> pkgList = new ArrayList<String>();
                    for (AppItemInfo itemInfo : changes) {
                        if (itemInfo == null || TextUtils.isEmpty(itemInfo.packageName)) {
                            continue;
                        }
                        pkgList.add(itemInfo.packageName);
                    }
                    removePkgFromMode(pkgList, getCurLockMode(), false);
                }
            }
        });
    }

    private void tryLoadLockMode() {
        if (mLockModeLoaded) return;

        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                loadLockMode();
            }
        });
    }

    private void initLocationLock() {
        LeoLog.d("onNetworkStateChange", "initLocationLock");
        if (NetWorkUtil.isNetworkAvailable(mContext)) {
            if (NetWorkUtil.isWifiConnected(mContext)) {
                mCurNetType = mLastNetType = NETWORK_WIFI;
                mCurWifi = mLastWifi = NetWorkUtil.getCurWifiName(mContext);
                LeoLog.d("onNetworkStateChange", "mCurWifi = " + mCurWifi);
                LeoLog.d("onNetworkStateChange", "0");
                LeoLog.d("onNetworkStateChange", "mLocationLockList size = " + mLocationLockList.size());
                for (LocationLock lock : mLocationLockList) {
                    if (lock.using && TextUtils.equals(mCurWifi, lock.ssid)) {
                        LeoLog.d("onNetworkStateChange", "1");
                        for (LockMode lockMode : mLockModeList) {
                            if (lockMode.modeId == lock.entranceModeId) {
                                LeoLog.d("onNetworkStateChange", "hit location: " + lock.name);
                                LeoLog.d("onNetworkStateChange", "2");
                                setCurrentLockMode(lockMode, false);
                                break;
                            }
                        }
                    }
                }
            } else {
                mCurNetType = mLastNetType = NETWORK_MOBILE;
                mCurWifi = mLastWifi = "";
            }
        } else {
            mCurNetType = mLastNetType = NETWORK_NULL;
            mCurWifi = mLastWifi = "";
        }

        // active location lock
        LeoGlobalBroadcast.registerBroadcastListener(new NetworkStateListener() {
            @Override
            public void onNetworkStateChange(Intent intent) {
                LeoLog.d("onNetworkStateChange", "onNetworkStateChange");

                if (NetWorkUtil.isNetworkAvailable(mContext)) {
                    if (NetWorkUtil.isWifiConnected(mContext)) {
                        mCurNetType = NETWORK_WIFI;
                        mCurWifi = NetWorkUtil.getCurWifiName(mContext);
                    } else {
                        mCurNetType = NETWORK_MOBILE;
                        mCurWifi = "";
                    }
                } else {
                    mCurNetType = NETWORK_NULL;
                    mCurWifi = "";
                }

                LeoLog.d("onNetworkStateChange", "mCurWifi: " + mCurWifi);
                LeoLog.d("onNetworkStateChange", "mLastNetType --> " + mLastNetType);
                LeoLog.d("onNetworkStateChange", "mCurNetType --> " + mCurNetType);
                if (mLastNetType != mCurNetType) {
                    LeoLog.d("onNetworkStateChange", "mLastNetType != mCurNetType");
                    if (mLastNetType == NETWORK_WIFI) {
                        LeoLog.d("onNetworkStateChange", "mLastNetType == NETWORK_WIFI");
                        // change wifi to no-wifi
                        /*
                         * change wifi to no-wifi, select the last match
                         * location lock
                         */
                        LocationLock selseLock = null;
                        for (LocationLock lock : mLocationLockList) {
                            if (lock.using && TextUtils.equals(mLastWifi, lock.ssid)) {
                                if (selseLock == null) {
                                    selseLock = lock;
                                } else {
                                    if (lock.id > selseLock.id) {
                                        selseLock = lock;
                                    }
                                }
                            }
                        }
                        if (selseLock != null) {
                            for (LockMode lockMode : mLockModeList) {
                                if (lockMode.modeId == selseLock.quitModeId) {
                                    LeoLog.d("onNetworkStateChange", "hit location: "
                                            + selseLock.name + "   "
                                            + selseLock.quitModeName);
                                    setCurrentLockMode(lockMode, false);
                                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "modeschage",
                                            "local");
                                    break;
                                }
                            }
                        }
                    } else {
                        if (mCurNetType == NETWORK_WIFI) {
                            LeoLog.d("onNetworkStateChange", "mCurNetType == NETWORK_WIFI");
                            // change no-wifi to wifi
                            /*
                             * change no-wifi to wifi, select the last match
                             * location lock
                             */
                            LocationLock selseLock = null;
                            for (LocationLock lock : mLocationLockList) {
                                if (lock.using && TextUtils.equals(mCurWifi, lock.ssid)) {
                                    if (selseLock == null) {
                                        selseLock = lock;
                                    } else {
                                        if (lock.id > selseLock.id) {
                                            selseLock = lock;
                                        }
                                    }
                                }
                            }
                            if (selseLock != null) {
                                for (LockMode lockMode : mLockModeList) {
                                    if (lockMode.modeId == selseLock.entranceModeId) {
                                        LeoLog.d("onNetworkStateChange", "hit location: "
                                                + selseLock.name + "   "
                                                + selseLock.entranceModeName);
                                        setCurrentLockMode(lockMode, false);
                                        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "modeschage",
                                                "local");
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (mCurNetType == NETWORK_WIFI) {
                        LeoLog.d("onNetworkStateChange", "mCurNetType == NETWORK_WIFI");
                        // change wifi to another wifi
                        /*
                         * change wifi to another wifi, select the last match
                         * location lock
                         */
                        LocationLock selseLock = null;
                        for (LocationLock lock : mLocationLockList) {
                            LeoLog.d("onNetworkStateChange", "lock.name is --> " + lock.name);
                            LeoLog.d("onNetworkStateChange", " lock.ssid is --> " + lock.ssid);
                            LeoLog.d("onNetworkStateChange", "mCurWifi is --> " + mCurWifi);

                            if (lock.using) {
                                if (TextUtils.equals(mCurWifi, lock.ssid)) {
                                    if (selseLock == null) {
                                        selseLock = lock;
                                    } else {
                                        if (lock.id > selseLock.id) {
                                            selseLock = lock;
                                        }
                                    }
                                    if (selseLock != null) {
                                        LeoLog.d("onNetworkStateChange", "selseLock != null");
                                        for (LockMode lockMode : mLockModeList) {
                                            if (lockMode.modeId == selseLock.entranceModeId) {
                                                LeoLog.d("onNetworkStateChange", "hit location: "
                                                        + selseLock.name + "   "
                                                        + selseLock.entranceModeName);
                                                setCurrentLockMode(lockMode, false);
                                                SDKWrapper.addEvent(mContext, SDKWrapper.P1,
                                                        "modeschage",
                                                        "local");
                                                break;
                                            }
                                        }
                                    }
                                } else if (TextUtils.equals(mLastWifi, lock.ssid)) {
                                    // AM-1724 wifi-to-wifi but not same wifi of
                                    // lockModeName .
                                    if (selseLock == null) {
                                        selseLock = lock;
                                    } else {
                                        if (lock.id > selseLock.id) {
                                            selseLock = lock;
                                        }
                                    }
                                    if (selseLock != null) {
                                        for (LockMode lockMode : mLockModeList) {
                                            if (lockMode.modeId == selseLock.quitModeId) {
                                                LeoLog.d("onNetworkStateChange", "hit location: "
                                                        + selseLock.name + "   "
                                                        + selseLock.quitModeName);
                                                setCurrentLockMode(lockMode, false);
                                                SDKWrapper.addEvent(mContext, SDKWrapper.P1,
                                                        "modeschage",
                                                        "local");
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                mLastNetType = mCurNetType;
                mLastWifi = mCurWifi;

                LeoEventBus.getDefaultBus().post(
                        new LockModeEvent(EventId.EVENT_MODE_CHANGE, "location change"));

            }
        });
    }

    private void initTimeLock() {
        Calendar calendar = Calendar.getInstance();
        int curDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (curDayOfWeek == 1) {
            curDayOfWeek = 7;
        } else {
            curDayOfWeek -= 1;
        }
        int curHour = calendar.get(Calendar.HOUR_OF_DAY);
        int curMinute = calendar.get(Calendar.MINUTE);
        int curSecond = calendar.get(Calendar.SECOND);
        long curTime = curHour * 60 * 60 + curMinute * 60 + curSecond;
        long dayTime = 24 * 60 * 60;
        long weekTime = 7 * dayTime;
        long triggerSecond = 0;

        List<ScheduledFuture<?>> operations = null;
        for (TimeLock timeLock : mTimeLockList) {
            if (timeLock.using) {
                /* start this time lock */
                TimeLock.TimePoint time = timeLock.time;
                long trigleTime = time.hour * 60 * 60 + time.minute * 60;
                TimeLock.RepeatTime repeat = timeLock.repeatMode;
                byte[] repeatDay = repeat.getAllRepeatDayOfWeek();

                // add this operation to map
                operations = new ArrayList<ScheduledFuture<?>>();
                mTLMap.put(timeLock, operations);

                if (!TextUtils.isEmpty(timeLock.repeatMode.toString())) {
                    // scan repeat day
                    for (byte b : repeatDay) {
                        if (b > curDayOfWeek) {
                            triggerSecond = ((b - 1) * dayTime + trigleTime)
                                    - ((curDayOfWeek - 1) * dayTime + curTime);

                            LeoLog.d("time lock", "add alarm set, " + timeLock.name + " :  "
                                    + timeLock.time.toString() + "    triggerSecond = "
                                    + triggerSecond);
                            // set alarm
                            TimeLockOperation operation = new TimeLockOperation(timeLock.id,
                                    timeLock.lockModeId);
                            ScheduledFuture<?> fature = mScheduler.scheduleWithFixedDelay(
                                    operation,
                                    triggerSecond, weekTime, TimeUnit.SECONDS);
                            operations.add(fature);
                        } else if (b == curDayOfWeek) {
                            if (curTime > trigleTime) {
                                triggerSecond = weekTime - (curTime - trigleTime);
                            } else {
                                triggerSecond = trigleTime - curTime;
                            }
                            // set alarm
                            LeoLog.d("time lock", "add alarm set, " + timeLock.name + " :  "
                                    + timeLock.time.toString() + "    triggerSecond = "
                                    + triggerSecond);

                            TimeLockOperation operation = new TimeLockOperation(timeLock.id,
                                    timeLock.lockModeId);
                            ScheduledFuture<?> fature = mScheduler.scheduleWithFixedDelay(
                                    operation,
                                    triggerSecond, weekTime, TimeUnit.SECONDS);
                            operations.add(fature);
                        } else {
                            triggerSecond = 7 * dayTime - (((curDayOfWeek - 1) * dayTime + curTime)
                                    - ((b - 1) * dayTime + trigleTime));
                            // set alarm
                            LeoLog.d("time lock", "add alarm set, " + timeLock.name + " :  "
                                    + timeLock.time.toString() + "    triggerSecond = "
                                    + triggerSecond);
                            TimeLockOperation operation = new TimeLockOperation(timeLock.id,
                                    timeLock.lockModeId);
                            ScheduledFuture<?> fature = mScheduler.scheduleWithFixedDelay(
                                    operation,
                                    triggerSecond, weekTime, TimeUnit.SECONDS);
                            operations.add(fature);
                        }
                    }
                } else {
                    if (curTime <= trigleTime) {
                        triggerSecond = trigleTime - curTime;
                    } else {
                        triggerSecond = dayTime - curTime + triggerSecond;
                    }
                    LeoLog.d(
                            "time lock",
                            "add alarm set, " + timeLock.name + " :  "
                                    + timeLock.time.toString() + "   triggerSecond = "
                                    + triggerSecond);
                    // set alarm
                    TimeLockOperation operation = new TimeLockOperation(timeLock.id,
                            timeLock.lockModeId);
                    ScheduledFuture<?> fature = mScheduler.schedule(operation,
                            triggerSecond, TimeUnit.SECONDS);
                    operations.add(fature);
                }

            }
        }
    }

    public void onEvent(AppUnlockEvent event) {
        LeoLog.d(TAG, "onEvent, result: " + event.mUnlockResult);
        if (event.mUnlockResult == AppUnlockEvent.RESULT_UNLOCK_SUCCESSFULLY) {
            mLockPolicy.onUnlocked(event.mUnlockedPkg);
            if (mExtranalUnlockListener != null) {
                mExtranalUnlockListener.onUnlocked();
                mExtranalUnlockListener = null;
            }
        } else if (event.mUnlockResult == AppUnlockEvent.RESULT_UNLOCK_CANCELED) {
            if (mExtranalUnlockListener != null) {
                mExtranalUnlockListener.onUnlockCanceled();
                mExtranalUnlockListener = null;
            }
        } else if (event.mUnlockResult == AppUnlockEvent.RESULT_UNLOCK_OUTCOUNT) {
            if (mExtranalUnlockListener != null) {
                mExtranalUnlockListener.onUnlockOutcount();
            }
        }
    }

    @Override
    public void onDestory() {

    }

    @Override
    public void clearFilterList() {
        mFilterPgks.clear();
    }

    @Override
    public void setPauseScreenonLock(boolean value) {
        mPauseScreenonLock = value;
    }

    @Override
    public boolean inFilterList(String pkg) {
        if (pkg == null) return false;

        AppMasterApplication ctx = AppMasterApplication.getInstance();
        ProcessDetector detector = new ProcessDetector();
        if (pkg.equals(ctx.getPackageName())
                || pkg.equals(Constants.CP_PACKAGE)
                || pkg.equals(Constants.ISWIPE_PACKAGE)
                || pkg.equals(Constants.PL_PKG_NAME)
                || pkg.equals(Constants.SEARCH_BOX_PACKAGE)
                || detector.isHomePackage(pkg)) {
            return true;
        }
//        appDetailInfo.packageName.equals(this.getPackageName())
//                || appDetailInfo.packageName.equals(Constants.CP_PACKAGE)
//                || appDetailInfo.packageName.equals(Constants.ISWIPE_PACKAGE)
//                || appDetailInfo.packageName.equals(Constants.SEARCH_BOX_PACKAGE)
//                || detector.isHomePackage(appDetailInfo.packageName)
        return false;
    }

    @Override
    public void addPkg2Mode(List<String> pkgs, LockMode mode) {
        List<String> ignoreList = InstalledAppTable.getInstance().getIgnoredList();
        final List<String> coreChgList = new ArrayList<String>();
        synchronized (this) {
            if (pkgs != null && mode != null && mode.lockList != null && mode.defaultFlag != 0) {
                for (String pkg : pkgs) {
                    if (!TextUtils.isEmpty(pkg) && !mode.lockList.contains(pkg)) {
                        mode.lockList.add(0, pkg);
                        if (!ignoreList.contains(pkg)) {
                            coreChgList.add(pkg);
                        }
                    } else {
                        continue;
                    }
                }
            } else {
                return;
            }

            // add self pkg
            if (!mode.lockList.contains(mContext.getPackageName())) {
                mode.lockList.add(mContext.getPackageName());
            }
        }

        if (mode.isCurrentUsed) {
//            PrivacyHelper.getInstance(mContext).computePrivacyLevel(PrivacyHelper.VARABLE_APP_LOCK);
            mCachedScore += SPA * coreChgList.size();
            ThreadManager.executeOnAsyncThread(new Runnable() {
                @Override
                public void run() {
                    notifySecurityChange();
                    InstalledAppTable.getInstance().insertIgnoreList(coreChgList);
                }
            });
        }

        updateMode(mode);
        LeoEventBus.getDefaultBus().post(new AppLockChangeEvent(AppLockChangeEvent.APP_ADD));

    }

    @Override
    public void removePkgFromMode(List<String> pkgs, LockMode mode, boolean notifyChange) {
        long a = System.currentTimeMillis();
        synchronized (this) {
            long c = System.currentTimeMillis();
            if (pkgs != null && mode != null && mode.lockList != null) {
                for (String pkg : pkgs) {
                    if (mode.lockList.contains(pkg)) {
                        mode.lockList.remove(pkg);
                    } else {
                        continue;
                    }
                }
            } else {
                return;
            }
            long d = System.currentTimeMillis();
            LeoLog.d("testWhoNull", "part inner : " + (d - c));
        }
        long b = System.currentTimeMillis();
        LeoLog.d("testWhoNull", "part 123 : " + (b - a));

        updateMode(mode);
        if (notifyChange && mode.isCurrentUsed) {
            ThreadManager.executeOnAsyncThread(new Runnable() {
                @Override
                public void run() {
                    notifySecurityChange();
                }
            });
        }
    }

    @Override
    public void updateMode(final LockMode mode) {
        if (mode == null || mode.lockList == null)
            return;

        // add self pkg
        if (!mode.lockList.contains(mContext.getPackageName())) {
            mode.lockList.add(mContext.getPackageName());
        }
        mTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                LockModeDao lmd = new LockModeDao(mContext);
                lmd.updateLockMode(mode);
            }
        });
    }

    @Override
    public void addLockMode(final LockMode lockMode) {
        mLockModeList.add(lockMode);
        if (lockMode.isCurrentUsed) {
            mCurrentMode = lockMode;
        }

        // add self pkg
        if (!lockMode.lockList.contains(mContext.getPackageName())) {
            lockMode.lockList.add(mContext.getPackageName());
        }

        // sync database
        mTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                LockModeDao lmd = new LockModeDao(mContext);
                lmd.insertLockMode(lockMode);
            }
        });
    }

    @Override
    public void removeLockMode(final LockMode lockMode) {
        boolean removed = mLockModeList.remove(lockMode);
        if (!removed) return;

        if (lockMode.isCurrentUsed) {
            setCurrentLockMode(mLockModeList.get(0), false);
        }

        // sync database
        mTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                LockModeDao lmd = new LockModeDao(mContext);
                lmd.deleteLockMode(lockMode);

                List<TimeLock> deleteTimeList = new ArrayList<TimeLock>();
                for (TimeLock timeLock : mTimeLockList) {
                    if (timeLock.lockModeId == lockMode.modeId) {
                        deleteTimeList.add(timeLock);
                    }
                }

                if (deleteTimeList.size() > 0) {
                    mTimeLockList.removeAll(deleteTimeList);
                    for (TimeLock timeLock2 : deleteTimeList) {
                        lmd.deleteTimeLock(timeLock2);
                    }
                    // notify TimeLockFragment
                    LeoEventBus.getDefaultBus().post(
                            new TimeLockEvent(EventId.EVENT_TIME_LOCK_CHANGE, "remove time lock"));
                }

                List<LocationLock> deleteLocationList = new ArrayList<LocationLock>();
                for (LocationLock locationLock : mLocationLockList) {
                    if (locationLock.entranceModeId == lockMode.modeId
                            || locationLock.quitModeId == lockMode.modeId) {
                        deleteLocationList.add(locationLock);
                    }
                }

                if (deleteLocationList.size() > 0) {
                    mLocationLockList.removeAll(deleteLocationList);
                    for (TimeLock timeLock2 : deleteTimeList) {
                        lmd.deleteTimeLock(timeLock2);
                    }
                    // notify LocationLockFragment
                    LeoEventBus.getDefaultBus().post(
                            new LocationLockEvent(EventId.EVENT_LOCATION_LOCK_CHANGE, "remove location lock"));
                }
            }
        });
    }

    @Override
    public void addTimeLock(final TimeLock lock) {
        mTimeLockList.add(lock);
        updateTimeLockOperation(lock);
        // sync database
        mTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                LockModeDao lmd = new LockModeDao(mContext);
                lmd.insertTimeLock(lock);
            }
        });
        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "time", "time");
    }

    @Override
    public void removeTimeLock(final TimeLock timeLock) {
        // cancel old task if have
        boolean removed = mTimeLockList.remove(timeLock);
        if (!removed) return;

        if (timeLock.using) {
            // remove this timelock
            List<ScheduledFuture<?>> list = mTLMap.get(timeLock);
            if (list != null) {
                for (ScheduledFuture<?> scheduledFuture : list) {
                    scheduledFuture.cancel(true);
                }
                mTLMap.remove(timeLock);
            }
        }

        // sync database
        mTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                LockModeDao lmd = new LockModeDao(mContext);
                lmd.deleteTimeLock(timeLock);
            }
        });
    }

    @Override
    public void openTimeLock(final TimeLock timeLock, boolean open) {
        if (open) {
            updateTimeLockOperation(timeLock);
            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "time", "open");
        } else {
            // cancel this timelock
            List<ScheduledFuture<?>> list = mTLMap.get(timeLock);
            if (list != null) {
                for (ScheduledFuture<?> scheduledFuture : list) {
                    scheduledFuture.cancel(true);
                }
                mTLMap.remove(timeLock);
            }
            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "time", "close");
        }
        // sync database
        mTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                LockModeDao lmd = new LockModeDao(mContext);
                lmd.updateTimeLock(timeLock);
            }
        });
    }

    @Override
    public void updateTimeLock(final TimeLock lock) {
        if (lock == null) return;

        for (TimeLock timeLock : mTimeLockList) {
            if (timeLock.id == lock.id) {
                timeLock.lockModeId = lock.lockModeId;
                timeLock.lockModeName = lock.lockModeName;
                timeLock.name = lock.name;
                timeLock.repeatMode = lock.repeatMode;
                timeLock.selected = lock.selected;
                timeLock.time = lock.time;
                timeLock.using = lock.using;
                updateTimeLockOperation(timeLock);
                break;
            }
        }

        // sync database
        mTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                LockModeDao lmd = new LockModeDao(mContext);
                lmd.updateTimeLock(lock);
            }
        });
    }

    @Override
    public void addLocationLock(final LocationLock lock) {
        mLocationLockList.add(lock);
        // sync database
        mTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                LockModeDao lmd = new LockModeDao(mContext);
                lmd.insertLocationLock(lock);
            }
        });
        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "local", "local");
    }

    @Override
    public void removeLocationLock(final LocationLock lock) {
        if (mLocationLockList.remove(lock)) {
            if (lock.using) {
                // TODO some business
                // do nothing
            }

            // sync database
            mTaskExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    LockModeDao lmd = new LockModeDao(mContext);
                    lmd.deleteLocationLock(lock);
                }
            });
        }
    }

    @Override
    public void openLocationLock(final LocationLock lock, boolean open) {
        // sync database
        mTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                LockModeDao lmd = new LockModeDao(mContext);
                lmd.updateLocationLock(lock);
            }
        });
        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "local", open ? "open" : "close");
    }

    @Override
    public void updateLocationLock(final LocationLock lock) {
        if (lock == null) return;

        for (LocationLock locationLock : mLocationLockList) {
            if (locationLock.id == lock.id) {
                locationLock.entranceModeId = lock.entranceModeId;
                locationLock.entranceModeName = lock.entranceModeName;
                locationLock.name = lock.name;
                locationLock.quitModeId = lock.quitModeId;
                locationLock.quitModeName = lock.quitModeName;
                locationLock.ssid = lock.ssid;
                locationLock.selected = lock.selected;
                locationLock.using = lock.using;
                break;
            }
        }

        // sync database
        mTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                LockModeDao lmd = new LockModeDao(mContext);
                lmd.updateLocationLock(lock);
            }
        });
    }

    @Override
    public LockMode getCurLockMode() {
        return mCurrentMode;
    }

    @Override
    public String getCurLockName() {
        return mCurrentMode == null ? "" : mCurrentMode.modeName;
    }

    @Override
    public List<String> getCurLockList() {
        if (mCurrentMode == null || mCurrentMode.lockList == null
                || mCurrentMode.lockList.isEmpty()) {
            List<String> list = Collections.synchronizedList(new ArrayList<String>(1));
            list.add(mContext.getPackageName());
            return list;
        }
        return mCurrentMode.lockList;
    }

    @Override
    public boolean isPackageLocked(String packageName) {
        if (TextUtils.isEmpty(packageName)) return false;

        List<String> pkgList = getCurLockList();
        return pkgList.contains(packageName);
    }

    @Override
    public void setCurrentLockMode(final LockMode mode, boolean fromUser) {
        if (mCurrentMode == mode || mode == null) return;
        if (mCurrentMode != null) {
            mCurrentMode.isCurrentUsed = false;
        }
        final LockMode lastMode = mCurrentMode;
        mode.isCurrentUsed = true;
        mCurrentMode = mode;
//        PrivacyHelper.getInstance(mContext).computePrivacyLevel(PrivacyHelper.VARABLE_APP_LOCK);
        mTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                LockModeDao lmd = new LockModeDao(mContext);
                lmd.updateLockMode(lastMode);
                lmd.updateLockMode(mCurrentMode);
            }
        });
        if (mCurrentMode != null) {
            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "modesnow", mCurrentMode.modeName);
        }

        // check show time/location lock tip
        if (fromUser) {
            // checkLockTip();
        }
    }

    @Override
    public List<LockMode> getLockMode() {
        if (!mLockModeLoaded) {
            loadLockMode();
        }
        return mLockModeList;
    }

    @Override
    public List<TimeLock> getTimeLock() {
        if (!mLockModeLoaded) {
            loadLockMode();
        }
        return mTimeLockList;
    }

    @Override
    public List<LocationLock> getLocationLock() {
        if (!mLockModeLoaded) {
            loadLockMode();
        }
        return mLocationLockList;
    }

    @Override
    public void filterPackage(String pkg, boolean persistent) {
        if (!TextUtils.isEmpty(pkg)) {
            mFilterPgks.put(pkg, persistent);
        }
    }

    @Override
    public void filterAll(long outtime) {
        mFilterAll = true;

        if (mFillterAllTask != null) {
            mFillterAllTask.cancel();
        }

        mFillterAllTask = new TimerTask() {
            @Override
            public void run() {
                mFilterAll = false;
                mFillterAllTask = null;
            }
        };

        ThreadManager.getTimer().schedule(mFillterAllTask, outtime);
    }

    @Override
    public void filterPackage(final String packageName, long time) {
        filterPackage(packageName, true);
        ThreadManager.getTimer().schedule(new TimerTask() {
            @Override
            public void run() {
                mFilterPgks.remove(packageName);
            }
        }, time);
    }

    @Override
    public void filterSelfOneMinites() {
        AppMasterPreference amp = AppMasterPreference.getInstance(mContext);
        long curTime = System.currentTimeMillis();
        amp.setLastFilterSelfTime(curTime);
    }

    @Override
    public void startLockService() {
        LeoLog.d(TAG, "startLockService");
        AppMasterPreference amp = AppMasterPreference.getInstance(mContext);
        TaskDetectService service = TaskDetectService.getService();
        if (service != null) {
            if (amp.getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
                service.startDetect();
                amp.setDoubleCheck(null);
            }
        } else {
            mContext.startService(new Intent(mContext, TaskDetectService.class));
        }
    }

    @Override
    public void stopLockService() {
        LeoLog.d(TAG, "stopLockService");
        TaskDetectService service = TaskDetectService.getService();
        if (service != null) {
            service.stopDetect();
        } else {
            LeoLog.d(TAG, "mDetectService = null");
        }
    }

    @Override
    public boolean inRelockTime(String pkg) {
        return ((TimeoutRelockPolicy) mLockPolicy).inRelockTime(pkg);
    }

    private String lastPck = "";

    @Override
    public boolean applyLock(int lockMode, String lockedPkg, boolean restart, OnUnlockedListener listener) {
        if (mFilterAll) {
            mFilterAll = false;
            LeoLog.d(TAG, "mFilterAll");
            return false;
        }

        LeoLog.d("testApplyLock", "applyLock");
        LeoLog.d("testApplyLock", "applyLock lastPck : " + lastPck);
        LeoLog.d("testApplyLock", "applyLock lockedPkg : " + lockedPkg);
        //samsung when open wifi or bluetooth , show lockScreen , then com.android.settings will show immediately
        //new Ps: not only Samsung s6 will , but also Huawei p6

//        String phoneBrand = Build.BRAND;
//        if (lockedPkg.equals(SAMSUNG_SETTINGS) && phoneBrand.contains(SAMSUNG)) {
        if (lockedPkg.equals(SAMSUNG_SETTINGS)) {
            if ((lastPck.equals(SwitchGroup.WIFI_SWITCH) || lastPck.equals(SwitchGroup.BLUE_TOOTH_SWITCH))) {
                lastPck = "";
                return false;
            }
        }

        lastPck = lockedPkg;

        if (TextUtils.equals(mContext.getPackageName(), lockedPkg)) {
            AppMasterPreference amp = AppMasterPreference.getInstance(mContext);
            long filterTime = amp.getLastFilterSelfTime();
            long curTime = System.currentTimeMillis();
            if (filterTime != 0 && (curTime - filterTime) <= 60 * 1000) {
                amp.setLastFilterSelfTime(0);
                LeoLog.d(TAG, "Filter self 1 min");
                return false;
            }

        }

        if (mFilterPgks.containsKey(lockedPkg)) {
            boolean persistent = mFilterPgks.get(lockedPkg);
            if (!persistent) {
                mFilterPgks.remove(lockedPkg);
            }
            LeoLog.d(TAG, "filter package: " + lockedPkg);
            AppMasterPreference amp = AppMasterPreference.getInstance(mContext);
            amp.setUnlocked(true);
            amp.setDoubleCheck(null);
            return false;
        }

        mExtranalUnlockListener = listener;
        Intent intent = new Intent(mContext, LockScreenActivity.class);
        if (mLockPolicy != null && !mLockPolicy.onHandleLock(lockedPkg)) {
            if (AppMasterPreference.getInstance(mContext)
                    .getLockType() == AppMasterPreference.LOCK_TYPE_NONE)
                return false;
            if (lockMode == LOCK_MODE_PURE) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
            if (!TextUtils.isEmpty(lockedPkg)) {
                intent.putExtra(EXTRA_LOCKED_APP_PKG, lockedPkg);
            }
            intent.putExtra(LockScreenActivity.EXTRA_LOCK_MODE, lockMode);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            if (restart) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
            AppMasterPreference amp = AppMasterPreference.getInstance(mContext);

            boolean lockSelf = mContext.getPackageName().equals(lockedPkg);
            amp.setDoubleCheck(lockSelf ? null : lockedPkg);
            mContext.startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    public int getLockedAppCount() {
        return mCurrentMode == null || mCurrentMode.lockList == null ? 0 : (mCurrentMode.lockList.size() > 0 ? mCurrentMode.lockList
                .size() - 1 : 0);
    }

    @Override
    public int getAllAppCount() {
        return AppLoadEngine.getInstance(mContext).getAppCounts() - 1;
    }

    @Override
    public String getLastActivity() {
        TaskDetectService service = TaskDetectService.getService();
        if (service != null) {
            return service.getLastRunningActivity();
        }
        return null;
    }

    @Override
    public String getLastPackage() {
        TaskDetectService service = TaskDetectService.getService();
        if (service != null) {
            return service.getLastRunningPackage();
        }
        return null;
    }


    @Override
    public void recordOutcountTask(String pkg) {
        if (!TextUtils.isEmpty(pkg) && !mOutcountPkgMap.containsKey(pkg)) {
            OutcountTrackTask task = new OutcountTrackTask(pkg);
            ScheduledFuture<?> future = mScheduler.scheduleAtFixedRate(new OutcountTrackTask(pkg),
                    0, 500, TimeUnit.MILLISECONDS);
            mOutcountTaskMap.put(task, future);
            mOutcountPkgMap.put(pkg, 10 * 1000);
        }
    }

    @Override
    public int getOutcountTime(String pkg) {
        if (mOutcountPkgMap.containsKey(pkg)) {
            return mOutcountPkgMap.get(pkg);
        } else {
            return 0;
        }
    }

    private void updateTimeLockOperation(TimeLock timeLock) {
        if (timeLock == null)
            return;

        // cancel old task
        List<ScheduledFuture<?>> list = mTLMap.get(timeLock);
        if (list != null) {
            for (ScheduledFuture<?> scheduledFuture : list) {
                scheduledFuture.cancel(true);
            }
        }
        mTLMap.remove(timeLock);

        // if using, add new task
        if (timeLock.using) {
            /* start this time lock */
            // init some temp val
            Calendar calendar = Calendar.getInstance();
            int curDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            if (curDayOfWeek == 1) {
                curDayOfWeek = 7;
            } else {
                curDayOfWeek -= 1;
            }
            int curHour = calendar.get(Calendar.HOUR_OF_DAY);
            int curMinute = calendar.get(Calendar.MINUTE);
            int curSecond = calendar.get(Calendar.SECOND);
            long curTime = curHour * 60 * 60 + curMinute * 60 + curSecond;
            long dayTime = 24 * 60 * 60;
            long weekTime = 7 * dayTime;
            long triggerSecond = 0;

            // get time point and repeat mode
            TimeLock.TimePoint time = timeLock.time;
            long trigleTime = time.hour * 60 * 60 + time.minute * 60;
            TimeLock.RepeatTime repeat = timeLock.repeatMode;
            byte[] repeatDay = repeat.getAllRepeatDayOfWeek();

            // add this operation to map
            List<ScheduledFuture<?>> operations = new ArrayList<ScheduledFuture<?>>();
            mTLMap.put(timeLock, operations);

            // scan repeat day
            if (!TextUtils.isEmpty(timeLock.repeatMode.toString())) {
                for (byte b : repeatDay) {
                    if (b > curDayOfWeek) {
                        triggerSecond = ((b - 1) * dayTime + trigleTime)
                                - ((curDayOfWeek - 1) * dayTime + curTime);
                        LeoLog.d("time lock",
                                "add alarm set, " + timeLock.name + " :  "
                                        + timeLock.time.toString() + "   triggerSecond = "
                                        + triggerSecond);
                        // set alarm
                        TimeLockOperation operation = new TimeLockOperation(timeLock.id,
                                timeLock.lockModeId);
                        ScheduledFuture<?> fature = mScheduler.scheduleWithFixedDelay(operation,
                                triggerSecond, weekTime, TimeUnit.SECONDS);
                        operations.add(fature);
                    } else if (b == curDayOfWeek) {
                        if (curTime > trigleTime) {
                            triggerSecond = weekTime - (curTime - trigleTime);
                        } else {
                            triggerSecond = trigleTime - curTime;
                        }
                        LeoLog.d(
                                "time lock",
                                "add alarm set, " + timeLock.name + " :  "
                                        + timeLock.time.toString() + "   triggerSecond = "
                                        + triggerSecond);
                        // set alarm
                        TimeLockOperation operation = new TimeLockOperation(timeLock.id,
                                timeLock.lockModeId);
                        ScheduledFuture<?> fature = mScheduler.scheduleWithFixedDelay(operation,
                                triggerSecond, weekTime, TimeUnit.SECONDS);
                        operations.add(fature);
                    } else {
                        triggerSecond = 7 * dayTime - (((curDayOfWeek - 1) * dayTime + curTime)
                                - ((b - 1) * dayTime + trigleTime));
                        LeoLog.d(
                                "time lock",
                                "add alarm set, " + timeLock.name + " :  "
                                        + timeLock.time.toString() + "   triggerSecond = "
                                        + triggerSecond);
                        // set alarm
                        TimeLockOperation operation = new TimeLockOperation(timeLock.id,
                                timeLock.lockModeId);
                        ScheduledFuture<?> fature = mScheduler.scheduleWithFixedDelay(operation,
                                triggerSecond, weekTime, TimeUnit.SECONDS);
                        operations.add(fature);
                    }
                }
            } else {// do not repeat
                if (curTime <= trigleTime) {
                    triggerSecond = trigleTime - curTime;
                } else {
                    triggerSecond = dayTime - curTime + triggerSecond;
                }
                LeoLog.d(
                        "time lock",
                        "add alarm set, " + timeLock.name + " :  "
                                + timeLock.time.toString() + "   triggerSecond = "
                                + triggerSecond);
                // set alarm
                TimeLockOperation operation = new TimeLockOperation(timeLock.id,
                        timeLock.lockModeId);
                ScheduledFuture<?> fature = mScheduler.schedule(operation,
                        triggerSecond, TimeUnit.SECONDS);
                operations.add(fature);
            }
        }
    }

    @Override
    public int getSecurityScore() {
        List<AppItemInfo> newAppList = getNewAppList();

        int score = MAX_SCORE;
        if (newAppList != null) {
            score -= newAppList.size() * SPA;
        }

        mCachedScore = score;
        return score < 0 ? 0 : score;
//        int score = 0;
//        if (mCachedScore != NO_CACHE) {
//            score = mCachedScore < 0 ? 0 : mCachedScore;
//            return score;
//        }
//
//        score = calSecurityScore();
//
//        mCachedScore = score;
//        return score < 0 ? 0 : score;
    }

    @Override
    public synchronized List<AppItemInfo> getNewAppList() {
        List<AppItemInfo> result = new ArrayList<AppItemInfo>();
        AppMasterApplication ctx = AppMasterApplication.getInstance();
        long start = SystemClock.elapsedRealtime();
        List<AppItemInfo> allList = AppLoadEngine.getInstance(ctx).getAllPkgInfo();
        LeoLog.i(TAG, "getNewAppList, getAllPkgInfo-cost: " + (SystemClock.elapsedRealtime() - start));
        result.addAll(allList);

        List<String> lockList = getCurLockList();

        start = SystemClock.elapsedRealtime();
        // 排出已加锁列表 以及 过滤列表
        Iterator<AppItemInfo> iterator = result.iterator();
        while (iterator.hasNext()) {
            AppItemInfo info = iterator.next();
            if (lockList.contains(info.packageName) || inFilterList(info.packageName)) {
                iterator.remove();
            }
        }
        LeoLog.i(TAG, "getNewAppList, filter lock-cost: " + (SystemClock.elapsedRealtime() - start));

        start = SystemClock.elapsedRealtime();
        List<String> ignoreList = InstalledAppTable.getInstance().getIgnoredList();
        // 排出已忽略列表
        iterator = result.iterator();
        while (iterator.hasNext()) {
            AppItemInfo info = iterator.next();
            if (ignoreList.contains(info.packageName)) {
                iterator.remove();
            }
        }
        LeoLog.i(TAG, "getNewAppList, filter ignore-cost: " + (SystemClock.elapsedRealtime() - start));

        mNewList.clear();
        mNewList.addAll(result);
        return result;
    }

    @Override
    public int getSecurityScore(List<AppItemInfo> list) {
        if (list == null) return MAX_SCORE;

        int score = MAX_SCORE;
        if (list != null) {
            score -= list.size() * SPA;
        }

        mCachedScore = score;
        return score < 0 ? 0 : score;
    }

    @Override
    public int lockAddedApp(List<String> pkgList) {
        int oldScore = mCachedScore < 0 ? 0 : mCachedScore;

        addPkg2Mode(pkgList, getCurLockMode(), false);
        int addedScore = MAX_SCORE - oldScore;

        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                List<AppItemInfo> itemInfos = AppLoadEngine.getInstance(mContext).getAllPkgInfo();
                InstalledAppTable.getInstance().insertIgnoreItemList(itemInfos);
            }
        });
        addedScore = addedScore < 0 ? 0 : addedScore;
        return addedScore;
    }

    @Override
    public int getIncreaseScore(int appNum) {
        int oldScore = mCachedScore < 0 ? 0 : mCachedScore;

        int addedScore = MAX_SCORE - oldScore;

        return addedScore;
    }

    @Override
    public int ignore() {
        int oldScore = mCachedScore < 0 ? 0 : mCachedScore;

        int addedScore = MAX_SCORE - oldScore;

        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                List<AppItemInfo> itemInfos = AppLoadEngine.getInstance(mContext).getAllPkgInfo();
                InstalledAppTable.getInstance().insertIgnoreItemList(itemInfos);
            }
        });
        addedScore = addedScore < 0 ? 0 : addedScore;
        return addedScore;
    }

    @Override
    public int getMaxScore() {
        return MAX_SCORE;
    }

    private void addPkg2Mode(List<String> pkgs, LockMode mode, boolean notifyScoreChange) {
        List<String> ignoreList = InstalledAppTable.getInstance().getIgnoredList();
        List<String> coreChgList = new ArrayList<String>();
        synchronized (this) {
            if (pkgs != null && mode != null && mode.lockList != null && mode.defaultFlag != 0) {
                for (String pkg : pkgs) {
                    if (!TextUtils.isEmpty(pkg) && !mode.lockList.contains(pkg)) {
                        mode.lockList.add(0, pkg);
                        if (!ignoreList.contains(pkg)) {
                            coreChgList.add(pkg);
                        }
                    } else {
                        continue;
                    }
                }
            } else {
                return;
            }

            // add self pkg
            if (!mode.lockList.contains(mContext.getPackageName())) {
                mode.lockList.add(mContext.getPackageName());
            }
        }

        if (mode.isCurrentUsed) {
//            PrivacyHelper.getInstance(mContext).computePrivacyLevel(PrivacyHelper.VARABLE_APP_LOCK);
            mCachedScore += SPA * coreChgList.size();
            if (notifyScoreChange) {
                notifySecurityChange();
            }
            InstalledAppTable.getInstance().insertIgnoreList(coreChgList);
        }
        updateMode(mode);
        LeoEventBus.getDefaultBus().post(new AppLockChangeEvent(AppLockChangeEvent.APP_ADD));
    }

    private synchronized void loadLockMode() {
        if (AppMasterPreference.getInstance(mContext).isFirstUseLockMode()) {
            LeoLog.d("loadLockMode", "first Load ");
            boolean resault = addDefaultMode();
            mLockModeLoaded = resault;
            AppMasterPreference.getInstance(mContext).setFirstUseLockMode(!resault);
        } else {
            LeoLog.d("loadLockMode", "not first Load ");
            mTaskExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    LockModeDao lmd = new LockModeDao(mContext);
                    // load lock mode
                    mLockModeList = lmd.querryLockModeList();

                    LeoLog.d("loadLockMode", mLockModeList.size() + "");
                    // load time lock
                    mTimeLockList = lmd.querryTimeLockList();
                    // load location lock
                    mLocationLockList = lmd.querryLocationLockList();

                    // check remove unlock-all mode< v2.1 >
                    checkRemoveUnlockAll();

                    for (LockMode lockMode : mLockModeList) {
                        if (lockMode.isCurrentUsed) {
                            mCurrentMode = lockMode;
                            break;
                        }
                    }
                    mLockModeLoaded = true;
                }

            });
        }
        LeoLog.d("loadLockMode", "Load finish : " + mLockModeList.size());
    }

    // check remove unlock-all mode< v2.1 >
    private void checkRemoveUnlockAll() {
        LockMode unlockAll = null;
        for (LockMode mode : mLockModeList) {
            if (mode.defaultFlag == 0) {
                unlockAll = mode;
                break;
            }
        }

        if (unlockAll != null) {
            // first, rejust lock mode list
            LockModeDao lmd = new LockModeDao(mContext);
            lmd.deleteLockMode(unlockAll);
            mLockModeList.remove(unlockAll);

            // add home mode
            LockMode homeMode = new LockMode();
            homeMode.modeName = mContext.getString(R.string.family_mode);
            homeMode.isCurrentUsed = false;
            homeMode.defaultFlag = 3;

            List<String> list = Collections.synchronizedList(new LinkedList<String>());
            list.add(mContext.getPackageName());
            for (String pkg : Constants.sDefaultHomeModeList) {
                if (AppUtil.appInstalled(mContext, pkg)) {
                    list.add(pkg);
                }
            }
            homeMode.lockList = list;
            if (unlockAll.isCurrentUsed) {
                homeMode.isCurrentUsed = true;
            }
            // installHomeModeShortcut(mContext, lockMode);
            mLockModeList.add(1, homeMode);
            lmd.insertLockMode(homeMode);

            // second, rejust time lock
            for (TimeLock timeLock : mTimeLockList) {
                if (timeLock.lockModeId == unlockAll.modeId) {
                    timeLock.lockModeId = homeMode.modeId;
                    timeLock.lockModeName = homeMode.modeName;
                    lmd.updateTimeLock(timeLock);
                }
            }

            // third, rejust location lock
            boolean hit = false;
            for (LocationLock locationLock : mLocationLockList) {
                hit = false;
                if (locationLock.entranceModeId == unlockAll.modeId) {
                    locationLock.entranceModeId = homeMode.modeId;
                    locationLock.entranceModeName = homeMode.modeName;
                    hit = true;
                }
                if (locationLock.quitModeId == unlockAll.modeId) {
                    locationLock.quitModeId = homeMode.modeId;
                    locationLock.quitModeName = homeMode.modeName;
                    hit = true;
                }
                if (hit) {
                    lmd.updateLocationLock(locationLock);
                }
            }
        }

    }

    /* add default lock mode when we first load lock mode */
    private boolean addDefaultMode() {
        if (mLoadDefaultDataFuture != null && !mLoadDefaultDataFuture.isDone()) {
            try {
                return mLoadDefaultDataFuture.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            mLoadDefaultDataFuture = mTaskExecutor.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    LockModeDao lmd = new LockModeDao(mContext);
                    // add vistor mode
                    LockMode lockMode = new LockMode();
                    lockMode.modeName = mContext.getString(R.string.vistor_mode);
                    lockMode.isCurrentUsed = true;
                    lockMode.defaultFlag = 1;
                    List<String> list = Collections.synchronizedList(new LinkedList<String>());
                    list.add(mContext.getPackageName());
                    lockMode.lockList = list;
                    mLockModeList.add(lockMode);
                    mCurrentMode = lockMode;
                    lmd.insertLockMode(lockMode);

                    // add family mode
                    lockMode = new LockMode();
                    lockMode.modeName = mContext.getString(R.string.family_mode);
                    lockMode.isCurrentUsed = false;
                    lockMode.defaultFlag = 3;
                    list = Collections.synchronizedList(new LinkedList<String>());
                    list.add(mContext.getPackageName());
                    for (String pkg : Constants.sDefaultHomeModeList) {
                        // AM-1765 联想K2110Aandroid]testin测试运行失败
                        boolean isAppInstalled = false;
                        try {
                            isAppInstalled = AppUtil.appInstalled(mContext, pkg);
                        } catch (Exception e) {
                            isAppInstalled = false;
                        }
                        if (isAppInstalled) {
                            list.add(pkg);
                        }
                    }
                    lockMode.lockList = list;
                    mLockModeList.add(lockMode);
                    lmd.insertLockMode(lockMode);
                    return true;
                }
            });
            try {
                return mLoadDefaultDataFuture.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void handleScreenChange(Intent intent) {
        if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
            if (mLockPolicy instanceof TimeoutRelockPolicy) {
                if (AppMasterPreference.getInstance(mContext).isAutoLock()) {
                    ((TimeoutRelockPolicy) mLockPolicy).clearLockApp();
                }
            }
            LeoLog.d("handleScreenChange", "LockManage  handleScreenChange");
            stopLockService();

        } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            startLockService();
            CheckNewBootstrap.checkProxy();
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    checkScreenOn();
                }
            }, 500);
        } else if (PhoneInfo.getPhoneDeviceModel().contains("Nokia")
                && Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
            final String lastRunningPkg = getLastPackage();
            final String lastRunningActivity = getLastActivity();

            if (isPackageLocked(lastRunningPkg)
                    && !LockScreenActivity.class.getName().contains(lastRunningActivity)
                    && !WaitActivity.class.getName().contains(lastRunningActivity)
                    && !ProxyActivity.class.getName().contains(lastRunningActivity)) {
                applyLock(LOCK_MODE_FULL, lastRunningPkg, false, null);
            }
        }
    }

    private void checkScreenOn() {
        AppMasterPreference pref = AppMasterPreference.getInstance(mContext);
        if (!pref.isAutoLock()) {
            return;
        }
        List<String> list = getCurLockList();
        if (list == null) {
            LeoLog.d(TAG, "lockList = null");
            return;
        }
        TaskDetectService service = TaskDetectService.getService();
        if (service == null) {
            LeoLog.d(TAG, "mDetectService = null");
            return;
        }

        if (mPauseScreenonLock) {
            LeoLog.d(TAG, "mPauseScreenonLock = true");
            return;
        }

        final String lastRunningPkg = service.getLastRunningPackage();
        final String lastRunningActivity = service.getLastRunningActivity();
        if (list.contains(lastRunningPkg)
                && !IntruderCatchedActivity.class.getName().contains(lastRunningActivity)
                && !LockScreenActivity.class.getName().contains(lastRunningActivity)
                && !WaitActivity.class.getName().contains(lastRunningActivity)
                && !ProxyActivity.class.getName().contains(lastRunningActivity)
                && !BatteryShowViewActivity.class.getName().contains(lastRunningActivity)) {
            LeoLog.d("Track Lock Screen",
                    "apply lockscreen form screen on => " + lastRunningPkg
                            + "/" + lastRunningActivity);
            boolean lock = applyLock(LOCK_MODE_FULL, lastRunningPkg, false,
                    new OnUnlockedListener() {
                        @Override
                        public void onUnlocked() {
                            try {
                                Intent intent = new Intent();
                                intent.setClassName(lastRunningPkg, lastRunningActivity);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mContext.startActivity(intent);
                            } catch (Exception e) {

                            }
                        }

                        @Override
                        public void onUnlockOutcount() {
                        }

                        @Override
                        public void onUnlockCanceled() {
                        }
                    });
            if (lock) {
                pref.setDoubleCheck(null);
                pref.setUnlocked(false);
            }
        }
    }

    public class TimeLockOperation implements Runnable {
        long timeLockId;
        long modeId;

        public TimeLockOperation(long timeLockId, long modeId) {
            super();
            this.timeLockId = timeLockId;
            this.modeId = modeId;
        }

        @Override
        public void run() {
            if (modeId != 0) {
                for (LockMode lockMode : mLockModeList) {
                    if (lockMode.modeId == modeId) {
                        LeoLog.d("TimeLockReceiver", "change current lock mode:  " + lockMode.modeName);
                        setCurrentLockMode(lockMode, false);
                        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "modeschage", "time");
                        break;
                    }
                }
                for (TimeLock timeLock : mTimeLockList) {
                    if (timeLock.id == timeLockId) {
                        if (timeLock.using && timeLock.repeatMode.getAllRepeatDayOfWeek().length == 0) {
                            timeLock.using = false;
                            updateTimeLock(timeLock);
                            LeoLog.d("TimeLockReceiver", "timeLock:  " + timeLock.name);
                            LeoEventBus.getDefaultBus().post(
                                    new TimeLockEvent(EventId.EVENT_TIME_LOCK_CHANGE, "time lock change"));
                            break;
                        }
                    }
                }
                LeoEventBus.getDefaultBus().post(
                        new LockModeEvent(EventId.EVENT_MODE_CHANGE, "time lock receiver"));
            }
        }

    }

    public class OutcountTrackTask implements Runnable {
        String pkg;

        public OutcountTrackTask(String pkg) {
            super();
            this.pkg = pkg;
        }

        @Override
        public void run() {
            if (mOutcountPkgMap.containsKey(pkg)) {
                int time = mOutcountPkgMap.get(pkg).intValue();
                if (time <= 0) {
                    ScheduledFuture<?> future = mOutcountTaskMap.get(this);
                    mOutcountPkgMap.remove(this);
                    mOutcountPkgMap.remove(pkg);
                    future.cancel(true);
                } else {
                    mOutcountPkgMap.put(pkg, time - 500);
                }
            }
        }
    }

    class TimeChangeReceive extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_TIME_TICK.endsWith(action)) {
                LeoLog.d("TimeChangeReceive", "time change");
                mTaskExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        // cancel and clear futurelist
                        List<ScheduledFuture<?>> futureList;
                        if (!mTLMap.isEmpty()) {
                            Set<Map.Entry<TimeLock, List<ScheduledFuture<?>>>> set = mTLMap.entrySet();
                            for (Map.Entry<TimeLock, List<ScheduledFuture<?>>> entry : set) {
                                futureList = entry.getValue();
                                for (ScheduledFuture<?> future : futureList) {
                                    future.cancel(true);
                                }
                            }
                            mTLMap.clear();
                        }
                        // re-init time lock
                        initTimeLock();
                    }
                });

            }
        }
    }
}
