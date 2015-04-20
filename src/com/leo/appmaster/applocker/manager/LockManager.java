
package com.leo.appmaster.applocker.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
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
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.WindowManager;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.LocationLockEditActivity;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.applocker.RecommentAppLockListActivity;
import com.leo.appmaster.applocker.TimeLockEditActivity;
import com.leo.appmaster.applocker.model.LocationLock;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.applocker.model.TimeLock;
import com.leo.appmaster.applocker.model.TimeLock.RepeatTime;
import com.leo.appmaster.applocker.model.TimeLock.TimePoint;
import com.leo.appmaster.applocker.service.TaskDetectService;
import com.leo.appmaster.applocker.service.TaskDetectService.TaskDetectBinder;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.AppUnlockEvent;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.eventbus.event.LocationLockEvent;
import com.leo.appmaster.eventbus.event.LockModeEvent;
import com.leo.appmaster.eventbus.event.TimeLockEvent;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.ui.dialog.LEOThreeButtonDialog;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.BitmapUtils;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NetWorkUtil;
import com.leo.appmater.globalbroadcast.LeoGlobalBroadcast;
import com.leo.appmater.globalbroadcast.NetworkStateListener;
import com.leo.appmater.globalbroadcast.ScreenOnOffListener;

/**
 * @author zhangwenyang
 */
public class LockManager {
    public static final String TAG = "LockManager";

    public static final String ACTION_TIME_LOCK = "action_time_lock";

    public static final int LOCK_MODE_FULL = 1;
    public static final int LOCK_MODE_PURE = 2;

    public static final int NETWORK_NULL = -1;
    public static final int NETWORK_MOBILE = 0;
    public static final int NETWORK_WIFI = 1;

    public int mCurNetType = NETWORK_MOBILE;
    public String mCurWifi = "";

    public int mLastNetType = NETWORK_MOBILE;
    public String mLastWifi = "";

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

    public static final String EXTRA_LOCKED_APP_PKG = "locked_app_pkg";

    private ILockPolicy mLockPolicy;
    private static LockManager sInstance;
    private Context mContext;
    private ScreenOnOffListener mScreenListener;
    private TaskDetectService mDetectService;
    private Handler mHandler;

    /*
     * filter list
     */
    private HashMap<String, Boolean> mFilterPgks;
    private HashMap<String, Boolean> mFilterActivitys;

    /*
     * lock mode, time and location lock
     */
    private List<LockMode> mLockModeList;
    private List<TimeLock> mTimeLockList;
    private List<LocationLock> mLocationLockList;
    private HashMap<TimeLock, List<ScheduledFuture<?>>> mTLMap;
    private LockMode mCurrentMode;
    private boolean mLockModeLoaded;

    /*
     * record ortcount unlock
     */
    private HashMap<String, Integer> mOutcountPkgMap;
    private HashMap<Runnable, ScheduledFuture<?>> mOutcountTaskMap;

    /*
     * just call one time for every listener
     */
    private OnUnlockedListener mExtranalUnlockListener;

    /*
     * listener mimute change
     */
    private TimeChangeReceive mTimeChangeReceiver;

    /*
     * for time lock, and other time task
     */
    private ScheduledExecutorService mScheduler = Executors.newScheduledThreadPool(2);

    /*
     * for data operation
     */
    private ExecutorService mTaskExecutor = Executors.newSingleThreadExecutor();
    private Future<Boolean> mLoadDefaultDataFuture;

    private LockManager() {
        mContext = AppMasterApplication.getInstance();
        mLockPolicy = new TimeoutRelockPolicy(mContext);
        mFilterPgks = new HashMap<String, Boolean>();
        mFilterActivitys = new HashMap<String, Boolean>();
        mOutcountPkgMap = new HashMap<String, Integer>();
        mOutcountTaskMap = new HashMap<Runnable, ScheduledFuture<?>>();
        mLockModeList = new LinkedList<LockMode>();
        mTimeLockList = new LinkedList<TimeLock>();
        mLocationLockList = new LinkedList<LocationLock>();
        mTLMap = new HashMap<TimeLock, List<ScheduledFuture<?>>>();
        mHandler = new Handler();
        mTimeChangeReceiver = new TimeChangeReceive();

        initFilterList();
    }

    public static synchronized LockManager getInstatnce() {
        if (sInstance == null) {
            sInstance = new LockManager();
        }
        return sInstance;
    }

    private ServiceConnection mSc = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            LeoLog.d(TAG, "onServiceDisconnected");
            mDetectService = null;
        }

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder binder) {
            LeoLog.d(TAG, "onServiceConnected");
            mDetectService = ((TaskDetectBinder) binder).getService();
        }
    };

    private ScheduledFuture<?> mFilterSelfTast;

    public void initFilterList() {
        mFilterPgks.put("WaitActivity", true);
    }

    public void recordOutcountTask(String pkg) {
        if (!TextUtils.isEmpty(pkg) && !mOutcountPkgMap.containsKey(pkg)) {
            OutcountTrackTask task = new OutcountTrackTask(pkg);
            ScheduledFuture<?> future = mScheduler.scheduleAtFixedRate(new OutcountTrackTask(pkg),
                    0, 1, TimeUnit.SECONDS);
            mOutcountTaskMap.put(task, future);
            mOutcountPkgMap.put(pkg, 10);
        }
    }

    public int getOutcountTime(String pkg) {
        if (mOutcountPkgMap.containsKey(pkg)) {
            return mOutcountPkgMap.get(pkg);
        } else {
            return 0;
        }
    }

    public void init() {
        LeoLog.d(TAG, "init");
        bindService();
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
        tryLoadLockMode();
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
                TimePoint time = timeLock.time;
                long trigleTime = time.hour * 60 * 60 + time.minute * 60;
                RepeatTime repeat = timeLock.repeatMode;
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

    private void initLocationLock() {
        if (NetWorkUtil.isNetworkAvailable(mContext)) {
            if (NetWorkUtil.isWifiConnected(mContext)) {
                mCurNetType = mLastNetType = NETWORK_WIFI;
                mCurWifi = mLastWifi = NetWorkUtil.getCurWifiName(mContext);
                LeoLog.d("onNetworkStateChange", "mCurWifi = " + mCurWifi);
                LeoLog.d("onNetworkStateChange", "0");
                LeoLog.d("onNetworkStateChange",
                        "mLocationLockList size = " + mLocationLockList.size());
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
                if (mLastNetType != mCurNetType) {
                    if (mLastNetType == NETWORK_WIFI) {
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
                        // change wifi to another wifi
                        /*
                         * change wifi to another wifi, select the last match
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

                mLastNetType = mCurNetType;
                mLastWifi = mCurWifi;

                LeoEventBus.getDefaultBus().post(
                        new LockModeEvent(EventId.EVENT_MODE_CHANGE, "location change"));

            }
        });
    }

    public boolean filterPkg(String pklg) {
        return mFilterActivitys.containsKey(pklg);
    }

    private void tryLoadLockMode() {
        if (mLockModeLoaded)
            return;
        loadLockMode();
    }

    public void addPkg2Mode(List<String> pkgs, final LockMode mode) {

        if (pkgs != null && mode != null && mode.defaultFlag != 0) {
            for (String pkg : pkgs) {
                if (!TextUtils.isEmpty(pkg) && !mode.lockList.contains(pkg)) {
                    mode.lockList.add(0, pkg);
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

        if (mode.isCurrentUsed) {
            PrivacyHelper.getInstance(mContext).computePrivacyLevel(PrivacyHelper.VARABLE_APP_LOCK);
        }
        updateMode(mode);
    }

    public void removePkgFromMode(List<String> pkgs, final LockMode mode) {

        if (pkgs != null) {
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
        if (mode.isCurrentUsed) {
            PrivacyHelper.getInstance(mContext).computePrivacyLevel(PrivacyHelper.VARABLE_APP_LOCK);
        }

        updateMode(mode);
    }

    public void updateMode(final LockMode mode) {
        if (mode == null)
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

    public void removeLockMode(final LockMode lockMode) {
        if (mLockModeList.remove(lockMode)) {

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
                                new TimeLockEvent(EventId.EVENT_TIME_LOCK_CHANGE,
                                        "remove time lock"));
                    }

                    List<LocationLock> deleteLocationList = new ArrayList<LocationLock>();
                    for (LocationLock locationLock : mLocationLockList) {
                        if (locationLock.entranceModeId == lockMode.modeId
                                || locationLock.entranceModeId == lockMode.modeId) {
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
                                new LocationLockEvent(EventId.EVENT_LOCATION_LOCK_CHANGE,
                                        "remove location lock"));
                    }
                }
            });
        }
    }

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

    public void removeTimeLock(final TimeLock timeLock) {
        // cancel old task if have
        if (mTimeLockList.remove(timeLock)) {
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
            TimePoint time = timeLock.time;
            long trigleTime = time.hour * 60 * 60 + time.minute * 60;
            RepeatTime repeat = timeLock.repeatMode;
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

    public void updateTimeLock(final TimeLock lock) {
        if (lock == null)
            return;
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

    public void updateLocationLock(final LocationLock lock) {
        if (lock == null)
            return;
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
                    // check remove unlock-all mode< v2.1 >
                    checkRemoveUnlockAll();

                    LeoLog.d("loadLockMode", mLockModeList.size() + "");
                    // load time lock
                    mTimeLockList = lmd.querryTimeLockList();
                    // load location lock
                    mLocationLockList = lmd.querryLocationLockList();

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
            LockModeDao lmd = new LockModeDao(mContext);
            lmd.deleteLockMode(unlockAll);
            mLockModeList.remove(unlockAll);

            if (unlockAll.isCurrentUsed) {
                LockMode visitor = null;
                for (LockMode mode : mLockModeList) {
                    if (mode.defaultFlag == 1) {
                        visitor = mode;
                        break;
                    }
                }
                visitor.isCurrentUsed = true;
                lmd.updateLockMode(visitor);
            }

            // add home mode
            LockMode lockMode = new LockMode();
            lockMode.modeName = mContext.getString(R.string.family_mode);
            lockMode.isCurrentUsed = false;
            lockMode.defaultFlag = 3;
            lockMode.modeIcon =
                    BitmapFactory.decodeResource(mContext.getResources(),
                            R.drawable.lock_mode_family);
            LinkedList<String> list = new LinkedList<String>();
            list.add(mContext.getPackageName());
            for (String pkg : Constants.sDefaultHomeModeList) {
                if (AppUtil.appInstalled(mContext, pkg)) {
                    list.add(pkg);
                }
            }
            lockMode.lockList = list;
            mLockModeList.add(lockMode);
            lmd.insertLockMode(lockMode);
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
                    lockMode.modeIcon = BitmapFactory.decodeResource(mContext.getResources(),
                            R.drawable.lock_mode_visitor);
                    LinkedList<String> list = new LinkedList<String>();
                    list.add(mContext.getPackageName());
                    lockMode.lockList = list;
                    mLockModeList.add(lockMode);
                    mCurrentMode = lockMode;
                    lmd.insertLockMode(lockMode);

                    // add unlock all
                    // lockMode = new LockMode();
                    // lockMode.modeName =
                    // mContext.getString(R.string.unlock_all_mode);
                    // lockMode.isCurrentUsed = false;
                    // lockMode.defaultFlag = 0;
                    // lockMode.modeIcon =
                    // BitmapFactory.decodeResource(mContext.getResources(),
                    // R.drawable.lock_mode_unlock);
                    // list = new LinkedList<String>();
                    // list.add(mContext.getPackageName());
                    // lockMode.lockList = list;
                    // mLockModeList.add(lockMode);
                    // lmd.insertLockMode(lockMode);
                    // add office
                    // lockMode = new LockMode();
                    // lockMode.modeName = getString(R.string.office_mode);
                    // lockMode.isCurrentUsed = false;
                    // lockMode.defaultFlag = 2;
                    // lockMode.modeIcon =
                    // BitmapFactory.decodeResource(getResources(),
                    // R.drawable.lock_mode_office);
                    // list = new LinkedList<String>();
                    // list.add(mActivity.getPackageName());
                    // lockMode.lockList = list;
                    // mLockModeList.add(lockMode);
                    // lmd.insertLockMode(lockMode);

                    // add family mode
                    lockMode = new LockMode();
                    lockMode.modeName = mContext.getString(R.string.family_mode);
                    lockMode.isCurrentUsed = false;
                    lockMode.defaultFlag = 3;
                    lockMode.modeIcon =
                            BitmapFactory.decodeResource(mContext.getResources(),
                                    R.drawable.lock_mode_family);
                    list = new LinkedList<String>();
                    list.add(mContext.getPackageName());
                    for (String pkg : Constants.sDefaultHomeModeList) {
                        if (AppUtil.appInstalled(mContext, pkg)) {
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

    public synchronized LockMode getCurLockMode() {
        return mCurrentMode;
    }

    public synchronized String getCurLockName() {
        return mCurrentMode == null ? "" : mCurrentMode.modeName;
    }

    public synchronized List<String> getCurLockList() {
        if (mCurrentMode == null || mCurrentMode.lockList == null
                || mCurrentMode.lockList.isEmpty()) {
            ArrayList<String> list = new ArrayList<String>(1);
            list.add(mContext.getPackageName());
            return list;
        }
        return mCurrentMode.lockList;
    }

    public void setCurrentLockMode(final LockMode mode, boolean fromUser) {
        if (mCurrentMode == mode)
            return;
        mCurrentMode.isCurrentUsed = false;
        final LockMode lastMode = mCurrentMode;
        mode.isCurrentUsed = true;
        mCurrentMode = mode;
        PrivacyHelper.getInstance(mContext).computePrivacyLevel(PrivacyHelper.VARABLE_APP_LOCK);
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
            checkLockTip();
        }
    }

    private void checkLockTip() {
        int switchCount = AppMasterPreference.getInstance(mContext).getSwitchModeCount();
        switchCount++;
        AppMasterPreference.getInstance(mContext).setSwitchModeCount(switchCount);
        if (switchCount == 6) {
            // TODO show tip
            int timeLockCount = mTimeLockList.size();
            int locationLockCount = mLocationLockList.size();

            if (timeLockCount == 0 && locationLockCount == 0) {
                // show three btn dialog
                LEOThreeButtonDialog dialog = new LEOThreeButtonDialog(
                        mContext);
                dialog.setTitle(R.string.time_location_lock_tip_title);
                String tip = mContext.getString(R.string.time_location_lock_tip_content);
                dialog.setContent(tip);
                dialog.setLeftBtnStr(mContext.getString(R.string.cancel));
                dialog.setMiddleBtnStr(mContext.getString(R.string.lock_mode_time));
                dialog.setRightBtnStr(mContext.getString(R.string.lock_mode_location));
                dialog.setRightBtnBackground(R.drawable.dlg_right_button_selector);
                dialog.setOnClickListener(new LEOThreeButtonDialog.OnDiaogClickListener() {
                    @Override
                    public void onClick(int which) {
                        Intent intent = null;
                        if (which == 0) {
                            // cancel
                        } else if (which == 1) {
                            // new time lock
                            intent = new Intent(mContext, TimeLockEditActivity.class);
                            intent.putExtra("new_time_lock", true);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                        } else if (which == 2) {
                            // new location lock
                            intent = new Intent(mContext, LocationLockEditActivity.class);
                            intent.putExtra("new_location_lock", true);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                        }
                    }
                });
                dialog.getWindow().setType(
                        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                dialog.show();
            } else {
                if (timeLockCount == 0 && locationLockCount != 0) {
                    // show time lock btn dialog
                    LEOAlarmDialog dialog = new LEOAlarmDialog(mContext);
                    dialog.setTitle(R.string.time_location_lock_tip_title);
                    String tip = mContext.getString(R.string.time_location_lock_tip_content);
                    dialog.setContent(tip);
                    dialog.setRightBtnStr(mContext.getString(R.string.lock_mode_time));
                    dialog.setLeftBtnStr(mContext.getString(R.string.cancel));
                    dialog.setOnClickListener(new OnDiaogClickListener() {
                        @Override
                        public void onClick(int which) {
                            Intent intent = null;
                            if (which == 0) {
                                // cancel
                            } else if (which == 1) {
                                // new time lock
                                intent = new Intent(mContext, TimeLockEditActivity.class);
                                intent.putExtra("new_time_lock", true);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mContext.startActivity(intent);
                            }

                        }
                    });
                    dialog.getWindow().setType(
                            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    dialog.show();

                } else if (timeLockCount != 0 && locationLockCount == 0) {
                    // show lcaotion btn dialog
                    LEOAlarmDialog dialog = new LEOAlarmDialog(mContext);
                    dialog.setTitle(R.string.time_location_lock_tip_title);
                    String tip = mContext.getString(R.string.time_location_lock_tip_content);
                    dialog.setContent(tip);
                    dialog.setLeftBtnStr(mContext.getString(R.string.lock_mode_location));
                    dialog.setRightBtnStr(mContext.getString(R.string.cancel));
                    dialog.setOnClickListener(new OnDiaogClickListener() {
                        @Override
                        public void onClick(int which) {
                            if (which == 0) {
                                // cancel
                            } else if (which == 1) {
                                // new time lock
                                Intent intent = new Intent(mContext, LocationLockEditActivity.class);
                                intent.putExtra("new_location_lock", true);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mContext.startActivity(intent);
                            }

                        }
                    });
                    dialog.getWindow().setType(
                            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    dialog.show();
                }
            }
        }
    }

    public List<LockMode> getLockMode() {
        if (!mLockModeLoaded) {
            loadLockMode();
        }
        return mLockModeList;
    }

    public List<TimeLock> getTimeLock() {
        if (!mLockModeLoaded) {
            loadLockMode();
        }
        return mTimeLockList;
    }

    public List<LocationLock> getLocationLock() {
        if (!mLockModeLoaded) {
            loadLockMode();
        }
        return mLocationLockList;
    }

    public void bindService() {
        Intent intent = new Intent(mContext, TaskDetectService.class);
        mContext.bindService(intent, mSc, 1001);
    }

    public boolean serviceBound() {
        return mDetectService != null;
    }

    public void addFilterLockPackage(String filterPackage, boolean persistent) {
        if (!TextUtils.isEmpty(filterPackage)) {
            mFilterPgks.put(filterPackage, persistent);
        }
    }

    public void timeFilter(final String packageName, long outtime) {
        addFilterLockPackage(packageName, true);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                mFilterPgks.remove(packageName);
            }
        }, outtime);

    }

    /**
     * time filter self 1 minute
     */
    public void timeFilterSelf() {
        if (mFilterSelfTast != null && !mFilterSelfTast.isDone() && !mFilterSelfTast.isCancelled()) {
            mFilterSelfTast.cancel(true);
            mFilterSelfTast = null;
        }

        addFilterLockPackage(mContext.getPackageName(), true);
        mFilterSelfTast = mScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                mFilterPgks.remove(mContext.getPackageName());
            }
        }, 1, TimeUnit.MINUTES);
    }

    public void removeFilterLockPackage(String filterPackage) {
        mFilterPgks.remove(filterPackage);
    }

    public void onEvent(AppUnlockEvent event) {
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

    public void unInit() {
        stopLockService();
        mContext.unbindService(mSc);
        mSc = null;
        mDetectService = null;
        LeoGlobalBroadcast.unregisterBroadcastListener(mScreenListener);
        mContext.unregisterReceiver(mTimeChangeReceiver);
        mScreenListener = null;
        mLockModeLoaded = false;
        mLockModeList.clear();
        mTimeLockList.clear();
        mLocationLockList.clear();
        mTLMap.clear();
    }

    public void startLockService() {
        LeoLog.d(TAG, "startLockService");
        AppMasterPreference amp = AppMasterPreference.getInstance(mContext);
        if (mDetectService != null
                && amp.getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
            mDetectService.startDetect();
            amp.setDoubleCheck(null);
        } else {
            LeoLog.d(TAG, "mDetectService = null");
        }
    }

    public void stopLockService() {
        LeoLog.d(TAG, "stopLockService");
        if (mDetectService != null) {
            mDetectService.stopDetect();
        } else {
            LeoLog.d(TAG, "mDetectService = null");
        }
    }

    protected void handleScreenChange(Intent intent) {
        if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
            if (mLockPolicy instanceof TimeoutRelockPolicy) {
                if (AppMasterPreference.getInstance(mContext).isAutoLock()) {
                    ((TimeoutRelockPolicy) mLockPolicy).clearLockApp();
                }
            }
            LeoLog.d("handleScreenChange", "LockManage  handleScreenChange");
            stopLockService();

        } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            if (LockManager.getInstatnce().serviceBound()) {
                LockManager.getInstatnce().startLockService();
            } else {
                LockManager.getInstatnce().bindService();
            }
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    checkScreenOn();
                }
            }, 500);
        }
    }

    public void setLockPolicy(ILockPolicy policy) {
        mLockPolicy = policy;
    }

    public ILockPolicy getLockPoliy() {
        return mLockPolicy;
    }

    public boolean applyLock(int lockMode, String lockedPkg, boolean restart,
            OnUnlockedListener listener) {

        if (TextUtils.equals(mContext.getPackageName(), lockedPkg)) {
            if (mFilterSelfTast != null && !mFilterSelfTast.isDone()
                    && !mFilterSelfTast.isCancelled()) {
                mFilterSelfTast.cancel(true);
                mFilterSelfTast = null;
                mFilterPgks.remove(lockedPkg);
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
            intent.putExtra(LockScreenActivity.EXTRA_LOCK_MODE,
                    lockMode);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
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

    public int getAllAppCount() {
        return AppLoadEngine.getInstance(mContext).getAppCounts() - 1;
    }

    public synchronized int getLockedAppCount() {
        return mCurrentMode == null ? 0 : (mCurrentMode.lockList.size() > 0 ? mCurrentMode.lockList
                .size() - 1 : 0);
    }

    public String getLastActivity() {
        return mDetectService.getLastRunningActivity();
    }

    private void checkScreenOn() {
        AppMasterPreference pref = AppMasterPreference.getInstance(mContext);
        if (!pref.isAutoLock()) {
            return;
        }
        List<String> list = LockManager.getInstatnce().getCurLockList();
        if (list == null) {
            LeoLog.d(TAG, "lockList = null");
            return;
        }
        if (mDetectService == null) {
            LeoLog.d(TAG, "mDetectService = null");
            return;
        }
        final String lastRunningPkg = mDetectService.getLastRunningPackage();
        final String lastRunningActivity = mDetectService.getLastRunningActivity();
        if (list.contains(lastRunningPkg)
                && !LockScreenActivity.class.getName().contains(lastRunningActivity)) {
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

    public void clearFilterList() {
        mFilterPgks.clear();
    }

    public class FilterPackageHolder {
        String packageName;
        boolean persistent;

        public FilterPackageHolder(String packageName, boolean persistent) {
            super();
            this.packageName = packageName;
            this.persistent = persistent;
        }
    }

    public class TimeLockIntent extends Intent {

        public TimeLockIntent(String actionTimeLock) {
            super(actionTimeLock);
        }

        @Override
        public boolean filterEquals(Intent other) {
            String myAction = getAction();
            String ontherAction = other.getAction();

            long myLockId = this.getLongExtra("time_lock_id", -1);
            long otherLockId = other.getLongExtra("time_lock_id", -1);

            return TextUtils.equals(myAction, ontherAction) && myLockId == otherLockId;
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
                        LeoLog.d("TimeLockReceiver", "change current lock mode:  "
                                + lockMode.modeName);
                        setCurrentLockMode(lockMode, false);
                        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "modeschage", "time");
                        break;
                    }
                }
                for (TimeLock timeLock : mTimeLockList) {
                    if (timeLock.id == timeLockId) {
                        if (timeLock.using
                                && timeLock.repeatMode.getAllRepeatDayOfWeek().length == 0) {
                            timeLock.using = false;
                            updateTimeLock(timeLock);
                            LeoLog.d("TimeLockReceiver", "timeLock:  "
                                    + timeLock.name);
                            LeoEventBus.getDefaultBus().post(
                                    new TimeLockEvent(EventId.EVENT_TIME_LOCK_CHANGE,
                                            "time lock change"));
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
                    mOutcountPkgMap.put(pkg, time - 1);
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
                            Set<Entry<TimeLock, List<ScheduledFuture<?>>>> set = mTLMap.entrySet();
                            for (Entry<TimeLock, List<ScheduledFuture<?>>> entry : set) {
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
