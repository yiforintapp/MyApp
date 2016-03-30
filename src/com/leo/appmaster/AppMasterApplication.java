
package com.leo.appmaster;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.SystemClock;
import android.os.UserManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.bootstrap.Bootstrap;
import com.leo.appmaster.bootstrap.BootstrapGroup;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.ImageLoader;

public class AppMasterApplication extends Application {
    private static final String TAG = "AppMasterApplication";

    private static AppMasterApplication sInstance;
    private List<WeakReference<Activity>> mActivityList;
    private List<WeakReference<Activity>> mResumedList;
    private List<WeakReference<Activity>> mStartedList;
    public static boolean sCheckTs = true;
    public static long sAppOnCrate;
    public static boolean sIsSplashActioned = false;

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

    private Bootstrap mRootBootstrap;

    static {
        try {
            System.loadLibrary("leo_service");
            LeoLog.d(TAG, "load service lib succ.");
        } catch (Throwable e) {
            LeoLog.e(TAG, "load service lib ex.", e);
        }
    }

    private native void restartApplocker(int sdk, String userSerial);
    public native String[] getKeyArray();

    @Override
    public void onCreate() {
        super.onCreate();
        sAppOnCrate = SystemClock.elapsedRealtime();
        if (sInstance != null)
            return;

//        Debug.startMethodTracing("vivo1.trace");
        sInstance = this;
        // Use old sor
        try {
            System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        } catch (Exception e) {
        }
        long start = SystemClock.elapsedRealtime();
        LeoSettings.initialize();
        LeoLog.d(TAG, "initialize cost: " + (SystemClock.elapsedRealtime() - start));

        AppMasterPreference pref = AppMasterPreference.getInstance(this);
        String lastVer = pref.getLastVersion();
        try {
            sLastVersion = Integer.parseInt(lastVer);
        } catch (NumberFormatException e) {
            LeoLog.e(TAG, "parse last ver ex." + e.getMessage());
        }
        mActivityList = new ArrayList<WeakReference<Activity>>();
        mResumedList = new ArrayList<WeakReference<Activity>>();
        mStartedList = new ArrayList<WeakReference<Activity>>();
        mHandler = new Handler();

        sharedPreferences = getSharedPreferences("lockerTheme", Context.MODE_WORLD_WRITEABLE);
        usedThemePackage = sharedPreferences.getString("packageName", Constants.DEFAULT_THEME);

        // For android L and above, daemon service is not work, so disable it
        if (PhoneInfo.getAndroidVersion() < 20) {
            try {
                restartApplocker(PhoneInfo.getAndroidVersion(), getUserSerial());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
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

        mRootBootstrap = new BootstrapGroup();
        mRootBootstrap.mStepIds = mRootSteps;

        // 启动引导程序，包含：前台、后台、延时程序
        mRootBootstrap.execute();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private String getUserSerial() {
        String userSerial = null;
        if (PhoneInfo.getAndroidVersion() >= 17) {
            try {
                UserManager userManager = (UserManager) getSystemService(Context.USER_SERVICE);
                if (userManager != null) {
                    userSerial = String.valueOf(userManager
                            .getSerialNumberForUser(android.os.Process.myUserHandle()));
                }
            } catch (Exception e) {
            } catch (Error error) {
            }
        }
        return userSerial;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        ImageLoader.getInstance().clearMemoryCache();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        ImageLoader.getInstance().clearMemoryCache();
        SDKWrapper.endSession(this);
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
            if (activity != null && (activity instanceof HomeActivity)) {
                return true;
            }
        } else if (mActivityList.size() > 1) {
            WeakReference<Activity> reference0 = mActivityList.get(0);
            WeakReference<Activity> reference1 = mActivityList.get(1);
            Activity activity0 = reference0.get();
            Activity activity1 = reference1.get();
            if (activity0 != null && (activity0 instanceof HomeActivity)
                    && activity1 != null && (activity1 instanceof LockScreenActivity)) {
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
