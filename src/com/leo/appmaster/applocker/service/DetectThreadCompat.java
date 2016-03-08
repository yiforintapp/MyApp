package com.leo.appmaster.applocker.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.FileObserver;
import android.os.Process;
import android.provider.Settings;
import android.text.TextUtils;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.Constants;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.manager.TaskChangeHandler;
import com.leo.appmaster.applocker.model.ProcessAdj;
import com.leo.appmaster.applocker.model.ProcessDetector;
import com.leo.appmaster.applocker.model.ProcessDetectorCompat22;
import com.leo.appmaster.applocker.model.ProcessDetectorUsageStats;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.utils.LeoLog;

/**
 * oom_adj进程监控thread，sony机型5.1以上以及Android M以上
 * @author Jasper
 *
 */
public class DetectThreadCompat extends Thread {
    private static final String TAG = "DetectThreadCompat";
    // 超时，找到lockapp之后，除了设置监听，另外高频去检查adj的值，防止被锁app启动较慢，或者启动或拉起了另外的activity
    private static final int WAIT_LOCK_APP_TIMEOUT = 1000;
    private static final int WAIT_DOUBLE_CHECK_TIMEOUT = 200;
    private static final int WAIT_NOT_FOUND_TIMEOUT = 100;
    private static final int MAX_DOUBLE_CHECK_COUNT = 10;
    private static final int WAIT_IGNORE_PKG = 200;
    
    private static final int MAX_NOT_FOUND_COUNT = 5;
    public static final boolean DBG = true;
    
    private ProcessDetector mDetector;
    private FileObserver mAdjObserver;
    
    private TaskChangeHandler mLockHandler;
    
    private ProcessAdj mForegroundAdj;
    private boolean mFoundLockApp;
    
    public DetectThreadCompat(TaskChangeHandler handler) {
        super(TAG);
        if (handler == null) {
            throw new RuntimeException("TaskChangeHandler is null.");
        }

        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                ProcessDetector detector = new ProcessDetector();
                if (detector.checkAvailable()) {
                    mDetector = detector;
                } else {
                    mDetector = new ProcessDetectorCompat22();
                }
            }
        });
        mLockHandler = handler;
        
    }

    @Override
    public void run() {
        ProcessAdj lastProcessAdj = null;
        int doubleCheckCount = 0;
        int notFoundAppCount = 0;

        ProcessDetectorUsageStats usageStats = new ProcessDetectorUsageStats();
        ProcessDetector detector = null;

        boolean lastIsCompat22 = false;
        while (true && !isInterrupted()) {
            if (mDetector == null || !mDetector.ready()) continue;

            if (usageStats.checkAvailable()) {
                detector = usageStats;
                if (lastIsCompat22) {
                    // AM-4007，防止开启了用量权限后，立即又弹出了设置页面的锁
                    String usagePkg = getUsagePackage();
                    if (!TextUtils.isEmpty(usagePkg)) {
                        LockManager lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
                        lm.filterPackage(usagePkg, 2000);
                    }
                    lastIsCompat22 = false;
                }
            } else {
                detector = mDetector;
                if ((detector instanceof ProcessDetector) || (detector instanceof ProcessDetectorCompat22)) {
                    lastIsCompat22 = true;
                }
            }
            lastProcessAdj = mForegroundAdj;
            
            if (lastProcessAdj != null && lastProcessAdj.pid > 0
                    && !detector.isOOMScoreMode()
                    && !detector.isHomePackage(lastProcessAdj)
                    && !lastProcessAdj.user.equals("system")) {
                int scoreAdj = detector.getOomScoreAdj(lastProcessAdj.pid);
                if (scoreAdj == lastProcessAdj.oomAdj && scoreAdj == 0) {
                    if (!parseAdjAndDeliver(lastProcessAdj)) continue;

                    if (DBG) {
                        LeoLog.i(TAG, "last adj not modify, so wait " + WAIT_LOCK_APP_TIMEOUT + " ms.");
                    }
                    synchronized (this) {
                        try {
                            wait(WAIT_LOCK_APP_TIMEOUT);
                        } catch (InterruptedException e) {
                            // 收到一个interrupt
                            break;
                        }
                    }
                    continue;
                }
            }

            // 加锁app列表里是否包含oom_adj为0的app
            ProcessAdj needToListenAdj = findNeedToLockAndListenApp(detector);
            if (needToListenAdj != null && (Constants.ISWIPE_PACKAGE.equals(needToListenAdj.pkg) || Constants.PL_PKG_NAME.equals(needToListenAdj.pkg))) {
                synchronized (this) {
                    try {
                        wait(WAIT_IGNORE_PKG);
                    } catch (InterruptedException e) {
                        // 收到一个interrupt
                        break;
                    }
                }
                // 如果是iswipe，忽略此次检查
                continue;
            }
            mForegroundAdj = needToListenAdj;
            
            if (needToListenAdj != null) {
                mLockHandler.setPkgBeforeScreenOff(needToListenAdj.pkg);
                notFoundAppCount = 0;
                if (DBG) {
                    LeoLog.i(TAG, needToListenAdj.pkg);
                }

                int timeout = detector.getTimeoutMs(needToListenAdj);
                if (needToListenAdj.pid > 0) {
                    AdjFileObserver observer = createAdjFileObserver(needToListenAdj);
                    if (observer == null) continue;

                    mAdjObserver = observer;
                    if (!needToListenAdj.equals(lastProcessAdj)) {
                        if (DBG) {
                            LeoLog.i(TAG, "neet to stop and restart watching.");
                        }

                        doubleCheckCount = 0;
                        if (mAdjObserver != null) {
                            mAdjObserver.stopWatching();
                        }
                        mAdjObserver.startWatching();
                    } else {
                        if (DBG) {
                            LeoLog.i(TAG, "keeping watching.");
                        }
                        mAdjObserver.startWatching();
                        doubleCheckCount++;
                    }

                    boolean needDoubleCheck = mFoundLockApp && (doubleCheckCount < MAX_DOUBLE_CHECK_COUNT);
                    timeout = needDoubleCheck ? WAIT_DOUBLE_CHECK_TIMEOUT : timeout;
                }
                if (!parseAdjAndDeliver(needToListenAdj)) continue;

                if (timeout <= 0) continue;
                // 如果当前是加锁app，wait时间修改为200ms，以解决部分进程创建慢，比锁页面晚出来的问题
                synchronized (this) {
                    try {
                        wait(timeout);
                    } catch (InterruptedException e) {
                        // 收到一个interrupt
                        break;
                    }
                }
            } else {
                if (DBG) {
                    LeoLog.i(TAG, "cannnot find foreground app.");
                }
                // MAX_NOT_FOUND_COUNT之后如果还找不到才认为是真的没找到，避免误操作
                if (++notFoundAppCount >= MAX_NOT_FOUND_COUNT) {
                    // 在加锁app，回到google页面，找不到监控的app，所以没有把之前加锁app的状态清理掉
                    mLockHandler.handleAppLaunch(Constants.PKG_WHAT_EVER, "", "");
                    synchronized (this) {
                        try {
                            wait(1000);
                        } catch (InterruptedException e) {
                            // 收到一个interrupt
                            break;
                        }
                    }
                }
            }
            
        }
    }

    private String getUsagePackage() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        AppMasterApplication context = AppMasterApplication.getInstance();
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(intent, 0);
        if (resolveInfos == null || resolveInfos.isEmpty()) {
            return null;
        }

        ResolveInfo resolveInfo = resolveInfos.get(0);
        return resolveInfo.activityInfo.packageName;
    }
    
    private ProcessAdj checkSelfForeground(ProcessDetector detector, ProcessAdj needToListenAdj) {
        if (!detector.isOOMScoreMode()) return needToListenAdj;

        AppMasterApplication context = AppMasterApplication.getInstance();
        if (needToListenAdj == null && context.isForeground()) {
            needToListenAdj = new ProcessAdj();
            needToListenAdj.pkg = context.getPackageName();
            needToListenAdj.pid = Process.myPid();
            if (DBG) {
                LeoLog.i(TAG, "foreground is null, swith to Leo, because of has resumed activity.");
            }
            return needToListenAdj;
        }
        
        if (needToListenAdj != null 
                && !context.getPackageName().equals(needToListenAdj.pkg)
                && context.isForeground()) {
            // 获取到的前台应用不是Leo，但通过Activity判断Leo又在前台，解决切换到Leo没有弹锁的问题
            needToListenAdj.pkg = context.getPackageName();
            needToListenAdj.pid = Process.myPid();
            if (DBG) {
                LeoLog.i(TAG, "foreground swith to Leo, because of has resumed activity.");
            }
        }
        
        return needToListenAdj;
    }

    @Override
    public void interrupt() {
        super.interrupt();
        // 解决锁屏再亮屏，已加锁app没有弹出锁界面的问题
        mLockHandler.handleAppLaunch("what ever.", "", "");
    }

    /**
     * 分发请求是否成功，失败的原因：获取自己的栈顶activity为空，貌似不会出现~~
     * @param processAdj
     * @return
     */
    private boolean parseAdjAndDeliver(ProcessAdj processAdj) {
        if (processAdj == null) return false;
        
        Context context = AppMasterApplication.getInstance();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        
        String pkg = processAdj.pkg;
        String activityName = "";
        String baseActivity = null;
        if (pkg.equals(context.getPackageName())) {
            List<RunningTaskInfo> runningTasks = am.getRunningTasks(1);
            if (runningTasks != null && runningTasks.size() > 0) {
                RunningTaskInfo taskInfo = runningTasks.get(0);
                ComponentName top = taskInfo.topActivity;
                // 有可能获取到的activity和pkg不属于同一个app
                // eg: com.leo.appmaster/com.google.android.launcher.GEL
                if (top == null || !pkg.equals(top.getPackageName())) return false;

                ComponentName topActivity = taskInfo.topActivity;
                activityName = topActivity.getShortClassName();
                if(taskInfo.baseActivity != null) {
                    baseActivity = taskInfo.baseActivity.getShortClassName();
                }
            }
        }
        mLockHandler.handleAppLaunch(pkg, activityName, baseActivity);
        
        return true;
    }
    
    private ProcessAdj findNeedToLockAndListenApp(ProcessDetector detector) {
        mFoundLockApp = false;
        List<ProcessAdj> filteredLockList = new ArrayList<ProcessAdj>();
        LockManager lm = (LockManager)MgrContext.getManager(MgrContext.MGR_APPLOCKER);
        List<String> lockList = lm.getCurLockList();
        // oom_adj为0的列表
        ProcessAdj needToListenAdj = detector.getForegroundProcess();

        needToListenAdj = checkSelfForeground(detector, needToListenAdj);
        if (needToListenAdj == null) return null;
        
        if (filteredLockList.isEmpty()) {
            for (String pkg : lockList) {
                if (pkg.equals(needToListenAdj.pkg)) {
                    mFoundLockApp = true;
                    break;
                }
            }
        }
        
        return needToListenAdj;
    }
    
    private void notifyInternal() {
        synchronized (this) {
            try {
                notify();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private String getHomePackage() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        
        Context context = AppMasterApplication.getInstance();
        ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);
        if (res == null) return null;
        
        return res.resolvePackageName;
    }
    
    private AdjFileObserver createAdjFileObserver(ProcessAdj processAdj) {
        String path = mDetector.getObservePath(processAdj);
        
        File file = new File(path);
        if (!file.exists()) return null;

        AdjFileObserver observer = new AdjFileObserver(path);
        
        return observer;
    }
    
    private class AdjFileObserver extends FileObserver {
        public static final int mask = MODIFY | ATTRIB 
                | MOVED_FROM | MOVED_TO | DELETE | CREATE
                | DELETE_SELF | MOVE_SELF;

        public AdjFileObserver(String path) {
            super(path, mask);
        }

        @Override
        public void onEvent(int event, String path) {
            if (DBG) {
                LeoLog.i(TAG, "AdjFileObserver, onEvent, event: " + event + " | path: " + path);
            }
            stopWatching();
            notifyInternal();
        }
        
    }

}
