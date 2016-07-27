package com.zlf.appmaster;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Debug;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.zlf.appmaster.Constants;
import com.zlf.appmaster.PhoneInfo;
import com.zlf.appmaster.ThreadManager;
import com.zlf.appmaster.bootstrap.Bootstrap;
import com.zlf.appmaster.bootstrap.BootstrapGroup;
import com.zlf.appmaster.home.HomeTestActivity;
import com.zlf.appmaster.utils.LeoLog;
import com.zlf.imageloader.ImageLoader;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AppMasterApplication extends Application {
    private static final String TAG = "AppMasterApplication";

    private static AppMasterApplication sInstance;
    private List<WeakReference<Activity>> mActivityList;
    private List<WeakReference<Activity>> mResumedList;
    private List<WeakReference<Activity>> mStartedList;
    public static boolean sCheckTs = true;
    public static long sAppOnCrate;
    public static boolean sIsSplashActioned = false;

    private static boolean sNeedRestart;

    public static boolean DGB_TRACE = false;
    public static final String TRACE = "MotoG11.trace";

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private static int[] mRootSteps = {
            Bootstrap.STEP_FOREGROUND,
            Bootstrap.STEP_BACKGROUND,
            Bootstrap.STEP_FOREGROUND_DELAY,
            Bootstrap.STEP_BACKGROUND_DELAY
    };

    public Handler mHandler;
    public static SharedPreferences sharedPreferences;
    public static String usedThemePackage;
    private static int sLastVersion;

    public static int sScreenWidth = 0;
    public static int sScreenHeight = 0;

    public static long sStartTs;

    private Bootstrap mRootBootstrap;

    static {
        try {
            System.loadLibrary("leo_service");
            LeoLog.d(TAG, "load service lib succ.");
        } catch (Exception e) {
            LeoLog.e(TAG, "load service lib ex.", e);
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        sAppOnCrate = SystemClock.elapsedRealtime();
        if (sInstance != null)
            return;

        if (DGB_TRACE) {
            Debug.startMethodTracing(TRACE);
        }
        sStartTs = SystemClock.elapsedRealtime();
        sInstance = this;

        // Use old sor
        try {
            System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        } catch (Exception e) {
        }
        long start = SystemClock.elapsedRealtime();

        ThreadManager.initialize();
        LeoLog.d(TAG, "initialize cost: " + (SystemClock.elapsedRealtime() - start));

        mActivityList = new ArrayList<WeakReference<Activity>>();
        mResumedList = new ArrayList<WeakReference<Activity>>();
        mStartedList = new ArrayList<WeakReference<Activity>>();
        mHandler = new Handler();

        sharedPreferences = getSharedPreferences("lockerTheme", Context.MODE_WORLD_WRITEABLE);
        usedThemePackage = sharedPreferences.getString("packageName", Constants.DEFAULT_THEME);

        initScreenSize();
        mRootBootstrap = new BootstrapGroup();
        mRootBootstrap.mStepIds = mRootSteps;

        // 启动引导程序，包含：前台、后台、延时程序
        mRootBootstrap.execute();
    }

    private void initScreenSize() {
        try {
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(metrics);
            sScreenWidth = metrics.widthPixels;
            sScreenHeight = metrics.heightPixels;
            LeoLog.d(TAG, "zany, width: " + sScreenWidth + " | height: " + sScreenHeight);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        try {
            ImageLoader.getInstance().clearMemoryCache();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        ImageLoader.getInstance().clearMemoryCache();
    }

    public boolean needRestart() {
        return sNeedRestart;
    }

    public void setNeedRestart(boolean needRestart) {
        sNeedRestart = needRestart;
    }

    public static AppMasterApplication getInstance() {
        return sInstance;
    }

    // for force update strategy to exit application completely
    public synchronized void createActivity(Activity activity) {
        // mActivityList.add(activity);
        Iterator<WeakReference<Activity>> iterator = mActivityList.iterator();
        while (iterator.hasNext()) {
            WeakReference<Activity> reference = iterator.next();
            Activity ac = reference.get();
            if (ac != null && ac == activity) {
                return;
            } else if (ac == null) {
                // 存放的activity已经被释放掉，移除引用
                iterator.remove();
            }
        }

        mActivityList.add(new WeakReference<Activity>(activity));
    }

    public synchronized void destroyActivity(Activity activity) {
        Iterator<WeakReference<Activity>> iterator = mActivityList.iterator();
        while (iterator.hasNext()) {
            WeakReference<Activity> reference = iterator.next();
            Activity ac = reference.get();
            if (ac == null || ac == activity) {
                // 移除掉已经被释放掉的ref
                iterator.remove();
            }
        }
    }

    public synchronized void exitApplication() {
        Iterator<WeakReference<Activity>> iterator = mActivityList.iterator();
        while (iterator.hasNext()) {
            WeakReference<Activity> reference = iterator.next();
            Activity ac = reference.get();
            if (ac != null) {
                ac.finish();
            }
            iterator.remove();
        }

    }

    /**
     * 添加resumed过的Activity
     *
     * @param activity
     */
    public synchronized void resumeActivity(Activity activity) {
        Iterator<WeakReference<Activity>> iterator = mResumedList.iterator();
        while (iterator.hasNext()) {
            WeakReference<Activity> reference = iterator.next();
            Activity ac = reference.get();
            if (ac != null && ac == activity) {
                return;
            } else if (ac == null) {
                // 存放的activity已经被释放掉，移除引用
                iterator.remove();
            }
        }

        mResumedList.add(new WeakReference<Activity>(activity));
    }

    public synchronized void pauseActivity(Activity activity) {
        Iterator<WeakReference<Activity>> iterator = mResumedList.iterator();
        while (iterator.hasNext()) {
            WeakReference<Activity> reference = iterator.next();
            Activity ac = reference.get();
            if (ac == null || ac == activity) {
                // 移除掉已经被释放掉的ref
                iterator.remove();
            }
        }
    }

    public synchronized void startActivity(Activity activity) {
        Iterator<WeakReference<Activity>> iterator = mStartedList.iterator();
        while (iterator.hasNext()) {
            WeakReference<Activity> reference = iterator.next();
            Activity ac = reference.get();
            if (ac != null && ac == activity) {
                return;
            } else if (ac == null) {
                // 存放的activity已经被释放掉，移除引用
                iterator.remove();
            }
        }

        mStartedList.add(new WeakReference<Activity>(activity));
    }

    public synchronized void stopActivity(Activity activity) {
        Iterator<WeakReference<Activity>> iterator = mStartedList.iterator();
        while (iterator.hasNext()) {
            WeakReference<Activity> reference = iterator.next();
            Activity ac = reference.get();
            if (ac == null || ac == activity) {
                // 移除掉已经被释放掉的ref
                iterator.remove();
            }
        }
    }

    /**
     * 是否在前台
     *
     * @return
     */
    public boolean isForeground() {
        for (WeakReference<Activity> weakReference : mResumedList) {
            if (weakReference.get() != null) {
                return true;
            }
        }

        return false;
    }

    public boolean isVisible() {
        for (WeakReference<Activity> weakReference : mStartedList) {
            if (weakReference.get() != null) {
                return true;
            }
        }

        return false;
    }

    /**
     * home是否在栈顶，即只有Home还存活, 用户在首页点击home键回桌面
     *
     * @return
     */
    public boolean isHomeOnTopAndBackground() {
        if (!mStartedList.isEmpty() || mActivityList.isEmpty()) {
            // resume list 不为空，说明在前台
            // activity list 为空，说明所有activity被销毁
            return false;
        }

        if (mActivityList.size() == 1) {
            WeakReference<Activity> reference = mActivityList.get(0);
            Activity activity = reference.get();
            if (activity != null && (activity instanceof HomeTestActivity)) {
                return true;
            }
        } else if (mActivityList.size() > 1) {
            WeakReference<Activity> reference0 = mActivityList.get(0);
            WeakReference<Activity> reference1 = mActivityList.get(1);
            Activity activity0 = reference0.get();
            Activity activity1 = reference1.get();
            if (activity0 != null && (activity0 instanceof HomeTestActivity)) {
                return true;
            }
        }

        return false;
    }

    public Activity getTopActivity() {
        for (int i = mResumedList.size() - 1; i >= 0; i--) {
            WeakReference<Activity> top = mResumedList.get(i);
            if (top != null) {
                Activity activity = top.get();
                if (activity != null) {
                    return activity;
                }
            }
        }

        return null;
    }

    public static void setSharedPreferencesValue(String lockerTheme) {
        Editor editor = sharedPreferences.edit();
        editor.putString("packageName", lockerTheme);
        editor.apply();
        usedThemePackage = lockerTheme;
    }

    public static String getSelectedTheme() {
        return usedThemePackage;
    }

    /**
     * 是否是升级
     * 注意：进程起来后，此接口会一直有效，除非进程挂掉
     *
     * @return
     */
    public static boolean isAppUpgrade() {
        int versionCode = PhoneInfo.getVersionCode(sInstance);
        return versionCode > sLastVersion;
    }

}
